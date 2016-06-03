package io.leao.codecolors.appcompat.inflate.sample;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.support.v4.view.TintableBackgroundView;
import android.util.AttributeSet;
import android.view.View;

import java.util.Set;

import io.leao.codecolors.core.color.CcColorStateList;
import io.leao.codecolors.core.inflate.CcColorCallbackAdapter;

@SuppressLint("PrivateResource")
public class CcTintableBackgroundColorCallbackAdapter implements CcColorCallbackAdapter<View> {
    @Override
    public boolean onCache(CacheResult<View> outResult) {
        return false;
    }

    @Override
    public boolean onInflate(AttributeSet attrs, View view, int defStyleAttr, int defStyleRes,
                             InflateAddResult<View> outResult) {
        return onAdd(view, outResult);
    }

    @Override
    public boolean onAdd(View view, InflateAddResult<View> outResult) {
        if (view instanceof TintableBackgroundView) {
            ColorStateList backgroundTintList = ((TintableBackgroundView) view).getSupportBackgroundTintList();
            if (backgroundTintList instanceof CcColorStateList) {
                outResult.set(view);
                outResult.add(
                        (CcColorStateList) backgroundTintList,
                        new TintableBackgroundCallback(backgroundTintList));
                return true;
            }
        }
        return false;
    }

    private static class TintableBackgroundCallback implements CcColorStateList.AnchorCallback<View> {
        private ColorStateList mBackgroundTintList;

        public TintableBackgroundCallback(ColorStateList backgroundTintList) {
            mBackgroundTintList = backgroundTintList;
        }

        @Override
        public void invalidateColor(View view, CcColorStateList color) {
            if (!refreshDrawableState(view)) {
                color.removeCallback(this);
            }
        }

        @Override
        public void invalidateColors(View view, Set<CcColorStateList> colors) {
            if (!refreshDrawableState(view)) {
                for (CcColorStateList color : colors) {
                    color.removeCallback(this);
                }
            }
        }

        private boolean refreshDrawableState(View view) {
            if (((TintableBackgroundView) view).getSupportBackgroundTintList() == mBackgroundTintList) {
                view.refreshDrawableState();
                return true;
            } else {
                return false;
            }
        }
    }
}
