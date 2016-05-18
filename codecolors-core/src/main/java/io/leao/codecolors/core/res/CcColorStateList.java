package io.leao.codecolors.core.res;

import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import io.leao.codecolors.plugin.res.CcConfiguration;

public class CcColorStateList extends ColorStateList {
    public static final int NO_ID = 0;
    public static final int DEFAULT_ANIMATION_DURATION_MS = 400;
    public static final Interpolator DEFAULT_ANIMATION_INTERPOLATOR = new DecelerateInterpolator();

    private static final int DEFAULT_COLOR = Color.BLUE;
    private static final int[][] EMPTY = new int[][]{new int[0]};

    private int mId;
    private AnimatedDefaultColorHandler mColorHandler;
    private CcConfigurationParcelable mConfiguration;

    private boolean mBlockInvalidate;

    private SetEditor mSetEditor;
    private AnimateEditor mAnimateEditor;

    private CallbackHandler mCallbackHandler = new CallbackHandler();

    public CcColorStateList() {
        this(NO_ID);
    }

    public CcColorStateList(int id) {
        this(id, new AnimatedDefaultColorHandler());
    }

    private CcColorStateList(int id, AnimatedDefaultColorHandler colorHandler) {
        super(EMPTY, new int[]{DEFAULT_COLOR});
        mId = id;
        mColorHandler = colorHandler;
    }

    private CcColorStateList(Parcel source) {
        this(source.readInt(), AnimatedDefaultColorHandler.CREATOR.createFromParcel(source));
        if (source.readByte() == 1) {
            mConfiguration = CcConfigurationParcelable.CREATOR.createFromParcel(source);
        }
    }

    public int getId() {
        return mId;
    }

    public CcConfiguration getConfiguration() {
        return mConfiguration;
    }

    public void onConfigurationChanged(CcConfiguration configuration, ColorStateList defaultColor) {
        if (configuration == null) {
            mConfiguration = null;
        } else if (mConfiguration == null) {
            mConfiguration = new CcConfigurationParcelable(configuration);
        } else {
            mConfiguration.setTo(configuration);
        }

        mColorHandler.setDefaultColor(defaultColor);
    }

    @NonNull
    @Override
    public CcColorStateList withAlpha(int alpha) {
        return new CcColorStateList(NO_ID, mColorHandler.withAlpha(alpha));
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    @Override
    public boolean isOpaque() {
        return mColorHandler.isOpaque();
    }

    @Override
    public int getDefaultColor() {
        Integer defaultColor = mColorHandler.getDefaultColor();
        return defaultColor != null ? defaultColor : DEFAULT_COLOR;
    }

    @Override
    public int getColorForState(int[] stateSet, int defaultColor) {
        return mColorHandler.getColorForState(stateSet, defaultColor);
    }

    public SetEditor set() {
        if (mSetEditor == null) {
            mSetEditor = new SetEditor();
        } else {
            mSetEditor.reuse();
        }
        return mSetEditor;
    }

    public void set(Editor<?> editor) {
        endAnimation(true);

        for (Editor.Edit edit : editor.getEdits()) {
            edit.edit(mColorHandler.getColorSetter());
            edit.edit(mColorHandler.getAnimationColorSetter());
        }
    }

    public AnimateEditor animate() {
        if (mAnimateEditor == null) {
            mAnimateEditor = new AnimateEditor();
        } else {
            mAnimateEditor.reuse();
        }
        return mAnimateEditor;
    }

    public void startAnimation(Editor<?> editor, ValueAnimator animation) {
        endAnimation(true);

        for (Editor.Edit edit : editor.getEdits()) {
            edit.edit(mColorHandler.getAnimationColorSetter());
        }

        // Set animation to influence color.
        mColorHandler.setAnimation(animation);

        // Start animation.
        animation.start();
    }

    public void endAnimation(boolean blockInvalidate) {
        mBlockInvalidate = blockInvalidate;
        mColorHandler.endAnimation();
        mBlockInvalidate = false;
    }

    /**
     * The added callback is kept in a {@code set} generated from a {@link WeakHashMap}.
     * <p>
     * That means the callback should be an object used by the application, like a
     * {@link android.graphics.drawable.Drawable} or a {@link android.view.View}.
     */
    public void addCallback(Callback callback) {
        mCallbackHandler.addCallback(callback);
    }

    public void removeCallback(Callback callback) {
        mCallbackHandler.removeCallback(callback);
    }

    /**
     * The added anchor is the key of a {@link WeakHashMap}.
     * <p>
     * That means the anchor should be an object used by the application, like a
     * {@link android.graphics.drawable.Drawable} or a {@link android.view.View}.
     * <p>
     * Refrain from keeping a reference to the {@code anchor} in your {@code callback}. Otherwise, it might generate a
     * memory leak.
     *
     * @param anchor   the anchor object to which the callback is dependent.
     * @param callback the callback.
     */
    public void addAnchorCallback(Object anchor, AnchorCallback callback) {
        mCallbackHandler.addAnchorCallback(anchor, callback);
    }

    public void removeAnchor(AnchorCallback callback) {
        mCallbackHandler.removeAnchor(callback);
    }

    public void removeCallback(AnchorCallback callback) {
        mCallbackHandler.removeCallback(callback);
    }

    public void invalidateSelf() {
        if (!mBlockInvalidate) {
            mCallbackHandler.invalidateColor(this);
        }
    }

    public interface Callback {
        void invalidateColor(CcColorStateList color);
    }

    public interface AnchorCallback<T> {
        void invalidateColor(T anchor, CcColorStateList color);
    }

    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);

