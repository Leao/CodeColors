package io.leao.codecolors.core.color;

import android.app.Activity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import io.leao.codecolors.core.color.CallbackHandler.PairReference;
import io.leao.codecolors.core.color.CallbackHandler.Reference;

class CallbackManager {
    protected Map<Activity, CallbackHandler> mActivityHandler = new WeakHashMap<>();
    // Active CallbackHandlers.
    protected Set<CallbackHandler> mCallbackHandlers = new HashSet<>();
    // Last active CallbackHandler. If mCallbackHandlers.size() == 1,
    // this handler is directly used, instead of iteration.
    // Optimization to avoid instantiating an Iterator whenever possible. (too much?)
    protected CallbackHandler mCallbackHandler;

    public synchronized void onActivityCreated(Activity activity) {
        CallbackHandler callbackHandler = getCallbackHandler(activity);
        mCallbackHandlers.add(callbackHandler);

        if (mCallbackHandlers.size() == 1) {
            mCallbackHandler = callbackHandler;
        } else {
            mCallbackHandler = null;
        }
    }

    public synchronized void onActivityResumed(Activity activity) {
        CallbackHandler callbackHandler = getCallbackHandler(activity);
        mCallbackHandlers.add(callbackHandler);

        if (mCallbackHandlers.size() == 1) {
            mCallbackHandler = callbackHandler;
        } else {
            mCallbackHandler = null;
        }
    }

    public synchronized void onActivityPaused(Activity activity) {
        CallbackHandler callbackHandler = getCallbackHandler(activity);
        mCallbackHandlers.remove(callbackHandler);

        if (mCallbackHandlers.size() == 1) {
            mCallbackHandler = mCallbackHandlers.iterator().next();
        } else {
            mCallbackHandler = null;
        }
    }

    public synchronized void onActivityDestroyed(Activity activity) {
        mActivityHandler.remove(activity);
    }

    public synchronized CallbackHandler getCallbackHandler(Activity activity) {
        CallbackHandler callbackHandler = mActivityHandler.get(activity);
        if (callbackHandler == null) {
            callbackHandler = new CallbackHandler();
            mActivityHandler.put(activity, callbackHandler);
        }
        return callbackHandler;
    }

    public synchronized void invalidate(
            final CcColorStateList color,
            Set<Reference<CcColorStateList.SingleCallback>> invalidatedSingleCallbacks,
            Set<PairReference<CcColorStateList.AnchorCallback, Object>> invalidatedPairCallbacks) {

        if (mCallbackHandler != null) {
            invalidate(mCallbackHandler, color, invalidatedSingleCallbacks, invalidatedPairCallbacks);
        } else {
            for (CallbackHandler callbackHandler : mCallbackHandlers) {
                invalidate(callbackHandler, color, invalidatedSingleCallbacks, invalidatedPairCallbacks);
            }
        }
    }

    public synchronized void invalidate(
            CallbackHandler callbackHandler,
            final CcColorStateList color,
            Set<Reference<CcColorStateList.SingleCallback>> invalidatedSingleCallbacks,
            Set<PairReference<CcColorStateList.AnchorCallback, Object>> invalidatedPairCallbacks) {

        callbackHandler.iterateCallbacks(
                invalidatedSingleCallbacks,
                invalidatedPairCallbacks,
                new CallbackHandler.OnIterateCallbackListener() {
                    @Override
                    public void onIterateSingleCallback(Reference<CcColorStateList.SingleCallback> callbackReference,
                                                        CcColorStateList.SingleCallback callback) {
                        callback.invalidateColor(color);
                    }

                    @Override
                    public void onIteratePairCallback(
                            PairReference<CcColorStateList.AnchorCallback, Object> pairReference,
                            CcColorStateList.AnchorCallback callback, Object anchor) {

                        callback.invalidateColor(anchor, color);
                    }
                }
        );
    }

    public synchronized void invalidateMultiple(
            final CcColorStateList color,
            final Set<CcColorStateList> colors,
            Set<Reference<CcColorStateList.SingleCallback>> invalidatedSingleCallbacks,
            Set<PairReference<CcColorStateList.AnchorCallback, Object>> invalidatedPairCallbacks,
            final Set<CcColorStateList> invalidateColors) {

        if (mCallbackHandler != null) {
            invalidateMultiple(
                    mCallbackHandler,
                    color,
                    colors,
                    invalidatedSingleCallbacks,
                    invalidatedPairCallbacks,
                    invalidateColors);
        } else {
            for (CallbackHandler callbackHandler : mCallbackHandlers) {
                invalidateMultiple(
                        callbackHandler,
                        color,
                        colors,
                        invalidatedSingleCallbacks,
                        invalidatedPairCallbacks,
                        invalidateColors);
            }
        }
    }

