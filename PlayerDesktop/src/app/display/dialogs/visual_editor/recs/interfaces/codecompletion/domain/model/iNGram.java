package interfaces.codecompletion.domain.model;

import codecompletion.domain.model.Instance;

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
     * @return
     */
    List<Instance> getMatch(String key);

    /**
     * Get the value of N for the model.
     * @return
     */
    int getN();

    /**
     * Returns the Map object containing the NGram
     * @return
     */
    Map<String, List<Instance>> getDictionary();
}
