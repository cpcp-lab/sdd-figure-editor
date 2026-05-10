import java.util.function.BiConsumer;

public class Handle {
    public enum Type { ENDPOINT, SCALE_NW, SCALE_NE, SCALE_SE, SCALE_SW, ROTATE }

    private static final int HIT_RADIUS = 5;

    private final int cx, cy;
    private final Type type;
    private final BiConsumer<Integer, Integer> dragCallback;

    public Handle(int cx, int cy, Type type, BiConsumer<Integer, Integer> dragCallback) {
        this.cx = cx;
        this.cy = cy;
        this.type = type;
        this.dragCallback = dragCallback;
    }

    public int getCx() { return cx; }
    public int getCy() { return cy; }
    public Type getType() { return type; }

    public boolean contains(int x, int y) {
        return Math.abs(x - cx) <= HIT_RADIUS && Math.abs(y - cy) <= HIT_RADIUS;
    }

    public void drag(int x, int y) {
        if (dragCallback != null) dragCallback.accept(x, y);
    }
}
