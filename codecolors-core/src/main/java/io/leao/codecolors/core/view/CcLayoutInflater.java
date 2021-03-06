package io.leao.codecolors.core.view;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.xmlpull.v1.XmlPullParser;

import java.lang.reflect.Field;

import io.leao.codecolors.core.CcCore;

public class CcLayoutInflater extends LayoutInflater {
    private static final String LOG_TAG = CcLayoutInflater.class.getSimpleName();

    private static boolean sCheckedLayoutInflaterFactory2Field;
    private static Field sLayoutInflaterFactory2Field;

    private static final String[] sClassPrefixes = {"android.widget.", "android.webkit."};

    private Object[] mConstructorArgs;

    protected CcLayoutInflater(Context context) {
        super(context);
        init(context);
    }

    protected CcLayoutInflater(LayoutInflater original, Context newContext) {
        super(original, newContext);
        init(newContext);
    }

    private void init(Context context) {
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

        Resources resources = context.getResources();
        CcCore.getActivityManager().onConfigurationChanged(resources.getConfiguration(), resources);
    }

    @Override
    public View inflate(XmlPullParser parser, ViewGroup root, boolean attachToRoot) {
        ensureFactory2();
        return super.inflate(parser, root, attachToRoot);
    }

    protected CcLayoutInflaterFactoryWrapper onCreateFactoryWrapper(Factory2 factory) {
        return new CcLayoutInflaterFactoryWrapper(this, factory);
    }

    /**
     * Ensures that the factory is an instance of CcLayoutInflaterFactoryWrapper.
     */
    private void ensureFactory2() {
        Factory2 factory2 = getFactory2();
        if (!(factory2 instanceof CcLayoutInflaterFactoryWrapper)) {
            forceSetFactory2(onCreateFactoryWrapper(factory2));
        }
    }

    private void forceSetFactory2(Factory2 factory) {
        if (!sCheckedLayoutInflaterFactory2Field) {
            try {
                sLayoutInflaterFactory2Field = LayoutInflater.class.getDeclaredField("mFactory2");
                sLayoutInflaterFactory2Field.setAccessible(true);
            } catch (NoSuchFieldException e) {
                Log.e(LOG_TAG, "Could not find field 'mFactory2' on class " + LayoutInflater.class.getName() +
                        "; inflation may have unexpected results.", e);
            }
            sCheckedLayoutInflaterFactory2Field = true;
        }
        if (sLayoutInflaterFactory2Field != null) {
            try {
                sLayoutInflaterFactory2Field.set(this, factory);
            } catch (IllegalAccessException e) {
                Log.e(LOG_TAG, "Could not set the Factory2 on LayoutInflater " + this +
                        "; inflation may have unexpected results.", e);
            }
        }
    }

    /**
     * Called by {@link CcLayoutInflaterFactoryWrapper}.
     */
    View createViewFromTag(Context context, String name, AttributeSet attrs) {
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
        } catch (ClassNotFoundException e) {
            // In this case we want to let the base class take a crack at it.
        } finally {
            mConstructorArgs[0] = lastContext;
        }

        return view;
    }

    @Override
    public CcLayoutInflater cloneInContext(Context newContext) {
        return new CcLayoutInflater(this, newContext);
    }

    public static CcLayoutInflater copy(LayoutInflater inflater) {
        return new CcLayoutInflater(inflater, inflater.getContext());
    }
}