package io.leao.codecolors.core.inflate;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.leao.codecolors.core.CcCore;
import io.leao.codecolors.core.color.CcColorStateList;
import io.leao.codecolors.core.color.CodeColor;
import io.leao.codecolors.core.color.CodeColor.AnchorCallback;
import io.leao.codecolors.core.util.CcTempUtils;

class AttrCallbackHandler {
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
    public void onCreateView(AttributeSet attrs, View view, int defStyleAttr, int defStyleRes) {
        ensureAttrs();
        Context context = view.getContext();
        TypedArray ta = context.obtainStyledAttributes(attrs, mAttrs, defStyleAttr, defStyleRes);
        try {
            final int N = ta.getIndexCount();
            for (int i = 0; i < N; i++) {
                int index = ta.getIndex(i);
                int attr = mAttrs[index];
                List<CcAttrCallbackAdapter> adapters = mAdapters.get(attr);
                if (adapters != null) {
                    boolean resolved = false;
                    Set<Integer> resolvedIds = null;
                    CodeColor resolvedColor = null;

                    List<CacheResult> cacheResults = mCacheResults.get(attr);
                    for (int j = 0; j < adapters.size(); j++) {
                        CcAttrCallbackAdapter adapter = adapters.get(j);
                        InflateResult inflateResult = mTempInflateResult.reuse();
                        if (adapter.onInflate(view, attr, inflateResult)) {
                            CacheResult cacheResult = cacheResults.get(j);

                            if (!resolved) {
                                resolved = true;

                                ColorStateList color = ta.getColorStateList(index);
                                if (color instanceof CodeColor) {
                                    resolvedColor = (CodeColor) color;
                                } else {
                                    int resourceId = ta.getResourceId(index, 0);
                                    resolvedIds = CcTempUtils.getIntegerSet();
                                    resolvedIds.add(resourceId);
                                    CcCore.getDependencyManager().resolveDependencies(
                                            context.getTheme(), context.getResources(), resourceId, resolvedIds);
                                }
                            }

                            if (resolvedColor != null) {
                                resolvedColor.addAnchorCallback(null, inflateResult.anchor, cacheResult.callback);
                            } else {
                                // Add callbacks to all dependencies of resourceId.
                                // If there are dependencies, resourceId is included in the set.
                                // Otherwise, the set is empty.
                                for (Integer dependency : resolvedIds) {
                                    CcColorStateList codeColor = CcCore.getColorManager().getColor(dependency);
                                    if (codeColor != null) {
                                        codeColor.addAnchorCallback(null, inflateResult.anchor, cacheResult.callback);
                                    }
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
        public AnchorCallback<T> callback;

        @Override
        public void set(@NonNull int[] attrs, @NonNull AnchorCallback<T> callback) {
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