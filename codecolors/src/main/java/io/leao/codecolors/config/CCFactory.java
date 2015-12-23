package io.leao.codecolors.config;

import android.content.Context;

import io.leao.codecolors.res.CcColorStateList;

public interface CcFactory {
    CcColorStateList getColor(Context context, int resId);
}
