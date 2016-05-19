package io.leao.codecolors.core.drawable;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.TintableBackgroundView;
import android.support.v7.widget.AppCompatDrawableManager;
import android.view.View;

import java.util.HashSet;
import java.util.Set;

import io.leao.codecolors.appcompat.tint.CcTintManager;
import io.leao.codecolors.core.color.CcColorStateList;

import static io.leao.codecolors.core.drawable.CcAppCompatDrawableUtils.getContext;
import static io.leao.codecolors.core.drawable.CcAppCompatDrawableUtils.getRootDrawable;
import static io.leao.codecolors.core.drawable.CcAppCompatDrawableUtils.getView;

public class CcAppCompatDrawableWrapper extends CcDrawableWrapper {
    private boolean mCheckTheme = true;

    private boolean mUpdateTintList;
    private ColorStateList mTintList;

    public CcAppCompatDrawableWrapper(CcAppCompatDrawableWrapper.CcConstantState state, Drawable drawable) {
        super(state, drawable);
        mUpdateTintList = true;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        checkTheme();
        super.draw(canvas);
    }

    private void checkTheme() {
        if (mCheckTheme) {
            mCheckTheme = false;

            Context context = getContext(getView(getRootDrawable(this)));
            if (context != null) {
                applyTheme(context.getTheme());
            }
        }
    }

    @Override
    public void applyTheme(@NonNull Resources.Theme t) {
        mCheckTheme = false;
        super.applyTheme(t);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        // Check if we should enforce our inner tintList.
        if (mTintList != null) {
            Drawable rootDrawable = getRootDrawable(this);
            View view = getView(rootDrawable);

            if (isTintableBackground(view, rootDrawable)) {
                // TintableBackgroundViews store internal tintLists for known drawables.
                // Those tintLists can become outdated, if they contain attributes that are code-colors.
                // We make sure to store and enforce different tintList every time the code-colors change.
                ColorStateList tintList = ((TintableBackgroundView) view).getSupportBackgroundTintList();
                if (tintList == null) {
                    ((TintableBackgroundView) view).setSupportBackgroundTintList(mTintList);
                    return;
                }
            }
        }

        // We are not restoring our inner tintList.
        // Apply the color filter normally.
        super.setColorFilter(colorFilter);
    }

    @Override
    protected boolean onInvalidateColor(CcColorStateList color) {
        boolean isTintColor = isTintColor(color);
        if (isTintColor) {
            mUpdateTintList = true;
        }

        boolean invalidated = super.onInvalidateColor(color);

        /*
         * Make sure the drawable is properly invalidated.
         */
        if (invalidated) {
            return true;
        } else if (isTintColor) {
            // Invalidate drawable if it is a tint color and was not invalidated by super.
            invalidateDrawable(mDrawable);
            return true;
        } else {
            return false;
        }
    }

    protected boolean isTintColor(CcColorStateList color) {
        int colorId = color.getId();
        return colorId != CcColorStateList.NO_ID &&
                ((CcAppCompatConstantState) mState).mTintIds.contains(color.getId());
    }

    @Override
    public void invalidateDrawable(Drawable who) {
        updateTintList();
        super.invalidateDrawable(who);
    }

    protected void updateTintList() {
        if (mUpdateTintList) {
            mUpdateTintList = false;

            Drawable rootDrawable = getRootDrawable(this);
            View view = getView(rootDrawable);
            Context context = getContext(view);
            if (context != null) {
                AppCompatDrawableManager drawableManager = AppCompatDrawableManager.get();
                // The context wrapper ensures that the manager doesn't reuse old tint lists.
                Context contextWrapper = new ContextWrapper(context);
                ColorStateList tintList = drawableManager.getTintList(contextWrapper, mState.mId);

                if (tintList != null && isTintableBackground(view, rootDrawable)) {
                    ColorStateList viewTintList = ((TintableBackgroundView) view).getSupportBackgroundTintList();
                    // TintableBackgroundViews store internal tintLists for known drawables.
                    // Those tintLists can become outdated, if they contain attributes that are code-colors.
                    // We make sure to store and enforce different tintList every time the code-colors change.
                    if (viewTintList == null || viewTintList == mTintList) {
                        ((TintableBackgroundView) view).setSupportBackgroundTintList(tintList);
                    }
                    mTintList = tintList;
                } else {
                    // If tint list is null, tintDrawable will still try to set color filters.
                    CcTintManager.tintDrawable(context, tintList, mDrawable, mState.mId);
                }
            }
        }
    }

    private static boolean isTintableBackground(View view, Drawable rootDrawable) {
        return view instanceof TintableBackgroundView && view.getBackground() == rootDrawable;
    }

    static class CcAppCompatConstantState extends CcConstantState {
        private int[] mAttrs;
        private Set<Integer> mTintIds;

        public CcAppCompatConstantState(Resources res, int id, int[] attrs) {
            super(res, id);
            init(attrs, new HashSet<Integer>());
        }

        public CcAppCompatConstantState(Resources res, int id, ConstantState baseConstantState, int[] attrs) {
            super(res, id, baseConstantState);
            init(attrs, new HashSet<Integer>());
        }

        public CcAppCompatConstantState(CcAppCompatConstantState orig, ConstantState newConstantState) {
            super(orig, newConstantState);
            init(orig.mAttrs, new HashSet<>(orig.mTintIds));
        }

        private void init(int[] attrs, Set<Integer> tintIds) {
            mAttrs = attrs;
            mTintIds = tintIds;
        }

        @Override
        protected CcAppCompatConstantState createState(ConstantState constantState) {
            return new CcAppCompatConstantState(this, constantState);
        }

        @Override
        protected CcAppCompatDrawableWrapper createDrawable(Drawable drawable) {
            CcAppCompatDrawableWrapper wrapper = new CcAppCompatDrawableWrapper(this, drawable);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                // Tint attributes are always theme dependent.
                // Mutate drawable on older versions, as they didn't have theme support.
                wrapper.mutate();
            }
            return wrapper;
        }

        @Override
        protected void applyTheme(Resources.Theme t, CcDrawableWrapper drawable) {
            if (mTintIds.size() > 0) {
                // Remove callbacks from old tint dependencies.
                for (Integer id : mTintIds) {
                    removeCallback(id, drawable);
                }
                // Clear old tint ids.
                mTintIds.clear();
            }

            TypedArray ta = t.obtainStyledAttributes(mAttrs);
            try {
                int N = ta.getIndexCount();
                for (int i = 0; i < N; i++) {
                    int index = ta.getIndex(i);
                    int id = ta.getResourceId(index, 0);
                    if (addCallback(id, drawable)) {
                        mTintIds.add(id);
                    }
                }
            } finally {
                ta.recycle();
            }

            super.applyTheme(t, drawable);
        }
    }
}
