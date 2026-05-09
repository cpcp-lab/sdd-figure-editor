import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 目視確認テスト — 各図形を SVG 座標そのまま描画し，参照 PNG と比較する．
 * 実行: mvn test -Dgroups=visual
 */
@Tag("visual")
class ShapesVisualTest {

    // -----------------------------------------------------------------------
    // shapes-polyline-01-t.svg/png
    // polyline-01〜06 の座標・色をそのまま使用
    // -----------------------------------------------------------------------
    @Test
    void testPolylineVisual() throws Exception {
        assumeNotHeadless();
        Canvas canvas = makeCanvas();

        // polyline-01: fill=none, stroke=black, 6点 (5線分)
        Polyline pl1 = new Polyline(Color.BLACK);
        addPoints(pl1, new int[]{10, 35, 60, 85,110,135},
                       new int[]{50,150, 50,150, 50,150});
        canvas.addFigure(pl1);

        // polyline-02: fill=none, stroke=blue, 閉じた五角形 (6点で閉じる)
        Polyline pl2 = new Polyline(Color.BLUE);
        addPoints(pl2, new int[]{220,267,249,190,172,220},
                       new int[]{ 50, 84,140,140, 84, 50});
        canvas.addFigure(pl2);

        // polyline-03: fill=blue, stroke=green, 6点 (5線分)
        Polyline pl3 = new Polyline(Color.GREEN, Color.BLUE);
        addPoints(pl3, new int[]{310,335,360,385,410,435},
                       new int[]{ 50,150, 50,150, 50,150});
        canvas.addFigure(pl3);

        // polyline-04: fill=none, stroke=green, 7点 (7角形から1辺除く)
        Polyline pl4 = new Polyline(Color.GREEN);
        addPoints(pl4, new int[]{ 59, 98,108, 82, 39, 11, 19},
                       new int[]{185,203,245,279,280,247,205});
        canvas.addFigure(pl4);

        // polyline-05: fill=green, stroke=blue, 7点 (polyline-04 と同形・右に130移動)
        Polyline pl5 = new Polyline(Color.BLUE, Color.GREEN);
        addPoints(pl5, new int[]{189,228,238,212,169,141,149},
                       new int[]{185,203,245,279,280,247,205});
        canvas.addFigure(pl5);

        // polyline-06: fill=magenta, stroke=none, 8点
        Polyline pl6 = new Polyline(null, Color.MAGENTA);
        addPoints(pl6, new int[]{270,300,320,340,280,390,420,280},
                       new int[]{225,245,225,245,280,280,240,185});
        canvas.addFigure(pl6);

        showAndConfirm("Polyline", canvas, "W3C_SVG_11_TestSuite/png/shapes-polyline-01-t.png");
    }

    // -----------------------------------------------------------------------
    // shapes-circle-01-t.svg/png (前回 OK のため座標維持)
    // -----------------------------------------------------------------------
    @Test
    void testCircleVisual() throws Exception {
        assumeNotHeadless();
        Canvas canvas = makeCanvas();
        // cx=100 cy=100 r=50, stroke=black, fill=none
        canvas.addFigure(new Circle(100, 100, 50, Color.BLACK,          null));
        // cx=220 cy=100 r=35, fill=green (#008000), stroke=black
        canvas.addFigure(new Circle(220, 100, 35, Color.BLACK,          new Color(0, 128, 0)));
        // cx=340 cy=100 r=20, fill=black, stroke=lime
        canvas.addFigure(new Circle(340, 100, 20, Color.GREEN,          Color.BLACK));
        // cx=100 cy=260 r=20, stroke=lime, fill=yellow
        canvas.addFigure(new Circle(100, 260, 20, Color.GREEN,          Color.YELLOW));
        // cx=220 cy=260 r=35, stroke=none, fill=blue
        canvas.addFigure(new Circle(220, 260, 35, null,                 Color.BLUE));
        // cx=340 cy=260 r=50, stroke=green (#008000), fill=none
        canvas.addFigure(new Circle(340, 260, 50, new Color(0, 128, 0), null));
        showAndConfirm("Circle", canvas, "W3C_SVG_11_TestSuite/png/shapes-circle-01-t.png");
    }

    // -----------------------------------------------------------------------
    // shapes-ellipse-01-t.svg/png (前回 OK のため座標維持)
    // -----------------------------------------------------------------------
    @Test
    void testEllipseVisual() throws Exception {
        assumeNotHeadless();
        Canvas canvas = makeCanvas();
        // ellipse-01: cx=50 cy=75 rx=30 ry=50 → (20,25,80,125)
        canvas.addFigure(new Ellipse( 20,  25,  80, 125, Color.BLACK,          null));
        // ellipse-02: cx=160 cy=75 rx=30 ry=50 → (130,25,190,125)
        canvas.addFigure(new Ellipse(130,  25, 190, 125, null,                 new Color(0, 128, 0)));
        // ellipse-03: cx=270 cy=80 rx=ry=35 → Circle (SVG comment: "should actually draw circles")
        canvas.addFigure(new Circle(270,  80,  35, Color.BLACK,                null));
        // ellipse-04: cx=370 cy=80 rx=ry=35 → Circle
        canvas.addFigure(new Circle(370,  80,  35, null,                       new Color(0, 128, 0)));
        // ellipse-05: cx=50 cy=220 rx=30 ry=50 → (20,170,80,270)
        canvas.addFigure(new Ellipse( 20, 170,  80, 270, Color.BLUE,           null));
        // ellipse-06: cx=160 cy=220 rx=30 ry=50 → (130,170,190,270)
        canvas.addFigure(new Ellipse(130, 170, 190, 270, Color.BLUE,           Color.GREEN));
        // ellipse-07: cx=330 cy=220 rx=70 ry=40 → (260,180,400,260)
        canvas.addFigure(new Ellipse(260, 180, 400, 260, Color.BLUE,           Color.GREEN));
        showAndConfirm("Ellipse", canvas, "W3C_SVG_11_TestSuite/png/shapes-ellipse-01-t.png");
    }

