import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Canvas extends JPanel {
    private final List<Figure> figures = new ArrayList<>();
    private final Set<Figure> selected = new LinkedHashSet<>();
    private Figure preview;
    private Rectangle selectionRect;
    private float currentStrokeWidth = 1.0f;
    private Color currentStrokeColor = Color.BLACK;
    private Color currentFillColor   = null;

    public Canvas() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));
    }

    public float getCurrentStrokeWidth() { return currentStrokeWidth; }

    public void setCurrentStrokeWidth(float w) {
        currentStrokeWidth = w;
        for (Figure f : selected) f.setStrokeWidth(w);
        if (!selected.isEmpty()) repaint();
    }

    public Color getCurrentStrokeColor() { return currentStrokeColor; }

    public void setCurrentStrokeColor(Color c) {
        currentStrokeColor = c;
        for (Figure f : selected) f.setStrokeColor(c);
        if (!selected.isEmpty()) repaint();
    }

    public Color getCurrentFillColor() { return currentFillColor; }

    public void setCurrentFillColor(Color c) {
        currentFillColor = c;
        for (Figure f : selected) f.setFillColor(c);
        if (!selected.isEmpty()) repaint();
    }

    public void addFigure(Figure f) {
        figures.add(f);
        repaint();
    }

    public List<Figure> getFigures() {
        return new ArrayList<>(figures);
    }

    public void setFigures(List<Figure> newFigures) {
        figures.clear();
        figures.addAll(newFigures);
        selected.clear();
        repaint();
    }

    public void setPreview(Figure f) {
        preview = f;
        repaint();
    }

    public Set<Figure> getSelection() {
        return Collections.unmodifiableSet(selected);
    }

    public void select(Figure f, boolean addToSelection) {
        if (!addToSelection) selected.clear();
        if (f != null) selected.add(f);
        repaint();
    }

    public void clearSelection() {
        selected.clear();
        repaint();
    }

    public void setSelectionRect(Rectangle r) {
        selectionRect = r;
        repaint();
    }

    public List<Figure> figuresInRect(Rectangle rect) {
        List<Figure> result = new ArrayList<>();
        int step = 6;
        for (Figure f : figures) {
            outer:
            for (int x = rect.x; x <= rect.x + rect.width; x += step) {
                for (int y = rect.y; y <= rect.y + rect.height; y += step) {
                    if (f.contains(x, y)) { result.add(f); break outer; }
                }
            }
        }
        return result;
    }

    public Figure figureAt(int x, int y) {
        for (int i = figures.size() - 1; i >= 0; i--) {
            if (figures.get(i).contains(x, y)) return figures.get(i);
        }
        return null;
    }

    public void groupSelected() {
        if (selected.size() < 2) return;
        List<Figure> members = new ArrayList<>();
        List<Figure> next = new ArrayList<>();
        for (Figure f : figures) {
            if (selected.contains(f)) members.add(f);
            else next.add(f);
        }
        FigureGroup group = new FigureGroup(members);
        next.add(group);
        figures.clear();
        figures.addAll(next);
        selected.clear();
        selected.add(group);
        repaint();
    }

    public void deleteSelected() {
        figures.removeIf(selected::contains);
        selected.clear();
        repaint();
    }

    public void ungroupSelected() {
        if (selected.isEmpty()) return;
        List<Figure> next = new ArrayList<>();
        Set<Figure> newSelection = new LinkedHashSet<>();
        for (Figure f : figures) {
            List<Figure> baked = selected.contains(f) ? f.bakeTransform() : List.of(f);
            next.addAll(baked);
            if (selected.contains(f)) newSelection.addAll(baked);
        }
        figures.clear();
        figures.addAll(next);
        selected.clear();
        selected.addAll(newSelection);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (Figure f : figures) f.draw(g);
        if (preview != null) preview.draw(g);
        if (selectionRect != null) {
            g2.setColor(new Color(0, 100, 255, 60));
            g2.fillRect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
            g2.setColor(new Color(0, 100, 255, 200));
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10f, new float[]{6f, 4f}, 0f));
            g2.drawRect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
        }
        // 選択図形のハイライトとハンドル描画
        if (!selected.isEmpty()) {
            g2.setColor(new Color(0, 100, 255, 80));
            for (int xx = 0; xx < getWidth(); xx += 4) {
                for (int yy = 0; yy < getHeight(); yy += 4) {
                    for (Figure f : selected) {
                        if (f.contains(xx, yy)) { g2.fillRect(xx, yy, 2, 2); break; }
                    }
                }
            }
            for (Figure f : selected) {
                List<Handle> bbox = f.getBboxHandles();
                if (f instanceof FigureGroup) drawGroupBoundingBox(g2, bbox);
                for (Handle h : bbox) drawHandle(g2, h);
                for (Handle h : f.getHandles()) drawHandle(g2, h);
            }
        }
    }

    private void drawGroupBoundingBox(Graphics2D g2, List<Handle> handles) {
        Map<Handle.Type, Handle> hmap = new EnumMap<>(Handle.Type.class);
        for (Handle h : handles) hmap.put(h.getType(), h);
        Handle nw = hmap.get(Handle.Type.SCALE_NW);
        Handle ne = hmap.get(Handle.Type.SCALE_NE);
        Handle se = hmap.get(Handle.Type.SCALE_SE);
        Handle sw = hmap.get(Handle.Type.SCALE_SW);
        if (nw == null || ne == null || se == null || sw == null) return;
        int[] xs = {nw.getCx(), ne.getCx(), se.getCx(), sw.getCx()};
        int[] ys = {nw.getCy(), ne.getCy(), se.getCy(), sw.getCy()};
        g2.setColor(new Color(0, 100, 255, 180));
        g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
            10f, new float[]{6f, 4f}, 0f));
        g2.drawPolygon(xs, ys, 4);
        // 上辺中央と回転ハンドルを結ぶ線
        Handle rot = hmap.get(Handle.Type.ROTATE);
        if (rot != null) {
            int topMidX = (nw.getCx() + ne.getCx()) / 2;
            int topMidY = (nw.getCy() + ne.getCy()) / 2;
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawLine(topMidX, topMidY, rot.getCx(), rot.getCy());
        }
    }

    private static void drawHandle(Graphics2D g2, Handle h) {
        int hs = 8;
        int hx = h.getCx() - hs / 2;
        int hy = h.getCy() - hs / 2;
        g2.setStroke(new BasicStroke(1.5f));
        if (h.getType() == Handle.Type.ROTATE) {
            g2.setColor(Color.WHITE);
            g2.fillOval(hx, hy, hs, hs);
            g2.setColor(new Color(0, 100, 255));
            g2.drawOval(hx, hy, hs, hs);
        } else {
            g2.setColor(Color.WHITE);
            g2.fillRect(hx, hy, hs, hs);
            g2.setColor(new Color(0, 100, 255));
            g2.drawRect(hx, hy, hs, hs);
        }
    }
}
