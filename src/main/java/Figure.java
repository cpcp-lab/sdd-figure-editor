import java.awt.Graphics;

public abstract class Figure {
    public abstract void draw(Graphics g);
    public abstract void move(int dx, int dy);
    public abstract boolean contains(int x, int y);
}
