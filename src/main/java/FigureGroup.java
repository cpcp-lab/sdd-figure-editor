import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FigureGroup extends Figure {
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
    public void draw(Graphics g) {
        for (Figure f : children) f.draw(g);
    }

    @Override
    public void move(int dx, int dy) {
        for (Figure f : children) f.move(dx, dy);
    }

    @Override
    public boolean contains(int x, int y) {
        for (Figure f : children) {
            if (f.contains(x, y)) return true;
        }
        return false;
    }

    @Override
    public String toSvg() {
        StringBuilder sb = new StringBuilder("<g>\n");
        for (Figure f : children) sb.append("  ").append(f.toSvg()).append("\n");
        sb.append("</g>");
        return sb.toString();
    }
}