    public synchronized void invalidateMultiple(
            CallbackHandler callbackHandler,
            final CcColorStateList color,
            final Set<CcColorStateList> colors,
            Set<Reference<CcColorStateList.SingleCallback>> invalidatedSingleCallbacks,
            Set<PairReference<CcColorStateList.AnchorCallback, Object>> invalidatedPairCallbacks,
            final Set<CcColorStateList> invalidateColors) {

        callbackHandler.iterateCallbacks(
                invalidatedSingleCallbacks,
                invalidatedPairCallbacks,
                new CallbackHandler.OnIterateCallbackListener() {
                    @Override
                    public void onIterateSingleCallback(Reference<CcColorStateList.SingleCallback> callbackReference,
                                                        CcColorStateList.SingleCallback callback) {
                        CcColorStateList lastInvalidateColor = null;

                        invalidateColors.clear();
                        for (CcColorStateList c : colors) {
                            if (c == color || c.getCallbackManager().containsCallback(callbackReference)) {
                                invalidateColors.add(c);
                                lastInvalidateColor = c;
                            }
                        }

                        if (invalidateColors.size() == 1) {
                            callback.invalidateColor(lastInvalidateColor);
                        } else {
                            callback.invalidateColors(invalidateColors);
                        }
                    }

                    @Override
                    public void onIteratePairCallback(
                            PairReference<CcColorStateList.AnchorCallback, Object> pairReference,
                            CcColorStateList.AnchorCallback callback, Object anchor) {

                        CcColorStateList lastInvalidateColor = null;

                        invalidateColors.clear();
                        for (CcColorStateList c : colors) {
                            if (c == color || c.getCallbackManager().containsPairCallback(pairReference)) {
                                invalidateColors.add(c);
                                lastInvalidateColor = c;
                            }
                        }

                        if (invalidateColors.size() == 1) {
                            callback.invalidateColor(anchor, lastInvalidateColor);
                        } else {
                            callback.invalidateColors(anchor, invalidateColors);
                        }
                    }
                }
        );
    }

    public synchronized void addCallback(CcColorStateList.SingleCallback callback) {
        if (mCallbackHandler != null) {
            mCallbackHandler.addCallback(callback);
        } else {
            for (CallbackHandler callbackHandler : mCallbackHandlers) {
                callbackHandler.addCallback(callback);
            }
        }
    }

    public synchronized boolean containsCallback(CcColorStateList.SingleCallback callback) {
        if (mCallbackHandler != null) {
            return mCallbackHandler.containsCallback(callback);
        } else {
            for (CallbackHandler callbackHandler : mCallbackHandlers) {
                if (callbackHandler.containsCallback(callback)) {
                    return true;
                }
            }
        }
        return false;
    }

    synchronized boolean containsCallback(Reference<CcColorStateList.SingleCallback> callbackReference) {
        if (mCallbackHandler != null) {
            return mCallbackHandler.containsCallback(callbackReference);
        } else {
            for (CallbackHandler callbackHandler : mCallbackHandlers) {
                if (callbackHandler.containsCallback(callbackReference)) {
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized void removeCallback(CcColorStateList.SingleCallback callback) {
        if (mCallbackHandler != null) {
            mCallbackHandler.removeCallback(callback);
        } else {
            for (CallbackHandler callbackHandler : mCallbackHandlers) {
                callbackHandler.removeCallback(callback);
            }
        }
    }

    public synchronized void addPairCallback(CcColorStateList.AnchorCallback callback, Object anchor) {
        if (mCallbackHandler != null) {
            mCallbackHandler.addPairCallback(callback, anchor);
        } else {
            for (CallbackHandler callbackHandler : mCallbackHandlers) {
                callbackHandler.addPairCallback(callback, anchor);
            }
        }
    }

    synchronized boolean containsPairCallback(CcColorStateList.AnchorCallback callback, Object anchor) {
        if (mCallbackHandler != null) {
            return mCallbackHandler.containsPairCallback(callback, anchor);
        } else {
            for (CallbackHandler callbackHandler : mCallbackHandlers) {
                if (callbackHandler.containsPairCallback(callback, anchor)) {
                    return true;
                }
            }
        }
        return false;
    }

    synchronized boolean containsPairCallback(PairReference<CcColorStateList.AnchorCallback, Object> pairReference) {
        if (mCallbackHandler != null) {
            return mCallbackHandler.containsPairCallback(pairReference);
        } else {
            for (CallbackHandler callbackHandler : mCallbackHandlers) {
                if (callbackHandler.containsPairCallback(pairReference)) {
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized void removePairCallback(CcColorStateList.AnchorCallback callback,
                                                Object anchor) {
        if (mCallbackHandler != null) {
            mCallbackHandler.removePairCallback(callback, anchor);
        } else {
            for (CallbackHandler callbackHandler : mCallbackHandlers) {
                callbackHandler.removePairCallback(callback, anchor);
            }
        }
    }

    public synchronized void removeCallback(CcColorStateList.AnchorCallback callback) {
        if (mCallbackHandler != null) {
            mCallbackHandler.removeCallback(callback);
        } else {
            for (CallbackHandler callbackHandler : mCallbackHandlers) {
                callbackHandler.removeCallback(callback);
            }
        }
    }

    public synchronized void removeAnchor(Object anchor) {
        if (mCallbackHandler != null) {
            mCallbackHandler.removeAnchor(anchor);
        } else {
            for (CallbackHandler callbackHandler : mCallbackHandlers) {
                callbackHandler.removeAnchor(anchor);
            }
        }
    }
}
