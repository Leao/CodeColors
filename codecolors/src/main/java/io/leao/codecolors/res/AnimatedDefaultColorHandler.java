package io.leao.codecolors.res;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.res.ColorStateList;
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

    protected ValueAnimator mAnimation;
    protected CcColorStateList.AnimationCallback mAnimationCallback;
    protected AnimationUpdateListener mAnimationUpdateListener;

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
            return getDefaultColorOrDefault(mAnimationColorHandler);
        } else {
            return interpolate(
                    super.getDefaultColor(),
                    getDefaultColorOrDefault(mAnimationColorHandler),
                    (float) mAnimation.getAnimatedValue());
        }
    }

    @Override
    public boolean setColor(ColorStateList color) {
        boolean ended = endAnimation(true);
        boolean changed = super.setColor(color);
        mAnimationColorHandler.setColor(color);
        if (ended && !changed) {
            onColorChanged();
        }
        return changed;
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

    @Override
    public boolean setStates(int[][] states, int[] colors) {
        boolean ended = endAnimation(true);
        boolean changed = super.setStates(states, colors);
        mAnimationColorHandler.setStates(states, colors);
        if (ended && !changed) {
            onColorChanged();
        }
        return changed;
    }

    @Override
    public boolean setState(int[] state, int color) {
        boolean ended = endAnimation(true);
        boolean changed = super.setState(state, color);
        mAnimationColorHandler.setState(state, color);
        if (ended && !changed) {
            onColorChanged();
        }
        return changed;
    }

    @Override
    public boolean removeStates(int[][] states) {
        boolean ended = endAnimation(true);
        boolean changed = super.removeStates(states);
        mAnimationColorHandler.removeStates(states);
        if (ended && !changed) {
            onColorChanged();
        }
        return changed;
    }

    @Override
    public boolean removeState(int[] state) {
        boolean ended = endAnimation(true);
        boolean changed = super.removeState(state);
        mAnimationColorHandler.removeState(state);
        if (ended && !changed) {
            onColorChanged();
        }
        return changed;
    }

    public boolean setAnimationColor(int color) {
        return setAnimationColor(ColorStateList.valueOf(color));
    }

    public boolean setAnimationColor(ColorStateList color) {
        endAnimation(false);
        return mAnimationColorHandler.setColor(color);
    }

    public boolean setAnimationStates(int[][] states, int[] colors) {
        endAnimation(false);
        return mAnimationColorHandler.setStates(states, colors);
    }

    public boolean setAnimationState(int[] state, int color) {
        endAnimation(false);
        return mAnimationColorHandler.setState(state, color);
    }

    public boolean removeAnimationStates(int[][] states) {
        endAnimation(false);
        return mAnimationColorHandler.removeStates(states);
    }

    public boolean removeAnimationState(int[] state) {
        endAnimation(false);
        return mAnimationColorHandler.removeState(state);
    }

    public ValueAnimator startAnimation(@Nullable Integer duration,
                                        @Nullable Interpolator interpolator,
                                        @Nullable CcColorStateList.AnimationCallback callback) {
        mAnimationCallback = callback;

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
        mAnimation.setDuration(duration != null ? duration : DEFAULT_ANIMATION_DURATION_MS);
        mAnimation.setInterpolator(interpolator != null ? interpolator : DEFAULT_ANIMATION_INTERPOLATOR);
        mAnimation.start();

        return mAnimation;
    }

    public boolean endAnimation(boolean blockColorChange) {
        if (mAnimation != null && mAnimation.isStarted()) {
            if (blockColorChange && mAnimationUpdateListener != null) {
                mAnimationUpdateListener.blockColorChange();
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
            mColorHandler = new BaseColorHandler(mAnimationColorHandler);
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
                    onColorChanged();
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
