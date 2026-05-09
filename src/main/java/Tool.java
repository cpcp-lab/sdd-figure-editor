public interface Tool {
    void onPress(int x, int y);
    void onDrag(int x, int y);
    void onRelease(int x, int y);
    default void onClick(int x, int y) {}
    default void onDoubleClick(int x, int y) {}
    default void onMove(int x, int y) {}
}
