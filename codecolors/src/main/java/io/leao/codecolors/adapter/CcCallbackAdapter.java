package io.leao.codecolors.adapter;

import android.support.annotation.NonNull;

import io.leao.codecolors.res.CcColorStateList;

public interface CcCallbackAdapter<T> {
    /**
     * Only called once, due to a caching mechanism.
     */
    @NonNull
    CcColorStateList.AnchorCallback<T> getAnchorCallback();
}
