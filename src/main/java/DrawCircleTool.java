import java.awt.Color;

public class DrawCircleTool implements Tool {
    private final Canvas canvas;
    private Circle preview;
    private int pressX, pressY;

    public DrawCircleTool(Canvas canvas) {
        this.canvas = canvas;
    }

    @Override public void onActivate() { canvas.clearSelection(); }

    @Override
    public void onPress(int x, int y) {
        pressX = x; pressY = y;
        preview = new Circle(x, y, 0, canvas.getCurrentStrokeColor(), canvas.getCurrentFillColor());
        preview.setStrokeWidth(canvas.getCurrentStrokeWidth());
        canvas.setPreview(preview);
    }

    @Override
    public void onDrag(int x, int y) {
        if (preview != null) {
            preview.setRadius((int) Math.hypot(x - pressX, y - pressY));
            canvas.repaint();
        }
    }

    @Override
    public void onRelease(int x, int y) {
        if (preview != null) {
            preview.setRadius((int) Math.hypot(x - pressX, y - pressY));
            canvas.addFigure(preview);
            canvas.setPreview(null);
            preview = null;
        }
    }
}
