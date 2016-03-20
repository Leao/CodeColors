package io.leao.codecolors.res;

class CcStateSet {
    public static boolean equalsState(int[] stateSpec, int[] stateSet) {
        int[] small, big;
        if (stateSpec.length < stateSet.length) {
            small = stateSpec;
            big = stateSet;
        } else {
            small = stateSet;
            big = stateSpec;
        }

        boolean equals;
        for (int b : big) {
            equals = false;
            for (int s : small) {
                if (b == s) {
                    equals = true;
                    break;
                }
            }
            if (!equals) {
                return false;
            }
        }
        return true;
    }
}
