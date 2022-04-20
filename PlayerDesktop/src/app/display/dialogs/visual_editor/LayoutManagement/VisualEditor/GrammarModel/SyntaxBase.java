package app.display.dialogs.visual_editor.LayoutManagement.VisualEditor.GrammarModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static app.display.dialogs.visual_editor.LayoutManagement.VisualEditor.GrammarModelUtils.checkBrackets;
import static app.display.dialogs.visual_editor.LayoutManagement.VisualEditor.LayoutConfigs.GRAMMAR_PATH;
import static app.display.dialogs.visual_editor.LayoutManagement.VisualEditor.LayoutConfigs.PRODUCTION;

/**
 * The class can be utilized as the session storage for the ludeme language attributes i.e. syntax, categories...
 * @author nic0gin
 */
public class SyntaxBase
{
    private static SyntaxBase baseInstance;

    private final HashMap<String, List<String>> CategoryMap;
    private final HashMap<String, List<String>> SyntaxMap;

    private static final String CATEGORY_START = "// ";
    private static final String CATEGORY_END = "\n";
    private static final String LUDEME_END = "//-";

    private static final String REFERENCE_START = "\n<";
    private static final String REFERENCE_END = ">";
    private static final String SYNTAX_START = "::=";

    private SyntaxBase ()
    {
        CategoryMap = new HashMap<>();
        SyntaxMap = new HashMap<>();
    }

    public static SyntaxBase getInstance()
    {
        if (baseInstance == null)
        {
            baseInstance = new SyntaxBase();
        }
        return baseInstance;
    }

    public boolean generateSyntaxBase()
    {
        StringBuilder contents = null;

        // model.grammar to String
        //-------------------------------------------------------------------------

        if (PRODUCTION)
        {
            // In the production ready version, model.grammar is generated with respect to up-to-date ludii code base
            // call Grammar.model.grammar().toString()
        }
        else
        {
            File grammar = new File(GRAMMAR_PATH);
            try
            {
                contents = new StringBuilder(new String(Files.readAllBytes(Paths.get(grammar.toURI()))));
            }
            catch (IOException e)
            {
                System.out.println("Problems with reading model.grammar file");
                System.out.println(e.getMessage());
            }

        }

        // parse model.grammar to syntax instances
        //-------------------------------------------------------------------------
        parseReferences(contents);

        return false;
    }

    private void parseReferences(StringBuilder grammar)
    {

        // separate ludemes by categories
        // extracting pattern: category
        categoriseLudemes(grammar);

        // extracting pattern: ludeme
        for (Map.Entry<String, List<String>> stringListEntry : CategoryMap.entrySet())
        {

            // Process category instances
            // Ludeme references for current category
            StringBuilder ludemeInstance = new StringBuilder(stringListEntry.getValue().get(0));
            stringListEntry.setValue(new ArrayList<String>());
            //
            parseLudemesOfCategory(stringListEntry, ludemeInstance);

        }

    }

    private void parseLudemesOfCategory(Map.Entry<String, List<String>> stringListEntry, StringBuilder ludemeInstance) {
        while (!ludemeInstance.isEmpty())
        {
            // FIXME: refactor this
            // Parse the name of ludeme reference
            int referenceStart = ludemeInstance.indexOf(REFERENCE_START);
            int referenceEnd = ludemeInstance.indexOf(REFERENCE_END);
            String referenceName = ludemeInstance.substring(referenceStart+REFERENCE_START.length(), referenceEnd);

            // Parse the syntax of implementation of ludeme reference
            int syntaxStart = ludemeInstance.indexOf(SYNTAX_START, referenceEnd);
            int syntaxEnd = ludemeInstance.indexOf(REFERENCE_START, syntaxStart);
            String syntax = null;
            if (syntaxEnd < 0)
            {
                syntaxEnd = ludemeInstance.indexOf("\n\n", syntaxStart);
                syntax = ludemeInstance.substring(syntaxStart+SYNTAX_START.length()+1, syntaxEnd);
                ludemeInstance.delete(0, syntaxEnd+2);
            }
            else
            {
                syntax = ludemeInstance.substring(syntaxStart+SYNTAX_START.length()+1, syntaxEnd);
                // Clear processed instance
                ludemeInstance.delete(0, syntaxEnd);
            }

            // remove "\n              " from syntax instances (new line and 15 spaces)
            syntax = syntax.replaceAll("\n {15}", "");

            // breakdown sequence by implementations " | "
            String OR = " \\| ";
            ArrayList<String> implementations = new ArrayList<>(Arrays.asList(syntax.split(OR)));
            revertInvalidSplits(implementations);
            // Store implementations of ludeme
            SyntaxMap.put(referenceName, implementations);

            // Store reference name of ludeme for processed category instances
            stringListEntry.getValue().add(referenceName);

        }
    }

    private void revertInvalidSplits(ArrayList<String> implementations) {
        ListIterator<String> impIter = implementations.listIterator();

        while (impIter.hasNext())
        {
            String impInst = impIter.next();
            // check brackets of split instances
            if (!checkBrackets(impInst))
            {
                // if split was not "high-level": concatenate with the following instance
                int id =  impIter.nextIndex();
                String impInstNext = implementations.get(id);
                impInst = impInst + " | " + impInstNext;
                implementations.set(id, impInst);
                implementations.remove(id-1);
                impIter = implementations.listIterator(id-1);
            }
        }
    }

    private void categoriseLudemes(StringBuilder grammar) {
        System.out.println(CATEGORY_START+"(.+?)"+CATEGORY_END);
        Pattern patternCategories = Pattern.compile("// (.+?)\\n");
        Matcher matcherCategories = patternCategories.matcher(grammar.toString());
        System.out.println(matcherCategories.group());

        while (!grammar.isEmpty())
        {
            // FIXME: refactor this
            int categoryStart = Math.max(grammar.indexOf(CATEGORY_START), 0);
            grammar.delete(0, categoryStart);
            int categoryEnd = grammar.indexOf(CATEGORY_END) == 0 ? grammar.length() : grammar.indexOf(CATEGORY_END);
            String categoryName = grammar.substring(CATEGORY_START.length(), categoryEnd);
            grammar.delete(0, categoryEnd);

            int ludemeEnd = grammar.indexOf(LUDEME_END) < 0 ? (grammar.length()) : grammar.indexOf(LUDEME_END);
            String ludemes = grammar.substring(0, ludemeEnd);
            CategoryMap.put(categoryName, new ArrayList<String>() {{ add(ludemes); } });
        }
    }

    @Override
    public String toString()
    {
        return "SyntaxBase{" +
                "CategoryMap=" + CategoryMap +
                ", SyntaxMap=" + SyntaxMap +
                '}';
    }

}
