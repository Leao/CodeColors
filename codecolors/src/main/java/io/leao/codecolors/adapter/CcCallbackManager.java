package io.leao.codecolors.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatCheckedTextView;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatMultiAutoCompleteTextView;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.AppCompatTextView;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.leao.codecolors.R;
import io.leao.codecolors.manager.CcColorsManager;
import io.leao.codecolors.manager.CcDependenciesManager;
import io.leao.codecolors.res.CcColorStateList;
import io.leao.codecolors.util.TempUtils;

public class CcCallbackManager {
    private static CcCallbackManager sInstance;

    ColorCallbackAdaptersHandler mColorCallbackAdaptersHandler = new ColorCallbackAdaptersHandler();
    AttrCallbackAdaptersHandler mAttrCallbackAdaptersHandler = new AttrCallbackAdaptersHandler();
    DefStyleAdaptersHandler mDefStyleAdaptersHandler = new DefStyleAdaptersHandler();

    public static CcCallbackManager getInstance() {
        if (sInstance == null) {
            sInstance = new CcCallbackManager();
        }
        return sInstance;
    }

    public synchronized void addColorCallbackAdapter(CcColorCallbackAdapter adapter) {
        mColorCallbackAdaptersHandler.addAdapter(adapter);
    }

    public synchronized void addAttrCallbackAdapter(CcAttrCallbackAdapter adapter) {
        mAttrCallbackAdaptersHandler.addAdapter(adapter);
    }

    public synchronized void addDefStyleAdapter(CcDefStyleAdapter adapter) {
        mDefStyleAdaptersHandler.addAdapter(adapter);
    }

    public synchronized void onCreateView(Context context, AttributeSet attrs, View view) {
        CcDefStyleAdapter.InflateResult defStyle = mDefStyleAdaptersHandler.onCreateView(attrs, view);
        mColorCallbackAdaptersHandler.onCreateView(attrs, view, defStyle.attr, defStyle.res);
        mAttrCallbackAdaptersHandler.onCreateView(context, attrs, view, defStyle.attr, defStyle.res);
    }

    private static class ColorCallbackAdaptersHandler {
        private List<CcColorCallbackAdapter> mAdapters = new ArrayList<>();
        private List<CcColorCallbackAdapter.CacheResult> mCacheResults = new ArrayList<>();

        private CcColorCallbackAdapter.CacheResult mTempCacheResult = new CcColorCallbackAdapter.CacheResult<>();
        private CcColorCallbackAdapter.InflateResult mTempInflateResult = new CcColorCallbackAdapter.InflateResult();

        @SuppressWarnings("unchecked")
        public void addAdapter(CcColorCallbackAdapter adapter) {
            mAdapters.add(adapter);
            CcColorCallbackAdapter.CacheResult cacheResult = mTempCacheResult.reuse();
            if (adapter.onCache(cacheResult)) {
                mCacheResults.add(cacheResult);
                mTempCacheResult = new CcColorCallbackAdapter.CacheResult<>();
            } else {
                mCacheResults.add(null);
            }
        }

        @SuppressWarnings("unchecked")
        public void onCreateView(AttributeSet attrs, View view, int defStyleAttr, int defStyleRes) {
            for (int i = 0; i < mAdapters.size(); i++) {
                CcColorCallbackAdapter adapter = mAdapters.get(i);
                CcColorCallbackAdapter.InflateResult inflateResult = mTempInflateResult.reuse();

                if (adapter.onInflate(attrs, view, defStyleAttr, defStyleRes, inflateResult)) {
                    CcColorCallbackAdapter.CacheResult cacheResult = mCacheResults.get(i);
                    for (int j = 0; j < inflateResult.colors.size(); j++) {
                        CcColorStateList color = (CcColorStateList) inflateResult.colors.get(j);

                        CcColorStateList.AnchorCallback callback =
                                (CcColorStateList.AnchorCallback) inflateResult.callbacks.get(j);
                        if (callback == null) {
                            callback = cacheResult.defaultCallback;
                        }

                        if (callback != null) {
                            color.addAnchorCallback(inflateResult.anchor, callback);
                        }
                    }
                }
            }
        }
    }

    private static class AttrCallbackAdaptersHandler {
        private Map<Integer, List<CcAttrCallbackAdapter>> mAttrAdapters = new HashMap<>();
        private Map<Integer, List<CcAttrCallbackAdapter.CacheResult>> mAttrCacheResults = new HashMap<>();

        private CcAttrCallbackAdapter.CacheResult mTempCacheResult = new CcAttrCallbackAdapter.CacheResult<>();
        private CcAttrCallbackAdapter.InflateResult mTempInflateResult = new CcAttrCallbackAdapter.InflateResult();

        @SuppressWarnings("unchecked")
        public void addAdapter(CcAttrCallbackAdapter adapter) {
            CcAttrCallbackAdapter.CacheResult cacheResult = mTempCacheResult.reuse();
            if (adapter.onCache(cacheResult)) {
                for (int attr : cacheResult.attrs) {
                    List<CcAttrCallbackAdapter> adapters = mAttrAdapters.get(attr);
                    List<CcAttrCallbackAdapter.CacheResult> cacheResults = mAttrCacheResults.get(attr);
                    if (adapters == null) {
                        adapters = new ArrayList<>();
                        mAttrAdapters.put(attr, adapters);
                        cacheResults = new ArrayList<>();
                        mAttrCacheResults.put(attr, cacheResults);
                    }
                    adapters.add(0, adapter);
                    cacheResults.add(0, cacheResult);
                }
                mTempCacheResult = new CcAttrCallbackAdapter.CacheResult();
            }
        }

