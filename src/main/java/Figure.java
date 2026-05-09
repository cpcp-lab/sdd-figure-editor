import java.awt.Color;
import java.awt.Graphics;

public abstract class Figure {
    protected Color strokeColor;
    protected Color fillColor;

    protected Figure(Color strokeColor, Color fillColor) {
        this.strokeColor = strokeColor;
        this.fillColor = fillColor;
    }

    public abstract void draw(Graphics g);
    public abstract void move(int dx, int dy);
    public abstract boolean contains(int x, int y);
}
