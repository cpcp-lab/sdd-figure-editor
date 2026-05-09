import java.awt.Color;
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
    private record StyleCtx(Color fill, Color stroke, float strokeWidth, int tx, int ty) {
        static final StyleCtx DEFAULT = new StyleCtx(Color.BLACK, null, 1.0f, 0, 0);

        StyleCtx merge(Element el) {
            Color f = has(el, "fill")         ? SvgColor.parse(el.getAttribute("fill"))   : fill;
            Color s = has(el, "stroke")       ? SvgColor.parse(el.getAttribute("stroke")) : stroke;
            float sw = has(el, "stroke-width")
                ? Float.parseFloat(el.getAttribute("stroke-width").trim()) : strokeWidth;
            int[] t = parseTranslate(el.getAttribute("transform"));
            return new StyleCtx(f, s, sw, tx + t[0], ty + t[1]);
        }

        private static boolean has(Element el, String attr) {
            return el.hasAttribute(attr);
        }
    }

    private static final Pattern TRANSLATE_PAT =
        Pattern.compile("translate\\(\\s*([+-]?[\\d.]+)(?:[,\\s]+([+-]?[\\d.]+))?\\s*\\)");

    private static int[] parseTranslate(String transform) {
        if (transform == null || transform.isEmpty()) return new int[]{0, 0};
        Matcher m = TRANSLATE_PAT.matcher(transform);
        if (m.find()) {
            int x = (int) Double.parseDouble(m.group(1));
            int y = m.group(2) != null ? (int) Double.parseDouble(m.group(2)) : 0;
            return new int[]{x, y};
        }
        return new int[]{0, 0};
    }

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
            case "g"        -> processChildren(el, ctx.merge(el), out);
            case "circle"   -> out.add(makeCircle(el, ctx));
            case "ellipse"  -> out.add(makeEllipse(el, ctx));
            case "line"     -> out.add(makeLine(el, ctx));
            case "rect"     -> out.add(makeRect(el, ctx));
            case "polyline" -> out.add(makePolyline(el, ctx));
            case "polygon"  -> out.add(makePolygon(el, ctx));
            // text, defs, use などはスキップ
        }
    }

    // --- shape factories ---

    private static Circle makeCircle(Element el, StyleCtx ctx) {
        int cx = intAttr(el, "cx", 0) + ctx.tx();
        int cy = intAttr(el, "cy", 0) + ctx.ty();
        int r  = intAttr(el, "r",  0);
        StyleCtx s = ctx.merge(el);
        Circle c = new Circle(cx, cy, r, s.stroke(), s.fill());
        c.setStrokeWidth(s.strokeWidth());
        return c;
    }

    private static Ellipse makeEllipse(Element el, StyleCtx ctx) {
        int cx = intAttr(el, "cx", 0) + ctx.tx();
        int cy = intAttr(el, "cy", 0) + ctx.ty();
        int rx = intAttr(el, "rx", 0);
        int ry = intAttr(el, "ry", 0);
        StyleCtx s = ctx.merge(el);
        Ellipse e = new Ellipse(cx - rx, cy - ry, cx + rx, cy + ry, s.stroke(), s.fill());
        e.setStrokeWidth(s.strokeWidth());
        return e;
    }

    private static Line makeLine(Element el, StyleCtx ctx) {
        int x1 = (int) Math.round(doubleAttr(el, "x1", 0)) + ctx.tx();
        int y1 = (int) Math.round(doubleAttr(el, "y1", 0)) + ctx.ty();
        int x2 = (int) Math.round(doubleAttr(el, "x2", 0)) + ctx.tx();
        int y2 = (int) Math.round(doubleAttr(el, "y2", 0)) + ctx.ty();
        StyleCtx s = ctx.merge(el);
        Line l = new Line(x1, y1, x2, y2, s.stroke());
        l.setStrokeWidth(s.strokeWidth());
        return l;
    }

    private static Rectangle makeRect(Element el, StyleCtx ctx) {
        int x = intAttr(el, "x", 0) + ctx.tx();
        int y = intAttr(el, "y", 0) + ctx.ty();
        int w = intAttr(el, "width",  0);
        int h = intAttr(el, "height", 0);
        int rx = intAttr(el, "rx", -1);
        int ry = intAttr(el, "ry", -1);
        // rx/ry のデフォルト: 片方が指定されたらもう片方はその値を引き継ぐ
        if (rx < 0 && ry < 0) { rx = 0; ry = 0; }
        else if (rx < 0) rx = ry;
        else if (ry < 0) ry = rx;
        StyleCtx s = ctx.merge(el);
        Rectangle r = new Rectangle(x, y, x + w, y + h, s.stroke(), s.fill());
        r.setRoundedCorners(rx, ry);
        r.setStrokeWidth(s.strokeWidth());
        return r;
    }

    private static Polyline makePolyline(Element el, StyleCtx ctx) {
        StyleCtx s = ctx.merge(el);
        Polyline pl = new Polyline(s.stroke(), s.fill());
        pl.setStrokeWidth(s.strokeWidth());
        addPoints(pl, el.getAttribute("points"), ctx.tx(), ctx.ty());
        return pl;
    }

    private static Polygon makePolygon(Element el, StyleCtx ctx) {
        StyleCtx s = ctx.merge(el);
        Polygon pg = new Polygon(s.stroke(), s.fill());
        pg.setStrokeWidth(s.strokeWidth());
        addPoints(pg, el.getAttribute("points"), ctx.tx(), ctx.ty());
        return pg;
    }

    // --- helpers ---

    private static void addPoints(Polyline pl, String points, int tx, int ty) {
        double[] coords = parsePoints(points);
        for (int i = 0; i + 1 < coords.length; i += 2)
            pl.addPoint((int) Math.round(coords[i]) + tx, (int) Math.round(coords[i + 1]) + ty);
    }

    private static void addPoints(Polygon pg, String points, int tx, int ty) {
        double[] coords = parsePoints(points);
        for (int i = 0; i + 1 < coords.length; i += 2)
            pg.addPoint((int) Math.round(coords[i]) + tx, (int) Math.round(coords[i + 1]) + ty);
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
