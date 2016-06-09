package io.leao.codecolors.core.editor;

import android.content.res.ColorStateList;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.leao.codecolors.core.CcCore;
import io.leao.codecolors.core.color.CcColorStateList;

@SuppressWarnings("unchecked")
public abstract class CcMultiEditor<T extends CcMultiEditor> {
    protected Map<Integer, CcEditor> mEditors = new HashMap<>();

    protected CcEditor getEditor(int colorResId) {
        CcEditor editor;
        if (mEditors.containsKey(colorResId)) {
            editor = mEditors.get(colorResId);
        } else if (CcCore.getColorsManager().getColor(colorResId) != null) {
            editor = new CcEditor();
            mEditors.put(colorResId, editor);
        } else {
            editor = null;
        }
        return editor;
    }

    protected void invalidate(Set<CcColorStateList> colors) {
        CcCore.getColorsManager().invalidateMultiple(colors);
    }

    /**
     * Clears all editors created by this {@link CcMultiEditor} so that it can be reused.
     */
    public T reuse() {
        mEditors.clear();
        return (T) this;
    }

    public T setColor(int colorResId, int color) {
        return setColor(colorResId, ColorStateList.valueOf(color));
    }

    public T setColor(int colorResId, ColorStateList color) {
        CcEditor editor = getEditor(colorResId);
        if (editor != null) {
            editor.setColor(color);
        }
        return (T) this;
    }

    public T setStates(int colorResId, int[][] states, int[] colors) {
        CcEditor editor = getEditor(colorResId);
        if (editor != null) {
            editor.setStates(states, colors);
        }
        return (T) this;
    }

    public T setState(int colorResId, int[] state, int color) {
        CcEditor editor = getEditor(colorResId);
        if (editor != null) {
            editor.setState(state, color);
        }
        return (T) this;
    }

    public T removeStates(int colorResId, int[][] states) {
        CcEditor editor = getEditor(colorResId);
        if (editor != null) {
            editor.removeStates(states);
        }
        return (T) this;
    }

    public T removeState(int colorResId, int[] state) {
        CcEditor editor = getEditor(colorResId);
        if (editor != null) {
            editor.removeState(state);
        }
        return (T) this;
    }
}
