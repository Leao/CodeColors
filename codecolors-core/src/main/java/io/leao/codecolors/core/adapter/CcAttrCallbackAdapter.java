package io.leao.codecolors.core.adapter;

import android.support.annotation.NonNull;
import android.view.View;

import io.leao.codecolors.core.color.CcColorStateList;

/**
 * Adapter to set a {@link CcColorStateList.AnchorCallback} on every {@link CcColorStateList} that is a dependency of
 * the given attributes.
 * <p>
 * For instance, if a drawable is dependent on two {@link CcColorStateList}s, a {@link CcColorStateList.AnchorCallback}
 * is set for each color.
 */
public interface CcAttrCallbackAdapter<T> {
    /**
     * Called once and its result is cached by the library to be used with the given attributes.
     *
     * @return {@code true} if {@code outResult} is successfully set; {@code false}, otherwise.
     */
    boolean onCache(CacheResult<T> outResult);

    /**
     * @return {@code true} if {@code outResult} is successfully set; {@code false}, otherwise.
     */
    boolean onInflate(View view, int attr, InflateResult<T> outResult);

    interface CacheResult<T> {
        /**
         * @param attrs    the attributes to be resolved on inflation; if the resolved attributes are instances of
         *                 {@link CcColorStateList}, the callback is set.
         * @param callback the {@link CcColorStateList.AnchorCallback} to set, if the resolved attributes are instances
         *                 of {@link CcColorStateList}.
         */
        void set(@NonNull int[] attrs, @NonNull CcColorStateList.AnchorCallback<T> callback);
    }

    interface InflateResult<T> {
        void set(@NonNull T anchor);
    }
}
