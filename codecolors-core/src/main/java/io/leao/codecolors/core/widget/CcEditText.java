package io.leao.codecolors.core.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Same as its super class, but wraps its {@link Context} with a {@link CcDrawableCallbackContextWrapper} to control the
 * creation of interesting {@link Drawable}s and {@link android.view.View}s, and being able to properly invalidate them
 * when code-colors are updated.
 */
public class CcEditText extends EditText {
    private CcDrawableCallbackContextWrapper mContextWrapper;

    public CcEditText(Context context) {
        super(CcDrawableCallbackContextWrapper.wrap(context));
        ensureContextWrapper();
    }

    public CcEditText(Context context, AttributeSet attrs) {
        super(CcDrawableCallbackContextWrapper.wrap(context), attrs);
        ensureContextWrapper();
    }

    public CcEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(CcDrawableCallbackContextWrapper.wrap(context), attrs, defStyleAttr);
        ensureContextWrapper();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CcEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(CcDrawableCallbackContextWrapper.wrap(context), attrs, defStyleAttr, defStyleRes);
        ensureContextWrapper();
    }

    private void ensureContextWrapper() {
        if (mContextWrapper == null) {
            mContextWrapper = CcDrawableCallbackContextWrapper.init(this);
        }
    }

    private CcDrawableCallbackContextWrapper getContextWrapper() {
        ensureContextWrapper();
        return mContextWrapper;
    }

    @Override
    public void invalidateDrawable(Drawable drawable) {
        if (verifyDrawable(drawable)) {
            super.invalidateDrawable(drawable);
        } else {
            getContextWrapper().invalidateDrawable(drawable);
        }
    }
}
