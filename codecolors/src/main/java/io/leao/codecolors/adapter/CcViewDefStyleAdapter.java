package io.leao.codecolors.adapter;

import android.util.AttributeSet;
import android.view.View;

public interface CcViewDefStyleAdapter {
    /**
     * @param outDefStyle a pair of {@code ints} representing {@code defStyleAttr} and {@code defStyleRes};
     *                    they are used to obtain attributes for the given {@code view}, when adding code color
     *                    callbacks.
     *
     * @return {@code true} if {@code outDefStyle} is filled with valid attr and res; {@code false}, otherwise.
     */
    boolean getDefStyle(AttributeSet attrs, View view, DefStyle outDefStyle);

    class DefStyle {
        public int attr;
        public int res;
    }
}
