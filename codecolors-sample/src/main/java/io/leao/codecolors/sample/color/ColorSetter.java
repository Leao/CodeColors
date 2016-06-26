package io.leao.codecolors.sample.color;

import io.leao.codecolors.CodeColors;
import io.leao.codecolors.core.editor.CcEditorSet;
import io.leao.codecolors.core.editor.CcMultiEditor;
import io.leao.codecolors.core.editor.CcMultiEditorAnimate;
import io.leao.codecolors.core.editor.CcMultiEditorSet;
import io.leao.codecolors.sample.R;

public class ColorSetter {
    public static void setColorsTo(Integer primary, Integer primaryDark, Integer accent, Integer accentPressed) {
        CcMultiEditorSet setEditor = CodeColors.setMultiple();
        setColor(setEditor, R.color.cc__color_primary, primary);
        setColor(setEditor, R.color.cc__color_primary_dark, primaryDark);
        setColor(setEditor, R.color.cc__color_accent, accent);
        if (accentPressed != null) {
            setEditor.setState(R.color.cc__color_accent, new int[]{android.R.attr.state_pressed}, accentPressed);
        } else {
            setEditor.removeState(R.color.cc__color_accent, new int[]{android.R.attr.state_pressed});
        }
        setEditor.submit();
    }

    public static void animateColorsTo(Integer primary, Integer primaryDark, Integer accent, Integer accentPressed) {
        CcMultiEditorAnimate animateEditor = CodeColors.animateMultiple();
        setColor(animateEditor, R.color.cc__color_primary, primary);
        setColor(animateEditor, R.color.cc__color_primary_dark, primaryDark);
        setColor(animateEditor, R.color.cc__color_accent, accent);
        if (accentPressed != null) {
            animateEditor.setState(R.color.cc__color_accent, new int[]{android.R.attr.state_pressed}, accentPressed);
        } else {
            animateEditor.removeState(R.color.cc__color_accent, new int[]{android.R.attr.state_pressed});
        }
        animateEditor.start();
    }

    public static CcEditorSet setColor(CcEditorSet editor, Integer color) {
        if (color != null) {
            return editor.setColor(color);
        } else {
            return editor.setColor(null);
        }
    }

    public static CcMultiEditor setColor(CcMultiEditor editor, int resId, Integer color) {
        if (color != null) {
            return editor.setColor(resId, color);
        } else {
            return editor.setColor(resId, null);
        }
    }
}
