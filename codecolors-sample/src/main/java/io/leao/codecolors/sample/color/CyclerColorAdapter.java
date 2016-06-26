package io.leao.codecolors.sample.color;

import android.content.res.Configuration;

import io.leao.codecolors.core.color.CcColorAdapter;
import io.leao.codecolors.core.color.CcColorStateList;
import io.leao.codecolors.core.editor.CcEditorSet;
import io.leao.codecolors.sample.R;

public class CyclerColorAdapter implements CcColorAdapter {
    @Override
    public void onConfigurationCreated(Configuration config, CcColorStateList color, int colorResId) {
        if (colorResId == R.color.cc__color_primary) {
            ColorSetter.setColor(color.set(), ColorCycler.getPrimary()).submit();
        } else if (colorResId == R.color.cc__color_primary_dark) {
            ColorSetter.setColor(color.set(), ColorCycler.getPrimaryDark()).submit();
        } else if (colorResId == R.color.cc__color_accent) {
            CcEditorSet editor = ColorSetter.setColor(color.set(), ColorCycler.getAccent());
            Integer accentPressed = ColorCycler.getAccentPressed();
            if (accentPressed != null) {
                editor.setState(new int[]{android.R.attr.state_pressed}, accentPressed);
            } else {
                editor.removeState(new int[]{android.R.attr.state_pressed});
            }
            editor.submit();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration config, CcColorStateList color, int colorResId) {
        // Do nothing.
    }
}
