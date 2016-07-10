package io.leao.codecolors.core.color;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.StateSet;
import android.util.TypedValue;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import io.leao.codecolors.core.CcCore;
import io.leao.codecolors.core.color.CodeColor.AnchorCallback;
import io.leao.codecolors.core.color.CodeColor.SingleCallback;
import io.leao.codecolors.core.res.CcResources;

import static io.leao.codecolors.core.color.ColorStateListUtils.getColors;
import static io.leao.codecolors.core.color.ColorStateListUtils.getStateSpecs;
import static io.leao.codecolors.core.color.ColorStateListUtils.getThemeAttrs;

public class CcColorStateListList extends ColorStateList implements CodeColor {
    protected int mId;

    protected ColorStateList mBase;
    protected int[][] mStateSpecs;
    protected int[] mColors;
    protected int[][] mThemeAttrs;

    protected AlphaCodeColor[] mAlphaCodeColors;
    protected AlphaCodeColor mDefaultColor;

    protected ListCallbackHandler mListCallbackHandler = new ListCallbackHandler(this);

    public CcColorStateListList(int id, ColorStateList base) {
        super(EMPTY_STATES, DEFAULT_COLORS);
        mId = id;

        mBase = base;
        onBaseColorChanged();

        mAlphaCodeColors = new AlphaCodeColor[mColors.length];
    }

    protected CcColorStateListList(CcColorStateListList orig) {
        this(orig.mId, ColorStateListUtils.clone(orig.mBase));

        // Clone alpha code-colors.
        int N = mAlphaCodeColors.length;
        for (int i = 0; i < N; i++) {
            if (mAlphaCodeColors[i] != null) {
                CodeColor color = orig.mAlphaCodeColors[i].getColor();
                float alphaMod = orig.mAlphaCodeColors[i].getAlphaMod();
                mAlphaCodeColors[i] = new AlphaCodeColor(color, alphaMod);
            }
        }


        onCodeColorsChanged();
    }

    protected CcColorStateListList(Parcel source) {
        this(NO_ID, ColorStateList.CREATOR.createFromParcel(source));
    }

    protected void onBaseColorChanged() {
        mStateSpecs = getStateSpecs(mBase);
        mColors = getColors(mBase);
        mThemeAttrs = getThemeAttrs(mBase);
    }

    protected void onCodeColorsChanged() {
        AlphaCodeColor defaultColor = null;

        final int[][] states = mStateSpecs;
        final AlphaCodeColor[] colors = mAlphaCodeColors;
        final int N = states.length;
        if (N > 0) {
            defaultColor = colors[0];

            for (int i = N - 1; i > 0; i--) {
                if (states[i].length == 0) {
                    defaultColor = colors[i];
                    break;
                }
            }
        }

        mDefaultColor = defaultColor;
    }

