package com.example.new_chess.firebase;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;


public class ThemeManager {

    public static int[] getTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        String theme = prefs.getString("theme", "default");


        //fix up the themes and add example for what each  one is supposed to be(light, background ...) base on colorScheme
        switch (theme) {
            case "light":
                return new int[]{
                        Color.parseColor("#F0D9B5"),  // light square
                        Color.parseColor("#B58863"),  // dark square
                        Color.parseColor("#2d2c2d"),  // details
                        Color.parseColor("#f4f8fa")   // bg
                };

            case "dark":
                return new int[]{
                        Color.parseColor("#F0D9B5"),
                        Color.parseColor("#B58863"),
                        Color.parseColor("#f4f8fa"),
                        Color.parseColor("#2d2c2d")
                };

            default:
                return new int[]{
                        Color.parseColor("#94B4C1"),
                        Color.parseColor("#213448"),
                        Color.parseColor("#547792"),
                        Color.parseColor("#EAE0CF")
                };
        }
    }


    public static void applyTheme(View root, int[] theme) {

        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;

            for (int i = 0; i < group.getChildCount(); i++) {
                applyTheme(group.getChildAt(i), theme);
            }
        }

        root.setBackgroundColor(theme[3]);

        String themeName = root.getContext()
                .getSharedPreferences("settings", Context.MODE_PRIVATE)
                .getString("theme", "default");

        if (themeName.equals("default")){
            // Apply styles based on type
            if (root instanceof Button) {
                ((Button) root).setBackgroundTintList(
                        ColorStateList.valueOf(theme[0])
                );
                ((Button) root).setTextColor(theme[1]);
            }

            else if (root instanceof TextView) {
                ((TextView) root).setTextColor(theme[1]);
            }

            else if (root instanceof Switch) {
                ((Switch) root).setThumbTintList(
                        ColorStateList.valueOf(theme[3])
                );
                ((Switch) root).setTrackTintList(
                        ColorStateList.valueOf(theme[1])
                );
            }
        }

        else{// Apply styles based on type
            if (root instanceof Button) {
                ((Button) root).setBackgroundTintList(
                        ColorStateList.valueOf(theme[2])
                );
                ((Button) root).setTextColor(theme[3]);
            } else if (root instanceof TextView) {
                ((TextView) root).setTextColor(theme[2]);
            } else if (root instanceof Switch) {
                ((Switch) root).setThumbTintList(
                        ColorStateList.valueOf(theme[3])
                );
                ((Switch) root).setTrackTintList(
                        ColorStateList.valueOf(theme[2])
                );
            }
        }


    }


}