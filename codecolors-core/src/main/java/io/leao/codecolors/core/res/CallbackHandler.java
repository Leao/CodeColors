package io.leao.codecolors.core.res;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

class CallbackHandler {
    protected Set<CcColorStateList.Callback> mCallbacks =
            Collections.newSetFromMap(new WeakHashMap<CcColorStateList.Callback, Boolean>());

    protected Map<Object, Set<CcColorStateList.AnchorCallback>> mAnchorCallbacks = new WeakHashMap<>();
    /**
     * The set of anchors is generated from a {@link WeakHashMap} to make sure the anchors are discarded from
     * {@link CallbackHandler#mAnchorCallbacks} if they are not used by the application.
     */
    protected Map<CcColorStateList.AnchorCallback, Set<Object>> mCallbackAnchors = new WeakHashMap<>();

    public synchronized void addCallback(CcColorStateList.Callback callback) {
        mCallbacks.add(callback);
    }

    public synchronized void removeCallback(CcColorStateList.Callback callback) {
        mCallbacks.remove(callback);
    }

    public synchronized void addAnchorCallback(Object anchor, CcColorStateList.AnchorCallback callback) {
        Set<CcColorStateList.AnchorCallback> callbacks = mAnchorCallbacks.get(anchor);
        if (callbacks == null) {
            callbacks = new HashSet<>();
            mAnchorCallbacks.put(anchor, callbacks);
        }
        callbacks.add(callback);

        Set<Object> anchors = mCallbackAnchors.get(callback);
        if (anchors == null) {
            anchors = Collections.newSetFromMap(new WeakHashMap<Object, Boolean>());
            mCallbackAnchors.put(callback, anchors);
        }
        anchors.add(anchor);
    }

    public synchronized void removeAnchor(Object anchor) {
        Set<CcColorStateList.AnchorCallback> callbacks = mAnchorCallbacks.remove(anchor);
        if (callbacks != null) {
            for (CcColorStateList.AnchorCallback callback : callbacks) {
                Set<Object> anchors = mCallbackAnchors.get(callback);
                if (anchors != null) {
                    anchors.remove(anchor);
                    if (anchors.isEmpty()) {
                        mCallbackAnchors.remove(callback);
                    }
                }
            }
        }
    }

    public synchronized void removeCallback(CcColorStateList.AnchorCallback callback) {
        Set<Object> anchors = mCallbackAnchors.get(callback);
        if (anchors != null) {
            for (Object anchor : anchors) {
                Set<CcColorStateList.AnchorCallback> callbacks = mAnchorCallbacks.get(anchor);
                if (callbacks != null) {
                    callbacks.remove(callback);
                    if (callbacks.isEmpty()) {
                        mAnchorCallbacks.remove(anchor);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void invalidateColor(CcColorStateList color) {
        for (CcColorStateList.Callback callback : mCallbacks) {
            if (callback != null) {
                callback.invalidateColor(color);
            }
        }

        for (Object anchor : mAnchorCallbacks.keySet()) {
            Set<CcColorStateList.AnchorCallback> callbacks = mAnchorCallbacks.get(anchor);
            for (CcColorStateList.AnchorCallback callback : callbacks) {
                if (callback != null) {
                    callback.invalidateColor(anchor, color);
                }
            }
        }
    }
}
