import java.awt.event.MouseEvent;

public class DrawLineTool implements Tool {
    private final Canvas canvas;
    private Line preview;
    private int startX, startY;

    public DrawLineTool(Canvas canvas) {
        this.canvas = canvas;
    }

    @Override public void onActivate() { canvas.clearSelection(); }

    @Override
    public void onPress(int x, int y) {
        startX = x; startY = y;
        preview = new Line(x, y, x, y, canvas.getCurrentStrokeColor());
        preview.setStrokeWidth(canvas.getCurrentStrokeWidth());
        canvas.setPreview(preview);
    }

    @Override
    public void onDragEvent(MouseEvent e) {
        if (preview == null) return;
        int x = e.getX(), y = e.getY();
        if (e.isShiftDown()) {
            int[] s = angleSnap(x, y, startX, startY);
            x = s[0]; y = s[1];
        }
        preview.setEndPoint(x, y);
        canvas.repaint();
    }

    @Override
    public void onDrag(int x, int y) {
        // onDragEvent で処理するため使用しない
    }

    @Override
    public void onReleaseEvent(MouseEvent e) {
        if (preview == null) return;
        int x = e.getX(), y = e.getY();
        if (e.isShiftDown()) {
            int[] s = angleSnap(x, y, startX, startY);
            x = s[0]; y = s[1];
        }
        preview.setEndPoint(x, y);
        canvas.addFigure(preview);
        canvas.setPreview(null);
        preview = null;
    }

    @Override
    public void onRelease(int x, int y) {
        // onReleaseEvent で処理するため使用しない
    }

    private static int[] angleSnap(int x, int y, int ax, int ay) {
        double dx = x - ax, dy = y - ay;
        double r = Math.hypot(dx, dy);
        double snap = Math.PI / 12; // 15° (24等分)
        double angle = Math.round(Math.atan2(dy, dx) / snap) * snap;
        return new int[]{ax + (int) Math.round(r * Math.cos(angle)),
                         ay + (int) Math.round(r * Math.sin(angle))};
    }
}
