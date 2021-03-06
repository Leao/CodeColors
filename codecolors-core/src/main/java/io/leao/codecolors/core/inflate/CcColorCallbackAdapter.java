package io.leao.codecolors.core.inflate;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import io.leao.codecolors.CodeColors;
import io.leao.codecolors.core.color.CodeColor;
import io.leao.codecolors.core.color.CodeColor.AnchorCallback;

/**
 * Adapter to get the list of {@link CodeColor}s and corresponding {@link AnchorCallback}s from
 * a view, when it is inflated or added manually.
 * <p>
 * Tip: initialize in {@link CodeColors.Callback#onCodeColorsStarted()}.
 */
public interface CcColorCallbackAdapter<T> {
    /**
     * Called once and its result is cached by the library if set successfully.
     * <p>
     * The default callback is used when the corresponding color callbacks are null.
     *
     * @return {@code true} if {@code outResult} is successfully set; {@code false}, otherwise.
     */
    boolean onCache(CacheResult<T> outResult);

    /**
     * Called when views are inflated from xml.
     *
     * @return {@code true} if {@code outResult} is successfully set with any colors added; {@code false}, otherwise.
     */
    boolean onInflate(AttributeSet attrs, View view, int defStyleAttr, int defStyleRes, InflateAddResult<T> outResult);

    /**
     * Called when views are added manually added to {@link io.leao.codecolors.core.inflate.CcInflateManager}. This is
     * the easiest way to add callback to views that were inflated on Java code.
     * <p>
     * Called for all {@link CcColorCallbackAdapter}s after a call to {@link CodeColors#addView(View)}.
     *
     * @return {@code true} if {@code outResult} is successfully set with any colors added; {@code false}, otherwise.
     */
    boolean onAdd(View view, InflateAddResult<T> outResult);

    interface CacheResult<T> {
        void set(@Nullable AnchorCallback<T> defaultCallback);
    }

    interface InflateAddResult<T> {
        void set(@NonNull T anchor);

        /**
         * Adds a color to which the default callback will be set.
         */
        void add(@NonNull CodeColor color);

        /**
         * If {@code callback} is {@code null}, the {@code defaultCallback} (if previously cached) will be used.
         */
        void add(@NonNull CodeColor color, @Nullable AnchorCallback<T> callback);
    }
}
