package io.leao.codecolors.core.manager;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import io.leao.codecolors.core.CcCore;

/**
 * Receives the Activity life-cycle, and propagates its calls.
 * <p>
 * Configuration updates. Called by the initial setup and by activities when the configuration possibly changed, and
 * propagates the call if it truly changed.
 */
public class CcActivityManager {
    public static final WeakReference<Activity> EMPTY_ACTIVITY_REF = new WeakReference<>(null);

    private Map<Activity, WeakReference<Activity>> mActivityRefMap = new WeakHashMap<>();
    private WeakReference<Activity> mLastResumedActivityRef = EMPTY_ACTIVITY_REF;

    private Configuration mConfiguration;

    public synchronized void onActivityCreated(Activity activity) {
        setLastResumedActivityReference(activity);

        CcCore.getColorManager().onActivityCreated(activity);
    }

    public synchronized void onActivityResumed(Activity activity) {
        setLastResumedActivityReference(activity);

        CcCore.getColorManager().onActivityResumed(activity);
    }

    public synchronized void onActivityPaused(Activity activity) {
        CcCore.getColorManager().onActivityPaused(activity);
    }

    public synchronized void onActivityDestroyed(Activity activity) {
        if (mLastResumedActivityRef.get() == activity) {
            mLastResumedActivityRef = EMPTY_ACTIVITY_REF;
        }
        mActivityRefMap.remove(activity);

        CcCore.getColorManager().onActivityDestroyed(activity);
    }

    protected synchronized void setLastResumedActivityReference(Activity activity) {
        if (mLastResumedActivityRef.get() != activity) {
            mLastResumedActivityRef = getActivityReference(activity);
        }
    }

    public synchronized WeakReference<Activity> getActivityReference(Activity activity) {
        WeakReference<Activity> activityRef = mActivityRefMap.get(activity);
        if (activityRef == null) {
            activityRef = new WeakReference<>(activity);
            mActivityRefMap.put(activity, activityRef);
        }
        return activityRef;
    }

    /**
     * @return while the {@link WeakReference} object is never null, the inner {@link Activity} can be.
     */
    @NonNull
    public synchronized WeakReference<Activity> getLastResumedActivityReference() {
        return mLastResumedActivityRef;
    }

    @Nullable
    public synchronized Activity getLastResumedActivity() {
        return mLastResumedActivityRef.get();
    }

    public synchronized void onConfigurationChanged(Configuration configuration, Resources resources) {
        if (mConfiguration == null) {
            mConfiguration = new Configuration(configuration);
            onConfigurationCreated(resources, configuration);
        } else if (!configuration.equals(mConfiguration)) {
            mConfiguration.setTo(configuration);
            onConfigurationChanged(resources, configuration);
        }
    }

    protected synchronized void onConfigurationCreated(Resources resources, Configuration configuration) {
        CcCore.getColorManager().onConfigurationCreated(resources, configuration);
        CcCore.getDependencyManager().onConfigurationCreated(configuration);
    }

    protected synchronized void onConfigurationChanged(Resources resources, Configuration configuration) {
        CcCore.getColorManager().onConfigurationChanged(resources, configuration);
        CcCore.getDependencyManager().onConfigurationChanged(configuration);
    }
}
