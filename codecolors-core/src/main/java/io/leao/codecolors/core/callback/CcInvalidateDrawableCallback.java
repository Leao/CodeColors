package io.leao.codecolors.core.callback;

import android.graphics.drawable.Drawable;

import java.util.Set;

import io.leao.codecolors.core.color.CcColorStateList;

import static io.leao.codecolors.core.drawable.CcDrawableUtils.forceStateChange;

public class CcInvalidateDrawableCallback implements CcColorStateList.AnchorCallback<Drawable> {
    @Override
    public void invalidateColor(Drawable drawable, CcColorStateList color) {
        invalidate(drawable);
    }

    @Override
    public void invalidateColors(Drawable drawable, Set<CcColorStateList> colors) {
        invalidate(drawable);
    }

    public static void invalidate(Drawable drawable) {
        // Force a state change to update the color.
        forceStateChange(drawable);
        // Invalidate the drawable (invalidates the view).
        drawable.invalidateSelf();
    }
}
