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
class DefaultColorHandler implements ColorGetter<DefaultColorHandler>, Parcelable {
    protected BaseColorHandler mDefaultColorHandler;
    protected BaseColorHandler mColorHandler;

    public DefaultColorHandler(@NonNull BaseColorHandler defaultColorHandler,
                               @NonNull BaseColorHandler colorHandler) {
        mDefaultColorHandler = defaultColorHandler;
        mColorHandler = colorHandler;
    }

    protected DefaultColorHandler(Parcel source) {
        this((BaseColorHandler) source.readParcelable(DefaultColorHandler.class.getClassLoader()),
                (BaseColorHandler) source.readParcelable(DefaultColorHandler.class.getClassLoader()));
    }

    public ColorSetter getColorSetter() {
        return mColorHandler;
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
        return getDefaultColorFromHandlerOrDefaultHandler(mColorHandler);
    }

    protected Integer getDefaultColorFromHandlerOrDefaultHandler(BaseColorHandler colorHandler) {
        // Return handler default color when:
        // - its default color is not null
        // - its default state's length is 0
        // - its default state's length is not 0, but the default handler default state's length is also not 0.
        Integer handlerDefaultColor = colorHandler != null ? colorHandler.getDefaultColor() : null;
        if (handlerDefaultColor != null) {
            int[] handlerDefaultState = colorHandler.getDefaultColorState();
            if (handlerDefaultState == null || handlerDefaultState.length == 0) {
                return handlerDefaultColor;
            } else {
                int[] defaultHandlerDefaultState = mDefaultColorHandler.getDefaultColorState();
                if (defaultHandlerDefaultState == null || defaultHandlerDefaultState.length != 0) {
                    return handlerDefaultColor;
                }
            }
        }
        return mDefaultColorHandler.getDefaultColor();
    }

    @Override
    public Integer getColorForState(@Nullable int[] stateSet, Integer defaultColor) {
        return getColorForStateFromHandlerOrDefaultHandler(mColorHandler, stateSet, defaultColor);
    }

    protected Integer getColorForStateFromHandlerOrDefaultHandler(BaseColorHandler colorHandler,
                                                                  @Nullable int[] stateSet,
                                                                  Integer defaultColor) {
        Integer color = colorHandler != null ? colorHandler.getColorForState(stateSet, null) : null;
        if (color == null) {
            color = mDefaultColorHandler.getColorForState(stateSet, null);
        }
        return color != null ? color : defaultColor;
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
