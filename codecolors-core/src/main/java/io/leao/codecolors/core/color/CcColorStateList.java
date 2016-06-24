package io.leao.codecolors.core.color;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import java.util.Set;

import io.leao.codecolors.core.CcCore;
import io.leao.codecolors.core.editor.CcEditor;
import io.leao.codecolors.core.editor.CcEditorAnimate;
import io.leao.codecolors.core.editor.CcEditorSet;

public class CcColorStateList extends ColorStateList {
    public static final int NO_ID = 0;
    public static final int DEFAULT_ANIMATION_DURATION_MS = 400;
    public static final Interpolator DEFAULT_ANIMATION_INTERPOLATOR = new DecelerateInterpolator();

    private static final int DEFAULT_COLOR = Color.BLUE;
    private static final int[][] EMPTY = new int[][]{new int[0]};

    private int mId;
    private AnimatedBaseColorHandler mColorHandler;

    private CallbackHandlerManager mCallbackHandlerManager;

    public CcColorStateList() {
        this(NO_ID);
    }

    public CcColorStateList(int id) {
        this(id, new AnimatedBaseColorHandler());
    }

    private CcColorStateList(int id, AnimatedBaseColorHandler colorHandler) {
        super(EMPTY, new int[]{DEFAULT_COLOR});
        mId = id;
        mColorHandler = colorHandler;
        mCallbackHandlerManager = new CallbackHandlerManager(this);
    }

    private CcColorStateList(Parcel source) {
        this(source.readInt(), AnimatedBaseColorHandler.CREATOR.createFromParcel(source));
    }

    public int getId() {
        return mId;
    }

    protected void onBaseColorChanged(ColorStateList baseColor) {
        mColorHandler.setBaseColor(baseColor);
    }

