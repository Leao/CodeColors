package io.leao.codecolors.core.drawable;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.view.TintableBackgroundView;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v4.widget.TintableCompoundButton;
import android.support.v7.widget.AppCompatDrawableManager;
import android.view.View;
import android.widget.CompoundButton;

import io.leao.codecolors.appcompat.tint.CcTintManager;
import io.leao.codecolors.core.res.CcColorStateList;

public class CcAppCompatDrawableWrapper extends CcDrawableWrapper {
    private boolean mCheckContext = true;

    private ColorStateList mTintList;

    public CcAppCompatDrawableWrapper(CcDrawableWrapper.CcConstantState state, Drawable drawable) {
        super(state, drawable);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        checkContext();
        super.draw(canvas);
    }

    @Override
    protected boolean onStateChange(int[] state) {
        boolean changed = super.onStateChange(state);

        // Update tint list if it's not directly in the workflow of an AppCompat tintable view.
        Drawable rootDrawable = getRootDrawable();
        View view = getView(rootDrawable);
        Context context = getContext(view);
        if (context != null) {
            if (!isTintableBackground(view, rootDrawable) && !isTintableCompoundButton(view, rootDrawable)) {
                updateAppCompatTint(context, view, rootDrawable);
            }
        }

        return changed;
    }

    private void checkContext() {
        if (mCheckContext) {
            mCheckContext = false;

            Context context = getContext(getView(getRootDrawable()));
            if (context != null) {
                TypedArray ta = context.obtainStyledAttributes(((CcAppCompatConstantState) mState).mAttrs);
                try {
                    int N = ta.getIndexCount();
                    for (int i = 0; i < N; i++) {
                        int index = ta.getIndex(i);
                        ColorStateList color = ta.getColorStateList(index);
                        if (color instanceof CcColorStateList) {
                            ((CcColorStateList) color).addCallback(this);
                        }
                    }
                } finally {
                    ta.recycle();
                }
            }
        }
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        // Check if we should restore our inner tintList.
        if (mTintList != null) {
            Drawable rootDrawable = getRootDrawable();
            View view = getView(rootDrawable);

            if (isTintableBackground(view, rootDrawable)) {
                ColorStateList tintList = ((TintableBackgroundView) view).getSupportBackgroundTintList();
                if (tintList == null) {
                    ((TintableBackgroundView) view).setSupportBackgroundTintList(mTintList);
                    return;
                }
            } else if (isTintableCompoundButton(view, rootDrawable)) {
                ColorStateList tintList = ((TintableCompoundButton) view).getSupportButtonTintList();
                if (tintList == null) {
                    ((TintableCompoundButton) view).setSupportButtonTintList(mTintList);
                    return;
                }
            }
        }

        // We are not restoring our inner tintList.
        // Apply the color filter normally.
        super.setColorFilter(colorFilter);
    }

    @Override
    public void invalidateColor(CcColorStateList color) {
        Drawable rootDrawable = getRootDrawable();
        View view = getView(rootDrawable);
        Context context = getContext(view);
        if (context != null) {
            updateAppCompatTint(context, view, rootDrawable);
        } else {
            super.invalidateColor(color);
        }
    }

    protected void updateAppCompatTint(Context context, View view, Drawable rootDrawable) {
        AppCompatDrawableManager drawableManager = AppCompatDrawableManager.get();
        // The context wrapper ensures that the manager doesn't reuse old tint lists.
        Context contextWrapper = new ContextWrapper(context);
        ColorStateList tintList = drawableManager.getTintList(contextWrapper, mState.mId);

        if (tintList != null && isTintableBackground(view, rootDrawable)) {
            ColorStateList viewTintList = ((TintableBackgroundView) view).getSupportBackgroundTintList();
            if (viewTintList == null || viewTintList == mTintList) {
                ((TintableBackgroundView) view).setSupportBackgroundTintList(tintList);
            }
            mTintList = tintList;
        } else if (tintList != null && isTintableCompoundButton(view, rootDrawable)) {
            ColorStateList buttonTintList = ((TintableCompoundButton) view).getSupportButtonTintList();
            if (buttonTintList == null || buttonTintList == mTintList) {
                ((TintableCompoundButton) view).setSupportButtonTintList(tintList);
            }
            mTintList = tintList;
        } else {
            // If tint list is null, tintDrawable will still try to set color filters.
            CcTintManager.tintDrawable(context, tintList, mDrawable, mState.mId);
        }
    }

    private Context getContext(View view) {
        return view != null ? view.getContext() : null;
    }

    private View getView(Drawable rootDrawable) {
        Drawable.Callback callback = rootDrawable.getCallback();
        return callback instanceof View ? (View) callback : null;
    }

    private Drawable getRootDrawable() {
        return getRootDrawable(this);
    }

    private Drawable getRootDrawable(Drawable drawable) {
        Drawable.Callback callback = drawable.getCallback();
        if (callback instanceof Drawable) {
            return getRootDrawable((Drawable) callback);
        } else {
            return drawable;
        }
    }

    private static boolean isTintableBackground(View view, Drawable rootDrawable) {
        return view instanceof TintableBackgroundView &&
                view.getBackground() == rootDrawable;
    }

    private static boolean isTintableCompoundButton(View view, Drawable rootDrawable) {
        return view instanceof TintableCompoundButton &&
                CompoundButtonCompat.getButtonDrawable((CompoundButton) view) == rootDrawable;
    }

    static class CcAppCompatConstantState extends CcConstantState {
        private int[] mAttrs;

        public CcAppCompatConstantState(Resources res, int id, int[] attrs) {
            super(res, id);
            init(attrs);
        }

        public CcAppCompatConstantState(Resources res, int id, ConstantState baseConstantState, int[] attrs) {
            super(res, id, baseConstantState);
            init(attrs);
        }

        public CcAppCompatConstantState(CcAppCompatConstantState orig, ConstantState newConstantState) {
            super(orig, newConstantState);
            init(orig.mAttrs);
        }

        private void init(int[] attrs) {
            mAttrs = attrs;
        }

        @Override
        protected CcAppCompatConstantState createState(ConstantState constantState) {
            return new CcAppCompatConstantState(this, constantState);
        }

        @Override
        protected CcAppCompatDrawableWrapper createDrawable(Drawable drawable) {
            return new CcAppCompatDrawableWrapper(this, drawable);
        }
    }
}
