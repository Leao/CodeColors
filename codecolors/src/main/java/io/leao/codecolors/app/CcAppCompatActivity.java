package io.leao.codecolors.app;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.view.LayoutInflaterFactory;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import io.leao.codecolors.view.CcLayoutInflater;

public class CcAppCompatActivity extends AppCompatActivity {
    private LayoutInflater mLayoutInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        installAppCompatViewFactory();
        super.onCreate(savedInstanceState);
    }

    protected void installAppCompatViewFactory() {
        // Force app compat view factory installation.
        // The AppCompat library will still log an error installing the factory, but it will work.
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        LayoutInflaterCompat.setFactory(layoutInflater, (LayoutInflaterFactory) getDelegate());
    }

    @Override
    public Object getSystemService(@NonNull String name) {
        Object systemService = super.getSystemService(name);

        if (LAYOUT_INFLATER_SERVICE.equals(name)) {
            if (mLayoutInflater == null) {
                LayoutInflater layoutInflater = (LayoutInflater) systemService;
                // Copy the existing layout inflater and clone it to this context.
                // That also allows its factory to be reset.
                mLayoutInflater = CcLayoutInflater.copy(layoutInflater).cloneInContext(this);
            }
            return mLayoutInflater;
        } else {
            return systemService;
        }
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }
}
