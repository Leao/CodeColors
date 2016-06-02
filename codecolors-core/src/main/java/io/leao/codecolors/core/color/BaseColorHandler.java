package io.leao.codecolors.core.color;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.StateSet;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class BaseColorHandler
        implements CcColorStateList.ColorGetter<BaseColorHandler>, CcColorStateList.ColorSetter, Parcelable {
    private static Field sStateSpecsField;
    private static Field sColorsField;

    private ColorStateList mColor;
    private int[] mColors;

    private boolean mInvalidateDefaultColor;
    private Integer mDefaultColor;
    private int[] mDefaultColorState;

    private int mTransparentCount;

    public List<StateIndexValue> mStateIndexValueList;

    private BaseColorHandler(int stateCapacity) {
        mStateIndexValueList = new ArrayList<>(stateCapacity);
    }

    public BaseColorHandler() {
        this(0);
    }

    public BaseColorHandler(BaseColorHandler orig) {
        this(orig.mStateIndexValueList.size());
        setTo(orig);
    }

    public void setTo(BaseColorHandler orig) {
        mColor = orig.mColor;
        mColors = orig.mColors;

        mInvalidateDefaultColor = orig.mInvalidateDefaultColor;
        mDefaultColor = orig.mDefaultColor;
        mDefaultColorState = orig.mDefaultColorState;

        mTransparentCount = orig.mTransparentCount;

        mStateIndexValueList.clear();
        for (StateIndexValue stateIndexValue : orig.mStateIndexValueList) {
            mStateIndexValueList.add(new StateIndexValue(stateIndexValue));
        }
    }

    public BaseColorHandler(Parcel source) {
        setColor((ColorStateList) source.readParcelable(BaseColorHandler.class.getClassLoader()));
        int N = source.readInt();
        for (int i = 0; i < N; i++) {
            if (source.readByte() == 1) {
                setState(source.createIntArray(), (Integer) source.readValue(BaseColorHandler.class.getClassLoader()));
            }
        }
    }

    public ColorStateList getColor() {
        return mColor;
    }

    @Override
    public boolean setColor(ColorStateList color) {
        if (mColor != color) {
            if (color != null) {
                onNewColor(color);
            } else {
                onClearColor();
            }
            return true;
        } else {
            return false;
        }
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    private void onNewColor(ColorStateList color) {
        mColor = color;

        int[][] stateSpecs;
        try {
            if (sStateSpecsField == null) {
                sStateSpecsField = ColorStateList.class.getDeclaredField("mStateSpecs");
                sStateSpecsField.setAccessible(true);
            }
            stateSpecs = (int[][]) sStateSpecsField.get(mColor);

            if (sColorsField == null) {
                sColorsField = ColorStateList.class.getDeclaredField("mColors");
                sColorsField.setAccessible(true);
            }
            mColors = (int[]) sColorsField.get(mColor);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Failed to initialize ColorHandler", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to initialize ColorHandler", e);
        }

        // Backup previous list to restore custom states.
        List<StateIndexValue> oldStateIndexValueList = mStateIndexValueList;

        // Invalidate default color and reset transparent count.
        mInvalidateDefaultColor = true;
        mTransparentCount = 0;

        // Create new state color list.
        mStateIndexValueList = new ArrayList<>(stateSpecs.length);
        for (int i = 0; i < stateSpecs.length; i++) {
            StateIndexValue stateIndexValue = new StateIndexValue(stateSpecs[i], i, null);
            mStateIndexValueList.add(stateIndexValue);
            mTransparentCount += Color.alpha(stateIndexValue.getColor()) != 0xFF ? 1 : 0;
        }

        // Restore custom states, while ignoring old indexes.
        // Iterate from last to first, to maintain precedences when setting states.
        for (int i = oldStateIndexValueList.size() - 1; i >= 0; i--) {
            StateIndexValue stateIndexValue = oldStateIndexValueList.get(i);
            Integer colorValue = stateIndexValue.getColorValue();
            if (colorValue != null) {
                setState(stateIndexValue.getState(), colorValue);
            }
        }
    }

    protected void onClearColor() {
        mColor = null;
        mColors = null;
        mInvalidateDefaultColor = true;

        // Clear old indexes.
        Iterator<StateIndexValue> it = mStateIndexValueList.iterator();
        while (it.hasNext()) {
            StateIndexValue stateIndexValue = it.next();
            if (stateIndexValue.getColorValue() == null) {
                it.remove();
            } else {
                stateIndexValue.setColorIndex(null);
            }
        }
    }

    @Override
    public boolean setStates(int[][] states, int[] colors) {
        boolean changed = false;
        for (int i = states.length - 1; i >= 0; i--) {
            changed |= setState(states[i], colors[i]);
        }
        return changed;
    }

    @Override
    public boolean setState(int[] state, int color) {
        // Empty state potentially means a new default color.
        if (state.length == 0) {
            mInvalidateDefaultColor = true;
        }
        mTransparentCount += Color.alpha(color) != 0xFF ? 1 : 0;

        Integer addIndex = null;
        for (int i = 0; i < mStateIndexValueList.size(); i++) {
            StateIndexValue stateIndexValue = mStateIndexValueList.get(i);
            int[] stateSpec = stateIndexValue.getState();
            if (CcStateSet.equalsState(stateSpec, state)) {
                // Check if we need to update transparent count.
                Integer oldColor = stateIndexValue.getColorValue();
                boolean changed;
                if (oldColor != null) {
                    if (Color.alpha(oldColor) != 0xFF) {
                        // Decrement transparent count if the old color is transparent.
                        mTransparentCount--;
                    }
                    changed = oldColor != color;
                } else {
                    changed = true;
                }
                // Set new color.
                stateIndexValue.setColorValue(color);
                return changed;
            } else if (addIndex == null && state.length >= stateSpec.length) {
                addIndex = i;
            }
        }

        /*
         * As the state was not yet present in our list, we will add it.
         */

        // Check if the color could potentially become the default color.
        if (addIndex == null) {
            if (state.length == 0) {
                mInvalidateDefaultColor = true;
            }
        } else if (addIndex == 0) {
            mInvalidateDefaultColor = true;
        }

        // Add the new color.
        StateIndexValue stateIndexValue = new StateIndexValue(state, null, color);
        if (addIndex == null) {
            mStateIndexValueList.add(stateIndexValue);
        } else {
            mStateIndexValueList.add(addIndex, stateIndexValue);
        }

        return true;
    }

    @Override
    public boolean removeStates(int[][] states) {
        boolean changed = false;
        for (int[] state : states) {
            changed |= removeState(state);
        }
        return changed;
    }

    @Override
    public boolean removeState(int[] state) {
        for (int i = 0; i < mStateIndexValueList.size(); i++) {
            StateIndexValue stateIndexValue = mStateIndexValueList.get(i);
            if (CcStateSet.equalsState(stateIndexValue.getState(), state)) {
                if (stateIndexValue.getColorIndex() == null) {
                    mStateIndexValueList.remove(i);
                    return true;
                } else {
                    boolean changed = stateIndexValue.getColorValue() != null;
                    stateIndexValue.setColorValue(null);
                    return changed;
                }
            }
        }
        return false;
    }

    @Override
    public BaseColorHandler withAlpha(int alpha) {
        BaseColorHandler handler = new BaseColorHandler();
        if (mColor != null) {
            handler.setColor(mColor.withAlpha(alpha));
        }
        // Iterate from last to first, to maintain precedences when setting states.
        for (int i = mStateIndexValueList.size() - 1; i >= 0; i--) {
            StateIndexValue stateIndexValue = mStateIndexValueList.get(i);
            Integer colorValue = stateIndexValue.getColorValue();
            if (colorValue != null) {
                setState(stateIndexValue.getState(), withAlpha(colorValue, alpha));
            }
        }
        return handler;
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0xFFFFFF) | (alpha << 24);
    }

    @Override
    public boolean isOpaque() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mColor != null && mColor.isOpaque()) &&
                mTransparentCount == 0;
    }

    @Override
    public Integer getDefaultColor() {
        ensureDefaultColor();
        return mDefaultColor;
    }

    public int[] getDefaultColorState() {
        ensureDefaultColor();
        return mDefaultColorState;
    }

    private void ensureDefaultColor() {
        if (mInvalidateDefaultColor) {
            mInvalidateDefaultColor = false;

            if (mStateIndexValueList.size() > 0) {
                StateIndexValue defaultStateIndexValue = mStateIndexValueList.get(0);
                mDefaultColor = defaultStateIndexValue.getColor();
                mDefaultColorState = defaultStateIndexValue.getState();
                for (int i = mStateIndexValueList.size() - 1; i > 0; i--) {
                    StateIndexValue stateIndexValue = mStateIndexValueList.get(i);
                    if (stateIndexValue.getState().length == 0) {
                        mDefaultColor = stateIndexValue.getColor();
                        mDefaultColorState = stateIndexValue.getState();
                        break;
                    }
                }
            } else if (mColor != null) {
                mDefaultColor = mColor.getDefaultColor();
                mDefaultColorState = null;
            } else {
                mDefaultColor = null;
                mDefaultColorState = null;
            }
        }
    }

    @Override
    public Integer getColorForState(@Nullable int[] stateSet, Integer defaultColor) {
        for (StateIndexValue stateIndexValue : mStateIndexValueList) {
            if (StateSet.stateSetMatches(stateIndexValue.getState(), stateSet)) {
                return stateIndexValue.getColor();
            }
        }
        return defaultColor;
    }

    public class StateIndexValue {
        private int[] mState;
        private Integer mIndex;
        private Integer mValue;

        public StateIndexValue(int[] state, Integer index, Integer value) {
            mState = state;
            mIndex = index;
            mValue = value;
        }

        public StateIndexValue(StateIndexValue orig) {
            mState = orig.mState;
            mIndex = orig.mIndex;
            mValue = orig.mValue;
        }

        public int[] getState() {
            return mState;
        }

        public int getColor() {
            if (mValue != null) {
                return mValue;
            } else {
                return mColors[mIndex];
            }
        }

        public Integer getColorIndex() {
            return mIndex;
        }

        public void setColorIndex(Integer index) {
            mIndex = index;
        }

        public Integer getColorValue() {
            return mValue;
        }

        public void setColorValue(Integer value) {
            mValue = value;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mColor, flags);
        for (StateIndexValue stateIndexValue : mStateIndexValueList) {
            Integer colorValue = stateIndexValue.getColorValue();
            if (colorValue != null) {
                dest.writeByte((byte) 1);
                dest.writeIntArray(stateIndexValue.getState());
                dest.writeValue(colorValue);
            } else {
                dest.writeByte((byte) 0);
            }
        }
    }

    public static final Parcelable.Creator<BaseColorHandler> CREATOR = new Parcelable.Creator<BaseColorHandler>() {
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
