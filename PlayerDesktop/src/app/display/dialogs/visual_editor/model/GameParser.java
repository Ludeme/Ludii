package app.display.dialogs.visual_editor.model;

import app.display.dialogs.visual_editor.LayoutManagement.NodePlacementRoutines;
import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;
import app.display.dialogs.visual_editor.view.MainFrame;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import app.display.dialogs.visual_editor.view.panels.editor.EditorPanel;
import compiler.Arg;
import compiler.ArgClass;
import grammar.Grammar;
import main.Constants;
import main.grammar.*;
import main.options.UserSelections;
import other.GameLoader;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.regex.Pattern;

public class GameParser
{

    private static DescriptionGraph GRAPH = new DescriptionGraph();

    public static void main(String[] args) throws FileNotFoundException {
        // Playing around with Ludii methods needed for text-to-graph parser
        Description test_desc = new Description(Constants.BASIC_GAME_DESCRIPTION);

        parser.Parser.expandAndParse(test_desc, new UserSelections(new ArrayList<>()),new Report(),false);
        Token tokenTree_test = new Token(test_desc.expanded(), new Report());
        grammar.Grammar gm = grammar.Grammar.grammar();
        final ArgClass rootClass = (ArgClass) Arg.createFromToken(grammar.Grammar.grammar(), tokenTree_test);
        assert rootClass != null;
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

        constructGraph(callTree.args().get(0), 0, Handler.editorPanel.graph());

        //EditorPanel ep = new EditorPanel(4000, 4000);
        //JFrame jf = new MainFrame(ep);
        //ep.drawGraph(GRAPH);

    }

    public static void ParseFileToGraph(File file, IGraphPanel graphPanel)
    {
        StringBuilder sb = new StringBuilder();
        try (final BufferedReader rdr = new BufferedReader(new FileReader(file.getAbsolutePath())))
        {
            String line;
            while ((line = rdr.readLine()) != null)
                sb.append(line + "\n");
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }


        // creating a call tree from game description
        Description test_desc = new Description(Constants.BASIC_GAME_DESCRIPTION);
        parser.Parser.expandAndParse(test_desc, new UserSelections(new ArrayList<>()),new Report(),false);
        Token tokenTree_test = new Token(test_desc.expanded(), new Report());
        Grammar gm = grammar.Grammar.grammar();
        final ArgClass rootClass = (ArgClass) Arg.createFromToken(grammar.Grammar.grammar(), tokenTree_test);
        assert rootClass != null;
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


        // constructing a graph from call tree
        constructGraph(callTree.args().get(0), 0, graphPanel.graph());

    }

    /**
     * Prints out all the necessary information to construct ludeme graph
     * @param c parent call
     * @param d depth
     * @param graph graph in operation
     */
    private static Object constructGraph(Call c, int d, DescriptionGraph graph)
    {
        List<Call> cArgs = c.args();
        int matched = 0;
        int counter = 0;
        switch (c.type())
        {
            case Array:
                // Apply method for the arguments of c
                Object[] objects = new Object[cArgs.size()];
                for (int i = 0; i < cArgs.size(); i++)
                {
                    objects[i] = constructGraph(cArgs.get(i), d+1, graph);
                }
                return objects;
            case Terminal:
                return c.object();
            case Class:
                // TODO: debug this! currently the "OR group" is considered as multiple arguments :/
                Symbol ludemeSymbol = c.symbol();
                List<Clause> rhsList = c.symbol().rule().rhs();
                Clause currentClause = null;
                for (Clause clause : rhsList)
                {
                    if (clause.args() != null && c.args().size() == clause.args().size())
                    {
                        List<ClauseArg> iRhs = clause.args();
                        for (int j = 0; j < cArgs.size(); j++)
                        {
                            Symbol s = cArgs.get(j).symbol();
                            if (s != null)
                            {
                                if (s.returnType().token().equals(
                                        iRhs.get(j).symbol().returnType().token()))
                                {
                                    counter++;
                                }
                            }

                        }
                        if (counter > matched || rhsList.size() == 1)
                        {
                            matched = counter;
                            counter = 0;
                            currentClause = clause;
                        }
                    }
                }

                // creating ludeme node from call class
                LudemeNode ln = new LudemeNode(ludemeSymbol, NodePlacementRoutines.DEFAULT_X_POS,
                        NodePlacementRoutines.DEFAULT_Y_POS);
                Handler.addNode(graph, ln);
                // TODO: add edge to graph model
                // setting up current clause constructor
                Handler.updateCurrentClause(graph, ln, currentClause);
                // providing argument to the ludeme node
                for (int i = 0; i < cArgs.size(); i++)
                {
                    Call call = cArgs.get(i);
                    Object input = constructGraph(call, d+1, graph);
                    Handler.updateInput(graph, ln, ln.currentNodeArguments().get(i), input);
                }
                return ln;

            default:
                return null;
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

}
