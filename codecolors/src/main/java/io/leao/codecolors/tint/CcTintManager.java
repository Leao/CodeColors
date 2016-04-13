package io.leao.codecolors.tint;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.graphics.drawable.DrawableWrapper;
import android.support.v7.widget.TintManager;
import android.util.AttributeSet;

import java.util.HashMap;
import java.util.Map;

import io.leao.codecolors.R;

import static io.leao.codecolors.tint.CcTintManagerUtils.arrayContains;
import static io.leao.codecolors.tint.CcTintManagerUtils.createTintFilter;
import static io.leao.codecolors.tint.CcTintManagerUtils.getThemeAttrColor;
import static io.leao.codecolors.tint.CcTintManagerUtils.setPorterDuffColorFilter;

@SuppressLint("PrivateResource")
public class CcTintManager {

    private static final PorterDuff.Mode DEFAULT_MODE = PorterDuff.Mode.SRC_IN;

    /**
     * Drawables which should be tinted with the value of {@code R.attr.colorControlNormal},
     * using the default mode using a raw color filter.
     */
    private static final int[] COLORFILTER_TINT_COLOR_CONTROL_NORMAL = {
            R.drawable.abc_textfield_search_default_mtrl_alpha,
            R.drawable.abc_textfield_default_mtrl_alpha,
            R.drawable.abc_ab_share_pack_mtrl_alpha
    };

    /**
     * Drawables which should be tinted with the value of {@code R.attr.colorControlNormal}, using
     * {@link DrawableCompat}'s tinting functionality.
     */
    private static final int[] TINT_COLOR_CONTROL_NORMAL = {
            R.drawable.abc_ic_ab_back_mtrl_am_alpha,
            R.drawable.abc_ic_go_search_api_mtrl_alpha,
            R.drawable.abc_ic_search_api_mtrl_alpha,
            R.drawable.abc_ic_commit_search_api_mtrl_alpha,
            R.drawable.abc_ic_clear_mtrl_alpha,
            R.drawable.abc_ic_menu_share_mtrl_alpha,
            R.drawable.abc_ic_menu_copy_mtrl_am_alpha,
            R.drawable.abc_ic_menu_cut_mtrl_alpha,
            R.drawable.abc_ic_menu_selectall_mtrl_alpha,
            R.drawable.abc_ic_menu_paste_mtrl_am_alpha,
            R.drawable.abc_ic_menu_moreoverflow_mtrl_alpha,
            R.drawable.abc_ic_voice_search_api_mtrl_alpha
    };

    /**
     * Drawables which should be tinted with the value of {@code R.attr.colorControlActivated},
     * using a color filter.
     */
    private static final int[] COLORFILTER_COLOR_CONTROL_ACTIVATED = {
            R.drawable.abc_textfield_activated_mtrl_alpha,
            R.drawable.abc_textfield_search_activated_mtrl_alpha,
            R.drawable.abc_cab_background_top_mtrl_alpha,
            R.drawable.abc_text_cursor_material
    };

    /**
     * Drawables which should be tinted with the value of {@code android.R.attr.colorBackground},
     * using the {@link android.graphics.PorterDuff.Mode#MULTIPLY} mode and a color filter.
     */
    private static final int[] COLORFILTER_COLOR_BACKGROUND_MULTIPLY = {
            R.drawable.abc_popup_background_mtrl_mult,
            R.drawable.abc_cab_background_internal_bg,
            R.drawable.abc_menu_hardkey_panel_mtrl_mult
    };

    /**
     * Drawables which should be tinted using a state list containing values of
     * {@code R.attr.colorControlNormal} and {@code R.attr.colorControlActivated}
     */
    private static final int[] TINT_COLOR_CONTROL_STATE_LIST = {
            R.drawable.abc_edit_text_material,
            R.drawable.abc_tab_indicator_material,
            R.drawable.abc_textfield_search_material,
            R.drawable.abc_spinner_mtrl_am_alpha,
            R.drawable.abc_spinner_textfield_background_material,
            R.drawable.abc_ratingbar_full_material,
            R.drawable.abc_switch_track_mtrl_alpha,
            R.drawable.abc_switch_thumb_material,
            R.drawable.abc_btn_default_mtrl_shape,
            R.drawable.abc_btn_borderless_material
    };

    /**
     * Drawables which should be tinted using a state list containing values of
     * {@code R.attr.colorControlNormal} and {@code R.attr.colorControlActivated} for the checked
     * state.
     */
    private static final int[] TINT_CHECKABLE_BUTTON_LIST = {
            R.drawable.abc_btn_check_material,
            R.drawable.abc_btn_radio_material
    };

    private static final Map<Integer, int[]> mDrawableIdAttrs = new HashMap<>();

    /**
     * Based on {@link android.support.v7.widget.TintManager#getDrawable(int, boolean)} with some workflow changes to
     * account with drawables loaded by
     * {@link android.support.v7.widget.AppCompatBackgroundHelper#loadFromAttributes(AttributeSet, int)}.
     */
    public static void tintDrawable(Context context, TintManager tintManager, ColorStateList tintList,
                                    Drawable drawable, int resId) {
        if (tintList != null) {
            DrawableWrapper drawableWrapper = getDrawableWrapper(drawable);
            if (drawableWrapper != null) {
                drawableWrapper.setTintList(tintList);
            } else {
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
        } else {
            tintManager.tintDrawableUsingColorFilter(resId, drawable);
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
     * Based on {@link android.support.v7.widget.TintManager#getDrawable(int, boolean)}.
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
            } else {
                attrs = getColorFilterAttrs(resId);
            }
        }

        // Cache attrs for drawable.
        mDrawableIdAttrs.put(resId, attrs);

        return attrs;
    }

    private static int[] getTintListAttrs(int resId) {
        if (resId == R.drawable.abc_edit_text_material) {
            return new int[]{R.attr.colorControlNormal, R.attr.colorControlActivated};
        } else if (resId == R.drawable.abc_switch_track_mtrl_alpha) {
            return new int[]{android.R.attr.colorForeground, R.attr.colorControlActivated};
        } else if (resId == R.drawable.abc_switch_thumb_material) {
            return new int[]{R.attr.colorSwitchThumbNormal, R.attr.colorControlActivated};
        } else if (resId == R.drawable.abc_btn_default_mtrl_shape
                || resId == R.drawable.abc_btn_borderless_material) {
            return new int[]{R.attr.colorButtonNormal, R.attr.colorControlHighlight};
        } else if (resId == R.drawable.abc_btn_colored_material) {
            return new int[]{R.attr.colorAccent, R.attr.colorControlHighlight, R.attr.colorButtonNormal};
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
