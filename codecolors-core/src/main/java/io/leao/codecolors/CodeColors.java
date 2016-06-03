package io.leao.codecolors;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import io.leao.codecolors.core.CcCore;
import io.leao.codecolors.core.adapter.CcAttrCallbackAdapter;
import io.leao.codecolors.core.adapter.CcBaseColorAdapter;
import io.leao.codecolors.core.adapter.CcColorCallbackAdapter;
import io.leao.codecolors.core.adapter.CcDefStyleAdapter;
import io.leao.codecolors.core.color.CcColorStateList;
import io.leao.codecolors.core.manager.CcSetupManager;
import io.leao.codecolors.core.editor.CcEditorAnimate;
import io.leao.codecolors.core.editor.CcEditorSet;
import io.leao.codecolors.core.editor.CcMultiEditorAnimate;
import io.leao.codecolors.core.editor.CcMultiEditorSet;

public class CodeColors {
    public static void start(Context context) {
        start(context, null);
    }

    public static void start(Context context, Callback callback) {
        start(context, true, callback);
    }

    public static void start(Context context, boolean useDefaultCallbackAdapters, final Callback callback) {
        CcCore.getSetupManager().setup(context, useDefaultCallbackAdapters, new CcSetupManager.Callback() {
            @Override
            public void onCodeColorsSetupSuccess() {
                callback.onCodeColorsStarted();
            }

            @Override
            public void onCodeColorsSetupFailure(Exception e) {
                callback.onCodeColorsFailed(e);
            }
        });
    }

    /**
     * @return {@code true} if {@link #start(Context, boolean, Callback)} completed successfully;
     * {@code false}, otherwise.
     */
    public static boolean isActive() {
        return CcCore.getSetupManager().isActive();
    }

    public static CcColorStateList getColor(int resId) {
        return CcCore.getColorsManager().getColor(resId);
    }

    public static CcEditorSet set(int resId) {
        CcColorStateList color = getColor(resId);
        return color != null ? color.set() : null;
    }

    public static CcMultiEditorSet setMultiple() {
        return CcCore.getEditorManager().getMultiEditorSet();
    }

    public static CcEditorAnimate animate(int resId) {
        CcColorStateList color = getColor(resId);
        return color != null ? color.animate() : null;
    }

    public static CcMultiEditorAnimate animateMultiple() {
        return CcCore.getEditorManager().getMultiEditorAnimate();
    }

    /**
     * Adds a view to the callback manager. The library will call
     * {@link CcColorCallbackAdapter#onAdd(View, CcColorCallbackAdapter.InflateAddResult)} for all
     * {@link CcColorCallbackAdapter}s added.
     */
    public static void addView(@NonNull View view) {
        CcCore.getAdapterManager().addView(view);
    }

    public static void addAttrCallbackAdapter(@NonNull CcAttrCallbackAdapter adapter) {
        CcCore.getAdapterManager().addAttrCallbackAdapter(adapter);
    }

    public static void addViewCallbackAdapter(@NonNull CcColorCallbackAdapter adapter) {
        CcCore.getAdapterManager().addColorCallbackAdapter(adapter);
    }

    public static void addViewDefStyleAdapter(@NonNull CcDefStyleAdapter adapter) {
        CcCore.getAdapterManager().addDefStyleAdapter(adapter);
    }

    public static void setBaseColorAdapter(CcBaseColorAdapter adapter) {
        CcCore.getColorsManager().setBaseColorAdapter(adapter);
    }

    public interface Callback {
        void onCodeColorsStarted();

        void onCodeColorsFailed(Exception e);
    }
}
