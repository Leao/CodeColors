package io.leao.codecolors.core.color;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import io.leao.codecolors.core.color.CallbackHandler.Reference;
import io.leao.codecolors.core.color.CallbackHandler.PairReference;
import io.leao.codecolors.core.color.CcColorStateList.AnchorCallback;
import io.leao.codecolors.core.color.CcColorStateList.SingleCallback;

class CallbackTempUtils {
    private static Queue<Set<Reference<SingleCallback>>> sSingleCallbackSets = new LinkedList<>();
    private static Queue<Set<PairReference<AnchorCallback, Object>>> sPairCallbackSets = new LinkedList<>();

    /**
     * Make sure to call {@link #recycleSingleCallbackSet(Set)} when you are done with the set.
     */
    public static Set<CallbackHandler.Reference<SingleCallback>> getSingleCallbackSet() {
        if (!sSingleCallbackSets.isEmpty()) {
            return sSingleCallbackSets.poll();
        } else {
            return new HashSet<>();
        }
    }

    public static void recycleSingleCallbackSet(Set<CallbackHandler.Reference<SingleCallback>> set) {
        if (set != null) {
            set.clear();
            sSingleCallbackSets.add(set);
        }
    }

    /**
     * Make sure to call {@link #recyclePairCallbackSet(Set)} when you are done with the set.
     */
    public static Set<PairReference<AnchorCallback, Object>> getPairCallbackSet() {
        if (!sPairCallbackSets.isEmpty()) {
            return sPairCallbackSets.poll();
        } else {
            return new HashSet<>();
        }
    }

    public static void recyclePairCallbackSet(Set<PairReference<AnchorCallback, Object>> set) {
        if (set != null) {
            set.clear();
            sPairCallbackSets.add(set);
        }
    }
}
