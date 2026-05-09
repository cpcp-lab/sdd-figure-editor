import java.awt.Color;

public class DrawEllipseTool implements Tool {
    private final Canvas canvas;
    private Ellipse preview;

    public DrawEllipseTool(Canvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public void onPress(int x, int y) {
        preview = new Ellipse(x, y, x, y, Color.BLACK, null);
        canvas.setPreview(preview);
    }

    @Override
    public void onDrag(int x, int y) {
        if (preview != null) {
            preview.setEndCorner(x, y);
            canvas.repaint();
        }
    }

    @Override
    public void onRelease(int x, int y) {
        if (preview != null) {
            preview.setEndCorner(x, y);
            canvas.addFigure(preview);
            canvas.setPreview(null);
            preview = null;
        }
    }
}
