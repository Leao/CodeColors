package io.leao.codecolors.core.res;

import android.content.res.ColorStateList;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

class DefaultColorHandler implements ColorHandler<DefaultColorHandler>, Parcelable {
    protected BaseColorHandler mDefaultColorHandler;
    protected BaseColorHandler mColorHandler;

    protected SetBuilder mSetBuilder;

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

    public CcColorStateList.SetBuilder set() {
        if (mSetBuilder == null) {
            mSetBuilder = new SetBuilder(mColorHandler, new SetBuilder.Callback() {
                @Override
                public void onSubmit(SetBuilder builder) {
                    onSet(builder.hasChanged());
                }
            });
        }
        return mSetBuilder;
    }

    protected void onSet(boolean changed) {
        if (changed) {
            onColorChanged();
        }
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

    public static class SetBuilder extends Builder<SetBuilder> implements CcColorStateList.SetBuilder {

        public SetBuilder(BaseColorHandler baseColorHandler, Callback callback) {
            super(baseColorHandler, callback);
        }

        interface Callback extends Builder.Callback<SetBuilder> {
        }
    }

    @SuppressWarnings("unchecked")
    protected static class Builder<T extends Builder> {
        protected BaseColorHandler mBaseColorHandler;
        private Callback<T> mCallback;

        private boolean mChanged;

        public Builder(BaseColorHandler baseColorHandler, Callback<T> callback) {
            mBaseColorHandler = baseColorHandler;
            mCallback = callback;
        }

        public T setColor(int color) {
            return setColor(ColorStateList.valueOf(color));
        }

        public T setColor(ColorStateList color) {
            mChanged |= mBaseColorHandler.setColor(color);
            return (T) this;
        }

        public T setStates(int[][] states, int[] colors) {
            mChanged |= mBaseColorHandler.setStates(states, colors);
            return (T) this;
        }

        public T setState(int[] state, int color) {
            mChanged |= mBaseColorHandler.setState(state, color);
            return (T) this;
        }

        public T removeStates(int[][] states) {
            mChanged |= mBaseColorHandler.removeStates(states);
            return (T) this;
        }

        public T removeState(int[] state) {
            mChanged |= mBaseColorHandler.removeState(state);
            return (T) this;
        }

        public void submit() {
            mCallback.onSubmit((T) this);
        }

        public boolean hasChanged() {
            return mChanged;
        }

        interface Callback<T extends Builder> {
            void onSubmit(T builder);
        }
    }
}
