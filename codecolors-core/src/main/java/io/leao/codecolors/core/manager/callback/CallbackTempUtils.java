package io.leao.codecolors.core.manager.callback;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import io.leao.codecolors.core.color.CcColorStateList;

class CallbackTempUtils {
    private static Queue<Set<CallbackHandler>> sCallbackHandlers
            = new LinkedList<>();
    private static Queue<Set<CallbackHandler.Reference<CcColorStateList.SingleCallback>>> sSingleCallbackSets
            = new LinkedList<>();
    private static Queue<Set<CallbackHandler.PairReference<CcColorStateList.AnchorCallback, Object>>> sPairCallbackSets
            = new LinkedList<>();

    /**
     * Make sure to call {@link #recycleCallbackHandlerSet(Set)} when you are done with the set.
     */
    public static Set<CallbackHandler> getCallbackHandlerSet() {
        if (!sCallbackHandlers.isEmpty()) {
            return sCallbackHandlers.poll();
        } else {
            return new HashSet<>();
        }
    }

    public static void recycleCallbackHandlerSet(Set<CallbackHandler> set) {
        if (set != null) {
            set.clear();
            sCallbackHandlers.add(set);
        }
    }

    /**
     * Make sure to call {@link #recycleSingleCallbackSet(Set)} when you are done with the set.
     */
    public static Set<CallbackHandler.Reference<CcColorStateList.SingleCallback>> getSingleCallbackSet() {
        if (!sSingleCallbackSets.isEmpty()) {
            return sSingleCallbackSets.poll();
        } else {
            return new HashSet<>();
        }
    }

    public static void recycleSingleCallbackSet(Set<CallbackHandler.Reference<CcColorStateList.SingleCallback>> set) {
        if (set != null) {
            set.clear();
            sSingleCallbackSets.add(set);
        }
    }

    /**
     * Make sure to call {@link #recyclePairCallbackSet(Set)} when you are done with the set.
     */
    public static Set<CallbackHandler.PairReference<CcColorStateList.AnchorCallback, Object>> getPairCallbackSet() {
        if (!sPairCallbackSets.isEmpty()) {
            return sPairCallbackSets.poll();
        } else {
            return new HashSet<>();
        }
    }

    public static void recyclePairCallbackSet(
            Set<CallbackHandler.PairReference<CcColorStateList.AnchorCallback, Object>> set) {
        if (set != null) {
            set.clear();
            sPairCallbackSets.add(set);
        }
    }
}
