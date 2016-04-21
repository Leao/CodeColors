package io.leao.codecolors.appcompat.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import io.leao.codecolors.R;

/**
 * Same as {@link AppCompatEditText}, but sets itself as the cursor drawable callback.
 * <p>
 * That way, the cursor drawable will be able to update its tint when the code colors change their color.
 */
public class CcAppCompatEditText extends AppCompatEditText {

    public CcAppCompatEditText(Context context) {
        super(CursorContextWrapper.wrap(context));
        init();
    }

    public CcAppCompatEditText(Context context, AttributeSet attrs) {
        super(CursorContextWrapper.wrap(context), attrs);
        init();
    }

    public CcAppCompatEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(CursorContextWrapper.wrap(context), attrs, defStyleAttr);
        init();
    }

    private void init() {
        CursorContextWrapper context = getCursorContextWrapper(getContext());
        if (context != null) {
            context.getResources().registerView(this);
        }
    }

    private CursorContextWrapper getCursorContextWrapper(Context context) {
        if (context instanceof CursorContextWrapper) {
            return (CursorContextWrapper) context;
        } else if (context instanceof ContextWrapper) {
            return getCursorContextWrapper(((ContextWrapper) context).getBaseContext());
        } else {
            return null;
        }
    }

    /**
     * We wrap the {@link android.widget.EditText}'s {@link Context}, in order to intercept the cursor drawable creation
     * and set the {@link android.widget.EditText} as its callback.
     * <p/>
     * By setting the {@link EditText} as the drawable's callback, the
     * {@link io.leao.codecolors.core.drawable.CcAppCompatDrawableWrapper} will be able to update the drawables' tint when
     * the code colors change their color.
     */
    private static class CursorContextWrapper extends ContextWrapper {

        public static Context wrap(Context context) {
            if (!(context instanceof CursorContextWrapper)) {
                context = new CursorContextWrapper(context);
            }
            return context;
        }

        private CursorResourcesWrapper mResources;

        private CursorContextWrapper(Context base) {
            super(base);
        }

        @Override
        public CursorResourcesWrapper getResources() {
            if (mResources == null) {
                mResources = new CursorResourcesWrapper(super.getResources());
            }
            return mResources;
        }

        private static class CursorResourcesWrapper extends ResourcesWrapper {
            private List<Drawable> mCursorDrawables;

            private EditText mEditText;

            public CursorResourcesWrapper(Resources resources) {
                super(resources);
            }

            @SuppressLint("PrivateResource")
            @Override
            public Drawable getDrawable(int id) throws NotFoundException {
                Drawable drawable = super.getDrawable(id);
                if (drawable != null && id == R.drawable.abc_text_cursor_material) {
                    if (mEditText == null) {
                        if (mCursorDrawables == null) {
                            mCursorDrawables = new ArrayList<>(2);
                        }
                        mCursorDrawables.add(drawable);
                    } else {
                        drawable.setCallback(mEditText);
                    }
                }
                return drawable;
            }

            public void registerView(EditText editText) {
                mEditText = editText;
                if (mCursorDrawables != null) {
                    for (Drawable drawable : mCursorDrawables) {
                        drawable.setCallback(mEditText);
                    }
                    mCursorDrawables.clear();
                }
            }
        }
    }
}
