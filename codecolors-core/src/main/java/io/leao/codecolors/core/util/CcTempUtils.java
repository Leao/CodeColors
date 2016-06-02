package io.leao.codecolors.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import io.leao.codecolors.core.color.CcColorStateList;

public class CcTempUtils {
    private static Map<Integer, List<int[]>> sIntArrays = new HashMap<>();

    // Order bigger sets first. Supposedly, it helps in reducing the probability of the needed to allocate more memory.
    private static TreeSet<Set<Integer>> sIntegerSets = new TreeSet<>(new Comparator<Set>() {
        @Override
        public int compare(Set lhs, Set rhs) {
            return rhs.size() - lhs.size();
        }
    });

    private static Queue<Set<CcColorStateList>> sColorSets = new LinkedList<>();

    /**
     * Make sure to call {@link #recycleIntArray(int[])} when you are done with the array.
     */
    public static int[] toIntArray(Collection<Integer> collection) {
        int[] array = CcTempUtils.getIntArray(collection.size());
        int i = 0;
        for (Integer value : collection) {
            array[i++] = value;
        }
        return array;
    }

    /**
     * Make sure to call {@link #recycleIntArray(int[])} when you are done with the array.
     */
    public static int[] getIntArray(int length) {
        List<int[]> arrays = sIntArrays.get(length);
        if (arrays == null) {
            arrays = new ArrayList<>();
            sIntArrays.put(length, arrays);
        }
        int[] array;
        if (!arrays.isEmpty()) {
            array = arrays.remove(arrays.size() - 1);
        } else {
            array = new int[length];
        }
        return array;
    }

    public static void recycleIntArray(int[] array) {
        if (array != null) {
            int N = array.length;
            List<int[]> arrayList = sIntArrays.get(N);
            if (arrayList == null) {
                arrayList = new ArrayList<>();
                sIntArrays.put(N, arrayList);
            }
            arrayList.add(array);
        }
    }

    /**
     * Make sure to call {@link #recycleIntegerSet(Set)} when you are done with the set.
     */
    public static Set<Integer> getIntegerSet() {
        if (!sIntegerSets.isEmpty()) {
            return sIntegerSets.pollFirst();
        } else {
            return new HashSet<>();
        }
    }

    public static void recycleIntegerSet(Set<Integer> set) {
        if (set != null) {
            set.clear();
            sIntegerSets.add(set);
        }
    }

    /**
     * Make sure to call {@link #recycleColorSet(Set)} when you are done with the set.
     */
    public static Set<CcColorStateList> getColorSet() {
        if (!sColorSets.isEmpty()) {
            return sColorSets.poll();
        } else {
            return new HashSet<>();
        }
    }

    public static void recycleColorSet(Set<CcColorStateList> set) {
        if (set != null) {
            set.clear();
            sColorSets.add(set);
        }
    }
}
