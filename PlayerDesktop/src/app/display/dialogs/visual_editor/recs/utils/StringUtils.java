package app.display.dialogs.visual_editor.recs.utils;

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

    public static String charsBeforeCursor(String text, int caretPosition)
    {
        if (text.isEmpty()) return "";

        final int pos = caretPosition;
        int start = pos-1;
        try {
            while (start > 0 && Character.isLetterOrDigit(getSubstring(text,start-1,1).charAt(0)))
                start--;

            final String result = getSubstring(text,Math.max(0,start),pos-start);
            return result;
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * get the substring that starts at index offs with a length of len
     * @param s
     * @param offs
     * @param len
     * @return
     */
    public static String getSubstring(String s, int offs, int len) {
        int len2;
        if(offs + len == s.length() + 1) {
             len2 = len - 1;
        } else {
            len2 = len;
        }
        String substring = s.substring(offs, offs+len2);
        return substring;
    }

    public static String repeat(String s, int repetitions) {
        if(repetitions <= 0) {
            return "";
        } else {
            String t = "";
            for(int i = 0; i < repetitions; i++) {
                t += s;
            }
            return t;
        }
    }
}
