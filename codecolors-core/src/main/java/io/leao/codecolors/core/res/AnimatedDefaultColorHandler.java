package io.leao.codecolors.core.res;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

class AnimatedDefaultColorHandler extends DefaultColorHandler {
    public static final int DEFAULT_ANIMATION_DURATION_MS = 400;
    public static final Interpolator DEFAULT_ANIMATION_INTERPOLATOR = new DecelerateInterpolator();

    protected BaseColorHandler mAnimationColorHandler;

    protected AnimateBuilder mAnimateBuilder;

    protected ValueAnimator mAnimation;

    protected CcColorStateList.AnimationCallback mAnimationCallback;
    protected ValueAnimator mDefaultAnimation;
    protected AnimationListener mAnimationListener;
    protected AnimationUpdateListener mAnimationUpdateListener;
    protected AnimationPauseListener mAnimationPauseListener;

    public AnimatedDefaultColorHandler() {
        this(new BaseColorHandler(), new BaseColorHandler());
    }

    protected AnimatedDefaultColorHandler(@NonNull BaseColorHandler defaultColorHandler,
                                          @NonNull BaseColorHandler colorHandler) {
        super(defaultColorHandler, colorHandler);
        mAnimationColorHandler = new BaseColorHandler(mColorHandler);
    }

    protected AnimatedDefaultColorHandler(Parcel source) {
        super(source);
        mAnimationColorHandler = new BaseColorHandler(mColorHandler);
    }

    @Override
    public CcColorStateList.SetBuilder set() {
        endAnimation(true);
        return super.set();
    }

    @Override
    protected void onSet(boolean changed) {
        mAnimationColorHandler.setTo(mColorHandler);
        super.onSet(changed);
    }

    public CcColorStateList.AnimateBuilder animate() {
        endAnimation(true);
        if (mAnimateBuilder == null) {
            mAnimateBuilder = new AnimateBuilder(mAnimationColorHandler, new AnimateBuilder.Callback() {
                @Override
                public void onSubmit(AnimateBuilder builder) {
                    onAnimate(
                            builder.getAnimation(),
                            builder.getDuration(),
                            builder.getInterpolator(),
                            builder.getCallback());
                }
            });
        }
        return mAnimateBuilder;
    }

