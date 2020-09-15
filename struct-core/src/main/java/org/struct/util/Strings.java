package org.struct.util;

/**
 * @author TinyZ.
 * @version 2020.05.01
 */
public class Strings {

    public static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd";

    public static final String DATE_TIME_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private Strings() {
        //  no-op
    }

    public static String toUpperCaseFirstChar(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }
}
