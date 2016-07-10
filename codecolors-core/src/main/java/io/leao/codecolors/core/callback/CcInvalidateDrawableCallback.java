package io.leao.codecolors.core.callback;

import android.graphics.drawable.Drawable;

import java.util.Set;

import io.leao.codecolors.core.color.CodeColor;

import static io.leao.codecolors.core.drawable.CcDrawableUtils.forceStateChange;

public class CcInvalidateDrawableCallback implements CodeColor.AnchorCallback<Drawable> {
    @Override
    public void invalidateColor(Drawable drawable, CodeColor color) {
        invalidate(drawable);
    }

    @Override
    public <U extends CodeColor> void invalidateColors(Drawable drawable, Set<U> colors) {
        invalidate(drawable);
    }

    public static void invalidate(Drawable drawable) {
        // Force a state change to update the color.
        forceStateChange(drawable);
        // Invalidate the drawable (invalidates the view).
        drawable.invalidateSelf();
    }
}
