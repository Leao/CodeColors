package io.leao.codecolors.sample.adapter;

import android.annotation.SuppressLint;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import io.leao.codecolors.adapter.CcViewDefStyleAdapter;
import io.leao.codecolors.sample.R;

public class CcCoordinatorLayoutDefStyleAdapter implements CcViewDefStyleAdapter {

    @SuppressLint("PrivateResource")
    @Override
    public boolean getDefStyle(AttributeSet attrs, View view, DefStyle outDefStyle) {
        if (view instanceof CoordinatorLayout) {
            outDefStyle.attr = 0;
            outDefStyle.res = R.style.Widget_Design_CoordinatorLayout;
            return true;
        } else {
            return false;
        }
    }
}
