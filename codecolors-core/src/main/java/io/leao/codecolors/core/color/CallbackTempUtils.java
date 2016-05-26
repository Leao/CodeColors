package io.leao.codecolors.core.color;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import io.leao.codecolors.core.color.CallbackHandler.Reference;
import io.leao.codecolors.core.color.CallbackHandler.PairReference;

class CallbackTempUtils {
    public static Queue<Set<Reference<CcColorStateList.SingleCallback>>> sSingleCallbackSets =
            new LinkedList<>();
    public static Queue<Set<PairReference<CcColorStateList.AnchorCallback, Object>>> sPairCallbackSets =
            new LinkedList<>();
    public static Queue<Set<CcColorStateList>> sColorSets = new LinkedList<>();

    /**
     * Make sure to call {@link #recycleSingleCallbackSet(Set)} when you are done with the set.
     */
    public static Set<Reference<CcColorStateList.SingleCallback>> getSingleCallbackSet() {
        if (!sSingleCallbackSets.isEmpty()) {
            return sSingleCallbackSets.poll();
        } else {
            return new HashSet<>();
        }
    }

    public static void recycleSingleCallbackSet(Set<Reference<CcColorStateList.SingleCallback>> set) {
        if (set != null) {
            set.clear();
            sSingleCallbackSets.add(set);
        }
    }

    /**
     * Make sure to call {@link #recyclePairCallbackSet(Set)} when you are done with the set.
     */
    public static Set<PairReference<CcColorStateList.AnchorCallback, Object>> getPairCallbackSet() {
        if (!sPairCallbackSets.isEmpty()) {
            return sPairCallbackSets.poll();
        } else {
            return new HashSet<>();
        }
    }

    public static void recyclePairCallbackSet(Set<PairReference<CcColorStateList.AnchorCallback, Object>> set) {
        if (set != null) {
            set.clear();
            sPairCallbackSets.add(set);
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
