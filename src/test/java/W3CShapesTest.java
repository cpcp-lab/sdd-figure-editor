import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * 目視確認テスト — 各図形を参照 PNG と比較する．
 *
 * 実行モード (システムプロパティ -Dvisual.auto=true/false で切り替え):
 *   false (デフォルト): 描画結果と参照画像を並べたウィンドウを表示し，目視で確認する
 *   true             : 参照 PNG との画素比較 (差分ピクセル率 7% 以内) で自動確認する
 *
 * 実行例:
 *   mvn test -Dgroups=visual                       # 目視確認モード
 *   mvn test -Dgroups=visual -Dvisual.auto=true    # 自動比較モード
 */
@Tag("visual")
class W3CShapesTest {

    private static final double AUTO_THRESHOLD = 0.07;

    // -----------------------------------------------------------------------
    // shapes-polyline-01-t.svg/png
    // -----------------------------------------------------------------------
    @Test
    void testPolylineVisual() throws Exception {
        W3CImageComparator.assumeDisplay();
        Canvas canvas = makeCanvas();

        Polyline pl1 = new Polyline(Color.BLACK);           // stroke-width="1" (default)
        addPoints(pl1, new int[]{10, 35, 60, 85,110,135},
                       new int[]{50,150, 50,150, 50,150});
        canvas.addFigure(pl1);

        Polyline pl2 = new Polyline(Color.BLUE);            // stroke-width="8"
        pl2.setStrokeWidth(8);
        addPoints(pl2, new int[]{220,267,249,190,172,220},
                       new int[]{ 50, 84,140,140, 84, 50});
        canvas.addFigure(pl2);

        Polyline pl3 = new Polyline(Color.GREEN, Color.BLUE); // stroke-width="4"
        pl3.setStrokeWidth(4);
        addPoints(pl3, new int[]{310,335,360,385,410,435},
                       new int[]{ 50,150, 50,150, 50,150});
        canvas.addFigure(pl3);

        Polyline pl4 = new Polyline(new Color(0, 128, 0));  // stroke="green"=#008000, stroke-width="8"
        pl4.setStrokeWidth(8);
        addPoints(pl4, new int[]{ 59, 98,108, 82, 39, 11, 19},
                       new int[]{185,203,245,279,280,247,205});
        canvas.addFigure(pl4);

        Polyline pl5 = new Polyline(Color.BLUE, Color.GREEN); // stroke-width="8"
        pl5.setStrokeWidth(8);
        addPoints(pl5, new int[]{189,228,238,212,169,141,149},
                       new int[]{185,203,245,279,280,247,205});
        canvas.addFigure(pl5);

        Polyline pl6 = new Polyline(null, Color.MAGENTA);  // stroke-width="8"
        pl6.setStrokeWidth(8);
        addPoints(pl6, new int[]{270,300,320,340,280,390,420,280},
                       new int[]{225,245,225,245,280,280,240,185});
        canvas.addFigure(pl6);

        showAndConfirm("Polyline", canvas, "shapes-polyline-01-t");
    }

    // -----------------------------------------------------------------------
    // shapes-circle-01-t.svg/png
    // -----------------------------------------------------------------------
    @Test
    void testCircleVisual() throws Exception {
        W3CImageComparator.assumeDisplay();
        Canvas canvas = makeCanvas();
        canvas.addFigure(new Circle(100, 100, 50, Color.BLACK,          null));
        canvas.addFigure(new Circle(220, 100, 35, Color.BLACK,          new Color(0, 128, 0)));
        addCircle(canvas, 340, 100, 20, Color.GREEN,          Color.BLACK, 4f);
        addCircle(canvas, 100, 260, 20, Color.GREEN,          Color.YELLOW, 4f);
        canvas.addFigure(new Circle(220, 260, 35, null,                 Color.BLUE));
        addCircle(canvas, 340, 260, 50, new Color(0, 128, 0), null,        10f);
        showAndConfirm("Circle", canvas, "shapes-circle-01-t");
    }

