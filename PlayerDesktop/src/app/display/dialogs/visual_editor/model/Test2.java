package app.display.dialogs.visual_editor.model;

import grammar.Grammar;
import main.grammar.Clause;
import main.grammar.ClauseArg;
import main.grammar.Symbol;

import java.util.ArrayList;
import java.util.List;

public class Test2 {

    public static void main(String[] args) {
        Symbol s = Grammar.grammar().symbolsWithPartialKeyword("tile").get(0);
        Clause c = s.rule().rhs().get(0);


        String st = "";
        st += "("+c.symbol().token();
        if(c.args() != null)
        {
            int currentGroup = 0;
            for(ClauseArg cArg : c.args())
            {
                if(cArg.orGroup() > 0)
                {
                    if(currentGroup != cArg.orGroup())
                    {
                        st += "(";
                        currentGroup = cArg.orGroup();
                    }
                    else
                    {
                        st += "|";
                    }
                }
                if(cArg.orGroup() == 0 && currentGroup > 0)
                {
                    st += ")";
                    currentGroup = 0;
                }
                st += " " + cArg.toString() + " ";
            }
        }
        st += ")";
        st = st.replaceAll("\\s+", " ");
        st = st.replaceAll("\\( ", "(");
        st = st.replaceAll(" \\)", ")");


        System.out.println("Hello World!");
    }

}
