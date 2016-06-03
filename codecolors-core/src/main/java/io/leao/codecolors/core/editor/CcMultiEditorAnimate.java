package io.leao.codecolors.core.editor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;

import java.util.Set;

import io.leao.codecolors.core.CcCore;
import io.leao.codecolors.core.color.CcColorStateList;
import io.leao.codecolors.core.util.CcTempUtils;

public class CcMultiEditorAnimate extends CcMultiEditor<CcMultiEditorAnimate>
        implements ValueAnimator.AnimatorUpdateListener {
    protected Set<CcColorStateList> mChangedColors;

    protected ValueAnimator mAnimation;
    protected float mLastInvalidateFraction;

    public CcMultiEditorAnimate() {
        mAnimation = CcColorStateList.createDefaultAnimation();
        // Listener to invalidate color.
        mAnimation.addUpdateListener(this);
        mAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                CcTempUtils.recycleColorSet(mChangedColors);
                mChangedColors = null;
            }
        });
    }

    public void start() {
        Set<CcColorStateList> changedColors = CcTempUtils.getColorSet();

        for (int colorResId : mEditors.keySet()) {
            CcColorStateList color = CcCore.getColorsManager().getColor(colorResId);
            if (color != null) {
                CcEditor editor = mEditors.get(colorResId);
                boolean changed = color.animate(editor, mAnimation);
                if (changed) {
                    changedColors.add(color);
                }
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