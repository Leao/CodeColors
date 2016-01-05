package io.leao.codecolors.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.util.Set;

import io.leao.codecolors.R;
import io.leao.codecolors.manager.CcColorsManager;
import io.leao.codecolors.manager.CcDependenciesManager;
import io.leao.codecolors.res.CcColorStateList;

public class CcLayoutInflater extends LayoutInflater implements LayoutInflater.Factory2 {

    private static final String[] sClassPrefixes = {"android.widget.", "android.webkit."};

    protected Object[] mConstructorArgs;

    protected CcColorsManager mColorsManager;
    protected CcDependenciesManager mDependenciesManager;

    protected CcLayoutInflater(Context context) {
        super(context);
        init(context);
    }

    protected CcLayoutInflater(LayoutInflater original, Context newContext) {
        super(original, newContext);
        init(newContext);
    }

    private void init(Context context) {
        // Set factory if needed. If it's already set through cloning, do not reset it.
        if (!(getFactory() instanceof CcLayoutInflater)) {
            setFactory2(this);
        }

        // Get object args through reflection.
        try {
            Field mConstructorArgsField = LayoutInflater.class.getDeclaredField("mConstructorArgs");
            mConstructorArgsField.setAccessible(true);
            mConstructorArgs = (Object[]) mConstructorArgsField.get(this);
        } catch (Exception e) {
            // Dummy object to avoid NullPointerExceptions.
            // Will cause layout issues (with Themes, etc.), but won't crash.
            mConstructorArgs = new Object[2];
        }

        mColorsManager = CcColorsManager.obtain(context);
        mColorsManager.onNewContext(context);

        mDependenciesManager = CcDependenciesManager.obtain(context);
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return onCreateView(name, context, attrs);
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return createViewFromTag(context, name, attrs);
    }

    protected View createViewFromTag(Context context, String name, AttributeSet attrs) {
        final Context lastContext = (Context) mConstructorArgs[0];
        mConstructorArgs[0] = context;
        View view = null;
        try {
            if (-1 == name.indexOf('.')) {
                for (String prefix : sClassPrefixes) {
                    try {
                        view = createView(name, prefix, attrs);
                        if (view != null) {
                            break;
                        }
                    } catch (ClassNotFoundException e) {
                        // Ignore and try with other prefixes.
                    }
                }
                if (view == null) {
                    view = onCreateView(name, attrs);
                }
            } else {
                view = createView(name, null, attrs);
            }

            // Add callbacks to refresh drawable states.
            addCodeColorCallbacks(context, attrs, view);

        } catch (ClassNotFoundException e) {
            // In this case we want to let the base class take a crack at it.
        } finally {
            mConstructorArgs[0] = lastContext;
        }

        return view;
    }

    private void addCodeColorCallbacks(Context context, AttributeSet attrs, View view) {
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CodeColors);
            try {
                final int N = ta.getIndexCount();
                for (int i = 0; i < N; i++) {
                    int attr = ta.getIndex(i);
                    if (attr == R.styleable.CodeColors_android_background ||
                            attr == R.styleable.CodeColors_backgroundTint) {
                        Drawable backgroundDrawable = view.getBackground();
                        if (backgroundDrawable != null) {
                            int resourceId = ta.getResourceId(attr, 0);
                            addCodeColorCallbacks(resourceId, backgroundDrawable, mDrawableInvalidateCallback);
                        }
                    } else if (attr == R.styleable.CodeColors_android_src) {
                        if (view instanceof ImageView) {
                            Drawable srcDrawable = ((ImageView) view).getDrawable();
                            if (srcDrawable != null) {
                                int resourceId = ta.getResourceId(attr, 0);
                                addCodeColorCallbacks(resourceId, srcDrawable, mDrawableInvalidateCallback);
                            }
                        }
                    }
                }
            } finally {
                ta.recycle();
            }
        }
    }

    private void addCodeColorCallbacks(int resourceId, Drawable drawable, CcColorStateList.AnchorCallback callback) {
        Set<Integer> dependencies = mDependenciesManager.resolveDependencies(resourceId);
        if (dependencies != null) {
            for (Integer dependency : dependencies) {
                CcColorStateList codeColor = mColorsManager.getColor(dependency);
                if (codeColor != null) {
                    codeColor.addCallback(drawable, callback);
                }
            }
        }
    }

    private static final CcColorStateList.AnchorCallback<Drawable> mDrawableInvalidateCallback =
            new CcColorStateList.AnchorCallback<Drawable>() {
                @Override
                public void invalidateColor(Drawable drawable, CcColorStateList color) {
                    if (drawable != null) {
                        final int[] state = drawable.getState();
                        // Force a state change to update the color.
                        drawable.setState(new int[]{0});
                        drawable.setState(state);
                        // Invalidate the drawable (invalidates the view).
                        drawable.invalidateSelf();
                    }
                }
            };

    @Override
    public LayoutInflater cloneInContext(Context newContext) {
        return new CcLayoutInflater(this, newContext);
    }

    public static CcLayoutInflater copy(LayoutInflater inflater) {
        return new CcLayoutInflater(inflater, inflater.getContext());
    }
}