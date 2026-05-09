import java.awt.event.MouseEvent;

public interface Tool {
    void onPress(int x, int y);
    void onDrag(int x, int y);
    void onRelease(int x, int y);
    default void onActivate() {}
    default void onClick(int x, int y) {}
    default void onDoubleClick(int x, int y) {}
    default void onMove(int x, int y) {}

    default void onPressEvent(MouseEvent e)   { onPress(e.getX(), e.getY()); }
    default void onDragEvent(MouseEvent e)    { onDrag(e.getX(), e.getY()); }
    default void onReleaseEvent(MouseEvent e) { onRelease(e.getX(), e.getY()); }
}