    // -----------------------------------------------------------------------
    // shapes-line-01-t.svg/png
    // 上段: SVG 座標そのまま (上 OK 済み)
    // 下段: 3 ステップパターン + 右端の 1 線分を追加
    // -----------------------------------------------------------------------
    @Test
    void testLineVisual() throws Exception {
        assumeNotHeadless();
        Canvas canvas = makeCanvas();

        // 上段: 左下→右上 (SVG の x1,y1 → x2,y2 そのまま)
        canvas.addFigure(new Line( 38, 137, 113,  50, Color.BLACK));
        canvas.addFigure(new Line(113, 137, 188,  50, Color.YELLOW));
        canvas.addFigure(new Line(188, 137, 263,  50, new Color(0, 128, 0)));
        canvas.addFigure(new Line(263, 137, 338,  50, Color.BLUE));
        canvas.addFigure(new Line(338, 137, 413,  50, Color.MAGENTA));

        // 下段: SVG の 3 グループをそれぞれ Polyline で描画
        // lower-left-figure: stroke=blue, x=25〜175
        Polyline lowerLeft = new Polyline(Color.BLUE);
        addPoints(lowerLeft,
            new int[]{ 25,  75,  75, 125, 125, 175},
            new int[]{200, 200, 250, 250, 200, 200});
        canvas.addFigure(lowerLeft);

        // middle-figure: stroke=black, x=170〜320
        Polyline middle = new Polyline(Color.BLACK);
        addPoints(middle,
            new int[]{170, 220, 220, 270, 270, 320},
            new int[]{200, 200, 250, 250, 200, 200});
        canvas.addFigure(middle);

        // lower-right-figure: 5 本の個別線分 (各色)
        canvas.addFigure(new Line(320, 200, 370, 200, Color.BLUE));
        canvas.addFigure(new Line(370, 200, 370, 250, Color.GREEN));
        canvas.addFigure(new Line(370, 250, 420, 250, Color.BLACK));
        canvas.addFigure(new Line(420, 250, 420, 200, new Color(255, 165, 0))); // orange
        canvas.addFigure(new Line(420, 200, 470, 200, Color.MAGENTA));          // fuchsia

        showAndConfirm("Line", canvas, "W3C_SVG_11_TestSuite/png/shapes-line-01-t.png");
    }

    // -----------------------------------------------------------------------
    // shapes-rect-01-t.svg/png
    // SVG 座標をそのまま使用 (viewBox=480x360 と一致)
    // 丸角矩形: 矩形 + 円 2 つで近似
    // rx=30 ry=50 の矩形 (50x80): 実効 rx=25, ry=40 → 楕円として描画
    // -----------------------------------------------------------------------
    @Test
    void testRectVisual() throws Exception {
        assumeNotHeadless();
        Canvas canvas = makeCanvas();

        // --- 上段 ---
        // rect-1: x=30 y=46 w=50 h=80, stroke=black
        canvas.addFigure(new Rectangle(30, 46, 80, 126, Color.BLACK, null));
        // rect-2: x=130 y=46 w=50 h=80, fill=fuchsia
        canvas.addFigure(new Rectangle(130, 46, 180, 126, null, Color.MAGENTA));
        // rect-3: x=250 y=46 w=50 h=80 rx=30 → rx_clamp=25 → pill
        //   上円 center(275,71) r=25, 胴 Rectangle(250,71,300,101), 下円 center(275,101) r=25
        canvas.addFigure(new Circle(275,  71, 25, Color.BLACK, null));
        canvas.addFigure(new Rectangle(250, 71, 300, 101, Color.BLACK, null));
        canvas.addFigure(new Circle(275, 101, 25, Color.BLACK, null));
        // rect-4: x=350 y=46 w=50 h=80 rx=30 → fill=fuchsia pill
        canvas.addFigure(new Circle(375,  71, 25, null, Color.MAGENTA));
        canvas.addFigure(new Rectangle(350, 71, 400, 101, null, Color.MAGENTA));
        canvas.addFigure(new Circle(375, 101, 25, null, Color.MAGENTA));

        // --- 下段 ---
        // rect-5: x=30 y=196 w=50 h=80, stroke=blue
        canvas.addFigure(new Rectangle(30, 196, 80, 276, Color.BLUE, null));
        // rect-6: x=130 y=196 w=50 h=80, fill=green stroke=blue
        canvas.addFigure(new Rectangle(130, 196, 180, 276, Color.BLUE, Color.GREEN));
        // rect-7: x=250 y=196 w=50 h=80 rx=30 ry=50 → rx_clamp=25, ry_clamp=40 → 楕円
        canvas.addFigure(new Ellipse(250, 196, 300, 276, Color.BLUE, null));
        // rect-8: x=350 y=196 w=50 h=80 rx=30 ry=50 → fill=green 楕円
        canvas.addFigure(new Ellipse(350, 196, 400, 276, null, Color.GREEN));

        showAndConfirm("Rectangle", canvas, "W3C_SVG_11_TestSuite/png/shapes-rect-01-t.png");
    }