        @SuppressWarnings("unchecked")
        public void onCreateView(Context context, AttributeSet attrs, View view, int defStyleAttr, int defStyleRes) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CodeColors, defStyleAttr, defStyleRes);
            try {
                final int N = ta.getIndexCount();
                for (int i = 0; i < N; i++) {
                    int index = ta.getIndex(i);
                    int attr = R.styleable.CodeColors[index];
                    List<CcAttrCallbackAdapter> adapters = mAttrAdapters.get(attr);
                    List<CcAttrCallbackAdapter.CacheResult> cacheResults = mAttrCacheResults.get(attr);
                    if (adapters != null) {
                        int resourceId = ta.getResourceId(index, 0);
                        if (CcDependenciesManager.getInstance().hasDependencies(context.getResources(), resourceId)) {
                            Set<Integer> dependencies = null;
                            for (int j = 0; j < adapters.size(); j++) {
                                CcAttrCallbackAdapter adapter = adapters.get(j);
                                CcAttrCallbackAdapter.InflateResult inflateResult = mTempInflateResult.reuse();
                                if (adapter.onInflate(view, attr, inflateResult)) {
                                    CcAttrCallbackAdapter.CacheResult cacheResult = cacheResults.get(j);

                                    if (dependencies == null) {
                                        dependencies = TempUtils.getIntegerSet();
                                        dependencies.add(resourceId);
                                        CcDependenciesManager.getInstance().resolveDependencies(
                                                context.getTheme(), context.getResources(), resourceId, dependencies);
                                    }

                                    // Add callbacks to all dependencies of resourceId.
                                    // If there are dependencies, resourceId is included in the set.
                                    // Otherwise, the set is empty.
                                    for (Integer dependency : dependencies) {
                                        CcColorStateList codeColor = CcColorsManager.getInstance().getColor(dependency);
                                        if (codeColor != null) {
                                            codeColor.addAnchorCallback(inflateResult.anchor, cacheResult.callback);
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
    }

    private static class DefStyleAdaptersHandler {
        private List<CcDefStyleAdapter> mAdapters = new ArrayList<>();

        private CcDefStyleAdapter.InflateResult mTempInflateResult = new CcDefStyleAdapter.InflateResult();

        @SuppressWarnings("unchecked")
        public void addAdapter(CcDefStyleAdapter adapter) {
            mAdapters.add(adapter);
        }

        public CcDefStyleAdapter.InflateResult onCreateView(AttributeSet attrs, View view) {
            CcDefStyleAdapter.InflateResult defStyleInflateResult = mTempInflateResult.reuse();

            // Try to get attr and res from adapters.
            for (CcDefStyleAdapter adapter : mAdapters) {
                if (adapter.onInflate(attrs, view, defStyleInflateResult)) {
                    return defStyleInflateResult;
                }
            }

            // Get the default attr and res values.
            defStyleInflateResult.attr = getViewDefStyleAttr(view);
            defStyleInflateResult.res = 0;

            return defStyleInflateResult;
        }

        /**
         * Returns the default style attribute depending on the view class.
         * <p/>
         * Order matters: a {@link CheckBox} is also {@link Button}, so we have to be careful when returning the default
         * style attribute.
         */
        @SuppressLint({"InlinedApi", "PrivateResource"})
        private static int getViewDefStyleAttr(View view) {
            if (view instanceof AppCompatRadioButton) {
                return R.attr.radioButtonStyle;
            } else if (view instanceof RadioButton) {
                return android.R.attr.radioButtonStyle;
            } else if (view instanceof AppCompatCheckBox) {
                return R.attr.checkboxStyle;
            } else if (view instanceof CheckBox) {
                return android.R.attr.checkboxStyle;
            } else if (view instanceof AppCompatButton) {
                return R.attr.buttonStyle;
            } else if (view instanceof Button) {
                return android.R.attr.buttonStyle;
            } else if (view instanceof AppCompatMultiAutoCompleteTextView) {
                return R.attr.autoCompleteTextViewStyle;
            } else if (view instanceof MultiAutoCompleteTextView) {
                return android.R.attr.autoCompleteTextViewStyle;
            } else if (view instanceof AppCompatAutoCompleteTextView) {
                return R.attr.autoCompleteTextViewStyle;
            } else if (view instanceof AutoCompleteTextView) {
                return android.R.attr.autoCompleteTextViewStyle;
            } else if (view instanceof AppCompatEditText) {
                return R.attr.editTextStyle;
            } else if (view instanceof EditText) {
                return android.R.attr.editTextStyle;
            } else if (view instanceof AppCompatCheckedTextView || view instanceof CheckedTextView) {
                return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 ?
                        android.R.attr.checkedTextViewStyle : 0;
            } else if (view instanceof AppCompatTextView || view instanceof TextView) {
                return android.R.attr.textViewStyle;
            } else if (view instanceof AppCompatSpinner) {
                return R.attr.spinnerStyle;
            } else if (view instanceof Spinner) {
                return android.R.attr.spinnerStyle;
            } else if (view instanceof AppCompatImageButton) {
                return R.attr.imageButtonStyle;
            } else if (view instanceof ImageButton) {
                return android.R.attr.imageButtonStyle;
            } else if (view instanceof AppCompatRatingBar) {
                return R.attr.ratingBarStyle;
            } else if (view instanceof RatingBar) {
                return android.R.attr.ratingBarStyle;
            } else if (view instanceof AppCompatSeekBar) {
                return R.attr.seekBarStyle;
            } else if (view instanceof SeekBar) {
                return android.R.attr.seekBarStyle;
            } else {
                return 0;
            }
        }
    }
}
