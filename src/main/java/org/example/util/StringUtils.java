package org.example.util;

public class StringUtils {

    private StringUtils() { }

    /**
     * Provides extracted string without quotes from provided value
     * @param value Original string that might contain surrounding quotes
     * @return String without surrounding quotes
     */
    public static String getStringWithoutSurroundingQuotes(String value) {
        if (value.startsWith("\"") && value.endsWith("\"") ||
                value.startsWith("'") && value.endsWith("'")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
