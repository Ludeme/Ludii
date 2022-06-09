package codecompletion.domain.model;

import interfaces.codecompletion.domain.model.iInstance;
import utils.ListUtils;
import utils.StringUtils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

/**
 * @author filreh
 */
public class Instance implements iInstance {

    private final List<String> words;
    private final Context context;
    private final int length;
    private final String prediction;
    private final String key;
    private int multiplicity;

    public Instance(List<String> words, int multiplicity) {
        this.multiplicity = multiplicity;
        this.words = words;
        this.length = words.size();
        //the last word is left out of the context because it is the prediction
        this.context = new Context(words.subList(0,length - 1));
        this.prediction = words.get(length - 1);
        this.key = words.get(length - 2);
    }

    public Instance(List<String> words) {
        this.multiplicity = 1;
        this.words = words;
        this.length = words.size();
        //the last word is left out of the context because it is the prediction
        this.context = new Context(words.subList(0,length - 1));
        this.prediction = words.get(length - 1);
        this.key = words.get(length - 2);
    }

    /**
     * This method increases the multiplicity of the instance by one.
     */
    @Override
    public void increaseMultiplicity() {
        this.multiplicity++;
    }

    /**
     * This method simply counts up the number of words this instance has in common with the context,
     * starting at the back.
     *
     * @param c
     * @return
     */
    @Override
    public int matchingWords(Context c) {
        List<String> foreignContextWords = c.getWords();
        List<String> thisContextWords = this.context.getWords();
        int matchingWords = ListUtils.stringsInCommonBack(foreignContextWords,thisContextWords);
        return matchingWords;
    }

    @Override
    public String getPrediction() {
        return this.prediction;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public List<String> getWords() {
        return this.words;
    }

    @Override
    public Context getContext() {
        return this.context;
    }

    @Override
    public int getMultiplicity() {
        return this.multiplicity;
    }

    @Override
    public String toString() {
        String output = "{Words: ";
        for(String word : words) {
            output += word + " ";
        }
        output += "Prediction: " + prediction + " Key: "+key+" Multiplicity: " + multiplicity+"}";
        return  output;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Instance) {
            Instance i = (Instance) o;
            List<String> iWords = i.getWords();

            if(iWords.size() == words.size()) {
                for(int j = 0; j < words.size(); j++) {
                    if(!StringUtils.equals(iWords.get(j),words.get(j))) {
                        return false;
                    }
                }

                return true;
            }
        }
        return false;
    }
}
