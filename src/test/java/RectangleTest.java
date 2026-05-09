import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RectangleTest {

    @Test
    void constructorStoresCorners() {
        Rectangle r = new Rectangle(10, 20, 110, 120, null, null);
        assertEquals(10,  r.getX1());
        assertEquals(20,  r.getY1());
        assertEquals(110, r.getX2());
        assertEquals(120, r.getY2());
    }

    @Test
    void setEndCornerUpdatesSecondCorner() {
        Rectangle r = new Rectangle(0, 0, 0, 0, null, null);
        r.setEndCorner(50, 60);
        assertEquals(50, r.getX2());
        assertEquals(60, r.getY2());
    }

    @Test
    void moveTranslatesBothCorners() {
        Rectangle r = new Rectangle(10, 10, 50, 50, null, null);
        r.move(5, -3);
        assertEquals(15, r.getX1());
        assertEquals(7,  r.getY1());
        assertEquals(55, r.getX2());
        assertEquals(47, r.getY2());
    }

    @Test
    void containsPointInside() {
        Rectangle r = new Rectangle(10, 10, 90, 90, null, null);
        assertTrue(r.contains(50, 50));
    }

    @Test
    void containsPointOutside() {
        Rectangle r = new Rectangle(10, 10, 90, 90, null, null);
        assertFalse(r.contains(5, 50));
        assertFalse(r.contains(50, 5));
        assertFalse(r.contains(95, 50));
        assertFalse(r.contains(50, 95));
    }

    @Test
    void containsWorksWithReversedCorners() {
        Rectangle r = new Rectangle(90, 90, 10, 10, null, null);
        assertTrue(r.contains(50, 50));
        assertFalse(r.contains(5, 5));
    }
}
