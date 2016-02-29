package io.leao.codecolors.res;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.WeakHashMap;

import io.leao.codecolors.plugin.res.CcConfiguration;

public class CcColorStateList extends ColorStateList {
    private static final int DEFAULT_COLOR = Color.RED;
    private static final int[][] EMPTY = new int[][]{new int[0]};

    private int mId = CcResources.NO_ID;

    private ColorStateList mDefaultColor = ColorStateList.valueOf(DEFAULT_COLOR);
    private CcConfigurationParcelable mConfiguration;

    private ColorStateList mColor;

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
        return getColorInternal().getDefaultColor();
    }

    public void setDefaultColor(@NonNull CcConfiguration configuration, ColorStateList defaultColor) {
        if (mConfiguration == null) {
            mConfiguration = new CcConfigurationParcelable(configuration);
        } else {
            mConfiguration.setTo(configuration);
        }
        mDefaultColor = defaultColor != null ? defaultColor : mDefaultColor;
    }

    public Integer getColor() {
        return getColorInternal().getDefaultColor();
    }

    @Override
    public int getColorForState(int[] stateSet, int defaultColor) {
        return getColorInternal().getColorForState(stateSet, defaultColor);
    }

    public void setColor(Integer color) {
        setColor(ColorStateList.valueOf(color));
    }

    public void setColor(ColorStateList color) {
        if (mColor == null || !mColor.equals(color)) {
            mColor = color;
            invalidateSelf();
        }
    }

    private ColorStateList getColorInternal() {
        return mColor != null ? mColor : mDefaultColor;
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
