package io.leao.codecolors.core.res;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Locale;

import io.leao.codecolors.plugin.res.CcConfiguration;

public final class CcConfigurationParcelable extends CcConfiguration implements Parcelable {

    /**
     * Construct an invalid Configuration.  You must call {@link #setToDefaults}
     * for this object to be valid.  {@more}
     */
    public CcConfigurationParcelable() {
        setToDefaults();
    }

    /**
     * Makes a deep copy suitable for modification.
     */
    public CcConfigurationParcelable(CcConfiguration o) {
        setTo(o);
    }

    /**
     * Parcelable methods
     */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(fontScale);
        dest.writeInt(mcc);
        dest.writeInt(mnc);
        if (locale == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(1);
            dest.writeString(locale.getLanguage());
            dest.writeString(locale.getCountry());
            dest.writeString(locale.getVariant());
        }
        if (userSetLocale) {
            dest.writeInt(1);
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(touchscreen);
        dest.writeInt(keyboard);
        dest.writeInt(keyboardHidden);
        dest.writeInt(hardKeyboardHidden);
        dest.writeInt(navigation);
        dest.writeInt(navigationHidden);
        dest.writeInt(orientation);
        dest.writeInt(screenLayout);
        dest.writeInt(uiMode);
        dest.writeInt(screenWidthDp);
        dest.writeInt(screenHeightDp);
        dest.writeInt(smallestScreenWidthDp);
        dest.writeInt(densityDpi);
        dest.writeInt(sdkVersion);
    }

    public void readFromParcel(Parcel source) {
        fontScale = source.readFloat();
        mcc = source.readInt();
        mnc = source.readInt();
        if (source.readInt() != 0) {
            locale = new Locale(source.readString(), source.readString(),
                    source.readString());
        }
        userSetLocale = (source.readInt() == 1);
        touchscreen = source.readInt();
        keyboard = source.readInt();
        keyboardHidden = source.readInt();
        hardKeyboardHidden = source.readInt();
        navigation = source.readInt();
        navigationHidden = source.readInt();
        orientation = source.readInt();
        screenLayout = source.readInt();
        uiMode = source.readInt();
        screenWidthDp = source.readInt();
        screenHeightDp = source.readInt();
        smallestScreenWidthDp = source.readInt();
        densityDpi = source.readInt();
        sdkVersion = source.readInt();
    }

    public static final Creator<CcConfigurationParcelable> CREATOR = new Creator<CcConfigurationParcelable>() {
        public CcConfigurationParcelable createFromParcel(Parcel source) {
            return new CcConfigurationParcelable(source);
        }

        public CcConfigurationParcelable[] newArray(int size) {
            return new CcConfigurationParcelable[size];
        }
    };

    /**
     * Construct this Configuration object, reading from the Parcel.
     */
    private CcConfigurationParcelable(Parcel source) {
        readFromParcel(source);
    }
}
