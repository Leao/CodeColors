package io.leao.codecolors.adapter;

import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.v4.view.TintableBackgroundView;
import android.util.AttributeSet;
import android.view.View;

import java.util.Set;

import io.leao.codecolors.callback.CcRefreshDrawableStateCallback;
import io.leao.codecolors.res.CcColorStateList;

public class CcTintableBackgroundViewCallbackAdapter implements CcViewCallbackAdapter<View> {
    @NonNull
    @Override
    public CcColorStateList.AnchorCallback<View> getAnchorCallback() {
        return new CcRefreshDrawableStateCallback<>();
    }

    @Override
    public void getCodeColors(AttributeSet attrs, View view, Set<CcColorStateList> outColors) {
        if (view instanceof TintableBackgroundView) {
            ColorStateList backgroundTint = ((TintableBackgroundView) view).getSupportBackgroundTintList();
            if (backgroundTint instanceof CcColorStateList) {
                outColors.add((CcColorStateList) backgroundTint);
            }
        }
    }

    @Override
    public View getAnchor(AttributeSet attrs, View view) {
        return view;
    }
}
