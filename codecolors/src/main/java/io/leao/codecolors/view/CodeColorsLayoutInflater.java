package io.leao.codecolors.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import io.leao.codecolors.CodeColors;
import io.leao.codecolors.R;
import io.leao.codecolors.res.CodeColorStateList;

public class CodeColorsLayoutInflater extends LayoutInflater implements LayoutInflater.Factory2 {

    private static final String[] sClassPrefixes = {"android.widget.", "android.webkit."};

    protected Object[] mConstructorArgs;

    protected Map<Integer, Set<Integer>> mDependencies;
    private SparseIntArray mAttrResourceId = new SparseIntArray();

    private int[] mTempArray = new int[1];

    protected CodeColorsLayoutInflater(Context context) {
        super(context);
        init(context);
    }

    protected CodeColorsLayoutInflater(LayoutInflater original, Context newContext) {
        super(original, newContext);
        init(newContext);
    }

    private void init(Context context) {
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

        mDependencies = CodeColors.getDependencies(context);
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
                            addCodeColorCallbacks(context, resourceId, backgroundDrawable, mDrawableInvalidateCallback);
                        }
                    } else if (attr == R.styleable.CodeColors_android_src) {
                        if (view instanceof ImageView) {
                            Drawable srcDrawable = ((ImageView) view).getDrawable();
                            if (srcDrawable != null) {
                                int resourceId = ta.getResourceId(attr, 0);
                                addCodeColorCallbacks(context, resourceId, srcDrawable, mDrawableInvalidateCallback);
                            }
                        }
                    }
                }
            } finally {
                ta.recycle();
            }
        }
    }

    private void addCodeColorCallbacks(Context context, int resourceId, Drawable drawable,
                                       CodeColorStateList.AnchorCallback callback) {
        CodeColorStateList codeColor;
        // Add callback if the resource itself is a CodeColor.
        codeColor = CodeColors.getColor(resourceId);
        if (codeColor != null) {
            codeColor.addCallback(drawable, callback);
        }
        // Add callback for all resource's CodeColor dependencies.
        // That may require to resolve some attributes.
        Set<Integer> dependencies = mDependencies.get(resourceId);
        if (dependencies != null) {
            for (Integer dependency : dependencies) {
                codeColor = CodeColors.getColor(dependency);
                if (codeColor == null) {
                    if (mAttrResourceId.indexOfKey(dependency) < 0) {
                        mTempArray[0] = dependency;
                        TypedArray ta = context.obtainStyledAttributes(mTempArray);
                        int dependencyId = 0;
                        try {
                            dependencyId = ta.getResourceId(0, dependencyId);
                        } finally {
                            ta.recycle();
                        }
                        if (dependencyId != 0) {
                            codeColor = CodeColors.getColor(dependencyId);
                        }
                        if (codeColor != null) {
                            mAttrResourceId.append(dependency, dependencyId);
                        } else {
                            mAttrResourceId.append(dependency, 0);
                        }
                    } else {
                        codeColor = CodeColors.getColor(mAttrResourceId.get(dependency));
                    }
                }
                if (codeColor != null) {
                    codeColor.addCallback(drawable, callback);
                }
            }
        }
    }

    private static final CodeColorStateList.AnchorCallback<Drawable> mDrawableInvalidateCallback =
            new CodeColorStateList.AnchorCallback<Drawable>() {
                @Override
                public void invalidateColor(Drawable drawable, CodeColorStateList color) {
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
        return new CodeColorsLayoutInflater(this, newContext);
    }

    public static CodeColorsLayoutInflater copy(LayoutInflater inflater) {
        return new CodeColorsLayoutInflater(inflater, inflater.getContext());
    }
}