    // -----------------------------------------------------------------------
    // shapes-polygon-01-t.svg/png
    // SVG 座標をそのまま使用
    // -----------------------------------------------------------------------
    @Test
    void testPolygonVisual() throws Exception {
        assumeNotHeadless();
        Canvas canvas = makeCanvas();

        // polygon-01: fill=none, stroke=black (7点)
        Polygon pg1 = new Polygon(Color.BLACK, null);
        addPoints(pg1, new int[]{59, 95,108, 82, 39, 11, 19},
                       new int[]{45, 63,105,139,140,107, 65});
        canvas.addFigure(pg1);

        // polygon-02: fill=blue (8点, 最終点で閉じる)
        Polygon pg2 = new Polygon(null, Color.BLUE);
        addPoints(pg2, new int[]{179,218,228,202,159,131,139,179},
                       new int[]{ 45, 63,105,139,140,107, 65, 45});
        canvas.addFigure(pg2);

        // polygon-03: fill=blue, stroke=black (9点, 4方向星)
        Polygon pg3 = new Polygon(Color.BLACK, Color.BLUE);
        addPoints(pg3, new int[]{350,375,410,375,350,325,290,325,350},
                       new int[]{ 45, 80, 95,110,145,120, 95, 70, 45});
        canvas.addFigure(pg3);

        // polygon-05: fill=none, stroke=blue (8点, 閉じた7角形)
        Polygon pg5 = new Polygon(Color.BLUE, null);
        addPoints(pg5, new int[]{ 59, 98,108, 82, 39, 11, 19, 59},
                       new int[]{185,203,245,279,280,247,205,185});
        canvas.addFigure(pg5);

        // polygon-06: fill=green, stroke=blue (7点)
        Polygon pg6 = new Polygon(Color.BLUE, Color.GREEN);
        addPoints(pg6, new int[]{179,218,228,202,159,131,139},
                       new int[]{185,203,245,279,280,247,205});
        canvas.addFigure(pg6);

        // polygon-07: fill=none, stroke=green (8点, 矢印形)
        Polygon pg7 = new Polygon(Color.GREEN, null);
        addPoints(pg7, new int[]{270,300,320,340,280,390,420,280},
                       new int[]{225,245,225,245,280,280,240,185});
        canvas.addFigure(pg7);

        showAndConfirm("Polygon", canvas, "W3C_SVG_11_TestSuite/png/shapes-polygon-01-t.png");
    }

    // -----------------------------------------------------------------------
    // helpers
    // -----------------------------------------------------------------------

    private static void assumeNotHeadless() {
        Assumptions.assumeTrue(
            !GraphicsEnvironment.isHeadless(),
            "headless environment: visual test skipped");
    }

    private static Canvas makeCanvas() {
        Canvas c = new Canvas();
        c.setPreferredSize(new Dimension(480, 360));
        return c;
    }

    private static void addPoints(Polyline pl, int[] xs, int[] ys) {
        for (int i = 0; i < xs.length; i++) pl.addPoint(xs[i], ys[i]);
    }

    private static void addPoints(Polygon pg, int[] xs, int[] ys) {
        for (int i = 0; i < xs.length; i++) pg.addPoint(xs[i], ys[i]);
    }

    private static void showAndConfirm(String title, Canvas canvas, String refPath)
            throws Exception {
        int[] result = {JOptionPane.NO_OPTION};
        SwingUtilities.invokeAndWait(() -> {
            JLabel refLabel;
            try {
                BufferedImage img = ImageIO.read(new File(refPath));
                refLabel = new JLabel(new ImageIcon(img));
            } catch (IOException ex) {
                refLabel = new JLabel("(参照画像なし: " + refPath + ")");
            }

            JPanel content = new JPanel(new GridLayout(1, 2, 4, 0));
            content.add(canvas);
            content.add(refLabel);

            JFrame frame = new JFrame("Visual Test: " + title);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(content);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            result[0] = JOptionPane.showConfirmDialog(
                frame,
                "左: 描画結果   右: 参照画像\n" + title + " が正しく描画されていますか？",
                "目視確認: " + title,
                JOptionPane.YES_NO_OPTION);
            frame.dispose();
        });
        assertEquals(JOptionPane.YES_OPTION, result[0],
            title + " の目視確認に失敗しました");
    }
}
