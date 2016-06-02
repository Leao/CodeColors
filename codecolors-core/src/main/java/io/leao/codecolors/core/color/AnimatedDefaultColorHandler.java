package io.leao.codecolors.core.color;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Varies its color depending on the state of an animation.
 */
class AnimatedDefaultColorHandler extends DefaultColorHandler {
    protected BaseColorHandler mAnimationColorHandler;

    protected ValueAnimator mAnimation;
    protected AnimationListener mAnimationListener;

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

    public CcColorStateList.ColorSetter getAnimationColorSetter() {
        return mAnimationColorHandler;
    }

    public ValueAnimator getAnimation() {
        return mAnimation;
    }

    public void setAnimation(ValueAnimator animation) {
        if (mAnimation != animation) {
            if (mAnimation != null && mAnimationListener != null) {
                mAnimation.removeListener(mAnimationListener);
            }

            if (animation != null) {
                if (mAnimationListener == null) {
                    mAnimationListener = new AnimationListener();
                }
                animation.addListener(mAnimationListener);
            }

            mAnimation = animation;
        }
    }

    protected void endAnimation() {
        if (isAnimating()) {
            mAnimation.end();
        }
    }

    protected void cancelAnimation() {
        if (isAnimating()) {
            mAnimation.cancel();
        }
    }

    protected boolean isAnimating() {
        return mAnimation != null && mAnimation.isStarted();
    }

    @Override
    public AnimatedDefaultColorHandler withAlpha(int alpha) {
        return new AnimatedDefaultColorHandler(mDefaultColorHandler.withAlpha(alpha), mColorHandler.withAlpha(alpha));
    }

    @Override
    public boolean isOpaque() {
        boolean isOpaque = super.isOpaque();
        if (isOpaque && isAnimating()) {
            return mAnimationColorHandler.isOpaque();
        } else {
            return isOpaque;
        }
    }

    @Override
    public Integer getDefaultColor() {
        if (!isAnimating() || mAnimation.getAnimatedFraction() == 0) {
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
        if (!isAnimating() || mAnimation.getAnimatedFraction() == 0) {
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
        // Make sure we are not animating.
        endAnimation();

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

    private class AnimationListener extends AnimatorListenerAdapter {
        @Override
        public void onAnimationEnd(Animator animation) {
            // Set color to the color we just transitioned to.
            mColorHandler.setTo(mAnimationColorHandler);

            mAnimation.removeListener(this);
            mAnimation = null;
        }
    }
}