    // -----------------------------------------------------------------------
    // shapes-ellipse-01-t.svg/png
    // -----------------------------------------------------------------------
    @Test
    void testEllipseVisual() throws Exception {
        W3CImageComparator.assumeDisplay();
        Canvas canvas = makeCanvas();
        canvas.addFigure(new Ellipse( 20,  25,  80, 125, Color.BLACK,          null));
        canvas.addFigure(new Ellipse(130,  25, 190, 125, null,                 new Color(0, 128, 0)));
        canvas.addFigure(new Circle(270,  80,  35, Color.BLACK,                null));
        canvas.addFigure(new Circle(370,  80,  35, null,                       new Color(0, 128, 0)));
        addEllipse(canvas,  20, 170,  80, 270, Color.BLUE, null,        8f);
        addEllipse(canvas, 130, 170, 190, 270, Color.BLUE, Color.GREEN, 8f);
        addEllipse(canvas, 260, 180, 400, 260, Color.BLUE, Color.GREEN, 8f);
        showAndConfirm("Ellipse", canvas, "shapes-ellipse-01-t");
    }

    // -----------------------------------------------------------------------
    // shapes-line-01-t.svg/png
    // -----------------------------------------------------------------------
    @Test
    void testLineVisual() throws Exception {
        W3CImageComparator.assumeDisplay();
        Canvas canvas = makeCanvas();

        addLine(canvas, 37.5, 137,  112.5, 50,  Color.BLACK,          1f);
        addLine(canvas, 112.5, 137, 187.5, 50,  Color.YELLOW,         5f);
        addLine(canvas, 187.5, 137, 262.5, 50,  new Color(0, 128, 0), 7.5f);
        addLine(canvas, 262.5, 137, 337.5, 50,  Color.BLUE,           10f);
        addLine(canvas, 337.5, 137, 412.5, 50,  Color.MAGENTA,        12.5f);

        Polyline lowerLeft = new Polyline(Color.BLUE);
        lowerLeft.setStrokeWidth(10);
        addPoints(lowerLeft,
            new int[]{ 25,  75,  75, 125, 125, 175},
            new int[]{200, 200, 250, 250, 200, 200});
        canvas.addFigure(lowerLeft);

        Polyline middle = new Polyline(Color.BLACK);
        addPoints(middle,
            new int[]{170, 220, 220, 270, 270, 320},
            new int[]{200, 200, 250, 250, 200, 200});
        canvas.addFigure(middle);

        addLine(canvas, 320, 200, 370, 200, Color.BLUE,            10f);
        addLine(canvas, 370, 200, 370, 250, new Color(0, 128, 0),  10f);
        addLine(canvas, 370, 250, 420, 250, Color.BLACK,           10f);
        addLine(canvas, 420, 250, 420, 200, new Color(255, 165, 0),10f);
        addLine(canvas, 420, 200, 470, 200, Color.MAGENTA,         10f);

        showAndConfirm("Line", canvas, "shapes-line-01-t");
    }

    // -----------------------------------------------------------------------
    // shapes-rect-01-t.svg/png
    // -----------------------------------------------------------------------
    @Test
    void testRectVisual() throws Exception {
        W3CImageComparator.assumeDisplay();
        Canvas canvas = makeCanvas();

        canvas.addFigure(new Rectangle(30, 46, 80, 126, Color.BLACK, null));
        canvas.addFigure(new Rectangle(130, 46, 180, 126, null, Color.MAGENTA));
        canvas.addFigure(new Circle(275,  71, 25, Color.BLACK, null));
        canvas.addFigure(new Rectangle(250, 71, 300, 101, Color.BLACK, null));
        canvas.addFigure(new Circle(275, 101, 25, Color.BLACK, null));
        canvas.addFigure(new Circle(375,  71, 25, null, Color.MAGENTA));
        canvas.addFigure(new Rectangle(350, 71, 400, 101, null, Color.MAGENTA));
        canvas.addFigure(new Circle(375, 101, 25, null, Color.MAGENTA));

        addRect(canvas, 30, 196, 80, 276, Color.BLUE, null,        8f, 0, 0);
        addRect(canvas, 130, 196, 180, 276, Color.BLUE, Color.GREEN, 8f, 0, 0);
        addRect(canvas, 250, 196, 300, 276, Color.BLUE, null,        8f, 30, 50);
        canvas.addFigure(new Rectangle(350, 196, 400, 276, null, Color.GREEN));

        showAndConfirm("Rectangle", canvas, "shapes-rect-01-t");
    }

