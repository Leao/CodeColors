package io.leao.codecolors.core.color;

import android.app.Activity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import io.leao.codecolors.core.CcCore;
import io.leao.codecolors.core.color.CodeColor.AnchorCallback;
import io.leao.codecolors.core.color.CodeColor.SingleCallback;

class CallbackHandlerManager {
    protected CcColorStateList mColor;

    protected Map<Activity, CallbackHandler> mActivityHandler = new WeakHashMap<>();
    // Active CallbackHandlers.
    protected Set<CallbackHandler> mCallbackHandlers = new HashSet<>();
    protected Set<CallbackHandler> mInvalidatedCallbackHandlers = new HashSet<>();
    // Last active CallbackHandler. If mCallbackHandlers.size() == 1,
    // this handler is directly used, instead of iteration.
    // Optimization to avoid instantiating an Iterator whenever possible. (too much?)
    protected CallbackHandler mCallbackHandler;

    public CallbackHandlerManager(CcColorStateList color) {
        mColor = color;
    }

    public synchronized void onActivityCreated(Activity activity) {
        CallbackHandler callbackHandler = addCallbackHandler(activity);

        if (mCallbackHandlers.size() == 1) {
            mCallbackHandler = callbackHandler;
        } else {
            mCallbackHandler = null;
        }
    }

