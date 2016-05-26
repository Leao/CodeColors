package io.leao.codecolors.core.color;

import android.content.res.ColorStateList;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.leao.codecolors.core.CcCore;

@SuppressWarnings("unchecked")
public abstract class MultiEditor<T extends MultiEditor> {
    protected Map<Integer, CcColorStateList.Editor> mEditors = new HashMap<>();

    protected CcColorStateList.Editor getEditor(int resId) {
        CcColorStateList.Editor editor;
        if (mEditors.containsKey(resId)) {
            editor = mEditors.get(resId);
        } else if (getColor(resId) != null) {
            editor = new CcColorStateList.Editor();
            mEditors.put(resId, editor);
        } else {
            editor = null;
        }
        return editor;
    }

    protected CcColorStateList getColor(int resId) {
        return CcCore.getColorsManager().getColor(resId);
    }

    protected void invalidate(Set<CcColorStateList> colors) {
        CallbackHandler.invalidateColors(colors);
    }

    /**
     * Clears all editors created by this {@link MultiEditor} so that it can be reused.
     */
    public T reuse() {
        mEditors.clear();
        return (T) this;
    }

    public T setColor(int resId, int color) {
        return setColor(resId, ColorStateList.valueOf(color));
    }

    public T setColor(int resId, ColorStateList color) {
        CcColorStateList.Editor editor = getEditor(resId);
        if (editor != null) {
            editor.setColor(color);
        }
        return (T) this;
    }

    public T setStates(int resId, int[][] states, int[] colors) {
        CcColorStateList.Editor editor = getEditor(resId);
        if (editor != null) {
            editor.setStates(states, colors);
        }
        return (T) this;
    }

    public T setState(int resId, int[] state, int color) {
        CcColorStateList.Editor editor = getEditor(resId);
        if (editor != null) {
            editor.setState(state, color);
        }
        return (T) this;
    }

    public T removeStates(int resId, int[][] states) {
        CcColorStateList.Editor editor = getEditor(resId);
        if (editor != null) {
            editor.removeStates(states);
        }
        return (T) this;
    }

    public T removeState(int resId, int[] state) {
        CcColorStateList.Editor editor = getEditor(resId);
        if (editor != null) {
            editor.removeState(state);
        }
        return (T) this;
    }
}
