import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class Canvas extends JPanel {
    private final List<Figure> figures = new ArrayList<>();
    private Figure preview;

    public Canvas() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));
    }

    public void addFigure(Figure f) {
        figures.add(f);
        repaint();
    }

    public void setPreview(Figure f) {
        preview = f;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Figure f : figures) {
            f.draw(g);
        }
        if (preview != null) {
            preview.draw(g);
        }
    }
}
