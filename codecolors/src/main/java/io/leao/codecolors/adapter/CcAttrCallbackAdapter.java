package io.leao.codecolors.adapter;

import android.support.annotation.NonNull;
import android.view.View;

public interface CcAttrCallbackAdapter<T> extends CcCallbackAdapter<T> {
    /**
     * Only called once, due to a caching mechanism.
     */
    @NonNull
    int[] getAttrs();

    T getAnchor(View view, int attr);
}
