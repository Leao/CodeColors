package io.leao.codecolors.appcompat.inflate.sample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.support.v4.view.TintableBackgroundView;
import android.util.AttributeSet;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.Set;

import io.leao.codecolors.core.CcCore;
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
        private WeakReference<Activity> mActivityRef;

        public TintableBackgroundCallback(ColorStateList backgroundTintList) {
            mBackgroundTintList = backgroundTintList;
            mActivityRef = CcCore.getActivityManager().getLastResumedActivityReference();
        }

        @Override
        public void invalidateColor(View view, CcColorStateList color) {
            if (!refreshDrawableState(view)) {
                color.removeCallback(mActivityRef.get(), this);
            }
        }

        @Override
        public void invalidateColors(View view, Set<CcColorStateList> colors) {
            if (!refreshDrawableState(view)) {
                for (CcColorStateList color : colors) {
                    color.removeCallback(mActivityRef.get(), this);
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
