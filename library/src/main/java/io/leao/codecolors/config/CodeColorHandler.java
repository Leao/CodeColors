package io.leao.codecolors.config;

import io.leao.codecolors.res.CodeColorStateList;

public interface CodeColorHandler {
    int getColorCount();

    int getColorResId(int index);

    CodeColorStateList getColor(int index);
}
