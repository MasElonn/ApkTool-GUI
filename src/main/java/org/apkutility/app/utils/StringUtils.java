package org.apkutility.app.utils;

public final class StringUtils {

    private StringUtils() {
    }

    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean notBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
