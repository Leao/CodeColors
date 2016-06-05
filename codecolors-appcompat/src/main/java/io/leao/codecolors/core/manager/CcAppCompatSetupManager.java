package io.leao.codecolors.core.manager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.LongSparseArray;

import io.leao.codecolors.appcompat.adapter.CcBackgroundTintAttrCallbackAdapter;
import io.leao.codecolors.appcompat.adapter.CcTintableBackgroundColorCallbackAdapter;
import io.leao.codecolors.core.adapter.CcAdapterManager;
import io.leao.codecolors.core.drawable.CcAppCompatDrawableCache;
import io.leao.codecolors.core.drawable.CcDrawableCache;

public class CcAppCompatSetupManager extends CcSetupManager {
    @Override
    protected CcDrawableCache onCreateDrawableCache(Context context, LongSparseArray<Drawable.ConstantState> cache) {
        return new CcAppCompatDrawableCache(context, cache);
    }

    @Override
    protected void onAddDefaultCallbackAdapters(CcAdapterManager adapterManager) {
        super.onAddDefaultCallbackAdapters(adapterManager);
        adapterManager.addAttrCallbackAdapter(new CcBackgroundTintAttrCallbackAdapter());
        adapterManager.addColorCallbackAdapter(new CcTintableBackgroundColorCallbackAdapter());
    }
}
