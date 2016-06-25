package io.leao.codecolors.core.color;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import io.leao.codecolors.core.color.CcColorStateList.AnchorCallback;
import io.leao.codecolors.core.color.CcColorStateList.SingleCallback;

@SuppressWarnings("unchecked")
class CallbackHandler {
    protected List<Reference<SingleCallback>> mSingleCallbackList = new ArrayList<>();
    protected Set<Reference<SingleCallback>> mSingleCallbackSet = new HashSet<>();

    /*
     * AnchorCallbacks are stored in strong references.
     */
    protected List<PairReference<AnchorCallback, Object>> mPairCallbackList = new ArrayList<>();
    protected Set<PairReference<AnchorCallback, Object>> mPairCallbackSet = new HashSet<>();
    protected Set<Reference<AnchorCallback>> mAnchorCallbackSet = new HashSet<>();
    protected Set<Reference<Object>> mAnchorSet = new HashSet<>();

    protected Reference mTempReference = new Reference();
    protected PairReference<AnchorCallback, Object> mTempMultiReference = new PairReference();

    public synchronized void addCallback(SingleCallback callback) {
        if (callback == null) {
            return;
        }

        if (!containsCallback(callback)) {
            Reference<SingleCallback> singleCallbackRef = new Reference<>();
            singleCallbackRef.setWeak(callback);

            mSingleCallbackList.add(singleCallbackRef);
            mSingleCallbackSet.add(singleCallbackRef);
        }
    }

    public synchronized boolean containsCallback(SingleCallback callback) {
        mTempReference.set(callback);
        boolean contains = containsCallback(mTempReference);
        mTempReference.set(null);
        return contains;
    }

    synchronized boolean containsCallback(Reference<SingleCallback> callbackReference) {
        return mSingleCallbackSet.contains(callbackReference);
    }

    public synchronized void removeCallback(SingleCallback callback) {
        mTempReference.set(callback);
        mSingleCallbackSet.remove(mTempReference);
        mTempReference.set(null);
    }

    public synchronized void addPairCallback(AnchorCallback callback, Object anchor) {
        if (callback == null || anchor == null) {
            return;
        }

        if (!containsPairCallback(callback, anchor)) {
            // Create a pair reference for callback and anchor.
            // Note: that the callback reference is strong, while the anchor reference is weak.
            // That happens because the AnchorCallback is dependent on its anchor.
            PairReference<AnchorCallback, Object> pairReference = new PairReference<>();
            pairReference.getFirst().set(callback); // Not weak!
            pairReference.getSecond().setWeak(anchor);

            mPairCallbackList.add(pairReference);
            mPairCallbackSet.add(pairReference);
            // Add callback to set, but as a weak reference.
            Reference<AnchorCallback> callbackWeakRef = new Reference<>();
            callbackWeakRef.setWeak(callback);
            mAnchorCallbackSet.add(callbackWeakRef);
            // Add anchor weak reference to set.
            mAnchorSet.add(pairReference.getSecond());
        }
    }

    public synchronized boolean containsPairCallback(AnchorCallback callback, Object anchor) {
        mTempMultiReference.getFirst().set(callback);
        mTempMultiReference.getSecond().set(anchor);
        boolean contains = containsPairCallback(mTempMultiReference);
        mTempMultiReference.getFirst().set(null);
        mTempMultiReference.getSecond().set(null);
        return contains;
    }

    synchronized boolean containsPairCallback(PairReference<AnchorCallback, Object> pairReference) {
        return mPairCallbackSet.contains(pairReference);
    }

    public synchronized void removePairCallback(AnchorCallback callback, Object anchor) {
        mTempMultiReference.getFirst().set(callback);
        mTempMultiReference.getSecond().set(anchor);
        mPairCallbackSet.remove(mTempMultiReference);
        mTempMultiReference.getFirst().set(null);
        mTempMultiReference.getSecond().set(null);
    }

    public synchronized void removeCallback(AnchorCallback callback) {
        mTempReference.set(callback);
        mAnchorCallbackSet.remove(mTempReference);
        mTempReference.set(null);
    }

    public synchronized void removeAnchor(Object anchor) {
        mTempReference.set(anchor);
        mAnchorSet.remove(mTempReference);
        mTempReference.set(null);
    }

    public void iterateCallbacks(Set<Reference<SingleCallback>> iteratedSingleCallbacks,
                                 Set<PairReference<AnchorCallback, Object>> iteratedPairCallbacks,
                                 OnIterateCallbackListener listener) {
        /*
         * SingleCallbacks.
         */

        // List iterator supports add and remove while iterating.
        Iterator<Reference<SingleCallback>> singleCallbackRefIterator = mSingleCallbackList.listIterator();
        while (singleCallbackRefIterator.hasNext()) {

            // Was callback removed?
            Reference<SingleCallback> singleCallbackRef = singleCallbackRefIterator.next();
            if (containsCallback(singleCallbackRef)) {

                // Was callback collected by GC?
                SingleCallback singleCallback = singleCallbackRef.get();
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
        Iterator<PairReference<AnchorCallback, Object>> pairReferenceIterator = mPairCallbackList.listIterator();
        while (pairReferenceIterator.hasNext()) {

            // Was callback pair removed?
            PairReference<AnchorCallback, Object> pairReference = pairReferenceIterator.next();
            if (containsPairCallback(pairReference)) {

                // Were callback or anchor removed individually?
                Reference<AnchorCallback> anchorCallbackRef = pairReference.getFirst();
                Reference<Object> anchorRef = pairReference.getSecond();
                if (mAnchorCallbackSet.contains(anchorCallbackRef) && mAnchorSet.contains(anchorRef)) {

                    // Was the anchor collected by GC?
                    // Note: the AnchorCallback can't be collected by GC, because it is stored by strong reference.
                    Object anchor = anchorRef.get();
                    if (anchor != null) {

                        // Are we tracking iterations? Was callback pair iterated previously?
                        if (iteratedPairCallbacks == null || iteratedPairCallbacks.add(pairReference)) {

                            // Call listener.
                            listener.onIteratePairCallback(pairReference, anchorCallbackRef.get(), anchor);
                        }
                        // Continue without any removals.
                        continue;
                    }

                    // Anchor was collected by GC. Remove its null reference.
                    mAnchorSet.remove(anchorRef);
                }

                // Either the callback or anchor were removed (individually), or the anchor was collected by GC.
                // Remove callback pair from set.
                mPairCallbackSet.remove(pairReference);
            }

            // Either the callback or anchor were removed (individually), or the anchor was collected by GC,
            // or removed as a pair.
            // Remove callback pair from list.
            pairReferenceIterator.remove();
        }
    }

    public interface OnIterateCallbackListener {
        void onIterateSingleCallback(Reference<SingleCallback> callbackReference, SingleCallback callback);

        void onIteratePairCallback(PairReference<AnchorCallback, Object> pairReference,
                                   AnchorCallback callback, Object anchor);
    }

    public static class Reference<T> {
        /*
         * if objectRef != null ? hashCode != null && object == null
         * if object != null ? objectRef == null && hashCode == null
         */
        private WeakReference<T> objectRef;
        private Integer hashCode;
        private T object;

        public T get() {
            if (objectRef != null) {
                return objectRef.get();
            } else {
                return object;
            }
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
            this.firstRef = new Reference<>();
            this.secondRef = new Reference<>();
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
