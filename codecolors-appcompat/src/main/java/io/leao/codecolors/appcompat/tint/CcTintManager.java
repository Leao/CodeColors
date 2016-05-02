package io.leao.codecolors.appcompat.tint;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.graphics.drawable.DrawableWrapper;
import android.util.AttributeSet;

import java.util.HashMap;
import java.util.Map;

import io.leao.codecolors.R;

import static io.leao.codecolors.appcompat.tint.CcThemeUtils.getDisabledThemeAttrColor;
import static io.leao.codecolors.appcompat.tint.CcThemeUtils.getThemeAttrColor;
import static io.leao.codecolors.appcompat.tint.CcTintManagerUtils.COLORFILTER_COLOR_BACKGROUND_MULTIPLY;
import static io.leao.codecolors.appcompat.tint.CcTintManagerUtils.COLORFILTER_COLOR_CONTROL_ACTIVATED;
import static io.leao.codecolors.appcompat.tint.CcTintManagerUtils.COLORFILTER_TINT_COLOR_CONTROL_NORMAL;
import static io.leao.codecolors.appcompat.tint.CcTintManagerUtils.DEFAULT_MODE;
import static io.leao.codecolors.appcompat.tint.CcTintManagerUtils.TINT_CHECKABLE_BUTTON_LIST;
import static io.leao.codecolors.appcompat.tint.CcTintManagerUtils.TINT_COLOR_CONTROL_NORMAL;
import static io.leao.codecolors.appcompat.tint.CcTintManagerUtils.TINT_COLOR_CONTROL_STATE_LIST;
import static io.leao.codecolors.appcompat.tint.CcTintManagerUtils.arrayContains;
import static io.leao.codecolors.appcompat.tint.CcTintManagerUtils.createTintFilter;
import static io.leao.codecolors.appcompat.tint.CcTintManagerUtils.setPorterDuffColorFilter;
import static io.leao.codecolors.appcompat.tint.CcTintManagerUtils.tintDrawableUsingColorFilter;

@SuppressLint("PrivateResource")
public class CcTintManager {
    private static final Map<Integer, int[]> mDrawableIdAttrs = new HashMap<>();

    /**
     * Based on {@link android.support.v7.widget.AppCompatDrawableManager#getDrawable(Context, int, boolean)} with some
     * workflow changes to account with drawables loaded by
     * {@link android.support.v7.widget.AppCompatBackgroundHelper#loadFromAttributes(AttributeSet, int)}.
     */
    public static void tintDrawable(Context context, ColorStateList tintList, Drawable drawable, int resId) {
        if (tintList != null) {
            DrawableWrapper drawableWrapper = getDrawableWrapper(drawable);
            if (drawableWrapper != null) {
                drawableWrapper.setCompatTintList(tintList);
            } else {
                // For drawables loaded by loadFromAttributes().
                drawable.setColorFilter(createTintFilter(tintList, DEFAULT_MODE, drawable.getState()));
            }
        } else if (resId == R.drawable.abc_seekbar_track_material) {
            LayerDrawable ld = (LayerDrawable) drawable;
            setPorterDuffColorFilter(ld.findDrawableByLayerId(android.R.id.background),
                    getThemeAttrColor(context, R.attr.colorControlNormal), DEFAULT_MODE);
            setPorterDuffColorFilter(ld.findDrawableByLayerId(android.R.id.secondaryProgress),
                    getThemeAttrColor(context, R.attr.colorControlNormal), DEFAULT_MODE);
            setPorterDuffColorFilter(ld.findDrawableByLayerId(android.R.id.progress),
                    getThemeAttrColor(context, R.attr.colorControlActivated), DEFAULT_MODE);
        } else if (resId == R.drawable.abc_ratingbar_indicator_material
                || resId == R.drawable.abc_ratingbar_small_material) {
            LayerDrawable ld = (LayerDrawable) drawable;
            setPorterDuffColorFilter(ld.findDrawableByLayerId(android.R.id.background),
                    getDisabledThemeAttrColor(context, R.attr.colorControlNormal),
                    DEFAULT_MODE);
            setPorterDuffColorFilter(ld.findDrawableByLayerId(android.R.id.secondaryProgress),
                    getThemeAttrColor(context, R.attr.colorControlActivated), DEFAULT_MODE);
            setPorterDuffColorFilter(ld.findDrawableByLayerId(android.R.id.progress),
                    getThemeAttrColor(context, R.attr.colorControlActivated), DEFAULT_MODE);
        } else {
            tintDrawableUsingColorFilter(context, resId, drawable);
        }
    }

