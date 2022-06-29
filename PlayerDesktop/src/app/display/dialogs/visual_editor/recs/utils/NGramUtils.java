package app.display.dialogs.visual_editor.recs.utils;

import app.display.dialogs.visual_editor.recs.codecompletion.Ludeme;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.model.Context;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.model.Instance;

import java.util.*;

/**
 * @author filreh
 */
public class NGramUtils {
    private final static boolean DEBUG = false;

    /**
     * This method returns a list of all substrings of length N
     *
     * @param N
     * @return
     */
    public static List<List<String>> allSubstrings(String gameDescription, int N) {
        String[] words = gameDescription.split(" ");

        List<List<String>> substringsLengthN = new ArrayList<>();
        for(int i = 0; i <= words.length; i++) {
            int substringEnd = i + N;

            if(substringEnd <= words.length) {
                List<String> curSubstring = new ArrayList<>();
                for(int j = i; j < substringEnd; j++) {
                    curSubstring.add(words[j]);
                }
                substringsLengthN.add(curSubstring);
            }
        }
        return substringsLengthN;
    }

    /**
     * This method takes a list of words and turns it into an N Gram instance
     *
     * @param words
     * @return
     */
    public static Instance createInstance(List<String> words) {
        if(words.size() < 2) {
            return null;
        }
        Instance instance = new Instance(words);
        return instance;
    }

    /**
     * This method takes the context string and turns it into a Context object
     *
     * @param context
     * @return
     */
    public static Context createContext(String context) {
        List<String> words = Arrays.asList(context.split(" "));
        Context contextObject = new Context(words);
        return contextObject;
    }

    public static List<Pair<Instance,Integer>> calculateMatchingWords(List<Instance> unorderedPicklist, Context context) {
        List<Pair<Instance,Integer>> unorderedPicklistMatchingWords = new ArrayList<>();
        for(Instance instance : unorderedPicklist) {
            int matchingWords = instance.matchingWords(context);
            Pair<Instance,Integer> cur = new Pair<>(instance, matchingWords);
            unorderedPicklistMatchingWords.add(cur);
        }
        return  unorderedPicklistMatchingWords;
    }

    /**
     * This method extracts the name of a game out of its description as lud code
     * @param gameDescription
     * @return
     */
    public static String getGameName(String gameDescription) {
        String gameLudeme = "(game";
        int gameLocation = gameDescription.lastIndexOf(gameLudeme);
        char[] gameDescrChars = gameDescription.toCharArray();
        String gameName = "";
        boolean start = false;
        // iterate over game description to find the name of the game
        loop:for(int j = gameLocation; j < gameDescription.length(); j++) {
            char cur = gameDescrChars[j];
            if(cur == '"' && !start) {
                start = true;
            }  else if(cur == '"' && start) {
                // found end of string
                break loop;
            } else if(start) {
                gameName += cur;
            }
        }
        return gameName;
    }
    
    public static List<Pair<Instance, Integer>> uniteDuplicatePredictions(List<Instance> match, Context context) {
        //this list also stores the amount of matching words at the end of the ngram with the context
        List<Pair<Instance,Integer>> unorderedPicklistMatchingWords = NGramUtils.calculateMatchingWords(match,context);
        //sort into hashmap by prediction
        //now unite all ngrams with the same prediction into one
        //sort into hashmap by prediction
        HashMap<String,List<Pair<Instance,Integer>>> predictionMatch = new HashMap<>();
        for(Pair<Instance,Integer> p : unorderedPicklistMatchingWords) {
            String prediction = p.getR().getPrediction();
            // list of instances with the same prediction as p
            List<Pair<Instance,Integer>> samePredictionAsP = predictionMatch.getOrDefault(prediction, new ArrayList<>());
            samePredictionAsP.add(p);
            predictionMatch.put(prediction,samePredictionAsP);
        }
        //for each stored prediction, sum up the multiplicities & take the highest # matching words to create a new prediction
        Set<Map.Entry<String, List<Pair<Instance, Integer>>>> pmEntrySet = predictionMatch.entrySet();
        // wipe this clean to rewrite to it with united instances
        List<Pair<Instance, Integer>> uniquePredictions = new ArrayList<>();
        for(Map.Entry<String, List<Pair<Instance, Integer>>> entry : pmEntrySet) {
            int pMultiplicity = 0;
            int maxMatchingWords = 0;
            String entryPrediction = entry.getKey();
            Instance firstInstance = entry.getValue().get(0).getR();
            //not all instances have the same words, but same key bcs of NGram.getMatch and same prediction because of predictionMatch
            //therefore the resulting instance must have words equal to {key,prediction}
            List<String> newInstanceWords = Arrays.asList(firstInstance.getKey(),firstInstance.getPrediction());
            for(Pair<Instance,Integer> p : entry.getValue()) {
                pMultiplicity += p.getR().getMultiplicity();
                //if p has more matching words than stored, update, else do nothing
                maxMatchingWords = p.getS() > maxMatchingWords ? p.getS() : maxMatchingWords;
            }
            uniquePredictions.add(new Pair<>(new Instance(newInstanceWords,pMultiplicity), maxMatchingWords));
        }

        return  uniquePredictions;
    }

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

    public static List<Ludeme> filterByBegunWord(String begunWord, List<Ludeme> preliminaryPicklist) {
        List<Ludeme> picklist = new ArrayList<>();
        for(Ludeme ludeme : preliminaryPicklist) {
            String keyword = ludeme.getKeyword();
            if(keyword.startsWith(begunWord) || StringUtils.equals(keyword,begunWord)) {
                picklist.add(ludeme);
            }
        }
        return picklist;
    }
}
