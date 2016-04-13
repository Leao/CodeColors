package io.leao.codecolors.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import io.leao.codecolors.adapter.CcCallbackManager;

public class CcLayoutInflaterFactoryWrapper implements LayoutInflater.Factory2 {
    private final CcLayoutInflater mInflater;
    private LayoutInflater.Factory2 mFactory;

    public CcLayoutInflaterFactoryWrapper(CcLayoutInflater inflater, LayoutInflater.Factory2 factory) {
        mInflater = inflater;
        mFactory = factory;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return onCreateViewInternal(null, name, context, attrs, false);
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return onCreateViewInternal(parent, name, context, attrs, true);
    }

    private View onCreateViewInternal(View parent, String name, Context context, AttributeSet attrs, boolean isFactory2Call) {
        // We 'inject' our toolbar views, to make sure we add proper callbacks to their children views.
        // This workflow also exists in AppCompat library, but instead of creating the views ourselves, we simply change
        // the name of the view to inflate, to make sure the context is still themified by the AppCompat library.
        switch (name) {
            case "EditText":
                name = "io.leao.codecolors.widget.CcAppCompatEditText";
                break;
            case "Toolbar":
                name = "io.leao.codecolors.widget.CcToolbar";
                break;
            case "android.support.v7.widget.Toolbar":
                name = "io.leao.codecolors.widget.CcAppCompatToolbar";
                break;
            default:
                break;
        }

        View view = null;

        if (mFactory != null) {
            view = isFactory2Call ?
                    mFactory.onCreateView(parent, name, context, attrs) :
                    mFactory.onCreateView(name, context, attrs);
        }

        if (view == null) {
            view = mInflater.createViewFromTag(context, name, attrs);
        }

        if (view != null && attrs != null) {
            // Add callbacks to refresh drawable states.
            CcCallbackManager.getInstance().onCreateView(context, attrs, view);
        }

        return view;
    }
}