    // -----------------------------------------------------------------------
    // shapes-polygon-01-t.svg/png
    // -----------------------------------------------------------------------
    @Test
    void testPolygonVisual() throws Exception {
        W3CImageComparator.assumeDisplay();
        Canvas canvas = makeCanvas();

        Polygon pg1 = new Polygon(Color.BLACK, null);
        addPoints(pg1, new int[]{59, 95,108, 82, 39, 11, 19},
                       new int[]{45, 63,105,139,140,107, 65});
        canvas.addFigure(pg1);

        Polygon pg2 = new Polygon(null, Color.BLUE);
        addPoints(pg2, new int[]{179,218,228,202,159,131,139,179},
                       new int[]{ 45, 63,105,139,140,107, 65, 45});
        canvas.addFigure(pg2);

        Polygon pg3 = new Polygon(Color.BLACK, Color.BLUE);
        pg3.setStrokeWidth(6);
        addPoints(pg3, new int[]{350,375,410,375,350,325,290,325,350},
                       new int[]{ 45, 80, 95,110,145,120, 95, 70, 45});
        canvas.addFigure(pg3);

        Polygon pg5 = new Polygon(Color.BLUE, null);
        pg5.setStrokeWidth(8);
        addPoints(pg5, new int[]{ 59, 98,108, 82, 39, 11, 19, 59},
                       new int[]{185,203,245,279,280,247,205,185});
        canvas.addFigure(pg5);

        Polygon pg6 = new Polygon(Color.BLUE, Color.GREEN);
        pg6.setStrokeWidth(8);
        addPoints(pg6, new int[]{179,218,228,202,159,131,139},
                       new int[]{185,203,245,279,280,247,205});
        canvas.addFigure(pg6);

        Polygon pg7 = new Polygon(Color.GREEN, null);
        pg7.setStrokeWidth(8);
        addPoints(pg7, new int[]{270,300,320,340,280,390,420,280},
                       new int[]{225,245,225,245,280,280,240,185});
        canvas.addFigure(pg7);

        showAndConfirm("Polygon", canvas, "shapes-polygon-01-t");
    }

    // -----------------------------------------------------------------------
    // helpers
    // -----------------------------------------------------------------------

    private static Canvas makeCanvas() {
        Canvas c = new Canvas();
        c.setPreferredSize(new Dimension(480, 360));
        return c;
    }

    private static void showAndConfirm(String label, Canvas canvas, String svgName)
            throws Exception {
        int w = canvas.getPreferredSize().width;
        int h = canvas.getPreferredSize().height;
        var actual = W3CImageComparator.render(canvas.getFigures(), w, h);
        String pngPath = W3CImageComparator.SUITE_DIR + "/png/" + svgName + ".png";
        W3CImageComparator.showAndConfirm(label, actual, pngPath, AUTO_THRESHOLD);
    }

    private static void addLine(Canvas canvas, double x1, double y1, double x2, double y2,
                                Color color, float sw) {
        Line l = new Line(x1, y1, x2, y2, color);
        l.setStrokeWidth(sw);
        canvas.addFigure(l);
    }

    private static void addCircle(Canvas canvas, int cx, int cy, int r,
                                  Color stroke, Color fill, float sw) {
        Circle c = new Circle(cx, cy, r, stroke, fill);
        c.setStrokeWidth(sw);
        canvas.addFigure(c);
    }

    private static void addEllipse(Canvas canvas, int x1, int y1, int x2, int y2,
                                   Color stroke, Color fill, float sw) {
        Ellipse e = new Ellipse(x1, y1, x2, y2, stroke, fill);
        e.setStrokeWidth(sw);
        canvas.addFigure(e);
    }

    private static void addRect(Canvas canvas, int x1, int y1, int x2, int y2,
                                Color stroke, Color fill, float sw, int rx, int ry) {
        Rectangle r = new Rectangle(x1, y1, x2, y2, stroke, fill);
        r.setStrokeWidth(sw);
        r.setRoundedCorners(rx, ry);
        canvas.addFigure(r);
    }

    private static void addPoints(Polyline pl, int[] xs, int[] ys) {
        for (int i = 0; i < xs.length; i++) pl.addPoint(xs[i], ys[i]);
    }

    private static void addPoints(Polygon pg, int[] xs, int[] ys) {
        for (int i = 0; i < xs.length; i++) pg.addPoint(xs[i], ys[i]);
    }
}
