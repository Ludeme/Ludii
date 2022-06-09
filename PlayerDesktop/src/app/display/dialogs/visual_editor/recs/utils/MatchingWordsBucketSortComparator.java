package app.display.dialogs.visual_editor.recs.utils;

import app.display.dialogs.visual_editor.recs.codecompletion.domain.model.Instance;
import app.display.dialogs.visual_editor.recs.interfaces.utils.BucketSortComparator;

public class MatchingWordsBucketSortComparator implements BucketSortComparator<Pair<Instance,Integer>> {
    @Override
    public int getKey(Pair<Instance, Integer> p) {
        int matchingWords = p.getS();
        return matchingWords;
    }
}
