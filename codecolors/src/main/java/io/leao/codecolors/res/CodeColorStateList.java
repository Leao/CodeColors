package io.leao.codecolors.res;

import android.content.res.ColorStateList;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

public class CodeColorStateList extends ColorStateList {
    private static final int[][] EMPTY = new int[][]{new int[0]};

    private int mId = CodeResources.NO_ID;

    private Integer mColor;

    protected WeakHashMap<Callback, Object> mCallbacks = new WeakHashMap<>();
    protected WeakHashMap<Object, AnchorCallback> mAnchorCallbacks = new WeakHashMap<>();

    /**
     * Thread-safe cache of single-color ColorStateLists.
     */
    private static final SparseArray<WeakReference<CodeColorStateList>> sCache = new SparseArray<>();

    protected CodeColorStateList(int[][] states, int[] colors) {
        super(states, colors);
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

    public void setColor(Integer color) {
        if (mColor == null || !mColor.equals(color)) {
            mColor = color;
            invalidateSelf();
        }
    }

    public Integer getColor() {
        return mColor;
    }

    @Override
    public int getColorForState(int[] stateSet, int defaultColor) {
        return mColor != null ? mColor : super.getColorForState(stateSet, defaultColor);
    }

    @Override
    public int getDefaultColor() {
        return mColor != null ? mColor : super.getDefaultColor();
    }

    public void addCallback(Callback callback) {
        mCallbacks.put(callback, null);
    }

    /**
     * @param anchor the anchor object to which the callback is dependent.
     * @param callback the callback.
     */
    public void addCallback(Object anchor, AnchorCallback callback) {
        mAnchorCallbacks.put(anchor, callback);
    }

    public void removeCallback(Callback callback) {
        mCallbacks.remove(callback);
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

    /**
     * @return A CodeColorStateList with the same states and colors of the source ColorStateList.
     */
    public static CodeColorStateList valueOf(ColorStateList source) {
        Parcel parcel = Parcel.obtain();
        source.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        CodeColorStateList ccsl = CodeColorStateList.CREATOR.createFromParcel(parcel);
        parcel.recycle();
        return ccsl;
    }

    /**
     * @return A ColorStateList containing a single color.
     */
    @NonNull
    public static CodeColorStateList valueOf(@ColorInt int color) {
        synchronized (sCache) {
            final int index = sCache.indexOfKey(color);
            if (index >= 0) {
                final CodeColorStateList cached = sCache.valueAt(index).get();
                if (cached != null) {
                    return cached;
                }

                // Prune missing entry.
                sCache.removeAt(index);
            }

            // Prune the cache before adding new items.
            final int N = sCache.size();
            for (int i = N - 1; i >= 0; i--) {
                if (sCache.valueAt(i).get() == null) {
                    sCache.removeAt(i);
                }
            }

            final CodeColorStateList ccsl = new CodeColorStateList(EMPTY, new int[]{color});
            sCache.put(color, new WeakReference<>(ccsl));
            return ccsl;
        }
    }

    public static final Parcelable.Creator<CodeColorStateList> CREATOR = new Parcelable.Creator<CodeColorStateList>() {
        @Override
        public CodeColorStateList[] newArray(int size) {
            return new CodeColorStateList[size];
        }

        @Override
        public CodeColorStateList createFromParcel(Parcel source) {
            final int N = source.readInt();
            final int[][] stateSpecs = new int[N][];
            for (int i = 0; i < N; i++) {
                stateSpecs[i] = source.createIntArray();
            }
            final int[] colors = source.createIntArray();
            return new CodeColorStateList(stateSpecs, colors);
        }
    };

    public interface Callback {
        void invalidateColor(CodeColorStateList color);
    }

    public interface AnchorCallback<T> {
        void invalidateColor(T anchor, CodeColorStateList color);
    }
}
