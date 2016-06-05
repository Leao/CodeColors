package io.leao.codecolors.core.color;

import android.content.res.ColorStateList;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Varies its color depending on the color of a main color handler, but falls back to the color of a secondary color
 * handler, when the main one has no value for the current state.
 * <p>
 * The secondary color handler cannot be edited.
 */
class BaseColorHandler implements CcColorStateList.ColorGetter<BaseColorHandler>, Parcelable {
    protected ColorHandler mBaseColorHandler;
    protected ColorHandler mColorHandler;

    public BaseColorHandler(@NonNull ColorHandler baseColorHandler,
                            @NonNull ColorHandler colorHandler) {
        mBaseColorHandler = baseColorHandler;
        mColorHandler = colorHandler;
    }

    protected BaseColorHandler(Parcel source) {
        this((ColorHandler) source.readParcelable(BaseColorHandler.class.getClassLoader()),
                (ColorHandler) source.readParcelable(BaseColorHandler.class.getClassLoader()));
    }

    public CcColorStateList.ColorSetter getColorSetter() {
        return mColorHandler;
    }

    @Override
    public BaseColorHandler withAlpha(int alpha) {
        return new BaseColorHandler(mBaseColorHandler.withAlpha(alpha), mColorHandler.withAlpha(alpha));
    }

    @Override
    public boolean isOpaque() {
        return mBaseColorHandler.isOpaque() && mColorHandler.isOpaque();
    }

    public void setBaseColor(ColorStateList color) {
        if (mBaseColorHandler.getColor() != color) {
            mBaseColorHandler.setColor(color);
        }
    }

    @Override
    public Integer getDefaultColor() {
        return getDefaultColorFromHandlerOrBaseHandler(mColorHandler);
    }

    protected Integer getDefaultColorFromHandlerOrBaseHandler(ColorHandler colorHandler) {
        // Return colorHandler default color when:
        // - its default color is not null
        // - its default state's length is 0
        // - its default state's length is not 0, but the base handler default state's length is also not 0.
        Integer handlerDefaultColor = colorHandler != null ? colorHandler.getDefaultColor() : null;
        if (handlerDefaultColor != null) {
            int[] handlerDefaultState = colorHandler.getDefaultColorState();
            if (handlerDefaultState == null || handlerDefaultState.length == 0) {
                return handlerDefaultColor;
            } else {
                int[] baseHandlerDefaultState = mBaseColorHandler.getDefaultColorState();
                if (baseHandlerDefaultState == null || baseHandlerDefaultState.length != 0) {
                    return handlerDefaultColor;
                }
            }
        }
        return mBaseColorHandler.getDefaultColor();
    }

    @Override
    public Integer getColorForState(@Nullable int[] stateSet, Integer defaultColor) {
        return getColorForStateFromHandlerOrBaseHandler(mColorHandler, stateSet, defaultColor);
    }

    protected Integer getColorForStateFromHandlerOrBaseHandler(ColorHandler colorHandler, @Nullable int[] stateSet,
                                                               Integer defaultColor) {
        Integer color = colorHandler != null ? colorHandler.getColorForState(stateSet, null) : null;
        if (color == null) {
            color = mBaseColorHandler.getColorForState(stateSet, null);
        }
        return color != null ? color : defaultColor;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mBaseColorHandler, flags);
        dest.writeParcelable(mColorHandler, flags);
    }

    public static final Parcelable.Creator<BaseColorHandler> CREATOR =
            new Parcelable.Creator<BaseColorHandler>() {
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
