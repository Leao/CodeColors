package io.leao.codecolors.drawable;

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
import android.support.v7.widget.TintManager;
import android.view.View;

import io.leao.codecolors.tint.CcTintManager;
import io.leao.codecolors.res.CcColorStateList;

class CcAppCompatDrawableWrapper extends CcDrawableWrapper {

    private boolean mCheckContext = true;

    private ColorStateList mTintableBackgroundViewTintList;

    public CcAppCompatDrawableWrapper(CcDrawableWrapper.CcConstantState state, Drawable drawable) {
        super(state, drawable);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        checkContext();
        super.draw(canvas);
    }

    private void checkContext() {
        if (mCheckContext) {
            mCheckContext = false;

            Context context = getContext();

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
        if (mTintableBackgroundViewTintList != null) {
            TintableBackgroundView view = getTintableBackgroundView();
            if (view != null && ((View) view).getBackground() == this) {
                ColorStateList viewTintList = view.getSupportBackgroundTintList();
                if (viewTintList == null) {
                    view.setSupportBackgroundTintList(mTintableBackgroundViewTintList);
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
        Context context = getContext();
        if (context != null) {
            Context contextWrapper = new ContextWrapper(context);
            TintManager tintManager = TintManager.get(contextWrapper);
            ColorStateList tintList = tintManager.getTintList(mState.mId);

            TintableBackgroundView view = getTintableBackgroundView();
            if (tintList != null && view != null && ((View) view).getBackground() == this) {
                ColorStateList viewTintList = view.getSupportBackgroundTintList();
                if (viewTintList == null || viewTintList == mTintableBackgroundViewTintList) {
                    view.setSupportBackgroundTintList(tintList);
                }
                mTintableBackgroundViewTintList = tintList;
            } else {
                CcTintManager.tintDrawable(context, tintManager, tintList, this, mState.mId);
            }
        } else {
            super.invalidateColor(color);
        }
    }

    private TintableBackgroundView getTintableBackgroundView() {
        return getTintableBackgroundView(this);
    }

    private TintableBackgroundView getTintableBackgroundView(Drawable drawable) {
        Drawable.Callback callback = drawable.getCallback();
        if (callback instanceof TintableBackgroundView) {
            return (TintableBackgroundView) callback;
        } else if (callback instanceof Drawable) {
            return getTintableBackgroundView((Drawable) callback);
        } else {
            return null;
        }
    }

    private Context getContext() {
        return getContext(this);
    }

    private Context getContext(Drawable drawable) {
        Drawable.Callback callback = drawable.getCallback();
        if (callback instanceof View) {
            return ((View) callback).getContext();
        } else if (callback instanceof Drawable) {
            return getContext((Drawable) callback);
        } else {
            return null;
        }
    }

    static class CcAppCompatConstantState extends CcConstantState {
        private int[] mAttrs;

        public CcAppCompatConstantState(Resources res, int id, int[] attrs) {
            super(res, id);
            mAttrs = attrs;
        }

        public CcAppCompatConstantState(CcAppCompatConstantState orig, ConstantState newDrawableState) {
            super(orig, newDrawableState);
            mAttrs = orig.mAttrs;
        }

        @Override
        protected CcAppCompatConstantState createState(ConstantState drawableState) {
            return new CcAppCompatConstantState(this, drawableState);
        }

        @Override
        protected CcAppCompatDrawableWrapper createDrawable(Drawable drawable) {
            return new CcAppCompatDrawableWrapper(this, drawable);
        }
    }
}
