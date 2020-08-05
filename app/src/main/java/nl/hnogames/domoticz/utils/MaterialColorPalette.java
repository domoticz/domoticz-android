package nl.hnogames.domoticz.utils;

import android.graphics.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MaterialColorPalette {

    public static final int RED_500 = 0xFFF44336;
    public static final int PINK_500 = 0xFFE91E63;
    public static final int PURPLE_500 = 0xFF9C27B0;
    public static final int DEEP_PURPLE_500 = 0xFF673AB7;
    public static final int INDIGO_500 = 0xFF3F51B5;
    public static final int BLUE_500 = 0xFF2196F3;
    public static final int LIGHT_BLUE_500 = 0xFF03A9F4;
    public static final int CYAN_500 = 0xFF00BCD4;
    public static final int TEAL_500 = 0xFF009688;
    public static final int GREEN_500 = 0xFF4CAF50;
    public static final int LIGHT_GREEN_500 = 0xFF8BC34A;
    public static final int LIME_500 = 0xFFCDDC39;
    public static final int YELLOW_500 = 0xFFFFEB3B;
    public static final int AMBER_500 = 0xFFFFC107;
    public static final int ORANGE_500 = 0xFFFF9800;
    public static final int DEEP_ORANGE_500 = 0xFFFF5722;
    public static final int BROWN_500 = 0xFF795548;
    public static final int GREY_500 = 0xFF9E9E9E;
    public static final int BLUE_GREY_500 = 0xFF607D8B;

    private static final List<MaterialColorPalette> MATERIAL_PALETTES;

    private static final Random RANDOM = new Random();

    static {
        MATERIAL_PALETTES = new ArrayList<>();
        MATERIAL_PALETTES.add(new MaterialColorPalette(RED_500));
        MATERIAL_PALETTES.add(new MaterialColorPalette(PINK_500));
        MATERIAL_PALETTES.add(new MaterialColorPalette(PURPLE_500));
        MATERIAL_PALETTES.add(new MaterialColorPalette(DEEP_PURPLE_500));
        MATERIAL_PALETTES.add(new MaterialColorPalette(INDIGO_500));
        MATERIAL_PALETTES.add(new MaterialColorPalette(BLUE_500));
        MATERIAL_PALETTES.add(new MaterialColorPalette(LIGHT_BLUE_500));
        MATERIAL_PALETTES.add(new MaterialColorPalette(CYAN_500));
        MATERIAL_PALETTES.add(new MaterialColorPalette(TEAL_500));
        MATERIAL_PALETTES.add(new MaterialColorPalette(GREEN_500));
        MATERIAL_PALETTES.add(new MaterialColorPalette(LIGHT_GREEN_500));
        MATERIAL_PALETTES.add(new MaterialColorPalette(LIME_500));
        MATERIAL_PALETTES.add(new MaterialColorPalette(YELLOW_500));
        MATERIAL_PALETTES.add(new MaterialColorPalette(AMBER_500));
        MATERIAL_PALETTES.add(new MaterialColorPalette(ORANGE_500));
        MATERIAL_PALETTES.add(new MaterialColorPalette(DEEP_ORANGE_500));
        MATERIAL_PALETTES.add(new MaterialColorPalette(BROWN_500));
        MATERIAL_PALETTES.add(new MaterialColorPalette(GREY_500));
        MATERIAL_PALETTES.add(new MaterialColorPalette(BLUE_GREY_500));
    }

    public static int getRandomColor(String key) {
        return MATERIAL_PALETTES.get(RANDOM.nextInt(MATERIAL_PALETTES.size())).getColor(key);
    }

    /**
     * Lighten or darken a color
     *
     * @param color
     *     color value
     * @param percent
     *     -1.0 to 1.0
     * @return new shaded color
     * @see #shadeColor(String, double)
     */
    public static int shadeColor(int color, double percent) {
        return shadeColor(String.format("#%06X", (0xFFFFFF & color)), percent); // ignores alpha channel
    }

    /**
     * Lighten or darken a color
     *
     * @param color
     *     7 character string representing the color.
     * @param percent
     *     -1.0 to 1.0
     * @return new shaded color
     * @see #shadeColor(int, double)
     */
    public static int shadeColor(String color, double percent) {
        // based off http://stackoverflow.com/a/13542669/1048340
        long f = Long.parseLong(color.substring(1), 16);
        double t = percent < 0 ? 0 : 255;
        double p = percent < 0 ? percent * -1 : percent;
        long R = f >> 16;
        long G = f >> 8 & 0x00FF;
        long B = f & 0x0000FF;
        int red = (int) (Math.round((t - R) * p) + R);
        int green = (int) (Math.round((t - G) * p) + G);
        int blue = (int) (Math.round((t - B) * p) + B);
        return Color.rgb(red, green, blue);
    }

    private final HashMap<String, Integer> palette = new HashMap<>();

    /**
     * @param primary
     *     the 500 color
     */
    public MaterialColorPalette(int primary) {
        palette.put("50", shadeColor(primary, 0.9));
        palette.put("100", shadeColor(primary, 0.7));
        palette.put("200", shadeColor(primary, 0.5));
        palette.put("300", shadeColor(primary, 0.333));
        palette.put("400", shadeColor(primary, 0.166));
        palette.put("500", primary);
        palette.put("600", shadeColor(primary, -0.125));
        palette.put("700", shadeColor(primary, -0.25));
        palette.put("800", shadeColor(primary, -0.375));
        palette.put("900", shadeColor(primary, -0.5));
        palette.put("A100", shadeColor(primary, 0.7));
        palette.put("A200", shadeColor(primary, 0.5));
        palette.put("A400", shadeColor(primary, 0.166));
        palette.put("A700", shadeColor(primary, -0.25));
    }

    public int getColor(String key) {
        return palette.get(key);
    }

    public void putColor(String key, int color) {
        palette.put(key, color);
    }

}