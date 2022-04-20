package app.display.dialogs.visual_editor.recs.model.Ludii;


import app.display.dialogs.visual_editor.recs.interfaces.iNGramInstance;

import java.util.List;

public class NGramInstanceLudii implements iNGramInstance<List<String>,String> {
    private int multiplicity;
    private List<String> words;
    private final String key;

    public NGramInstanceLudii(List<String> words) {
        this.words = words;
        this.multiplicity = 1;
        key = words.get(words.size() - 2);
    }

    public NGramInstanceLudii(List<String> words, int multiplicity) {
        this.words = words;
        this.multiplicity = multiplicity;
        key = words.get(words.size() - 2);
    }

    @Override
    public List<String> getWords() {
        return words;
    }

    @Override
    public int getMultiplicity() {
        return multiplicity;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getLast() {
        return words.get(words.size() - 1);
    }

    @Override
    public void increaseMultiplicity() {
        this.multiplicity++;
    }

    @Override
    public boolean equals(iNGramInstance<List<String>,String> otherInstance) {
        List<String> otherWords = otherInstance.getWords();
        if(words.size() != otherWords.size()) {
            return false;
        }
        for(int i = 0; i < words.size(); i++) {
            String w1 = words.get(i), w2 = otherWords.get(i);
            if(!isWordEqual(w1,w2)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isWordEqual(String w1, String w2) {
        return w1.equals(w2);
    }

    @Override
    public boolean contains(List<String> context) {

        return false;
    }

    public String toString() {
        return "Key: "+key+" Words: " + words.toString() + " Multiplicity: " +multiplicity;
    }
}
