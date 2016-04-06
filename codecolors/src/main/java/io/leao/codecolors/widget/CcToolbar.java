package io.leao.codecolors.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toolbar;

import io.leao.codecolors.callback.CcRefreshDrawableStateCallback;
import io.leao.codecolors.res.CcColorStateList;

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
            addChildCallbackIfPossible(getChildAt(i));
        }
    }

    @Override
    public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
        mOnHierarchyChangeListener = listener;
    }

    @Override
    public void onChildViewAdded(View parent, View child) {
        addChildCallbackIfPossible(child);
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

    private void addChildCallbackIfPossible(View child) {
        if (child instanceof TextView) {
            ColorStateList textColors = ((TextView) child).getTextColors();
            if (textColors instanceof CcColorStateList) {
                ((CcColorStateList) textColors).addAnchorCallback(child, new CcRefreshDrawableStateCallback());
            }
        }
    }
}
