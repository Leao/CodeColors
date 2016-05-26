package io.leao.codecolors.core.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import io.leao.codecolors.core.CcCore;
import io.leao.codecolors.core.adapter.CcAttrCallbackAdapter;
import io.leao.codecolors.core.adapter.CcColorCallbackAdapter;
import io.leao.codecolors.core.adapter.CcDefStyleAdapter;
import io.leao.codecolors.core.color.CcColorStateList;
import io.leao.codecolors.core.color.MultiAnimateEditor;
import io.leao.codecolors.core.color.MultiSetEditor;
import io.leao.codecolors.core.util.CcTempUtils;

public class CcCallbackManager {
    private MultiSetEditor mMultiSetEditor;
    private MultiAnimateEditor mMultiAnimateEditor;

    private ColorCallbackAdaptersHandler mColorCallbackAdaptersHandler;
    private AttrCallbackAdaptersHandler mAttrCallbackAdaptersHandler;
    private DefStyleAdaptersHandler mDefStyleAdaptersHandler;

    public CcCallbackManager() {
        mColorCallbackAdaptersHandler = onCreateColorCallbackAdaptersHandler();
        mAttrCallbackAdaptersHandler = onCreateAttrCallbackAdaptersHandler();
        mDefStyleAdaptersHandler = onCreateDefStyleAdaptersHandler();
    }

    public synchronized CcColorStateList.SetEditor set(int resId) {
        CcColorStateList color = CcCore.getColorsManager().getColor(resId);
        if (color != null) {
            return color.set();
        } else {
            return null;
        }
    }

    public synchronized MultiSetEditor setMultiple() {
        if (mMultiSetEditor == null) {
            mMultiSetEditor = new MultiSetEditor();
        } else {
            mMultiSetEditor.reuse();
        }
        return mMultiSetEditor;
    }

    public synchronized CcColorStateList.AnimateEditor animate(int resId) {
        CcColorStateList color = CcCore.getColorsManager().getColor(resId);
        if (color != null) {
            return color.animate();
        } else {
            return null;
        }
    }

    public synchronized MultiAnimateEditor animateMultiple() {
        if (mMultiAnimateEditor == null) {
            mMultiAnimateEditor = new MultiAnimateEditor();
        } else {
            mMultiAnimateEditor.reuse();
        }
        return mMultiAnimateEditor;
    }

    protected ColorCallbackAdaptersHandler onCreateColorCallbackAdaptersHandler() {
        return new ColorCallbackAdaptersHandler();
    }

    protected AttrCallbackAdaptersHandler onCreateAttrCallbackAdaptersHandler() {
        return new AttrCallbackAdaptersHandler();
    }

    protected DefStyleAdaptersHandler onCreateDefStyleAdaptersHandler() {
        return new DefStyleAdaptersHandler();
    }

    public synchronized void addColorCallbackAdapter(@NonNull CcColorCallbackAdapter adapter) {
        mColorCallbackAdaptersHandler.addAdapter(adapter);
    }

    public synchronized void addAttrCallbackAdapter(@NonNull CcAttrCallbackAdapter adapter) {
        mAttrCallbackAdaptersHandler.addAdapter(adapter);
    }

    public synchronized void addDefStyleAdapter(@NonNull CcDefStyleAdapter adapter) {
        mDefStyleAdaptersHandler.addAdapter(adapter);
    }

    public synchronized void onCreateView(@NonNull Context context, @NonNull AttributeSet attrs, @NonNull View view) {
        DefStyleAdaptersHandler.InflateResult defStyle = mDefStyleAdaptersHandler.onCreateView(attrs, view);
        mColorCallbackAdaptersHandler.onCreateView(attrs, view, defStyle.attr, defStyle.res);
        mAttrCallbackAdaptersHandler.onCreateView(context, attrs, view, defStyle.attr, defStyle.res);
    }

    public synchronized void addView(@NonNull View view) {
        mColorCallbackAdaptersHandler.onAddView(view);
    }

    protected static class ColorCallbackAdaptersHandler {
        protected List<CcColorCallbackAdapter> mAdapters = new ArrayList<>();
        protected List<CacheResult> mCacheResults = new ArrayList<>();

        protected CacheResult mTempCacheResult = new CacheResult<>();
        protected InflateAddResult mTempInflateAddResult = new InflateAddResult();

        @SuppressWarnings("unchecked")
        public void addAdapter(CcColorCallbackAdapter adapter) {
            mAdapters.add(adapter);
            CacheResult cacheResult = mTempCacheResult.reuse();
            if (adapter.onCache(cacheResult)) {
                mCacheResults.add(cacheResult);
                mTempCacheResult = new CacheResult<>();
            } else {
                mCacheResults.add(null);
            }
        }

