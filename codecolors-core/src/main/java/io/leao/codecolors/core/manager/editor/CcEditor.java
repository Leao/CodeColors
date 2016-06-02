package io.leao.codecolors.core.manager.editor;

import android.content.res.ColorStateList;

import java.util.ArrayList;
import java.util.List;

import io.leao.codecolors.core.color.CcColorStateList;

@SuppressWarnings("unchecked")
public class CcEditor<T extends CcEditor> {
    protected List<Edit> mEdits = new ArrayList<>();

    public List<Edit> getEdits() {
        return mEdits;
    }

    /**
     * Clears all edits created by this {@link CcEditor} so that it can be reused.
     */
    public T reuse() {
        mEdits.clear();
        return (T) this;
    }

    public T setColor(int color) {
        return setColor(ColorStateList.valueOf(color));
    }

    public T setColor(ColorStateList color) {
        mEdits.add(new SetColorEdit(color));
        return (T) this;
    }

    public T setStates(int[][] states, int[] colors) {
        mEdits.add(new SetStatesEdit(states, colors));
        return (T) this;
    }

    public T setState(int[] state, int color) {
        mEdits.add(new SetStateEdit(state, color));
        return (T) this;
    }

    public T removeStates(int[][] states) {
        mEdits.add(new RemoveStatesEdit(states));
        return (T) this;
    }

    public T removeState(int[] state) {
        mEdits.add(new RemoveStateEdit(state));
        return (T) this;
    }

    public interface Edit {
        boolean apply(CcColorStateList.ColorSetter colorSetter);
    }

    private class SetColorEdit implements Edit {
        private ColorStateList mColor;

        public SetColorEdit(ColorStateList color) {
            mColor = color;
        }

        @Override
        public boolean apply(CcColorStateList.ColorSetter colorSetter) {
            return colorSetter.setColor(mColor);
        }
    }

    private class SetStatesEdit implements Edit {
        private int[][] mStates;
        private int[] mColors;

        public SetStatesEdit(int[][] states, int[] colors) {
            mStates = states;
            mColors = colors;
        }

        @Override
        public boolean apply(CcColorStateList.ColorSetter colorSetter) {
            return colorSetter.setStates(mStates, mColors);
        }
    }

    private class SetStateEdit implements Edit {
        private int[] mState;
        private int mColor;

        public SetStateEdit(int[] state, int color) {
            mState = state;
            mColor = color;
        }

        @Override
        public boolean apply(CcColorStateList.ColorSetter colorSetter) {
            return colorSetter.setState(mState, mColor);
        }
    }

    private class RemoveStatesEdit implements Edit {
        private int[][] mStates;

        public RemoveStatesEdit(int[][] states) {
            mStates = states;
        }

        @Override
        public boolean apply(CcColorStateList.ColorSetter colorSetter) {
            return colorSetter.removeStates(mStates);
        }
    }

    private class RemoveStateEdit implements Edit {
        private int[] mState;

        public RemoveStateEdit(int[] state) {
            mState = state;
        }

        @Override
        public boolean apply(CcColorStateList.ColorSetter colorSetter) {
            return colorSetter.removeState(mState);
        }
    }
}