package io.leao.codecolors.core.adapter;

import android.annotation.SuppressLint;
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
import android.view.View;

import io.leao.codecolors.R;

class AppCompatAdapterDefStyleHandler extends AdapterDefStyleHandler {

    @SuppressLint({"PrivateResource", "InlinedApi"})
    @Override
    protected int getDefaultViewDefStyleAttr(View view) {
        if (view instanceof AppCompatRadioButton) {
            return R.attr.radioButtonStyle;
        } else if (view instanceof AppCompatCheckBox) {
            return R.attr.checkboxStyle;
        } else if (view instanceof AppCompatButton) {
            return R.attr.buttonStyle;
        } else if (view instanceof AppCompatMultiAutoCompleteTextView) {
            return R.attr.autoCompleteTextViewStyle;
        } else if (view instanceof AppCompatAutoCompleteTextView) {
            return R.attr.autoCompleteTextViewStyle;
        } else if (view instanceof AppCompatEditText) {
            return R.attr.editTextStyle;
        } else if (view instanceof AppCompatCheckedTextView) {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 ?
                    android.R.attr.checkedTextViewStyle : 0;
        } else if (view instanceof AppCompatTextView) {
            return android.R.attr.textViewStyle;
        } else if (view instanceof AppCompatSpinner) {
            return R.attr.spinnerStyle;
        } else if (view instanceof AppCompatImageButton) {
            return R.attr.imageButtonStyle;
        } else if (view instanceof AppCompatRatingBar) {
            return R.attr.ratingBarStyle;
        } else if (view instanceof AppCompatSeekBar) {
            return R.attr.seekBarStyle;
        } else {
            return super.getDefaultViewDefStyleAttr(view);
        }
    }
}