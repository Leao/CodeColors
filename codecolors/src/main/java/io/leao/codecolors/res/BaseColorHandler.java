package io.leao.codecolors.res;

import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.StateSet;

import java.lang.reflect.Field;

class BaseColorHandler implements ColorHandler<BaseColorHandler>, Parcelable {
    private static Field mStateSpecsField;
    private static Field mColorsField;

    private int[][] mStateSpecs;
    private int[] mColors;

    private ColorStateList mColor;

    public BaseColorHandler(ColorStateList color) {
        try {
            if (mStateSpecsField == null) {
                mStateSpecsField = ColorStateList.class.getDeclaredField("mStateSpecs");
                mStateSpecsField.setAccessible(true);
            }
            mStateSpecs = (int[][]) mStateSpecsField.get(color);

            if (mColorsField == null) {
                mColorsField = ColorStateList.class.getDeclaredField("mColors");
                mColorsField.setAccessible(true);
            }
            mColors = (int[]) mColorsField.get(color);

            mColor = color;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Failed to initialize ColorHandler", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to initialize ColorHandler", e);
        }
    }

    private BaseColorHandler(Parcel source) {
        this(ColorStateList.CREATOR.createFromParcel(source));
    }

    @Override
    public BaseColorHandler withAlpha(int alpha) {
        return new BaseColorHandler(mColor.withAlpha(alpha));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean isOpaque() {
        return mColor.isOpaque();
    }

    @Override
    public int getDefaultColor() {
        return mColor.getDefaultColor();
    }

    public ColorStateList getColor() {
        return mColor;
    }

    @Override
    public Integer getColorForState(@Nullable int[] stateSet, Integer defaultColor) {
        final int setLength = mStateSpecs.length;
        for (int i = 0; i < setLength; i++) {
            final int[] stateSpec = mStateSpecs[i];
            if (StateSet.stateSetMatches(stateSpec, stateSet)) {
                return mColors[i];
            }
        }
        return defaultColor;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        mColor.writeToParcel(dest, flags);
    }

    public static final Creator<BaseColorHandler> CREATOR =
            new Creator<BaseColorHandler>() {
                @Override
                public BaseColorHandler[] newArray(int size) {
                    return new BaseColorHandler[size];
                }

                @Override
                public BaseColorHandler createFromParcel(Parcel source) {
                    return new BaseColorHandler(source);
                }
            };
}
