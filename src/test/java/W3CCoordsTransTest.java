import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

/**
 * coords-trans-*.svg の内容を Java コードで構築し，参照 PNG と比較する目視確認テスト．
 *
 * W3CShapesTest.java と同様の構造で，AffineTransform を各図形に適用する．
 * text・grid などのスキップ分を考慮して閾値 10%．
 *
 * 実行例:
 *   mvn test -Dtest=W3CCoordsTransTest -Dgroups=visual -DexcludedTestGroups= -Dvisual.auto=true
 */
@Tag("visual")
class W3CCoordsTransTest {

    private static final String PNG_DIR = W3CImageComparator.SUITE_DIR + "/png/";
    private static final int W = 480, H = 360;
    private static final double THRESHOLD = 0.10;

    // -----------------------------------------------------------------------
    // coords-trans-01-b: translate, rotate, skewX, skewY, scale (elementary)
    //                    + nested scale/translate
    // -----------------------------------------------------------------------
    @Test
    void testCoordsTranslate01() throws Exception {
        W3CImageComparator.assumeDisplay();
        List<Figure> figs = new ArrayList<>();

        // outer: translate(0,30), inner: translate(0,10) → cumulative translate(0,40)
        AffineTransform outer = compose("translate(0,30)");
        AffineTransform inner = compose("translate(0,30)", "translate(0,10)");

        addGrid(figs);

        // --- elementary transforms section ---
        // translate(50,50)
        addRect(figs, 0, 0, 20, 2, null, Color.BLUE,
                compose("translate(0,30)", "translate(0,10)", "translate(50,50)"));
        addRect(figs, 0, 0, 2, 20, null, Color.RED,
                compose("translate(0,30)", "translate(0,10)", "translate(50,50)"));
        // rotate(-90) about (150,70)
        addRect(figs, 0, 0, 20, 2, null, Color.BLUE,
                compose("translate(0,30)", "translate(0,10)", "translate(150,70)", "rotate(-90)"));
        addRect(figs, 0, 0, 2, 20, null, Color.RED,
                compose("translate(0,30)", "translate(0,10)", "translate(150,70)", "rotate(-90)"));
        // skewX(45)
        addRect(figs, 0, 0, 20, 2, null, Color.BLUE,
                compose("translate(0,30)", "translate(0,10)", "translate(250,50)", "skewX(45)"));
        addRect(figs, 0, 0, 2, 20, null, Color.RED,
                compose("translate(0,30)", "translate(0,10)", "translate(250,50)", "skewX(45)"));
        // skewY(45)
        addRect(figs, 0, 0, 20, 2, null, Color.BLUE,
                compose("translate(0,30)", "translate(0,10)", "translate(350,50)", "skewY(45)"));
        addRect(figs, 0, 0, 2, 20, null, Color.RED,
                compose("translate(0,30)", "translate(0,10)", "translate(350,50)", "skewY(45)"));
        // scale(2)
        addRect(figs, 0, 0, 20, 1, null, Color.BLUE,
                compose("translate(0,30)", "translate(0,10)", "translate(210,120)", "scale(2)"));
        addRect(figs, 0, 0, 1, 20, null, Color.RED,
                compose("translate(0,30)", "translate(0,10)", "translate(210,120)", "scale(2)"));

        // markers (within inner: translate(0,40))
        addMarkers(figs, inner, 48, 48, 68, 48, 48, 68);   // translate
        addMarkers(figs, inner, 148, 68, 148, 48, 168, 68); // rotate
        addMarkers(figs, inner, 248, 48, 268, 48, 268, 68); // skewX
        addMarkers(figs, inner, 348, 48, 368, 68, 348, 68); // skewY
        addMarkers(figs, inner, 208, 118, 248, 118, 208, 158); // scale

        // --- nested transforms section (within outer: translate(0,30)) ---
        addRect(figs, 0, 0, 20, 1, null, Color.BLUE,
                compose("translate(0,30)", "scale(3,2)", "translate(16.666667,105)"));
        addRect(figs, 0, 0, 1, 20, null, Color.RED,
                compose("translate(0,30)", "scale(3,2)", "translate(16.666667,105)"));
        addRect(figs, 0, 0, 20, 1, null, Color.BLUE,
                compose("translate(0,30)", "translate(200,0)", "scale(3,2)", "translate(16.666667,105)"));
        addRect(figs, 0, 0, 1, 20, null, Color.RED,
                compose("translate(0,30)", "translate(200,0)", "scale(3,2)", "translate(16.666667,105)"));

        // markers (within outer: translate(0,30))
        addMarkers(figs, outer, 48, 208, 108, 208, 48, 248);
        addMarkers(figs, outer, 248, 208, 308, 208, 248, 248);

        addFrame(figs);
        confirm("coords-trans-01-b", figs);
    }

