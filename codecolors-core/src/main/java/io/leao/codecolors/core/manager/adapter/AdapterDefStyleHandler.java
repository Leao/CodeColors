package io.leao.codecolors.core.manager.adapter;

import android.annotation.SuppressLint;
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
import java.util.List;

import io.leao.codecolors.core.adapter.CcDefStyleAdapter;

class AdapterDefStyleHandler {
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