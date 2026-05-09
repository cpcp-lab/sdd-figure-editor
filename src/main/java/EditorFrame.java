import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

public class EditorFrame extends JFrame {
    private final Canvas canvas;

    public EditorFrame() {
        super("Figure Editor");
        canvas = new Canvas();
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

        setJMenuBar(buildMenuBar());
        add(toolbar, BorderLayout.NORTH);
        add(canvas,  BorderLayout.CENTER);
        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem saveItem = new JMenuItem("Save...");
        saveItem.addActionListener(e -> saveFile());

        JMenuItem openItem = new JMenuItem("Open...");
        openItem.addActionListener(e -> openFile());

        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        menuBar.add(fileMenu);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e ->
            JOptionPane.showMessageDialog(this, "Figure Editor", "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private void saveFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("SVG files", "svg"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getPath();
            if (!path.endsWith(".svg")) path += ".svg";
            try {
                SvgWriter.write(canvas.getFigures(), path, canvas.getWidth(), canvas.getHeight());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "保存エラー: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("SVG files", "svg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                List<Figure> figures = SvgReader.read(chooser.getSelectedFile().getPath());
                canvas.setFigures(figures);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "読み込みエラー: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addToolButton(JPanel toolbar, String label,
                               EditorController controller,
                               java.util.function.Supplier<Tool> factory) {
        JButton btn = new JButton(label);
        btn.addActionListener(e -> controller.setTool(factory.get()));
        toolbar.add(btn);
    }

    public static void main(String[] args) {
        // macOS のスクリーンメニューバーを有効化 (Swing 初期化より前に設定する必要がある)
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Figure Editor");
        SwingUtilities.invokeLater(() -> new EditorFrame().setVisible(true));
    }
}
