package io.leao.codecolors.core.callback;

import android.graphics.drawable.Drawable;

import io.leao.codecolors.core.color.CcColorStateList;

import static io.leao.codecolors.core.drawable.CcDrawableUtils.forceStateChange;

public class CcInvalidateDrawableCallback implements CcColorStateList.AnchorCallback<Drawable> {
    @Override
    public void invalidateColor(Drawable drawable, CcColorStateList color) {
        invalidate(drawable);
    }

    public static void invalidate(Drawable drawable) {
        if (drawable != null) {
            // Force a state change to update the color.
            forceStateChange(drawable);
            // Invalidate the drawable (invalidates the view).
            drawable.invalidateSelf();
        }
    }
}
