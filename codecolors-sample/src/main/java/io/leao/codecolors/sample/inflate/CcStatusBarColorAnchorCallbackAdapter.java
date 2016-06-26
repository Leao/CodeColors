package io.leao.codecolors.sample.inflate;

import android.support.design.widget.CoordinatorLayout;
import android.view.View;

import java.util.Set;

import io.leao.codecolors.core.color.CcColorStateList;
import io.leao.codecolors.core.inflate.CcAttrCallbackAdapter;
import io.leao.codecolors.sample.R;

public class CcStatusBarColorAnchorCallbackAdapter implements CcAttrCallbackAdapter<CoordinatorLayout> {
    @Override
    public boolean onCache(CacheResult<CoordinatorLayout> outResult) {
        outResult.set(
                new int[]{R.attr.statusBarBackground},
                new CcColorStateList.AnchorCallback<CoordinatorLayout>() {
                    @Override
                    public void invalidateColor(CoordinatorLayout layout, CcColorStateList color) {
                        invalidate(layout);
                    }

                    @Override
                    public void invalidateColors(CoordinatorLayout layout, Set<CcColorStateList> colors) {
                        invalidate(layout);
                    }

                    private void invalidate(CoordinatorLayout layout) {
                        layout.invalidate();
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
