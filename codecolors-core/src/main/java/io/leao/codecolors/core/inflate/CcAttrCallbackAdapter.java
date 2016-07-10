package io.leao.codecolors.core.inflate;

import android.support.annotation.NonNull;
import android.view.View;

import io.leao.codecolors.CodeColors;
import io.leao.codecolors.core.color.CcColorStateList;
import io.leao.codecolors.core.color.CodeColor.AnchorCallback;

/**
 * Adapter to set a {@link AnchorCallback} on every {@link CcColorStateList} that is a dependency of the given
 * attributes.
 * <p>
 * For instance, if a drawable is dependent on two {@link CcColorStateList}s, a {@link AnchorCallback} is set for each
 * color.
 * <p>
 * Tip: initialize in {@link CodeColors.Callback#onCodeColorsStarted()}.
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
         * @param callback the {@link AnchorCallback} to set, if the resolved attributes are instances of
         *                 {@link CcColorStateList}.
         */
        void set(@NonNull int[] attrs, @NonNull AnchorCallback<T> callback);
    }

    interface InflateResult<T> {
        void set(@NonNull T anchor);
    }
}
