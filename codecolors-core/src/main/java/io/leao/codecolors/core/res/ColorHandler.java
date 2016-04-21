package io.leao.codecolors.core.res;

import android.support.annotation.Nullable;

interface ColorHandler<T extends ColorHandler> {
    T withAlpha(int alpha);

    boolean isOpaque();

    Integer getDefaultColor();

    Integer getColorForState(@Nullable int[] stateSet, Integer defaultColor);
}
