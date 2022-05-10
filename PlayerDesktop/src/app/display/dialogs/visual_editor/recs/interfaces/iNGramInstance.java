package app.display.dialogs.visual_editor.recs.interfaces;

import java.util.List;

/**
 *
 * @param <G> class of words
 */
public interface iNGramInstance<H,G> {
    List<G> getWords();
    int getMultiplicity();
    G getKey();
    G getLast();
    void increaseMultiplicity();
    boolean equals(iNGramInstance<H,G> otherInstance);
    boolean isWordEqual(G w1, G w2);
    boolean contains(H context);
    String toString();
}
