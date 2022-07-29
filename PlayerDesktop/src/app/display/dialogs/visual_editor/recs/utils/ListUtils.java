package app.display.dialogs.visual_editor.recs.utils;

import java.util.List;

public class ListUtils {
    /**
     * This method checks how many Strings two lists have in common, starting at the back
     * @param l1
     */
    public static int stringsInCommonBack(List<String> l1, List<String> l2) {
        int l1Length = l1.size();
        int l2Length = l2.size();
        //get the smaller length
        int smallerLength = l1Length < l2Length  ? l1Length : l2Length ;
        int matchingWords = 0;
        for(int i = 0; i < smallerLength; i++) {
            //iterate both from the back
            //this is the ith word from the back
            String curWordL1 = l1.get(l1Length - 1 - i);
            String curWordL2 = l2.get(l2Length  - 1 - i);

            if(StringUtils.equals(curWordL1, curWordL2)) {
                matchingWords++;
            } else {
                break;
            }
        }
        return matchingWords;
    }
}
