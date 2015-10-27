package io.leao.codecolors.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import java.lang.reflect.Field;

public class CodeColorsLayoutInflater extends LayoutInflater implements LayoutInflater.Factory2 {

    private static final String[] sClassPrefixes = {"android.widget.", "android.webkit."};

    protected Object[] mConstructorArgs;

    protected CodeColorsLayoutInflater(Context context) {
        super(context);
        init();
    }

    protected CodeColorsLayoutInflater(LayoutInflater original, Context newContext) {
        super(original, newContext);
        init();
    }

    private void init() {
        // Set factory if needed. If it's already set through cloning, do not reset it.
        if (!(getFactory() instanceof CodeColorsLayoutInflater)) {
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
        } catch (ClassNotFoundException e) {
            // In this case we want to let the base class take a crack at it.
        } finally {
            mConstructorArgs[0] = lastContext;
        }
        return view;
    }

    @Override
    public LayoutInflater cloneInContext(Context newContext) {
        return new CodeColorsLayoutInflater(this, newContext);
    }

    public static CodeColorsLayoutInflater copy(LayoutInflater inflater) {
        return new CodeColorsLayoutInflater(inflater, inflater.getContext());
    }
}