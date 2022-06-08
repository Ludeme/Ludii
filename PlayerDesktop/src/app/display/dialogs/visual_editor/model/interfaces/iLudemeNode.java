package app.display.dialogs.visual_editor.model.interfaces;

import app.display.dialogs.visual_editor.model.grammar.Constructor;
import app.display.dialogs.visual_editor.model.grammar.Ludeme;
import app.display.dialogs.visual_editor.model.grammar.input.Input;
import main.grammar.Clause;
import main.grammar.Symbol;

/**
 * Interface for a node representation of a ludeme in the current description
 * @author Filipp Dokienko
 */

public interface iLudemeNode {

    // Symbol/Ludeme that the node represents
    Symbol symbol();
    // Current selected clause by the user
    Clause selectedClause();
    // Current provided inputs
    Object[] providedInputs();
    // Set an input
    void setProvidedInput(int index, Object input);
    // Sets the parent of the node to another node
    void setParent(iLudemeNode ludemeNode);

    /* The .lud equivalent of that node
     e.g. for a (game ) for an empty <game> LudemeNode
     */
    String getStringRepresentation();


}
