package io.leao.codecolors.sample.inflate;

import android.support.design.widget.CoordinatorLayout;
import android.view.View;

import java.util.Set;

import io.leao.codecolors.core.color.CodeColor;
import io.leao.codecolors.core.inflate.CcAttrCallbackAdapter;
import io.leao.codecolors.sample.R;

public class CcStatusBarBackgroundAttrCallbackAdapter implements CcAttrCallbackAdapter<CoordinatorLayout> {
    @Override
    public boolean onCache(CacheResult<CoordinatorLayout> outResult) {
        outResult.set(
                new int[]{R.attr.statusBarBackground},
                new CodeColor.AnchorCallback<CoordinatorLayout>() {
                    @Override
                    public void invalidateColor(CoordinatorLayout layout, CodeColor color) {
                        invalidate(layout);
                    }

                    @Override
                    public <U extends CodeColor> void invalidateColors(CoordinatorLayout layout, Set<U> colors) {
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
