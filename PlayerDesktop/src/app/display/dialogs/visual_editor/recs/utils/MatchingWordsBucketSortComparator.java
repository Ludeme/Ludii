package utils;

import codecompletion.domain.model.Instance;
import interfaces.utils.BucketSortComparator;

public class MatchingWordsBucketSortComparator implements BucketSortComparator<Pair<Instance,Integer>> {
    @Override
    public int getKey(Pair<Instance, Integer> p) {
        int matchingWords = p.getS();
        return matchingWords;
    }
}
