import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SvgWriter {
    public static void write(List<Figure> figures, String path, int width, int height)
            throws Exception {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(Path.of(path)))) {
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.printf("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 %d %d\">%n",
                width, height, width, height);
            for (Figure f : figures) {
                pw.println("  " + f.toSvg());
            }
            pw.println("</svg>");
        }
    }
}
