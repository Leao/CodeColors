package io.leao.codecolors.sample.adapter;

import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;

import io.leao.codecolors.adapter.CcAttrCallbackAdapter;
import io.leao.codecolors.res.CcColorStateList;
import io.leao.codecolors.sample.R;

public class CcStatusBarColorAnchorCallbackAdapter implements CcAttrCallbackAdapter<CoordinatorLayout> {
    @NonNull
    @Override
    public int[] getAttrs() {
        return new int[]{R.attr.statusBarBackground};
    }

    @Override
    public CoordinatorLayout getAnchor(View view, int attr) {
        if (view instanceof CoordinatorLayout) {
            return (CoordinatorLayout) view;
        } else {
            return null;
        }
    }


    @NonNull
    @Override
    public CcColorStateList.AnchorCallback<CoordinatorLayout> getAnchorCallback() {
        return new CcColorStateList.AnchorCallback<CoordinatorLayout>() {
            @Override
            public void invalidateColor(CoordinatorLayout anchor, CcColorStateList color) {
                anchor.invalidate();
            }
        };
    }
}
