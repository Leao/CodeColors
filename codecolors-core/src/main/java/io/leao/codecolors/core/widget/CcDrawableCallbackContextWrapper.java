package io.leao.codecolors.core.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import io.leao.codecolors.core.drawable.CcDrawableWrapper;

/**
 * Wrap the view's {@link Context}, in order to intercept the drawable creation and set the view as its callback.
 * <p>
 * By setting the view as the drawable's callback, the CodeColors drawable wrappers will be able to invalidate the
 * view when the code-colors are updated.
 */
public class CcDrawableCallbackContextWrapper extends ContextWrapper {
    private WeakReference<View> mView;

    private DrawableCallbackResourcesWrapper mResources;
    private DrawableCallbackWindowManagerWrapper mWindowManager;

    public static Context wrap(Context context) {
        if (!(context instanceof CcDrawableCallbackContextWrapper)) {
            context = new CcDrawableCallbackContextWrapper(context);
        }
        return context;
    }

    public static CcDrawableCallbackContextWrapper init(View view) {
        CcDrawableCallbackContextWrapper context = get(view.getContext());
        context.setView(view);
        return context;
    }

    private static CcDrawableCallbackContextWrapper get(Context context) {
        if (context instanceof CcDrawableCallbackContextWrapper) {
            return (CcDrawableCallbackContextWrapper) context;
        } else if (context instanceof ContextWrapper) {
            return get(((ContextWrapper) context).getBaseContext());
        } else {
            return null;
        }
    }

    private CcDrawableCallbackContextWrapper(Context base) {
        super(base);
    }

    private View getView() {
        return mView != null ? mView.get() : null;
    }

    private void setView(View view) {
        mView = new WeakReference<>(view);
    }

    @Override
    public DrawableCallbackResourcesWrapper getResources() {
        if (mResources == null) {
            mResources = new DrawableCallbackResourcesWrapper(super.getResources());
        }
        return mResources;
    }

    @Override
    public Object getSystemService(@NonNull String name) {
        Object systemService = super.getSystemService(name);

        if (WINDOW_SERVICE.equals(name)) {
            if (mWindowManager == null || mWindowManager.getBaseWindowManager() != systemService) {
                mWindowManager = new DrawableCallbackWindowManagerWrapper((WindowManager) systemService);
            }
            return mWindowManager;
        } else {
            return systemService;
        }
    }

    /**
     * Called when the drawable couldn't be verified in the original view.
     * <p>
     * Invalidates the views created by this Context's WindowManager, making sure that views like
     * {@link android.widget.Editor.SelectionStartHandleView} and {@link android.widget.Editor.SelectionEndHandleView}
     * are invalidated when code-colors are updated.
     */
    @SuppressWarnings("JavadocReference")
    public void invalidateDrawable(Drawable drawable) {
        if (mWindowManager != null && mResources.verifyDrawable(drawable)) {
            mWindowManager.invalidateViews();
        }
    }

    /**
     * Intercepts the creation of drawables that are related with the original view, making sure they have a proper
     * callback, which allows the CodeColors wrappers to properly tint and invalidate them.
     */
    public class DrawableCallbackResourcesWrapper extends ResourcesWrapper {
        private Set<Drawable> mDrawables = Collections.newSetFromMap(new WeakHashMap<Drawable, Boolean>());

        public DrawableCallbackResourcesWrapper(Resources resources) {
            super(resources);
        }

        @SuppressLint("PrivateResource")
        @Override
        public Drawable getDrawable(int id) throws Resources.NotFoundException {
            Drawable drawable = super.getDrawable(id);
            ensureDrawableCallback(drawable);
            return drawable;
        }

        @Override
        public Drawable getDrawable(int id, Resources.Theme theme) throws Resources.NotFoundException {
            Drawable drawable = super.getDrawable(id, theme);
            ensureDrawableCallback(drawable);
            return drawable;
        }

        private void ensureDrawableCallback(Drawable drawable) {
            if (drawable instanceof CcDrawableWrapper && drawable.getCallback() == null) {
                View view = getView();
                if (view != null) {
                    drawable.setCallback(view);
                    mDrawables.add(drawable);
                }
            }
        }

        public boolean verifyDrawable(Drawable drawable) {
            return mDrawables.contains(drawable);
        }
    }

    /**
     * Intercepts the creation of "children" views in {@link android.widget.PopupWindow}s, like
     * {@link android.widget.Editor.SelectionStartHandleView} and {@link android.widget.Editor.SelectionEndHandleView},
     * making it possible to invalidate them when some drawables are updated.
     */
    @SuppressWarnings("JavadocReference")
    public class DrawableCallbackWindowManagerWrapper extends WindowManagerWrapper {
        private Set<View> mViews = Collections.newSetFromMap(new WeakHashMap<View, Boolean>());

        public DrawableCallbackWindowManagerWrapper(WindowManager windowManager) {
            super(windowManager);
        }

        @Override
        public void addView(View view, ViewGroup.LayoutParams params) {
            super.addView(view, params);

            // Store only "simple" views, and not ViewGroups, as they could be very expensive to invalidate.
            if (!(view instanceof ViewGroup)) {
                mViews.add(view);
            }
        }

        public void invalidateViews() {
            for (View view : mViews) {
                view.invalidate();
            }
        }
    }
}