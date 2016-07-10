package io.leao.codecolors.core.color;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import io.leao.codecolors.core.CcCore;
import io.leao.codecolors.core.editor.CcEditor;
import io.leao.codecolors.core.editor.CcEditorAnimate;
import io.leao.codecolors.core.editor.CcEditorSet;

public class CcColorStateList extends ColorStateList implements CodeColor {
    public static final int DEFAULT_ANIMATION_DURATION_MS = 400;
    public static final Interpolator DEFAULT_ANIMATION_INTERPOLATOR = new DecelerateInterpolator();

    protected int mId;
    protected AnimatedBaseColorHandler mColorHandler;

    protected CallbackHandlerManager mCallbackHandlerManager;

    public CcColorStateList() {
        this(NO_ID);
    }

    public CcColorStateList(int id) {
        this(id, new AnimatedBaseColorHandler());
    }

    protected CcColorStateList(int id, AnimatedBaseColorHandler colorHandler) {
        super(EMPTY_STATES, DEFAULT_COLORS);
        mId = id;
        mColorHandler = colorHandler;
        mCallbackHandlerManager = new CallbackHandlerManager(this);
    }

    protected CcColorStateList(Parcel source) {
        this(NO_ID, AnimatedBaseColorHandler.CREATOR.createFromParcel(source));
    }

    protected void onBaseColorChanged(ColorStateList baseColor) {
        mColorHandler.setBaseColor(baseColor);
    }

    @Override
    public int getId() {
        return mId;
    }

    @NonNull
    @Override
    public CcColorStateList withAlpha(int alpha) {
        return new CcColorStateList(NO_ID, mColorHandler.withAlpha(alpha));
    }

    //@Override
    public boolean canApplyTheme() {
        return false;
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
    public int getColorForState(int[] stateSet, int defaultColor) {
        return mColorHandler.getColorForState(stateSet, defaultColor);
    }

    @Override
    public int getDefaultColor() {
        Integer defaultColor = mColorHandler.getDefaultColor();
        return defaultColor != null ? defaultColor : DEFAULT_COLOR;
    }

    @Override
    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }

    public void invalidateSelf() {
        CcCore.getColorManager().invalidate(this);
    }

    /*
     * Setters and animation setters.
     */

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
     * Activity lifecycle.
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

    /*
     * Callbacks.
     */

    @Override
    public void addCallback(@Nullable Activity activity, SingleCallback callback) {
        mCallbackHandlerManager.addCallback(activity, callback);
    }

    @Override
    public boolean containsCallback(@Nullable Activity activity, SingleCallback callback) {
        return mCallbackHandlerManager.containsCallback(activity, callback);
    }

    @Override
    public void removeCallback(@Nullable Activity activity, SingleCallback callback) {
        mCallbackHandlerManager.removeCallback(activity, callback);
    }

    @Override
    public void addAnchorCallback(@Nullable Activity activity, Object anchor, AnchorCallback callback) {
        mCallbackHandlerManager.addPairCallback(activity, anchor, callback);
    }

    @Override
    public boolean containsAnchorCallback(@Nullable Activity activity, Object anchor, AnchorCallback callback) {
        return mCallbackHandlerManager.containsPairCallback(activity, anchor, callback);
    }

    @Override
    public void removeAnchorCallback(@Nullable Activity activity, Object anchor, AnchorCallback callback) {
        mCallbackHandlerManager.removePairCallback(activity, anchor, callback);
    }

    @Override
    public void removeAnchor(@Nullable Activity activity, Object anchor) {
        mCallbackHandlerManager.removeAnchor(activity, anchor);
    }

    @Override
    public void removeCallback(@Nullable Activity activity, AnchorCallback callback) {
        mCallbackHandlerManager.removeCallback(activity, callback);
    }

    /*
     * Interfaces.
     */

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
}
