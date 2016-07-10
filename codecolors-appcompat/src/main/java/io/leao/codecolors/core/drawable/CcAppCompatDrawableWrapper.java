package io.leao.codecolors.core.drawable;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.TintableBackgroundView;
import android.support.v7.widget.AppCompatDrawableManager;
import android.view.View;

import java.util.HashSet;
import java.util.Set;

import io.leao.codecolors.appcompat.tint.CcTintManager;
import io.leao.codecolors.core.CcCore;
import io.leao.codecolors.core.color.CcColorStateList;
import io.leao.codecolors.core.color.CodeColor;

import static io.leao.codecolors.core.drawable.CcDrawableUtils.getContext;
import static io.leao.codecolors.core.drawable.CcDrawableUtils.getRootDrawable;
import static io.leao.codecolors.core.drawable.CcDrawableUtils.getView;

public class CcAppCompatDrawableWrapper extends CcDrawableWrapper {
    private boolean mUpdateTintList;
    private ColorStateList mTintList;

    public CcAppCompatDrawableWrapper(CcAppCompatDrawableWrapper.CcConstantState state, Drawable drawable) {
        super(state, drawable);
        mUpdateTintList = true;
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
    protected boolean onInvalidateColor(CodeColor color) {
        boolean isTintColor = isTintColor(color);
        if (isTintColor) {
            mUpdateTintList = true;
        }

        boolean invalidated = super.onInvalidateColor(color);

        return invalidateDrawable(invalidated, isTintColor);
    }

    @Override
    protected <T extends CodeColor> boolean onInvalidateColors(Set<T> colors) {
        boolean hasTintColors = hasTintColors(colors);
        if (hasTintColors) {
            mUpdateTintList = true;
        }

        boolean invalidated = super.onInvalidateColors(colors);

        return invalidateDrawable(invalidated, hasTintColors);
    }

    private boolean invalidateDrawable(boolean superInvalidated, boolean hasTintColors) {
        /*
         * Make sure the drawable is properly invalidated:
         * - if it was invalidated by super, to nothing;
         * - if it wasn't invalidated by super, but has tint colors, invalidate it now.
         */
        if (superInvalidated) {
            return true;
        } else if (hasTintColors) {
            // Invalidate drawable if it is a tint color and was not invalidated by super.
            invalidateDrawable(mDrawable);
            return true;
        } else {
            return false;
        }
    }

    protected <T extends CodeColor> boolean hasTintColors(Set<T> colors) {
        for (CodeColor color : colors) {
            if (isTintColor(color)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isTintColor(CodeColor color) {
        int colorId = color.getId();
        return colorId != CodeColor.NO_ID &&
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
        public void resolveAttributes(Resources.Theme theme) {
            super.resolveAttributes(theme);

            if (mTintIds.size() > 0) {
                // Clear old tint ids.
                mTintIds.clear();
            }

            TypedArray ta = theme.obtainStyledAttributes(mAttrs);
            try {
                int N = ta.getIndexCount();
                for (int i = 0; i < N; i++) {
                    int index = ta.getIndex(i);
                    int id = ta.getResourceId(index, 0);
                    // If the color exists, adds this as callback, and the id as a valid tint id.
                    CcColorStateList color = CcCore.getColorManager().getColor(id);
                    if (color != null) {
                        mTintIds.add(id);
                    }
                }
            } finally {
                ta.recycle();
            }
        }

        @Override
        protected void onAddCallbacks(Activity activity, CcDrawableWrapper drawable) {
            super.onAddCallbacks(activity, drawable);
            addCallbacks(mTintIds, activity, drawable);
        }
    }
}