    // -----------------------------------------------------------------------
    // coords-trans-02-t: translate + rotate within scale(2.5) context
    // -----------------------------------------------------------------------
    @Test
    void testCoordsTranslate02() throws Exception {
        W3CImageComparator.assumeDisplay();
        List<Figure> figs = new ArrayList<>();

        // outer: translate(0,30), inner: translate(0,10), then translate(-30,0) scale(2.5,2.5)
        AffineTransform ctx = compose("translate(0,30)", "translate(0,10)", "translate(-30,0)", "scale(2.5,2.5)");
        AffineTransform markers = ctx;

        // translate(50,50)
        AffineTransform t1 = compose("translate(0,30)", "translate(0,10)", "translate(-30,0)", "scale(2.5,2.5)", "translate(50,50)");
        addRect(figs, 0, 0, 20, 2, null, Color.BLUE, t1);
        addRect(figs, 0, 0, 2, 20, null, Color.RED,  t1);
        // rotate(-90)
        AffineTransform t2 = compose("translate(0,30)", "translate(0,10)", "translate(-30,0)", "scale(2.5,2.5)", "translate(150,70)", "rotate(-90)");
        addRect(figs, 0, 0, 20, 2, null, Color.BLUE, t2);
        addRect(figs, 0, 0, 2, 20, null, Color.RED,  t2);

        addMarkers(figs, markers, 48, 48, 68, 48, 48, 68);
        addMarkers(figs, markers, 148, 68, 148, 48, 168, 68);

        addFrame(figs);
        confirm("coords-trans-02-t", figs);
    }

    // -----------------------------------------------------------------------
    // coords-trans-03-t: skewX + skewY within scale(2.5) context
    // -----------------------------------------------------------------------
    @Test
    void testCoordsTranslate03() throws Exception {
        W3CImageComparator.assumeDisplay();
        List<Figure> figs = new ArrayList<>();

        AffineTransform markers = compose("translate(0,30)", "translate(0,10)", "translate(-560,0)", "scale(2.5,2.5)");

        // skewX(45)
        AffineTransform t1 = compose("translate(0,30)", "translate(0,10)", "translate(-560,0)", "scale(2.5,2.5)", "translate(250,50)", "skewX(45)");
        addRect(figs, 0, 0, 20, 2, null, Color.BLUE, t1);
        addRect(figs, 0, 0, 2, 20, null, Color.RED,  t1);
        // skewY(45)
        AffineTransform t2 = compose("translate(0,30)", "translate(0,10)", "translate(-560,0)", "scale(2.5,2.5)", "translate(350,50)", "skewY(45)");
        addRect(figs, 0, 0, 20, 2, null, Color.BLUE, t2);
        addRect(figs, 0, 0, 2, 20, null, Color.RED,  t2);

        addMarkers(figs, markers, 248, 48, 268, 48, 268, 68);
        addMarkers(figs, markers, 348, 48, 368, 68, 348, 68);

        addFrame(figs);
        confirm("coords-trans-03-t", figs);
    }

    // -----------------------------------------------------------------------
    // coords-trans-04-t: nested scale+translate
    // -----------------------------------------------------------------------
    @Test
    void testCoordsTranslate04() throws Exception {
        W3CImageComparator.assumeDisplay();
        List<Figure> figs = new ArrayList<>();

        AffineTransform markers = compose("translate(0,30)", "translate(-364,-230)", "scale(2.5,2.5)");
        AffineTransform t = compose("translate(0,30)", "translate(60,45)", "scale(2.5,2.5)", "translate(40,10)", "scale(2)");
        addRect(figs, 0, 0, 20, 1, null, Color.BLUE, t);
        addRect(figs, 0, 0, 1, 20, null, Color.RED,  t);

        addMarkers(figs, markers, 208, 118, 248, 118, 208, 158);

        addFrame(figs);
        confirm("coords-trans-04-t", figs);
    }

    // -----------------------------------------------------------------------
    // coords-trans-05-t: scale+translate combined in one attribute
    // -----------------------------------------------------------------------
    @Test
    void testCoordsTranslate05() throws Exception {
        W3CImageComparator.assumeDisplay();
        List<Figure> figs = new ArrayList<>();

        AffineTransform markers = compose("translate(0,30)", "translate(-90,-450)", "scale(2.5,2.5)");
        AffineTransform t = compose("translate(0,30)", "translate(-90,-450)", "scale(7.5,5)", "translate(16.666667,105)");
        addRect(figs, 0, 0, 20, 1, null, Color.BLUE, t);
        addRect(figs, 0, 0, 1, 20, null, Color.RED,  t);

        addMarkers(figs, markers, 48, 208, 108, 208, 48, 248);

        addFrame(figs);
        confirm("coords-trans-05-t", figs);
    }

