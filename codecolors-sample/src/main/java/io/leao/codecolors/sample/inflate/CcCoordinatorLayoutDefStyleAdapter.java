package io.leao.codecolors.sample.inflate;

import android.annotation.SuppressLint;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import io.leao.codecolors.core.inflate.CcDefStyleAdapter;
import io.leao.codecolors.sample.R;

public class CcCoordinatorLayoutDefStyleAdapter implements CcDefStyleAdapter {
    @SuppressLint("PrivateResource")
    @Override
    public boolean onInflate(AttributeSet attrs, View view, InflateResult outResult) {
        if (view instanceof CoordinatorLayout) {
            outResult.set(0, R.style.Widget_Design_CoordinatorLayout);
            return true;
        }
        return false;
    }
}
