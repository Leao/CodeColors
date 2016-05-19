package io.leao.codecolors.core.color;

import android.support.annotation.Nullable;

interface ColorGetter<T extends ColorGetter> {
    T withAlpha(int alpha);

    boolean isOpaque();

    Integer getDefaultColor();

    Integer getColorForState(@Nullable int[] stateSet, Integer defaultColor);
}