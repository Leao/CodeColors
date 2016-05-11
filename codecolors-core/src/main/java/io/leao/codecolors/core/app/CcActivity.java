package io.leao.codecolors.core.app;

import android.app.Activity;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;

import io.leao.codecolors.CodeColors;
import io.leao.codecolors.core.CcCore;
import io.leao.codecolors.core.view.CcLayoutInflater;

public class CcActivity extends Activity {
    private CcLayoutInflater mLayoutInflater;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        CcCore.getConfigurationManager().onConfigurationChanged(getResources());
    }

    @Override
    public Object getSystemService(@NonNull String name) {
        Object systemService = super.getSystemService(name);

        if (CodeColors.isActive() && LAYOUT_INFLATER_SERVICE.equals(name)) {
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
