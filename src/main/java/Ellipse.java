import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Ellipse extends Figure implements Rotatable {
    private int x1, y1, x2, y2;

    public Ellipse(int x1, int y1, int x2, int y2, Color strokeColor, Color fillColor) {
        super(strokeColor, fillColor);
        this.x1 = x1; this.y1 = y1;
        this.x2 = x2; this.y2 = y2;
    }

    public static Ellipse fromCenter(int cx, int cy, int rx, int ry,
                                     Color strokeColor, Color fillColor) {
        return new Ellipse(cx - rx, cy - ry, cx + rx, cy + ry, strokeColor, fillColor);
    }

    public static Ellipse fromCenter(int cx, int cy, int r,
                                     Color strokeColor, Color fillColor) {
        return fromCenter(cx, cy, r, r, strokeColor, fillColor);
    }

    public void setEndCorner(int x2, int y2) {
        this.x2 = x2;
        this.y2 = y2;
    }

    private int left()   { return Math.min(x1, x2); }
    private int top()    { return Math.min(y1, y2); }
    private int width()  { return Math.abs(x2 - x1); }
    private int height() { return Math.abs(y2 - y1); }

    @Override
    protected void drawShape(Graphics2D g2) {
        int x = left(), y = top(), w = width(), h = height();
        applyStroke(g2);
        if (fillColor != null) {
            g2.setColor(fillColor);
            g2.fillOval(x, y, w, h);
        }
        if (strokeColor != null) {
            g2.setColor(strokeColor);
            g2.drawOval(x, y, w, h);
        }
    }

    @Override
    public void move(int dx, int dy) {
        x1 += dx; y1 += dy;
        x2 += dx; y2 += dy;
    }

    @Override
    protected boolean containsLocal(int x, int y) {
        int w = width(), h = height();
        if (w == 0 || h == 0) return false;
        double dx = x - (left() + w / 2.0);
        double dy = y - (top()  + h / 2.0);
        double a = w / 2.0, b = h / 2.0;
        return (dx * dx) / (a * a) + (dy * dy) / (b * b) <= 1.0;
    }

    @Override
    public void rotate(double theta, double cx, double cy) {
        transform.preConcatenate(AffineTransform.getRotateInstance(theta, cx, cy));
    }

    @Override
    public List<Figure> bakeTransform() {
        double sx = Math.hypot(transform.getScaleX(), transform.getShearY());
        double sy = Math.hypot(transform.getShearX(), transform.getScaleY());
        strokeWidth *= (float) Math.sqrt(sx * sy);
        double angle = Math.atan2(transform.getShearY(), transform.getScaleX());
        Point2D sc = toScreen((x1 + x2) / 2.0, (y1 + y2) / 2.0);
        int ncx = (int) Math.round(sc.getX());
        int ncy = (int) Math.round(sc.getY());
        int hrx = (int) Math.round(width()  / 2.0 * sx);
        int hry = (int) Math.round(height() / 2.0 * sy);
        x1 = ncx - hrx; y1 = ncy - hry;
        x2 = ncx + hrx; y2 = ncy + hry;
        if (Math.abs(angle) > 1e-9)
            transform.setToRotation(angle, ncx, ncy);
        else
            transform.setToIdentity();
        return List.of(this);
    }

    @Override
    public List<Handle> getHandles() {
        int[][] lc = {{x1, y1}, {x2, y1}, {x2, y2}, {x1, y2}};
        List<Handle> handles = new ArrayList<>();
        for (int[] c : lc) {
            final int lx = c[0], ly = c[1];
            final boolean isX1Side = (lx == x1);
            final boolean isY1Side = (ly == y1);
            Point2D sc = toScreen(lx, ly);
            handles.add(new Handle((int) Math.round(sc.getX()), (int) Math.round(sc.getY()),
                Handle.Type.ENDPOINT, (nx, ny) -> {
                    Point2D lp = toLocal(nx, ny);
                    if (isX1Side) x1 = (int) Math.round(lp.getX());
                    else          x2 = (int) Math.round(lp.getX());
                    if (isY1Side) y1 = (int) Math.round(lp.getY());
                    else          y2 = (int) Math.round(lp.getY());
                }));
        }
        return handles;
    }

    @Override
    public String toSvg() {
        int cx = (x1 + x2) / 2, cy = (y1 + y2) / 2;
        int rx = width() / 2,   ry = height() / 2;
        return String.format("<ellipse cx=\"%d\" cy=\"%d\" rx=\"%d\" ry=\"%d\" %s%s/>",
            cx, cy, rx, ry, strokeAttrs(), transformAttr());
    }

    public int getX1() { return x1; }
    public int getY1() { return y1; }
    public int getX2() { return x2; }
    public int getY2() { return y2; }
}
