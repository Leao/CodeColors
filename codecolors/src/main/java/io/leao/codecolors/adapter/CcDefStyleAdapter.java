package io.leao.codecolors.adapter;

import android.util.AttributeSet;
import android.view.View;

public interface CcDefStyleAdapter {
    /**
     * @param outResult a pair of {@code ints} representing {@code defStyleAttr} and {@code defStyleRes};
     *                  they are used to obtain attributes for the given {@code view}, when adding code color
     *                  callbacks.
     * @return {@code true} if {@code outResult} is successfully set; {@code false}, otherwise.
     */
    boolean onInflate(AttributeSet attrs, View view, InflateResult outResult);

    class InflateResult {
        int attr;
        int res;

        public void set(int attr, int res) {
            this.attr = attr;
            this.res = res;
        }

        InflateResult reuse() {
            attr = 0;
            res = 0;
            return this;
        }
    }
}
