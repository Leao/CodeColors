package io.leao.codecolors.core.color;

import java.util.Set;

import io.leao.codecolors.core.CcCore;

public class MultiSetEditor extends MultiEditor<MultiSetEditor> {
    public void submit() {
        Set<CcColorStateList> changedColors = CallbackTempUtils.getColorSet();

        for (int colorResId : mEditors.keySet()) {
            CcColorStateList.Editor editor = mEditors.get(colorResId);
            CcColorStateList color = CcCore.getColorsManager().getColor(colorResId);
            boolean changed = color.set(editor);
            if (changed) {
                changedColors.add(color);
            }
        }

        if (changedColors.size() > 0) {
            invalidate(changedColors);
        }

        CallbackTempUtils.recycleColorSet(changedColors);
    }
}