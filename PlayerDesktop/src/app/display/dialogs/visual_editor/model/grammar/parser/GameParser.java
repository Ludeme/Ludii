package app.display.dialogs.visual_editor.model.grammar.parser;


import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.grammar.Ludeme;
import app.display.dialogs.visual_editor.recs.utils.FileUtils;
import compiler.Arg;
import compiler.ArgClass;
import compiler.exceptions.CompilerErrorWithMessageException;
import main.Constants;
import main.grammar.*;
import main.options.UserSelections;
import other.GameLoader;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class GameParser
{
    private static Parser p = new Parser();
    private static List<Ludeme> L = p.getLudemes();
    private static List<Grammar> G = p.getGRAMMAR();

    public static void main(String[] args) throws FileNotFoundException {
        // Playing around with Ludii methods needed for text-to-graph parser
        Description test_desc = new Description(Constants.BASIC_GAME_DESCRIPTION);
        parser.Parser.expandAndParse(test_desc, new UserSelections(new ArrayList<>()),new Report(),false);
        Token tokenTree_test = new Token(test_desc.expanded(), new Report());
        grammar.Grammar gm = grammar.Grammar.grammar();
        final ArgClass rootClass = (ArgClass) Arg.createFromToken(grammar.Grammar.grammar(), tokenTree_test);
        rootClass.matchSymbols(gm, new Report());

        // Attempt to compile the game
        Class<?> clsRoot = null;
        try
        {
            clsRoot = Class.forName("game.Game");
        } catch (final ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        // Create call tree with dummy root
        Call callTree = new Call(Call.CallType.Null);
        final Map<String, Boolean> hasCompiled = new HashMap<>();
        rootClass.compile(clsRoot, (-1), new Report(), callTree, hasCompiled);

        constructGraph(callTree.args().get(0), 0);


        DescriptionGraph graph = new DescriptionGraph();
        LudemeNode r = new LudemeNode(getLudemeForEditor("game"), 0, 0);
        Handler.addNode(graph, r);

        //findConstructors();
    }

    /**
     * Prints out all the necessary information to construct ludeme graph
     * @param c
     * @param d
     */
    private static void constructGraph(Call c, int d)
    {
        List<Call> cArgs = c.args();
        int matched = 0;
        int counter = 0;
        switch (c.type())
        {
            case Array:
                // Apply method for the arguments of c
                for (Call call : cArgs)
                {
                    constructGraph(call, d+1);
                }
                break;
            case Terminal:
                // Return lhs and the object
                System.out.println("    ".repeat(d)+"LHS: "+c.symbol().returnType().toString()+" RHS: "+c.object().toString());
                break;
            case Class:
                // TODO: debug this!
                String lhs = c.symbol().rule().lhs().returnType().token();
                List<Clause> rhsList = c.symbol().rule().rhs();
                String rhs = null;
                for (Clause clause : rhsList) {
                    if (clause.args() != null && c.args().size() == clause.args().size()) {
                        List<ClauseArg> iRhs = clause.args();
                        for (int j = 0; j < cArgs.size(); j++) {
                            Symbol s = cArgs.get(j).symbol();
                            if (s != null)
                            {
                                if (s.returnType().token().equals(
                                        iRhs.get(j).symbol().returnType().token())) {
                                    counter++;
                                }
                            }

                        }

                        if (counter > matched || rhsList.size() == 1)
                        {
                            matched = counter;
                            counter = 0;
                            rhs = clause.toString();
                        }
                    }
                }

                // Output correct lhs + rhs
                System.out.println("    ".repeat(d)+"Return type: "+c.symbol().returnType().toString()+" LHS: "+lhs+" RHS: "+rhs);
                // Apply method for the arguments of c
                for (Call call : cArgs)
                {
                    constructGraph(call, d+1);
                }
                break;
            default:
                break;
        }
    }

    private static StringBuilder descriptionToString(String gamePath)
    {
        final StringBuilder sb = new StringBuilder();

        String path = gamePath.replaceAll(Pattern.quote("\\"), "/");
        path = path.substring(path.indexOf("/lud/"));

        InputStream in = GameLoader.class.getResourceAsStream(path);
        try (final BufferedReader rdr = new BufferedReader(new InputStreamReader(in, "UTF-8")))
        {
            String line;
            while ((line = rdr.readLine()) != null)
                sb.append(line + "\n");
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        return sb;
    }

    private static boolean findLudemeOfTerminal(String token)
    {
        for(Grammar g : G)
            if(g.constructors.contains(token))
            {
                System.out.println(g.NAME);
                //return true;
            };
        return false;
    }

    private static boolean findLudemeOfConstructor(List<String> inputs)
    {
        for(Grammar g : G)
        {
            for (String c : g.constructors)
            {
                List<String> c0 = Arrays.asList(c.split("(<)|(>)|( )"));
                if (g.NAME.equals("booleans.is.is")) System.out.println(c0.toString());
                if (c0.containsAll(inputs)) {
                    System.out.println(g.NAME);
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean findConstructors()
    {
        for(Grammar g : G)
        {
            try
            {
                System.out.println(g.NAME + " " + g.constructors.get(0));
            }
            catch (NullPointerException e)
            {
                System.out.println(g.NAME + " !!!!!!!!!");
            }
        }

        return false;
    }

    private static Ludeme getLudemeForEditor(String keyword)
    {
        for(Ludeme l : p.getLudemes())
            if(l.getName().equals(keyword)) return l;
        return null;
    }

}