package io.leao.codecolors.core.color;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unchecked")
class CallbackHandler {
    protected List<Reference<CcColorStateList.SingleCallback>> mSingleCallbackList = new ArrayList<>();
    protected Set<Reference<CcColorStateList.SingleCallback>> mSingleCallbackSet = new HashSet<>();

    protected List<PairReference<CcColorStateList.AnchorCallback, Object>> mPairCallbackList = new ArrayList<>();
    protected Set<PairReference<CcColorStateList.AnchorCallback, Object>> mPairCallbackSet = new HashSet<>();
    protected Set<Reference<CcColorStateList.AnchorCallback>> mAnchorCallbackSet = new HashSet<>();
    protected Set<Reference<Object>> mAnchorSet = new HashSet<>();

    protected Reference mTempReference = new Reference();
    protected PairReference mTempPairReference = new PairReference();

    public synchronized void addCallback(CcColorStateList.SingleCallback callback) {
        if (callback == null) {
            return;
        }

        mTempReference.set(callback);
        if (!mSingleCallbackSet.contains(mTempReference)) {
            Reference<CcColorStateList.SingleCallback> singleCallbackRef = mTempReference;
            mTempReference = new Reference();

            mSingleCallbackList.add(singleCallbackRef);
            mSingleCallbackSet.add(singleCallbackRef);
        }
    }

    public synchronized boolean containsCallback(CcColorStateList.SingleCallback callback) {
        mTempReference.set(callback);
        return mSingleCallbackSet.contains(mTempReference);
    }

    public synchronized void removeCallback(CcColorStateList.SingleCallback callback) {
        mTempReference.set(callback);
        mSingleCallbackSet.remove(mTempReference);
    }

    public synchronized void addPairCallback(CcColorStateList.AnchorCallback callback, Object anchor) {
        if (callback == null || anchor == null) {
            return;
        }

        mTempPairReference.set(callback, anchor);
        if (!mPairCallbackSet.contains(mTempPairReference)) {
            PairReference<CcColorStateList.AnchorCallback, Object> pairCallbackRef = mTempPairReference;
            mTempPairReference = new PairReference();

            mPairCallbackList.add(pairCallbackRef);
            mPairCallbackSet.add(pairCallbackRef);
            mAnchorCallbackSet.add(pairCallbackRef.firstRef);
            mAnchorSet.add(pairCallbackRef.secondRef);
        }
    }

    public synchronized boolean containsPairCallback(CcColorStateList.AnchorCallback callback, Object anchor) {
        mTempPairReference.set(callback, anchor);
        return mPairCallbackSet.contains(mTempPairReference);
    }

    public synchronized void removePairCallback(CcColorStateList.AnchorCallback callback, Object anchor) {
        mTempPairReference.set(callback, anchor);
        mPairCallbackSet.remove(mTempPairReference);
    }

    public synchronized void removeCallback(CcColorStateList.AnchorCallback callback) {
        mTempReference.set(callback);
        mAnchorSet.remove(mTempReference);
    }

    public synchronized void removeAnchor(Object anchor) {
        mTempReference.set(anchor);
        mAnchorSet.remove(mTempReference);
    }

    public void invalidateColor(CcColorStateList color) {
        iterateCallbacks(color, new OnIterateCallbackListener() {
            @Override
            public void onIterateSingleCallback(CcColorStateList.SingleCallback callback, CcColorStateList color) {
                callback.invalidateColor(color);
            }

            @Override
            public void onIteratePairCallback(CcColorStateList.AnchorCallback callback, Object anchor,
                                              CcColorStateList color) {
                callback.invalidateColor(anchor, color);
            }
        });
    }

    public static void invalidateColors(final Set<CcColorStateList> colors) {
        Set<Reference<CcColorStateList.SingleCallback>> invalidatedSingleCallbacks =
                CallbackTempUtils.getSingleCallbackSet();
        Set<PairReference<CcColorStateList.AnchorCallback, Object>> invalidatedPairCallbacks =
                CallbackTempUtils.getPairCallbackSet();
        final Set<CcColorStateList> invalidateColors = CallbackTempUtils.getColorSet();

        for (CcColorStateList color : colors) {
            iterateCallbacks(
                    color,
                    invalidatedSingleCallbacks,
                    invalidatedPairCallbacks,
                    new OnIterateCallbackListener() {
                        @Override
                        public void onIterateSingleCallback(CcColorStateList.SingleCallback callback,
                                                            CcColorStateList color) {
                            invalidateColors.clear();
                            for (CcColorStateList invalidateColor : colors) {
                                if (invalidateColor == color ||
                                        invalidateColor.getCallbackHandler().containsCallback(callback)) {
                                    invalidateColors.add(invalidateColor);
                                }
                            }
                            callback.invalidateColors(invalidateColors);
                        }

                        @Override
                        public void onIteratePairCallback(CcColorStateList.AnchorCallback callback, Object anchor,
                                                          CcColorStateList color) {
                            invalidateColors.clear();
                            for (CcColorStateList invalidateColor : colors) {
                                if (invalidateColor == color ||
                                        invalidateColor.getCallbackHandler().containsPairCallback(callback, anchor)) {
                                    invalidateColors.add(invalidateColor);
                                }
                            }
                            callback.invalidateColors(anchor, colors);
                        }
                    }
            );
        }

        CallbackTempUtils.recycleSingleCallbackSet(invalidatedSingleCallbacks);
        CallbackTempUtils.recyclePairCallbackSet(invalidatedPairCallbacks);
        CallbackTempUtils.recycleColorSet(invalidateColors);
    }

