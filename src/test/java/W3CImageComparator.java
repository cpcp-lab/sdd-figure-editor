import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * W3C SVG テストスイートの参照 PNG との画素比較に使う共通ユーティリティ．
 */
class W3CImageComparator {

    static final String SUITE_DIR =
        System.getProperty("w3c.testsuite.dir", "W3C_SVG_11_TestSuite");

    static final double DEFAULT_THRESHOLD = 0.10;

    // Figure リストを白背景の BufferedImage にレンダリングする
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

    // actual と参照 PNG を比較し，差分ピクセル率が threshold 以下であることを表明する
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

    // RGBA 参照画像を白背景に α 合成した上で画素比較し，差分ピクセル率を返す
    static double pixelDiffRatio(BufferedImage a, BufferedImage b) {
        int w = Math.min(a.getWidth(),  b.getWidth());
        int h = Math.min(a.getHeight(), b.getHeight());
        int diff = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int ca = a.getRGB(x, y);
                int cb = compositeOverWhite(b.getRGB(x, y));
                if (!close(ca, cb)) diff++;
            }
        }
        return (double) diff / (w * h);
    }

    // ARGB ピクセルを白背景に α 合成して返す
    private static int compositeOverWhite(int argb) {
        int alpha = (argb >>> 24) & 0xff;
        if (alpha == 255) return argb;
        if (alpha == 0)   return 0xFFFFFFFF;
        int r = ((argb >> 16) & 0xff) * alpha / 255 + 255 * (255 - alpha) / 255;
        int g = ((argb >>  8) & 0xff) * alpha / 255 + 255 * (255 - alpha) / 255;
        int b = ( argb        & 0xff) * alpha / 255 + 255 * (255 - alpha) / 255;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    // RGB 各チャネルが 30 以内なら同一とみなす
    private static boolean close(int ca, int cb) {
        int dr = Math.abs(((ca >> 16) & 0xff) - ((cb >> 16) & 0xff));
        int dg = Math.abs(((ca >>  8) & 0xff) - ((cb >>  8) & 0xff));
        int db = Math.abs(( ca        & 0xff) - ( cb        & 0xff));
        return dr < 30 && dg < 30 && db < 30;
    }
}
