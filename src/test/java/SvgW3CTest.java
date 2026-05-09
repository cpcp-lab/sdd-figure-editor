import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * W3C SVG 1.1 テストスイートの shapes-*.svg を読み込み，
 * 対応する参照 PNG と画素比較する自動テスト．
 *
 * 許容誤差 10% : テキストラベル未描画・アンチエイリアス差異を考慮．
 */
class SvgW3CTest {

    private static final String SVG_DIR = "W3C_SVG_11_TestSuite/svg/";
    private static final String PNG_DIR = "W3C_SVG_11_TestSuite/png/";
    private static final int WIDTH  = 480;
    private static final int HEIGHT = 360;
    private static final double THRESHOLD = 0.10;  // 10%

    @ParameterizedTest
    @ValueSource(strings = {
        "shapes-circle-01-t",
        "shapes-circle-02-t",
        "shapes-ellipse-01-t",
        "shapes-ellipse-02-t",
        "shapes-intro-01-t",
        "shapes-line-01-t",
        "shapes-polygon-01-t",
        "shapes-polygon-02-t",
        "shapes-polygon-03-t",
        "shapes-polyline-01-t",
        // shapes-polyline-02-t: <path> オーバーレイ要素を含むため除外
        "shapes-rect-01-t",
        "shapes-rect-02-t",
        // shapes-intro-02-f: <path> が主要要素のため除外
        "shapes-rect-04-f",
        "shapes-rect-06-f",
        "shapes-rect-07-f",
    })
    void svgMatchesPng(String name) throws Exception {
        String svgPath = SVG_DIR + name + ".svg";
        String pngPath = PNG_DIR + name + ".png";

        List<Figure> figures = SvgReader.read(svgPath);

        BufferedImage actual = render(figures);
        BufferedImage ref = ImageIO.read(new File(pngPath));

        double diff = pixelDiffRatio(actual, ref);
        assertTrue(diff <= THRESHOLD,
            String.format("%s: diff=%.2f%% > threshold=%.0f%%", name, diff * 100, THRESHOLD * 100));
    }

    private static BufferedImage render(List<Figure> figures) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        for (Figure f : figures) f.draw(g);
        g.dispose();
        return img;
    }

    private static double pixelDiffRatio(BufferedImage a, BufferedImage b) {
        int w = Math.min(a.getWidth(),  b.getWidth());
        int h = Math.min(a.getHeight(), b.getHeight());
        int diff = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                // 参照画像 b は RGBA: 透明部分を白背景に合成してから比較
                int ca = a.getRGB(x, y);
                int cb = compositeOverWhite(b.getRGB(x, y));
                if (!close(ca, cb)) diff++;
            }
        }
        return (double) diff / (w * h);
    }

    // ARGB ピクセルを白 (0xFFFFFF) 背景に α 合成して RGB を返す
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
