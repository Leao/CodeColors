package io.leao.codecolors.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Set;

import io.leao.codecolors.R;
import io.leao.codecolors.manager.CcColorsManager;
import io.leao.codecolors.manager.CcDependenciesManager;
import io.leao.codecolors.res.CcColorStateList;

public class CcLayoutInflaterFactoryWrapper implements LayoutInflater.Factory2 {
    private final CcLayoutInflater mInflater;
    private LayoutInflater.Factory2 mFactory;

    protected CcColorsManager mColorsManager;
    protected CcDependenciesManager mDependenciesManager;

    public CcLayoutInflaterFactoryWrapper(CcLayoutInflater inflater, LayoutInflater.Factory2 factory) {
        mInflater = inflater;
        mFactory = factory;

        Context context = inflater.getContext();

        mColorsManager = CcColorsManager.obtain(context);
        mColorsManager.onNewContext(context);

        mDependenciesManager = CcDependenciesManager.obtain(context);
    }

    public LayoutInflater.Factory2 getFactory() {
        return mFactory;
    }

    public void setFactory(LayoutInflater.Factory2 factory) {
        if (mFactory == null) {
            mFactory = factory;
        } else {
            throw new IllegalStateException("Factory already defined.");
        }
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return onCreateView(name, context, attrs);
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        View view;
        if (mFactory != null) {
            view = mFactory.onCreateView(name, context, attrs);
        } else {
            view = null;
        }

        if (view == null) {
            view = mInflater.createViewFromTag(context, name, attrs);
        }

        if (view != null && attrs != null) {
            // Add callbacks to refresh drawable states.
            addCodeColorCallbacks(context, attrs, view);
        }

        return view;
    }

    protected void addCodeColorCallbacks(Context context, AttributeSet attrs, View view) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CodeColors, getViewDefStyleAttr(view), 0);
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

    /**
     * Returns the default style attribute depending on the view class.
     * <p/>
     * Order matters: a {@link CheckBox} is also {@link Button}, so we have to be careful when returning the default
     * style attribute.
     */
    @SuppressLint("InlinedApi")
    private static int getViewDefStyleAttr(View view) {
        if (view instanceof RadioButton) {
            return android.R.attr.radioButtonStyle;
        } else if (view instanceof CheckBox) {
            return android.R.attr.checkboxStyle;
        } else if (view instanceof Button) {
            return android.R.attr.buttonStyle;
        } else if (view instanceof MultiAutoCompleteTextView) {
            return android.R.attr.autoCompleteTextViewStyle;
        } else if (view instanceof AutoCompleteTextView) {
            return android.R.attr.autoCompleteTextViewStyle;
        } else if (view instanceof EditText) {
            return android.R.attr.editTextStyle;
        } else if (view instanceof CheckedTextView) {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 ?
                    android.R.attr.checkedTextViewStyle : 0;
        } else if (view instanceof TextView) {
            return android.R.attr.textViewStyle;
        } else if (view instanceof Spinner) {
            return android.R.attr.spinnerStyle;
        } else if (view instanceof ImageButton) {
            return android.R.attr.imageButtonStyle;
        } else if (view instanceof RatingBar) {
            return android.R.attr.ratingBarStyle;
        } else if (view instanceof SeekBar) {
            return android.R.attr.seekBarStyle;
        } else {
            return 0;
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
}