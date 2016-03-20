package io.leao.codecolors.res;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.animation.Interpolator;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import io.leao.codecolors.plugin.res.CcConfiguration;

public class CcColorStateList extends ColorStateList {
    private static final int DEFAULT_COLOR = Color.BLUE;
    private static final int[][] EMPTY = new int[][]{new int[0]};

    private AnimatedDefaultColorHandler mColorHandler;
    private CcConfigurationParcelable mConfiguration;

    private AnimationBuilder mAnimationBuilder;

    protected Map<Callback, Object> mCallbacks =
            Collections.synchronizedMap(new WeakHashMap<Callback, Object>());
    protected Map<Object, AnchorCallback> mAnchorCallbacks =
            Collections.synchronizedMap(new WeakHashMap<Object, AnchorCallback>());

    public CcColorStateList() {
        this(new AnimatedDefaultColorHandler());
    }

    private CcColorStateList(AnimatedDefaultColorHandler colorHandler) {
        super(EMPTY, new int[]{DEFAULT_COLOR});
        mColorHandler = colorHandler;
        mColorHandler.setOnColorChangedListener(new DefaultColorHandler.OnColorChangedListener() {
            @Override
            public void onColorChanged() {
                invalidateSelf();
            }
        });
    }

    private CcColorStateList(Parcel source) {
        this(AnimatedDefaultColorHandler.CREATOR.createFromParcel(source));
        if (source.readByte() == 1) {
            mConfiguration = CcConfigurationParcelable.CREATOR.createFromParcel(source);
        }
    }

    public CcConfiguration getConfiguration() {
        return mConfiguration;
    }

    public void onConfigurationChanged(@NonNull CcConfiguration configuration, ColorStateList defaultColor) {
        if (mConfiguration == null) {
            mConfiguration = new CcConfigurationParcelable(configuration);
        } else {
            mConfiguration.setTo(configuration);
        }
        mColorHandler.setDefaultColor(defaultColor);
    }

    @NonNull
    @Override
    public CcColorStateList withAlpha(int alpha) {
        return new CcColorStateList(mColorHandler.withAlpha(alpha));
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

    public void setColor(int color) {
        mColorHandler.setColor(color);
    }

    public void setColor(ColorStateList color) {
        mColorHandler.setColor(color);
    }

    public void setStates(int[][] states, int[] colors) {
        mColorHandler.setStates(states, colors);
    }

    public void setState(int[] state, int color) {
        mColorHandler.setState(state, color);
    }

    public void removeStates(int[][] states) {
        mColorHandler.removeStates(states);
    }

    public void removeState(int[] state) {
        mColorHandler.removeState(state);
    }

    public AnimationBuilder animate() {
        if (mAnimationBuilder == null) {
            mAnimationBuilder = new AnimationBuilder();
        } else {
            mAnimationBuilder.reset();
        }
        return mAnimationBuilder;
    }

    public void endAnimation() {
        // End animation, without blocking the color change call.
        mColorHandler.endAnimation(false);
    }

    public void addCallback(Callback callback) {
        mCallbacks.put(callback, null);
    }

    public void removeCallback(Callback callback) {
        mCallbacks.remove(callback);
    }

    /**
     * @param anchor   the anchor object to which the callback is dependent.
     * @param callback the callback.
     */
    public void addCallback(Object anchor, AnchorCallback callback) {
        mAnchorCallbacks.put(anchor, callback);
    }

    public void removeCallback(AnchorCallback callback) {
        mAnchorCallbacks.remove(callback);
    }

    @SuppressWarnings("unchecked")
    public void invalidateSelf() {
        for (Callback callback : mCallbacks.keySet()) {
            if (callback != null) {
                callback.invalidateColor(this);
            }
        }

        for (Object anchor : mAnchorCallbacks.keySet()) {
            AnchorCallback callback = mAnchorCallbacks.get(anchor);
            if (callback != null) {
                callback.invalidateColor(anchor, this);
            }
        }
    }

    public class AnimationBuilder {
        private Integer mDuration;
        private Interpolator mInterpolator;
        private AnimationCallback mCallback;

        protected void reset() {
            mDuration = null;
            mInterpolator = null;
            mCallback = null;
        }

        public AnimationBuilder setColor(int color) {
            mColorHandler.setAnimationColor(color);
            return this;
        }

        public AnimationBuilder setColor(ColorStateList color) {
            mColorHandler.setAnimationColor(color);
            return this;
        }

        public AnimationBuilder setStates(int[][] states, int[] colors) {
            mColorHandler.setAnimationStates(states, colors);
            return this;
        }

        public AnimationBuilder setState(int[] state, int color) {
            mColorHandler.setAnimationState(state, color);
            return this;
        }

        public AnimationBuilder removeStates(int[][] states) {
            mColorHandler.removeAnimationStates(states);
            return this;
        }

        public AnimationBuilder removeState(int[] state) {
            mColorHandler.removeAnimationState(state);
            return this;
        }

        public AnimationBuilder setDuration(int duration) {
            mDuration = duration;
            return this;
        }

        public AnimationBuilder setInterpolator(Interpolator interpolator) {
            mInterpolator = interpolator;
            return this;
        }

        public AnimationBuilder setCallback(AnimationCallback callback) {
            mCallback = callback;
            return this;
        }

        public ValueAnimator start() {
            return mColorHandler.startAnimation(mDuration, mInterpolator, mCallback);
        }
    }

    public interface Callback {
        void invalidateColor(CcColorStateList color);
    }

    public interface AnchorCallback<T> {
        void invalidateColor(T anchor, CcColorStateList color);
    }

    public abstract class AnimationCallback
            implements ValueAnimator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {

        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public abstract class AnimationCallbackKitKat extends AnimationCallback
            implements ValueAnimator.AnimatorPauseListener {

        @Override
        public void onAnimationPause(Animator animation) {
        }

        @Override
        public void onAnimationResume(Animator animation) {
        }
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
}
