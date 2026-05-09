import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class SvgColor {
    private static final Map<String, Color> NAMED = new HashMap<>();

    static {
        NAMED.put("black",   new Color(  0,   0,   0));
        NAMED.put("white",   new Color(255, 255, 255));
        NAMED.put("red",     new Color(255,   0,   0));
        NAMED.put("green",   new Color(  0, 128,   0));
        NAMED.put("lime",    new Color(  0, 255,   0));
        NAMED.put("blue",    new Color(  0,   0, 255));
        NAMED.put("yellow",  new Color(255, 255,   0));
        NAMED.put("fuchsia", new Color(255,   0, 255));
        NAMED.put("magenta", new Color(255,   0, 255));
        NAMED.put("aqua",    new Color(  0, 255, 255));
        NAMED.put("cyan",    new Color(  0, 255, 255));
        NAMED.put("orange",  new Color(255, 165,   0));
        NAMED.put("purple",  new Color(128,   0, 128));
        NAMED.put("gray",    new Color(128, 128, 128));
        NAMED.put("grey",    new Color(128, 128, 128));
        NAMED.put("silver",  new Color(192, 192, 192));
        NAMED.put("maroon",  new Color(128,   0,   0));
        NAMED.put("navy",    new Color(  0,   0, 128));
        NAMED.put("teal",    new Color(  0, 128, 128));
        NAMED.put("olive",   new Color(128, 128,   0));
    }

    // null means "none"
    public static Color parse(String s) {
        if (s == null || s.isEmpty() || s.equals("none")) return null;
        s = s.trim().toLowerCase();
        Color named = NAMED.get(s);
        if (named != null) return named;
        if (s.startsWith("#")) {
            String hex = s.substring(1);
            if (hex.length() == 3) {
                int r = Integer.parseInt(hex.substring(0, 1) + hex.substring(0, 1), 16);
                int g = Integer.parseInt(hex.substring(1, 2) + hex.substring(1, 2), 16);
                int b = Integer.parseInt(hex.substring(2, 3) + hex.substring(2, 3), 16);
                return new Color(r, g, b);
            }
            if (hex.length() == 6) {
                return new Color(
                    Integer.parseInt(hex.substring(0, 2), 16),
                    Integer.parseInt(hex.substring(2, 4), 16),
                    Integer.parseInt(hex.substring(4, 6), 16));
            }
        }
        return null;
    }

    public static String toSvg(Color c) {
        if (c == null) return "none";
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }
}
