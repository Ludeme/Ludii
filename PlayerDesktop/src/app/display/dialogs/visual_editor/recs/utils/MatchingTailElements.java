package app.display.dialogs.visual_editor.recs.utils;

import java.util.List;

public final class MatchingTailElements {
    private static final boolean DEBUG = false;

    /**
     * Returns the number of matching elements starting at the end of the list.
     * E.g.1: Same last element
     * --> get(superlist=[1,52,6,2,9],sublist=[1,2,3,4,5,6,7,8,9]) = 1
     * E.g.2: Same first 5 elements but different last element
     * --> get(superlist=[1,2,3,4,5],sublist=[1,2,3,4,5,6]) = 0
     * E.g.3: Same first 4 elements but different last element
     * --> get(superlist=[1,2,3,4,5],sublist=[1,2,3,4,6]) = 0
     * E.g.4: Same last 3 elements
     * --> get(superlist=[1,2,3,4,5],sublist=[3,4,5]) = 3
     * E.g.5: sublist is a sublist of superlist, but not at the end
     * --> get(superlist=[0,1,2,3,0],sublist=[1,2,3]) = 0
     * E.g.6: sublist is longer than superlist, but the last 3 elements match
     * --> get(superlist=[3,4,5],sublist=[1,2,3,4,5]) = 3
     * @param superlist
     * @param sublist
     * @param <F>
     * @return
     */
    public static <F> int count(List<F> superlist, List<F> sublist) {
        if(DEBUG)System.out.println("------------Match----------");
        if(DEBUG)System.out.println(superlist);
        if(DEBUG)System.out.println(sublist);
        //0. if one of them is empty, return false
        if(superlist.isEmpty() || sublist.isEmpty())
            return 0;
        //2. Starting at the back, compare the elements of both lists
        int sameTailLength = 0;
        if(DEBUG)System.out.println("SUPERLIST.size():"+superlist.size() + " SUBLIST:size():" + sublist.size());
        for(int i = 0; i < superlist.size() && i < sublist.size(); i++){
            F superW = superlist.get(superlist.size() - i - 1), subW = sublist.get(sublist.size() - i - 1);
            if(DEBUG)System.out.println("SUPER word:\""+superW+"\" SUB word:"+subW);
            if(subW.equals(superW)) {
                sameTailLength++;
            } else {//no match anymore, end loop
                break;
            }
        }
        if(DEBUG)System.out.println("MATCHING WORDS:"+sameTailLength);
        return sameTailLength;
    }
}
