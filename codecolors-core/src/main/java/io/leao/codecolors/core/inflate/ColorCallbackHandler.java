package io.leao.codecolors.core.inflate;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import io.leao.codecolors.core.color.CodeColor;
import io.leao.codecolors.core.color.CodeColor.AnchorCallback;

class ColorCallbackHandler {
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
        AnchorCallback defaultCallback = cacheResult != null ? cacheResult.defaultCallback : null;

        for (int j = 0; j < result.colors.size(); j++) {
            CodeColor color = (CodeColor) result.colors.get(j);

            AnchorCallback callback = (AnchorCallback) result.callbacks.get(j);
            // If no callback was specified, use the default callback (might be null).
            if (callback == null) {
                callback = defaultCallback;
            }
            // Set callback, if valid.
            if (callback != null) {
                color.addAnchorCallback(null, result.anchor, callback);
            }
        }
    }

    protected static class CacheResult<T> implements CcColorCallbackAdapter.CacheResult<T> {
        public AnchorCallback<T> defaultCallback;

        @Override
        public void set(@Nullable AnchorCallback<T> defaultCallback) {
            this.defaultCallback = defaultCallback;
        }

        CacheResult reuse() {
            defaultCallback = null;
            return this;
        }
    }

    protected static class InflateAddResult<T> implements CcColorCallbackAdapter.InflateAddResult<T> {
        public T anchor;
        public List<CodeColor> colors = new ArrayList<>();
        public List<AnchorCallback<T>> callbacks = new ArrayList<>();

        @Override
        public void set(@NonNull T anchor) {
            this.anchor = anchor;
        }

        @Override
        public void add(@NonNull CodeColor color) {
            add(color, null);
        }

        @Override
        public void add(@NonNull CodeColor color, @Nullable AnchorCallback<T> callback) {
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