    public ValueAnimator onAnimate(@Nullable ValueAnimator animation, @Nullable Integer duration,
                                   @Nullable Interpolator interpolator,
                                   @Nullable CcColorStateList.AnimationCallback callback) {
        mAnimationCallback = callback;

        if (animation != null) {
            mAnimation = animation;
        } else {
            if (mDefaultAnimation == null) {
                mDefaultAnimation = ValueAnimator.ofFloat(0, 1);
            }
            mAnimation = mDefaultAnimation;
        }
        // Listener.
        if (mAnimationListener == null) {
            mAnimationListener = new AnimationListener();
        }
        mAnimation.addListener(mAnimationListener);

        // Update listener (make sure to reset it).
        if (mAnimationUpdateListener == null) {
            mAnimationUpdateListener = new AnimationUpdateListener();
        }
        mAnimation.addUpdateListener(mAnimationUpdateListener.reset());

        // Pause listener.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (mAnimationPauseListener == null) {
                mAnimationPauseListener = new AnimationPauseListener();
            }
            mAnimation.addPauseListener(mAnimationPauseListener);
        }

        // Duration and interpolator.
        mAnimation.setDuration(duration != null ? duration : DEFAULT_ANIMATION_DURATION_MS);
        mAnimation.setInterpolator(interpolator != null ? interpolator : DEFAULT_ANIMATION_INTERPOLATOR);
        mAnimation.start();

        return mAnimation;
    }

    @Override
    public AnimatedDefaultColorHandler withAlpha(int alpha) {
        endAnimation(false);
        return new AnimatedDefaultColorHandler(mDefaultColorHandler.withAlpha(alpha), mColorHandler.withAlpha(alpha));
    }

    @Override
    public boolean isOpaque() {
        boolean isOpaque = super.isOpaque();
        if (isOpaque && mAnimation != null && mAnimation.isStarted()) {
            return mAnimationColorHandler.isOpaque();
        } else {
            return isOpaque;
        }
    }

    @Override
    public Integer getDefaultColor() {
        if (mAnimation == null || !mAnimation.isStarted() || mAnimation.getAnimatedFraction() == 0) {
            return super.getDefaultColor();
        } else if (mAnimation.getAnimatedFraction() == 1) {
            return getDefaultColorFromHandlerOrDefaultHandler(mAnimationColorHandler);
        } else {
            return interpolate(
                    super.getDefaultColor(),
                    getDefaultColorFromHandlerOrDefaultHandler(mAnimationColorHandler),
                    (float) mAnimation.getAnimatedValue());
        }
    }

    @Override
    public Integer getColorForState(@Nullable int[] stateSet, Integer defaultColor) {
        if (mAnimation == null || !mAnimation.isStarted() || mAnimation.getAnimatedFraction() == 0) {
            return super.getColorForState(stateSet, defaultColor);
        } else if (mAnimation.getAnimatedFraction() == 1) {
            return getColorForStateFromHandlerOrDefaultHandler(mAnimationColorHandler, stateSet, defaultColor);
        } else {
            return interpolate(
                    super.getColorForState(stateSet, defaultColor),
                    getColorForStateFromHandlerOrDefaultHandler(mAnimationColorHandler, stateSet, defaultColor),
                    (float) mAnimation.getAnimatedValue());
        }
    }

    public boolean endAnimation(boolean blockOnColorChanged) {
        if (mAnimation != null && mAnimation.isStarted()) {
            if (blockOnColorChanged && mAnimationUpdateListener != null) {
                mAnimationUpdateListener.blockOnColorChangedOnEnd();
            }
            mAnimation.end();
            return true;
        } else {
            return false;
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
            mColorHandler.setTo(mAnimationColorHandler);

            // Remove our listeners, as the animation may be external to this ColorHandler.
            removeListeners((ValueAnimator) animation);

            // Reset animation.
            mAnimationCallback = null;
            mAnimation = null;
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

        private void removeListeners(ValueAnimator animation) {
            animation.removeListener(mAnimationListener);
            animation.removeUpdateListener(mAnimationUpdateListener);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                animation.removePauseListener(mAnimationPauseListener);
            }
        }
    }

    /**
     * Calls animation callback and OnColorChangedListener (only when necessary).
     */
    private class AnimationUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        private float mUpdateFraction;
        private boolean mBlockOnColorChangedOnEnd;

        /**
         * Reset listener state before a new animation.
         */
        public AnimationUpdateListener reset() {
            mUpdateFraction = 0;
            mBlockOnColorChangedOnEnd = false;
            return this;
        }

        public void blockOnColorChangedOnEnd() {
            mBlockOnColorChangedOnEnd = true;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            if (mAnimationCallback != null) {
                mAnimationCallback.onAnimationUpdate(animation);
            }

            float updateFraction = ((int) (animation.getAnimatedFraction() * 100) / 100f);

            if (updateFraction != mUpdateFraction) {
                mUpdateFraction = updateFraction;

                // Call onColorChanged() for every update that does not end the animation,
                // or if we are not blocking its call when the animation ends.
                if (mUpdateFraction != 1 || !mBlockOnColorChangedOnEnd) {
                    onColorChanged();
                }
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

    public static class AnimateBuilder extends Builder<AnimateBuilder> implements CcColorStateList.AnimateBuilder {
        private ValueAnimator mAnimation;
        private Integer mDuration;
        private Interpolator mInterpolator;
        private CcColorStateList.AnimationCallback mCallback;

        public AnimateBuilder(BaseColorHandler baseColorHandler, Callback callback) {
            super(baseColorHandler, callback);
        }

        public ValueAnimator getAnimation() {
            return mAnimation;
        }

        public AnimateBuilder setAnimation(ValueAnimator animation) {
            mAnimation = animation;
            return this;
        }

        public Integer getDuration() {
            return mDuration;
        }

        public AnimateBuilder setDuration(int duration) {
            mDuration = duration;
            return this;
        }

        public Interpolator getInterpolator() {
            return mInterpolator;
        }

        public AnimateBuilder setInterpolator(Interpolator interpolator) {
            mInterpolator = interpolator;
            return this;
        }

        public CcColorStateList.AnimationCallback getCallback() {
            return mCallback;
        }

        public AnimateBuilder setCallback(CcColorStateList.AnimationCallback callback) {
            mCallback = callback;
            return this;
        }

        interface Callback extends Builder.Callback<AnimateBuilder> {
        }
    }
}
