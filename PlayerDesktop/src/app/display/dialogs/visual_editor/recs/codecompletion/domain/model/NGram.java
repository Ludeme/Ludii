package app.display.dialogs.visual_editor.recs.codecompletion.domain.model;

import app.display.dialogs.visual_editor.recs.interfaces.codecompletion.domain.model.iNGram;
import app.display.dialogs.visual_editor.recs.utils.StringUtils;

import java.util.*;

import static app.display.dialogs.visual_editor.recs.codecompletion.domain.model.Preprocessing.*;

/**
 * @author filreh
 */
public class NGram implements iNGram {
    private final int N;
    private HashMap<String, List<Instance>> dictionary;

    public NGram(int N) {
        this.N = N;
        this.dictionary = new HashMap<>();
        addSpecialCases();
    }

    /**
     * This constructor is to read in a model
     * @param N
     * @param dictionary
     */
    public NGram(int N, HashMap<String, List<Instance>> dictionary) {
        this.N = N;
        this.dictionary = dictionary;
        addSpecialCases();
    }

    private void addSpecialCases() {
        String key = "";
        List<Instance> specialCases = new ArrayList<>();
        specialCases.add(new Instance(Arrays.asList(new String[]{"","(game"}),Integer.MAX_VALUE - 1));
        specialCases.add(new Instance(Arrays.asList(new String[]{"","(define"}),Integer.MAX_VALUE - 2));
        specialCases.add(new Instance(Arrays.asList(new String[]{"","(metadata"}),Integer.MAX_VALUE - 3));
        dictionary.put(key,specialCases);
    }

    /**
     * This method adds one instance to the model, be weary of multiplicities
     *
     * @param instance
     */
    @Override
    public void addInstanceToModel(Instance instance) {
        if(!specifyNumberAndBoolean(instance)) {
            List<Instance> recs = dictionary.getOrDefault(instance.getKey(), new ArrayList<>());
            boolean foundEqual = false;
            for (Instance i : recs) {
                if (i.equals(instance)) {
                    // found an instance with the same words --> increase its multiplicity
                    i.increaseMultiplicity();
                    foundEqual = true;
                    break;
                }
            }

            if (!foundEqual) {
                recs.add(instance);
            }

            dictionary.put(instance.getKey(), recs);
        }
    }

    /**
     * This method duplicates any instance with NUMBER_WILDCARD in it to be an instance with
     * FLOAT and INT wildcards.
     * @param instance
     */
    private boolean specifyNumberAndBoolean(Instance instance) {
        //TODO Move this method into the getPicklist, then it only gets applied to the requested ludemes
        //and keeps the filesize down
        boolean containsNumber = false, containsBoolean = false;
        String[] wildcards = new String[] {INTEGER_WILDCARD, FLOAT_WILDCARD};
        String[] booleanLudemes = new String[] {"True","False"};
        List<String> words = instance.getWords();
        //to replace the generic number wildcard with float and int wildcards
        for(String wildcard : wildcards) {
            if(instance.getWords().contains(NUMBER_WILDCARD)) {
                containsNumber = true;
                List<String> newWords = new ArrayList<>();
                for(String word : words) {
                    if(StringUtils.equals(word, NUMBER_WILDCARD)) {
                        newWords.add(wildcard);
                    } else {
                        newWords.add(word);
                    }
                }
                Instance newInstance = new Instance(newWords, instance.getMultiplicity());
                addInstanceToModel(newInstance);
            }
        }

        // to replace the generic boolean wildcard
        for(String booleanValue : booleanLudemes) {
            if(instance.getWords().contains(BOOLEAN_WILDCARD)) {
                containsBoolean = true;
                List<String> newWords = new ArrayList<>();
                for(String word : words) {
                    if(StringUtils.equals(word, BOOLEAN_WILDCARD)) {
                        newWords.add(booleanValue);
                    } else {
                        newWords.add(word);
                    }
                }
                Instance newInstance = new Instance(newWords, instance.getMultiplicity());
                addInstanceToModel(newInstance);
            }
        }
        return (containsNumber || containsBoolean);
    }

    /**
     * This method returns a list of all instances with the same key as the provided one.
     *
     * @param key
     */
    @Override
    public List<Instance> getMatch(String key) {
        List<Instance> match = dictionary.getOrDefault(key, new ArrayList<>());
        return match;
    }

    /**
     * Get the value of N for the model.
     */
    @Override
    public int getN() {
        return N;
    }

    /**
     * Returns the Map object containing the NGram
     */
    @Override
    public Map<String, List<Instance>> getDictionary() {
        return dictionary;
    }
}
