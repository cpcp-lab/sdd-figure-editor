import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.List;

/**
 * W3C SVG 1.1 テストスイートの shapes-*.svg を読み込み，
 * 対応する参照 PNG と画素比較する自動テスト．
 *
 * 許容誤差 10% : テキストラベル未描画・アンチエイリアス差異を考慮．
 */
class W3CShapesSvgTest {

    private static final String SVG_DIR = W3CImageComparator.SUITE_DIR + "/svg/";
    private static final String PNG_DIR = W3CImageComparator.SUITE_DIR + "/png/";
    private static final int WIDTH  = 480;
    private static final int HEIGHT = 360;

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
        List<Figure> figures = SvgReader.read(SVG_DIR + name + ".svg");
        var actual = W3CImageComparator.render(figures, WIDTH, HEIGHT);
        W3CImageComparator.assertMatchesPng(name, actual, PNG_DIR + name + ".png");
    }
}
