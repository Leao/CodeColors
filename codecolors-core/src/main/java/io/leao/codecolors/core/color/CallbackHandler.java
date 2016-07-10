package io.leao.codecolors.core.color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import io.leao.codecolors.core.color.CodeColor.AnchorCallback;
import io.leao.codecolors.core.color.CodeColor.SingleCallback;

@SuppressWarnings("unchecked")
class CallbackHandler {
    protected List<Reference<SingleCallback>> mSingleCallbackList = new ArrayList<>();
    protected Set<Reference<SingleCallback>> mSingleCallbackSet = new HashSet<>();

    /*
     * AnchorCallbacks are stored in strong references.
     */
    protected List<ReferencePair<Object, AnchorCallback>> mPairCallbackList = new ArrayList<>();
    protected Set<ReferencePair<Object, AnchorCallback>> mPairCallbackSet = new HashSet<>();
    protected Set<Reference<Object>> mAnchorSet = new HashSet<>();
    protected Set<Reference<AnchorCallback>> mAnchorCallbackSet = new HashSet<>();

    protected Reference mTempReference = new Reference();
    protected ReferencePair<Object, AnchorCallback> mTempMultiReference = new ReferencePair();

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

    public synchronized void addPairCallback(Object anchor, AnchorCallback callback) {
        if (anchor == null || callback == null) {
            return;
        }

        if (!containsPairCallback(anchor, callback)) {
            // Create a pair reference for callback and anchor.
            // Note: that the callback reference is strong, while the anchor reference is weak.
            // That happens, because the AnchorCallback is dependent on its anchor.
            ReferencePair<Object, AnchorCallback> referencePair = new ReferencePair<>();
            referencePair.getFirst().setWeak(anchor);
            referencePair.getSecond().set(callback); // Not weak!

            mPairCallbackList.add(referencePair);
            mPairCallbackSet.add(referencePair);
            // Add anchor weak reference to set.
            mAnchorSet.add(referencePair.getFirst());
            // Add callback to set, but as a weak reference.
            Reference<AnchorCallback> callbackWeakRef = new Reference<>();
            callbackWeakRef.setWeak(callback);
            mAnchorCallbackSet.add(callbackWeakRef);
        }
    }

    public synchronized boolean containsPairCallback(Object anchor, AnchorCallback callback) {
        mTempMultiReference.getFirst().set(anchor);
        mTempMultiReference.getSecond().set(callback);
        boolean contains = containsPairCallback(mTempMultiReference);
        mTempMultiReference.getFirst().set(null);
        mTempMultiReference.getSecond().set(null);
        return contains;
    }

    synchronized boolean containsPairCallback(ReferencePair<Object, AnchorCallback> referencePair) {
        return mPairCallbackSet.contains(referencePair);
    }

    public synchronized void removePairCallback(Object anchor, AnchorCallback callback) {
        mTempMultiReference.getFirst().set(anchor);
        mTempMultiReference.getSecond().set(callback);
        mPairCallbackSet.remove(mTempMultiReference);
        mTempMultiReference.getFirst().set(null);
        mTempMultiReference.getSecond().set(null);
    }

    public synchronized void removeAnchor(Object anchor) {
        mTempReference.set(anchor);
        mAnchorSet.remove(mTempReference);
        mTempReference.set(null);
    }

    public synchronized void removeCallback(AnchorCallback callback) {
        mTempReference.set(callback);
        mAnchorCallbackSet.remove(mTempReference);
        mTempReference.set(null);
    }

    public void iterateCallbacks(Set<Reference<SingleCallback>> iteratedSingleCallbacks,
                                 Set<ReferencePair<Object, AnchorCallback>> iteratedPairCallbacks,
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
        Iterator<ReferencePair<Object, AnchorCallback>> pairReferenceIterator = mPairCallbackList.listIterator();
        while (pairReferenceIterator.hasNext()) {

            // Was callback pair removed?
            ReferencePair<Object, AnchorCallback> referencePair = pairReferenceIterator.next();
            if (containsPairCallback(referencePair)) {

                // Were callback or anchor removed individually?
                Reference<Object> anchorRef = referencePair.getFirst();
                Reference<AnchorCallback> anchorCallbackRef = referencePair.getSecond();
                if (mAnchorSet.contains(anchorRef) && mAnchorCallbackSet.contains(anchorCallbackRef)) {

                    // Was the anchor collected by GC?
                    // Note: the AnchorCallback can't be collected by GC, because it is stored by strong reference.
                    Object anchor = anchorRef.get();
                    if (anchor != null) {

                        // Are we tracking iterations? Was callback pair iterated previously?
                        if (iteratedPairCallbacks == null || iteratedPairCallbacks.add(referencePair)) {

                            // Call listener.
                            listener.onIteratePairCallback(referencePair, anchor, anchorCallbackRef.get());
                        }
                        // Continue without any removals.
                        continue;
                    }

                    // Anchor was collected by GC. Remove its null reference.
                    mAnchorSet.remove(anchorRef);
                }

                // Either the callback or anchor were removed (individually), or the anchor was collected by GC.
                // Remove callback pair from set.
                mPairCallbackSet.remove(referencePair);
            }

            // Either the callback or anchor were removed (individually), or the anchor was collected by GC,
            // or removed as a pair.
            // Remove callback pair from list.
            pairReferenceIterator.remove();
        }
    }

    public interface OnIterateCallbackListener {
        void onIterateSingleCallback(Reference<SingleCallback> callbackReference, SingleCallback callback);

        void onIteratePairCallback(ReferencePair<Object, AnchorCallback> referencePair, Object anchor,
                                   AnchorCallback callback);
    }
}
