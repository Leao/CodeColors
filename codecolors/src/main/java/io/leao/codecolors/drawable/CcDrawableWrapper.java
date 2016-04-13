package io.leao.codecolors.drawable;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.TypedValue;

import java.util.HashSet;
import java.util.Set;

import io.leao.codecolors.CodeColors;
import io.leao.codecolors.callback.CcInvalidateDrawableCallback;
import io.leao.codecolors.manager.CcDependenciesManager;
import io.leao.codecolors.res.CcColorStateList;
import io.leao.codecolors.res.CcResources;

class CcDrawableWrapper extends InsetDrawable implements CcColorStateList.Callback {
    CcConstantState mState;
    Drawable mDrawable;

    public CcDrawableWrapper(CcConstantState state, Drawable drawable) {
        super(drawable, 0);
        mState = state;
        mDrawable = drawable;
    }

    @Override
    public ConstantState getConstantState() {
        if (mState.canConstantState()) {
            mState.mChangingConfigurations = getChangingConfigurations();
            return mState;
        }
        return null;
    }

    @NonNull
    @Override
    public Drawable mutate() {
        if (super.mutate() == this) {
            ConstantState mutatedState = mDrawable.getConstantState();
            if (mState.mDrawableState != mutatedState) {
                mState = mState.createState(mutatedState);
            }
        }
        return this;
    }

    @Override
    public void applyTheme(Resources.Theme t) {
        super.applyTheme(t);

        if (mState.mUnresolvedAttrs.size() > 0) {
            if (mState.mThemeIds.size() > 0) {
                // Remove callbacks from old theme dependencies.
                for (Integer id : mState.mThemeIds) {
                    CcColorStateList color = CodeColors.getColor(id);
                    if (color != null) {
                        color.removeCallback(this);
                    }
                }
                // Clear old theme ids.
                mState.mThemeIds.clear();
            }

            CcDependenciesManager.getInstance()
                    .resolveDependencies(t, mState.mResources, mState.mUnresolvedAttrs, mState.mThemeIds);
            for (Integer id : mState.mThemeIds) {
                CcColorStateList color = CodeColors.getColor(id);
                if (color != null) {
                    mState.mThemeIds.add(id);

                    color.addCallback(this);
                }
            }
        }
    }

    @Override
    public void invalidateColor(CcColorStateList color) {
        CcInvalidateDrawableCallback.invalidate(this);
    }

    static class CcConstantState extends ConstantState {
        Resources mResources;
        int mId;
        Set<Integer> mResolvedIds;
        Set<Integer> mUnresolvedAttrs;
        Set<Integer> mThemeIds; // Resolved mUnresolvedAttrs. Not shared between constant states.

        ConstantState mDrawableState;

        int mChangingConfigurations;

        public CcConstantState(Resources res, int id) {
            mResources = res;
            mId = id;

            mResolvedIds = new HashSet<>();
            mResolvedIds.add(id); // Add own id, as it has dependencies.
            mUnresolvedAttrs = new HashSet<>();
            // Get dependencies. Cannot resolve them right away, because the Theme is not yet available.
            CcDependenciesManager.getInstance().getDependencies(res, id, mResolvedIds, mUnresolvedAttrs);
            // Minimum size as large as mUnresolvedAttrs, but it could be larger, due to other dependencies.
            mThemeIds = new HashSet<>(mUnresolvedAttrs.size());

            TypedValue value = new TypedValue();
            res.getValue(mId, value, true);
            mDrawableState = CcResources.loadDrawableForCookie(res, value, mId, null).getConstantState();
        }

        public CcConstantState(CcConstantState orig, ConstantState drawableState) {
            mResources = orig.mResources;
            mId = orig.mId;
            mResolvedIds = orig.mResolvedIds;
            mUnresolvedAttrs = orig.mUnresolvedAttrs;
            mThemeIds = new HashSet<>(orig.mThemeIds);
            mChangingConfigurations = orig.mChangingConfigurations;
            mDrawableState = drawableState;
        }

        @Override
        public Drawable newDrawable() {
            return newDrawable(null);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return newDrawableInternal(mDrawableState.newDrawable(res));
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public Drawable newDrawable(Resources res, Resources.Theme theme) {
            Drawable drawable = newDrawableInternal(mDrawableState.newDrawable(res, theme));
            if (theme != null) {
                drawable.applyTheme(theme);
            }
            return drawable;
        }

        protected Drawable newDrawableInternal(Drawable baseDrawable) {
            // Some drawables keep changing their "constant" state.
            // Make sure to also change our state when that happens.
            ConstantState baseDrawableState = baseDrawable.getConstantState();
            CcConstantState state;
            if (mDrawableState == baseDrawableState) {
                state = this;
            } else {
                state = createState(baseDrawableState);
            }

            CcDrawableWrapper drawable = state.createDrawable(baseDrawable);

            addCallbacks(mResolvedIds, drawable);
            addCallbacks(mThemeIds, drawable);

            return drawable;
        }

        protected CcConstantState createState(ConstantState drawableState) {
            return new CcConstantState(this, drawableState);
        }

        protected CcDrawableWrapper createDrawable(Drawable drawable) {
            return new CcDrawableWrapper(this, drawable);
        }

        protected void addCallbacks(Set<Integer> dependencies, CcDrawableWrapper drawable) {
            for (int dependencyId : dependencies) {
                CcColorStateList color = CodeColors.getColor(dependencyId);
                if (color != null) {
                    color.addCallback(drawable);
                }
            }
        }

        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations
                    | (mDrawableState != null ? mDrawableState.getChangingConfigurations() : 0);
        }

        public boolean canConstantState() {
            return mDrawableState != null;
        }
    }
}
