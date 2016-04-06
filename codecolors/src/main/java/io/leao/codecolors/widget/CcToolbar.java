package io.leao.codecolors.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.widget.Toolbar;

import io.leao.codecolors.callback.CcRefreshDrawableStateCallback;
import io.leao.codecolors.res.CcColorStateList;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CcToolbar extends Toolbar {
    public CcToolbar(Context context) {
        super(context);
    }

    public CcToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CcToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CcToolbar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
