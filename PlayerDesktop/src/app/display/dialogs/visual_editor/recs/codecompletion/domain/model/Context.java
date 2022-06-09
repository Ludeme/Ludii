package codecompletion.domain.model;

import interfaces.codecompletion.domain.model.iContext;

import java.util.List;

/**
 * @author filreh
 */
public class Context implements iContext {

    private final List<String> words;
    private final int length;
    private final String key;

    /**
     * This class expects the words to have already undergone preprocessing
     * @param words
     */
    public Context(List<String> words) {
        this.words = words;
        this.length = words.size();
        this.key = words.get(length - 1);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public List<String> getWords() {
        return words;
    }

    @Override
    public String toString() {
        String output = "{Words: ";
        for(String word : words) {
            output += word + " ";
        }
        output += "Key: "+key+"}";
        return  output;
    }
}
