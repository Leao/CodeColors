package io.leao.codecolors.res;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

class AnimatedDefaultColorHandler extends DefaultColorHandler {
    public static final int DEFAULT_ANIMATION_DURATION_MS = 400;
    public static final Interpolator DEFAULT_ANIMATION_INTERPOLATOR = new DecelerateInterpolator();

    protected BaseColorHandler mAnimationColorHandler;

    protected ValueAnimator mAnimation;
    protected CcColorStateList.AnimationCallback mAnimationCallback;
    protected AnimationUpdateListener mAnimationUpdateListener;

    public AnimatedDefaultColorHandler() {
        this(null, null);
    }

    protected AnimatedDefaultColorHandler(@Nullable BaseColorHandler defaultColorHandler,
                                          @Nullable BaseColorHandler colorHandler) {
        super(defaultColorHandler, colorHandler);
    }

    protected AnimatedDefaultColorHandler(Parcel source) {
        super(source);
    }

    @Override
    public AnimatedDefaultColorHandler withAlpha(int alpha) {
        endAnimation(false);
        return new AnimatedDefaultColorHandler(
                mDefaultColorHandler != null ? mDefaultColorHandler.withAlpha(alpha) : null,
                mColorHandler != null ? mColorHandler.withAlpha(alpha) : null);
    }

    @Override
    public boolean isOpaque() {
        boolean isOpaque = super.isOpaque();
        if (mAnimation != null && mAnimation.isStarted()) {
            isOpaque &= mAnimationColorHandler == null || mAnimationColorHandler.isOpaque();
        }
        return isOpaque;
    }

    @Override
    public int getDefaultColor() {
        if (mAnimation == null || !mAnimation.isStarted() || mAnimation.getAnimatedFraction() == 0) {
            return super.getDefaultColor();
        } else if (mAnimation.getAnimatedFraction() == 1) {
            return getDefaultColorOrDefault(mAnimationColorHandler);
        } else {
            return interpolate(
                    super.getDefaultColor(),
                    getDefaultColorOrDefault(mAnimationColorHandler),
                    (float) mAnimation.getAnimatedValue());
        }
    }

    @Override
    public void setColor(ColorStateList color) {
        endAnimation(true);
        super.setColor(color);
    }

    @Override
    public Integer getColorForState(@Nullable int[] stateSet, Integer defaultColor) {
        if (mAnimation == null || !mAnimation.isStarted() || mAnimation.getAnimatedFraction() == 0) {
            return super.getColorForState(stateSet, defaultColor);
        } else if (mAnimation.getAnimatedFraction() == 1) {
            return getColorForStateOrDefault(mAnimationColorHandler, stateSet, defaultColor);
        } else {
            return interpolate(
                    super.getColorForState(stateSet, defaultColor),
                    getColorForStateOrDefault(mAnimationColorHandler, stateSet, defaultColor),
                    (float) mAnimation.getAnimatedValue());
        }
    }

    public ValueAnimator animateTo(ColorStateList color,
                                   @Nullable Integer duration,
                                   @Nullable Interpolator interpolator,
                                   @Nullable CcColorStateList.AnimationCallback callback) {
        endAnimation(false);

        if (!isChangingColor(color)) {
            return null;
        }

        mAnimationColorHandler = color != null ? new BaseColorHandler(color) : null;
        mAnimationCallback = callback;

        ensureAnimation();
        mAnimation.setDuration(duration != null ? duration : DEFAULT_ANIMATION_DURATION_MS);
        mAnimation.setInterpolator(interpolator != null ? interpolator : DEFAULT_ANIMATION_INTERPOLATOR);
        mAnimation.start();

        return mAnimation;
    }

    private void ensureAnimation() {
        if (mAnimation == null) {
            mAnimation = ValueAnimator.ofFloat(0, 1);
            // Listener.
            mAnimation.addListener(new AnimationListener());
            // Update listener.
            mAnimationUpdateListener = new AnimationUpdateListener();
            mAnimation.addUpdateListener(mAnimationUpdateListener);
            // Pause listener.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mAnimation.addPauseListener(new AnimationPauseListener());
            }
        }
    }

    public void endAnimation(boolean blockColorChange) {
        if (mAnimation != null && mAnimation.isStarted()) {
            if (blockColorChange && mAnimationUpdateListener != null) {
                mAnimationUpdateListener.blockColorChange();
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        endAnimation(true);
        super.writeToParcel(dest, flags);
    }

    public static final Parcelable.Creator<AnimatedDefaultColorHandler> CREATOR =
            new Parcelable.Creator<AnimatedDefaultColorHandler>() {
                @Override
                public AnimatedDefaultColorHandler[] newArray(int size) {
                    return new AnimatedDefaultColorHandler[size];
                }

                @Override
                public AnimatedDefaultColorHandler createFromParcel(Parcel source) {
                    return new AnimatedDefaultColorHandler(source);
                }
            };

    /**
     * Calls animation callback and resets handler state at the end.
     */
    private class AnimationListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation) {
            if (mAnimationCallback != null) {
                mAnimationCallback.onAnimationStart(animation);
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (mAnimationCallback != null) {
                mAnimationCallback.onAnimationEnd(animation);
            }

            // Reset handler state.
            mColorHandler = mAnimationColorHandler;
            mAnimationColorHandler = null;
            mAnimationCallback = null;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            if (mAnimationCallback != null) {
                mAnimationCallback.onAnimationCancel(animation);
            }
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
            if (mAnimationCallback != null) {
                mAnimationCallback.onAnimationRepeat(animation);
            }
        }
    }

    /**
     * Calls animation callback and OnColorChangedListener (only when necessary).
     */
    private class AnimationUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        private float mUpdateFraction;
        private boolean mBlockColorChange;

        public void blockColorChange() {
            mBlockColorChange = true;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            if (mAnimationCallback != null) {
                mAnimationCallback.onAnimationUpdate(animation);
            }

            float updateFraction = ((int) (animation.getAnimatedFraction() * 100) / 100f);

            if (updateFraction != mUpdateFraction) {
                mUpdateFraction = updateFraction;

                if (mUpdateFraction != 1 || !mBlockColorChange) {
                    mOnColorChangedListener.onColorChanged();
                }
            }

            // Reset listener state.
            if (mUpdateFraction == 1) {
                mUpdateFraction = 0;
                mBlockColorChange = false;
            }
        }
    }

    /**
     * Calls animation callback.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private class AnimationPauseListener implements Animator.AnimatorPauseListener {

        @Override
        public void onAnimationPause(Animator animation) {
            if (mAnimationCallback instanceof CcColorStateList.AnimationCallbackKitKat) {
                ((CcColorStateList.AnimationCallbackKitKat) mAnimationCallback).onAnimationPause(animation);
            }
        }

        @Override
        public void onAnimationResume(Animator animation) {
            if (mAnimationCallback instanceof CcColorStateList.AnimationCallbackKitKat) {
                ((CcColorStateList.AnimationCallbackKitKat) mAnimationCallback).onAnimationResume(animation);
            }
        }
    }
}
