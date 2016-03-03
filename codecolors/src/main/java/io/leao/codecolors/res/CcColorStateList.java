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
import android.support.annotation.Nullable;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import java.util.WeakHashMap;

import io.leao.codecolors.plugin.res.CcConfiguration;

public class CcColorStateList extends ColorStateList {
    public static final int DEFAULT_ANIMATION_DURATION_MS = 400;
    public static final Interpolator DEFAULT_ANIMATION_INTERPOLATOR = new DecelerateInterpolator();

    private static final int DEFAULT_COLOR = Color.RED;
    private static final int[][] EMPTY = new int[][]{new int[0]};

    private int mId = CcResources.NO_ID;

    private ColorStateList mDefaultColor = ColorStateList.valueOf(DEFAULT_COLOR);
    private CcConfigurationParcelable mConfiguration;

    private ColorStateList mColor;

    private ValueAnimator mAnimation;
    private AnimationCallbackInternal mAnimationCallback;
    private ColorStateList mAnimationColor;

    protected WeakHashMap<Callback, Object> mCallbacks = new WeakHashMap<>();
    protected WeakHashMap<Object, AnchorCallback> mAnchorCallbacks = new WeakHashMap<>();

    public CcColorStateList() {
        super(EMPTY, new int[]{DEFAULT_COLOR});
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    public void setId(int id) {
        mId = id;
    }

    public int getId() {
        return mId;
    }

    public CcConfiguration getConfiguration() {
        return mConfiguration;
    }

    @Override
    public int getDefaultColor() {
        if (mAnimation == null || !mAnimation.isStarted() || mAnimation.getAnimatedFraction() == 0) {
            return getColorInternal().getDefaultColor();
        } else if (mAnimation.getAnimatedFraction() == 1) {
            return getAnimationColorInternal().getDefaultColor();
        } else {
            return interpolate(
                    getColorInternal().getDefaultColor(),
                    getAnimationColorInternal().getDefaultColor(),
                    (float) mAnimation.getAnimatedValue());
        }
    }

    public void setDefaultColor(@NonNull CcConfiguration configuration, ColorStateList defaultColor) {
        if (mConfiguration == null) {
            mConfiguration = new CcConfigurationParcelable(configuration);
        } else {
            mConfiguration.setTo(configuration);
        }
        mDefaultColor = defaultColor != null ? defaultColor : mDefaultColor;
    }

    @Override
    public int getColorForState(int[] stateSet, int defaultColor) {
        if (mAnimation == null || !mAnimation.isStarted() || mAnimation.getAnimatedFraction() == 0) {
            return getColorInternal().getColorForState(stateSet, defaultColor);
        } else if (mAnimation.getAnimatedFraction() == 1) {
            return getAnimationColorInternal().getColorForState(stateSet, defaultColor);
        } else {
            return interpolate(
                    getColorInternal().getColorForState(stateSet, defaultColor),
                    getAnimationColorInternal().getColorForState(stateSet, defaultColor),
                    (float) mAnimation.getAnimatedValue());
        }
    }

    public void setColor(int color) {
        setColor(ColorStateList.valueOf(color));
    }

    public void setColor(ColorStateList color) {
        endAnimation(true);
        if (isColorChanging(color)) {
            mColor = color;
            invalidateSelf();
        } else {
            mColor = color;
        }
    }

    private boolean isColorChanging(ColorStateList color) {
        return mColor == null || !mColor.equals(color);
    }

    private ColorStateList getColorInternal() {
        return mColor != null ? mColor : mDefaultColor;
    }

    private ColorStateList getAnimationColorInternal() {
        return mAnimationColor != null ? mAnimationColor : mDefaultColor;
    }

    public ValueAnimator animateTo(int color) {
        return animateTo(ColorStateList.valueOf(color));
    }

    public ValueAnimator animateTo(ColorStateList color) {
        return animateTo(color, DEFAULT_ANIMATION_DURATION_MS, null, null);
    }

    public ValueAnimator animateTo(int color, int duration, @Nullable Interpolator interpolator,
                                   @Nullable AnimationCallback callback) {
        return animateTo(ColorStateList.valueOf(color), duration, interpolator, callback);
    }

    /**
     * @return the animation animator, if the color is going to change; null, otherwise.
     */
    public ValueAnimator animateTo(ColorStateList color, int duration, @Nullable Interpolator interpolator,
                                   @Nullable AnimationCallback callback) {
        endAnimation(false);

        if (isColorChanging(color)) {
            if (mAnimation == null) {
                mAnimation = ValueAnimator.ofFloat(0, 1);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mAnimationCallback = new AnimationKitKatCallbackInternal();
                } else {
                    mAnimationCallback = new AnimationCallbackInternal();
                }
                mAnimation.addListener(mAnimationCallback);
                mAnimation.addUpdateListener(mAnimationCallback);
            }

            mAnimationColor = color;

            mAnimation.setDuration(duration);
            mAnimation.setInterpolator(interpolator != null ? interpolator : DEFAULT_ANIMATION_INTERPOLATOR);

            mAnimationCallback.setCallback(callback);

            // Start animation!
            mAnimation.start();
            return mAnimation;
        } else {
            mColor = color;
            return null;
        }
    }

