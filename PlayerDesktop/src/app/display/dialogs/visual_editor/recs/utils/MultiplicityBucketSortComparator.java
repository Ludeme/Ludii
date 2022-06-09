package utils;

import codecompletion.domain.model.Instance;
import interfaces.utils.BucketSortComparator;

public class MultiplicityBucketSortComparator implements BucketSortComparator<Pair<Instance,Integer>> {
    @Override
    public int getKey(Pair<Instance, Integer> p) {
        int multiplicity = p.getR().getMultiplicity();
        return multiplicity;
    }
}