    // -----------------------------------------------------------------------
    // coords-trans-06-t: same transforms as 05 but split across nested elements
    // -----------------------------------------------------------------------
    @Test
    void testCoordsTranslate06() throws Exception {
        W3CImageComparator.assumeDisplay();
        List<Figure> figs = new ArrayList<>();

        AffineTransform markers = compose("translate(0,30)", "translate(-600,-450)", "scale(2.5,2.5)");
        AffineTransform t = compose("translate(0,30)", "translate(-102,-450)", "scale(7.5,5)", "translate(16.666667,105)");
        addRect(figs, 0, 0, 20, 1, null, Color.BLUE, t);
        addRect(figs, 0, 0, 1, 20, null, Color.RED,  t);

        addMarkers(figs, markers, 248, 208, 308, 208, 248, 248);

        addFrame(figs);
        confirm("coords-trans-06-t", figs);
    }

    // -----------------------------------------------------------------------
    // coords-trans-07-t: rotate before/after translate
    // -----------------------------------------------------------------------
    @Test
    void testCoordsTranslate07() throws Exception {
        W3CImageComparator.assumeDisplay();
        List<Figure> figs = new ArrayList<>();

        // object_1: rotate(30) translate(200,100)
        AffineTransform t1 = compose("rotate(30)", "translate(200,100)");
        addRect(figs, 0, 0, 150, 5, null, new Color(0, 128, 0), t1);
        addRect(figs, 0, 0, 5,  50, null, Color.RED,            t1);

        // object_2: translate(200,100) rotate(30)
        AffineTransform t2 = compose("translate(200,100)", "rotate(30)");
        addRect(figs, 0, 0, 150, 5, null, Color.BLUE, t2);
        addRect(figs, 0, 0, 5,  50, null, Color.RED,  t2);

        addFrame(figs);
        confirm("coords-trans-07-t", figs);
    }

    // -----------------------------------------------------------------------
    // coords-trans-08-t: skewX+skewY vs skewY+skewX
    // -----------------------------------------------------------------------
    @Test
    void testCoordsTranslate08() throws Exception {
        W3CImageComparator.assumeDisplay();
        List<Figure> figs = new ArrayList<>();

        // object_1: skewX(45) skewY(45)
        AffineTransform t1 = compose("skewX(45)", "skewY(45)");
        addRect(figs,   0,  0, 150,  5, null, Color.BLUE,    t1);
        addRect(figs,   0,  0,   5, 50, null, Color.RED,     t1);
        addRect(figs, 150,  0,   5, 50, null, Color.BLACK,   t1);
        addRect(figs,   0, 50, 150,  5, null, Color.BLACK,   t1);
        addEllipse(figs, 75 - 40, 25 - 15, 75 + 40, 25 + 15, null, new Color(128, 0, 128), t1);

        // object_2: translate(200,0) skewY(45) skewX(45)
        AffineTransform t2 = compose("translate(200,0)", "skewY(45)", "skewX(45)");
        addRect(figs,   0,  0, 150,  5, null, Color.BLUE,    t2);
        addRect(figs,   0,  0,   5, 50, null, Color.RED,     t2);
        addRect(figs, 150,  0,   5, 50, null, Color.BLACK,   t2);
        addRect(figs,   0, 50, 150,  5, null, Color.BLACK,   t2);
        addEllipse(figs, 75 - 40, 25 - 15, 75 + 40, 25 + 15, null, new Color(128, 0, 128), t2);

        addFrame(figs);
        confirm("coords-trans-08-t", figs);
    }

    // -----------------------------------------------------------------------
    // coords-trans-09-t: 7 matrix() transforms
    // -----------------------------------------------------------------------
    @Test
    void testCoordsTranslate09() throws Exception {
        W3CImageComparator.assumeDisplay();
        List<Figure> figs = new ArrayList<>();

        String[][] matrices = {
            {"matrix(0,0,0,0,0,0)"},
            {"matrix(1,0,0,1,100,100)"},
            {"matrix(1.5,0,0,1.5,70,60)"},
            {"matrix(1,0,0.5,1,30,170)"},
            {"matrix(1,0.5,0,1,100,200)"},
            {"matrix(0,1,-1,0,450,0)"},
            {"matrix(1,0.8,0.8,1,300,220)"},
        };

        for (String[] m : matrices) {
            AffineTransform t = compose(m[0]);
            addRect(figs, 0, 0, 150, 5, null, Color.BLUE, t);
            addRect(figs, 0, 0, 5,  50, null, Color.RED,  t);
        }

        addFrame(figs);
        confirm("coords-trans-09-t", figs);
    }