    private void endAnimation(boolean blockEndInvalidate) {
        if (mAnimation != null && mAnimation.isStarted()) {
            if (blockEndInvalidate) {
                mAnimationCallback.blockEndInvalidate();
            }
            mAnimation.end();
        }
    }

    private int interpolate(int startColor, int endColor, float fraction) {
        if (startColor != endColor) {
            int startA = (startColor >> 24) & 0xff;
            int startR = (startColor >> 16) & 0xff;
            int startG = (startColor >> 8) & 0xff;
            int startB = startColor & 0xff;
            int endA = (endColor >> 24) & 0xff;
            int endR = (endColor >> 16) & 0xff;
            int endG = (endColor >> 8) & 0xff;
            int endB = endColor & 0xff;
            return ((startA + (int) (fraction * (endA - startA))) << 24) |
                    ((startR + (int) (fraction * (endR - startR))) << 16) |
                    ((startG + (int) (fraction * (endG - startG))) << 8) |
                    ((startB + (int) (fraction * (endB - startB))));
        } else {
            return startColor;
        }
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (mConfiguration != null) {
            dest.writeByte((byte) 1);
            mConfiguration.writeToParcel(dest, 0);
            mDefaultColor.writeToParcel(dest, 0);
        } else {
            dest.writeByte((byte) 0);
        }

        ColorStateList color = mAnimation == null || !mAnimation.isStarted() ? mColor : mAnimationColor;
        if (color != null) {
            dest.writeByte((byte) 1);
            color.writeToParcel(dest, 0);
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
            CcColorStateList cccsl = new CcColorStateList();
            if (source.readByte() == 1) {
                cccsl.setDefaultColor(
                        CcConfigurationParcelable.CREATOR.createFromParcel(source),
                        ColorStateList.CREATOR.createFromParcel(source));
            }
            if (source.readByte() == 1) {
                cccsl.setColor(ColorStateList.CREATOR.createFromParcel(source));
            }
            return cccsl;
        }
    };

    public interface Callback {
        void invalidateColor(CcColorStateList color);
    }

    public interface AnchorCallback<T> {
        void invalidateColor(T anchor, CcColorStateList color);
    }

    private class AnimationCallbackInternal extends AnimationCallback {
        protected AnimationCallback mCallback;

        private float mUpdateFraction;
        private boolean mBlockEndInvalidate; // Invalidate on end by default.

        public void setCallback(AnimationCallback callback) {
            mCallback = callback;
        }

        public void blockEndInvalidate() {
            mBlockEndInvalidate = true;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            if (mCallback != null) {
                mCallback.onAnimationStart(animation);
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (mCallback != null) {
                mCallback.onAnimationEnd(animation);
            }

            mColor = mAnimationColor;
            mAnimationColor = null;
            mCallback = null;
            mUpdateFraction = 0;
            mBlockEndInvalidate = false;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            if (mCallback != null) {
                mCallback.onAnimationCancel(animation);
            }
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
            if (mCallback != null) {
                mCallback.onAnimationRepeat(animation);
            }
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float updateFraction = animation.getAnimatedFraction();
            if (updateFraction != mUpdateFraction) {
                mUpdateFraction = updateFraction;

                if (mCallback != null) {
                    mCallback.onAnimationUpdate(animation);
                }

                if (mUpdateFraction != 1 || !mBlockEndInvalidate) {
                    invalidateSelf();
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public class AnimationKitKatCallbackInternal extends AnimationCallbackInternal
            implements ValueAnimator.AnimatorPauseListener {

        @Override
        public void onAnimationPause(Animator animation) {
            if (mCallback instanceof AnimationKitKatCallback) {
                ((AnimationKitKatCallback) mCallback).onAnimationPause(animation);
            }
        }

        @Override
        public void onAnimationResume(Animator animation) {
            if (mCallback instanceof AnimationKitKatCallback) {
                ((AnimationKitKatCallback) mCallback).onAnimationResume(animation);
            }
        }
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
    public abstract class AnimationKitKatCallback extends AnimationCallback
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
}
