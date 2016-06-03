package io.leao.codecolors.core.manager;

import java.util.Map;
import java.util.WeakHashMap;

import io.leao.codecolors.core.color.CcColorStateList;
import io.leao.codecolors.core.editor.CcEditorAnimate;
import io.leao.codecolors.core.editor.CcEditorSet;
import io.leao.codecolors.core.editor.CcMultiEditorAnimate;
import io.leao.codecolors.core.editor.CcMultiEditorSet;

public class CcEditorManager {
    private Map<CcColorStateList, CcEditorSet> mColorEditorSet = new WeakHashMap<>();
    private Map<CcColorStateList, CcEditorAnimate> mColorEditorAnimate = new WeakHashMap<>();

    private CcMultiEditorSet mMultiSetEditorSet;
    private CcMultiEditorAnimate mMultiSetEditorAnimate;

    public synchronized CcEditorSet getEditorSet(CcColorStateList color) {
        CcEditorSet editor = mColorEditorSet.get(color);
        if (editor == null) {
            editor = new CcEditorSet(color);
            mColorEditorSet.put(color, editor);
        } else {
            editor.reuse();
        }
        return editor;
    }

    public synchronized CcMultiEditorSet getMultiEditorSet() {
        if (mMultiSetEditorSet == null) {
            mMultiSetEditorSet = new CcMultiEditorSet();
        } else {
            mMultiSetEditorSet.reuse();
        }
        return mMultiSetEditorSet;
    }

    public synchronized CcEditorAnimate getEditorAnimate(CcColorStateList color) {
        CcEditorAnimate editor = mColorEditorAnimate.get(color);
        if (editor == null) {
            editor = new CcEditorAnimate(color);
            mColorEditorAnimate.put(color, editor);
        } else {
            editor.reuse();
        }
        return editor;
    }

    public synchronized CcMultiEditorAnimate getMultiEditorAnimate() {
        if (mMultiSetEditorAnimate == null) {
            mMultiSetEditorAnimate = new CcMultiEditorAnimate();
        } else {
            mMultiSetEditorAnimate.reuse();
        }
        return mMultiSetEditorAnimate;
    }
}
