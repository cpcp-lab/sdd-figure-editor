import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class Polygon extends Figure {
    private final List<Integer> xs = new ArrayList<>();
    private final List<Integer> ys = new ArrayList<>();

    private static final double HIT_THRESHOLD = 5.0;

    public Polygon(Color strokeColor, Color fillColor) {
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
        if (xs.size() < 3) return;
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
            g.drawPolygon(xa, ya, n);
        }
    }

    @Override
    public void move(int dx, int dy) {
        xs.replaceAll(x -> x + dx);
        ys.replaceAll(y -> y + dy);
    }

    @Override
    public boolean contains(int px, int py) {
        int n = xs.size();
        if (n < 3) return false;
        boolean inside = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            int xi = xs.get(i), yi = ys.get(i);
            int xj = xs.get(j), yj = ys.get(j);
            if ((yi > py) != (yj > py)
                    && px < (double)(xj - xi) * (py - yi) / (yj - yi) + xi) {
                inside = !inside;
            }
        }
        return inside;
    }

    @Override
    public String toSvg() {
        StringJoiner pts = new StringJoiner(" ");
        for (int i = 0; i < xs.size(); i++) pts.add(xs.get(i) + "," + ys.get(i));
        return String.format("<polygon points=\"%s\" %s/>", pts, strokeAttrs());
    }
}
