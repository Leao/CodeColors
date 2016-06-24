package io.leao.codecolors.core.drawable;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.Set;

import io.leao.codecolors.core.CcCore;
import io.leao.codecolors.core.color.CcColorStateList;

import static io.leao.codecolors.core.drawable.CcDrawableUtils.getActivity;
import static io.leao.codecolors.core.drawable.CcDrawableUtils.getContext;
import static io.leao.codecolors.core.drawable.CcDrawableUtils.getRootDrawable;
import static io.leao.codecolors.core.drawable.CcDrawableUtils.getView;

/**
 * Inspired in {@link android.graphics.drawable.ColorDrawable}, but instead of making use of a {@code int} color, it
 * makes use of a {@link CcColorStateList}.
 */
public class CcColorDrawable extends Drawable implements CcColorStateList.SingleCallback {
    private static final int COLOR_DEFAULT = Color.BLUE;
    private static final int ALPHA_OPAQUE = 255;

    private static final PorterDuff.Mode DEFAULT_TINT_MODE = PorterDuff.Mode.SRC_IN;

    protected final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean mFirstDraw = true;

    protected CodeColorState mCodeColorState;
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

    protected int mUseColor;
    protected CodePorterDuffColorFilter mTintFilter;

    private boolean mMutated;

    /**
     * Creates a new ColorDrawable with the specified color.
     *
     * @param color The color to draw.
     */
    public CcColorDrawable(CcColorStateList color) {
        this(new CodeColorState(color));
    }

    private CcColorDrawable(CodeColorState state) {
        mCodeColorState = state;
        mActivityRef = CcCore.getActivityManager().getLastResumedActivityReference();
        updateLocalState();
    }

    /**
     * Initializes local dynamic properties from state. This should be called
     * after significant state changes, e.g. from the One True Constructor and
     * after inflating or applying a theme.
     */
    protected void updateLocalState() {
        updateUseColor(mCodeColorState.mColor, getState(), mCodeColorState.mAlpha);
        updateTintFilter(mCodeColorState.mTint, mCodeColorState.mTintMode);
    }

    /**
     * Ensures the use color is consistent with the current color state and alpha.
     *
     * @return {@code true} if use color changed; false, otherwise.
     */
    protected boolean updateUseColor(CcColorStateList color, int[] stateSet, int alpha) {
        // Color.
        int baseColor = color != null ?
                color.getColorForState(stateSet, color.getDefaultColor()) :
                COLOR_DEFAULT;
        // Alpha.
        alpha += alpha >> 7;   // make it 0..256
        int baseAlpha = baseColor >>> 24;
        int useAlpha = baseAlpha * alpha >> 8;
        int useColor = (baseColor << 8 >>> 8) | (useAlpha << 24);

        if (mUseColor != useColor) {
            mUseColor = useColor;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Ensures the tint filter is consistent with the current tint color and mode.
     *
     * @return {@code true} if tint filter changed; false, otherwise.
     */
    protected boolean updateTintFilter(ColorStateList tint, PorterDuff.Mode tintMode) {
        CodePorterDuffColorFilter tintFilter;
        if (tint == null || tintMode == null) {
            tintFilter = null;
        } else {
            final int color = tint.getColorForState(getState(), Color.TRANSPARENT);
            if (mTintFilter == null || mTintFilter.getColor() != color || mTintFilter.getMode() != tintMode) {
                tintFilter = new CodePorterDuffColorFilter(color, tintMode);
            } else {
                tintFilter = mTintFilter;
            }
        }

        if (mTintFilter != tintFilter) {
            mTintFilter = tintFilter;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (mFirstDraw) {
            mFirstDraw = false;
            onFirstDraw();
        }

        final ColorFilter colorFilter = mPaint.getColorFilter();
        if ((mUseColor >>> 24) != 0 || colorFilter != null || mTintFilter != null) {
            if (colorFilter == null) {
                mPaint.setColorFilter(mTintFilter);
            }

            mPaint.setColor(mUseColor);
            canvas.drawRect(getBounds(), mPaint);

            // Restore original color filter.
            mPaint.setColorFilter(colorFilter);
        }
    }

    protected void onFirstDraw() {
        // If we have a valid color, add this drawable as its callback.
        if (mCodeColorState.mColor != null) {
            // Get the Activity to which this drawable belongs, based on its Drawable.Callback.
            Activity activity = getActivity(getContext(getView(getRootDrawable(this))));
            if (activity != null) {
                mActivityRef = CcCore.getActivityManager().getActivityReference(activity);
            }
            mCodeColorState.mColor.addCallback(mActivityRef.get(), this);
        }
    }

    @Override
    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | mCodeColorState.getChangingConfigurations();
    }

    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            mCodeColorState = new CodeColorState(mCodeColorState);
            mMutated = true;
        }
        return this;
    }

    /**
     * Gets the drawable's color value.
     *
     * @return int The color to draw.
     */
    public CcColorStateList getColor() {
        return mCodeColorState.mColor;
    }

