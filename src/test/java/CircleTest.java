import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CircleTest {

    @Test
    void constructorStoresFields() {
        Circle c = new Circle(50, 60, 30, null, null);
        assertEquals(50, c.getCx());
        assertEquals(60, c.getCy());
        assertEquals(30, c.getRadius());
    }

    @Test
    void setRadiusUpdatesRadius() {
        Circle c = new Circle(0, 0, 10, null, null);
        c.setRadius(25);
        assertEquals(25, c.getRadius());
    }

    @Test
    void setRadiusClampedToZero() {
        Circle c = new Circle(0, 0, 10, null, null);
        c.setRadius(-5);
        assertEquals(0, c.getRadius());
    }

    @Test
    void moveTranslatesCenter() {
        Circle c = new Circle(50, 50, 20, null, null);
        c.move(10, -5);
        assertEquals(60, c.getCx());
        assertEquals(45, c.getCy());
    }

    @Test
    void containsCenter() {
        Circle c = new Circle(50, 50, 30, null, null);
        assertTrue(c.contains(50, 50));
    }

    @Test
    void containsPointOnEdge() {
        Circle c = new Circle(50, 50, 30, null, null);
        assertTrue(c.contains(80, 50));
    }

    @Test
    void containsPointOutside() {
        Circle c = new Circle(50, 50, 30, null, null);
        assertFalse(c.contains(81, 50));
        assertFalse(c.contains(50, 81));
    }
}
