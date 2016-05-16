package io.leao.codecolors.core.drawable;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

public class CcAppCompatDrawableUtils {
    public static Context getContext(View view) {
        return view != null ? view.getContext() : null;
    }

    public static View getView(Drawable rootDrawable) {
        Drawable.Callback callback = rootDrawable.getCallback();
        return callback instanceof View ? (View) callback : null;
    }

    public static Drawable getRootDrawable(Drawable drawable) {
        Drawable.Callback callback = drawable.getCallback();
        if (callback instanceof Drawable) {
            return getRootDrawable((Drawable) callback);
        } else {
            return drawable;
        }
    }
}
