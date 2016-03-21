package io.leao.codecolors.adapter;

import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import java.util.Set;

import io.leao.codecolors.callback.CcRefreshDrawableStateCallback;
import io.leao.codecolors.res.CcColorStateList;

public class CcTextColorsViewCallbackAdapter implements CcViewCallbackAdapter<TextView> {
    @NonNull
    @Override
    public CcColorStateList.AnchorCallback<TextView> getAnchorCallback() {
        return new CcRefreshDrawableStateCallback<>();
    }

    @Override
    public void getCodeColors(AttributeSet attrs, View view, Set<CcColorStateList> outColors) {
        if (view instanceof TextView) {
            ColorStateList textColors = ((TextView) view).getTextColors();
            if (textColors instanceof CcColorStateList) {
                outColors.add((CcColorStateList) textColors);
            }
        }
    }

    @Override
    public TextView getAnchor(AttributeSet attrs, View view) {
        return (TextView) view;
    }
}
