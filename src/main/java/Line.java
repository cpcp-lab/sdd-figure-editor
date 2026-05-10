import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Line extends Figure {
    private double x1, y1, x2, y2;

    private static final double HIT_THRESHOLD = 5.0;

    public Line(int x1, int y1, int x2, int y2) {
        super(Color.BLACK, null);
        this.x1 = x1; this.y1 = y1;
        this.x2 = x2; this.y2 = y2;
    }

    public Line(int x1, int y1, int x2, int y2, Color strokeColor) {
        super(strokeColor, null);
        this.x1 = x1; this.y1 = y1;
        this.x2 = x2; this.y2 = y2;
    }

    public Line(double x1, double y1, double x2, double y2, Color strokeColor) {
        super(strokeColor, null);
        this.x1 = x1; this.y1 = y1;
        this.x2 = x2; this.y2 = y2;
    }

    @Override
    protected void drawShape(Graphics2D g2) {
        if (strokeColor != null) {
            applyStroke(g2);
            g2.setColor(strokeColor);
            g2.draw(new Line2D.Double(x1, y1, x2, y2));
        }
    }

    @Override
    public void move(int dx, int dy) {
        x1 += dx; y1 += dy;
        x2 += dx; y2 += dy;
    }

    @Override
    protected boolean containsLocal(int x, int y) {
        return pointToSegmentDistance(x, y) < HIT_THRESHOLD;
    }

    @Override
    public List<Handle> getHandles() {
        List<Handle> handles = new ArrayList<>();
        Point2D s1 = toScreen(x1, y1);
        Point2D s2 = toScreen(x2, y2);
        handles.add(new Handle((int) Math.round(s1.getX()), (int) Math.round(s1.getY()),
            Handle.Type.ENDPOINT, (nx, ny) -> {
                Point2D lp = toLocal(nx, ny);
                x1 = lp.getX(); y1 = lp.getY();
            }));
        handles.add(new Handle((int) Math.round(s2.getX()), (int) Math.round(s2.getY()),
            Handle.Type.ENDPOINT, (nx, ny) -> {
                Point2D lp = toLocal(nx, ny);
                x2 = lp.getX(); y2 = lp.getY();
            }));
        return handles;
    }

    @Override
    public String toSvg() {
        return String.format("<line x1=\"%s\" y1=\"%s\" x2=\"%s\" y2=\"%s\" %s%s/>",
            fmt(x1), fmt(y1), fmt(x2), fmt(y2), strokeAttrs(), transformAttr());
    }

    @Override
    public List<Figure> bakeTransform() {
        Point2D p1 = toScreen(x1, y1);
        Point2D p2 = toScreen(x2, y2);
        x1 = p1.getX(); y1 = p1.getY();
        x2 = p2.getX(); y2 = p2.getY();
        transform.setToIdentity();
        return List.of(this);
    }

    public void setEndPoint(int x2, int y2) {
        this.x2 = x2;
        this.y2 = y2;
    }

    public int getX1() { return (int) Math.round(x1); }
    public int getY1() { return (int) Math.round(y1); }
    public int getX2() { return (int) Math.round(x2); }
    public int getY2() { return (int) Math.round(y2); }

    private static String fmt(double v) {
        return (v == (long) v) ? String.valueOf((long) v) : String.valueOf(v);
    }

    private double pointToSegmentDistance(int px, int py) {
        double dx = x2 - x1, dy = y2 - y1;
        double lenSq = dx * dx + dy * dy;
        if (lenSq == 0.0) return Math.hypot(px - x1, py - y1);
        double t = Math.max(0.0, Math.min(1.0, ((px - x1) * dx + (py - y1) * dy) / lenSq));
        return Math.hypot(px - (x1 + t * dx), py - (y1 + t * dy));
    }
}
