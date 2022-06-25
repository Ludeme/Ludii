package app.display.dialogs.visual_editor.model;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;
import app.display.dialogs.visual_editor.view.MainFrame;
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

        constructGraph(callTree.args().get(0), 0, null);

        EditorPanel ep = new EditorPanel(4000, 4000);
        JFrame jf = new MainFrame(ep);
        ep.drawGraph(GRAPH);

    }

    /**
     * Prints out all the necessary information to construct ludeme graph
     * @param c
     * @param d
     */
    private static void constructGraph(Call c, int d, LudemeNode pLn)
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
                    constructGraph(call, d+1, null);
                }
                break;
            case Terminal:
                // Return lhs and the object
                // TODO: Not Java 8 (do not know how to replace) System.out.println("    ".repeat(d)+"LHS: "+c.symbol().returnType().toString()+" RHS: "+c.object().toString());
                break;
            case Class:
                // TODO: debug this!
                String lhs = c.symbol().rule().lhs().returnType().token();
                Symbol nodeSymbol = c.symbol();

                // System.out.println("    ".repeat(d)+"Return type: "+c.symbol().returnType().toString()+" LHS: "+lhs+" RHS: "+rhs);

                // Initialize ludeme node with found Symbol and correct rhs
                LudemeNode ln = new LudemeNode(nodeSymbol, 0, 0);
                Handler.addNode(GRAPH, ln);
                for (int i = 0; i < c.args().size(); i++)
                {
                        // [ TODO : providedInputs changed ] ln.setProvidedInput(i, c.object());
                }

                // Output correct lhs + rhs
                // Apply method for the arguments of c
                for (int i = 0; i < cArgs.size(); i++) {
                    Call call = cArgs.get(i);
                    if (!call.type().equals(Call.CallType.Class)) continue;
                    // [TODO: Changed LudemeNode ] Handler.updateInput(GRAPH, ln, i, call.object());

                    constructGraph(call, d+1, ln);
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

}
