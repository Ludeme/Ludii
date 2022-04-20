package app.display.dialogs.visual_editor.LayoutManagement.VisualEditor;

import main.StringRoutines;

/**
 * Class provides helper method for the GrammarModel parser; partial reuse of StringRoutines method(s)
 * @author nic0gin
 */
public class GrammarModelUtils
{

    public final static char[][] grammarBrackets =
            {
                    { '(', ')' },
                    { '{', '}' },
                    { '[', ']' }
            };

    public static boolean checkBrackets(final String str)
    {
        for (char[] brc : grammarBrackets)
        {
            int numOpen  = StringRoutines.numChar(str, brc[0]);
            int numClose = StringRoutines.numChar(str, brc[1]);

            if (numOpen != numClose) return false;
        }

        return true;
    }
}
