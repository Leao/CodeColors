package io.leao.codecolors.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import io.leao.codecolors.manager.CcCallbackManager;
import io.leao.codecolors.widget.CcAppCompatToolbar;
import io.leao.codecolors.widget.CcToolbar;

public class CcLayoutInflaterFactoryWrapper implements LayoutInflater.Factory2 {
    private final CcLayoutInflater mInflater;
    private LayoutInflater.Factory2 mFactory;

    public CcLayoutInflaterFactoryWrapper(CcLayoutInflater inflater, LayoutInflater.Factory2 factory) {
        mInflater = inflater;
        mFactory = factory;
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return onCreateView(name, context, attrs);
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        View view;
        // We 'inject' our toolbar views, to make sure we add proper callbacks to its children (title, subtitle) views.
        // This workflow also exists in AppCompat library.
        switch (name) {
            case "Toolbar":
                view = new CcToolbar(context, attrs);
                break;
            case "android.support.v7.widget.Toolbar":
                view = new CcAppCompatToolbar(context, attrs);
                break;
            default:
                view = null;
                break;
        }

        if (view == null && mFactory != null) {
            view = mFactory.onCreateView(name, context, attrs);
        }

        if (view == null) {
            view = mInflater.createViewFromTag(context, name, attrs);
        }

        if (view != null && attrs != null) {
            // Add callbacks to refresh drawable states.
            CcCallbackManager.getInstance().addColorCallbacks(context, attrs, view);
        }

        return view;
    }
}