    public static void iterateCallbacks(CcColorStateList color, OnIterateCallbackListener listener) {
        iterateCallbacks(color, null, null, listener);
    }

    public static void iterateCallbacks(
            CcColorStateList color,
            Set<Reference<CcColorStateList.SingleCallback>> iteratedSingleCallbacks,
            Set<PairReference<CcColorStateList.AnchorCallback, Object>> iteratedPairCallbacks,
            OnIterateCallbackListener listener) {
        CallbackHandler currentCallbackHandler = color.getCallbackHandler();

        /*
         * SingleCallbacks.
         */

        // List iterator supports add and remove while iterating.
        Iterator<Reference<CcColorStateList.SingleCallback>> singleCallbackRefIterator =
                currentCallbackHandler.mSingleCallbackList.listIterator();
        while (singleCallbackRefIterator.hasNext()) {

            // Was callback removed?
            Reference<CcColorStateList.SingleCallback> singleCallbackRef = singleCallbackRefIterator.next();
            if (currentCallbackHandler.mSingleCallbackSet.contains(singleCallbackRef)) {

                // Was callback collected by GC?
                CcColorStateList.SingleCallback singleCallback = singleCallbackRef.ref.get();
                if (singleCallback != null) {

                    // Are we tracking iterations? Was callback iterated previously?
                    if (iteratedSingleCallbacks == null || iteratedSingleCallbacks.add(singleCallbackRef)) {

                        // Call listener.
                        listener.onIterateSingleCallback(singleCallback, color);
                    }
                    // Continue without any removals.
                    continue;
                }

                // Callback was collected by GC.
                // Remove callback from set.
                currentCallbackHandler.mSingleCallbackSet.remove(singleCallbackRef);
            }

            // Either callback was collected by GC, or was removed.
            // Remove callback from list.
            singleCallbackRefIterator.remove();
        }

         /*
         * PairCallbacks.
         */

        // List iterator supports add and remove while iterating.
        Iterator<PairReference<CcColorStateList.AnchorCallback, Object>> pairCallbackRefIterator =
                currentCallbackHandler.mPairCallbackList.listIterator();
        while (pairCallbackRefIterator.hasNext()) {

            // Was callback pair removed?
            PairReference<CcColorStateList.AnchorCallback, Object> pairCallbackRef = pairCallbackRefIterator.next();
            if (currentCallbackHandler.mPairCallbackSet.contains(pairCallbackRef)) {

                // Were callback or anchor removed individually?
                Reference<CcColorStateList.AnchorCallback> anchorCallbackRef = pairCallbackRef.firstRef;
                Reference<Object> anchorRef = pairCallbackRef.secondRef;
                if (currentCallbackHandler.mAnchorCallbackSet.contains(anchorCallbackRef) &&
                        currentCallbackHandler.mAnchorSet.contains(anchorRef)) {

                    // Were callback or anchor collected by GC?
                    CcColorStateList.AnchorCallback anchorCallback = anchorCallbackRef.ref.get();
                    Object anchor = anchorRef.ref.get();
                    if (anchorCallback != null && anchor != null) {

                        // Are we tracking iterations? Was callback pair iterated previously?
                        if (iteratedPairCallbacks == null || iteratedPairCallbacks.add(pairCallbackRef)) {

                            // Call listener.
                            listener.onIteratePairCallback(anchorCallback, anchor, color);
                        }
                        // Continue without any removals.
                        continue;
                    }

                    // Callback or anchor were collected by GC.
                    // Remove callback or anchor null references.
                    if (anchorCallback == null) {
                        currentCallbackHandler.mAnchorCallbackSet.remove(anchorCallbackRef);
                    }
                    if (anchor == null) {
                        currentCallbackHandler.mAnchorSet.remove(anchorRef);
                    }
                }

                // Either the callback or anchor were collected by GC, or removed (individually).
                // Remove callback pair from set.
                currentCallbackHandler.mPairCallbackSet.remove(pairCallbackRef);
            }
            // Either the callback or anchor were collected by GC, removed (individually), or removed as a pair.
            // Remove callback pair from list.
            pairCallbackRefIterator.remove();
        }
    }

    private interface OnIterateCallbackListener {
        void onIterateSingleCallback(CcColorStateList.SingleCallback callback, CcColorStateList color);

        void onIteratePairCallback(CcColorStateList.AnchorCallback callback, Object anchor, CcColorStateList color);
    }

    public static class Reference<T> {
        public WeakReference<T> ref;
        private int hashCode;

        public void set(T object) {
            ref = new WeakReference<>(object);
            hashCode = object.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Object object = ref.get();

            Reference that = (Reference) o;
            Object thatObject = that.ref.get();

            return object != null ? object.equals(thatObject) : thatObject == null;

        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }

    public static class PairReference<U, V> {
        public Reference<U> firstRef = new Reference<>();
        public Reference<V> secondRef = new Reference<>();

        public void set(U first, V second) {
            firstRef.set(first);
            secondRef.set(second);
        }

        @SuppressWarnings("SimplifiableIfStatement")
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PairReference<?, ?> that = (PairReference<?, ?>) o;

            if (!firstRef.equals(that.firstRef)) return false;
            return secondRef.equals(that.secondRef);

        }

        @Override
        public int hashCode() {
            int result = firstRef.hashCode();
            result = 31 * result + secondRef.hashCode();
            return result;
        }
    }
}
