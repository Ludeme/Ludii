package app.display.dialogs.visual_editor.recs.utils;

public class StringUtils {
    public static boolean isInteger(String s) {
        int commas = 0;
        for(int i = 0; i < s.length(); i++) {
            char cur = s.charAt(i);
            switch(cur) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    public static boolean isRealNumber(String s) {
        String[] split;
        if(s.contains(",")) {
            split = s.split(",");
        } else if(s.contains(".")) {
            split = s.split(".");
        } else {
            split = new String[]{s};
        }

        if(split.length > 2) { // can only have at most 1 comma
            return false;
        }
        if(split.length == 2) {
            return isInteger(split[0]) && isInteger(split[1]);
        } else if(split.length == 1) {
            return isInteger(split[0]);
        }
        return false;
    }

    public static boolean containsOnly(String s, char c) {
        for(int i = 0; i < s.length(); i++) {
            if(s.charAt(i) != c) {
                return false;
            }
        }
        return true;
    }
}
