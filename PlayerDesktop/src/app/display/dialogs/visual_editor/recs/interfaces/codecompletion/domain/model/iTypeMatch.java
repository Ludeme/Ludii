package app.display.dialogs.visual_editor.recs.interfaces.codecompletion.domain.model;

import main.grammar.Symbol;

import java.util.List;

public interface iTypeMatch {

    /**
     * This method takes in the game description as a string and a list of suggestions from the grammar
     * Then it
     * @param gameDescription
     * @param possibleSymbols
     * @return
     */
    List<Symbol> typematch(String gameDescription, List<Symbol> possibleSymbols);
}
