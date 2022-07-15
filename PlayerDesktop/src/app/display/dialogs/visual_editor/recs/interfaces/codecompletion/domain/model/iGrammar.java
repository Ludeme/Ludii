package app.display.dialogs.visual_editor.recs.interfaces.codecompletion.domain.model;

import app.display.dialogs.editor.SuggestionInstance;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.model.Instance;

import java.util.List;

/**
 * @author filreh
 */
public interface iGrammar {
    /**
     * This method takes a list of instances with matching keys to the context and filters out the ones
     * that do not match the context, leaving only valid choices behind.
     * @param match
     * @return
     */

    List<SuggestionInstance> filterOutInvalid(String contextString, List<Instance> match);

    String getLocation();
}
