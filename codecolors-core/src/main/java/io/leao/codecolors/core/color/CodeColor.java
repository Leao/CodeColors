package io.leao.codecolors.core.color;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.Nullable;

import java.util.Set;

/**
 * Dynamic {@link ColorStateList}s that can take callbacks to listen to their changes.
 */
public interface CodeColor {
    int NO_ID = 0;
    int[][] EMPTY_STATES = new int[][]{new int[0]};
    int DEFAULT_COLOR = Color.BLUE;
    int[] DEFAULT_COLORS = new int[]{DEFAULT_COLOR};

    int getId();

    int getDefaultColor();

    int getColorForState(int[] stateSet, int defaultColor);

    /**
     * The library will keep a weak reference to the callback.
     * <p>
     * Make sure to maintain a strong reference while it is needed.
     *
     * @param activity if null, the library will use the most recently created or resumed activity.
     */
    void addCallback(@Nullable Activity activity, SingleCallback callback);

    /**
     * @param activity if null, the library will use the most recently created or resumed activity.
     */
    boolean containsCallback(@Nullable Activity activity, SingleCallback callback);

    /**
     * @param activity if null, the library will use the most recently created or resumed activity.
     */
    void removeCallback(@Nullable Activity activity, SingleCallback callback);

    /**
     * The library will keep a weak reference to the anchor and a strong reference to the callback.
     * <p>
     * The callback is dependent on the anchor. Once, the anchor is GC'ed, the callback will also get removed.
     * <p>
     * Make sure to maintain a strong reference to the anchor, while it is needed.
     *
     * @param activity if null, the library will use the most recently created or resumed activity.
     * @param anchor   the anchor object to which the callback is dependent.
     * @param callback the callback.
     */
    void addAnchorCallback(@Nullable Activity activity, Object anchor, AnchorCallback callback);

    /**
     * @param activity if null, the library will use the most recently created or resumed activity.
     */
    boolean containsAnchorCallback(@Nullable Activity activity, Object anchor, AnchorCallback callback);

    /**
     * @param activity if null, the library will use the most recently created or resumed activity.
     */
    void removeAnchorCallback(@Nullable Activity activity, Object anchor, AnchorCallback callback);

    /**
     * @param activity if null, the library will use the most recently created or resumed activity.
     */
    void removeAnchor(@Nullable Activity activity, Object anchor);

    /**
     * @param activity if null, the library will use the most recently created or resumed activity.
     */
    void removeCallback(@Nullable Activity activity, AnchorCallback callback);

    interface SingleCallback {
        void invalidateColor(CodeColor color);

        <T extends CodeColor> void invalidateColors(Set<T> colors);
    }

    interface AnchorCallback<T> {
        void invalidateColor(T anchor, CodeColor color);

        <U extends CodeColor> void invalidateColors(T anchor, Set<U> colors);
    }
}