        @SuppressWarnings("unchecked")
        public void onCreateView(AttributeSet attrs, View view, int defStyleAttr, int defStyleRes) {
            for (int i = 0; i < mAdapters.size(); i++) {
                CcColorCallbackAdapter adapter = mAdapters.get(i);
                InflateAddResult inflateResult = mTempInflateAddResult.reuse();

                if (adapter.onInflate(attrs, view, defStyleAttr, defStyleRes, inflateResult)) {
                    processResults(mCacheResults.get(i), inflateResult);
                }
            }
        }

        @SuppressWarnings("unchecked")
        public void onAddView(View view) {
            for (int i = 0; i < mAdapters.size(); i++) {
                CcColorCallbackAdapter adapter = mAdapters.get(i);
                InflateAddResult addResult = mTempInflateAddResult.reuse();

                if (adapter.onAdd(view, addResult)) {
                    processResults(mCacheResults.get(i), addResult);
                }
            }
        }

        protected void processResults(CacheResult cacheResult, InflateAddResult result) {
            // Get default callback for view.
            CcColorStateList.AnchorCallback defaultCallback =
                    cacheResult != null ? cacheResult.defaultCallback : null;

            for (int j = 0; j < result.colors.size(); j++) {
                CcColorStateList color = (CcColorStateList) result.colors.get(j);

                CcColorStateList.AnchorCallback callback =
                        (CcColorStateList.AnchorCallback) result.callbacks.get(j);
                // If no callback was specified, use the default callback (might be null).
                if (callback == null) {
                    callback = defaultCallback;
                }
                // Set callback, if valid.
                if (callback != null) {
                    color.addAnchorCallback(callback, result.anchor);
                }
            }
        }

        protected static class CacheResult<T> implements CcColorCallbackAdapter.CacheResult<T> {
            public CcColorStateList.AnchorCallback<T> defaultCallback;

            @Override
            public void set(@Nullable CcColorStateList.AnchorCallback<T> defaultCallback) {
                this.defaultCallback = defaultCallback;
            }

            CacheResult reuse() {
                defaultCallback = null;
                return this;
            }
        }

        protected static class InflateAddResult<T> implements CcColorCallbackAdapter.InflateAddResult<T> {
            public T anchor;
            public List<CcColorStateList> colors = new ArrayList<>();
            public List<CcColorStateList.AnchorCallback<T>> callbacks = new ArrayList<>();

            @Override
            public void set(@NonNull T anchor) {
                this.anchor = anchor;
            }

            @Override
            public void add(@NonNull CcColorStateList color) {
                add(color, null);
            }

            @Override
            public void add(@NonNull CcColorStateList color, @Nullable CcColorStateList.AnchorCallback<T> callback) {
                colors.add(color);
                callbacks.add(callback);
            }

            InflateAddResult reuse() {
                anchor = null;
                colors.clear();
                callbacks.clear();
                return this;
            }
        }
    }

    protected static class AttrCallbackAdaptersHandler {
        protected Map<Integer, List<CcAttrCallbackAdapter>> mAdapters = new HashMap<>();
        protected Map<Integer, List<CacheResult>> mCacheResults = new HashMap<>();
        protected int[] mAttrs;

        protected CacheResult mTempCacheResult = new CacheResult();
        protected InflateResult mTempInflateResult = new InflateResult();

        @SuppressWarnings("unchecked")
        public void addAdapter(CcAttrCallbackAdapter adapter) {
            CacheResult cacheResult = mTempCacheResult.reuse();
            if (adapter.onCache(cacheResult)) {
                boolean invalidateAttrs = false;
                for (int attr : cacheResult.attrs) {
                    List<CcAttrCallbackAdapter> adapters = mAdapters.get(attr);
                    List<CacheResult> cacheResults = mCacheResults.get(attr);
                    if (adapters == null) {
                        adapters = new ArrayList<>();
                        mAdapters.put(attr, adapters);
                        cacheResults = new ArrayList<>();
                        mCacheResults.put(attr, cacheResults);
                        // A new attribute was added. Invalidate the current array.
                        invalidateAttrs = true;
                    }
                    adapters.add(0, adapter);
                    cacheResults.add(0, cacheResult);
                }
                mTempCacheResult = new CacheResult();

                if (invalidateAttrs) {
                    // Invalidate the attrs array as new attributes were added.
                    invalidateAttrs();
                }
            }
        }

        public void invalidateAttrs() {
            mAttrs = null;
        }

