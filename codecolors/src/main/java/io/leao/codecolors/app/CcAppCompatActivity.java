package io.leao.codecolors.app;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;

import io.leao.codecolors.manager.CcConfigurationManager;
import io.leao.codecolors.view.CcLayoutInflater;

public class CcAppCompatActivity extends AppCompatActivity {
    private CcLayoutInflater mLayoutInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflater.from(this); // Makes sure mLayoutInflater is initialized.
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        CcConfigurationManager.getInstance().onConfigurationChanged(getResources());
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
}
