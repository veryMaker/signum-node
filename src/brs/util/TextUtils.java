package brs.util;

import brs.Constants;

import java.util.Locale;

public class TextUtils {
    public TextUtils() {
    }
    
    private static boolean isInAlphabet(String input, String alphabet) {
        if (input == null) return true;
        for (char c : input.toLowerCase(Locale.ENGLISH).toCharArray()) {
            if (!alphabet.contains(String.valueOf(c))) return false;
        }
        return true;
    }

    public static boolean isInAlphabet(String input) {
        return isInAlphabet(input, Constants.ALPHABET);
    }
    
    public static boolean isInAlphabetOrUnderline(String input) {
        return isInAlphabet(input, Constants.ALPHABET_);
    }
}
