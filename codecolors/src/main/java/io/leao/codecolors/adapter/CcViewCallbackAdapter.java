package io.leao.codecolors.adapter;

import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.Set;

import io.leao.codecolors.res.CcColorStateList;

public interface CcViewCallbackAdapter<T> extends CcCallbackAdapter<T> {
    /**
     * @param outColors the code colors from the given view that should receive the callback.
     */
    void getCodeColors(@Nullable AttributeSet attrs, View view, Set<CcColorStateList> outColors);

    T getAnchor(@Nullable AttributeSet attrs, View view);
}
