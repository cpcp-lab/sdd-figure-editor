import java.awt.Color;
import java.awt.Graphics;

public class Line extends Figure {
    private int x1, y1, x2, y2;

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

    @Override
    public void draw(Graphics g) {
        if (strokeColor != null) g.setColor(strokeColor);
        g.drawLine(x1, y1, x2, y2);
    }

    @Override
    public void move(int dx, int dy) {
        x1 += dx; y1 += dy;
        x2 += dx; y2 += dy;
    }

    @Override
    public boolean contains(int x, int y) {
        return pointToSegmentDistance(x, y) < HIT_THRESHOLD;
    }

    public void setEndPoint(int x2, int y2) {
        this.x2 = x2;
        this.y2 = y2;
    }

    public int getX1() { return x1; }
    public int getY1() { return y1; }
    public int getX2() { return x2; }
    public int getY2() { return y2; }

    private double pointToSegmentDistance(int px, int py) {
        double dx = x2 - x1, dy = y2 - y1;
        double lenSq = dx * dx + dy * dy;
        if (lenSq == 0.0) return Math.hypot(px - x1, py - y1);
        double t = Math.max(0.0, Math.min(1.0, ((px - x1) * dx + (py - y1) * dy) / lenSq));
        return Math.hypot(px - (x1 + t * dx), py - (y1 + t * dy));
    }
}
