import java.awt.Rectangle;
import java.awt.event.MouseEvent;

public class SelectTool implements Tool {
    private final Canvas canvas;
    private Figure dragTarget;   // クリックで掴んだ図形 (移動用)
    private int pressX, pressY;
    private int lastX, lastY;
    private boolean rubberBanding; // ラバーバンドモード中

    public SelectTool(Canvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public void onPressEvent(MouseEvent e) {
        pressX = e.getX();
        pressY = e.getY();
        lastX  = e.getX();
        lastY  = e.getY();
        dragTarget = canvas.figureAt(pressX, pressY);
        rubberBanding = false;

        if (dragTarget != null) {
            // すでに選択中の図形をクリックした場合は選択を維持してドラッグ移動に備える
            // 選択外の図形をクリックした場合は選択を切り替える (Ctrl で追加)
            if (!canvas.getSelection().contains(dragTarget)) {
                canvas.select(dragTarget, e.isControlDown());
            }
        } else {
            // 空白クリック: 選択解除してラバーバンド開始準備
            if (!e.isControlDown()) canvas.clearSelection();
        }
    }

    @Override
    public void onPress(int x, int y) {}

    @Override
    public void onDrag(int x, int y) {
        if (dragTarget != null) {
            // 選択図形を移動
            for (Figure f : canvas.getSelection()) f.move(x - lastX, y - lastY);
            canvas.repaint();
        } else {
            // ラバーバンド矩形を更新
            rubberBanding = true;
            canvas.setSelectionRect(makeRect(pressX, pressY, x, y));
        }
        lastX = x;
        lastY = y;
    }

    @Override
    public void onRelease(int x, int y) {
        if (rubberBanding) {
            Rectangle rect = makeRect(pressX, pressY, x, y);
            canvas.setSelectionRect(null);
            for (Figure f : canvas.figuresInRect(rect)) canvas.select(f, true);
            rubberBanding = false;
        }
        dragTarget = null;
    }

    private static Rectangle makeRect(int x1, int y1, int x2, int y2) {
        return new Rectangle(Math.min(x1, x2), Math.min(y1, y2),
                             Math.abs(x2 - x1), Math.abs(y2 - y1));
    }
}
