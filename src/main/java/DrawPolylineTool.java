import java.awt.Color;

public class DrawPolylineTool implements Tool {
    private final Canvas canvas;
    private Polyline preview;

    public DrawPolylineTool(Canvas canvas) {
        this.canvas = canvas;
    }

    @Override public void onActivate() { canvas.clearSelection(); }
    @Override public void onPress(int x, int y) {}
    @Override public void onDrag(int x, int y) {}
    @Override public void onRelease(int x, int y) {}

    @Override
    public void onClick(int x, int y) {
        if (preview == null) {
            preview = new Polyline(canvas.getCurrentStrokeColor(), canvas.getCurrentFillColor());
            preview.setStrokeWidth(canvas.getCurrentStrokeWidth());
            preview.addPoint(x, y);
            preview.addPoint(x, y); // rubber-band point
            canvas.setPreview(preview);
        } else {
            preview.setLastPoint(x, y); // confirm rubber-band
            preview.addPoint(x, y);     // new rubber-band
        }
        canvas.repaint();
    }

    @Override
    public void onMove(int x, int y) {
        if (preview != null) {
            preview.setLastPoint(x, y);
            canvas.repaint();
        }
    }

    @Override
    public void onDoubleClick(int x, int y) {
        if (preview != null) {
            preview.removeLastPoint(); // remove rubber-band
            canvas.addFigure(preview);
            canvas.setPreview(null);
            preview = null;
        }
    }
}
