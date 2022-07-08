package app.display.dialogs.visual_editor.documentation;

import grammar.Grammar;
import main.grammar.Clause;
import main.grammar.ClauseArg;
import main.grammar.Symbol;
import main.grammar.ebnf.EBNFClause;

import java.io.*;
import java.util.*;

/**
 * Reads EditorHelp.txt and stores help information for each symbol
 * @author Filipp Dokienko
 */

public class DocumentationReader
{
    private static DocumentationReader instance = null;
    private static File helpFile = null;
    private static final HashMap<Symbol, HelpInformation> documentation = new HashMap<>();
    private static final Grammar grammar = Grammar.grammar();

    private static final HashMap<Clause, EBNFClause> clauseMap = new HashMap<>();


    public static void main(String[] args)
    {
        helpFile = new File(System.getProperty("user.dir")+"\\Common\\res\\help\\EditorHelp.txt");
        try
        {
            readDoc();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void readDoc() throws IOException
    {
        // read helpFile by line
        BufferedReader reader = new BufferedReader(new FileReader(helpFile));

        HelpInformation currentHelpInfo = null;
        Clause currentClause = null;

        String line;
        while ((line = reader.readLine()) != null)
        {
            if(line.startsWith("TYPE:"))
            {
                String symbolString = line.substring(6);
                Symbol symbol = findSymbol(symbolString);
                currentClause = null;

                currentHelpInfo = new HelpInformation(symbol);
                documentation.put(symbol, currentHelpInfo);
            }
            else if(line.startsWith("TYPE JAVADOC:"))
            {
                String descriptionString = line.substring(14);
                currentHelpInfo.setDescription(descriptionString);
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
            else if(line.startsWith("REMARKS:"))
            {
                String remarkString = line.substring(9);
                currentHelpInfo.setRemark(remarkString);
            }
        }

    }


    private static Symbol findSymbol(String name)
    {
        for(Symbol symbol : grammar.symbols())
            if(symbol.path().equals(name))
                return symbol;
        return null;
    }

    private static Clause findClause(Symbol s, String string)
    {

        EBNFClause ec = null;
        try
        {
            ec = new EBNFClause(string.replaceAll("\\s+", " "));
        }
        catch(Exception ignored){}

        String string2 = string;

        for(Clause clause : s.rule().rhs())
        {
            if(clause.args() == null) continue;
            String clauseString = clause.toString();
            for(ClauseArg arg : clause.args())
            {
                string2 = string2.replace("<"+arg.symbol().token()+">", "<"+arg.symbol().grammarLabel()+">");
            }
            clauseString = clauseString.replaceAll("<int>", "int");
            clauseString = clauseString.replaceAll("<ints>", "ints");
            string2 = string2.replaceAll("<int>", "int");
            string2 = string2.replaceAll("<ints>", "ints");
            string2 = string2.replaceAll("\\s+", " ");
            if(clauseString.equalsIgnoreCase(string2))
            {
                clauseMap.put(clause, ec);
                return clause;
            }
            if(clauseToString(clause).equalsIgnoreCase(string2))
            {
                clauseMap.put(clause, ec);
                return clause;
            }
        }

        for(Clause clause : s.rule().rhs())
        {
            string2 = string2.replaceAll("<region>","<sites>");
            string2 = string2.replaceAll("<rangefunction>","<range>");
            string2 = string2.replaceAll("<intarrayfunction>","ints");
            string2 = string2.replaceAll("<dimfunction>","<dim>");
            if(clauseToString(clause).equalsIgnoreCase(string2))
            {
                clauseMap.put(clause, ec);
                return clause;
            }
        }

        System.out.println("Could not find clause: "+string);

        return null;
    }

    private static String clauseToString(Clause c)
    {
        int currentGroup = 0;
        int lastGroup = 0;
        StringBuilder st = new StringBuilder();
        st.append("(").append(c.symbol().token()).append(" ");
        if(c.args() != null)
            for(ClauseArg cArg : c.args())
            {
                if(cArg.orGroup() > 0)
                {
                    if(currentGroup != cArg.orGroup())
                    {
                        if(currentGroup > 0)
                            st.append(") ");
                        st.append("(");
                        currentGroup = cArg.orGroup();
                    }
                    else
                        st.append("|");
                }
                if(cArg.orGroup() == 0 && currentGroup > 0)
                {
                    st.append(")");
                    currentGroup = 0;
                }
                st.append(" ").append(cArg).append(" ");
                lastGroup = cArg.orGroup();
            }
        if(lastGroup > 0)
            st.append(")");
        st.append(")");
        st = new StringBuilder(st.toString().replaceAll("\\s+", " "));
        st = new StringBuilder(st.toString().replaceAll("\\( ", "("));
        st = new StringBuilder(st.toString().replaceAll(" \\)", ")"));
        st = new StringBuilder(st.toString().replaceAll("<int>", "int"));
        st = new StringBuilder(st.toString().replaceAll("<ints>", "ints"));
        return st.toString();
    }

    private static ClauseArg findClauseArg(Clause c, String string)
    {
        if(c==null)
            return null;

        List<String> strings = new ArrayList<>();

        String string2 = string;

        List<ClauseArg> cas = c.args();
        for(ClauseArg ca : cas)
        {
            String caString = ca.toString();
            string2 = string2.replace( "<"+ca.symbol().token()+">", "<"+ca.symbol().grammarLabel()+">");
            caString = caString.replaceAll("<int>", "int");
            caString = caString.replaceAll("<ints>", "ints");
            string2 = string2.replaceAll("<int>","int");
            string2 = string2.replaceAll("<ints>","ints");
            string2 = string2.toLowerCase();
            caString = caString.toLowerCase();
            strings.add(caString);
            if(caString.equals(string2))
                return ca;
        }

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
                    untilIndex+=2;
                untilIndex+=1;

                splitlist.set(fromIndex, splitlist.get(fromIndex).substring(1));
                splitlist.set(untilIndex, splitlist.get(untilIndex).substring(0, splitlist.get(untilIndex).length()-1));
                char closeType = ' ';
                if(type == '[')
                    closeType = ']';
                else if(type == '{')
                    closeType = '}';
                if(type == '[' || type == '{')
                {
                    for(int i = fromIndex; i<=untilIndex; i++)
                    {
                        if(splitlist.get(i).equals("|"))
                            continue;
                        splitlist.set(i, type+splitlist.get(i)+closeType);
                    }
                }
                for(int i = fromIndex; i<=untilIndex; i++)
                    if(splitlist.get(i).equals("|"))
                        splitlist.set(i, "[REMOVE]");
                // remove all [REMOVE] from splitlist
                while(splitlist.contains("[REMOVE]"))
                    splitlist.remove("[REMOVE]");
            }
            split = splitlist.toArray(new String[0]);
        }

        int index = Arrays.asList(split).indexOf(string);
        if(index <= 0 || index > c.args().size())
            return null;
        return c.args().get(index-1);
    }




    public static DocumentationReader instance()
    {
        if (instance == null)
            instance = new DocumentationReader();
        return instance;
    }

    DocumentationReader()
    {
        helpFile = new File("..\\Common\\res\\help\\EditorHelp.txt");
        try
        {
            readDoc();
        }
        catch (IOException e)
        {
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
