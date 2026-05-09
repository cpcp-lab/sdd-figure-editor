import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EllipseTest {

    @Test
    void constructorStoresCorners() {
        Ellipse e = new Ellipse(10, 20, 110, 120, null, null);
        assertEquals(10,  e.getX1());
        assertEquals(20,  e.getY1());
        assertEquals(110, e.getX2());
        assertEquals(120, e.getY2());
    }

    @Test
    void setEndCornerUpdatesSecondCorner() {
        Ellipse e = new Ellipse(0, 0, 0, 0, null, null);
        e.setEndCorner(80, 60);
        assertEquals(80, e.getX2());
        assertEquals(60, e.getY2());
    }

    @Test
    void moveTranslatesBothCorners() {
        Ellipse e = new Ellipse(10, 10, 90, 50, null, null);
        e.move(5, 10);
        assertEquals(15, e.getX1());
        assertEquals(20, e.getY1());
        assertEquals(95, e.getX2());
        assertEquals(60, e.getY2());
    }

    @Test
    void containsCenter() {
        Ellipse e = new Ellipse(0, 0, 100, 60, null, null);
        assertTrue(e.contains(50, 30)); // center
    }

    @Test
    void containsPointInside() {
        Ellipse e = new Ellipse(0, 0, 100, 60, null, null);
        assertTrue(e.contains(60, 30));
    }

    @Test
    void containsPointOutside() {
        Ellipse e = new Ellipse(0, 0, 100, 60, null, null);
        assertFalse(e.contains(5, 5));
        assertFalse(e.contains(95, 5));
    }
}
