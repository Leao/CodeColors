package io.leao.codecolors.core.color;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;

import java.util.Set;

import io.leao.codecolors.core.CcCore;

public class MultiAnimateEditor extends MultiEditor<MultiAnimateEditor>
        implements ValueAnimator.AnimatorUpdateListener {
    Set<CcColorStateList> mChangedColors;

    private ValueAnimator mAnimation;
    private float mLastInvalidateFraction;

    public MultiAnimateEditor() {
        mAnimation = CcColorStateList.createDefaultAnimation();
        // Listener to invalidate color.
        mAnimation.addUpdateListener(this);
        mAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                CallbackTempUtils.recycleColorSet(mChangedColors);
                mChangedColors = null;
            }
        });
    }

    public void start() {
        Set<CcColorStateList> changedColors = CallbackTempUtils.getColorSet();

        for (int colorResId : mEditors.keySet()) {
            CcColorStateList.Editor editor = mEditors.get(colorResId);
            CcColorStateList color = CcCore.getColorsManager().getColor(colorResId);
            boolean changed = color.animate(editor, mAnimation);
            if (changed) {
                changedColors.add(color);
            }
        }

        if (changedColors.size() > 0) {
            mChangedColors = changedColors;
            mLastInvalidateFraction = 0;
            mAnimation.start();
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float invalidateFraction = ((int) (animation.getAnimatedFraction() * 100) / 100f);

        if (invalidateFraction != mLastInvalidateFraction) {
            mLastInvalidateFraction = invalidateFraction;

            invalidate(mChangedColors);
        }
    }
}