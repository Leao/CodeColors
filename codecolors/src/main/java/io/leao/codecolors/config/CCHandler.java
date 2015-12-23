package io.leao.codecolors.config;

import io.leao.codecolors.res.CcColorStateList;

public interface CcHandler {
    int getColorCount();

    int getColorResId(int index);

    CcColorStateList getColor(int index);
}
