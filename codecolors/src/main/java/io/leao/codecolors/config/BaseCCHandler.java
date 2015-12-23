package io.leao.codecolors.config;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import io.leao.codecolors.res.CcColorStateList;

public class BaseCcHandler implements CcHandler {
    private List<Entry> mCodeColors;

    public BaseCcHandler(Context context, CcFactory factory, int[] colorResIds) {
        mCodeColors = new ArrayList<>(colorResIds.length);
        for (int colorResId : colorResIds) {
            mCodeColors.add(new Entry(colorResId, factory.getColor(context, colorResId)));
        }
    }

    public int getColorCount() {
        return mCodeColors.size();
    }

    @Override
    public int getColorResId(int index) {
        return mCodeColors.get(index).resId;
    }

    @Override
    public CcColorStateList getColor(int index) {
        return mCodeColors.get(index).color;
    }

    private static class Entry {
        public int resId;
        public CcColorStateList color;

        public Entry(int resId, CcColorStateList color) {
            this.resId = resId;
            this.color = color;
        }
    }
}
