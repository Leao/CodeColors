package io.leao.codecolors.core.editor;

import java.util.Set;

import io.leao.codecolors.core.CcCore;
import io.leao.codecolors.core.color.CcColorStateList;
import io.leao.codecolors.core.util.CcTempUtils;

public class CcMultiEditorSet extends CcMultiEditor<CcMultiEditorSet> {
    public void submit() {
        Set<CcColorStateList> changedColors = CcTempUtils.getColorSet();

        for (int colorResId : mEditors.keySet()) {
            CcColorStateList color = CcCore.getColorManager().getColor(colorResId);
            if (color != null) {
                CcEditor editor = mEditors.get(colorResId);
                boolean changed = color.set(editor);
                if (changed) {
                    changedColors.add(color);
                }
            }
        }

        if (changedColors.size() > 0) {
            invalidate(changedColors);
        }

        CcTempUtils.recycleColorSet(changedColors);
    }
}