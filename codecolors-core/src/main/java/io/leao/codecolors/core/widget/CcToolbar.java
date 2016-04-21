package io.leao.codecolors.core.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

import io.leao.codecolors.CodeColors;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CcToolbar extends Toolbar implements ViewGroup.OnHierarchyChangeListener {
    private OnHierarchyChangeListener mOnHierarchyChangeListener;

    public CcToolbar(Context context) {
        super(context);
        init();
    }

    public CcToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CcToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CcToolbar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        super.setOnHierarchyChangeListener(this);
        for (int i = 0; i < getChildCount(); i++) {
            CodeColors.addView(getChildAt(i));
        }
    }

    @Override
    public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
        mOnHierarchyChangeListener = listener;
    }

    @Override
    public void onChildViewAdded(View parent, View child) {
        CodeColors.addView(child);

        if (mOnHierarchyChangeListener != null) {
            mOnHierarchyChangeListener.onChildViewAdded(parent, child);
        }
    }

    @Override
    public void onChildViewRemoved(View parent, View child) {
        if (mOnHierarchyChangeListener != null) {
            mOnHierarchyChangeListener.onChildViewRemoved(parent, child);
        }
    }
}
