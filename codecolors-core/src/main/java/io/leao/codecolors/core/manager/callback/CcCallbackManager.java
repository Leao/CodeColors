package io.leao.codecolors.core.manager.callback;

import android.app.Activity;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import io.leao.codecolors.core.color.CcColorStateList;

public class CcCallbackManager {
    protected Set<Activity> mActivities = Collections.newSetFromMap(new WeakHashMap<Activity, Boolean>());
    protected Map<Activity, Map<CcColorStateList, CallbackHandler>> mActivityColorHandler = new WeakHashMap<>();

    public synchronized void onActivityCreated(Activity activity) {
        mActivities.add(activity);
    }

    public synchronized void onActivityResumed(Activity activity) {
        mActivities.add(activity);
    }

    public synchronized void onActivityPaused(Activity activity) {
        mActivities.remove(activity);
    }

    public synchronized void onActivityDestroyed(Activity activity) {
        mActivityColorHandler.remove(activity);
    }

    protected synchronized CallbackHandler getCallbackHandler(Activity activity, CcColorStateList color) {
        Map<CcColorStateList, CallbackHandler> colorHandler = mActivityColorHandler.get(activity);
        if (colorHandler == null) {
            colorHandler = new WeakHashMap<>();
            mActivityColorHandler.put(activity, colorHandler);
        }
        CallbackHandler handler = colorHandler.get(color);
        if (handler == null) {
            handler = new CallbackHandler(color);
            colorHandler.put(color, handler);
        }
        return handler;
    }

    public synchronized void addCallback(CcColorStateList color, CcColorStateList.SingleCallback callback) {
        for (Activity activity : mActivities) {
            CallbackHandler handler = getCallbackHandler(activity, color);
            handler.addCallback(callback);
        }
    }

    public synchronized boolean containsCallback(CcColorStateList color, CcColorStateList.SingleCallback callback) {
        for (Activity activity : mActivities) {
            CallbackHandler handler = getCallbackHandler(activity, color);
            if (handler.containsCallback(callback)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void removeCallback(CcColorStateList color, CcColorStateList.SingleCallback callback) {
        for (Activity activity : mActivities) {
            CallbackHandler handler = getCallbackHandler(activity, color);
            handler.removeCallback(callback);
        }
    }

    public synchronized void addPairCallback(CcColorStateList color, CcColorStateList.AnchorCallback callback,
                                             Object anchor) {
        for (Activity activity : mActivities) {
            CallbackHandler handler = getCallbackHandler(activity, color);
            handler.addPairCallback(callback, anchor);
        }
    }

    public synchronized boolean containsPairCallback(CcColorStateList color, CcColorStateList.AnchorCallback callback,
                                                     Object anchor) {
        for (Activity activity : mActivities) {
            CallbackHandler handler = getCallbackHandler(activity, color);
            if (handler.containsPairCallback(callback, anchor)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void removePairCallback(CcColorStateList color, CcColorStateList.AnchorCallback callback,
                                                Object anchor) {
        for (Activity activity : mActivities) {
            CallbackHandler handler = getCallbackHandler(activity, color);
            handler.removePairCallback(callback, anchor);
        }
    }

    public synchronized void removeCallback(CcColorStateList color, CcColorStateList.AnchorCallback callback) {
        for (Activity activity : mActivities) {
            CallbackHandler handler = getCallbackHandler(activity, color);
            handler.removeCallback(callback);
        }
    }

    public synchronized void removeAnchor(CcColorStateList color, Object anchor) {
        for (Activity activity : mActivities) {
            CallbackHandler handler = getCallbackHandler(activity, color);
            handler.removeAnchor(anchor);
        }
    }

    public synchronized void invalidate(CcColorStateList color) {
        Set<CallbackHandler.Reference<CcColorStateList.SingleCallback>> invalidatedSingleCallbacks =
                CallbackTempUtils.getSingleCallbackSet();
        Set<CallbackHandler.PairReference<CcColorStateList.AnchorCallback, Object>> invalidatedPairCallbacks =
                CallbackTempUtils.getPairCallbackSet();

        for (Activity activity : mActivities) {
            CallbackHandler handler = getCallbackHandler(activity, color);
            if (handler != null) {
                handler.invalidate(invalidatedSingleCallbacks, invalidatedPairCallbacks);
            }
        }

        CallbackTempUtils.recycleSingleCallbackSet(invalidatedSingleCallbacks);
        CallbackTempUtils.recyclePairCallbackSet(invalidatedPairCallbacks);
    }

    public synchronized void invalidateMultiple(Set<CcColorStateList> colors) {
        Set<CallbackHandler.Reference<CcColorStateList.SingleCallback>> invalidatedSingleCallbacks =
                CallbackTempUtils.getSingleCallbackSet();
        Set<CallbackHandler.PairReference<CcColorStateList.AnchorCallback, Object>> invalidatedPairCallbacks =
                CallbackTempUtils.getPairCallbackSet();

        for (Activity activity : mActivities) {
            for (CcColorStateList color : colors) {
                CallbackHandler handler = getCallbackHandler(activity, color);
                if (handler != null) {
                    handler.invalidateMultiple(colors, invalidatedSingleCallbacks, invalidatedPairCallbacks);
                }
            }
        }

        CallbackTempUtils.recycleSingleCallbackSet(invalidatedSingleCallbacks);
        CallbackTempUtils.recyclePairCallbackSet(invalidatedPairCallbacks);
    }
}
