package app.display.dialogs.visual_editor.recs.codecompletion.domain.model;

import app.display.dialogs.visual_editor.recs.interfaces.codecompletion.domain.model.iTypeMatch;
import main.grammar.Symbol;

import java.util.List;

public class TypeMatch implements iTypeMatch {

    private static TypeMatch instance;

    public static TypeMatch getInstance() {
        if(instance == null) {
            instance = new TypeMatch();
        }
        return instance;
    }

    private TypeMatch() {

    }

    /**
     * This method takes in the game description as a string and a list of suggestions from the grammar
     * Then it
     *
     * @param gameDescription
     * @param possibleSymbols
     * @return
     */
    @Override
    public List<Symbol> typematch(String gameDescription, List<Symbol> possibleSymbols) {
        return possibleSymbols;
    }
}
