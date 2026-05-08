import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class EditorController extends MouseAdapter {
    private Tool tool;

    public void setTool(Tool tool) {
        this.tool = tool;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (tool != null) tool.onPress(e.getX(), e.getY());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (tool != null) tool.onDrag(e.getX(), e.getY());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (tool != null) tool.onRelease(e.getX(), e.getY());
    }
}
