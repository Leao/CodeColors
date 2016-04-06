package io.leao.codecolors.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import io.leao.codecolors.res.CcColorStateList;

public interface CcColorCallbackAdapter<T> {
    /**
     * Caches a default callback, to be used when the corresponding color callbacks are null.
     *
     * @return {@code true} if {@code outResult} is successfully set; {@code false}, otherwise.
     */
    boolean onCache(CacheResult<T> outResult);

    /**
     * @return {@code true} if {@code outResult} is successfully set; {@code false}, otherwise.
     */
    boolean onInflate(AttributeSet attrs, View view, int defStyleAttr, int defStyleRes, InflateResult<T> outResult);

    class CacheResult<T> {
        CcColorStateList.AnchorCallback<T> defaultCallback;

        public void set(@Nullable CcColorStateList.AnchorCallback<T> defaultCallback) {
            this.defaultCallback = defaultCallback;
        }

        CacheResult reuse() {
            defaultCallback = null;
            return this;
        }
    }

    class InflateResult<T> {
        T anchor;
        List<CcColorStateList> colors = new ArrayList<>();
        List<CcColorStateList.AnchorCallback<T>> callbacks = new ArrayList<>();

        public void set(@NonNull T anchor) {
            this.anchor = anchor;
        }

        /**
         * Adds a color to which will be added the default callback.
         */
        public void add(@NonNull CcColorStateList color) {
            add(color, null);
        }

        /**
         * If {@code callback} is {@code null}, the {@code defaultCallback} (if previously cached) will be used.
         */
        public void add(@NonNull CcColorStateList color, @Nullable CcColorStateList.AnchorCallback<T> callback) {
            colors.add(color);
            callbacks.add(callback);
        }

        InflateResult reuse() {
            anchor = null;
            colors.clear();
            callbacks.clear();
            return this;
        }
    }
}
