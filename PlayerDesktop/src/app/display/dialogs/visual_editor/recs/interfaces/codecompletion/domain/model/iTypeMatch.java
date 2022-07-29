package app.display.dialogs.visual_editor.recs.interfaces.codecompletion.domain.model;

import app.display.dialogs.visual_editor.recs.codecompletion.controller.NGramController;
import main.grammar.Symbol;

import java.util.List;

public interface iTypeMatch {

    /**
     * This method takes in the game description as a string and a list of suggestions from the grammar
     * Then it
     * @param gameDescription
     * @param possibleSymbols
     */
    List<Symbol> typematch(String gameDescription, NGramController NGramController, List<Symbol> possibleSymbols);
}
