package com.example.new_chess.firebase;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;


public class ThemeManager {

    public static int[] getTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        String theme = prefs.getString("theme", "default");


        //fix up the themes and add example for what each  one is supposed to be(light, background ...) base on colorScheme
        switch (theme) {
            case "light":
                return new int[]{
                        Color.parseColor("#F0D9B5"),
                        Color.parseColor("#B58863"),
                        Color.parseColor("#000000"),
                        Color.parseColor("#FFFFFF")
                };

            case "dark":
                return new int[]{
                        Color.parseColor("#769656"),
                        Color.parseColor("#EEEED2"),
                        Color.parseColor("#FFFFFF"),
                        Color.parseColor("#000000")
                };

            default:
                return new int[]{
                        Color.parseColor("#94B4C1"),
                        Color.parseColor("#213448"),
                        Color.parseColor("#213448"),
                        Color.parseColor("#EAE0CF")
                };
        }
    }
}