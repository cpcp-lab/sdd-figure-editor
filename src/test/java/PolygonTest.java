import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PolygonTest {

    private Polygon makeSquare() {
        Polygon pg = new Polygon(null, null);
        pg.addPoint(10, 10);
        pg.addPoint(90, 10);
        pg.addPoint(90, 90);
        pg.addPoint(10, 90);
        return pg;
    }

    @Test
    void addPointIncreasesCount() {
        Polygon pg = new Polygon(null, null);
        pg.addPoint(0, 0);
        pg.addPoint(50, 0);
        pg.addPoint(25, 50);
        assertEquals(3, pg.getPointCount());
    }

    @Test
    void setLastPointUpdatesLastVertex() {
        Polygon pg = makeSquare();
        pg.setLastPoint(20, 80);
        assertEquals(20, pg.getPointX(3));
        assertEquals(80, pg.getPointY(3));
    }

    @Test
    void removeLastPointDecreasesCount() {
        Polygon pg = makeSquare();
        pg.removeLastPoint();
        assertEquals(3, pg.getPointCount());
    }

    @Test
    void moveTranslatesAllPoints() {
        Polygon pg = makeSquare();
        pg.move(10, 20);
        assertEquals(20, pg.getPointX(0));
        assertEquals(30, pg.getPointY(0));
        assertEquals(100, pg.getPointX(1));
    }

    @Test
    void containsPointInside() {
        assertTrue(makeSquare().contains(50, 50));
    }

    @Test
    void containsPointOutside() {
        assertFalse(makeSquare().contains(5, 5));
        assertFalse(makeSquare().contains(95, 95));
    }

    @Test
    void doesNotContainWhenFewerThanThreePoints() {
        Polygon pg = new Polygon(null, null);
        pg.addPoint(0, 0);
        pg.addPoint(100, 0);
        assertFalse(pg.contains(50, 0));
    }
}
