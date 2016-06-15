package io.leao.codecolors.plugin.aapt;

import org.apache.commons.lang.ArrayUtils;

import java.util.Locale;

import io.leao.codecolors.plugin.res.CcConfiguration;

/**
 * Based on AaptLocaleValue struct in AaptAssets.h, with methods defined in AaptAssets.cpp.
 */
public class AaptLocaleValue {
    String mLanguage;
    String mRegion;
    String mScript;
    String mVariant;

    public void setLanguage(String language) {
        mLanguage = language.toLowerCase();
    }

    public void setRegion(String region) {
        mRegion = region.toUpperCase();
    }

    public void setScript(String script) {
        mScript = AaptUtil.capitalize(script.toLowerCase());
    }

    public void setVariant(String variant) {
        mVariant = variant;
    }

    public void writeTo(CcConfiguration out) {
        Locale.Builder builder = new Locale.Builder();

        if (mLanguage != null) {
            builder.setLanguage(mLanguage);
        }
        if (mRegion != null) {
            builder.setRegion(mRegion);
        }
        if (mScript != null) {
            builder.setScript(mScript);
        }
        if (mVariant != null) {
            builder.setVariant(mVariant);
        }

        out.locale = builder.build();
    }

    public int initFromDirName(String[] parts, int startIndex) {
        int size = parts.length;
        int currentIndex = startIndex;

        String part = parts[currentIndex];
        if (part.charAt(0) == 'b' && part.charAt(1) == '+') {
            // This is a "modified" BCP-47 language tag. Same semantics as BCP-47 tags,
            // except that the separator is "+" and not "-".
            String[] subtags = AaptUtil.splitAndLowerCase(part, "+");
            subtags = (String[]) ArrayUtils.remove(subtags, 0);
            if (subtags.length == 1) {
                setLanguage(subtags[0]);
            } else if (subtags.length == 2) {
                setLanguage(subtags[0]);

                // The second tag can either be a region, a variant or a script.
                switch (subtags[1].length()) {
                    case 2:
                    case 3:
                        setRegion(subtags[1]);
                        break;
                    case 4:
                        setScript(subtags[1]);
                        break;
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                        setVariant(subtags[1]);
                        break;
                    default:
                        System.err.println("ERROR: Invalid BCP-47 tag in directory name " + part);
                        return -1;
                }
            } else if (subtags.length == 3) {
                // The language is always the first subtag.
                setLanguage(subtags[0]);

                // The second subtag can either be a script or a region code.
                // If its size is 4, it's a script code, else it's a region code.
                //boolean hasRegion = false; // In the original code, but without any purpose (apparently).
                if (subtags[1].length() == 4) {
                    setScript(subtags[1]);
                } else if (subtags[1].length() == 2 || subtags[1].length() == 3) {
                    setRegion(subtags[1]);
                    //hasRegion = true; // In the original code, but without any purpose (apparently).
                } else {
                    System.err.println("ERROR: Invalid BCP-47 tag in directory name " + part);
                    return -1;
                }

                // The third tag can either be a region code (if the second tag was
                // a script), else a variant code.
                if (subtags[2].length() > 4) {
                    setVariant(subtags[2]);
                } else {
                    setRegion(subtags[2]);
                }
            } else if (subtags.length == 4) {
                setLanguage(subtags[0]);
                setScript(subtags[1]);
                setRegion(subtags[2]);
                setVariant(subtags[3]);
            } else {
                System.err.println("ERROR: Invalid BCP-47 tag in directory name: " + part);
                return -1;
            }

            return ++currentIndex;
        } else {
            if ((part.length() == 2 || part.length() == 3)
                    && isAlpha(part) && "car".compareTo(part) > 0) {
                setLanguage(part);
                if (++currentIndex == size) {
                    return size;
                }
            } else {
                return currentIndex;
            }

            part = parts[currentIndex];
            if (part.charAt(0) == 'r' && part.length() == 3) {
                setRegion(part.substring(1));
                if (++currentIndex == size) {
                    return size;
                }
            }
        }

        return currentIndex;
    }

    public static boolean isAlpha(String str) {
        int length = str.length();
        for (int i = 0; i < length; ++i) {
            if (Character.isAlphabetic(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
