package app.display.dialogs.visual_editor.model.grammar.parser;


import app.display.dialogs.visual_editor.model.grammar.Ludeme;
import app.display.dialogs.visual_editor.recs.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameParser
{
    private static Parser p = new Parser();
    private static List<Ludeme> L = p.getLudemes();
    private static List<Grammar> G = p.getGRAMMAR();

    public static void main(String[] args) throws FileNotFoundException {
        String path  = "src/main/resources/Tic-Tac-Toe.lud";
        File f = new File(path);
        String desc = FileUtils.getContents(f);

        System.out.println(findLudemeOfTerminal("Mover"));
        findLudemeOfConstructor(new ArrayList<String>(){
            {
                add("isPlayerType");
                add("resultType");
            }});
        //findConstructors();
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

}
/*
(game
    "Tic-Tac-Toe"
    (players 2)
    (equipment {
        (board (square 3))
        (piece "Disc" P1)
        (piece "Cross" P2)
    })
    (rules
        (play (move Add (to (sites Empty))))
        (end (if (is Line 3) (result Mover Win)))
    )
)

 */