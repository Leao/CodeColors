package io.leao.codecolors.core.color;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import io.leao.codecolors.core.color.CodeColor.AnchorCallback;
import io.leao.codecolors.core.color.CodeColor.SingleCallback;

class CallbackTempUtils {
    private static Queue<Set<Reference<SingleCallback>>> sSingleCallbackSets = new LinkedList<>();
    private static Queue<Set<ReferencePair<Object, AnchorCallback>>> sPairCallbackSets = new LinkedList<>();

    /**
     * Make sure to call {@link #recycleSingleCallbackSet(Set)} when you are done with the set.
     */
    public static Set<Reference<SingleCallback>> getSingleCallbackSet() {
        if (!sSingleCallbackSets.isEmpty()) {
            return sSingleCallbackSets.poll();
        } else {
            return new HashSet<>();
        }
    }

    public static void recycleSingleCallbackSet(Set<Reference<SingleCallback>> set) {
        if (set != null) {
            set.clear();
            sSingleCallbackSets.add(set);
        }
    }

    /**
     * Make sure to call {@link #recyclePairCallbackSet(Set)} when you are done with the set.
     */
    public static Set<ReferencePair<Object, AnchorCallback>> getPairCallbackSet() {
        if (!sPairCallbackSets.isEmpty()) {
            return sPairCallbackSets.poll();
        } else {
            return new HashSet<>();
        }
    }

    public static void recyclePairCallbackSet(Set<ReferencePair<Object, AnchorCallback>> set) {
        if (set != null) {
            set.clear();
            sPairCallbackSets.add(set);
        }
    }
}
