package utils;

/**
 * @author filreh
 */
public class StringUtils {
    /**
     * This method compares two strings for equality. Possibility for use of hashing.
     *
     * @param s1
     * @param s2
     * @return
     */
    public static boolean equals(String s1, String s2) {
        return s1.equals(s2);
    }

    /**
     * Source: https://www.techiedelight.com/how-to-remove-a-suffix-from-a-string-in-java/
     * @param s
     * @param suffix
     * @return
     */
    public static String removeSuffix(final String s, final String suffix)
    {
        if (s != null && suffix != null && s.endsWith(suffix)) {
            return s.substring(0, s.length() - suffix.length());
        }
        return s;
    }
}
