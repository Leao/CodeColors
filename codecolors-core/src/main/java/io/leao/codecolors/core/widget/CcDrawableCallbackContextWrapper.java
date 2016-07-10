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
    private WeakReference<CcDrawableCallbackContextWrapperHost> mHost;

    private CcDrawableCallbackResourcesWrapper mResources;
    private CcDrawableCallbackWindowManagerWrapper mWindowManager;

    public static Context wrap(Context context) {
        if (!(context instanceof CcDrawableCallbackContextWrapper)) {
            context = new CcDrawableCallbackContextWrapper(context);
        }
        return context;
    }

    public static CcDrawableCallbackContextWrapper init(Context context, CcDrawableCallbackContextWrapperHost host) {
        CcDrawableCallbackContextWrapper contextWrapper = get(context);
        contextWrapper.setHost(host);
        return contextWrapper;
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

    private CcDrawableCallbackContextWrapperHost getHost() {
        return mHost != null ? mHost.get() : null;
    }

    private void setHost(CcDrawableCallbackContextWrapperHost host) {
        mHost = new WeakReference<>(host);
    }

    @Override
    public CcDrawableCallbackResourcesWrapper getResources() {
        if (mResources == null) {
            mResources = new CcDrawableCallbackResourcesWrapper(super.getResources());
        }
        return mResources;
    }

    @Override
    public Object getSystemService(@NonNull String name) {
        Object systemService = super.getSystemService(name);

        if (WINDOW_SERVICE.equals(name)) {
            if (mWindowManager == null || mWindowManager.getBaseWindowManager() != systemService) {
                mWindowManager = new CcDrawableCallbackWindowManagerWrapper((WindowManager) systemService);
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
        if (mWindowManager != null && mWindowManager.hasViews() && mResources.verifyDrawable(drawable)) {
            mWindowManager.invalidateViews();
        }
    }

    /**
     * Intercepts the creation of drawables that are related with the original view, making sure they have a proper
     * callback, which allows the CodeColors wrappers to properly tint and invalidate them.
     */
    public class CcDrawableCallbackResourcesWrapper extends ResourcesWrapper {
        private Set<Drawable> mDrawables = Collections.newSetFromMap(new WeakHashMap<Drawable, Boolean>());

        public CcDrawableCallbackResourcesWrapper(Resources resources) {
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
                CcDrawableCallbackContextWrapperHost host = getHost();
                if (host != null) {
                    drawable.setCallback(host);
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
    public class CcDrawableCallbackWindowManagerWrapper extends WindowManagerWrapper {
        private Set<View> mViews = Collections.newSetFromMap(new WeakHashMap<View, Boolean>());

        public CcDrawableCallbackWindowManagerWrapper(WindowManager windowManager) {
            super(windowManager);
        }

        @Override
        public void addView(View view, ViewGroup.LayoutParams params) {
            super.addView(view, params);

            addChildViews(view);
        }

        public void addChildViews(View view) {
            if (view != null) {
                if (view instanceof ViewGroup) {
                    ViewGroup viewGroup = (ViewGroup) view;
                    int N = viewGroup.getChildCount();
                    for (int i = 0; i < N; i++) {
                        addChildViews(viewGroup.getChildAt(i));
                    }
                } else {
                    CcDrawableCallbackContextWrapperHost host = getHost();
                    // Store only views accepted by the host view, to reduce the impact of views' invalidation.
                    if (host != null && host.onAddDrawableCallbackView(view)) {
                        mViews.add(view);
                    }
                }
            }
        }

        @Override
        public void removeView(View view) {
            removeChildViews(view);

            super.removeView(view);
        }

        @Override
        public void removeViewImmediate(View view) {
            removeChildViews(view);

            super.removeViewImmediate(view);
        }

        public void removeChildViews(View view) {
            if (view != null) {
                if (view instanceof ViewGroup) {
                    ViewGroup viewGroup = (ViewGroup) view;
                    int N = viewGroup.getChildCount();
                    for (int i = 0; i < N; i++) {
                        removeChildViews(viewGroup.getChildAt(i));
                    }
                } else {
                    mViews.remove(view);
                }
            }
        }

        public boolean hasViews() {
            return mViews.size() > 0;
        }

        public void invalidateViews() {
            for (View view : mViews) {
                view.invalidate();
            }
        }
    }

    public interface CcDrawableCallbackContextWrapperHost extends Drawable.Callback {
        boolean onAddDrawableCallbackView(View view);
    }
}