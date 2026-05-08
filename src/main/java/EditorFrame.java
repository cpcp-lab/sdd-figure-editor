import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class EditorFrame extends JFrame {
    public EditorFrame() {
        super("Figure Editor");
        Canvas canvas = new Canvas();
        EditorController controller = new EditorController();
        controller.setTool(new DrawLineTool(canvas));
        canvas.addMouseListener(controller);
        canvas.addMouseMotionListener(controller);
        add(canvas);
        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EditorFrame().setVisible(true));
    }
}
