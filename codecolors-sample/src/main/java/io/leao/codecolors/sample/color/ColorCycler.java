package io.leao.codecolors.sample.color;

import static android.graphics.Color.parseColor;

public class ColorCycler {
    private Integer[][] mColors = new Integer[][]{
            {null, null, null, null},
            {parseColor("#8BC34A"), parseColor("#689F38"), parseColor("#FF5722"), null},
            {parseColor("#FFC107"), parseColor("#FFA000"), parseColor("#607D8B"), null},
            {parseColor("#9E9E9E"), parseColor("#616161"), parseColor("#FF5252"), null},
            {null, null, null, null},
            {parseColor("#8BC34A"), parseColor("#689F38"), parseColor("#FF5722"), parseColor("#CDDC39")},
            {parseColor("#FFC107"), parseColor("#FFA000"), parseColor("#607D8B"), parseColor("#CDDC39")},
            {parseColor("#9E9E9E"), parseColor("#616161"), parseColor("#FF5252"), parseColor("#CDDC39")},
    };

    private int mCurrentTheme = 0;

    public void cycle() {
        mCurrentTheme++;
        if (mCurrentTheme >= mColors.length) {
            mCurrentTheme = 0;
        }
    }

    public Integer getPrimary() {
        return mColors[mCurrentTheme][0];
    }

    public Integer getPrimaryDark() {
        return mColors[mCurrentTheme][1];
    }

    public Integer getAccent() {
        return mColors[mCurrentTheme][2];
    }

    public Integer getAccentPressed() {
        return mColors[mCurrentTheme][3];
    }
}
