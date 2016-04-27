package io.leao.codecolors.sample.adapter;

import android.support.design.widget.CoordinatorLayout;
import android.view.View;

import io.leao.codecolors.core.adapter.CcAttrCallbackAdapter;
import io.leao.codecolors.core.res.CcColorStateList;
import io.leao.codecolors.sample.R;

public class CcStatusBarColorAnchorCallbackAdapter implements CcAttrCallbackAdapter<CoordinatorLayout> {
    @Override
    public boolean onCache(CacheResult<CoordinatorLayout> outResult) {
        outResult.set(
                new int[]{R.attr.statusBarBackground},
                new CcColorStateList.AnchorCallback<CoordinatorLayout>() {
                    @Override
                    public void invalidateColor(CoordinatorLayout anchor, CcColorStateList color) {
                        anchor.invalidate();
                    }
                });
        return true;
    }

    @Override
    public boolean onInflate(View view, int attr, InflateResult<CoordinatorLayout> outResult) {
        if (view instanceof CoordinatorLayout) {
            outResult.set((CoordinatorLayout) view);
            return true;
        }
        return false;
    }
}
