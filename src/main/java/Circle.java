import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.awt.geom.AffineTransform;

public class Circle extends Figure {
    private int cx, cy, radius;

    public Circle(int cx, int cy, int radius, Color strokeColor, Color fillColor) {
        super(strokeColor, fillColor);
        this.cx = cx; this.cy = cy;
        this.radius = Math.max(0, radius);
    }

    public void setRadius(int radius) {
        this.radius = Math.max(0, radius);
    }

    @Override
    protected void drawShape(Graphics2D g2) {
        int x = cx - radius, y = cy - radius, d = 2 * radius;
        applyStroke(g2);
        if (fillColor != null) {
            g2.setColor(fillColor);
            g2.fillOval(x, y, d, d);
        }
        if (strokeColor != null) {
            g2.setColor(strokeColor);
            g2.drawOval(x, y, d, d);
        }
    }

    @Override
    public void move(int dx, int dy) {
        cx += dx; cy += dy;
    }

    @Override
    protected boolean containsLocal(int x, int y) {
        return Math.hypot(x - cx, y - cy) <= radius;
    }

    @Override
    public List<Figure> bakeTransform() {
        double sx = Math.hypot(transform.getScaleX(), transform.getShearY());
        double sy = Math.hypot(transform.getShearX(), transform.getScaleY());
        Point2D center = toScreen(cx, cy);
        int ncx = (int) Math.round(center.getX());
        int ncy = (int) Math.round(center.getY());
        if (Math.abs(sx - sy) < 1e-4) {
            // 均一スケール: Circle のまま bake
            cx = ncx; cy = ncy;
            radius = Math.max(0, (int) Math.round(radius * (sx + sy) / 2));
            transform.setToIdentity();
            return List.of(this);
        } else {
            // 非均一スケール: Ellipse に変換し bakeTransform() で残余成分も焼き込む
            int rx = Math.max(0, (int) Math.round(radius * sx));
            int ry = Math.max(0, (int) Math.round(radius * sy));
            Ellipse e = Ellipse.fromCenter(ncx, ncy, rx, ry, strokeColor, fillColor);
            e.setStrokeWidth(strokeWidth);
            double angle = Math.atan2(transform.getShearY(), transform.getScaleX());
            if (Math.abs(angle) > 1e-6) {
                e.getTransform().setToRotation(angle, ncx, ncy);
            }
            return e.bakeTransform();
        }
    }

    @Override
    public List<Handle> getHandles() {
        List<Handle> handles = new ArrayList<>();
        // Radius handle at right edge (cx+radius, cy)
        Point2D sc = toScreen(cx + radius, cy);
        handles.add(new Handle((int) Math.round(sc.getX()), (int) Math.round(sc.getY()),
            Handle.Type.ENDPOINT, (nx, ny) -> {
                Point2D lp = toLocal(nx, ny);
                radius = Math.max(0, (int) Math.round(Math.hypot(lp.getX() - cx, lp.getY() - cy)));
            }));
        return handles;
    }

    @Override
    public String toSvg() {
        return String.format("<circle cx=\"%d\" cy=\"%d\" r=\"%d\" %s%s/>",
            cx, cy, radius, strokeAttrs(), transformAttr());
    }

    public int getCx()     { return cx; }
    public int getCy()     { return cy; }
    public int getRadius() { return radius; }
}
