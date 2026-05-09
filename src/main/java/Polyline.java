import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class Polyline extends Figure {
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
    public void draw(Graphics g) {
        if (xs.size() < 2) return;
        int n = xs.size();
        int[] xa = xs.stream().mapToInt(v -> v).toArray();
        int[] ya = ys.stream().mapToInt(v -> v).toArray();
        applyStroke(g);
        if (fillColor != null) {
            g.setColor(fillColor);
            g.fillPolygon(xa, ya, n);
        }
        if (strokeColor != null) {
            g.setColor(strokeColor);
            g.drawPolyline(xa, ya, n);
        }
    }

    @Override
    public void move(int dx, int dy) {
        xs.replaceAll(x -> x + dx);
        ys.replaceAll(y -> y + dy);
    }

    @Override
    public boolean contains(int px, int py) {
        for (int i = 0; i < xs.size() - 1; i++) {
            if (segmentDist(px, py, xs.get(i), ys.get(i), xs.get(i + 1), ys.get(i + 1)) < HIT_THRESHOLD)
                return true;
        }
        return false;
    }

    @Override
    public String toSvg() {
        StringJoiner pts = new StringJoiner(" ");
        for (int i = 0; i < xs.size(); i++) pts.add(xs.get(i) + "," + ys.get(i));
        return String.format("<polyline points=\"%s\" %s/>", pts, strokeAttrs());
    }

    private static double segmentDist(int px, int py, int x1, int y1, int x2, int y2) {
        double dx = x2 - x1, dy = y2 - y1;
        double lenSq = dx * dx + dy * dy;
        if (lenSq == 0.0) return Math.hypot(px - x1, py - y1);
        double t = Math.max(0.0, Math.min(1.0, ((px - x1) * dx + (py - y1) * dy) / lenSq));
        return Math.hypot(px - (x1 + t * dx), py - (y1 + t * dy));
    }
}
