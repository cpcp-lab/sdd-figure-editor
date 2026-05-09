import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class EditorFrame extends JFrame {
    public EditorFrame() {
        super("Figure Editor");
        Canvas canvas = new Canvas();
        EditorController controller = new EditorController();
        canvas.addMouseListener(controller);
        canvas.addMouseMotionListener(controller);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addToolButton(toolbar, "Line",     controller, () -> new DrawLineTool(canvas));
        addToolButton(toolbar, "Rect",     controller, () -> new DrawRectTool(canvas));
        addToolButton(toolbar, "Circle",   controller, () -> new DrawCircleTool(canvas));
        addToolButton(toolbar, "Ellipse",  controller, () -> new DrawEllipseTool(canvas));
        addToolButton(toolbar, "Polyline", controller, () -> new DrawPolylineTool(canvas));
        addToolButton(toolbar, "Polygon",  controller, () -> new DrawPolygonTool(canvas));

        controller.setTool(new DrawLineTool(canvas));

        add(toolbar, BorderLayout.NORTH);
        add(canvas,  BorderLayout.CENTER);
        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void addToolButton(JPanel toolbar, String label,
                               EditorController controller,
                               java.util.function.Supplier<Tool> factory) {
        JButton btn = new JButton(label);
        btn.addActionListener(e -> controller.setTool(factory.get()));
        toolbar.add(btn);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EditorFrame().setVisible(true));
    }
}
