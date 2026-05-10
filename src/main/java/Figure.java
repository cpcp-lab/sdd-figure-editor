import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public abstract class Figure {
    protected Color strokeColor;
    protected Color fillColor;
    protected float strokeWidth = 1.0f;
    protected AffineTransform transform = new AffineTransform();

    protected Figure(Color strokeColor, Color fillColor) {
        this.strokeColor = strokeColor;
        this.fillColor = fillColor;
    }

    public void setStrokeWidth(float w) { strokeWidth = w; }
    public float getStrokeWidth() { return strokeWidth; }
    public void setStrokeColor(Color c) { strokeColor = c; }
    public void setFillColor(Color c)   { fillColor = c; }
    public AffineTransform getTransform() { return transform; }

    protected void applyStroke(Graphics2D g2) {
        g2.setStroke(new BasicStroke(strokeWidth));
    }

    public final void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.transform(transform);
        drawShape(g2);
        g2.dispose();
    }

    protected abstract void drawShape(Graphics2D g2);

    public final boolean contains(int x, int y) {
        try {
            Point2D pt = transform.inverseTransform(new Point2D.Double(x, y), null);
            return containsLocal((int) Math.round(pt.getX()), (int) Math.round(pt.getY()));
        } catch (NoninvertibleTransformException e) {
            return false;
        }
    }

    protected abstract boolean containsLocal(int x, int y);

    public abstract void move(int dx, int dy);

    /** 図形固有の制御点ハンドル (端点・頂点など) を返す． */
    public abstract List<Handle> getHandles();

    /**
     * transform をローカル座標に焼き込み，この Figure の代わりに配置する Figure のリストを返す．
     * デフォルト実装は transform を保持したまま自身を返す．
     * FigureGroup はオーバーライドして子図形のリストを返す (ungroup)．
     */
    public List<Figure> bakeTransform() { return List.of(this); }

    /**
     * バウンディングボックスハンドルを返す．
     * - Rotatable 図形 (FigureGroup 以外): ROTATE ハンドルのみ
     * - FigureGroup: getBboxHandles() をオーバーライドして SCALE + ROTATE を返す
     * - その他: 空リスト
     */
    public List<Handle> getBboxHandles() {
        if (!(this instanceof Rotatable)) return List.of();
        List<Handle> ep = getHandles();
        if (ep.isEmpty()) return List.of();
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        for (Handle h : ep) {
            minX = Math.min(minX, h.getCx()); minY = Math.min(minY, h.getCy());
            maxX = Math.max(maxX, h.getCx()); maxY = Math.max(maxY, h.getCy());
        }
        minX -= 4; minY -= 4; maxX += 4; maxY += 4;
        return List.of(new Handle((minX + maxX) / 2, minY - 25, Handle.Type.ROTATE, null));
    }

    public abstract String toSvg();

    /** スクリーン空間で (dx, dy) だけ平行移動する．transform に平行移動を合成する． */
    public final void moveInScreen(int dx, int dy) {
        transform.preConcatenate(AffineTransform.getTranslateInstance(dx, dy));
    }

    protected String transformAttr() {
        if (transform.isIdentity()) return "";
        double[] m = new double[6];
        transform.getMatrix(m);
        return String.format(" transform=\"matrix(%s %s %s %s %s %s)\"",
            fmtD(m[0]), fmtD(m[1]), fmtD(m[2]), fmtD(m[3]), fmtD(m[4]), fmtD(m[5]));
    }

    private static String fmtD(double v) {
        // Round to 6 decimal places for clean SVG output
        double r = Math.round(v * 1e6) / 1e6;
        return (r == (long) r) ? String.valueOf((long) r) : String.valueOf(r);
    }

    protected String strokeAttrs() {
        String sw = (strokeWidth == (int) strokeWidth)
            ? String.valueOf((int) strokeWidth)
            : String.valueOf(strokeWidth);
        return String.format("fill=\"%s\" stroke=\"%s\" stroke-width=\"%s\"",
            SvgColor.toSvg(fillColor), SvgColor.toSvg(strokeColor), sw);
    }

    protected Point2D toScreen(double lx, double ly) {
        return transform.transform(new Point2D.Double(lx, ly), null);
    }

    protected Point2D toLocal(int sx, int sy) {
        try {
            return transform.inverseTransform(new Point2D.Double(sx, sy), null);
        } catch (NoninvertibleTransformException e) {
            return new Point2D.Double(sx, sy);
        }
    }
}
