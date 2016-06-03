package io.leao.codecolors.core.editor;

import android.animation.ValueAnimator;

import java.lang.ref.WeakReference;

import io.leao.codecolors.core.color.CcColorStateList;

public class CcEditorAnimate extends CcEditor<CcEditorAnimate> implements ValueAnimator.AnimatorUpdateListener {
    protected WeakReference<CcColorStateList> mColorRef;
    protected ValueAnimator mAnimation;
    protected float mLastInvalidateFraction;

    public CcEditorAnimate(CcColorStateList color) {
        mColorRef = new WeakReference<>(color);

        mAnimation = CcColorStateList.createDefaultAnimation();
        // Listener to invalidate color.
        mAnimation.addUpdateListener(this);
    }

    public void start() {
        CcColorStateList color = mColorRef.get();
        if (color != null) {
            boolean changed = color.animate(this, mAnimation);
            if (changed) {
                mLastInvalidateFraction = 0;
                mAnimation.start();
            }
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float invalidateFraction = ((int) (animation.getAnimatedFraction() * 100) / 100f);

        if (invalidateFraction != mLastInvalidateFraction) {
            mLastInvalidateFraction = invalidateFraction;

            CcColorStateList color = mColorRef.get();
            if (color != null) {
                color.invalidateSelf();
            }
        }
    }
}