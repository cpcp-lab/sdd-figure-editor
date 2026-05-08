public class DrawLineTool implements Tool {
    private final Canvas canvas;
    private Line preview;

    public DrawLineTool(Canvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public void onPress(int x, int y) {
        preview = new Line(x, y, x, y);
        canvas.setPreview(preview);
    }

    @Override
    public void onDrag(int x, int y) {
        if (preview != null) {
            preview.setEndPoint(x, y);
            canvas.repaint();
        }
    }

    @Override
    public void onRelease(int x, int y) {
        if (preview != null) {
            preview.setEndPoint(x, y);
            canvas.addFigure(preview);
            canvas.setPreview(null);
            preview = null;
        }
    }
}
