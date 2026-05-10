import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SvgReader {

    // SVG スタイルの継承コンテキスト
    private record StyleCtx(Color fill, Color stroke, float strokeWidth, AffineTransform ctxTransform) {
        static final StyleCtx DEFAULT =
            new StyleCtx(Color.BLACK, null, 1.0f, new AffineTransform());

        StyleCtx merge(Element el) {
            Color f  = has(el, "fill")         ? SvgColor.parse(el.getAttribute("fill"))   : fill;
            Color s  = has(el, "stroke")       ? SvgColor.parse(el.getAttribute("stroke")) : stroke;
            float sw = has(el, "stroke-width")
                ? Float.parseFloat(el.getAttribute("stroke-width").trim()) : strokeWidth;
            AffineTransform elT = parseTransform(el.getAttribute("transform"));
            AffineTransform combined = new AffineTransform(ctxTransform);
            combined.concatenate(elT);
            return new StyleCtx(f, s, sw, combined);
        }

        private static boolean has(Element el, String attr) {
            return el.hasAttribute(attr);
        }
    }

    // --- transform パーサ ---

    private static final Pattern FUNC_PAT =
        Pattern.compile("(translate|scale|rotate|skewX|skewY|matrix)\\(([^)]*)\\)");
    private static final Pattern NUM_PAT =
        Pattern.compile("[+-]?(?:\\d+\\.?\\d*|\\.\\d+)(?:[eE][+-]?\\d+)?");

    /**
     * SVG transform 属性文字列を AffineTransform に変換する．
     * translate, scale, rotate, skewX, skewY, matrix の連結に対応する．
     */
    static AffineTransform parseTransform(String s) {
        AffineTransform result = new AffineTransform();
        if (s == null || s.isBlank()) return result;
        Matcher m = FUNC_PAT.matcher(s);
        while (m.find()) {
            String func = m.group(1);
            double[] a  = parseNums(m.group(2));
            AffineTransform t = switch (func) {
                case "translate" -> AffineTransform.getTranslateInstance(
                    a.length > 0 ? a[0] : 0,
                    a.length > 1 ? a[1] : 0);
                case "scale" -> AffineTransform.getScaleInstance(
                    a.length > 0 ? a[0] : 1,
                    a.length > 1 ? a[1] : a[0]);
                case "rotate" -> a.length >= 3
                    ? AffineTransform.getRotateInstance(Math.toRadians(a[0]), a[1], a[2])
                    : AffineTransform.getRotateInstance(Math.toRadians(a.length > 0 ? a[0] : 0));
                case "skewX" -> {
                    AffineTransform sk = new AffineTransform();
                    sk.shear(Math.tan(Math.toRadians(a.length > 0 ? a[0] : 0)), 0);
                    yield sk;
                }
                case "skewY" -> {
                    AffineTransform sk = new AffineTransform();
                    sk.shear(0, Math.tan(Math.toRadians(a.length > 0 ? a[0] : 0)));
                    yield sk;
                }
                // SVG matrix(a,b,c,d,e,f) → Java AffineTransform(m00,m10,m01,m11,m02,m12)
                case "matrix" -> a.length >= 6
                    ? new AffineTransform(a[0], a[1], a[2], a[3], a[4], a[5])
                    : new AffineTransform();
                default -> new AffineTransform();
            };
            result.concatenate(t);
        }
        return result;
    }

    private static double[] parseNums(String s) {
        if (s == null || s.isBlank()) return new double[0];
        List<Double> vals = new ArrayList<>();
        Matcher m = NUM_PAT.matcher(s);
        while (m.find()) vals.add(Double.parseDouble(m.group()));
        return vals.stream().mapToDouble(Double::doubleValue).toArray();
    }

    // --- エントリポイント ---

    public static List<Figure> read(String path) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        Document doc = factory.newDocumentBuilder().parse(Path.of(path).toFile());
        doc.getDocumentElement().normalize();
        List<Figure> figures = new ArrayList<>();
        processChildren(doc.getDocumentElement(), StyleCtx.DEFAULT, figures);
        return figures;
    }

    private static void processChildren(Element el, StyleCtx ctx, List<Figure> out) {
        NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n instanceof Element child) processElement(child, ctx, out);
        }
    }

    private static void processElement(Element el, StyleCtx ctx, List<Figure> out) {
        // SVG 名前空間プレフィックスを除去した要素名
        String tag = el.getLocalName();
        if (tag == null) tag = el.getTagName();
        int colon = tag.indexOf(':');
        if (colon >= 0) tag = tag.substring(colon + 1);

        switch (tag) {
            case "g"        -> {
                List<Figure> groupChildren = new ArrayList<>();
                processChildren(el, ctx.merge(el), groupChildren);
                if (!groupChildren.isEmpty()) out.add(new FigureGroup(groupChildren));
            }
            case "circle"   -> { Figure c = makeCircle(el, ctx);  if (c != null) out.add(c); }
            case "ellipse"  -> { Figure e = makeEllipse(el, ctx); if (e != null) out.add(e); }
            case "line"     -> out.add(makeLine(el, ctx));
            case "rect"     -> { Figure r = makeRect(el, ctx); if (r != null) out.add(r); }
            case "polyline" -> out.add(makePolyline(el, ctx));
            case "polygon"  -> out.add(makePolygon(el, ctx));
            case "path"     -> { Polygon pg = makePathPolygon(el, ctx);
                                 if (pg != null) out.add(pg); }
            // text, defs, use などはスキップ
        }
    }

    /** 累積 transform が identity でなければ figure.transform にセットする． */
    private static void applyTransform(Figure fig, StyleCtx s) {
        if (!s.ctxTransform().isIdentity())
            fig.getTransform().setTransform(s.ctxTransform());
    }

    // --- shape ファクトリ ---
    // ローカル座標 (SVG 属性値をそのまま使用) で Figure を構築し，
    // 累積 AffineTransform を figure.transform にセットする．

    // r が 0 の場合，SVG 仕様により描画しない (null を返す)
    private static Circle makeCircle(Element el, StyleCtx ctx) {
        int cx = intAttr(el, "cx", 0);
        int cy = intAttr(el, "cy", 0);
        int r  = intAttr(el, "r",  0);
        if (r == 0) return null;
        StyleCtx s = ctx.merge(el);
        Circle c = new Circle(cx, cy, r, s.stroke(), s.fill());
        c.setStrokeWidth(s.strokeWidth());
        applyTransform(c, s);
        return c;
    }

    // rx または ry が 0 の場合，SVG 仕様により描画しない (null を返す)
    private static Ellipse makeEllipse(Element el, StyleCtx ctx) {
        int cx = intAttr(el, "cx", 0);
        int cy = intAttr(el, "cy", 0);
        int rx = intAttr(el, "rx", 0);
        int ry = intAttr(el, "ry", 0);
        if (rx == 0 || ry == 0) return null;
        StyleCtx s = ctx.merge(el);
        Ellipse e = new Ellipse(cx - rx, cy - ry, cx + rx, cy + ry, s.stroke(), s.fill());
        e.setStrokeWidth(s.strokeWidth());
        applyTransform(e, s);
        return e;
    }

    private static Line makeLine(Element el, StyleCtx ctx) {
        double x1 = doubleAttr(el, "x1", 0);
        double y1 = doubleAttr(el, "y1", 0);
        double x2 = doubleAttr(el, "x2", 0);
        double y2 = doubleAttr(el, "y2", 0);
        StyleCtx s = ctx.merge(el);
        Line l = new Line(x1, y1, x2, y2, s.stroke());
        l.setStrokeWidth(s.strokeWidth());
        applyTransform(l, s);
        return l;
    }

    // width または height が 0 の場合，SVG 仕様により描画しない (null を返す)
    private static Rectangle makeRect(Element el, StyleCtx ctx) {
        int x = intAttr(el, "x", 0);
        int y = intAttr(el, "y", 0);
        int w = intAttr(el, "width",  0);
        int h = intAttr(el, "height", 0);
        if (w == 0 || h == 0) return null;
        int rx = intAttr(el, "rx", -1);
        int ry = intAttr(el, "ry", -1);
        if (rx < 0 && ry < 0) { rx = 0; ry = 0; }
        else if (rx < 0) rx = ry;
        else if (ry < 0) ry = rx;
        StyleCtx s = ctx.merge(el);
        Rectangle r = new Rectangle(x, y, x + w, y + h, s.stroke(), s.fill());
        r.setRoundedCorners(rx, ry);
        r.setStrokeWidth(s.strokeWidth());
        applyTransform(r, s);
        return r;
    }

    private static Polyline makePolyline(Element el, StyleCtx ctx) {
        StyleCtx s = ctx.merge(el);
        Polyline pl = new Polyline(s.stroke(), s.fill());
        pl.setStrokeWidth(s.strokeWidth());
        addPoints(pl, el.getAttribute("points"));
        applyTransform(pl, s);
        return pl;
    }

    private static Polygon makePolygon(Element el, StyleCtx ctx) {
        StyleCtx s = ctx.merge(el);
        Polygon pg = new Polygon(s.stroke(), s.fill());
        pg.setStrokeWidth(s.strokeWidth());
        addPoints(pg, el.getAttribute("points"));
        applyTransform(pg, s);
        return pg;
    }

    /**
     * M/L/Z のみで構成された閉じたパスを Polygon に変換する．
     * カーブ命令 (C/Q/A など) を含む場合は null を返してスキップする．
     */
    private static Polygon makePathPolygon(Element el, StyleCtx ctx) {
        String d = el.getAttribute("d");
        if (d == null || d.isBlank()) return null;
        // 改行・タブを空白に正規化してから大文字 M/L/Z のみ許容 (小文字相対座標は未対応)
        String upper = d.trim().replaceAll("\\s+", " ").toUpperCase();
        if (!upper.matches("[MLZ0-9 ,+\\-.eE]+")) return null;
        // コマンドと座標を分割して解析
        List<double[]> points = new ArrayList<>();
        Matcher m = Pattern.compile("([MLZ])([^MLZ]*)").matcher(upper);
        boolean closed = false;
        while (m.find()) {
            String cmd = m.group(1);
            double[] nums = parseNums(m.group(2));
            switch (cmd) {
                case "M", "L" -> {
                    for (int i = 0; i + 1 < nums.length; i += 2)
                        points.add(new double[]{nums[i], nums[i + 1]});
                }
                case "Z" -> closed = true;
            }
        }
        if (!closed || points.size() < 2) return null;
        StyleCtx s = ctx.merge(el);
        Polygon pg = new Polygon(s.stroke(), s.fill());
        pg.setStrokeWidth(s.strokeWidth());
        for (double[] p : points)
            pg.addPoint((int) Math.round(p[0]), (int) Math.round(p[1]));
        applyTransform(pg, s);
        return pg;
    }

    // --- helpers ---

    private static void addPoints(Polyline pl, String points) {
        double[] coords = parsePoints(points);
        for (int i = 0; i + 1 < coords.length; i += 2)
            pl.addPoint((int) Math.round(coords[i]), (int) Math.round(coords[i + 1]));
    }

    private static void addPoints(Polygon pg, String points) {
        double[] coords = parsePoints(points);
        for (int i = 0; i + 1 < coords.length; i += 2)
            pg.addPoint((int) Math.round(coords[i]), (int) Math.round(coords[i + 1]));
    }

    private static double[] parsePoints(String s) {
        if (s == null || s.isBlank()) return new double[0];
        String[] tokens = s.trim().split("[,\\s]+");
        double[] vals = new double[tokens.length];
        int n = 0;
        for (String t : tokens) {
            if (!t.isEmpty()) {
                try { vals[n++] = Double.parseDouble(t); } catch (NumberFormatException ignored) {}
            }
        }
        double[] result = new double[n];
        System.arraycopy(vals, 0, result, 0, n);
        return result;
    }

    private static int intAttr(Element el, String name, int def) {
        String v = el.getAttribute(name);
        if (v == null || v.isEmpty()) return def;
        try { return (int) Math.round(Double.parseDouble(v.trim())); }
        catch (NumberFormatException e) { return def; }
    }

    private static double doubleAttr(Element el, String name, double def) {
        String v = el.getAttribute(name);
        if (v == null || v.isEmpty()) return def;
        try { return Double.parseDouble(v.trim()); }
        catch (NumberFormatException e) { return def; }
    }
}
