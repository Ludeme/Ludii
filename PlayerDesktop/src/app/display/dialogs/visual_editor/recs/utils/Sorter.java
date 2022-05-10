package app.display.dialogs.visual_editor.recs.utils;


import app.display.dialogs.visual_editor.recs.model.Ludii.NGramInstanceLudii;

import java.util.*;

public class Sorter {
    /**
     * This list fisrt uses a bucketsort to sort all recommendations after the number of occurrences
     * It the filters out the the top entries, specified in the limit parameter
     * Then it uses a second becket sort for every previous bucket to sort them by the number of occurrences
     * @param matchingCountPicklist
     * @param limit
     * @return
     */
    public static  List<Pair<NGramInstanceLudii,Integer>> nestedBucketSort(List<Pair<NGramInstanceLudii,Integer>> matchingCountPicklist, int limit) {
        //use a bucket sort to sort after matched words
        //this is how each pair is: matchingCountPicklist.add(new Pair<>(new NGramInstanceLudii(newInstanceWords,pMultiplicity), maxMatchingWords));
        Map<Integer,List<Pair<NGramInstanceLudii,Integer>>> firstBuckets = new HashMap<>();
        for(Pair<NGramInstanceLudii,Integer> p : matchingCountPicklist) {
            int matchingWords = p.getS();
            List<Pair<NGramInstanceLudii,Integer>> curBucket = firstBuckets.getOrDefault(matchingWords, new ArrayList<>());
            curBucket.add(p);
            firstBuckets.put(matchingWords,curBucket);
        }
        //count the number of elements in each bucket
        List<Pair<Integer,Integer>> firstBucketSizes = new ArrayList<>();
        for(Map.Entry<Integer,List<Pair<NGramInstanceLudii,Integer>>> entry : firstBuckets.entrySet()) {
            int bucketSize = entry.getValue().size();
            firstBucketSizes.add(new Pair<>(entry.getKey(), bucketSize));
        }
        firstBucketSizes.sort(new Comparator<Pair<Integer, Integer>>() {
            @Override
            public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
                //R is the size of the bucket
                return o1.getR() - o2.getR();
            }
        });
        //now create a new map, where all the elements together are at most limit
        Map<Integer,List<Pair<NGramInstanceLudii,Integer>>> secondBuckets = new HashMap<>();
        int sum = 0;
        for(Pair<Integer,Integer> p : firstBucketSizes) {
            int matchingWords = p.getR();
            int bucketSize = p.getS();
            List<Pair<NGramInstanceLudii, Integer>> curBucket = firstBuckets.get(matchingWords);
            //TODO: sort by occurrence before putting
            if(sum + bucketSize <= limit) {
                //if all elements fit, just add the whole bucket to the second map
                secondBuckets.put(matchingWords,firstBuckets.get(matchingWords));
                sum += bucketSize;
            } else {
                int leftOverSpace = limit - sum;
                //take the first leftOverSpace items and just put them in a bucket
                secondBuckets.put(matchingWords,firstBuckets.get(matchingWords).subList(0, leftOverSpace + 1));
            }
        }

        List<Pair<NGramInstanceLudii,Integer>> sortedList = new ArrayList<Pair<NGramInstanceLudii,Integer>>();
        return sortedList;
    }

    /**
     * Standard sort used before
     * @param matchingCountPicklist
     * @return
     */
    public static  List<Pair<NGramInstanceLudii,Integer>> defaultSort(List<Pair<NGramInstanceLudii,Integer>> matchingCountPicklist) {
        matchingCountPicklist.sort(new Comparator<Pair<NGramInstanceLudii, Integer>>() {
            @Override
            public int compare(Pair<NGramInstanceLudii, Integer> p1, Pair<NGramInstanceLudii, Integer> p2) {
                int matchingWords1 = p1.getS(), matchingWords2 = p2.getS();
                int diffMatchingwords = matchingWords2 - matchingWords1;
                if(diffMatchingwords == 0) {
                    //need to check multiplicity, since both have the same multiplicity
                    int multiplicity1 = p1.getR().getMultiplicity(), multiplicity2 = p2.getR().getMultiplicity();
                    int diffMultiplicity = multiplicity2 - multiplicity1;
                    return diffMultiplicity;
                } else {
                    return diffMatchingwords;
                }
            }
        });
        return matchingCountPicklist;
    }
}
