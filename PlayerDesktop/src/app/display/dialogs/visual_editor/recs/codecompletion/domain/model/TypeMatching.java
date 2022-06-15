package app.display.dialogs.visual_editor.recs.codecompletion.domain.model;

import app.display.dialogs.editor.SuggestionInstance;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.filehandling.DocHandler;
import app.display.dialogs.visual_editor.recs.interfaces.codecompletion.domain.model.iGrammar;

import java.util.List;

/**
 * @author filreh
 */
public class TypeMatching implements iGrammar {

    //singleton
    private static TypeMatching typeMatching;


    private String location;

    public static TypeMatching getInstance() {
        // create object if it's not already created
        if(typeMatching == null) {
            typeMatching = new TypeMatching();
        }

        // returns the singleton object
        return typeMatching;
    }

    private TypeMatching() {
        this.location = getLocation();
        //TODO initialize typeMatching object
    }

    /**
     * This method takes a list of instances with matching keys to the context and filters out the ones
     * that do not match the context, leaving only valid choices behind.
     *
     * It expects a wildcard [#] at the position where a recommendation should be made
     *
     * @param match
     * @return
     */
    @Override
    public List<SuggestionInstance> filterOutInvalid(String contextString, List<Instance> match) {
        // 1. find the [#]
        // 2. find the nearest ( before, and determine which ludeme it belongs to
        // 3. find out which constructor it belongs to
        // 4. find all possible parameters

        return null;
    }

    @Override
    public String getLocation() {
        if(location == null) {
            DocHandler docHandler = DocHandler.getInstance();
            location = docHandler.getGrammarLocation();
        }
        return location;
    }

    private static boolean matches(final String charsBefore, final String substitution)
    {
        final boolean result = substitution.startsWith(charsBefore) || substitution.startsWith("("+charsBefore);
        //System.out.println("testing: "+charsBefore+" vs "+substitution);
        return result;
    }
}
