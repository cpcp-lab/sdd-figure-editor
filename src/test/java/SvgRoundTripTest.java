import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.awt.Color;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SvgRoundTripTest {

    @TempDir Path tmp;

    @Test
    void roundTripLine() throws Exception {
        Line orig = new Line(10, 20, 100, 200, Color.RED);
        orig.setStrokeWidth(3.0f);
        List<Figure> written = List.of(orig);
        String path = tmp.resolve("test.svg").toString();
        SvgWriter.write(written, path, 480, 360);

        List<Figure> read = SvgReader.read(path);
        assertEquals(1, read.size());
        assertInstanceOf(Line.class, read.get(0));
        Line l = (Line) read.get(0);
        assertEquals(10, l.getX1());
        assertEquals(20, l.getY1());
        assertEquals(100, l.getX2());
        assertEquals(200, l.getY2());
        assertEquals(Color.RED, l.strokeColor);
        assertEquals(3.0f, l.getStrokeWidth(), 0.01f);
    }

    @Test
    void roundTripCircle() throws Exception {
        Circle orig = new Circle(50, 60, 30, Color.BLUE, new Color(0, 128, 0));
        String path = tmp.resolve("circle.svg").toString();
        SvgWriter.write(List.of(orig), path, 480, 360);

        List<Figure> read = SvgReader.read(path);
        assertEquals(1, read.size());
        assertInstanceOf(Circle.class, read.get(0));
        Circle c = (Circle) read.get(0);
        assertEquals(50, c.getCx());
        assertEquals(60, c.getCy());
        assertEquals(30, c.getRadius());
        assertEquals(Color.BLUE, c.strokeColor);
        assertEquals(new Color(0, 128, 0), c.fillColor);
    }

    @Test
    void roundTripEllipse() throws Exception {
        Ellipse orig = new Ellipse(20, 30, 120, 90, Color.BLACK, null);
        String path = tmp.resolve("ellipse.svg").toString();
        SvgWriter.write(List.of(orig), path, 480, 360);

        List<Figure> read = SvgReader.read(path);
        assertEquals(1, read.size());
        assertInstanceOf(Ellipse.class, read.get(0));
        Ellipse e = (Ellipse) read.get(0);
        assertEquals(20, e.getX1());
        assertEquals(30, e.getY1());
        assertEquals(120, e.getX2());
        assertEquals(90, e.getY2());
    }

    @Test
    void roundTripRectangle() throws Exception {
        Rectangle orig = new Rectangle(10, 20, 110, 70, Color.BLACK, Color.YELLOW);
        orig.setRoundedCorners(10, 5);
        String path = tmp.resolve("rect.svg").toString();
        SvgWriter.write(List.of(orig), path, 480, 360);

        List<Figure> read = SvgReader.read(path);
        assertEquals(1, read.size());
        assertInstanceOf(Rectangle.class, read.get(0));
        Rectangle r = (Rectangle) read.get(0);
        assertEquals(10, r.getX1());
        assertEquals(20, r.getY1());
        assertEquals(110, r.getX2());
        assertEquals(70, r.getY2());
    }

    @Test
    void roundTripPolyline() throws Exception {
        Polyline orig = new Polyline(Color.GREEN, null);
        orig.addPoint(10, 50); orig.addPoint(50, 100); orig.addPoint(90, 50);
        String path = tmp.resolve("polyline.svg").toString();
        SvgWriter.write(List.of(orig), path, 480, 360);

        List<Figure> read = SvgReader.read(path);
        assertEquals(1, read.size());
        assertInstanceOf(Polyline.class, read.get(0));
        Polyline pl = (Polyline) read.get(0);
        assertEquals(3, pl.getPointCount());
        assertEquals(10, pl.getPointX(0)); assertEquals(50, pl.getPointY(0));
        assertEquals(50, pl.getPointX(1)); assertEquals(100, pl.getPointY(1));
        assertEquals(90, pl.getPointX(2)); assertEquals(50, pl.getPointY(2));
    }

    @Test
    void roundTripPolygon() throws Exception {
        Polygon orig = new Polygon(Color.BLACK, Color.BLUE);
        orig.addPoint(50, 10); orig.addPoint(90, 90); orig.addPoint(10, 90);
        String path = tmp.resolve("polygon.svg").toString();
        SvgWriter.write(List.of(orig), path, 480, 360);

        List<Figure> read = SvgReader.read(path);
        assertEquals(1, read.size());
        assertInstanceOf(Polygon.class, read.get(0));
        Polygon pg = (Polygon) read.get(0);
        assertEquals(3, pg.getPointCount());
        assertEquals(50, pg.getPointX(0)); assertEquals(10, pg.getPointY(0));
    }

    @Test
    void roundTripMultipleFigures() throws Exception {
        List<Figure> orig = List.of(
            new Line(0, 0, 100, 100, Color.BLACK),
            new Circle(50, 50, 20, Color.RED, null),
            new Rectangle(10, 10, 90, 90, null, Color.BLUE)
        );
        String path = tmp.resolve("multi.svg").toString();
        SvgWriter.write(orig, path, 480, 360);

        List<Figure> read = SvgReader.read(path);
        assertEquals(3, read.size());
        assertInstanceOf(Line.class,      read.get(0));
        assertInstanceOf(Circle.class,    read.get(1));
        assertInstanceOf(Rectangle.class, read.get(2));
    }
}
