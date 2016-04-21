package io.leao.codecolors.appcompat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import io.leao.codecolors.core.view.CcLayoutInflater;
import io.leao.codecolors.core.view.CcLayoutInflaterFactoryWrapper;

public class CcAppCompatLayoutInflaterFactoryWrapper extends CcLayoutInflaterFactoryWrapper {

    public CcAppCompatLayoutInflaterFactoryWrapper(CcLayoutInflater inflater, LayoutInflater.Factory2 factory) {
        super(inflater, factory);
    }

    @Override
    protected View onCreateView(View parent, String name, Context context, AttributeSet attrs, boolean isFactory2Call) {
        // We 'inject' our AppCompat views, to make sure we add proper callbacks to their children views.
        // This workflow also exists in AppCompat library, but instead of creating the views ourselves, we simply change
        // the name of the view to inflate, to make sure the context is still themified by the AppCompat library.
        switch (name) {
            case "EditText":
                name = "io.leao.codecolors.appcompat.widget.CcAppCompatEditText";
                break;
            case "android.support.v7.widget.Toolbar":
                name = "io.leao.codecolors.appcompat.widget.CcAppCompatToolbar";
                break;
            default:
                break;
        }

        return super.onCreateView(parent, name, context, attrs, isFactory2Call);
    }
}
