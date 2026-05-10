import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.List;

/**
 * W3C SVG 1.1 テストスイートの SVG ファイルを SvgReader で読み込み，
 * 対応する参照 PNG と比較する目視確認テスト．
 *
 * 実行モード (システムプロパティ -Dvisual.auto=true/false で切り替え):
 *   false (デフォルト): 描画結果と参照画像を並べたウィンドウを表示し，目視で確認する
 *   true             : 参照 PNG との画素比較で自動確認する
 *
 * 実行例:
 *   mvn test -Dgroups=visual                       # 目視確認モード
 *   mvn test -Dgroups=visual -Dvisual.auto=true    # 自動比較モード
 */
@Tag("visual")
class W3CShapesSvgTest {

    private static final String SVG_DIR = W3CImageComparator.SUITE_DIR + "/svg/";
    private static final String PNG_DIR = W3CImageComparator.SUITE_DIR + "/png/";
    private static final int WIDTH  = 480;
    private static final int HEIGHT = 360;

    /**
     * shapes-*.svg: 基本図形の読み込みテスト．
     * text ラベルのスキップ分を考慮して閾値 5%．
     */
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
        // struct-group-02-b: ネストした <svg> 要素 (x/y オフセット付き) を使用するため除外
        // struct-group-01-t: rotate transform 未対応のため除外
        // struct-group-03-t: <use> 要素未対応のため除外
    })
    void svgMatchesPng(String name) throws Exception {
        W3CImageComparator.assumeDisplay();
        List<Figure> figures = SvgReader.read(SVG_DIR + name + ".svg");
        var actual = W3CImageComparator.render(figures, WIDTH, HEIGHT);
        W3CImageComparator.showAndConfirm(name, actual, PNG_DIR + name + ".png");
    }


}
