package io.leao.codecolors.core.manager;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.util.LongSparseArray;

import java.lang.reflect.Field;

import io.leao.codecolors.core.CcCore;
import io.leao.codecolors.core.color.CcColorCache;
import io.leao.codecolors.core.color.CcColorFactoryCache;
import io.leao.codecolors.core.color.CcColorManager;
import io.leao.codecolors.core.drawable.CcDrawableCache;
import io.leao.codecolors.core.inflate.CcInflateManager;
import io.leao.codecolors.core.inflate.sample.CcTextColorsColorCallbackAdapter;
import io.leao.codecolors.core.inflate.sample.CcTextHighlightColorColorCallbackAdapter;

public class CcSetupManager {
    private static final String LOG_TAG = CcSetupManager.class.getSimpleName();

    private static boolean sIsActive = false;

    @SuppressWarnings("unchecked")
    public void setup(Context context, boolean useDefaultCallbackAdapters, Callback callback) {
        try {
            String packageName = context.getPackageName();
            // Initialize colors.
            CcColorManager colorsManager = CcCore.getColorManager();
            colorsManager.init(packageName);
            // Initialize dependencies.
            CcCore.getDependencyManager().init(packageName);

            // The SparseArray that holds the entries of preloaded colors.
            Field sPreloadedColorStateListsField = Resources.class.getDeclaredField("sPreloadedColorStateLists");
            sPreloadedColorStateListsField.setAccessible(true);
            LongSparseArray sPreloadedColorStateLists = (LongSparseArray) sPreloadedColorStateListsField.get(null);
            // Whether we should cache the color directly, or its ConstantState.
            boolean useColorStateListConstantState = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
            if (useColorStateListConstantState) {
                sPreloadedColorStateLists = new CcColorFactoryCache(context, sPreloadedColorStateLists);
            } else {
                sPreloadedColorStateLists = new CcColorCache(context, sPreloadedColorStateLists);
            }
            sPreloadedColorStateListsField.set(null, sPreloadedColorStateLists);

            // The SparseArray (or array) that holds the entries of preloaded drawables.
            Field sPreloadedDrawablesField = Resources.class.getDeclaredField("sPreloadedDrawables");
            sPreloadedDrawablesField.setAccessible(true);
            Object sPreloadedDrawables = sPreloadedDrawablesField.get(null);
            // Whether to layout orientation to cache drawables.
            // If sPreloadedDrawables type is not supported, useLayoutDirectionDrawableCache throws an exception.
            boolean useLayoutDirectionDrawableCache = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
            if (useLayoutDirectionDrawableCache) {
                sPreloadedDrawables = new CcDrawableCache[]{
                        onCreateDrawableCache(context, ((LongSparseArray[]) sPreloadedDrawables)[0]),
                        onCreateDrawableCache(context, ((LongSparseArray[]) sPreloadedDrawables)[1])};
            } else {
                sPreloadedDrawables = onCreateDrawableCache(context, (LongSparseArray) sPreloadedDrawables);
            }
            // Override preloaded drawables cache.
            sPreloadedDrawablesField.set(null, sPreloadedDrawables);
        } catch (Exception e) {
            onCodeColorsSetupFailure(e, callback);

            return; // Finish without setting code colors active.
        }

        // Code colors successfully injected.
        sIsActive = true;

        onCodeColorsSetupSuccess(useDefaultCallbackAdapters, callback);

        // Configure colors and dependencies for current configuration.
        Resources resources = context.getResources();
        CcCore.getActivityManager().onConfigurationChanged(resources.getConfiguration(), resources);
    }

    protected CcDrawableCache onCreateDrawableCache(Context context, LongSparseArray<Drawable.ConstantState> cache) {
        return new CcDrawableCache(context, cache);
    }

    protected void onCodeColorsSetupSuccess(boolean useDefaultCallbackAdapters, Callback callback) {
        // Setup default callback adapters, if desired.
        if (useDefaultCallbackAdapters) {
            onAddDefaultCallbackAdapters(CcCore.getInflateManager());
        }

        if (callback != null) {
            callback.onCodeColorsSetupSuccess();
        }
    }

    protected void onAddDefaultCallbackAdapters(CcInflateManager inflateManager) {
        inflateManager.addColorCallbackAdapter(new CcTextColorsColorCallbackAdapter());
        inflateManager.addColorCallbackAdapter(new CcTextHighlightColorColorCallbackAdapter());
    }

    protected void onCodeColorsSetupFailure(Exception e, Callback callback) {
        Log.w(LOG_TAG, "ColorStateList preload failed. Dynamic colors will not work.", e);

        if (callback != null) {
            callback.onCodeColorsSetupFailure(e);
        }
    }

    public boolean isActive() {
        return sIsActive;
    }

    public interface Callback {
        void onCodeColorsSetupSuccess();

        void onCodeColorsSetupFailure(Exception e);
    }
}
