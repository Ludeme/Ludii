package utils;

import codecompletion.domain.model.Instance;
import interfaces.utils.BucketSortComparator;

import java.util.*;

/**
 * @author filreh
 */
public class BucketSort {
    private static final boolean DEBUG = false;

    /**
     * This method takes in an unordered picklist that has already been filtered for invalid choices.
     * Now it first uses a bucket sort to sort it into buckets according to the amount of matching words of each
     * instance.
     * Then it sorts those buckets by multiplicity and ravels everything into one list again.
     *
     * @param unorderedPicklist
     * @return
     */
    public static List<Instance> sort(List<Pair<Instance,Integer>> unorderedPicklist, int limit) {
        //use a bucket sort to sort after matched words
        //this is how each pair is: matchingCountPicklist.add(new Pair<>(new Instance(newInstanceWords,pMultiplicity), maxMatchingWords));
        List<List<Pair<Instance,Integer>>> firstBucketsSortedList = bucketSort(unorderedPicklist, new MatchingWordsBucketSortComparator());
        //count up the first limit items and discard of the rest, this method also sorts the sublists
        List<Pair<Instance,Integer>> sortedListMW = reduceSubListItems(firstBucketsSortedList, limit);
        //extract only the instances
        List<Instance> sortedList = new ArrayList<>();
        sortedListMW.forEach(p -> sortedList.add(p.getR()));
        return sortedList;
    }

    private static  List<Pair<Instance,Integer>> ravelList(List<List<Pair<Instance,Integer>>> list2d) {
        List<Pair<Instance,Integer>> ravelledList = new ArrayList<>();
        for(List<Pair<Instance,Integer>> bucket : list2d) {
            for(Pair<Instance,Integer> p : bucket) {
                ravelledList.add(p);
            }
        }
        return ravelledList;
    }

    /**
     * This sorts the list after some criterion provided by the comparator into buckets,
     * and then empties the buckets into the list in the correct order
     * @param list
     * @return
     */
    private static List<List<Pair<Instance,Integer>>> bucketSort(List<Pair<Instance,Integer>> list, BucketSortComparator<Pair<Instance,Integer>> comparator) {
        Map<Integer,List<Pair<Instance,Integer>>> firstBuckets = new HashMap<>();
        for(Pair<Instance,Integer> p : list) {
            int key = comparator.getKey(p);
            List<Pair<Instance,Integer>> curBucket = firstBuckets.getOrDefault(key, new ArrayList<>());
            curBucket.add(p);
            firstBuckets.put(key,curBucket);
        }
        //count the number of elements in each bucket
        // first is the key from firstBuckets and then the bucketsize
        List<Pair<Integer,Integer>> firstBucketSizes = new ArrayList<>();
        for(Map.Entry<Integer,List<Pair<Instance,Integer>>> entry : firstBuckets.entrySet()) {
            int bucketSize = entry.getValue().size();
            firstBucketSizes.add(new Pair<>(entry.getKey(), bucketSize));
        }
        firstBucketSizes.sort(new Comparator<Pair<Integer, Integer>>() {
            @Override
            public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
                //R is the size of the bucket
                return o2.getR() - o1.getR();
            }
        });
        //create a new list that contains the lists presorted after matchingwords descendingly
        List<List<Pair<Instance,Integer>>> firstBucketsSortedList = new ArrayList<>();
        for(int i = 0; i < firstBucketSizes.size(); i++) {
            int curMatchinWords = firstBucketSizes.get(i).getR();
            List<Pair<Instance,Integer>> curBucket = firstBuckets.get(curMatchinWords);
            firstBucketsSortedList.add(curBucket);
            if(DEBUG)System.out.println("Matching words: " + curMatchinWords + " List of instances: " + curBucket);
        }
        return firstBucketsSortedList;
    }

    /**
     * This method removes any sublist items past the limit, or leaves everything intact if there are less than limit items
     * @param limit
     * @return
     */
    private static List<Pair<Instance,Integer>> reduceSubListItems(List<List<Pair<Instance,Integer>>> list, int limit) {
        int amountOfItems = 0;
        List<Pair<Instance,Integer>> picklist = new ArrayList<>();
        for(int i = 0; i < list.size(); i++) {
            List<Pair<Instance,Integer>> curSubList = list.get(i);
            //first need to sort sublist by multiplicity
            List<List<Pair<Instance,Integer>>> curSubListSorted = bucketSort(curSubList,new MultiplicityBucketSortComparator());
            // ravel the sorted list
            curSubList = ravelList(curSubListSorted);

            //case 1: the recommendations in the current sublist are not over the limit
            if(amountOfItems + curSubList.size() <= limit) {
                picklist.addAll(curSubList);
                amountOfItems += curSubList.size();
                continue;
            }
            //case 2: the recommendations in the current sublist are over the limit
            else {
                // calculate how many recs are still allowed
                int allowed = limit - amountOfItems;
                // the first allowed items of the curSubList are still allowed
                List<Pair<Instance,Integer>> curSubListAllowed = curSubList.subList(0,allowed);
                picklist.addAll(curSubListAllowed);
                break;
            }
        }
        return picklist;
    }
}
