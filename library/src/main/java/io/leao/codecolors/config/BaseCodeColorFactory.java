package io.leao.codecolors.config;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;

import io.leao.codecolors.res.CodeColorStateList;
import io.leao.codecolors.res.CodeResources;

public class BaseCodeColorFactory implements CodeColorFactory {
    @SuppressLint("NewApi")
    public CodeColorStateList getColor(Context context, int resId) {
        ColorStateList csl = CodeResources.loadColorStateList(context.getResources(), resId, context.getTheme());
        return CodeColorStateList.valueOf(csl);
    }
}
