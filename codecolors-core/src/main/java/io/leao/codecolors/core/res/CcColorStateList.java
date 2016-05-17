package io.leao.codecolors.core.res;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.animation.Interpolator;

import java.util.WeakHashMap;

import io.leao.codecolors.plugin.res.CcConfiguration;

public class CcColorStateList extends ColorStateList {
    public static final int NO_ID = 0;

    private static final int DEFAULT_COLOR = Color.BLUE;
    private static final int[][] EMPTY = new int[][]{new int[0]};

    private int mId;
    private AnimatedDefaultColorHandler mColorHandler;
    private CcConfigurationParcelable mConfiguration;

    private CallbackHandler mCallbackHandler = new CallbackHandler();

    public CcColorStateList() {
        this(NO_ID);
    }

    public CcColorStateList(int id) {
        this(id, new AnimatedDefaultColorHandler());
    }

    private CcColorStateList(int id, AnimatedDefaultColorHandler colorHandler) {
        super(EMPTY, new int[]{DEFAULT_COLOR});
        mId = id;
        mColorHandler = colorHandler;
        mColorHandler.setOnColorChangedListener(new DefaultColorHandler.OnColorChangedListener() {
            @Override
            public void onColorChanged() {
                invalidateSelf();
            }
        });
    }

    private CcColorStateList(Parcel source) {
        this(source.readInt(), AnimatedDefaultColorHandler.CREATOR.createFromParcel(source));
        if (source.readByte() == 1) {
            mConfiguration = CcConfigurationParcelable.CREATOR.createFromParcel(source);
        }
    }

    public int getId() {
        return mId;
    }

    public CcConfiguration getConfiguration() {
        return mConfiguration;
    }

    public void onConfigurationChanged(CcConfiguration configuration, ColorStateList defaultColor) {
        if (configuration == null) {
            mConfiguration = null;
        } else if (mConfiguration == null) {
            mConfiguration = new CcConfigurationParcelable(configuration);
        } else {
            mConfiguration.setTo(configuration);
        }

        mColorHandler.setDefaultColor(defaultColor);
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

    public SetBuilder set() {
        return mColorHandler.set();
    }

    public AnimateBuilder animate() {
        return mColorHandler.animate();
    }

    public void endAnimation() {
        // End animation, without blocking the color change call.
        mColorHandler.endAnimation(false);
    }

    /**
     * The added callback is kept in a {@code set} generated from a {@link WeakHashMap}.
     * <p>
     * That means the callback should be an object used by the application, like a
     * {@link android.graphics.drawable.Drawable} or a {@link android.view.View}.
     */
    public void addCallback(Callback callback) {
        mCallbackHandler.addCallback(callback);
    }

    public void removeCallback(Callback callback) {
        mCallbackHandler.removeCallback(callback);
    }

    /**
     * The added anchor is the key of a {@link WeakHashMap}.
     * <p>
     * That means the anchor should be an object used by the application, like a
     * {@link android.graphics.drawable.Drawable} or a {@link android.view.View}.
     * <p>
     * Refrain from keeping a reference to the {@code anchor} in your {@code callback}. Otherwise, it might generate a
     * memory leak.
     *
     * @param anchor   the anchor object to which the callback is dependent.
     * @param callback the callback.
     */
    public void addAnchorCallback(Object anchor, AnchorCallback callback) {
        mCallbackHandler.addAnchorCallback(anchor, callback);
    }

    public void removeAnchor(AnchorCallback callback) {
        mCallbackHandler.removeAnchor(callback);
    }

    public void removeCallback(AnchorCallback callback) {
        mCallbackHandler.removeCallback(callback);
    }

    public void invalidateSelf() {
        mCallbackHandler.invalidateColor(this);
    }

    public interface SetBuilder extends Builder<SetBuilder> {
    }

    public interface AnimateBuilder extends Builder<AnimateBuilder> {
        AnimateBuilder setAnimation(ValueAnimator animation);

        AnimateBuilder setDuration(int duration);

        AnimateBuilder setInterpolator(Interpolator interpolator);

        AnimateBuilder setCallback(AnimationCallback callback);
    }

    protected interface Builder<T extends Builder> {
        T setColor(int color);

        T setColor(ColorStateList color);

        T setStates(int[][] states, int[] colors);

        T setState(int[] state, int color);

        T removeStates(int[][] states);

        T removeState(int[] state);

        void submit();
    }

    public interface Callback {
        void invalidateColor(CcColorStateList color);
    }

    public interface AnchorCallback<T> {
        void invalidateColor(T anchor, CcColorStateList color);
    }

    public abstract class AnimationCallback
            implements ValueAnimator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {

        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public abstract class AnimationCallbackKitKat extends AnimationCallback
            implements ValueAnimator.AnimatorPauseListener {

        @Override
        public void onAnimationPause(Animator animation) {
        }

        @Override
        public void onAnimationResume(Animator animation) {
        }
    }

    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);

        mColorHandler.writeToParcel(dest, flags);

        if (mConfiguration != null) {
            dest.writeByte((byte) 1);
            mConfiguration.writeToParcel(dest, 0);
        } else {
            dest.writeByte((byte) 0);
        }
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
