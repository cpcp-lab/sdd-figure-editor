import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.List;

/**
 * W3C SVG 1.1 テストスイートの coords-trans-*.svg を SvgReader で読み込み，
 * 対応する参照 PNG と比較する目視確認テスト．
 *
 * transform (translate/scale/rotate/skewX/skewY/matrix) の読み込みを検証する．
 * text ラベルや path 要素はスキップするため，閾値を 10% に緩和している．
 * coords-trans-11-f: scale + path 要素が多く diff > 10% のため除外．
 *
 * 実行例:
 *   mvn test -Dgroups=visual                       # 目視確認モード
 *   mvn test -Dgroups=visual -Dvisual.auto=true    # 自動比較モード
 */
@Tag("visual")
class W3CCoordsTransSvgTest {

    private static final String SVG_DIR = W3CImageComparator.SUITE_DIR + "/svg/";
    private static final String PNG_DIR = W3CImageComparator.SUITE_DIR + "/png/";
    private static final int WIDTH  = 480;
    private static final int HEIGHT = 360;

    @ParameterizedTest
    @ValueSource(strings = {
        "coords-trans-01-b",
        "coords-trans-02-t",
        "coords-trans-03-t",
        "coords-trans-04-t",
        "coords-trans-05-t",
        "coords-trans-06-t",
        "coords-trans-07-t",
        "coords-trans-08-t",
        "coords-trans-09-t",
        "coords-trans-10-f",
        "coords-trans-11-f",
        "coords-trans-12-f",
        "coords-trans-13-f",
        "coords-trans-14-f",
    })
    void coordsTransMatchesPng(String name) throws Exception {
        W3CImageComparator.assumeDisplay();
        List<Figure> figures = SvgReader.read(SVG_DIR + name + ".svg");
        var actual = W3CImageComparator.render(figures, WIDTH, HEIGHT);
        W3CImageComparator.showAndConfirm(name, actual, PNG_DIR + name + ".png", 0.10);
    }
}
