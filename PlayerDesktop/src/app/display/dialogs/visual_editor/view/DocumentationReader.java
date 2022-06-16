package app.display.dialogs.visual_editor.view;

import grammar.Grammar;
import main.Status;
import main.grammar.Symbol;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Provides documentation/help for a symbol
 * @author Filipp Dokienko
 */

public class DocumentationReader
{
    private static DocumentationReader instance = null;
    private static File helpFile = null;
    private static HashMap<Symbol, HelpInformation> documentation = new HashMap<>();
    private Grammar grammar = Grammar.grammar();

    public static DocumentationReader instance()
    {
        if (instance == null) {
            instance = new DocumentationReader();
        }
        return instance;
    }

    DocumentationReader()
    {
        helpFile = new File(System.getProperty("user.dir")+"\\Common\\res\\help\\EditorHelp.txt"); // TODO: not absolute path
        for(Symbol symbol : grammar.symbols())
        {
            try {
                documentation.put(symbol, readHelpFile(symbol));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static HelpInformation readHelpFile(Symbol symbol) throws IOException
    {
        // Search for TYPE: symbol.grammarLabel() in help file
        String type = symbol.path();
        BufferedReader reader = new BufferedReader(new FileReader(helpFile));
        String line;

        HelpInformation help = null;
        String usage = "";
        String description = "";
        HashMap<String, String> parameter_description = new HashMap<>();
        List<String> example = new ArrayList<>();

        boolean found = false;
        String lastLine = null;
        while ((line = reader.readLine()) != null)
        {

            if(line.contains("TYPE:") && line.contains(type)) // start of help
            {
                found = true;
            }

            if(found)
            {
                if(line.contains("TYPE JAVADOC"))
                {
                    description = line.substring(line.indexOf(":") + 1);
                }
                else if(line.contains("PARAM JAVADOC") || line.contains("CONST JAVADOC"))
                {
                    String trimmedLine = line.substring(line.indexOf(":") + 1);
                    String param = trimmedLine.substring(0, trimmedLine.lastIndexOf(":"));
                    String description_c = trimmedLine.substring(trimmedLine.lastIndexOf(":") + 1);
                    parameter_description.put(param, description_c);
                }
                else if(line.contains("EXAMPLE"))
                {
                    example.add(line.substring(line.indexOf(":") + 1));
                }
            }
            if(lastLine != null && lastLine.contains("NEW CTOR"))
            {
                usage = line.substring(lastLine.indexOf(":") + 1);
            }


            if(found && line.contains("TYPE:") && !line.contains(type)) // end of help
            {
                return new HelpInformation(description, usage, parameter_description, example.toArray(new String[0]));
            }
            lastLine = line;
        }
        return null;
    }

    public HashMap<Symbol, HelpInformation> documentation()
    {
        return documentation;
    }

    public HelpInformation help(Symbol symbol)
    {
        return documentation.get(symbol);
    }

}
