package io.leao.codecolors.res;

import android.content.res.ColorStateList;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

class DefaultColorHandler implements ColorHandler<DefaultColorHandler>, Parcelable {
    protected BaseColorHandler mDefaultColorHandler;
    protected BaseColorHandler mColorHandler;

    protected OnColorChangedListener mOnColorChangedListener;

    protected DefaultColorHandler(@Nullable BaseColorHandler defaultColorHandler,
                                  @Nullable BaseColorHandler colorHandler) {
        mDefaultColorHandler = defaultColorHandler;
        mColorHandler = colorHandler;
    }

    protected DefaultColorHandler(Parcel source) {
        this(source.readByte() == 1 ? BaseColorHandler.CREATOR.createFromParcel(source) : null,
                source.readByte() == 1 ? BaseColorHandler.CREATOR.createFromParcel(source) : null);
    }

    @Override
    public DefaultColorHandler withAlpha(int alpha) {
        return new DefaultColorHandler(
                mDefaultColorHandler != null ? mDefaultColorHandler.withAlpha(alpha) : null,
                mColorHandler != null ? mColorHandler.withAlpha(alpha) : null);
    }

    @Override
    public boolean isOpaque() {
        return (mDefaultColorHandler == null || mDefaultColorHandler.isOpaque()) &&
                (mColorHandler == null || mColorHandler.isOpaque());
    }

    public void setDefaultColor(ColorStateList color) {
        mDefaultColorHandler = new BaseColorHandler(color);
    }

    @Override
    public int getDefaultColor() {
        return getDefaultColorOrDefault(mColorHandler);
    }

    protected int getDefaultColorOrDefault(BaseColorHandler colorHandler) {
        return colorHandler != null ? colorHandler.getDefaultColor() : mDefaultColorHandler.getDefaultColor();
    }

    protected boolean isChangingColor(ColorStateList color) {
        if (color == null) {
            return mColorHandler != null;
        } else {
            return mColorHandler == null || mColorHandler.getColor() != color;
        }
    }

    public void setColor(ColorStateList color) {
        if (isChangingColor(color)) {
            mColorHandler = color != null ? new BaseColorHandler(color) : null;
            mOnColorChangedListener.onColorChanged();
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
            color = mDefaultColorHandler.getColorForState(stateSet, defaultColor);
        }
        return color;
    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        mOnColorChangedListener = listener;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        writeColorHandlerToParcel(mDefaultColorHandler, dest, flags);
        writeColorHandlerToParcel(mColorHandler, dest, flags);
    }

    private void writeColorHandlerToParcel(BaseColorHandler handler, Parcel dest, int flags) {
        if (handler != null) {
            dest.writeByte((byte) 1);
            handler.writeToParcel(dest, flags);
        } else {
            dest.writeByte((byte) 0);
        }
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

    public interface OnColorChangedListener {
        void onColorChanged();
    }
}
