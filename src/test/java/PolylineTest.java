import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PolylineTest {

    @Test
    void addPointIncreasesCount() {
        Polyline pl = new Polyline(null);
        pl.addPoint(10, 20);
        pl.addPoint(30, 40);
        assertEquals(2, pl.getPointCount());
        assertEquals(10, pl.getPointX(0));
        assertEquals(40, pl.getPointY(1));
    }

    @Test
    void setLastPointUpdatesLastVertex() {
        Polyline pl = new Polyline(null);
        pl.addPoint(0, 0);
        pl.addPoint(10, 10);
        pl.setLastPoint(50, 60);
        assertEquals(50, pl.getPointX(1));
        assertEquals(60, pl.getPointY(1));
        assertEquals(2, pl.getPointCount());
    }

    @Test
    void removeLastPointDecreasesCount() {
        Polyline pl = new Polyline(null);
        pl.addPoint(0, 0);
        pl.addPoint(10, 10);
        pl.removeLastPoint();
        assertEquals(1, pl.getPointCount());
    }

    @Test
    void moveTranslatesAllPoints() {
        Polyline pl = new Polyline(null);
        pl.addPoint(10, 20);
        pl.addPoint(30, 40);
        pl.move(5, -10);
        assertEquals(15, pl.getPointX(0));
        assertEquals(10, pl.getPointY(0));
        assertEquals(35, pl.getPointX(1));
        assertEquals(30, pl.getPointY(1));
    }

    @Test
    void containsPointNearSegment() {
        Polyline pl = new Polyline(null);
        pl.addPoint(0, 0);
        pl.addPoint(100, 0);
        assertTrue(pl.contains(50, 2));
    }

    @Test
    void containsPointFarFromSegment() {
        Polyline pl = new Polyline(null);
        pl.addPoint(0, 0);
        pl.addPoint(100, 0);
        assertFalse(pl.contains(50, 20));
    }
}
