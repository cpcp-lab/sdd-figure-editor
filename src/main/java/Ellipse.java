import java.awt.Color;
import java.awt.Graphics;

public class Ellipse extends Figure {
    private int x1, y1, x2, y2;

    public Ellipse(int x1, int y1, int x2, int y2, Color strokeColor, Color fillColor) {
        super(strokeColor, fillColor);
        this.x1 = x1; this.y1 = y1;
        this.x2 = x2; this.y2 = y2;
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
    public void draw(Graphics g) {
        int x = left(), y = top(), w = width(), h = height();
        if (fillColor != null) {
            g.setColor(fillColor);
            g.fillOval(x, y, w, h);
        }
        if (strokeColor != null) {
            g.setColor(strokeColor);
            g.drawOval(x, y, w, h);
        }
    }

    @Override
    public void move(int dx, int dy) {
        x1 += dx; y1 += dy;
        x2 += dx; y2 += dy;
    }

    @Override
    public boolean contains(int x, int y) {
        int w = width(), h = height();
        if (w == 0 || h == 0) return false;
        double dx = x - (left() + w / 2.0);
        double dy = y - (top()  + h / 2.0);
        double a = w / 2.0, b = h / 2.0;
        return (dx * dx) / (a * a) + (dy * dy) / (b * b) <= 1.0;
    }

    public int getX1() { return x1; }
    public int getY1() { return y1; }
    public int getX2() { return x2; }
    public int getY2() { return y2; }
}
