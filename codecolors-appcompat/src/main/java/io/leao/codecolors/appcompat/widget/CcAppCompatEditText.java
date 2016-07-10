package io.leao.codecolors.appcompat.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.View;

import io.leao.codecolors.core.widget.CcDrawableCallbackContextWrapper;

/**
 * Same as its super class, but wraps its {@link Context} with a {@link CcDrawableCallbackContextWrapper} to control the
 * creation of interesting {@link Drawable}s and {@link android.view.View}s, and being able to properly invalidate them
 * when code-colors are updated.
 */
public class CcAppCompatEditText extends AppCompatEditText
        implements CcDrawableCallbackContextWrapper.CcDrawableCallbackContextWrapperHost {
    private CcDrawableCallbackContextWrapper mContextWrapper;

    public CcAppCompatEditText(Context context) {
        super(CcDrawableCallbackContextWrapper.wrap(context));
        ensureContextWrapper();
    }

    public CcAppCompatEditText(Context context, AttributeSet attrs) {
        super(CcDrawableCallbackContextWrapper.wrap(context), attrs);
        ensureContextWrapper();
    }

    public CcAppCompatEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(CcDrawableCallbackContextWrapper.wrap(context), attrs, defStyleAttr);
        ensureContextWrapper();
    }

    private void ensureContextWrapper() {
        if (mContextWrapper == null) {
            mContextWrapper = CcDrawableCallbackContextWrapper.init(getContext(), this);
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

    @Override
    public boolean onAddDrawableCallbackView(View view) {
        return view.getClass().getSimpleName().contains("HandleView");
    }
}
