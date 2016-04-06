package io.leao.codecolors.adapter;

import android.content.res.ColorStateList;
import android.support.v4.view.TintableBackgroundView;
import android.util.AttributeSet;
import android.view.View;

import io.leao.codecolors.callback.CcRefreshDrawableStateCallback;
import io.leao.codecolors.res.CcColorStateList;

public class CcTintableBackgroundColorCallbackAdapter implements CcColorCallbackAdapter<View> {
    @Override
    public boolean onCache(CacheResult<View> outResult) {
        outResult.set(new CcRefreshDrawableStateCallback<>());
        return true;
    }

    @Override
    public boolean onInflate(AttributeSet attrs, View view, int defStyleAttr, int defStyleRes,
                             InflateResult<View> outResult) {
        if (view instanceof TintableBackgroundView) {
            ColorStateList backgroundTint = ((TintableBackgroundView) view).getSupportBackgroundTintList();
            if (backgroundTint instanceof CcColorStateList) {
                outResult.set(view);
                outResult.add((CcColorStateList) backgroundTint);
                return true;
            }
        }
        return false;
    }
}