    @NonNull
    @Override
    public CcColorStateList withAlpha(int alpha) {
        return new CcColorStateList(NO_ID, mColorHandler.withAlpha(alpha));
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    @Override
    public boolean isOpaque() {
        return mColorHandler.isOpaque();
    }

    @Override
    public int getDefaultColor() {
        Integer defaultColor = mColorHandler.getDefaultColor();
        return defaultColor != null ? defaultColor : DEFAULT_COLOR;
    }

    @Override
    public int getColorForState(int[] stateSet, int defaultColor) {
        return mColorHandler.getColorForState(stateSet, defaultColor);
    }

    public CcEditorSet set() {
        return CcCore.getEditorManager().getEditorSet(this);
    }

    /**
     * @return true, if the color changed; false, otherwise.
     */
    public boolean set(CcEditor<?> editor) {
        endAnimation();

        boolean changed = false;
        for (CcEditor.Edit edit : editor.getEdits()) {
            // Only account with the main color setter to check if the color changed.
            changed |= edit.apply(mColorHandler.getColorSetter());
            edit.apply(mColorHandler.getAnimationColorSetter());
        }
        return changed;
    }

    public CcEditorAnimate animate() {
        return CcCore.getEditorManager().getEditorAnimate(this);
    }

    /**
     * @return true, if the color changed and the animation started; false, otherwise.
     */
    public boolean animate(CcEditor<?> editor, ValueAnimator animation) {
        endAnimation();

        boolean changed = false;
        for (CcEditor.Edit edit : editor.getEdits()) {
            changed |= edit.apply(mColorHandler.getAnimationColorSetter());
        }

        // Set the animation to affect color.
        mColorHandler.setAnimation(animation);

        return changed;
    }

    public ValueAnimator getAnimation() {
        return mColorHandler.getAnimation();
    }

    public void endAnimation() {
        mColorHandler.endAnimation();
    }

    public void cancelAnimation() {
        mColorHandler.cancelAnimation();
    }

    public static ValueAnimator createDefaultAnimation() {
        ValueAnimator animation = ValueAnimator.ofFloat(0, 1);
        animation.setDuration(DEFAULT_ANIMATION_DURATION_MS);
        animation.setInterpolator(DEFAULT_ANIMATION_INTERPOLATOR);
        return animation;
    }

    /*
     * Callbacks.
     */

    CallbackHandlerManager getCallbackHandlerManager() {
        return mCallbackHandlerManager;
    }

    void onActivityCreated(Activity activity) {
        mCallbackHandlerManager.onActivityCreated(activity);
    }

    void onActivityResumed(Activity activity) {
        mCallbackHandlerManager.onActivityResumed(activity);
    }

    void onActivityPaused(Activity activity) {
        mCallbackHandlerManager.onActivityPaused(activity);
    }

    void onActivityDestroyed(Activity activity) {
        mCallbackHandlerManager.onActivityDestroyed(activity);
    }

    /**
     * The library will keep a weak reference to the callback.
     * <p/>
     * Make sure to maintain a strong reference while it is needed.
     *
     * @param activity if null, the library will use the most recently created or resumed activity.
     */
    public void addCallback(@Nullable Activity activity, SingleCallback callback) {
        mCallbackHandlerManager.addCallback(activity, callback);
    }

    /**
     * @param activity if null, the library will use the most recently created or resumed activity.
     */
    public boolean containsCallback(@Nullable Activity activity, CcColorStateList.SingleCallback callback) {
        return mCallbackHandlerManager.containsCallback(activity, callback);
    }

    /**
     * @param activity if null, the library will use the most recently created or resumed activity.
     */
    public void removeCallback(@Nullable Activity activity, SingleCallback callback) {
        mCallbackHandlerManager.removeCallback(activity, callback);
    }

    /**
     * The library will keep a weak reference to both callback and anchor.
     * <p/>
     * Make sure to maintain a strong reference while they are needed.
     *
     * @param activity if null, the library will use the most recently created or resumed activity.
     * @param callback the callback.
     * @param anchor   the anchor object to which the callback is dependent.
     */
    public void addAnchorCallback(@Nullable Activity activity, AnchorCallback callback, Object anchor) {
        mCallbackHandlerManager.addPairCallback(activity, callback, anchor);
    }

    /**
     * @param activity if null, the library will use the most recently created or resumed activity.
     */
    public boolean containsAnchorCallback(@Nullable Activity activity, CcColorStateList.AnchorCallback callback,
                                          Object anchor) {
        return mCallbackHandlerManager.containsPairCallback(activity, callback, anchor);
    }

    /**
     * @param activity if null, the library will use the most recently created or resumed activity.
     */
    public void removeAnchorCallback(@Nullable Activity activity, AnchorCallback callback, Object anchor) {
        mCallbackHandlerManager.removePairCallback(activity, callback, anchor);
    }

    /**
     * @param activity if null, the library will use the most recently created or resumed activity.
     */
    public void removeCallback(@Nullable Activity activity, AnchorCallback callback) {
        mCallbackHandlerManager.removeCallback(activity, callback);
    }

    /**
     * @param activity if null, the library will use the most recently created or resumed activity.
     */
    public void removeAnchor(@Nullable Activity activity, Object anchor) {
        mCallbackHandlerManager.removeAnchor(activity, anchor);
    }

    public void invalidateSelf() {
        CcCore.getColorManager().invalidate(this);
    }

    /*
     * Interfaces.
     */

    public interface SingleCallback extends Callback {
        void invalidateColor(CcColorStateList color);

        void invalidateColors(Set<CcColorStateList> colors);
    }

    public interface AnchorCallback<T> extends Callback {
        void invalidateColor(T anchor, CcColorStateList color);

        void invalidateColors(T anchor, Set<CcColorStateList> colors);
    }

    interface Callback {
    }

    public interface ColorGetter<T extends ColorGetter> {
        T withAlpha(int alpha);

        boolean isOpaque();

        Integer getDefaultColor();

        Integer getColorForState(@Nullable int[] stateSet, Integer defaultColor);
    }

    public interface ColorSetter {
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

    /*
     * Parcelable.
     */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);

        mColorHandler.writeToParcel(dest, flags);
    }

    public static final Parcelable.Creator<CcColorStateList> CREATOR = new Parcelable.Creator<CcColorStateList>() {
        @Override
        public CcColorStateList[] newArray(int size) {
            return new CcColorStateList[size];
        }

        @Override
        public CcColorStateList createFromParcel(Parcel source) {
            return new CcColorStateList(source);
        }
    };

    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }
}
