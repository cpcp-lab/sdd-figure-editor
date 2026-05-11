import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.awt.geom.AffineTransform;
import java.util.StringJoiner;

public class Polyline extends Figure implements Rotatable {
    private final List<Integer> xs = new ArrayList<>();
    private final List<Integer> ys = new ArrayList<>();

    private static final double HIT_THRESHOLD = 5.0;

    public Polyline(Color strokeColor) {
        super(strokeColor, null);
    }

    public Polyline(Color strokeColor, Color fillColor) {
        super(strokeColor, fillColor);
    }

    public void addPoint(int x, int y) {
        xs.add(x);
        ys.add(y);
    }

    public void setLastPoint(int x, int y) {
        if (!xs.isEmpty()) {
            xs.set(xs.size() - 1, x);
            ys.set(ys.size() - 1, y);
        }
    }

    public void removeLastPoint() {
        if (!xs.isEmpty()) {
            xs.remove(xs.size() - 1);
            ys.remove(ys.size() - 1);
        }
    }

    public int getPointCount()  { return xs.size(); }
    public int getPointX(int i) { return xs.get(i); }
    public int getPointY(int i) { return ys.get(i); }

    @Override
    protected void drawShape(Graphics2D g2) {
        if (xs.size() < 2) return;
        int n = xs.size();
        int[] xa = xs.stream().mapToInt(v -> v).toArray();
        int[] ya = ys.stream().mapToInt(v -> v).toArray();
        applyStroke(g2);
        if (fillColor != null) {
            g2.setColor(fillColor);
            g2.fillPolygon(xa, ya, n);
        }
        if (strokeColor != null) {
            g2.setColor(strokeColor);
            g2.drawPolyline(xa, ya, n);
        }
    }

    @Override
    public void move(int dx, int dy) {
        xs.replaceAll(x -> x + dx);
        ys.replaceAll(y -> y + dy);
    }

    @Override
    protected boolean containsLocal(int px, int py) {
        for (int i = 0; i < xs.size() - 1; i++) {
            if (segmentDist(px, py, xs.get(i), ys.get(i), xs.get(i + 1), ys.get(i + 1)) < HIT_THRESHOLD)
                return true;
        }
        return false;
    }

    @Override
    public List<Handle> getHandles() {
        List<Handle> handles = new ArrayList<>();
        for (int i = 0; i < xs.size(); i++) {
            final int idx = i;
            Point2D sc = toScreen(xs.get(i), ys.get(i));
            handles.add(new Handle((int) Math.round(sc.getX()), (int) Math.round(sc.getY()),
                Handle.Type.ENDPOINT, (nx, ny) -> {
                    Point2D lp = toLocal(nx, ny);
                    xs.set(idx, (int) Math.round(lp.getX()));
                    ys.set(idx, (int) Math.round(lp.getY()));
                }));
        }
        return handles;
    }

    @Override
    public void rotate(double theta, double cx, double cy) {
        transform.preConcatenate(AffineTransform.getRotateInstance(theta, cx, cy));
    }

    @Override
    public List<Figure> bakeTransform() {
        strokeWidth *= (float) strokeScale();
        for (int i = 0; i < xs.size(); i++) {
            Point2D p = toScreen(xs.get(i), ys.get(i));
            xs.set(i, (int) Math.round(p.getX()));
            ys.set(i, (int) Math.round(p.getY()));
        }
        transform.setToIdentity();
        return List.of(this);
    }

    @Override
    public String toSvg() {
        StringJoiner pts = new StringJoiner(" ");
        for (int i = 0; i < xs.size(); i++) pts.add(xs.get(i) + "," + ys.get(i));
        return String.format("<polyline points=\"%s\" %s%s/>", pts, strokeAttrs(), transformAttr());
    }

    private static double segmentDist(int px, int py, int x1, int y1, int x2, int y2) {
        double dx = x2 - x1, dy = y2 - y1;
        double lenSq = dx * dx + dy * dy;
        if (lenSq == 0.0) return Math.hypot(px - x1, py - y1);
        double t = Math.max(0.0, Math.min(1.0, ((px - x1) * dx + (py - y1) * dy) / lenSq));
        return Math.hypot(px - (x1 + t * dx), py - (y1 + t * dy));
    }
}