    @Override
    public int getId() {
        return mId;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static ColorStateList createFromXml(@NonNull Resources resources, int id, @Nullable Resources.Theme theme)
            throws IOException, XmlPullParserException {

        XmlResourceParser parser = resources.getXml(id);
        ColorStateList baseColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                ColorStateList.createFromXml(resources, parser, theme) :
                ColorStateList.createFromXml(resources, parser);
        parser.close();

        if (CcCore.getDependencyManager().hasDependencies(resources, id)) {
            CcColorStateListList color = new CcColorStateListList(id, baseColor);

            parser = resources.getXml(id);
            color.inflateCodeColors(resources, parser);
            parser.close();

            return color;
        } else {
            return baseColor;
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public void inflateCodeColors(@NonNull Resources resources, @NonNull XmlPullParser parser)
            throws IOException, XmlPullParserException {

        final AttributeSet attrs = Xml.asAttributeSet(parser);

        int type;
        while ((type = parser.next()) != XmlPullParser.START_TAG
                && type != XmlPullParser.END_DOCUMENT) {
            // Seek parser to start tag.
        }

        if (type != XmlPullParser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }

        final int innerDepth = parser.getDepth() + 1;
        int depth;
        int listSize = 0;

        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && ((depth = parser.getDepth()) >= innerDepth || type != XmlPullParser.END_TAG)) {
            if (type != XmlPullParser.START_TAG || depth > innerDepth
                    || !parser.getName().equals("item")) {
                continue;
            }

            ColorStateList color = null;
            float alphaMod = 1.0f;

            final TypedArray ta =
                    resources.obtainAttributes(attrs, new int[]{android.R.attr.color, android.R.attr.alpha});
            try {
                final int M = ta.length();
                for (int j = 0; j < M; j++) {
                    int index = ta.getIndex(j);

                    if (index == 0) { // Index 0: android:color attribute.
                        int resId = ta.getResourceId(index, 0);
                        if (resId != 0) {
                            color = ta.getColorStateList(index);
                        }
                    } else { // Index 1: android:alpha attribute.
                        TypedValue value = CcResources.getValue();
                        ta.getValue(index, value);
                        if (value.type == TypedValue.TYPE_FLOAT) {
                            alphaMod = ta.getFloat(index, 1.0f);
                        }
                    }
                }
            } finally {
                ta.recycle();
            }

            if (color instanceof CodeColor) {
                mAlphaCodeColors[listSize] = new AlphaCodeColor((CodeColor) color, alphaMod);
            }

            listSize++;
        }

        onCodeColorsChanged();
    }

    @TargetApi(Build.VERSION_CODES.M)
    //@Override
    public boolean canApplyTheme() {
        return mThemeAttrs != null;
    }

    @SuppressWarnings("ResourceType")
    @TargetApi(Build.VERSION_CODES.M)
    private void applyTheme(Resources.Theme t) {
        final int[][] themeAttrsList = mThemeAttrs;
        final int N = themeAttrsList.length;

        for (int i = 0; i < N; i++) {
            if (themeAttrsList[i] != null) {
                ColorStateList color = null;
                float alphaMod = 1.0f;

                final TypedArray ta = t.obtainStyledAttributes(themeAttrsList[i]);
                try {
                    // Index 0: android:color attribute.
                    color = ta.getColorStateList(0);

                    float defaultAlphaMod;
                    if (themeAttrsList[i][0] != 0) {
                        // If the base color hasn't been resolved yet, the current
                        // color's alpha channel is either full-opacity (if we
                        // haven't resolved the alpha modulation yet) or
                        // pre-modulated. Either is okay as a default value.
                        defaultAlphaMod = Color.alpha(mColors[i]) / 255.0f;
                    } else {
                        // Otherwise, the only correct default value is 1. Even if
                        // nothing is resolved during this call, we can apply this
                        // multiple times without losing of information.
                        defaultAlphaMod = 1.0f;
                    }
                    // Index 1: android:alpha attribute.
                    alphaMod = ta.getFloat(1, defaultAlphaMod);
                } finally {
                    ta.recycle();
                }

                if (color instanceof CodeColor) {
                    mAlphaCodeColors[i] = new AlphaCodeColor((CodeColor) color, alphaMod);
                }
            }
        }

        onCodeColorsChanged();

        // Apply theme only *after* resolving mThemeAttrs and their code-colors.
        // Otherwise, mThemeAttrs content will change, as it is pointing to mBase's variables.
        ColorStateListUtils.applyTheme(mBase, t);
        onBaseColorChanged();
    }

    @TargetApi(Build.VERSION_CODES.M)
    //@Override
    public ColorStateList obtainForTheme(Resources.Theme t) {
        if (t == null || !canApplyTheme()) {
            return this;
        }

        final CcColorStateListList clone = new CcColorStateListList(this);
        clone.applyTheme(t);
        return clone;
    }

    @NonNull
    @Override
    public CcColorStateListList withAlpha(int alpha) {
        CcColorStateListList clone = new CcColorStateListList(NO_ID, mBase.withAlpha(alpha));

        // Clone alpha code-colors with adjusted alphaMod.
        float newAlphaMod = alpha / 255.0f;
        int N = mAlphaCodeColors.length;
        for (int i = 0; i < N; i++) {
            if (mAlphaCodeColors[i] != null) {
                CodeColor color = mAlphaCodeColors[i].getColor();
                float alphaMod = mAlphaCodeColors[i].getAlphaMod() * newAlphaMod;
                clone.mAlphaCodeColors[i] = new AlphaCodeColor(color, alphaMod);
            }
        }

        clone.onCodeColorsChanged();

        return clone;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public int getChangingConfigurations() {
        return mBase.getChangingConfigurations();
    }

    @Override
    public boolean isStateful() {
        return mBase.isStateful();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean isOpaque() {
        return mBase.isOpaque();
    }

    @Override
    public int getColorForState(int[] stateSet, int defaultColor) {
        final int setLength = mStateSpecs.length;
        for (int i = 0; i < setLength; i++) {
            final int[] stateSpec = mStateSpecs[i];
            if (StateSet.stateSetMatches(stateSpec, stateSet)) {
                if (mAlphaCodeColors[i] != null) {
                    return mAlphaCodeColors[i].getColorForState(stateSet, defaultColor);
                } else {
                    return mColors[i];
                }
            }
        }
        return defaultColor;
    }

    @Override
    public int getDefaultColor() {
        return mDefaultColor != null ? mDefaultColor.getDefaultColor() : mBase.getDefaultColor();
    }

    @Override
    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }

    @Override
    public void addCallback(@Nullable Activity activity, SingleCallback callback) {
        for (AlphaCodeColor color : mAlphaCodeColors) {
            if (color != null) {
                mListCallbackHandler.addCallback(activity, color.getColor(), callback);
            }
        }
    }

    @Override
    public boolean containsCallback(@Nullable Activity activity, SingleCallback callback) {
        for (AlphaCodeColor color : mAlphaCodeColors) {
            if (color != null && mListCallbackHandler.containsCallback(activity, color.getColor(), callback)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void removeCallback(@Nullable Activity activity, SingleCallback callback) {
        for (AlphaCodeColor color : mAlphaCodeColors) {
            if (color != null) {
                mListCallbackHandler.removeCallback(activity, color.getColor(), callback);
            }
        }
    }

    @Override
    public void addAnchorCallback(@Nullable Activity activity, Object anchor, AnchorCallback callback) {
        for (AlphaCodeColor color : mAlphaCodeColors) {
            if (color != null) {
                mListCallbackHandler.addAnchorCallback(activity, color.getColor(), anchor, callback);
            }
        }
    }

    @Override
    public boolean containsAnchorCallback(@Nullable Activity activity, Object anchor, AnchorCallback callback) {
        for (AlphaCodeColor color : mAlphaCodeColors) {
            if (color != null &&
                    mListCallbackHandler.containsAnchorCallback(activity, color.getColor(), anchor, callback)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void removeAnchorCallback(@Nullable Activity activity, Object anchor, AnchorCallback callback) {
        for (AlphaCodeColor color : mAlphaCodeColors) {
            if (color != null) {
                mListCallbackHandler.removeAnchorCallback(activity, color.getColor(), anchor, callback);
            }
        }
    }

    @Override
    public void removeAnchor(@Nullable Activity activity, Object anchor) {
        for (AlphaCodeColor color : mAlphaCodeColors) {
            if (color != null) {
                mListCallbackHandler.removeAnchor(activity, color.getColor(), anchor);
            }
        }
    }

    @Override
    public void removeCallback(@Nullable Activity activity, AnchorCallback callback) {
        for (AlphaCodeColor color : mAlphaCodeColors) {
            if (color != null) {
                mListCallbackHandler.removeCallback(activity, color.getColor(), callback);
            }
        }
    }

    /**
     * {@link CodeColor} wrapper to allow dynamic alpha.
     */
    protected static class AlphaCodeColor {
        private CodeColor mColor;
        private float mAlphaMod;

        public AlphaCodeColor(CodeColor color, float alphaMod) {
            mColor = color;
            mAlphaMod = alphaMod;
        }

        public CodeColor getColor() {
            return mColor;
        }

        public float getAlphaMod() {
            return mAlphaMod;
        }

        public int getDefaultColor() {
            return modulateColorAlpha(mColor.getDefaultColor(), mAlphaMod);
        }

        public int getColorForState(int[] stateSet, int defaultColor) {
            return modulateColorAlpha(mColor.getColorForState(stateSet, defaultColor), mAlphaMod);
        }

        private static int modulateColorAlpha(int baseColor, float alphaMod) {
            if (alphaMod == 1.0f) {
                return baseColor;
            }

            final int baseAlpha = Color.alpha(baseColor);
            final int alpha = constrain((int) (baseAlpha * alphaMod + 0.5f), 0, 255);
            return (baseColor & 0xFFFFFF) | (alpha << 24);
        }

        public static int constrain(int amount, int low, int high) {
            return amount < low ? low : (amount > high ? high : amount);
        }
    }

    /*
     * Parcelable.
     */

    @Override
    public int describeContents() {
        return mBase.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        mBase.writeToParcel(dest, flags);
    }

    public static final Parcelable.Creator<ColorStateList> CREATOR = new Parcelable.Creator<ColorStateList>() {
        @Override
        public ColorStateList[] newArray(int size) {
            return new CcColorStateListList[size];
        }

        @Override
        public ColorStateList createFromParcel(Parcel source) {
            return new CcColorStateListList(source);
        }
    };
}
