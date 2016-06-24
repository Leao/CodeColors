package io.leao.codecolors.core.drawable;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.TypedValue;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;

import io.leao.codecolors.core.CcCore;
import io.leao.codecolors.core.color.CcColorStateList;
import io.leao.codecolors.core.res.CcResources;

import static io.leao.codecolors.core.drawable.CcDrawableUtils.forceStateChange;
import static io.leao.codecolors.core.drawable.CcDrawableUtils.getActivity;
import static io.leao.codecolors.core.drawable.CcDrawableUtils.getContext;
import static io.leao.codecolors.core.drawable.CcDrawableUtils.getRootDrawable;
import static io.leao.codecolors.core.drawable.CcDrawableUtils.getView;

public class CcDrawableWrapper extends InsetDrawable implements CcColorStateList.SingleCallback {
    private boolean mFirstDraw = true;

    protected CcConstantState mState;
    /**
     * We store an Activity reference to be able to divide the {@link CcColorStateList.SingleCallback}s by different
     * activities. However, sometimes is not easy to guess the Activity to which this drawable belongs to.
     * <p>
     * When the drawable is first created, we store a reference to the last resumed Activity. This is our first guess.
     * However, some drawables are only created in the drawing pass, and sometimes their Activity was already paused and
     * a different Activity resumed.
     * <p>
     * On our first drawing pass, we will try to get an Activity based on our {@link Drawable.Callback}'s Context. If we
     * are able to get a valid Activity, we will override our first guess reference with the newly found Activity
     * reference. Otherwise, we will fallback to our first guess to add the {@link CcColorStateList.SingleCallback}s.
     * <p>
     * If, for some reason, our first guess had a null Activity referenced, and we couldn't get a valid Activity based
     * on our {@link Drawable.Callback}, we will make use of the last resumed Activity on the first drawing pass, to add
     * the {@link CcColorStateList.SingleCallback}s. That is what happens when we pass null to
     * {@link CcColorStateList#addCallback(Activity, CcColorStateList.SingleCallback)}.
     */
    protected WeakReference<Activity> mActivityRef;
    protected Drawable mDrawable;

    private Set<Drawable> mPreparedDrawables;
    private Drawable mPreparingDrawable;

    public CcDrawableWrapper(CcConstantState state, Drawable drawable) {
        super(drawable, 0);
        mState = state;
        mActivityRef = CcCore.getActivityManager().getLastResumedActivityReference();
        mDrawable = drawable;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (mFirstDraw) {
            mFirstDraw = false;
            onFirstDraw();
        }
        super.draw(canvas);
    }

