package io.leao.codecolors.callback;

import android.graphics.drawable.Drawable;

import io.leao.codecolors.res.CcColorStateList;

public class CcInvalidateDrawableCallback implements CcColorStateList.AnchorCallback<Drawable> {
    @Override
    public void invalidateColor(Drawable drawable, CcColorStateList color) {
        invalidate(drawable);
    }

    public static void invalidate(Drawable drawable) {
        if (drawable != null) {
            final int[] state = drawable.getState();
            // Force a state change to update the color.
            drawable.setState(new int[]{0});
            drawable.setState(state);
            // Invalidate the drawable (invalidates the view).
            drawable.invalidateSelf();
        }
    }
}
