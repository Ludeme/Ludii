package interfaces.codecompletion.domain.model;

import codecompletion.domain.model.Context;
import codecompletion.domain.model.Instance;

import java.util.List;

/**
 * @author filreh
 */
public interface iGrammar {
    /**
     * This method takes a list of instances with matching keys to the context and filters out the ones
     * that do not match the context, leaving only valid choices behind.
     * @param context
     * @param match
     * @return
     */
    List<Instance> filterOutInvalid(Context context, List<Instance> match);

    String getLocation();
}