    public synchronized void onActivityResumed(Activity activity) {
        CallbackHandler callbackHandler = addCallbackHandler(activity);

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

    protected synchronized CallbackHandler addCallbackHandler(Activity activity) {
        CallbackHandler callbackHandler = getCallbackHandler(activity);
        mCallbackHandlers.add(callbackHandler);

        // If there was an invalidation, and this callback was not invalidated, make sure to invalidate it.
        if (!mInvalidatedCallbackHandlers.contains(callbackHandler)) {
            invalidate(callbackHandler, null, null);
        }

        return callbackHandler;
    }

    protected synchronized CallbackHandler getCallbackHandler(Activity activity) {
        CallbackHandler callbackHandler = mActivityHandler.get(activity);
        if (callbackHandler == null) {
            callbackHandler = new CallbackHandler();
            mActivityHandler.put(activity, callbackHandler);
            // A new handler is invalidated by default.
            mInvalidatedCallbackHandlers.add(callbackHandler);
        }
        return callbackHandler;
    }

    public synchronized void invalidate(Set<Reference<SingleCallback>> invalidatedSingleCallbacks,
                                        Set<ReferencePair<Object, AnchorCallback>> invalidatedPairCallbacks) {
        // Clear invalidated CallbackHandlers.
        mInvalidatedCallbackHandlers.clear();

        if (mCallbackHandler != null) {
            invalidate(mCallbackHandler, null, null);
        } else if (mCallbackHandlers.size() > 0) {
            for (CallbackHandler callbackHandler : mCallbackHandlers) {
                invalidate(callbackHandler, invalidatedSingleCallbacks, invalidatedPairCallbacks);
            }
        }
    }

    private synchronized void invalidate(CallbackHandler callbackHandler,
                                         Set<Reference<SingleCallback>> invalidatedSingleCallbacks,
                                         Set<ReferencePair<Object, AnchorCallback>> invalidatedPairCallbacks) {
        // Add CallbackHandler as invalidated.
        mInvalidatedCallbackHandlers.add(callbackHandler);

        callbackHandler.iterateCallbacks(
                invalidatedSingleCallbacks,
                invalidatedPairCallbacks,
                new CallbackHandler.OnIterateCallbackListener() {
                    @Override
                    public void onIterateSingleCallback(Reference<SingleCallback> callbackReference,
                                                        SingleCallback callback) {
                        callback.invalidateColor(mColor);
                    }

                    @Override
                    public void onIteratePairCallback(ReferencePair<Object, AnchorCallback> referencePair,
                                                      Object anchor, AnchorCallback callback) {
                        callback.invalidateColor(anchor, mColor);
                    }
                }
        );
    }

    public synchronized void invalidateMultiple(final Set<CcColorStateList> colors,
                                                Set<Reference<SingleCallback>> invalidatedSingleCallbacks,
                                                Set<ReferencePair<Object, AnchorCallback>> invalidatedPairCallbacks,
                                                final Set<CcColorStateList> invalidateColors) {
        // Clear invalidated CallbackHandlers.
        mInvalidatedCallbackHandlers.clear();

        if (mCallbackHandler != null) {
            invalidateMultiple(
                    mCallbackHandler,
                    colors,
                    invalidatedSingleCallbacks,
                    invalidatedPairCallbacks,
                    invalidateColors);
        } else if (mCallbackHandlers.size() > 0) {
            for (CallbackHandler callbackHandler : mCallbackHandlers) {
                invalidateMultiple(
                        callbackHandler,
                        colors,
                        invalidatedSingleCallbacks,
                        invalidatedPairCallbacks,
                        invalidateColors);
            }
        }
    }

    private synchronized void invalidateMultiple(CallbackHandler callbackHandler,
                                                 final Set<CcColorStateList> colors,
                                                 Set<Reference<SingleCallback>> invalidatedSingleCallbacks,
                                                 Set<ReferencePair<Object, AnchorCallback>> invalidatedPairCallbacks,
                                                 final Set<CcColorStateList> invalidateColors) {
        // Add CallbackHandler as invalidated.
        mInvalidatedCallbackHandlers.add(callbackHandler);

        callbackHandler.iterateCallbacks(
                invalidatedSingleCallbacks,
                invalidatedPairCallbacks,
                new CallbackHandler.OnIterateCallbackListener() {
                    @Override
                    public void onIterateSingleCallback(Reference<SingleCallback> callbackReference,
                                                        SingleCallback callback) {
                        CcColorStateList lastInvalidateColor = null;

                        invalidateColors.clear();
                        for (CcColorStateList c : colors) {
                            if (c == mColor || c.getCallbackHandlerManager().containsCallback(callbackReference)) {
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
                    public void onIteratePairCallback(ReferencePair<Object, AnchorCallback> referencePair,
                                                      Object anchor, AnchorCallback callback) {
                        CcColorStateList lastInvalidateColor = null;

                        invalidateColors.clear();
                        for (CcColorStateList c : colors) {
                            if (c == mColor || c.getCallbackHandlerManager().containsPairCallback(referencePair)) {
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

    public synchronized void addCallback(Activity activity, SingleCallback callback) {
        if (activity == null) {
            activity = CcCore.getActivityManager().getLastResumedActivity();
        }
        if (activity != null) {
            getCallbackHandler(activity).addCallback(callback);
        }
    }

    public synchronized boolean containsCallback(Activity activity, SingleCallback callback) {
        if (activity == null) {
            activity = CcCore.getActivityManager().getLastResumedActivity();
        }
        return activity != null && getCallbackHandler(activity).containsCallback(callback);
    }

    synchronized boolean containsCallback(Reference<SingleCallback> callbackReference) {
        if (mCallbackHandler != null) {
            return mCallbackHandler.containsCallback(callbackReference);
        } else if (mCallbackHandlers.size() > 0) {
            for (CallbackHandler callbackHandler : mCallbackHandlers) {
                if (callbackHandler.containsCallback(callbackReference)) {
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized void removeCallback(Activity activity, SingleCallback callback) {
        if (activity == null) {
            activity = CcCore.getActivityManager().getLastResumedActivity();
        }
        if (activity != null) {
            getCallbackHandler(activity).removeCallback(callback);
        }
    }

    public synchronized void addPairCallback(Activity activity, Object anchor, AnchorCallback callback) {
        if (activity == null) {
            activity = CcCore.getActivityManager().getLastResumedActivity();
        }
        if (activity != null) {
            getCallbackHandler(activity).addPairCallback(anchor, callback);
        }
    }

    public synchronized boolean containsPairCallback(Activity activity, Object anchor, AnchorCallback callback) {
        if (activity == null) {
            activity = CcCore.getActivityManager().getLastResumedActivity();
        }
        return activity != null && getCallbackHandler(activity).containsPairCallback(anchor, callback);
    }

    synchronized boolean containsPairCallback(ReferencePair<Object, AnchorCallback> referencePair) {
        if (mCallbackHandler != null) {
            return mCallbackHandler.containsPairCallback(referencePair);
        } else if (mCallbackHandlers.size() > 0) {
            for (CallbackHandler callbackHandler : mCallbackHandlers) {
                if (callbackHandler.containsPairCallback(referencePair)) {
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized void removePairCallback(Activity activity, Object anchor, AnchorCallback callback) {
        if (activity == null) {
            activity = CcCore.getActivityManager().getLastResumedActivity();
        }
        if (activity != null) {
            getCallbackHandler(activity).removePairCallback(anchor, callback);
        }
    }

    public synchronized void removeAnchor(Activity activity, Object anchor) {
        if (activity == null) {
            activity = CcCore.getActivityManager().getLastResumedActivity();
        }
        if (activity != null) {
            getCallbackHandler(activity).removeAnchor(anchor);
        }
    }

    public synchronized void removeCallback(Activity activity, AnchorCallback callback) {
        if (activity == null) {
            activity = CcCore.getActivityManager().getLastResumedActivity();
        }
        if (activity != null) {
            getCallbackHandler(activity).removeCallback(callback);
        }
    }
}
