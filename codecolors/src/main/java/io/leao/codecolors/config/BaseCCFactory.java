package io.leao.codecolors.config;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;

import io.leao.codecolors.res.CcColorStateList;
import io.leao.codecolors.res.CcColorsResources;

public class BaseCcFactory implements CcFactory {
    @SuppressLint("NewApi")
    public CcColorStateList getColor(Context context, int resId) {
        ColorStateList csl = CcColorsResources.loadColorStateList(context.getResources(), resId, context.getTheme());
        return CcColorStateList.valueOf(csl);
    }
}
