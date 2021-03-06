package io.leao.codecolors.core.drawable;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.os.Build;
import android.util.StateSet;
import android.view.View;

public class CcDrawableUtils {
    public static Activity getActivity(Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return getActivity(((ContextWrapper) context).getBaseContext());
        } else {
            return null;
        }
    }

    public static Context getContext(View view) {
        return view != null ? view.getContext() : null;
    }

    public static View getView(Drawable rootDrawable) {
        Drawable.Callback callback = rootDrawable.getCallback();
        return callback instanceof View ? (View) callback : null;
    }

    public static Drawable getRootDrawable(Drawable drawable) {
        Drawable.Callback callback = drawable.getCallback();
        if (callback instanceof Drawable) {
            return getRootDrawable((Drawable) callback);
        } else {
            return drawable;
        }
    }

    public static void forceStateChange(Drawable drawable) {
        forceStateChange(drawable, true);
    }

    public static void forceStateChange(Drawable drawable, boolean forceOnChildren) {
        drawable.setState(getForceState(drawable.getState()));

        if (forceOnChildren) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                forceStateChangeOnChildrenCompat(drawable);
            } else {
                forceStateChangeOnChildren(drawable);
            }
        }
    }

    private static void forceStateChangeOnChildrenCompat(Drawable drawable) {
        Drawable.ConstantState drawableState = drawable.getConstantState();
        if (drawableState instanceof DrawableContainer.DrawableContainerState) {
            DrawableContainer.DrawableContainerState drawableContainerState =
                    (DrawableContainer.DrawableContainerState) drawableState;

            Drawable[] children = drawableContainerState.getChildren();
            if (children != null) {
                for (Drawable child : children) {
                    forceStateChange(child, true);
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static void forceStateChangeOnChildren(Drawable drawable) {
        Drawable.ConstantState drawableState = drawable.getConstantState();
        if (drawableState instanceof DrawableContainer.DrawableContainerState) {
            DrawableContainer.DrawableContainerState drawableContainerState =
                    (DrawableContainer.DrawableContainerState) drawableState;

            for (int i = 0; i < drawableContainerState.getChildCount(); i++) {
                Drawable child = drawableContainerState.getChild(i);
                forceStateChange(child, true);
            }
        }
    }

    /**
     * Returns a new state that will seem to have changed in {@link Drawable#setState(int[])}, allowing
     * {@link Drawable#onStateChange(int[])} to be called.
     * <p>
     * However, the returned state is the same state with 0 (nothing) as the last item, or the reverse of that.
     */
    public static int[] getForceState(int[] state) {
        if (state.length == 0) {
            return StateSet.NOTHING;
        } else if (state[state.length - 1] == 0) {
            return removeFrom(state);
        } else {
            return addTo(state);
        }
    }

    private static int[] removeFrom(int[] state) {
        int[] newState = new int[state.length - 1];
        for (int i = 0; i < newState.length; i++) {
            newState[i] = state[i];
        }
        return newState;
    }

    private static int[] addTo(int[] state) {
        int[] newState = new int[state.length + 1];
        for (int i = 0; i < state.length; i++) {
            newState[i] = state[i];
        }
        return newState;
    }
}
