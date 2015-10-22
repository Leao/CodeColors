package io.leao.codecolors.res;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

/**
 * A replica of {@link android.graphics.drawable.ColorDrawable}, but instead of making use of a {@code int} color, it
 * makes use of a {@link CodeColorStateList}.
 */
public class CodeColorDrawable extends Drawable {
    private static final PorterDuff.Mode DEFAULT_TINT_MODE = PorterDuff.Mode.SRC_IN;

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private CodeColorState mCodeColorState;
    private PorterDuffColorFilter mTintFilter;

    private boolean mMutated;

    /**
     * Creates a new ColorDrawable with the specified color.
     *
     * @param color The color to draw.
     */
    public CodeColorDrawable(CodeColorStateList color) {
        mCodeColorState = new CodeColorState(color);

        setColor(color.getDefaultColor());
    }

    @Override
    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | mCodeColorState.getChangingConfigurations();
    }

    @Override
    public void draw(Canvas canvas) {
        final ColorFilter colorFilter = mPaint.getColorFilter();
        if ((mCodeColorState.mUseColor >>> 24) != 0 || colorFilter != null || mTintFilter != null) {
            if (colorFilter == null) {
                mPaint.setColorFilter(mTintFilter);
            }

            mPaint.setColor(mCodeColorState.mUseColor);
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
    @ColorInt
    public int getColor() {
        return mCodeColorState.mUseColor;
    }

    /**
     * Sets the drawable's color value. This action will clobber the results of
     * prior calls to {@link #setAlpha(int)} on this object, which side-affected
     * the underlying color.
     *
     * @param color The color to draw.
     */
    public void setColor(@ColorInt int color) {
        if (mCodeColorState.mBaseColor != color || mCodeColorState.mUseColor != color) {
            mCodeColorState.mBaseColor = mCodeColorState.mUseColor = color;
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
        return mCodeColorState.mUseColor >>> 24;
    }

    /**
     * Sets the color's alpha value.
     *
     * @param alpha The alpha value to set, between 0 and 255.
     */
    @Override
    public void setAlpha(int alpha) {
        alpha += alpha >> 7;   // make it 0..256
        final int baseAlpha = mCodeColorState.mBaseColor >>> 24;
        final int useAlpha = baseAlpha * alpha >> 8;
        final int useColor = (mCodeColorState.mBaseColor << 8 >>> 8) | (useAlpha << 24);
        if (mCodeColorState.mUseColor != useColor) {
            mCodeColorState.mUseColor = useColor;
            invalidateSelf();
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
        mTintFilter = updateTintFilter(tint, mCodeColorState.mTintMode);
        invalidateSelf();
    }

    @Override
    public void setTintMode(@NonNull PorterDuff.Mode tintMode) {
        mCodeColorState.mTintMode = tintMode;
        mTintFilter = updateTintFilter(mCodeColorState.mTint, tintMode);
        invalidateSelf();
    }

    @Override
    protected boolean onStateChange(int[] stateSet) {
        final CodeColorState state = mCodeColorState;
        if (state.mTint != null && state.mTintMode != null) {
            mTintFilter = updateTintFilter(state.mTint, state.mTintMode);
            return true;
        }
        return false;
    }

    @Override
    public boolean isStateful() {
        return mCodeColorState.mTint != null && mCodeColorState.mTint.isStateful();
    }

    @Override
    public int getOpacity() {
        if (mTintFilter != null || mPaint.getColorFilter() != null) {
            return PixelFormat.TRANSLUCENT;
        }

        switch (mCodeColorState.mUseColor >>> 24) {
            case 255:
                return PixelFormat.OPAQUE;
            case 0:
                return PixelFormat.TRANSPARENT;
        }
        return PixelFormat.TRANSLUCENT;
    }

//    @Override
//    public void getOutline(@NonNull Outline outline) {
//        outline.setRect(getBounds());
//        outline.setAlpha(getAlpha() / 255.0f);
//    }

    @Override
    public boolean canApplyTheme() {
        return false;
    }

    @Override
    public ConstantState getConstantState() {
        return mCodeColorState;
    }

    final static class CodeColorState extends ConstantState {
        int[] mThemeAttrs;
        CodeColorStateList mColor;
        int mBaseColor; // base color, independent of setAlpha()
        int mUseColor;  // base color modulated by setAlpha()
        int mChangingConfigurations;
        ColorStateList mTint = null;
        PorterDuff.Mode mTintMode = DEFAULT_TINT_MODE;

        CodeColorState(CodeColorStateList color) {
            mColor = color;
        }

        CodeColorState(CodeColorState state) {
            mColor = state.mColor;
            mThemeAttrs = state.mThemeAttrs;
            mBaseColor = state.mBaseColor;
            mUseColor = state.mUseColor;
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
    private void updateLocalState() {
        mTintFilter = updateTintFilter(mCodeColorState.mTint, mCodeColorState.mTintMode);
    }

    /**
     * Ensures the tint filter is consistent with the current tint color and
     * mode.
     */
    PorterDuffColorFilter updateTintFilter(ColorStateList tint, PorterDuff.Mode tintMode) {
        if (tint == null || tintMode == null) {
            return null;
        }

        final int color = tint.getColorForState(getState(), Color.TRANSPARENT);
        return new PorterDuffColorFilter(color, tintMode);
    }
}
