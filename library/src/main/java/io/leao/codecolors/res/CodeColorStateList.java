package io.leao.codecolors.res;

import android.content.res.ColorStateList;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.lang.ref.WeakReference;

public class CodeColorStateList extends ColorStateList {
    private static final int[][] EMPTY = new int[][]{new int[0]};
    
    /**
     * Thread-safe cache of single-color ColorStateLists.
     */
    private static final SparseArray<WeakReference<CodeColorStateList>> sCache = new SparseArray<>();

    public CodeColorStateList(int[][] states, int[] colors) {
        super(states, colors);
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
}
