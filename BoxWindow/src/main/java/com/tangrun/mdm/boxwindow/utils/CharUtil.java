package com.tangrun.mdm.boxwindow.utils;

public class CharUtil {
    public static String encode(String s) {
        // a-z 97-122
        // A-Z 65-90
        // 0-9 48-57
        char[] chars = s.toCharArray();
        char[] newChars = new char[chars.length];
        for (int i = 0; i < chars.length; i++) {
            char origin = chars[i];
            if (origin <= 122 && origin >= 97) {
                int offset = origin - 97;
                origin = (char) (90 - offset);
            } else if (origin <= 90 && origin >= 65) {
                int offset = origin - 65;
                origin = (char) (122 - offset);
            } else if (origin <= 57 && origin >= 48) {
                origin = (char) (105 - origin);
            }
            newChars[i] = origin;
        }
        return new String(newChars);
    }
}
