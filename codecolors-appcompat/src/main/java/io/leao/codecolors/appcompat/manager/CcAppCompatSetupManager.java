package io.leao.codecolors.appcompat.manager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.LongSparseArray;

import io.leao.codecolors.appcompat.adapter.CcBackgroundTintAttrCallbackAdapter;
import io.leao.codecolors.appcompat.adapter.CcTintableBackgroundColorCallbackAdapter;
import io.leao.codecolors.core.manager.CcCallbackManager;
import io.leao.codecolors.core.manager.CcSetupManager;
import io.leao.codecolors.core.drawable.CcAppCompatDrawableCache;
import io.leao.codecolors.core.drawable.CcDrawableCache;

public class CcAppCompatSetupManager extends CcSetupManager {
    @Override
    protected CcDrawableCache onCreateDrawableCache(Context context, LongSparseArray<Drawable.ConstantState> cache) {
        return new CcAppCompatDrawableCache(context, cache);
    }

    @Override
    protected void onAddDefaultCallbackAdapters(CcCallbackManager callbackManager) {
        super.onAddDefaultCallbackAdapters(callbackManager);
        callbackManager.addAttrCallbackAdapter(new CcBackgroundTintAttrCallbackAdapter());
        callbackManager.addColorCallbackAdapter(new CcTintableBackgroundColorCallbackAdapter());
    }
}
