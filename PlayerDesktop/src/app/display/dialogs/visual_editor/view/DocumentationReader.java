package app.display.dialogs.visual_editor.view;

import grammar.Grammar;
import main.Status;
import main.grammar.Clause;
import main.grammar.ClauseArg;
import main.grammar.Symbol;
import main.grammar.ebnf.EBNF;
import main.grammar.ebnf.EBNFClause;
import main.grammar.ebnf.EBNFClauseArg;

import java.io.*;
import java.util.*;

/**
 * Provides documentation/help for a symbol
 * @author Filipp Dokienko
 */

public class DocumentationReader
{
    private static DocumentationReader instance = null;
    private static File helpFile = null;
    private static HashMap<Symbol, HelpInformation> documentation = new HashMap<>();
    private static Grammar grammar = Grammar.grammar();
    private static EBNF ebnf = grammar.ebnf();

    private static HashMap<Clause, EBNFClause> clauseMap = new HashMap<>();


    public static void main(String[] args)
    {
        helpFile = new File(System.getProperty("user.dir")+"\\Common\\res\\help\\EditorHelp.txt"); // TODO: not absolute path
        try {
            readDoc();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println();
    }

    public static void readDoc() throws IOException {
        // read helpFile by line
        BufferedReader reader = new BufferedReader(new FileReader(helpFile));

        HelpInformation currentHelpInfo = null;
        Clause currentClause = null;
        List<Symbol> subclasses = new ArrayList<>();

        String line;
        while ((line = reader.readLine()) != null)
        {
            if(line.startsWith("TYPE:"))
            {
                String symbolString = line.substring(6);
                Symbol symbol = findSymbol(symbolString);
                currentClause = null;

                if(currentHelpInfo != null && !subclasses.isEmpty())
                {
                    for(Symbol s : subclasses)
                    {
                        HelpInformation hi = new HelpInformation(s);
                        hi.setDescription(currentHelpInfo.description());
                        hi.setRemark(currentHelpInfo.remark());
                        hi.setCtor(currentHelpInfo.ctors());
                        hi.setExamples(currentHelpInfo.examples());
                        hi.setParameters(currentHelpInfo.parameters());
                        documentation.put(s, hi);
                    }
                    subclasses.clear();
                }
                currentHelpInfo = new HelpInformation(symbol);
                documentation.put(symbol, currentHelpInfo);
            }
            else if(line.startsWith("TYPE JAVADOC:"))
            {
                String descriptionString = line.substring(14);
                currentHelpInfo.setDescription(descriptionString);
            }
            else if(line.startsWith("SUBCLASS:"))
            {
                if(true) continue;
                subclasses.add(findSymbol(line.substring(10)));
            }
            else if(line.startsWith("NEW CTOR"))
            {
                // read next line
                line = reader.readLine();
                currentClause = findClause(currentHelpInfo.symbol(), line);
                currentHelpInfo.addCtor(currentClause, line);
            }
            else if(line.startsWith("PARAM JAVADOC:"))
            {
                String fullLine = line.substring(15);
                String parameterStringName = fullLine.substring(0, fullLine.lastIndexOf(":"));
                ClauseArg arg = findClauseArg(currentClause, parameterStringName);
                String parameterString = fullLine.substring(fullLine.lastIndexOf(":")+1);
                currentHelpInfo.addParameter(arg, parameterString);
            }
            else if(line.startsWith("EXAMPLE:"))
            {
                String exampleString = line.substring(10);
                currentHelpInfo.addExample(currentClause, exampleString);
            }
            else if(line.startsWith("REMARK:"))
            {
                String remarkString = line.substring(9);
                currentHelpInfo.setRemark(remarkString);
            }
        }

        if(currentHelpInfo != null && !subclasses.isEmpty())
        {
            for(Symbol s : subclasses)
            {
                HelpInformation hi = new HelpInformation(s);
                hi.setDescription(currentHelpInfo.description());
                hi.setRemark(currentHelpInfo.remark());
                hi.setCtor(currentHelpInfo.ctors());
                hi.setExamples(currentHelpInfo.examples());
                hi.setParameters(currentHelpInfo.parameters());
                documentation.put(s, hi);
            }
        }

    }


    private static Symbol findSymbol(String name)
    {
        for(Symbol symbol : grammar.symbols())
        {
            if(symbol.path().equals(name))
            {
                return symbol;
            }
        }
        return null;
    }

    private static Clause findClause(Symbol s, String string)
    {

        // get ebnfclause
        EBNFClause ec;
        String temp = "";
        try {
            ec = new EBNFClause(string);
        }
        catch (Exception e)
        {
            System.out.println("Couldnt parse clause: "+string);
            return null;
        }
        // get decomposed clause string
        String dstring = ec.toString();
        for(EBNFClauseArg a : ec.args())
        {
            if(a.parameterName() == null) continue;
            dstring = dstring.replaceAll(a.parameterName()+":","");
        }

        // replace int with <int>, ints with <ints>
        if(dstring.contains(" ")) {
            String dstring1 = dstring.substring(0, dstring.indexOf(" "));
            String dstring2 = dstring.substring(dstring.indexOf(" ") + 1);
            dstring2 = dstring2.replaceAll(" ints", " <ints>");
            dstring2 = dstring2.replaceAll("\\[ints", "[<ints>");
            dstring2 = dstring2.replaceAll("\\(ints", "(<ints>");
            dstring2 = dstring2.replaceAll("\\{ints", "{<ints>");

            dstring2 = dstring2.replaceAll(" int\\]", " <int>]");
            dstring2 = dstring2.replaceAll(" int\\)", " <int>)");
            dstring2 = dstring2.replaceAll(" int\\}", " <int>}");

            dstring2 = dstring2.replaceAll("\\[int ", "[<int> ");
            dstring2 = dstring2.replaceAll("\\[int]", "[<int>]");

            dstring2 = dstring2.replaceAll("\\{int ", "{<int> ");
            dstring2 = dstring2.replaceAll("\\{int}", "{<int>}");
            dstring2 = dstring2.replaceAll(" int\\)", "<int>)");

            while(dstring2.contains(" int ")) dstring2 = dstring2.replace(" int ", " <int> ");

            if((dstring2.contains(" ") && dstring2.substring(0, dstring2.indexOf(" ")).equals("int")) || dstring2.equals("int")) {
                dstring2 = dstring2.replaceFirst("int", "<int>");
            }

            //while(dstring2.contains(" int ")) dstring2 = dstring2.replace(" int ", " <int> ");

            dstring = dstring1 + " " + dstring2;
        }

        for(Clause c : s.rule().rhs())
        {
            if(c.args() != null) {

                if (c.toString().equals(dstring)) {
                    clauseMap.put(c, ec);
                    return c;
                }

                if(dstring.contains(" ")) {
                    String dstring1 = dstring.substring(0, dstring.indexOf(" "));
                    String dstring2 = dstring.substring(dstring.indexOf(" ") + 1);
                    for (ClauseArg a : c.args()) {
                        dstring2 = dstring2.replaceAll("<" + a.symbol().token() + ">", "<" + a.symbol().grammarLabel() + ">");
                    }
                    dstring = dstring1 + " " + dstring2;
                }
                temp = dstring;
                if (c.toString().equals(dstring)) {
                    clauseMap.put(c, ec);
                    return c;
                }
            }
        }

        System.out.println("Couldnt match clause: "+string);
        return null;
    }

    private static ClauseArg findClauseArg(Clause c, String string)
    {
        if(c==null) return null;

        EBNFClause ec = clauseMap.get(c);

        if(string.contains(":"))
        {
            for(EBNFClauseArg arg : ec.args())
            {
                if(arg.parameterName() == null) continue;
                if(string.contains(arg.parameterName()))
                {
                    string = string.replaceAll(arg.parameterName()+":","");
                    break;
                }
            }
        }

        String[] split = ec.toString().substring(1, ec.toString().length()-1).split(" ");
        int index = Arrays.asList(split).indexOf(string);
        if(index <= 0)
            return null;
        return c.args().get(index-1);
    }




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
        try {
            readDoc();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println();
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
