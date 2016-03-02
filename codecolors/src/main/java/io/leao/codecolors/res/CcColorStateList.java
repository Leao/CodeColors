package io.leao.codecolors.res;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.animation.DecelerateInterpolator;

import java.util.WeakHashMap;

import io.leao.codecolors.plugin.res.CcConfiguration;

public class CcColorStateList extends ColorStateList
        implements ValueAnimator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {

    private static final int DEFAULT_COLOR = Color.RED;
    private static final int[][] EMPTY = new int[][]{new int[0]};

    private int mId = CcResources.NO_ID;

    private ColorStateList mDefaultColor = ColorStateList.valueOf(DEFAULT_COLOR);
    private CcConfigurationParcelable mConfiguration;

    private ColorStateList mColor;

    private ValueAnimator mAnimator;
    private ColorStateList mAnimationColor;

    protected WeakHashMap<Callback, Object> mCallbacks = new WeakHashMap<>();
    protected WeakHashMap<Object, AnchorCallback> mAnchorCallbacks = new WeakHashMap<>();

    public CcColorStateList() {
        super(EMPTY, new int[]{DEFAULT_COLOR});
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    public void setId(int id) {
        mId = id;
    }

    public int getId() {
        return mId;
    }

    public CcConfiguration getConfiguration() {
        return mConfiguration;
    }

    @Override
    public int getDefaultColor() {
        int color = getColorInternal().getDefaultColor();
        if (mAnimationColor == null) {
            return color;
        } else {
            return interpolate(color, mAnimationColor.getDefaultColor(), (float) mAnimator.getAnimatedValue());
        }
    }

    public void setDefaultColor(@NonNull CcConfiguration configuration, ColorStateList defaultColor) {
        if (mConfiguration == null) {
            mConfiguration = new CcConfigurationParcelable(configuration);
        } else {
            mConfiguration.setTo(configuration);
        }
        mDefaultColor = defaultColor != null ? defaultColor : mDefaultColor;
    }

    @Override
    public int getColorForState(int[] stateSet, int defaultColor) {
        int color = getColorInternal().getColorForState(stateSet, defaultColor);
        if (mAnimationColor == null) {
            return color;
        } else {
            return interpolate(
                    color,
                    mAnimationColor.getColorForState(stateSet, defaultColor),
                    (float) mAnimator.getAnimatedValue());
        }
    }

    public void setColor(int color) {
        setColor(ColorStateList.valueOf(color));
    }

    public void setColor(ColorStateList color) {
        endAnimation();
        setColorInternal(color);
    }

    private void setColorInternal(ColorStateList color) {
        if (mColor == null || !mColor.equals(color)) {
            mColor = color;
            invalidateSelf();
        }
        // Clear animation color.
        mAnimationColor = null;
    }

    private ColorStateList getColorInternal() {
        return mColor != null ? mColor : mDefaultColor;
    }

    public void animateTo(int color) {
        animateTo(ColorStateList.valueOf(color));
    }

    public void animateTo(ColorStateList color) {
        if (mAnimator == null) {
            mAnimator = ValueAnimator.ofFloat(0, 1);
            mAnimator.setDuration(400);
            mAnimator.setInterpolator(new DecelerateInterpolator());
            mAnimator.addUpdateListener(this);
            mAnimator.addListener(this);
        } else {
            endAnimation();
        }

        mAnimationColor = color;
        mAnimator.start();
    }

    private void endAnimation() {
        if (mAnimator != null && mAnimator.isStarted()) {
            mAnimator.end();
        }
    }

    private int interpolate(int startColor, int endColor, float fraction) {
        if (startColor != endColor) {
            int startA = (startColor >> 24) & 0xff;
            int startR = (startColor >> 16) & 0xff;
            int startG = (startColor >> 8) & 0xff;
            int startB = startColor & 0xff;
            int endA = (endColor >> 24) & 0xff;
            int endR = (endColor >> 16) & 0xff;
            int endG = (endColor >> 8) & 0xff;
            int endB = endColor & 0xff;
            return ((startA + (int) (fraction * (endA - startA))) << 24) |
                    ((startR + (int) (fraction * (endR - startR))) << 16) |
                    ((startG + (int) (fraction * (endG - startG))) << 8) |
                    ((startB + (int) (fraction * (endB - startB))));
        } else {
            return startColor;
        }
    }

    @Override
    public void onAnimationStart(Animator animation) {
        // Do nothing.
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        setColorInternal(mAnimationColor);
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        setColorInternal(mAnimationColor);
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
        // Do nothing.
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        invalidateSelf();
    }

    public void addCallback(Callback callback) {
        mCallbacks.put(callback, null);
    }

    public void removeCallback(Callback callback) {
        mCallbacks.remove(callback);
    }

    /**
     * @param anchor   the anchor object to which the callback is dependent.
     * @param callback the callback.
     */
    public void addCallback(Object anchor, AnchorCallback callback) {
        mAnchorCallbacks.put(anchor, callback);
    }

    public void removeCallback(AnchorCallback callback) {
        mAnchorCallbacks.remove(callback);
    }

    @SuppressWarnings("unchecked")
    public void invalidateSelf() {
        for (Callback callback : mCallbacks.keySet()) {
            if (callback != null) {
                callback.invalidateColor(this);
            }
        }

        for (Object anchor : mAnchorCallbacks.keySet()) {
            AnchorCallback callback = mAnchorCallbacks.get(anchor);
            if (callback != null) {
                callback.invalidateColor(anchor, this);
            }
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (mConfiguration != null) {
            dest.writeByte((byte) 1);
            mConfiguration.writeToParcel(dest, 0);
            mDefaultColor.writeToParcel(dest, 0);
        } else {
            dest.writeByte((byte) 0);
        }

        if (mColor != null) {
            dest.writeByte((byte) 1);
            mColor.writeToParcel(dest, 0);
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
            CcColorStateList cccsl = new CcColorStateList();
            if (source.readByte() == 1) {
                cccsl.setDefaultColor(
                        CcConfigurationParcelable.CREATOR.createFromParcel(source),
                        ColorStateList.CREATOR.createFromParcel(source));
            }
            if (source.readByte() == 1) {
                cccsl.setColor(ColorStateList.CREATOR.createFromParcel(source));
            }
            return cccsl;
        }
    };

    public interface Callback {
        void invalidateColor(CcColorStateList color);
    }

    public interface AnchorCallback<T> {
        void invalidateColor(T anchor, CcColorStateList color);
    }

    @Override
    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }
}
