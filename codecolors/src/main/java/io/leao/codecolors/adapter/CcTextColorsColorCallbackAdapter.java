package io.leao.codecolors.adapter;

import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import io.leao.codecolors.callback.CcRefreshDrawableStateCallback;
import io.leao.codecolors.res.CcColorStateList;

public class CcTextColorsColorCallbackAdapter implements CcColorCallbackAdapter<TextView> {
    @Override
    public boolean onCache(CacheResult<TextView> outResult) {
        outResult.defaultCallback = new CcRefreshDrawableStateCallback<>();
        return true;
    }

    @Override
    public boolean onInflate(AttributeSet attrs, View view, int defStyleAttr, int defStyleRes,
                             InflateResult<TextView> outResult) {
        if (view instanceof TextView) {
            ColorStateList textColors = ((TextView) view).getTextColors();
            if (textColors instanceof CcColorStateList) {
                outResult.set((TextView) view);
                outResult.add((CcColorStateList) textColors);
                return true;
            }
        }
        return false;
    }
}
