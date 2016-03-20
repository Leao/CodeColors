package io.leao.codecolors.res;

import android.content.res.ColorStateList;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

class DefaultColorHandler implements ColorHandler<DefaultColorHandler>, Parcelable {
    protected BaseColorHandler mDefaultColorHandler;
    protected BaseColorHandler mColorHandler;

    protected OnColorChangedListener mOnColorChangedListener;

    public DefaultColorHandler(@NonNull BaseColorHandler defaultColorHandler,
                               @NonNull BaseColorHandler colorHandler) {
        mDefaultColorHandler = defaultColorHandler;
        mColorHandler = colorHandler;
    }

    protected DefaultColorHandler(Parcel source) {
        this((BaseColorHandler) source.readParcelable(DefaultColorHandler.class.getClassLoader()),
                (BaseColorHandler) source.readParcelable(DefaultColorHandler.class.getClassLoader()));
    }

    @Override
    public DefaultColorHandler withAlpha(int alpha) {
        return new DefaultColorHandler(mDefaultColorHandler.withAlpha(alpha), mColorHandler.withAlpha(alpha));
    }

    @Override
    public boolean isOpaque() {
        return mDefaultColorHandler.isOpaque() && mColorHandler.isOpaque();
    }

    public void setDefaultColor(ColorStateList color) {
        mDefaultColorHandler.setColor(color);
    }

    @Override
    public Integer getDefaultColor() {
        return getDefaultColorOrDefault(mColorHandler);
    }

    protected Integer getDefaultColorOrDefault(BaseColorHandler colorHandler) {
        Integer defaultColor = colorHandler != null ? colorHandler.getDefaultColor() : null;
        if (defaultColor == null) {
            defaultColor = mDefaultColorHandler.getDefaultColor();
        }
        return defaultColor;
    }

    public boolean setColor(int color) {
        return setColor(ColorStateList.valueOf(color));
    }

    public boolean setColor(ColorStateList color) {
        if (mColorHandler.setColor(color)) {
            onColorChanged();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Integer getColorForState(@Nullable int[] stateSet, Integer defaultColor) {
        return getColorForStateOrDefault(mColorHandler, stateSet, defaultColor);
    }

    protected Integer getColorForStateOrDefault(BaseColorHandler colorHandler, @Nullable int[] stateSet,
                                                Integer defaultColor) {
        Integer color = colorHandler != null ? colorHandler.getColorForState(stateSet, null) : null;
        if (color == null) {
            color = mDefaultColorHandler.getColorForState(stateSet, null);
        }
        return color != null ? color : defaultColor;
    }

    public boolean setStates(int[][] states, int[] colors) {
        if (mColorHandler.setStates(states, colors)) {
            onColorChanged();
            return true;
        } else {
            return false;
        }
    }

    public boolean setState(int[] state, int color) {
        if (mColorHandler.setState(state, color)) {
            onColorChanged();
            return true;
        } else {
            return false;
        }
    }

    public boolean removeStates(int[][] states) {
        if (mColorHandler.removeStates(states)) {
            onColorChanged();
            return true;
        } else {
            return false;
        }
    }

    public boolean removeState(int[] state) {
        if (mColorHandler.removeState(state)) {
            onColorChanged();
            return true;
        } else {
            return false;
        }
    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        mOnColorChangedListener = listener;
    }

    protected void onColorChanged() {
        if (mOnColorChangedListener != null) {
            mOnColorChangedListener.onColorChanged();
        }
    }

    public interface OnColorChangedListener {
        void onColorChanged();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mDefaultColorHandler, flags);
        dest.writeParcelable(mColorHandler, flags);
    }

    public static final Parcelable.Creator<DefaultColorHandler> CREATOR =
            new Parcelable.Creator<DefaultColorHandler>() {
                @Override
                public DefaultColorHandler[] newArray(int size) {
                    return new DefaultColorHandler[size];
                }

                @Override
                public DefaultColorHandler createFromParcel(Parcel source) {
                    return new DefaultColorHandler(source);
                }
            };
}