    // -----------------------------------------------------------------------
    // coords-trans-10-f: translate ≡ matrix(1,0,0,1,tx,ty)
    // -----------------------------------------------------------------------
    @Test
    void testCoordsTranslate10() throws Exception {
        W3CImageComparator.assumeDisplay();
        List<Figure> figs = new ArrayList<>();

        // group 1: red=translate(40,20), black=matrix(1,0,0,1,40,20)
        addShapeSet(figs, Color.RED,   null,        compose("translate(40,20)"));
        addShapeSet(figs, Color.BLACK, Color.BLACK, compose("matrix(1,0,0,1,40,20)"));

        // group 2 (offset translate(0,100))
        addShapeSet(figs, Color.RED,   null,        compose("translate(0,100)", "matrix(1,0,0,1,40,20)"));
        addShapeSet(figs, Color.BLACK, Color.BLACK, compose("translate(0,100)", "translate(40,20)"));

        addFrame(figs);
        confirm("coords-trans-10-f", figs);
    }

    // -----------------------------------------------------------------------
    // coords-trans-11-f: scale ≡ matrix(sx,0,0,sy,0,0)
    // -----------------------------------------------------------------------
    @Test
    void testCoordsTranslate11() throws Exception {
        W3CImageComparator.assumeDisplay();
        List<Figure> figs = new ArrayList<>();

        addShapeSet(figs, Color.RED,   null,        compose("scale(1.2,2.5)"));
        addShapeSet(figs, Color.BLACK, Color.BLACK, compose("matrix(1.2,0,0,2.5,0,0)"));

        addShapeSet(figs, Color.RED,   null,        compose("translate(0,150)", "matrix(1.2,0,0,2.5,0,0)"));
        addShapeSet(figs, Color.BLACK, Color.BLACK, compose("translate(0,150)", "scale(1.2,2.5)"));

        addFrame(figs);
        confirm("coords-trans-11-f", figs);
    }

    // -----------------------------------------------------------------------
    // coords-trans-12-f: rotate(90) ≡ matrix(0,1,-1,0,0,0)
    // -----------------------------------------------------------------------
    @Test
    void testCoordsTranslate12() throws Exception {
        W3CImageComparator.assumeDisplay();
        List<Figure> figs = new ArrayList<>();

        addShapeSet(figs, Color.RED,   null,        compose("translate(200)", "rotate(90)"));
        addShapeSet(figs, Color.BLACK, Color.BLACK, compose("translate(200)", "matrix(0,1,-1,0,0,0)"));

        addShapeSet(figs, Color.RED,   null,        compose("translate(310)", "matrix(0,1,-1,0,0,0)"));
        addShapeSet(figs, Color.BLACK, Color.BLACK, compose("translate(310)", "rotate(90)"));

        addFrame(figs);
        confirm("coords-trans-12-f", figs);
    }

    // -----------------------------------------------------------------------
    // coords-trans-13-f: skewX(45) ≡ matrix(1,0,1,1,0,0)
    // -----------------------------------------------------------------------
    @Test
    void testCoordsTranslate13() throws Exception {
        W3CImageComparator.assumeDisplay();
        List<Figure> figs = new ArrayList<>();

        addShapeSet(figs, Color.RED,   null,        compose("skewX(45)"));
        addShapeSet(figs, Color.BLACK, Color.BLACK, compose("matrix(1,0,1,1,0,0)"));

        addShapeSet(figs, Color.RED,   null,        compose("translate(0,150)", "matrix(1,0,1,1,0,0)"));
        addShapeSet(figs, Color.BLACK, Color.BLACK, compose("translate(0,150)", "skewX(45)"));

        addFrame(figs);
        confirm("coords-trans-13-f", figs);
    }

