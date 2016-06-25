package io.leao.codecolors;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import io.leao.codecolors.core.CcCore;
import io.leao.codecolors.core.inflate.CcAttrCallbackAdapter;
import io.leao.codecolors.core.color.CcColorAdapter;
import io.leao.codecolors.core.inflate.CcColorCallbackAdapter;
import io.leao.codecolors.core.inflate.CcDefStyleAdapter;
import io.leao.codecolors.core.color.CcColorStateList;
import io.leao.codecolors.core.editor.CcEditorAnimate;
import io.leao.codecolors.core.editor.CcEditorSet;
import io.leao.codecolors.core.editor.CcMultiEditorAnimate;
import io.leao.codecolors.core.editor.CcMultiEditorSet;
import io.leao.codecolors.core.manager.CcSetupManager;

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
        return CcCore.getColorManager().getColor(resId);
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
        CcCore.getInflateManager().addView(view);
    }

    public static void addAttrCallbackAdapter(@NonNull CcAttrCallbackAdapter adapter) {
        CcCore.getInflateManager().addAttrCallbackAdapter(adapter);
    }

    public static void addViewCallbackAdapter(@NonNull CcColorCallbackAdapter adapter) {
        CcCore.getInflateManager().addColorCallbackAdapter(adapter);
    }

    public static void addViewDefStyleAdapter(@NonNull CcDefStyleAdapter adapter) {
        CcCore.getInflateManager().addDefStyleAdapter(adapter);
    }

    public static void setColorAdapter(CcColorAdapter adapter) {
        CcCore.getColorManager().setColorAdapter(adapter);
    }

    public interface Callback {
        void onCodeColorsStarted();

        void onCodeColorsFailed(Exception e);
    }
}
