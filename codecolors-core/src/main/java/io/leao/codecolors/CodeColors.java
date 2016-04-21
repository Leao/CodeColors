package io.leao.codecolors;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.view.View;

import io.leao.codecolors.core.CcCore;
import io.leao.codecolors.core.adapter.CcAttrCallbackAdapter;
import io.leao.codecolors.core.adapter.CcColorCallbackAdapter;
import io.leao.codecolors.core.adapter.CcDefStyleAdapter;
import io.leao.codecolors.core.manager.CcSetupManager;
import io.leao.codecolors.core.res.CcColorStateList;

public class CodeColors {
    public static void setup(Context context) {
        setup(context, null);
    }

    public static void setup(Context context, Callback callback) {
        setup(context, true, callback);
    }

    public static void setup(Context context, boolean useDefaultCallbackAdapters, final Callback callback) {
        CcCore.getSetupManager().setup(context, useDefaultCallbackAdapters, new CcSetupManager.Callback() {
            @Override
            public void onCodeColorsSetupSuccess() {
                callback.onCodeColorsSetupSuccess();
            }

            @Override
            public void onCodeColorsSetupFailure() {
                callback.onCodeColorsSetupFailure();
            }
        });
    }

    /**
     * @return {@code true} if {@link #setup(Context, boolean, Callback)} completed successfully;
     * {@code false}, otherwise.
     */
    public static boolean isActive() {
        return CcCore.getSetupManager().isActive();
    }

    public static CcColorStateList getColor(int resId) {
        return CcCore.getColorsManager().getColor(resId);
    }

    public static void setColor(int resId, int color) {
        CcCore.getColorsManager().setColor(resId, color);
    }

    public static void setColor(int resId, ColorStateList color) {
        CcCore.getColorsManager().setColor(resId, color);
    }

    public static void setState(int resId, int[] state, int color) {
        CcCore.getColorsManager().setState(resId, state, color);
    }

    public static void setStates(int resId, int[][] states, int[] colors) {
        CcCore.getColorsManager().setStates(resId, states, colors);
    }

    public static void removeState(int resId, int[] state) {
        CcCore.getColorsManager().removeState(resId, state);
    }

    public static void removeStates(int resId, int[][] states) {
        CcCore.getColorsManager().removeStates(resId, states);
    }

    public static CcColorStateList.AnimationBuilder animate(int resId) {
        return CcCore.getColorsManager().animate(resId);
    }

    /**
     * Adds a view to the callback manager. The library will call
     * {@link CcColorCallbackAdapter#onAdd(View, CcColorCallbackAdapter.InflateAddResult)} for all
     * {@link CcColorCallbackAdapter}s added.
     */
    public static void addView(@NonNull View view) {
        CcCore.getCallbackManager().addView(view);
    }

    public static void addAttrCallbackAdapter(@NonNull CcAttrCallbackAdapter adapter) {
        CcCore.getCallbackManager().addAttrCallbackAdapter(adapter);
    }

    public static void addViewCallbackAdapter(@NonNull CcColorCallbackAdapter adapter) {
        CcCore.getCallbackManager().addColorCallbackAdapter(adapter);
    }

    public static void addViewDefStyleAdapter(@NonNull CcDefStyleAdapter adapter) {
        CcCore.getCallbackManager().addDefStyleAdapter(adapter);
    }

    public static void addColorCallback(int resId, Object anchor, CcColorStateList.AnchorCallback callback) {
        CcColorStateList color = getColor(resId);
        if (color != null) {
            color.addAnchorCallback(anchor, callback);
        }
    }

    public interface Callback {
        void onCodeColorsSetupSuccess();

        void onCodeColorsSetupFailure();
    }
}
