package io.leao.codecolors.core.res;

import android.content.res.ColorStateList;

interface ColorSetter {
    /**
     * @return true, if color changed; false, otherwise.
     */
    boolean setColor(ColorStateList color);

    /**
     * @return true, if color changed; false, otherwise.
     */
    boolean setStates(int[][] states, int[] colors);

    /**
     * @return true, if color changed; false, otherwise.
     */
    boolean setState(int[] state, int color);

    /**
     * @return true, if color changed; false, otherwise.
     */
    boolean removeStates(int[][] states);

    /**
     * @return true, if color changed; false, otherwise.
     */
    boolean removeState(int[] state);
}
