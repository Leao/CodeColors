package io.leao.codecolors.adapter;

import android.support.annotation.NonNull;
import android.view.View;

import io.leao.codecolors.res.CcColorStateList;

public interface CcAttrCallbackAdapter<T> {
    /**
     * Called once and cached by the library to be used with the given attributes.
     *
     * @return {@code true} if {@code outResult} is successfully set; {@code false}, otherwise.
     */
    boolean onCache(CacheResult<T> outResult);

    /**
     * @return {@code true} if {@code outResult} is successfully set; {@code false}, otherwise.
     */
    boolean onInflate(View view, int attr, InflateResult<T> outResult);

    class CacheResult<T> {
        int[] attrs;
        CcColorStateList.AnchorCallback<T> callback;

        public void set(@NonNull int[] attrs, @NonNull CcColorStateList.AnchorCallback<T> callback) {
            this.attrs = attrs;
            this.callback = callback;
        }

        CacheResult reuse() {
            attrs = null;
            callback = null;
            return this;
        }
    }

    class InflateResult<T> {
        T anchor;

        public void set(@NonNull T anchor) {
            this.anchor = anchor;
        }

        InflateResult reuse() {
            anchor = null;
            return this;
        }
    }
}
