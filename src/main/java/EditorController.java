import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class EditorController extends MouseAdapter {
    private Tool tool;

    public void setTool(Tool tool) {
        this.tool = tool;
        tool.onActivate();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (tool != null) tool.onPressEvent(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (tool != null) tool.onDragEvent(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (tool != null) tool.onReleaseEvent(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (tool == null) return;
        if (e.getClickCount() == 2) {
            tool.onDoubleClick(e.getX(), e.getY());
        } else {
            tool.onClick(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (tool != null) tool.onMove(e.getX(), e.getY());
    }
}
