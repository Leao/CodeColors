package io.leao.codecolors.core.inflate;

import android.util.AttributeSet;
import android.view.View;

import io.leao.codecolors.CodeColors;

/**
 * Adapter to get the default style attribute and resource for each view after it is inflated from xml.
 * <p>
 * The pair (attr, res) is used when resolving attributes.
 * <p>
 * Tip: initialize in {@link CodeColors.Callback#onCodeColorsStarted()}.
 */
public interface CcDefStyleAdapter {
    /**
     * @param outResult a pair of {@code ints} representing {@code defStyleAttr} and {@code defStyleRes};
     *                  they are used to obtain attributes for the given {@code view}, when adding code color
     *                  callbacks.
     * @return {@code true} if {@code outResult} is successfully set; {@code false}, otherwise.
     */
    boolean onInflate(AttributeSet attrs, View view, InflateResult outResult);

    interface InflateResult {
        void set(int attr, int res);
    }
}
