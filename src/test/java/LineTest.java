import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LineTest {

    @Test
    void constructorSetsEndpoints() {
        Line ls = new Line(1, 2, 3, 4);
        assertEquals(1, ls.getX1());
        assertEquals(2, ls.getY1());
        assertEquals(3, ls.getX2());
        assertEquals(4, ls.getY2());
    }

    @Test
    void moveTranslatesAllEndpoints() {
        Line ls = new Line(0, 0, 10, 10);
        ls.move(5, 3);
        assertEquals(5,  ls.getX1());
        assertEquals(3,  ls.getY1());
        assertEquals(15, ls.getX2());
        assertEquals(13, ls.getY2());
    }

    @Test
    void setEndPointUpdatesSecondEndpoint() {
        Line ls = new Line(0, 0, 0, 0);
        ls.setEndPoint(20, 30);
        assertEquals(20, ls.getX2());
        assertEquals(30, ls.getY2());
        assertEquals(0,  ls.getX1());
        assertEquals(0,  ls.getY1());
    }

    @Test
    void containsPointOnLine() {
        Line ls = new Line(0, 0, 100, 0);
        assertTrue(ls.contains(50, 0));
    }

    @Test
    void containsPointNearLine() {
        Line ls = new Line(0, 0, 100, 0);
        assertTrue(ls.contains(50, 3));
    }

    @Test
    void containsPointTooFarFromLine() {
        Line ls = new Line(0, 0, 100, 0);
        assertFalse(ls.contains(50, 10));
    }

    @Test
    void containsPointBeyondEndpoint() {
        Line ls = new Line(0, 0, 100, 0);
        assertFalse(ls.contains(200, 0));
    }

    @Test
    void containsWorksForZeroLengthSegment() {
        Line ls = new Line(10, 10, 10, 10);
        assertTrue(ls.contains(10, 10));
        assertFalse(ls.contains(20, 10));
    }
}
