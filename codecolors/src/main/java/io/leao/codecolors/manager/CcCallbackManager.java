package io.leao.codecolors.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.leao.codecolors.R;
import io.leao.codecolors.adapter.CcAttrCallbackAdapter;
import io.leao.codecolors.adapter.CcCallbackAdapter;
import io.leao.codecolors.adapter.CcViewCallbackAdapter;
import io.leao.codecolors.res.CcColorStateList;

public class CcCallbackManager {
    private static CcCallbackManager sInstance;

    private Map<Integer, List<CcAttrCallbackAdapter>> mAttrCallbackAdapters = new HashMap<>();
    private List<CcViewCallbackAdapter> mViewCallbackAdapters = new ArrayList<>();

    private Map<Class<?>, CcColorStateList.AnchorCallback> mAdapterClassAnchorCallback = new HashMap<>();

    // Used to retrieve code colors when processing view callback adapters.
    private Set<CcColorStateList> mTempColors = new HashSet<>();

    public static CcCallbackManager getInstance() {
        if (sInstance == null) {
            sInstance = new CcCallbackManager();
        }
        return sInstance;
    }

    public synchronized void addAttrCallbackAdapter(CcAttrCallbackAdapter adapter) {
        for (int attr : adapter.getAttrs()) {
            List<CcAttrCallbackAdapter> adapters = mAttrCallbackAdapters.get(attr);
            if (adapters == null) {
                adapters = new ArrayList<>();
                mAttrCallbackAdapters.put(attr, adapters);
            }
            adapters.add(0, adapter);
        }
        if (mAdapterClassAnchorCallback.get(adapter.getClass()) == null) {
            mAdapterClassAnchorCallback.put(adapter.getClass(), adapter.getAnchorCallback());
        }
    }

    public synchronized void addViewCallbackAdapter(CcViewCallbackAdapter adapter) {
        mViewCallbackAdapters.add(adapter);
        mAdapterClassAnchorCallback.put(adapter.getClass(), adapter.getAnchorCallback());
    }

    public synchronized void addColorCallbacks(Context context, AttributeSet attrs, View view) {
        processViewCallbackAdapters(attrs, view);
        processAttrCallbackAdapters(context, attrs, view);
    }

    private synchronized void processViewCallbackAdapters(AttributeSet attrs, View view) {
        for (CcViewCallbackAdapter adapter : mViewCallbackAdapters) {
            mTempColors.clear();
            adapter.getCodeColors(attrs, view, mTempColors);
            for (CcColorStateList color : mTempColors) {
                Object anchor = adapter.getAnchor(attrs, view);
                if (anchor != null) {
                    color.addCallback(adapter.getAnchor(attrs, view), getAnchorCallback(adapter));
                }
            }
        }
    }

    private synchronized void processAttrCallbackAdapters(Context context, AttributeSet attrs, View view) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CodeColors, getViewDefStyleAttr(view), 0);
        try {
            final int N = ta.getIndexCount();
            for (int i = 0; i < N; i++) {
                int index = ta.getIndex(i);
                int attr = R.styleable.CodeColors[index];
                List<CcAttrCallbackAdapter> adapters = mAttrCallbackAdapters.get(attr);
                if (adapters != null) {
                    int resourceId = ta.getResourceId(index, 0);
                    if (CcDependenciesManager.getInstance().hasDependencies(context.getResources(), resourceId)) {
                        Set<Integer> dependencies = null;

                        for (CcAttrCallbackAdapter adapter : adapters) {
                            Object anchor = adapter.getAnchor(view, attr);
                            if (anchor != null) {
                                if (dependencies == null) {
                                    dependencies = TempUtils.getIntegerSet();
                                    dependencies.add(resourceId);
                                    CcDependenciesManager.getInstance().resolveDependencies(
                                            context.getTheme(), context.getResources(), resourceId, dependencies);
                                }

                                // Add callbacks to all dependencies of resourceId.
                                // If there are dependencies, resourceId is included in the list.
                                // Otherwise, the list is null.
                                CcColorStateList.AnchorCallback callback = null;
                                for (Integer dependency : dependencies) {
                                    CcColorStateList codeColor = CcColorsManager.getInstance().getColor(dependency);
                                    if (codeColor != null) {
                                        if (callback == null) {
                                            callback = getAnchorCallback(adapter);
                                        }
                                        codeColor.addCallback(anchor, callback);
                                    }
                                }
                            }
                        }

                        // Recycle set for future reuse.
                        TempUtils.recycleIntegerSet(dependencies);
                    }
                }
            }
        } finally {
            ta.recycle();
        }
    }

    private synchronized CcColorStateList.AnchorCallback getAnchorCallback(CcCallbackAdapter adapter) {
        CcColorStateList.AnchorCallback callback = mAdapterClassAnchorCallback.get(adapter.getClass());
        if (callback == null) {
            callback = adapter.getAnchorCallback();
            mAdapterClassAnchorCallback.put(adapter.getClass(), callback);
        }
        return callback;
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
}
