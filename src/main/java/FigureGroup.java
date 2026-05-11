import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FigureGroup extends Figure implements Rotatable {
    private final List<Figure> children = new ArrayList<>();

    public FigureGroup() {
        super(null, null);
    }

    public FigureGroup(List<Figure> figures) {
        super(null, null);
        children.addAll(figures);
    }

    public void add(Figure f) { children.add(f); }

    public List<Figure> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    protected void drawShape(Graphics2D g2) {
        for (Figure f : children) f.draw(g2);
    }

    /** グループ全体を平行移動する (transform への合成)． */
    @Override
    public void move(int dx, int dy) {
        transform.preConcatenate(AffineTransform.getTranslateInstance(dx, dy));
    }

    @Override
    protected boolean containsLocal(int x, int y) {
        for (Figure f : children) {
            if (f.contains(x, y)) return true;
        }
        return false;
    }

    @Override
    public void rotate(double theta, double cx, double cy) {
        transform.preConcatenate(AffineTransform.getRotateInstance(theta, cx, cy));
    }

    public void scale(double sx, double sy, double originX, double originY) {
        AffineTransform t = AffineTransform.getTranslateInstance(originX, originY);
        t.scale(sx, sy);
        t.translate(-originX, -originY);
        transform.preConcatenate(t);
    }

    /** ローカル座標軸に沿ってスケールする (transform への後乗算)． */
    public void scaleLocal(double sx, double sy, double localOriginX, double localOriginY) {
        AffineTransform t = AffineTransform.getTranslateInstance(localOriginX, localOriginY);
        t.scale(sx, sy);
        t.translate(-localOriginX, -localOriginY);
        transform.concatenate(t);
    }

    /** 上位 transform を子に合成し，子図形のリストを返す (ungroup)． */
    @Override
    public List<Figure> bakeTransform() {
        List<Figure> result = new ArrayList<>();
        for (Figure child : children) {
            child.getTransform().preConcatenate(transform);
            result.addAll(child.bakeTransform());
        }
        return result;
    }

    /** グループは端点ハンドルを持たない． */
    @Override
    public List<Handle> getHandles() {
        return List.of();
    }

    /**
     * グループの bbox ハンドルを返す (局所空間の boundsLocal() を transform で変換)．
     * - SCALE_NW/NE/SE/SW: bbox 四隅 (スケール操作用)
     * - ROTATE: 上辺中央の外側 (回転操作用)
     */
    @Override
    public List<Handle> getBboxHandles() {
        int[] b = boundsLocal();
        if (b == null) return List.of();
        int bx1 = b[0], by1 = b[1], bx2 = b[2], by2 = b[3];

        double[][] localCorners = {
            {bx1, by1}, {bx2, by1}, {bx2, by2}, {bx1, by2}
        };
        Handle.Type[] types = {
            Handle.Type.SCALE_NW, Handle.Type.SCALE_NE,
            Handle.Type.SCALE_SE, Handle.Type.SCALE_SW
        };

        List<Handle> handles = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Point2D sc = toScreen(localCorners[i][0], localCorners[i][1]);
            handles.add(new Handle(
                (int) Math.round(sc.getX()), (int) Math.round(sc.getY()), types[i], null));
        }

        // 回転ハンドル: 局所空間で上辺中央から -25px (transform 適用後)
        Point2D rotSc = toScreen((bx1 + bx2) / 2.0, by1 - 25);
        handles.add(new Handle(
            (int) Math.round(rotSc.getX()), (int) Math.round(rotSc.getY()),
            Handle.Type.ROTATE, null));

        return handles;
    }

    /**
     * 子図形のハンドル座標から局所空間での bbox を計算する．
     * 子図形は局所空間 (FigureGroup.transform 適用前) に描画される．
     * @return [minX, minY, maxX, maxY] または null (子なし)
     */
    int[] boundsLocal() {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        for (Figure child : children) {
            List<Handle> hs = child.getHandles();
            if (hs.isEmpty()) hs = child.getBboxHandles();
            for (Handle h : hs) {
                minX = Math.min(minX, h.getCx());
                minY = Math.min(minY, h.getCy());
                maxX = Math.max(maxX, h.getCx());
                maxY = Math.max(maxY, h.getCy());
            }
        }
        if (minX == Integer.MAX_VALUE) return null;
        // Expand slightly so handles are outside figure edges
        return new int[]{minX - 4, minY - 4, maxX + 4, maxY + 4};
    }

    @Override
    public String toSvg() {
        StringBuilder sb = new StringBuilder("<g");
        sb.append(transformAttr()).append(">\n");
        for (Figure f : children) sb.append("  ").append(f.toSvg()).append("\n");
        sb.append("</g>");
        return sb.toString();
    }
}
