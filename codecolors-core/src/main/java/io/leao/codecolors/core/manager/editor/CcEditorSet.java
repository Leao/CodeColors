package io.leao.codecolors.core.manager.editor;

import java.lang.ref.WeakReference;

import io.leao.codecolors.core.color.CcColorStateList;

public class CcEditorSet extends CcEditor<CcEditorSet> {
    protected WeakReference<CcColorStateList> mColorRef;

    public CcEditorSet(CcColorStateList color) {
        mColorRef = new WeakReference<>(color);
    }

    public void submit() {
        CcColorStateList color = mColorRef.get();
        if (color != null) {
            boolean changed = color.set(this);
            if (changed) {
                // Invalidate color.
                color.invalidateSelf();
            }
        }
    }
}