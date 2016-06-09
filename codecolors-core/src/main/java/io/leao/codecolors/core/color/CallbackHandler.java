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

        if (!containsCallback(callback)) {
            Reference<CcColorStateList.SingleCallback> singleCallbackRef = new Reference<>(callback);
            mSingleCallbackList.add(singleCallbackRef);
            mSingleCallbackSet.add(singleCallbackRef);
        }
    }

    public synchronized boolean containsCallback(CcColorStateList.SingleCallback callback) {
        mTempReference.set(callback);
        boolean contains = containsCallback(mTempReference);
        mTempReference.set(null);
        return contains;
    }

    synchronized boolean containsCallback(Reference<CcColorStateList.SingleCallback> callbackReference) {
        return mSingleCallbackSet.contains(callbackReference);
    }

    public synchronized void removeCallback(CcColorStateList.SingleCallback callback) {
        mTempReference.set(callback);
        mSingleCallbackSet.remove(mTempReference);
        mTempReference.set(null);
    }

    public synchronized void addPairCallback(CcColorStateList.AnchorCallback callback, Object anchor) {
        if (callback == null || anchor == null) {
            return;
        }

        if (!containsPairCallback(callback, anchor)) {
            PairReference<CcColorStateList.AnchorCallback, Object> pairReference =
                    new PairReference<>(callback, anchor);

            mPairCallbackList.add(pairReference);
            mPairCallbackSet.add(pairReference);
            mAnchorCallbackSet.add(pairReference.getFirst());
            mAnchorSet.add(pairReference.getSecond());
        }
    }

    public synchronized boolean containsPairCallback(CcColorStateList.AnchorCallback callback, Object anchor) {
        mTempPairReference.set(callback, anchor);
        boolean contains = containsPairCallback(mTempPairReference);
        mTempPairReference.set(null, null);
        return contains;
    }

    synchronized boolean containsPairCallback(
            PairReference<CcColorStateList.AnchorCallback, Object> pairReference) {
        return mPairCallbackList.contains(pairReference);
    }

    public synchronized void removePairCallback(CcColorStateList.AnchorCallback callback, Object anchor) {
        mTempPairReference.set(callback, anchor);
        mPairCallbackSet.remove(mTempPairReference);
        mTempPairReference.set(null, null);
    }

    public synchronized void removeCallback(CcColorStateList.AnchorCallback callback) {
        mTempReference.set(callback);
        mAnchorSet.remove(mTempReference);
        mTempReference.set(null);
    }

    public synchronized void removeAnchor(Object anchor) {
        mTempReference.set(anchor);
        mAnchorSet.remove(mTempReference);
        mTempReference.set(null);
    }

    public void iterateCallbacks(
            Set<Reference<CcColorStateList.SingleCallback>> iteratedSingleCallbacks,
            Set<PairReference<CcColorStateList.AnchorCallback, Object>> iteratedPairCallbacks,
            OnIterateCallbackListener listener) {
        /*
         * SingleCallbacks.
         */

        // List iterator supports add and remove while iterating.
        Iterator<Reference<CcColorStateList.SingleCallback>> singleCallbackRefIterator =
                mSingleCallbackList.listIterator();
        while (singleCallbackRefIterator.hasNext()) {

            // Was callback removed?
            Reference<CcColorStateList.SingleCallback> singleCallbackRef = singleCallbackRefIterator.next();
            if (containsCallback(singleCallbackRef)) {

                // Was callback collected by GC?
                CcColorStateList.SingleCallback singleCallback = singleCallbackRef.get();
                if (singleCallback != null) {

                    // Are we tracking iterations? Was callback iterated previously?
                    if (iteratedSingleCallbacks == null || iteratedSingleCallbacks.add(singleCallbackRef)) {

                        // Call listener.
                        listener.onIterateSingleCallback(singleCallbackRef, singleCallback);
                    }
                    // Continue without any removals.
                    continue;
                }

                // Callback was collected by GC.
                // Remove callback from set.
                mSingleCallbackSet.remove(singleCallbackRef);
            }

            // Either callback was collected by GC, or was removed.
            // Remove callback from list.
            singleCallbackRefIterator.remove();
        }

         /*
         * PairCallbacks.
         */

        // List iterator supports add and remove while iterating.
        Iterator<PairReference<CcColorStateList.AnchorCallback, Object>> pairReferenceIterator =
                mPairCallbackList.listIterator();
        while (pairReferenceIterator.hasNext()) {

            // Was callback pair removed?
            PairReference<CcColorStateList.AnchorCallback, Object> pairReference = pairReferenceIterator.next();
            if (containsPairCallback(pairReference)) {

                // Were callback or anchor removed individually?
                Reference<CcColorStateList.AnchorCallback> anchorCallbackRef = pairReference.getFirst();
                Reference<Object> anchorRef = pairReference.getSecond();
                if (mAnchorCallbackSet.contains(anchorCallbackRef) && mAnchorSet.contains(anchorRef)) {

                    // Were callback or anchor collected by GC?
                    CcColorStateList.AnchorCallback anchorCallback = anchorCallbackRef.get();
                    Object anchor = anchorRef.get();
                    if (anchorCallback != null && anchor != null) {

                        // Are we tracking iterations? Was callback pair iterated previously?
                        if (iteratedPairCallbacks == null || iteratedPairCallbacks.add(pairReference)) {

                            // Call listener.
                            listener.onIteratePairCallback(pairReference, anchorCallback, anchor);
                        }
                        // Continue without any removals.
                        continue;
                    }

                    // Callback or anchor were collected by GC.
                    // Remove callback or anchor null references.
                    if (anchorCallback == null) {
                        mAnchorCallbackSet.remove(anchorCallbackRef);
                    }
                    if (anchor == null) {
                        mAnchorSet.remove(anchorRef);
                    }
                }

                // Either the callback or anchor were collected by GC, or removed (individually).
                // Remove callback pair from set.
                mPairCallbackSet.remove(pairReference);
            }
            // Either the callback or anchor were collected by GC, removed (individually), or removed as a pair.
            // Remove callback pair from list.
            pairReferenceIterator.remove();
        }
    }

    public interface OnIterateCallbackListener {
        void onIterateSingleCallback(Reference<CcColorStateList.SingleCallback> callbackReference,
                                     CcColorStateList.SingleCallback callback);

        void onIteratePairCallback(PairReference<CcColorStateList.AnchorCallback, Object> pairReference,
                                   CcColorStateList.AnchorCallback callback, Object anchor);
    }

    public static class Reference<T> {
        /*
         * if objectRef != null ? hashCode != null && object == null
         * if object != null ? objectRef == null && hashCode == null
         */
        private WeakReference<T> objectRef;
        private Integer hashCode;
        private T object;

        public Reference() {
        }

        public Reference(T object) {
            setWeak(object);
        }

        public void setWeak(T object) {
            objectRef = new WeakReference<>(object);
            hashCode = object != null ? object.hashCode() : 0;
            this.object = null;
        }

        public void set(T object) {
            objectRef = null;
            hashCode = null;
            this.object = object;
        }

        public T get() {
            if (objectRef != null) {
                return objectRef.get();
            } else {
                return object;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Object object = get();

            Reference that = (Reference) o;
            Object thatObject = that.get();

            return object != null ? object.equals(thatObject) : thatObject == null;
        }

        @Override
        public int hashCode() {
            return object != null ? object.hashCode() : hashCode;
        }
    }

    public static class PairReference<U, V> {
        private Reference<U> firstRef;
        private Reference<V> secondRef;

        public PairReference() {
            firstRef = new Reference<>();
            secondRef = new Reference<>();
        }

        public PairReference(U first, V second) {
            this();
            setWeak(first, second);
        }

        public void setWeak(U first, V second) {
            firstRef.setWeak(first);
            secondRef.setWeak(second);
        }

        public void set(U first, V second) {
            firstRef.set(first);
            secondRef.set(second);
        }

        public Reference<U> getFirst() {
            return firstRef;
        }

        public Reference<V> getSecond() {
            return secondRef;
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
