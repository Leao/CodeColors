package io.leao.codecolors.appcompat.manager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.LongSparseArray;

import io.leao.codecolors.appcompat.inflate.sample.CcBackgroundTintAttrCallbackAdapter;
import io.leao.codecolors.appcompat.inflate.sample.CcTintableBackgroundColorCallbackAdapter;
import io.leao.codecolors.core.drawable.CcAppCompatDrawableCache;
import io.leao.codecolors.core.drawable.CcDrawableCache;
import io.leao.codecolors.core.inflate.CcInflateManager;
import io.leao.codecolors.core.manager.CcSetupManager;

public class CcAppCompatSetupManager extends CcSetupManager {
    @Override
    protected CcDrawableCache onCreateDrawableCache(Context context, LongSparseArray<Drawable.ConstantState> cache) {
        return new CcAppCompatDrawableCache(context, cache);
    }

    @Override
    protected void onAddDefaultCallbackAdapters(CcInflateManager inflateManager) {
        super.onAddDefaultCallbackAdapters(inflateManager);
        inflateManager.addAttrCallbackAdapter(new CcBackgroundTintAttrCallbackAdapter());
        inflateManager.addColorCallbackAdapter(new CcTintableBackgroundColorCallbackAdapter());
    }
}
