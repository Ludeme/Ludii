package app.display.dialogs.visual_editor.documentation;

import grammar.Grammar;
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

                String[] split = parameterStringName.split(" ");
                if(split.length > 1) parameterStringName = split[0].substring(0, split[0].length()-1);

                ClauseArg arg = findClauseArg(currentClause, parameterStringName);
                String parameterString = fullLine.substring(parameterStringName.length()+2);
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

        string = string.replaceAll("\\s+", " ");
        // get ebnfclause
        EBNFClause ec;
        String temp = "";
        try {
            ec = new EBNFClause(string.replaceAll("\\s+", " "));
        }
        catch (Exception e)
        {
            System.out.println("Couldnt parse clause: "+string.replaceAll("\\s+", " "));
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
        String[] split = ec.toString().substring(1, ec.toString().length()-1).split(" ");

        if(ec.toString().contains("|"))
        {
            List<String> splitlist = new LinkedList<>(Arrays.asList(split));
            while(splitlist.contains("|"))
            {
                int indexOfBar = splitlist.indexOf("|");
                // element before
                char type = splitlist.get(indexOfBar-1).charAt(0);
                int fromIndex = indexOfBar-1;
                int untilIndex = indexOfBar;
                while(untilIndex+2 < splitlist.size() && splitlist.get(untilIndex+2).equals("|"))
                {
                    untilIndex+=2;
                }
                untilIndex+=1;

                splitlist.set(fromIndex, splitlist.get(fromIndex).substring(1));
                splitlist.set(untilIndex, splitlist.get(untilIndex).substring(0, splitlist.get(untilIndex).length()-1));
                char closeType = ' ';
                if(type == '[') closeType = ']';
                else if(type == '{') closeType = '}';
                if(type == '[' || type == '{')
                {
                    for(int i = fromIndex; i<=untilIndex; i++)
                    {
                        if(splitlist.get(i).equals("|")) continue;
                        splitlist.set(i, type+splitlist.get(i)+closeType);
                    }
                }
                for(int i = fromIndex; i<=untilIndex; i++)
                {
                    if(splitlist.get(i).equals("|")) splitlist.set(i, "[REMOVE]");
                }
                // remove all [REMOVE] from splitlist
                while(splitlist.contains("[REMOVE]"))
                {
                    splitlist.remove("[REMOVE]");
                }
            }
            split = splitlist.toArray(new String[splitlist.size()]);
        }

        int index = Arrays.asList(split).indexOf(string);
        if(index <= 0 || index > c.args().size())
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
