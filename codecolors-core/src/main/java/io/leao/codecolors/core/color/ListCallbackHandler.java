package io.leao.codecolors.core.color;

import android.app.Activity;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import io.leao.codecolors.core.color.CodeColor.AnchorCallback;
import io.leao.codecolors.core.color.CodeColor.SingleCallback;

public class ListCallbackHandler {
    protected CcColorStateListList mColor;

    protected Map<SingleCallback, SingleCallback> mSingleCallbackReference = new WeakHashMap<>();

    protected SingleCallbackReference mTempSingleCallbackReference = new SingleCallbackReference();
    protected AnchorCallbackReference mTempAnchorCallbackReference = new AnchorCallbackReference();

    public ListCallbackHandler(CcColorStateListList color) {
        mColor = color;
    }

    public void addCallback(Activity activity, CodeColor color, SingleCallback callback) {
        SingleCallbackReference reference = new SingleCallbackReference();
        reference.getFirst().setWeak(callback);
        mSingleCallbackReference.put(callback, reference);

        color.addCallback(activity, reference);
    }

    public boolean containsCallback(Activity activity, CodeColor color, SingleCallback callback) {
        mTempSingleCallbackReference.getFirst().set(callback);
        boolean contains = color.containsCallback(activity, mTempSingleCallbackReference);
        mTempSingleCallbackReference.getFirst().set(null);
        return contains;
    }

    public void removeCallback(Activity activity, CodeColor color, SingleCallback callback) {
        mTempSingleCallbackReference.getFirst().set(callback);
        color.removeCallback(activity, mTempSingleCallbackReference);
        mTempSingleCallbackReference.getFirst().set(null);
    }

    public void addAnchorCallback(Activity activity, CodeColor color, Object anchor, AnchorCallback callback) {
        AnchorCallbackReference reference = new AnchorCallbackReference();
        reference.getFirst().set(callback);
        color.addAnchorCallback(activity, anchor, reference);
    }

    public boolean containsAnchorCallback(Activity activity, CodeColor color, Object anchor, AnchorCallback callback) {
        mTempAnchorCallbackReference.getFirst().set(callback);
        boolean contains = color.containsAnchorCallback(activity, anchor, mTempAnchorCallbackReference);
        mTempAnchorCallbackReference.getFirst().set(null);
        return contains;
    }

    public void removeAnchorCallback(Activity activity, CodeColor color, Object anchor, AnchorCallback callback) {
        mTempAnchorCallbackReference.getFirst().set(callback);
        color.removeAnchorCallback(activity, anchor, mTempAnchorCallbackReference);
        mTempAnchorCallbackReference.getFirst().set(null);
    }

    public void removeAnchor(Activity activity, CodeColor color, Object anchor) {
        color.removeAnchor(activity, anchor);
    }

    public void removeCallback(Activity activity, CodeColor color, AnchorCallback callback) {
        mTempAnchorCallbackReference.getFirst().set(callback);
        color.removeCallback(activity, mTempAnchorCallbackReference);
        mTempAnchorCallbackReference.getFirst().set(null);
    }

    protected class SingleCallbackReference extends ReferencePair<SingleCallback, CcColorStateListList>
            implements SingleCallback {

        public SingleCallbackReference() {
            getSecond().setWeak(mColor);
        }

        @Override
        public void invalidateColor(CodeColor color) {
            getFirst().get().invalidateColor(getSecond().get());
        }

        @Override
        public <T extends CodeColor> void invalidateColors(Set<T> colors) {
            getFirst().get().invalidateColor(getSecond().get());
        }
    }

    protected class AnchorCallbackReference extends ReferencePair<AnchorCallback, CcColorStateListList>
            implements AnchorCallback {

        public AnchorCallbackReference() {
            getSecond().setWeak(mColor);
        }

        @Override
        public void invalidateColor(Object anchor, CodeColor color) {
            getFirst().get().invalidateColor(anchor, getSecond().get());
        }

        @Override
        public void invalidateColors(Object anchor, Set colors) {
            getFirst().get().invalidateColor(anchor, getSecond().get());
        }
    }
}
