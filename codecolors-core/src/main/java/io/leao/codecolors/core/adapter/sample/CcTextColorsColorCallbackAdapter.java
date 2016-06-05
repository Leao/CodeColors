package io.leao.codecolors.core.adapter.sample;

import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import io.leao.codecolors.core.adapter.CcColorCallbackAdapter;
import io.leao.codecolors.core.callback.sample.CcRefreshDrawableStateCallback;
import io.leao.codecolors.core.color.CcColorStateList;

public class CcTextColorsColorCallbackAdapter implements CcColorCallbackAdapter<View> {
    @Override
    public boolean onCache(CacheResult<View> outResult) {
        outResult.set(new CcRefreshDrawableStateCallback<>());
        return true;
    }

    @Override
    public boolean onInflate(AttributeSet attrs, View view, int defStyleAttr, int defStyleRes,
                             InflateAddResult<View> outResult) {
        return onAdd(view, outResult);
    }

    @Override
    public boolean onAdd(View view, InflateAddResult<View> outResult) {
        if (view instanceof TextView) {
            ColorStateList textColors = ((TextView) view).getTextColors();
            if (textColors instanceof CcColorStateList) {
                outResult.set(view);
                outResult.add((CcColorStateList) textColors);
                return true;
            }
        }
        return false;
    }
}