    // -----------------------------------------------------------------------
    // coords-trans-14-f: skewY(45) ≡ matrix(1,1,0,1,0,0), outer scale+rotate
    // -----------------------------------------------------------------------
    @Test
    void testCoordsTranslate14() throws Exception {
        W3CImageComparator.assumeDisplay();
        List<Figure> figs = new ArrayList<>();

        String outer = "scale(0.75) rotate(-20)";
        addShapeSet(figs, Color.RED,   null,        compose(outer, "skewY(45)"));
        addShapeSet(figs, Color.BLACK, Color.BLACK, compose(outer, "matrix(1,1,0,1,0,0)"));

        addShapeSet(figs, Color.RED,   null,        compose(outer, "translate(0,150)", "matrix(1,1,0,1,0,0)"));
        addShapeSet(figs, Color.BLACK, Color.BLACK, compose(outer, "translate(0,150)", "skewY(45)"));

        addFrame(figs);
        confirm("coords-trans-14-f", figs);
    }

    // -----------------------------------------------------------------------
    // helpers
    // -----------------------------------------------------------------------

    /** Composes multiple SVG transform strings left-to-right (outer→inner). */
    private static AffineTransform compose(String... transforms) {
        AffineTransform r = new AffineTransform();
        for (String s : transforms) r.concatenate(SvgReader.parseTransform(s));
        return r;
    }

    private static void setT(Figure f, AffineTransform at) {
        if (!at.isIdentity()) f.getTransform().setTransform(at);
    }

    private static void addRect(List<Figure> figs, int x, int y, int w, int h,
                                Color stroke, Color fill, AffineTransform t) {
        Rectangle r = new Rectangle(x, y, x + w, y + h, stroke, fill);
        setT(r, t);
        figs.add(r);
    }

    private static void addEllipse(List<Figure> figs, int x1, int y1, int x2, int y2,
                                   Color stroke, Color fill, AffineTransform t) {
        Ellipse e = new Ellipse(x1, y1, x2, y2, stroke, fill);
        setT(e, t);
        figs.add(e);
    }

    private static void addLine(List<Figure> figs, double x1, double y1, double x2, double y2,
                                Color stroke, float sw, AffineTransform t) {
        Line l = new Line(x1, y1, x2, y2, stroke);
        l.setStrokeWidth(sw);
        setT(l, t);
        figs.add(l);
    }

    private static void addTriangle(List<Figure> figs, Color fill, Color stroke, AffineTransform t) {
        Polygon pg = new Polygon(stroke, fill);
        pg.addPoint(20, 20);
        pg.addPoint(70, 20);
        pg.addPoint(45, 60);
        setT(pg, t);
        figs.add(pg);
    }

    /** 3 marker rects: black at (bx,by), blue at (ux,uy), red at (rx,ry), within ctx transform. */
    private static void addMarkers(List<Figure> figs, AffineTransform ctx,
                                   int bx, int by, int ux, int uy, int rx, int ry) {
        addRect(figs, bx, by, 5, 5, null, Color.BLACK, ctx);
        addRect(figs, ux, uy, 5, 5, null, Color.BLUE,  ctx);
        addRect(figs, rx, ry, 5, 5, null, Color.RED,   ctx);
    }

    /** Frame rect common to all test cases. */
    private static void addFrame(List<Figure> figs) {
        figs.add(new Rectangle(1, 1, 479, 359, Color.BLACK, null));
    }

    /** 10px grid used in coords-trans-01-b (within translate(0,30)). */
    private static void addGrid(List<Figure> figs) {
        Color cc = new Color(0xCC, 0xCC, 0xCC);
        AffineTransform t = compose("translate(0,30)");
        for (int y = 10; y <= 260; y += 10)
            addLine(figs, 10, y + 0.5, 470, y + 0.5, cc, 1f, t);
        for (int x = 10; x <= 470; x += 10)
            addLine(figs, x + 0.5, 10, x + 0.5, 260.5, cc, 1f, t);
    }

    /**
     * Adds the standard shape set used in coords-trans-10-f through 14-f:
     * triangle (path M20,20 L70,20 L45,60 z), ellipse (cx=120,cy=35,rx=30,ry=10),
     * rect (x=250,y=20,w=30,h=50), line (310,20→350,70 sw=5).
     */
    private static void addShapeSet(List<Figure> figs, Color fill, Color stroke, AffineTransform t) {
        addTriangle(figs, fill, stroke, t);
        addEllipse(figs, 90, 25, 150, 45, stroke, fill, t);
        addRect(figs, 250, 20, 30, 50, stroke, fill, t);
        addLine(figs, 310, 20, 350, 70, stroke != null ? stroke : fill, 5f, t);
    }

    private static void confirm(String name, List<Figure> figs) throws Exception {
        var img = W3CImageComparator.render(figs, W, H);
        W3CImageComparator.showAndConfirm(name, img, PNG_DIR + name + ".png", THRESHOLD);
    }
}
