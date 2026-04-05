package com.example.new_chess.firebase;

public class ColorScheme {
    public String lightest;
    public String medium;
    public String darkest;
    public String background;

    public ColorScheme() {}

    public ColorScheme(String light, String medium, String darkest, String bg) {
        this.lightest = light;
        this.medium = medium;
        this.darkest = darkest;
        this.background = bg;
    }

    public static ColorScheme getDefault() {
        return new ColorScheme(
                "#94B4C1",
                "#547792",
                "#213448",
                "#EAE0CF"
        );
    }

    public static ColorScheme getClasicBright() {
        return new ColorScheme(
                "#F0D9B5",
                "#000000",
                "#B58863",
                "#FFFFFF"
        );
    }

    public static ColorScheme getClasicDark() {
        return new ColorScheme(
                "#F0D9B5",
                "#FFFFFF",
                "#B58863",
                "#000000"
        );
    }
}