    public static DrawableWrapper getDrawableWrapper(Drawable drawable) {
        if (drawable instanceof DrawableWrapper) {
            return (DrawableWrapper) drawable;
        } else {
            Drawable.Callback callback = drawable.getCallback();
            if (callback instanceof Drawable) {
                return getDrawableWrapper((Drawable) callback);
            }
        }

        return null;
    }

    /**
     * The attributes used on the drawable returned by
     * {@link android.support.v7.widget.AppCompatDrawableManager#getDrawable(Context, int, boolean)}.
     *
     * @return the attrs used when tinting the drawable, if any.
     */
    public static int[] getAttrs(int resId) {
        if (mDrawableIdAttrs.containsKey(resId)) {
            return mDrawableIdAttrs.get(resId);
        }

        int[] attrs = getTintListAttrs(resId);
        if (attrs == null) {
            if (resId == R.drawable.abc_seekbar_track_material) {
                attrs = new int[]{R.attr.colorControlNormal, R.attr.colorControlActivated};
            } else if (resId == R.drawable.abc_ratingbar_indicator_material
                    || resId == R.drawable.abc_ratingbar_small_material) {
                attrs = new int[]{R.attr.colorControlNormal, R.attr.colorControlActivated};
            } else {
                attrs = getColorFilterAttrs(resId);
            }
        }

        // Cache attrs for drawable.
        mDrawableIdAttrs.put(resId, attrs);

        return attrs;
    }

    /**
     * The attributes used on {@link android.support.v7.widget.AppCompatDrawableManager#getTintList(Context, int)}.
     */
    private static int[] getTintListAttrs(int resId) {
        if (resId == R.drawable.abc_edit_text_material) {
            return new int[]{R.attr.colorControlNormal, R.attr.colorControlActivated};
        } else if (resId == R.drawable.abc_switch_track_mtrl_alpha) {
            return new int[]{android.R.attr.colorForeground, R.attr.colorControlActivated};
        } else if (resId == R.drawable.abc_switch_thumb_material) {
            return new int[]{R.attr.colorSwitchThumbNormal, R.attr.colorControlActivated};
        } else if (resId == R.drawable.abc_btn_default_mtrl_shape) {
            return new int[]{R.attr.colorButtonNormal, R.attr.colorControlHighlight};
        } else if (resId == R.drawable.abc_btn_borderless_material) {
            return new int[]{R.attr.colorButtonNormal, R.attr.colorControlHighlight};
        } else if (resId == R.drawable.abc_btn_colored_material) {
            return new int[]{R.attr.colorAccent, R.attr.colorButtonNormal, R.attr.colorControlHighlight};
        } else if (resId == R.drawable.abc_spinner_mtrl_am_alpha
                || resId == R.drawable.abc_spinner_textfield_background_material) {
            return new int[]{R.attr.colorControlNormal, R.attr.colorControlActivated};
        } else if (arrayContains(TINT_COLOR_CONTROL_NORMAL, resId)) {
            return new int[]{R.attr.colorControlNormal};
        } else if (arrayContains(TINT_COLOR_CONTROL_STATE_LIST, resId)) {
            return new int[]{R.attr.colorControlNormal, R.attr.colorControlActivated};
        } else if (arrayContains(TINT_CHECKABLE_BUTTON_LIST, resId)) {
            return new int[]{R.attr.colorControlNormal, R.attr.colorControlActivated};
        } else if (resId == R.drawable.abc_seekbar_thumb_material) {
            return new int[]{R.attr.colorControlActivated};
        } else {
            return null;
        }
    }

    /**
     * The attributes used on
     * {@link android.support.v7.widget.AppCompatDrawableManager#tintDrawableUsingColorFilter(Context, int, Drawable)}
     * and subsequently on {@link CcTintManagerUtils#tintDrawableUsingColorFilter(Context, int, Drawable)}.
     */
    private static int[] getColorFilterAttrs(int resId) {
        if (arrayContains(COLORFILTER_TINT_COLOR_CONTROL_NORMAL, resId)) {
            return new int[]{R.attr.colorControlNormal};
        } else if (arrayContains(COLORFILTER_COLOR_CONTROL_ACTIVATED, resId)) {
            return new int[]{R.attr.colorControlActivated};
        } else if (arrayContains(COLORFILTER_COLOR_BACKGROUND_MULTIPLY, resId)) {
            return new int[]{android.R.attr.colorBackground};
        } else if (resId == R.drawable.abc_list_divider_mtrl_alpha) {
            return new int[]{android.R.attr.colorForeground};
        } else {
            return null;
        }
    }
}
