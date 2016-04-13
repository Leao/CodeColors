package io.leao.codecolors.adapter;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.support.v4.view.TintableBackgroundView;
import android.util.AttributeSet;
import android.view.View;

import io.leao.codecolors.res.CcColorStateList;

@SuppressLint("PrivateResource")
public class CcTintableBackgroundColorCallbackAdapter implements CcColorCallbackAdapter<View> {
    @Override
    public boolean onCache(CacheResult<View> outResult) {
        return false;
    }

    @Override
    public boolean onInflate(AttributeSet attrs, View view, int defStyleAttr, int defStyleRes,
                             InflateResult<View> outResult) {
        if (view instanceof TintableBackgroundView) {
            ColorStateList backgroundTintList = ((TintableBackgroundView) view).getSupportBackgroundTintList();
            if (backgroundTintList instanceof CcColorStateList) {
                outResult.set(view);
                outResult.add(
                        (CcColorStateList) backgroundTintList,
                        new TintableBackgroundRefreshDrawableCallback(backgroundTintList));
                return true;
            }
        }
        return false;
    }

    private static class TintableBackgroundRefreshDrawableCallback implements CcColorStateList.AnchorCallback<View> {
        private ColorStateList mBackgroundTintList;

        public TintableBackgroundRefreshDrawableCallback(ColorStateList backgroundTintList) {
            mBackgroundTintList = backgroundTintList;
        }

        @Override
        public void invalidateColor(View anchor, CcColorStateList color) {
            if (((TintableBackgroundView) anchor).getSupportBackgroundTintList() == mBackgroundTintList) {
                anchor.refreshDrawableState();
            } else {
                color.removeCallback(this);
            }
        }
    }
}
