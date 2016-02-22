package io.leao.codecolors.adapter;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import io.leao.codecolors.res.CcColorStateList;

public class CcTextColorAnchorCallbackAdapter implements CcAttrCallbackAdapter<TextView> {
    @NonNull
    @Override
    public int[] getAttrs() {
        return new int[]{android.R.attr.textColor};
    }

    @Override
    public TextView getAnchor(View view, int attr) {
        if (view instanceof TextView) {
            return (TextView) view;
        } else {
            return null;
        }
    }

    @NonNull
    @Override
    public CcColorStateList.AnchorCallback<TextView> getAnchorCallback() {
        return new CcColorStateList.AnchorCallback<TextView>() {
            @Override
            public void invalidateColor(TextView anchor, CcColorStateList color) {
                anchor.refreshDrawableState();
            }
        };
    }
}
