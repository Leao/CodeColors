package io.leao.codecolors.core.inflate;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

public class CcInflateManager {
    private ColorCallbackHandler mColorCallbackHandler;
    private AttrCallbackHandler mAttrCallbackHandler;
    private DefStyleHandler mDefStyleHandler;

    public CcInflateManager() {
        mColorCallbackHandler = onCreateColorCallbackAdaptersHandler();
        mAttrCallbackHandler = onCreateAttrCallbackAdaptersHandler();
        mDefStyleHandler = onCreateDefStyleAdaptersHandler();
    }

    protected ColorCallbackHandler onCreateColorCallbackAdaptersHandler() {
        return new ColorCallbackHandler();
    }

    protected AttrCallbackHandler onCreateAttrCallbackAdaptersHandler() {
        return new AttrCallbackHandler();
    }

    protected DefStyleHandler onCreateDefStyleAdaptersHandler() {
        return new DefStyleHandler();
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
        DefStyleHandler.InflateResult defStyle = mDefStyleHandler.onCreateView(attrs, view);
        mColorCallbackHandler.onCreateView(attrs, view, defStyle.attr, defStyle.res);
        mAttrCallbackHandler.onCreateView(context, attrs, view, defStyle.attr, defStyle.res);
    }

    public synchronized void addView(@NonNull View view) {
        mColorCallbackHandler.onAddView(view);
    }
}
