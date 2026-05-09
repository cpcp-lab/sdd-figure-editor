import org.junit.jupiter.api.Test;
import java.awt.Color;
import java.io.File;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FigureGroupTest {

    @Test
    void testContainsDelegatesToChildren() {
        Circle c = new Circle(100, 100, 20, Color.BLACK, null);
        Rectangle r = new Rectangle(200, 200, 250, 250, Color.BLACK, null);
        FigureGroup g = new FigureGroup(List.of(c, r));

        assertTrue(g.contains(100, 100));
        assertTrue(g.contains(225, 225));
        assertFalse(g.contains(0, 0));
    }

    @Test
    void testMoveDelegatesToChildren() {
        Circle c = new Circle(50, 50, 10, Color.BLACK, null);
        FigureGroup g = new FigureGroup(List.of(c));

        g.move(30, 20);

        // 移動後に (80, 70) を含む
        assertTrue(g.contains(80, 70));
        assertFalse(g.contains(50, 50));
    }

    @Test
    void testToSvgWrapsInGElement() {
        Circle c = new Circle(10, 10, 5, Color.BLACK, null);
        FigureGroup g = new FigureGroup(List.of(c));
        String svg = g.toSvg();

        assertTrue(svg.startsWith("<g>"));
        assertTrue(svg.endsWith("</g>"));
        assertTrue(svg.contains(c.toSvg()));
    }

    @Test
    void testNestedGroup() {
        Circle c = new Circle(50, 50, 5, Color.BLACK, null);
        FigureGroup inner = new FigureGroup(List.of(c));
        Rectangle r = new Rectangle(200, 200, 220, 220, Color.RED, null);
        FigureGroup outer = new FigureGroup(List.of(inner, r));

        assertTrue(outer.contains(50, 50));
        outer.move(100, 100);
        assertTrue(outer.contains(150, 150));
        assertFalse(outer.contains(50, 50));
    }

    @Test
    void testSvgRoundTrip() throws Exception {
        Circle c = new Circle(100, 100, 30, Color.BLACK, null);
        Rectangle r = new Rectangle(10, 10, 60, 60, null, Color.BLUE);
        FigureGroup g = new FigureGroup(List.of(c, r));

        File tmp = File.createTempFile("group-test", ".svg");
        tmp.deleteOnExit();
        SvgWriter.write(List.of(g), tmp.getPath(), 400, 300);

        List<Figure> figures = SvgReader.read(tmp.getPath());
        assertEquals(1, figures.size());
        assertInstanceOf(FigureGroup.class, figures.get(0));

        FigureGroup read = (FigureGroup) figures.get(0);
        assertEquals(2, read.getChildren().size());
    }

    @Test
    void testGetChildrenIsUnmodifiable() {
        FigureGroup g = new FigureGroup(List.of(new Circle(0, 0, 5, Color.BLACK, null)));
        assertThrows(UnsupportedOperationException.class,
            () -> g.getChildren().add(new Circle(1, 1, 5, Color.BLACK, null)));
    }
}