        mColorHandler.writeToParcel(dest, flags);

        if (mConfiguration != null) {
            dest.writeByte((byte) 1);
            mConfiguration.writeToParcel(dest, 0);
        } else {
            dest.writeByte((byte) 0);
        }
    }

    public static final Parcelable.Creator<CcColorStateList> CREATOR = new Parcelable.Creator<CcColorStateList>() {
        @Override
        public CcColorStateList[] newArray(int size) {
            return new CcColorStateList[size];
        }

        @Override
        public CcColorStateList createFromParcel(Parcel source) {
            return new CcColorStateList(source);
        }
    };


    public class SetEditor extends Editor<SetEditor> {
        public void submit() {
            CcColorStateList.this.set(this);
            // Invalidate color.
            invalidateSelf();
        }
    }

    public class AnimateEditor extends Editor<AnimateEditor> implements ValueAnimator.AnimatorUpdateListener {
        private ValueAnimator mAnimation;
        private float mUpdateFraction;

        public AnimateEditor() {
            mAnimation = ValueAnimator.ofFloat(0, 1);
            mAnimation.setDuration(DEFAULT_ANIMATION_DURATION_MS);
            mAnimation.setInterpolator(DEFAULT_ANIMATION_INTERPOLATOR);
            // Listener to invalidate color.
            mAnimation.addUpdateListener(this);
        }

        @Override
        public AnimateEditor reuse() {
            mUpdateFraction = 0;
            return super.reuse();
        }

        public void start() {
            startAnimation(this, mAnimation);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float updateFraction = ((int) (animation.getAnimatedFraction() * 100) / 100f);

            if (updateFraction != mUpdateFraction) {
                mUpdateFraction = updateFraction;
                invalidateSelf();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static class Editor<T extends Editor> {
        private List<Edit> mEdits = new ArrayList<>();

        protected List<Edit> getEdits() {
            return mEdits;
        }

        /**
         * Clears all edits created by this {@link Editor} so that it can be reused.
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
            boolean edit(ColorSetter colorSetter);
        }

        private class SetColorEdit implements Edit {
            private ColorStateList mColor;

            public SetColorEdit(ColorStateList color) {
                mColor = color;
            }

            @Override
            public boolean edit(ColorSetter colorSetter) {
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
            public boolean edit(ColorSetter colorSetter) {
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
            public boolean edit(ColorSetter colorSetter) {
                return colorSetter.setState(mState, mColor);
            }
        }

        private class RemoveStatesEdit implements Edit {
            private int[][] mStates;

            public RemoveStatesEdit(int[][] states) {
                mStates = states;
            }

            @Override
            public boolean edit(ColorSetter colorSetter) {
                return colorSetter.removeStates(mStates);
            }
        }

        private class RemoveStateEdit implements Edit {
            private int[] mState;

            public RemoveStateEdit(int[] state) {
                mState = state;
            }

            @Override
            public boolean edit(ColorSetter colorSetter) {
                return colorSetter.removeState(mState);
            }
        }
    }
}
