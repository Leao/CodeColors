package io.leao.codecolors.res;

import android.content.res.ColorStateList;
import android.content.res.Resources;
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

/**
 * Inspired in {@link android.graphics.drawable.ColorDrawable}, but instead of making use of a {@code int} color, it
 * makes use of a {@link CodeColorStateList}.
 */
public class CodeColorDrawable extends Drawable implements CodeColorStateList.Callback {
    private static final int COLOR_DEFAULT = Color.BLUE;
    private static final int ALPHA_OPAQUE = 255;

    private static final PorterDuff.Mode DEFAULT_TINT_MODE = PorterDuff.Mode.SRC_IN;

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private CodeColorState mCodeColorState;
    private CodePorterDuffColorFilter mTintFilter;

    private int mUseColor;

    private boolean mMutated;

    /**
     * Creates a new ColorDrawable with the specified color.
     *
     * @param color The color to draw.
     */
    public CodeColorDrawable(CodeColorStateList color) {
        mCodeColorState = new CodeColorState(color);
        updateLocalState();
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

    @Override
    public void draw(Canvas canvas) {
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

    /**
     * Gets the drawable's color value.
     *
     * @return int The color to draw.
     */
    public CodeColorStateList getColor() {
        return mCodeColorState.mColor;
    }

    /**
     * Sets the drawable's color value. This action will clobber the results of
     * prior calls to {@link #setAlpha(int)} on this object, which side-affected
     * the underlying color.
     *
     * @param color The color to draw.
     */
    public void setColor(CodeColorStateList color) {
        if (mCodeColorState.mColor != color) {
            if (mCodeColorState.mColor != null) {
                mCodeColorState.mColor.removeCallback(this);
            }
            mCodeColorState.mColor = color;
            if (mCodeColorState.mColor != null) {
                mCodeColorState.mColor.addCallback(this);
            }
            if (updateUseColor(mCodeColorState.mColor, getState(), mCodeColorState.mAlpha)) {
                invalidateSelf();
            }
        }
    }

    @Override
    public void invalidateColor(CodeColorStateList color) {
        if (updateUseColor(color, getState(), mCodeColorState.mAlpha)) {
            invalidateSelf();
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
     * <p/>
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

    public static CodeColorState getConstantStateForColor(CodeColorStateList color) {
        return new CodeColorState(color);
    }

    protected static class CodeColorState extends ConstantState {
        int[] mThemeAttrs;
        CodeColorStateList mColor;
        int mAlpha = ALPHA_OPAQUE;
        int mChangingConfigurations;
        ColorStateList mTint = null;
        PorterDuff.Mode mTintMode = DEFAULT_TINT_MODE;

        CodeColorState(CodeColorStateList color) {
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

        @Override
        public Drawable newDrawable() {
            return new CodeColorDrawable(this, null);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new CodeColorDrawable(this, res);
        }

        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations;
        }
    }

    private CodeColorDrawable(CodeColorState state, Resources res) {
        mCodeColorState = state;
        updateLocalState();
    }

    /**
     * Initializes local dynamic properties from state. This should be called
     * after significant state changes, e.g. from the One True Constructor and
     * after inflating or applying a theme.
     */
    protected void updateLocalState() {
        if (mCodeColorState.mColor != null) {
            mCodeColorState.mColor.addCallback(this);
        }
        updateUseColor(mCodeColorState.mColor, getState(), mCodeColorState.mAlpha);
        updateTintFilter(mCodeColorState.mTint, mCodeColorState.mTintMode);
    }

    /**
     * Ensures the use color is consistent with the current color state and alpha.
     *
     * @return {@code true} if use color changed; false, otherwise.
     */
    protected boolean updateUseColor(CodeColorStateList color, int[] stateSet, int alpha) {
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
