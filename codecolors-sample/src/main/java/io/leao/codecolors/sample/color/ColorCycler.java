package io.leao.codecolors.sample.color;

import android.content.Context;
import android.content.SharedPreferences;

import io.leao.codecolors.sample.CodeColorsSample;

import static android.graphics.Color.parseColor;

public class ColorCycler {
    private static final String PREFERENCES_NAME = "io.leao.codecolors.sample.SHARED_PREFERENCES";
    private static final String PREFERENCES_KEY_CYCLER_POSITION = "CYCLER_POSITION";

    private static Integer[][] sColors = new Integer[][]{
            {null, null, null, null},
            {parseColor("#8BC34A"), parseColor("#689F38"), parseColor("#FF5722"), null},
            {parseColor("#FFC107"), parseColor("#FFA000"), parseColor("#607D8B"), null},
            {parseColor("#9E9E9E"), parseColor("#616161"), parseColor("#FF5252"), null},
            {null, null, null, parseColor("#CDDC39")},
            {parseColor("#8BC34A"), parseColor("#689F38"), parseColor("#FF5722"), parseColor("#CDDC39")},
            {parseColor("#FFC107"), parseColor("#FFA000"), parseColor("#607D8B"), parseColor("#CDDC39")},
            {parseColor("#9E9E9E"), parseColor("#616161"), parseColor("#FF5252"), parseColor("#CDDC39")},
    };

    private static int sPosition = loadCyclerPosition();

    public static void cycle() {
        sPosition++;
        if (sPosition >= sColors.length) {
            sPosition = 0;
        }
        saveCyclerPosition(sPosition);
    }

    public static Integer getPrimary() {
        return sColors[sPosition][0];
    }

    public static Integer getPrimaryDark() {
        return sColors[sPosition][1];
    }

    public static Integer getAccent() {
        return sColors[sPosition][2];
    }

    public static Integer getAccentPressed() {
        return sColors[sPosition][3];
    }

    /*
     * SharedPreferences utils.
     */

    public static void saveCyclerPosition(int position) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putInt(PREFERENCES_KEY_CYCLER_POSITION, position);
        editor.apply();
    }

    public static int loadCyclerPosition() {
        return getPreferences().getInt(PREFERENCES_KEY_CYCLER_POSITION, 0);
    }

    public static SharedPreferences getPreferences() {
        return CodeColorsSample
                .getInstance()
                .getApplicationContext()
                .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }
}
