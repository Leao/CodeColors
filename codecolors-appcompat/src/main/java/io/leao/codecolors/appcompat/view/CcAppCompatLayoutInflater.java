package io.leao.codecolors.appcompat.view;

import android.content.Context;
import android.view.LayoutInflater;

import io.leao.codecolors.core.view.CcLayoutInflater;
import io.leao.codecolors.core.view.CcLayoutInflaterFactoryWrapper;

public class CcAppCompatLayoutInflater extends CcLayoutInflater {

    protected CcAppCompatLayoutInflater(Context context) {
        super(context);
    }

    protected CcAppCompatLayoutInflater(LayoutInflater original, Context newContext) {
        super(original, newContext);
    }

    @Override
    protected CcLayoutInflaterFactoryWrapper onCreateFactoryWrapper(Factory2 factory) {
        return new CcAppCompatLayoutInflaterFactoryWrapper(this, factory);
    }

    @Override
    public CcLayoutInflater cloneInContext(Context newContext) {
        return new CcAppCompatLayoutInflater(this, newContext);
    }

    public static CcLayoutInflater copy(LayoutInflater inflater) {
        return new CcAppCompatLayoutInflater(inflater, inflater.getContext());
    }
}
