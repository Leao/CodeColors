package io.leao.codecolors.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import io.leao.codecolors.callback.CcRefreshDrawableStateCallback;
import io.leao.codecolors.res.CcColorStateList;

public class CcAppCompatToolbar extends Toolbar {
    public CcAppCompatToolbar(Context context) {
        super(context);
    }

    public CcAppCompatToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CcAppCompatToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);

        if (child instanceof TextView) {
            ColorStateList textColors = ((TextView) child).getTextColors();
            if (textColors instanceof CcColorStateList) {
                ((CcColorStateList) textColors).addAnchorCallback(child, new CcRefreshDrawableStateCallback());
            }
        }
    }
}