    protected void onFirstDraw() {
        Context context = getContext(getView(getRootDrawable(this)));

        // Backwards compatibility for Themes to resolve attributes.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (context != null) {
                applyThemeInternal(context.getTheme());
            }
        }

        // Get the Activity to which this drawable belongs, based on its Drawable.Callback.
        Activity activity = getActivity(context);
        if (activity != null) {
            mActivityRef = CcCore.getActivityManager().getActivityReference(activity);
        }
        mState.onAddCallbacks(mActivityRef.get(), this);
    }

    /**
     * @return the wrapped drawable
     */
    public Drawable getDrawable() {
        return mDrawable;
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
            if (mState.mBaseConstantState != mutatedState) {
                mState = mState.createState(mutatedState);
            }
        }
        return this;
    }

    @Override
    public void applyTheme(@NonNull Resources.Theme t) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.applyTheme(t);
        }
        applyThemeInternal(t);
    }

    protected void applyThemeInternal(Resources.Theme theme) {
        mState.resolveAttributes(theme);
    }

    @Override
    public void invalidateColor(CcColorStateList color) {
        onInvalidateColor(color);
    }

    /**
     * @return true, if the drawable was invalidated; false, otherwise.
     */
    protected boolean onInvalidateColor(CcColorStateList color) {
        if (isDependencyColor(color)) {
            invalidateDrawable();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void invalidateColors(Set<CcColorStateList> colors) {
        onInvalidateColors(colors);
    }

    /**
     * @return true, if the drawable was invalidated; false, otherwise.
     */
    protected boolean onInvalidateColors(Set<CcColorStateList> colors) {
        if (hasDependencyColors(colors)) {
            invalidateDrawable();
            return true;
        } else {
            return false;
        }
    }

    protected boolean isDependencyColor(CcColorStateList color) {
        int colorId = color.getId();
        return colorId != CcColorStateList.NO_ID &&
                (mState.mResolvedIds.contains(colorId) || mState.mThemeIds.contains(colorId));
    }

    protected boolean hasDependencyColors(Set<CcColorStateList> colors) {
        for (CcColorStateList color : colors) {
            if (isDependencyColor(color)) {
                return true;
            }
        }
        return false;
    }

    private void invalidateDrawable() {
        // Drawables' color could have changed.
        // Make sure the set of prepared drawables is empty.
        if (mPreparedDrawables == null) {
            mPreparedDrawables = Collections.newSetFromMap(new IdentityHashMap<Drawable, Boolean>());
        } else {
            mPreparedDrawables.clear();
        }

        // Invalidate drawable changing its color and updating the view.
        invalidateDrawable(mDrawable);
    }

    @Override
    public void invalidateDrawable(Drawable who) {
        // If a code color was invalidated, mPreparedDrawables != null.
        // It means that we started tracking the drawables that are prepared, or not, forcing a state change, to make
        // sure their appearance is correct.
        if (mPreparedDrawables != null) {
            if (mPreparingDrawable == who) {
                // Do not propagate drawable invalidation twice.
                return;
            }

            Drawable current = getCurrent(who);
            if (!mPreparedDrawables.contains(current)) {
                mPreparedDrawables.add(current);

                // Store the drawable to which we are forcing a state change, to prevent invalidateSelf() from
                // propagating its call twice to the view holding the drawable, resulting in the a double invalidation,
                // and double the time, on some cases.
                // We could set its callback to null, but setting a callback creates a new WeekReference every time,
                // which would make this slower.
                mPreparingDrawable = who;
                // Force a state change to update the color.
                forceStateChange(current, false);
                mPreparingDrawable = null;
            }
        }

        super.invalidateDrawable(who);
    }

    private Drawable getCurrent(Drawable who) {
        Drawable current = who.getCurrent();
        return current != who ? getCurrent(current) : current;
    }

    static class CcConstantState extends ConstantState {
        Resources mResources;
        int mId;
        ConstantState mBaseConstantState;

        Set<Integer> mResolvedIds;
        Set<Integer> mUnresolvedAttrs;
        Set<Integer> mThemeIds; // Resolved mUnresolvedAttrs. Not shared between constant states.

        int mChangingConfigurations;

        private static ConstantState loadBaseConstantState(Resources res, int id) {
            TypedValue value = new TypedValue();
            res.getValue(id, value, true);
            return CcResources.loadDrawableForCookie(res, value, id, null).getConstantState();
        }

        public CcConstantState(Resources res, int id) {
            this(res, id, loadBaseConstantState(res, id));
        }

        public CcConstantState(Resources res, int id, ConstantState baseConstantState) {
            mResources = res;
            mId = id;
            mBaseConstantState = baseConstantState;

            Set<Integer> resolvedIds = new HashSet<>();
            Set<Integer> unresolvedAttrs = new HashSet<>();
            // Add own id, as a possible code-color.
            resolvedIds.add(id);
            // Get dependencies. Cannot resolve them right away, because the Theme is not yet available.
            CcCore.getDependencyManager().getDependencies(res, id, resolvedIds, unresolvedAttrs);

            mResolvedIds = resolvedIds;
            mUnresolvedAttrs = unresolvedAttrs;
            mThemeIds = new HashSet<>(unresolvedAttrs.size()); // As large as mUnresolvedAttrs, but it can be larger.
        }

        public CcConstantState(CcConstantState orig, ConstantState baseConstantState) {
            mResources = orig.mResources;
            mId = orig.mId;
            mBaseConstantState = baseConstantState;
            mResolvedIds = orig.mResolvedIds;
            mUnresolvedAttrs = orig.mUnresolvedAttrs;
            mThemeIds = new HashSet<>(orig.mThemeIds);
            mChangingConfigurations = orig.mChangingConfigurations;
        }

        public Drawable.ConstantState getBaseConstantState() {
            return mBaseConstantState;
        }

        @NonNull
        public Drawable newDrawable() {
            return newDrawable(null);
        }

        @NonNull
        @Override
        public Drawable newDrawable(Resources res) {
            return newDrawableInternal(mBaseConstantState.newDrawable(res));
        }

        @NonNull
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public Drawable newDrawable(Resources res, Resources.Theme theme) {
            Drawable drawable = newDrawableInternal(mBaseConstantState.newDrawable(res, theme));
            if (theme != null) {
                drawable.applyTheme(theme);
            }
            return drawable;
        }

        protected Drawable newDrawableInternal(Drawable baseDrawable) {
            // Some drawables keep changing their "constant" state.
            // Make sure to also change our state when that happens.
            ConstantState baseConstantState = baseDrawable.getConstantState();
            CcConstantState state;
            if (mBaseConstantState == baseConstantState) {
                state = this;
            } else {
                state = createState(baseConstantState);
            }

            return state.createDrawable(baseDrawable);
        }

        protected CcConstantState createState(ConstantState baseConstantState) {
            return new CcConstantState(this, baseConstantState);
        }

        protected CcDrawableWrapper createDrawable(Drawable drawable) {
            return new CcDrawableWrapper(this, drawable);
        }

        public void resolveAttributes(Resources.Theme theme) {
            // Theme dependent resources.
            if (mUnresolvedAttrs.size() > 0) {
                if (mThemeIds.size() > 0) {
                    // Clear old theme ids.
                    mThemeIds.clear();
                }

                CcCore.getDependencyManager().resolveDependencies(theme, mResources, mUnresolvedAttrs, mThemeIds);
            }
        }

        protected void onAddCallbacks(Activity activity, CcDrawableWrapper drawable) {
            addCallbacks(mResolvedIds, activity, drawable);
            addCallbacks(mThemeIds, activity, drawable);
        }

        protected void addCallbacks(Set<Integer> dependencies, Activity activity, CcDrawableWrapper drawable) {
            Iterator<Integer> iterator = dependencies.iterator();
            while (iterator.hasNext()) {
                int dependencyId = iterator.next();
                CcColorStateList color = CcCore.getColorManager().getColor(dependencyId);
                if (color != null) {
                    color.addCallback(activity, drawable);
                } else {
                    iterator.remove();
                }
            }
        }

        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations
                    | (mBaseConstantState != null ? mBaseConstantState.getChangingConfigurations() : 0);
        }

        public boolean canConstantState() {
            return mBaseConstantState != null;
        }
    }
}
