package app.display.dialogs.visual_editor.recs.interfaces.codecompletion.domain.model;

import app.display.dialogs.visual_editor.recs.codecompletion.domain.model.Instance;

import java.util.List;
import java.util.Map;

/**
 * @author filreh
 */
public interface iNGram {

    /**
     * This method adds one instance to the model, be weary of multiplicities
     * @param instance
     */
    void addInstanceToModel(Instance instance);

    /**
     * This method returns a list of all instances with the same key as the provided one.
     * @param key
     */
    List<Instance> getMatch(String key);

    /**
     * Get the value of N for the model.
     */
    int getN();

    /**
     * Returns the Map object containing the NGram
     */
    Map<String, List<Instance>> getDictionary();
}
