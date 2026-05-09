import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Supplier;

public class EditorFrame extends JFrame {
    private final Canvas canvas;
    private final EditorController controller;
    private final ButtonGroup toolGroup = new ButtonGroup();

    public EditorFrame() {
        super("Figure Editor");
        canvas = new Canvas();
        controller = new EditorController();
        canvas.addMouseListener(controller);
        canvas.addMouseMotionListener(controller);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addToolButton(toolbar, "Select", () -> new SelectTool(canvas));
        JToggleButton shapeBtn = buildShapeDropdown(toolbar);
        shapeBtn.setSelected(true);  // 起動時は図形ドロップダウン側をハイライト
        buildStrokeWidthDropdown(toolbar);
        buildStrokeColorDropdown(toolbar);
        buildFillColorDropdown(toolbar);

        controller.setTool(new DrawLineTool(canvas));

        setJMenuBar(buildMenuBar());
        canvas.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e)  { maybeShowPopup(e); }
            @Override public void mouseReleased(MouseEvent e) { maybeShowPopup(e); }
            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) buildPopupMenu().show(canvas, e.getX(), e.getY());
            }
        });
        add(toolbar, BorderLayout.NORTH);
        add(canvas,  BorderLayout.CENTER);
        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open...");
        openItem.addActionListener(e -> openFile());
        JMenuItem saveItem = new JMenuItem("Save...");
        saveItem.addActionListener(e -> saveFile());
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu("Edit");
        JMenuItem groupItem = new JMenuItem("Group");
        groupItem.addActionListener(e -> canvas.groupSelected());
        JMenuItem ungroupItem = new JMenuItem("Ungroup");
        ungroupItem.addActionListener(e -> canvas.ungroupSelected());
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(e -> canvas.deleteSelected());
        editMenu.add(groupItem);
        editMenu.add(ungroupItem);
        editMenu.addSeparator();
        editMenu.add(deleteItem);
        menuBar.add(editMenu);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e ->
            JOptionPane.showMessageDialog(this, "Figure Editor", "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private JPopupMenu buildPopupMenu() {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem groupItem = new JMenuItem("Group");
        groupItem.addActionListener(e -> canvas.groupSelected());
        popup.add(groupItem);
        JMenuItem ungroupItem = new JMenuItem("Ungroup");
        ungroupItem.addActionListener(e -> canvas.ungroupSelected());
        popup.add(ungroupItem);
        popup.addSeparator();
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(e -> canvas.deleteSelected());
        popup.add(deleteItem);
        return popup;
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

    /** 図形種別ドロップダウンボタンを作成してツールバーに追加し，そのボタンを返す． */
    private JToggleButton buildShapeDropdown(JPanel toolbar) {
        JPopupMenu menu = new JPopupMenu();

        String[] labels = {"Line", "Rect", "Circle", "Ellipse", "Polyline", "Polygon"};
        @SuppressWarnings("unchecked")
        Supplier<Tool>[] factories = new Supplier[]{
            () -> new DrawLineTool(canvas),
            () -> new DrawRectTool(canvas),
            () -> new DrawCircleTool(canvas),
            () -> new DrawEllipseTool(canvas),
            () -> new DrawPolylineTool(canvas),
            () -> new DrawPolygonTool(canvas),
        };

        JToggleButton btn = new JToggleButton("Line ▾");
        toolGroup.add(btn);
        toolbar.add(btn);

        for (int i = 0; i < labels.length; i++) {
            String label = labels[i];
            Supplier<Tool> factory = factories[i];
            JMenuItem item = new JMenuItem(label);
            item.addActionListener(e -> {
                btn.setText(label + " ▾");
                btn.setSelected(true);
                controller.setTool(factory.get());
            });
            menu.add(item);
        }

        btn.addActionListener(e -> {
            if (btn.isSelected()) menu.show(btn, 0, btn.getHeight());
        });

        return btn;
    }

    private void buildStrokeWidthDropdown(JPanel toolbar) {
        String[] items = {"Thin (1px)", "Normal (3px)", "Thick (6px)"};
        float[] widths = {1.0f, 3.0f, 6.0f};
        JComboBox<String> combo = new JComboBox<>(items);
        combo.addActionListener(e -> {
            int idx = combo.getSelectedIndex();
            canvas.setCurrentStrokeWidth(widths[idx]);
        });
        toolbar.add(new JLabel("Width:"));
        toolbar.add(combo);
    }

    private void buildStrokeColorDropdown(JPanel toolbar) {
        String[] names  = {"Black", "White", "Red", "Green", "Blue",
                           "Yellow", "Cyan", "Magenta", "Orange", "Gray"};
        Color[]  colors = {Color.BLACK, Color.WHITE, Color.RED, new Color(0, 128, 0), Color.BLUE,
                           Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.ORANGE, Color.GRAY};

        JComboBox<Integer> combo = new JComboBox<>();
        for (int i = 0; i < names.length; i++) combo.addItem(i);

        combo.setRenderer(new ListCellRenderer<Integer>() {
            private final JLabel label = new JLabel();
            @Override
            public Component getListCellRendererComponent(
                    JList<? extends Integer> list, Integer value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                int i = (value == null) ? 0 : value;
                label.setText(names[i]);
                label.setIcon(colorIcon(colors[i]));
                label.setOpaque(true);
                label.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
                label.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
                return label;
            }
        });

        combo.addActionListener(e -> {
            int idx = combo.getSelectedIndex();
            canvas.setCurrentStrokeColor(colors[idx]);
        });
        toolbar.add(new JLabel("Color:"));
        toolbar.add(combo);
    }

    private void buildFillColorDropdown(JPanel toolbar) {
        String[] names  = {"None", "Black", "White", "Red", "Green", "Blue",
                           "Yellow", "Cyan", "Magenta", "Orange", "Gray"};
        Color[]  colors = {null, Color.BLACK, Color.WHITE, Color.RED, new Color(0, 128, 0), Color.BLUE,
                           Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.ORANGE, Color.GRAY};

        JComboBox<Integer> combo = new JComboBox<>();
        for (int i = 0; i < names.length; i++) combo.addItem(i);

        combo.setRenderer(new ListCellRenderer<Integer>() {
            private final JLabel label = new JLabel();
            @Override
            public Component getListCellRendererComponent(
                    JList<? extends Integer> list, Integer value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                int i = (value == null) ? 0 : value;
                label.setText(names[i]);
                label.setIcon(colors[i] == null ? noFillIcon() : colorIcon(colors[i]));
                label.setOpaque(true);
                label.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
                label.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
                return label;
            }
        });

        combo.addActionListener(e -> {
            int idx = combo.getSelectedIndex();
            canvas.setCurrentFillColor(colors[idx]);
        });
        toolbar.add(new JLabel("Fill:"));
        toolbar.add(combo);
    }

    private static Icon noFillIcon() {
        return new Icon() {
            @Override public int getIconWidth()  { return 14; }
            @Override public int getIconHeight() { return 14; }
            @Override public void paintIcon(Component comp, Graphics g, int x, int y) {
                g.setColor(Color.WHITE);
                g.fillRect(x, y, 14, 14);
                g.setColor(Color.DARK_GRAY);
                g.drawRect(x, y, 13, 13);
                g.drawLine(x, y, x + 13, y + 13);
            }
        };
    }

    private static Icon colorIcon(Color c) {
        return new Icon() {
            @Override public int getIconWidth()  { return 14; }
            @Override public int getIconHeight() { return 14; }
            @Override public void paintIcon(Component comp, Graphics g, int x, int y) {
                g.setColor(c);
                g.fillRect(x, y, 14, 14);
                g.setColor(Color.DARK_GRAY);
                g.drawRect(x, y, 13, 13);
            }
        };
    }

    private void addToolButton(JPanel toolbar, String label, Supplier<Tool> factory) {
        JToggleButton btn = new JToggleButton(label);
        toolGroup.add(btn);
        btn.addActionListener(e -> controller.setTool(factory.get()));
        toolbar.add(btn);
    }

    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Figure Editor");
        SwingUtilities.invokeLater(() -> new EditorFrame().setVisible(true));
    }
}
