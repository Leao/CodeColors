package io.leao.codecolors.core.inflate.sample;

import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import io.leao.codecolors.core.callback.CcRefreshDrawableStateCallback;
import io.leao.codecolors.core.color.CodeColor;
import io.leao.codecolors.core.inflate.CcColorCallbackAdapter;

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
            if (textColors instanceof CodeColor) {
                outResult.set(view);
                outResult.add((CodeColor) textColors);
                return true;
            }
        }
        return false;
    }
}
