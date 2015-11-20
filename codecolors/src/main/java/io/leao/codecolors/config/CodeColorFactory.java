package io.leao.codecolors.config;

import android.content.Context;

import io.leao.codecolors.res.CodeColorStateList;

public interface CodeColorFactory {
    CodeColorStateList getColor(Context context, int resId);
}
