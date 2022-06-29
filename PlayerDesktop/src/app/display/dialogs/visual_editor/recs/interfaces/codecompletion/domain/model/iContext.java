package app.display.dialogs.visual_editor.recs.interfaces.codecompletion.domain.model;

import java.util.List;

/**
 * @author filreh
 */
public interface iContext {
    /**
     * This method produces a string that represents the context with all its fields.
     * @return
     */
    String toString();

    String getKey();

    List<String> getWords();
}
