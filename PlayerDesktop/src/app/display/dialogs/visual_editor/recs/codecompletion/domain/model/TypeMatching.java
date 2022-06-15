package app.display.dialogs.visual_editor.recs.codecompletion.domain.model;

import app.display.dialogs.editor.EditorHelpDataHelper;
import app.display.dialogs.editor.SuggestionInstance;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.filehandling.DocHandler;
import app.display.dialogs.visual_editor.recs.interfaces.codecompletion.domain.model.iGrammar;
import app.display.dialogs.visual_editor.recs.utils.StringUtils;
import grammar.Grammar;
import main.EditorHelpData;

import java.util.ArrayList;
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
     * @param match
     * @return
     */
    @Override
    public List<SuggestionInstance> filterOutInvalid(String contextString, List<Instance> match, int caretPosition) {
        // 1. get list of Suggestion Instances from Grammar
        boolean isPartial = false;
        List<SuggestionInstance> suggestionInstances = new ArrayList<>();
        final List<String> allCandidates = Grammar.grammar().classPaths(contextString, caretPosition, isPartial);

        for(String candidate: allCandidates) {
            System.out.println("candidate: " + candidate);
        }

        final List<SuggestionInstance> suggestionsFromClasspaths = EditorHelpDataHelper.suggestionsForClasspaths(EditorHelpData.get(), allCandidates, isPartial);

        final String charsBefore = StringUtils.charsBeforeCursor(contextString, caretPosition);
        //System.out.println("### charsBefore:" + charsBefore);

        for (final SuggestionInstance si: suggestionsFromClasspaths)
        {
            if (!isPartial || matches(charsBefore, si.getSubstitution()))
                suggestionInstances.add(si);
        }
        return suggestionInstances;
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
