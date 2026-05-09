import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public abstract class Figure {
    protected Color strokeColor;
    protected Color fillColor;
    protected float strokeWidth = 1.0f;

    protected Figure(Color strokeColor, Color fillColor) {
        this.strokeColor = strokeColor;
        this.fillColor = fillColor;
    }

    public void setStrokeWidth(float w) { strokeWidth = w; }
    public float getStrokeWidth() { return strokeWidth; }

    protected void applyStroke(Graphics g) {
        if (g instanceof Graphics2D g2) {
            g2.setStroke(new BasicStroke(strokeWidth));
        }
    }

    protected String strokeAttrs() {
        String sw = (strokeWidth == (int) strokeWidth)
            ? String.valueOf((int) strokeWidth)
            : String.valueOf(strokeWidth);
        return String.format("fill=\"%s\" stroke=\"%s\" stroke-width=\"%s\"",
            SvgColor.toSvg(fillColor), SvgColor.toSvg(strokeColor), sw);
    }

    public abstract void draw(Graphics g);
    public abstract void move(int dx, int dy);
    public abstract boolean contains(int x, int y);
    public abstract String toSvg();
}
