package io.leao.codecolors.core.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

public class CcAdapterManager {
    private AdapterColorCallbackHandler mColorCallbackHandler;
    private AdapterAttrCallbackHandler mAttrCallbackHandler;
    private AdapterDefStyleHandler mDefStyleHandler;

    public CcAdapterManager() {
        mColorCallbackHandler = onCreateColorCallbackAdaptersHandler();
        mAttrCallbackHandler = onCreateAttrCallbackAdaptersHandler();
        mDefStyleHandler = onCreateDefStyleAdaptersHandler();
    }

    protected AdapterColorCallbackHandler onCreateColorCallbackAdaptersHandler() {
        return new AdapterColorCallbackHandler();
    }

    protected AdapterAttrCallbackHandler onCreateAttrCallbackAdaptersHandler() {
        return new AdapterAttrCallbackHandler();
    }

    protected AdapterDefStyleHandler onCreateDefStyleAdaptersHandler() {
        return new AdapterDefStyleHandler();
    }

    public synchronized void addColorCallbackAdapter(@NonNull CcColorCallbackAdapter adapter) {
        mColorCallbackHandler.addAdapter(adapter);
    }

    public synchronized void addAttrCallbackAdapter(@NonNull CcAttrCallbackAdapter adapter) {
        mAttrCallbackHandler.addAdapter(adapter);
    }

    public synchronized void addDefStyleAdapter(@NonNull CcDefStyleAdapter adapter) {
        mDefStyleHandler.addAdapter(adapter);
    }

    public synchronized void onCreateView(@NonNull Context context, @NonNull AttributeSet attrs, @NonNull View view) {
        AdapterDefStyleHandler.InflateResult defStyle = mDefStyleHandler.onCreateView(attrs, view);
        mColorCallbackHandler.onCreateView(attrs, view, defStyle.attr, defStyle.res);
        mAttrCallbackHandler.onCreateView(context, attrs, view, defStyle.attr, defStyle.res);
    }

    public synchronized void addView(@NonNull View view) {
        mColorCallbackHandler.onAddView(view);
    }
}
