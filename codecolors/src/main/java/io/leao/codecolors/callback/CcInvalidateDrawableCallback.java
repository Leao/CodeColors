package io.leao.codecolors.callback;

import android.graphics.drawable.Drawable;
import android.util.StateSet;

import java.util.Arrays;

import io.leao.codecolors.res.CcColorStateList;

public class CcInvalidateDrawableCallback implements CcColorStateList.AnchorCallback<Drawable> {
    @Override
    public void invalidateColor(Drawable drawable, CcColorStateList color) {
        invalidate(drawable);
    }

    public static void invalidate(Drawable drawable) {
        if (drawable != null) {
            // Force a state change to update the color.
            drawable.setState(fakeStateChange(drawable.getState()));
            // Invalidate the drawable (invalidates the view).
            drawable.invalidateSelf();
        }
    }

    /**
     * Returns a new state that will seem to have changed in {@link Drawable#setState(int[])}, allowing
     * {@link Drawable#onStateChange(int[])} to be called.
     * <p/>
     * However, the returned state is the same state with 0 (nothing) as the last item, or the reverse of that.
     */
    private static int[] fakeStateChange(int[] state) {
        if (state.length == 0) {
            return StateSet.NOTHING;
        } else if (state[state.length - 1] == 0) {
            return Arrays.copyOf(state, state.length - 1);
        } else {
            return Arrays.copyOf(state, state.length + 1);
        }
    }
}
