package io.leao.codecolors.adapter;

import android.annotation.SuppressLint;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.v4.view.TintableBackgroundView;
import android.support.v7.widget.TintManager;
import android.util.AttributeSet;
import android.view.View;

import io.leao.codecolors.R;
import io.leao.codecolors.res.CcColorStateList;

@SuppressLint("PrivateResource")
public class CcTintableBackgroundColorCallbackAdapter implements CcColorCallbackAdapter<View> {

    private static final int[] ATTRS_ARRAY = {
            android.R.attr.background,
            R.attr.colorControlNormal,
            R.attr.colorControlActivated,
            android.R.attr.colorForeground,
            R.attr.colorSwitchThumbNormal,
            R.attr.colorButtonNormal,
            R.attr.colorControlHighlight,
            R.attr.colorAccent,
    };

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

    private static final int[] TINT_CHECKABLE_BUTTON_LIST = {
            R.drawable.abc_btn_check_material,
            R.drawable.abc_btn_radio_material
    };

    @Override
    public boolean onCache(CacheResult<View> outResult) {
        return false;
    }

    @Override
    public boolean onInflate(AttributeSet attrs, View view, int defStyleAttr, int defStyleRes,
                             InflateResult<View> outResult) {
        if (view instanceof TintableBackgroundView) {
            // Set anchor.
            outResult.set(view);

            /*
             * Try to add colors and callbacks.
             */

            ColorStateList backgroundTintList = ((TintableBackgroundView) view).getSupportBackgroundTintList();
            if (backgroundTintList instanceof CcColorStateList) {
                outResult.add(
                        (CcColorStateList) backgroundTintList,
                        new TintableBackgroundRefreshDrawableCallback(backgroundTintList));
            } else if (backgroundTintList == null) {
                TypedArray ta = view.getContext().obtainStyledAttributes(attrs, ATTRS_ARRAY, defStyleAttr, defStyleRes);
                try {
                    final int backgroundResId = ta.getResourceId(0, -1);
                    if (backgroundResId != -1) {
                        CcColorStateList.AnchorCallback<View> callback =
                                new TintableBackgroundUpdateTintListCallback(backgroundResId);

                        /*
                         * Checks if the attributes used by {@link TintManager#getTintList(int)} given the view's
                         * background are instances of {@link CcColorStateList} and adds them to the result if possible.
                         */

                        if (backgroundResId == R.drawable.abc_edit_text_material) {
                            addColorCallbackIfPossible(ta, 1, outResult, callback);
                            addColorCallbackIfPossible(ta, 2, outResult, callback);
                        } else if (backgroundResId == R.drawable.abc_switch_track_mtrl_alpha) {
                            addColorCallbackIfPossible(ta, 2, outResult, callback);
                            addColorCallbackIfPossible(ta, 3, outResult, callback);
                        } else if (backgroundResId == R.drawable.abc_switch_thumb_material) {
                            addColorCallbackIfPossible(ta, 2, outResult, callback);
                            addColorCallbackIfPossible(ta, 4, outResult, callback);
                        } else if (backgroundResId == R.drawable.abc_btn_default_mtrl_shape
                                || backgroundResId == R.drawable.abc_btn_borderless_material) {
                            addColorCallbackIfPossible(ta, 5, outResult, callback);
                            addColorCallbackIfPossible(ta, 6, outResult, callback);
                        } else if (backgroundResId == R.drawable.abc_btn_colored_material) {
                            addColorCallbackIfPossible(ta, 5, outResult, callback);
                            addColorCallbackIfPossible(ta, 6, outResult, callback);
                            addColorCallbackIfPossible(ta, 7, outResult, callback);
                        } else if (backgroundResId == R.drawable.abc_spinner_mtrl_am_alpha
                                || backgroundResId == R.drawable.abc_spinner_textfield_background_material) {
                            addColorCallbackIfPossible(ta, 1, outResult, callback);
                            addColorCallbackIfPossible(ta, 2, outResult, callback);
                            addColorCallbackIfPossible(ta, 3, outResult, callback);
                        } else if (arrayContains(TINT_COLOR_CONTROL_NORMAL, backgroundResId)) {
                            addColorCallbackIfPossible(ta, 1, outResult, callback);
                        } else if (arrayContains(TINT_COLOR_CONTROL_STATE_LIST, backgroundResId)) {
                            addColorCallbackIfPossible(ta, 1, outResult, callback);
                            addColorCallbackIfPossible(ta, 2, outResult, callback);
                        } else if (arrayContains(TINT_CHECKABLE_BUTTON_LIST, backgroundResId)) {
                            addColorCallbackIfPossible(ta, 1, outResult, callback);
                            addColorCallbackIfPossible(ta, 2, outResult, callback);
                        } else if (backgroundResId == R.drawable.abc_seekbar_thumb_material) {
                            addColorCallbackIfPossible(ta, 2, outResult, callback);
                        }
                    }
                } finally {
                    ta.recycle();
                }
            }

            return outResult.colors.size() > 0;
        }
        return false;
    }

    /**
     * Adds color to result if it is a instance of {@link CcColorStateList}.
     */
    private static void addColorCallbackIfPossible(TypedArray ta,
                                                   int index,
                                                   CcColorCallbackAdapter.InflateResult<View> result,
                                                   CcColorStateList.AnchorCallback<View> callback) {
        ColorStateList csl = ta.getColorStateList(index);
        if (csl instanceof CcColorStateList) {
            result.add((CcColorStateList) csl, callback);
        }
    }

    private static boolean arrayContains(int[] array, int value) {
        for (int id : array) {
            if (id == value) {
                return true;
            }
        }
        return false;
    }

    private static class TintableBackgroundRefreshDrawableCallback implements CcColorStateList.AnchorCallback<View> {
        private ColorStateList mBackgroundTintList;

        public TintableBackgroundRefreshDrawableCallback(ColorStateList backgroundTintList) {
            mBackgroundTintList = backgroundTintList;
        }

        @Override
        public void invalidateColor(View anchor, CcColorStateList color) {
            if (((TintableBackgroundView) anchor).getSupportBackgroundTintList() == mBackgroundTintList) {
                anchor.refreshDrawableState();
            } else {
                color.removeCallback(this);
            }
        }
    }

    private static class TintableBackgroundUpdateTintListCallback implements CcColorStateList.AnchorCallback<View> {
        private int mBackgroundId;
        private ColorStateList mBackgroundTintList;

        public TintableBackgroundUpdateTintListCallback(int backgroundId) {
            mBackgroundId = backgroundId;
        }

        @Override
        public void invalidateColor(View anchor, CcColorStateList color) {
            if (((TintableBackgroundView) anchor).getSupportBackgroundTintList() == mBackgroundTintList) {
                TintManager manager = TintManager.get(new ContextWrapper(anchor.getContext()));
                ColorStateList backgroundTintList = manager.getTintList(mBackgroundId);
                if (backgroundTintList != null) {
                    ((TintableBackgroundView) anchor).setSupportBackgroundTintList(backgroundTintList);
                    mBackgroundTintList = ((TintableBackgroundView) anchor).getSupportBackgroundTintList();
                    return; // Success!
                }
            }
            // Failure, removes this callback from color.
            color.removeCallback(this);
        }
    }
}
