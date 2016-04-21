package io.leao.codecolors.appcompat.widget;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import io.leao.codecolors.CodeColors;

public class CcAppCompatToolbar extends Toolbar implements ViewGroup.OnHierarchyChangeListener {
    private OnHierarchyChangeListener mOnHierarchyChangeListener;

    public CcAppCompatToolbar(Context context) {
        super(context);
        init();
    }

    public CcAppCompatToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CcAppCompatToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
