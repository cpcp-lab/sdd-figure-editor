import java.awt.Color;
import java.awt.Graphics;

public class Rectangle extends Figure {
    private int x1, y1, x2, y2;
    private int rx = 0, ry = 0;

    public Rectangle(int x1, int y1, int x2, int y2, Color strokeColor, Color fillColor) {
        super(strokeColor, fillColor);
        this.x1 = x1; this.y1 = y1;
        this.x2 = x2; this.y2 = y2;
    }

    public void setRoundedCorners(int rx, int ry) {
        this.rx = rx;
        this.ry = ry;
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
        applyStroke(g);
        if (rx > 0 || ry > 0) {
            // SVG rx/ry を Java の arc 幅 (直径) に変換してクランプ
            int arcW = 2 * Math.min(rx, w / 2);
            int arcH = 2 * Math.min(ry, h / 2);
            if (fillColor != null) {
                g.setColor(fillColor);
                g.fillRoundRect(x, y, w, h, arcW, arcH);
            }
            if (strokeColor != null) {
                g.setColor(strokeColor);
                g.drawRoundRect(x, y, w, h, arcW, arcH);
            }
        } else {
            if (fillColor != null) {
                g.setColor(fillColor);
                g.fillRect(x, y, w, h);
            }
            if (strokeColor != null) {
                g.setColor(strokeColor);
                g.drawRect(x, y, w, h);
            }
        }
    }

    @Override
    public void move(int dx, int dy) {
        x1 += dx; y1 += dy;
        x2 += dx; y2 += dy;
    }

    @Override
    public boolean contains(int x, int y) {
        return left() <= x && x <= left() + width()
            && top() <= y && y <= top() + height();
    }

    @Override
    public String toSvg() {
        String rxry = (rx > 0 || ry > 0)
            ? String.format(" rx=\"%d\" ry=\"%d\"", rx, ry) : "";
        return String.format("<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\"%s %s/>",
            left(), top(), width(), height(), rxry, strokeAttrs());
    }

    public int getX1() { return x1; }
    public int getY1() { return y1; }
    public int getX2() { return x2; }
    public int getY2() { return y2; }
}