        protected void ensureAttrs() {
            if (mAttrs == null) {
                Set<Integer> attrsSet = mAdapters.keySet();
                mAttrs = new int[attrsSet.size()];
                int i = 0;
                for (int attr : attrsSet) {
                    mAttrs[i++] = attr;
                }
            }
        }

        @SuppressWarnings("unchecked")
        public void onCreateView(Context context, AttributeSet attrs, View view, int defStyleAttr, int defStyleRes) {
            ensureAttrs();
            TypedArray ta = context.obtainStyledAttributes(attrs, mAttrs, defStyleAttr, defStyleRes);
            try {
                final int N = ta.getIndexCount();
                for (int i = 0; i < N; i++) {
                    int index = ta.getIndex(i);
                    int attr = mAttrs[index];
                    List<CcAttrCallbackAdapter> adapters = mAdapters.get(attr);
                    if (adapters != null) {
                        int resourceId = ta.getResourceId(index, 0);

                        List<CacheResult> cacheResults = mCacheResults.get(attr);
                        Set<Integer> resolvedIds = null;
                        for (int j = 0; j < adapters.size(); j++) {
                            CcAttrCallbackAdapter adapter = adapters.get(j);
                            InflateResult inflateResult = mTempInflateResult.reuse();
                            if (adapter.onInflate(view, attr, inflateResult)) {
                                CacheResult cacheResult = cacheResults.get(j);

                                if (resolvedIds == null) {
                                    resolvedIds = CcTempUtils.getIntegerSet();
                                    resolvedIds.add(resourceId);
                                    CcCore.getDependenciesManager().resolveDependencies(
                                            context.getTheme(), context.getResources(), resourceId, resolvedIds);
                                }

                                // Add callbacks to all dependencies of resourceId.
                                // If there are dependencies, resourceId is included in the set.
                                // Otherwise, the set is empty.
                                for (Integer dependency : resolvedIds) {
                                    CcColorStateList codeColor = CcCore.getColorsManager().getColor(dependency);
                                    if (codeColor != null) {
                                        codeColor.addAnchorCallback(cacheResult.callback, inflateResult.anchor);
                                    }
                                }
                            }
                        }

                        if (resolvedIds != null) {
                            // Recycle set for future reuse.
                            CcTempUtils.recycleIntegerSet(resolvedIds);
                        }
                    }
                }
            } finally {
                ta.recycle();
            }
        }

        protected static class CacheResult<T> implements CcAttrCallbackAdapter.CacheResult<T> {
            public int[] attrs;
            public CcColorStateList.AnchorCallback<T> callback;

            @Override
            public void set(@NonNull int[] attrs, @NonNull CcColorStateList.AnchorCallback<T> callback) {
                this.attrs = attrs;
                this.callback = callback;
            }

            CacheResult reuse() {
                attrs = null;
                callback = null;
                return this;
            }
        }

        protected static class InflateResult<T> implements CcAttrCallbackAdapter.InflateResult<T> {
            public T anchor;

            @Override
            public void set(@NonNull T anchor) {
                this.anchor = anchor;
            }

            InflateResult reuse() {
                anchor = null;
                return this;
            }
        }
    }

    protected static class DefStyleAdaptersHandler {
        protected List<CcDefStyleAdapter> mAdapters = new ArrayList<>();

        protected InflateResult mTempInflateResult = new InflateResult();

        @SuppressWarnings("unchecked")
        public void addAdapter(CcDefStyleAdapter adapter) {
            mAdapters.add(adapter);
        }

        public InflateResult onCreateView(AttributeSet attrs, View view) {
            InflateResult defStyleInflateResult = mTempInflateResult.reuse();

            // Try to get attr and res from adapters.
            for (CcDefStyleAdapter adapter : mAdapters) {
                if (adapter.onInflate(attrs, view, defStyleInflateResult)) {
                    return defStyleInflateResult;
                }
            }

            // Get the default attr and res values.
            defStyleInflateResult.attr = getDefaultViewDefStyleAttr(view);
            defStyleInflateResult.res = 0;

            return defStyleInflateResult;
        }

        /**
         * Returns the default style attribute depending on the view class.
         * <p>
         * Order matters: a {@link CheckBox} is also {@link Button}, so we have to be careful when returning the default
         * style attribute.
         */
        @SuppressLint({"InlinedApi", "PrivateResource"})
        protected int getDefaultViewDefStyleAttr(View view) {
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

        protected static class InflateResult implements CcDefStyleAdapter.InflateResult {
            public int attr;
            public int res;

            @Override
            public void set(int attr, int res) {
                this.attr = attr;
                this.res = res;
            }

            InflateResult reuse() {
                attr = 0;
                res = 0;
                return this;
            }
        }
    }
}
