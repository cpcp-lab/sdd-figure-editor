import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.jupiter.api.Assumptions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * W3C SVG テストスイートの参照 PNG との画素比較に使う共通ユーティリティ．
 *
 * 実行モード (システムプロパティ -Dvisual.auto=true/false で切り替え):
 *   false (デフォルト): 描画結果と参照画像を並べたウィンドウを表示し，目視で確認する
 *   true             : 参照 PNG との画素比較で自動確認する
 */
class W3CImageComparator {

    static final String SUITE_DIR =
        System.getProperty("w3c.testsuite.dir", "W3C_SVG_11_TestSuite");

    static final double DEFAULT_THRESHOLD = 0.05;

    /** -Dvisual.auto=true のとき自動比較モード，false のとき目視確認モード． */
    static final boolean AUTO = Boolean.getBoolean("visual.auto");

    // --- レンダリング ---

    /** Figure リストを白背景の BufferedImage にレンダリングする． */
    static BufferedImage render(List<Figure> figures, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        for (Figure f : figures) f.draw(g);
        g.dispose();
        return img;
    }

    // --- 確認 (自動 / 目視) ---

    /**
     * モードに応じて自動比較または目視確認を行う．
     * 目視モードではヘッドレス環境でスキップし，自動モードでは常に実行する．
     */
    static void showAndConfirm(String label, BufferedImage actual, String pngPath,
                               double threshold) throws Exception {
        if (AUTO) {
            assertMatchesPng(label, actual, pngPath, threshold);
        } else {
            interactiveConfirm(label, actual, pngPath);
        }
    }

    static void showAndConfirm(String label, BufferedImage actual, String pngPath)
            throws Exception {
        showAndConfirm(label, actual, pngPath, DEFAULT_THRESHOLD);
    }

    /**
     * 目視モードではヘッドレス環境をスキップする．
     * 自動モードでは何もしない (常に実行する)．
     */
    static void assumeDisplay() {
        if (!AUTO) {
            Assumptions.assumeTrue(
                !GraphicsEnvironment.isHeadless(),
                "headless environment: visual test skipped");
        }
    }

    // --- 自動比較 ---

    /** actual と参照 PNG を比較し，差分ピクセル率が threshold 以下であることを表明する． */
    static void assertMatchesPng(String label, BufferedImage actual, String pngPath,
                                 double threshold) throws Exception {
        BufferedImage ref = ImageIO.read(new File(pngPath));
        double diff = pixelDiffRatio(actual, ref);
        assertTrue(diff <= threshold,
            String.format("%s: diff=%.2f%% > threshold=%.0f%%",
                label, diff * 100, threshold * 100));
    }

    static void assertMatchesPng(String label, BufferedImage actual, String pngPath)
            throws Exception {
        assertMatchesPng(label, actual, pngPath, DEFAULT_THRESHOLD);
    }

    // --- 目視確認 ---

    private static void interactiveConfirm(String label, BufferedImage actual, String pngPath)
            throws Exception {
        Assumptions.assumeTrue(
            !GraphicsEnvironment.isHeadless(),
            "headless environment: visual test skipped");
        int[] result = {JOptionPane.NO_OPTION};
        SwingUtilities.invokeAndWait(() -> {
            JLabel actualLabel = new JLabel(new ImageIcon(actual));
            JLabel refLabel;
            try {
                refLabel = new JLabel(new ImageIcon(ImageIO.read(new File(pngPath))));
            } catch (IOException ex) {
                refLabel = new JLabel("(参照画像なし: " + pngPath + ")");
            }

            JPanel content = new JPanel(new GridLayout(1, 2, 4, 0));
            content.add(actualLabel);
            content.add(refLabel);

            JFrame frame = new JFrame("Visual Test: " + label);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(content);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            result[0] = JOptionPane.showConfirmDialog(
                frame,
                "左: 描画結果   右: 参照画像\n" + label + " が正しく描画されていますか？",
                "目視確認: " + label,
                JOptionPane.YES_NO_OPTION);
            frame.dispose();
        });
        assertEquals(JOptionPane.YES_OPTION, result[0],
            label + " の目視確認に失敗しました");
    }

    // --- 画素差分計算 ---

    /** RGBA 参照画像を白背景に α 合成した上で画素比較し，差分ピクセル率を返す． */
    static double pixelDiffRatio(BufferedImage a, BufferedImage b) {
        int w = Math.min(a.getWidth(),  b.getWidth());
        int h = Math.min(a.getHeight(), b.getHeight());
        int diff = 0;
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                if (!close(a.getRGB(x, y), compositeOverWhite(b.getRGB(x, y)))) diff++;
        return (double) diff / (w * h);
    }

    private static int compositeOverWhite(int argb) {
        int alpha = (argb >>> 24) & 0xff;
        if (alpha == 255) return argb;
        if (alpha == 0)   return 0xFFFFFFFF;
        int r = ((argb >> 16) & 0xff) * alpha / 255 + 255 * (255 - alpha) / 255;
        int g = ((argb >>  8) & 0xff) * alpha / 255 + 255 * (255 - alpha) / 255;
        int b = ( argb        & 0xff) * alpha / 255 + 255 * (255 - alpha) / 255;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static boolean close(int ca, int cb) {
        int dr = Math.abs(((ca >> 16) & 0xff) - ((cb >> 16) & 0xff));
        int dg = Math.abs(((ca >>  8) & 0xff) - ((cb >>  8) & 0xff));
        int db = Math.abs(( ca        & 0xff) - ( cb        & 0xff));
        return dr < 30 && dg < 30 && db < 30;
    }
}