    /**
     * Sets the drawable's color value. This action will clobber the results of
     * prior calls to {@link #setAlpha(int)} on this object, which side-affected
     * the underlying color.
     *
     * @param color The color to draw.
     */
    public void setColor(CcColorStateList color) {
        if (mCodeColorState.mColor != color) {
            if (mCodeColorState.mColor != null) {
                mCodeColorState.mColor.removeCallback(mActivityRef.get(), this);
            }
            mCodeColorState.mColor = color;
            if (mCodeColorState.mColor != null) {
                mCodeColorState.mColor.addCallback(mActivityRef.get(), this);
            }
            if (updateUseColor(mCodeColorState.mColor, getState(), mCodeColorState.mAlpha)) {
                invalidateSelf();
            }
        }
    }

    @Override
    public void invalidateColor(CcColorStateList color) {
        if (updateUseColor(color, getState(), mCodeColorState.mAlpha)) {
            invalidateSelf();
        }
    }

    @Override
    public void invalidateColors(Set<CcColorStateList> colors) {
        // If everything works well this should never be called.
        for (CcColorStateList color : colors) {
            invalidateColor(color);
        }
    }

    /**
     * Returns the alpha value of this drawable's color.
     *
     * @return A value between 0 and 255.
     */
    @Override
    public int getAlpha() {
        return mUseColor >>> 24;
    }

    /**
     * Sets the color's alpha value.
     *
     * @param alpha The alpha value to set, between 0 and 255.
     */
    @Override
    public void setAlpha(int alpha) {
        if (mCodeColorState.mAlpha != alpha) {
            mCodeColorState.mAlpha = alpha;
            if (updateUseColor(mCodeColorState.mColor, getState(), alpha)) {
                invalidateSelf();
            }
        }
    }

    /**
     * Sets the color filter applied to this color.
     * <p>
     * Only supported on version {@link android.os.Build.VERSION_CODES#LOLLIPOP} and
     * above. Calling this method has no effect on earlier versions.
     *
     * @see android.graphics.drawable.Drawable#setColorFilter(ColorFilter)
     */
    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public void setTintList(ColorStateList tint) {
        mCodeColorState.mTint = tint;
        if (updateTintFilter(tint, mCodeColorState.mTintMode)) {
            invalidateSelf();
        }
    }

    @Override
    public void setTintMode(@NonNull PorterDuff.Mode tintMode) {
        mCodeColorState.mTintMode = tintMode;
        if (updateTintFilter(mCodeColorState.mTint, tintMode)) {
            invalidateSelf();
        }
    }

    @Override
    protected boolean onStateChange(int[] stateSet) {
        boolean changed = false;
        if (updateUseColor(mCodeColorState.mColor, stateSet, mCodeColorState.mAlpha)) {
            changed = true;
        }
        if (updateTintFilter(mCodeColorState.mTint, mCodeColorState.mTintMode)) {
            changed = true;
        }
        return changed;
    }

    @Override
    public boolean isStateful() {
        return (mCodeColorState.mColor != null && mCodeColorState.mColor.isStateful()) ||
                (mCodeColorState.mTint != null && mCodeColorState.mTint.isStateful());
    }

    @Override
    public int getOpacity() {
        if (mTintFilter != null || mPaint.getColorFilter() != null) {
            return PixelFormat.TRANSLUCENT;
        }

        switch (mUseColor >>> 24) {
            case 255:
                return PixelFormat.OPAQUE;
            case 0:
                return PixelFormat.TRANSPARENT;
        }
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void getOutline(@NonNull Outline outline) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            outline.setRect(getBounds());
            outline.setAlpha(getAlpha() / 255.0f);
        }
    }

    @Override
    public boolean canApplyTheme() {
        return false;
    }

    @Override
    public ConstantState getConstantState() {
        return mCodeColorState;
    }

    public static CodeColorState getConstantStateForColor(CcColorStateList color) {
        return new CodeColorState(color);
    }

    public static class CodeColorState extends ConstantState {
        int[] mThemeAttrs;
        CcColorStateList mColor;
        int mAlpha = ALPHA_OPAQUE;
        int mChangingConfigurations;
        ColorStateList mTint = null;
        PorterDuff.Mode mTintMode = DEFAULT_TINT_MODE;

        CodeColorState(CcColorStateList color) {
            mColor = color;
        }

        CodeColorState(CodeColorState state) {
            mColor = state.mColor;
            mAlpha = state.mAlpha;
            mThemeAttrs = state.mThemeAttrs;
            mChangingConfigurations = state.mChangingConfigurations;
            mTint = state.mTint;
            mTintMode = state.mTintMode;
        }

        @NonNull
        @Override
        public Drawable newDrawable() {
            return new CcColorDrawable(this);
        }

        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations;
        }
    }

    /**
     * Like {@link PorterDuffColorFilter}, but with getters.
     */
    private static class CodePorterDuffColorFilter extends PorterDuffColorFilter {
        private int mColor;
        private PorterDuff.Mode mMode;

        /**
         * Create a color filter that uses the specified color and Porter-Duff mode.
         *
         * @param color The ARGB source color used with the specified Porter-Duff mode
         * @param mode  The porter-duff mode that is applied
         */
        public CodePorterDuffColorFilter(int color, PorterDuff.Mode mode) {
            super(color, mode);
            mColor = color;
            mMode = mode;
        }

        public int getColor() {
            return mColor;
        }

        public PorterDuff.Mode getMode() {
            return mMode;
        }
    }
}
