import java.awt.Color;
import java.awt.Graphics;

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
    public void draw(Graphics g) {
        int x = cx - radius, y = cy - radius, d = 2 * radius;
        if (fillColor != null) {
            g.setColor(fillColor);
            g.fillOval(x, y, d, d);
        }
        if (strokeColor != null) {
            g.setColor(strokeColor);
            g.drawOval(x, y, d, d);
        }
    }

    @Override
    public void move(int dx, int dy) {
        cx += dx; cy += dy;
    }

    @Override
    public boolean contains(int x, int y) {
        return Math.hypot(x - cx, y - cy) <= radius;
    }

    public int getCx()     { return cx; }
    public int getCy()     { return cy; }
    public int getRadius() { return radius; }
}
