package io.leao.codecolors.core.inflate.sample;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import java.util.Set;

import io.leao.codecolors.core.color.CodeColor;
import io.leao.codecolors.core.inflate.CcColorCallbackAdapter;

public class CcTextHighlightColorColorCallbackAdapter implements CcColorCallbackAdapter<TextView> {
    @Override
    public boolean onCache(CacheResult<TextView> outResult) {
        outResult.set(
                new CodeColor.AnchorCallback<TextView>() {
                    @Override
                    public void invalidateColor(TextView textView, CodeColor color) {
                        textView.setHighlightColor(color.getDefaultColor());
                    }

                    @Override
                    public <U extends CodeColor> void invalidateColors(TextView textView, Set<U> colors) {
                        invalidateColor(textView, colors.iterator().next());
                    }
                });
        return true;
    }

    @SuppressWarnings("ResourceType")
    @Override
    public boolean onInflate(AttributeSet attrs, View view, int defStyleAttr, int defStyleRes,
                             InflateAddResult<TextView> outResult) {
        if (view instanceof TextView) {
            ColorStateList highlightColor = null;

            Context context = view.getContext();
            TypedArray ta = context.obtainStyledAttributes(
                    attrs, new int[]{android.R.attr.textAppearance, android.R.attr.textColorHighlight});
            try {
                int appearanceResId = ta.getResourceId(0, 0);
                if (appearanceResId != 0) {
                    TypedArray ap = context.obtainStyledAttributes(
                            appearanceResId, new int[]{android.R.attr.textColorHighlight});
                    try {
                        highlightColor = ap.getColorStateList(0);
                    } finally {
                        ap.recycle();
                    }
                }

                if (highlightColor == null) {
                    highlightColor = ta.getColorStateList(1);
                }
            } finally {
                ta.recycle();
            }

            if (highlightColor instanceof CodeColor) {
                outResult.set((TextView) view);
                outResult.add((CodeColor) highlightColor);
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean onAdd(View view, InflateAddResult<TextView> outResult) {
        return false;
    }
}
