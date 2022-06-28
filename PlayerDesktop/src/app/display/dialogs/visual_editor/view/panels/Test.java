package app.display.dialogs.visual_editor.view.panels;

import app.display.dialogs.visual_editor.view.DocumentationReader;
import app.display.dialogs.visual_editor.view.HelpInformation;
import grammar.ClassEnumerator;
import grammar.Grammar;
import main.grammar.Clause;
import main.grammar.ClauseArg;
import main.grammar.Symbol;
import main.grammar.ebnf.EBNFClause;
import main.grammar.ebnf.EBNFClauseArg;
import main.grammar.ebnf.EBNFRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Test {
    public static void main(String[] args) throws ClassNotFoundException {
        Grammar grammar = Grammar.grammar();
        System.out.println(");");

        for(EBNFRule r : grammar.ebnf().rules().values())
        {
            for(EBNFClause c : r.rhs())
            {
                if(c.args() == null) continue;
                for(EBNFClauseArg a : c.args())
                {
                    if(a.parameterName() != null)
                        System.out.println(a.token());
                }
            }
        }


        for(Symbol s : grammar.symbols())
        {
            if(s.ludemeType().equals(Symbol.LudemeType.Primitive)) System.out.println("Primitive: " + s.grammarLabel());
        }

        for(Symbol s : grammar.symbols())
        {
            if(s.ludemeType().equals(Symbol.LudemeType.Predefined)) System.out.println("Predefined: " + s.grammarLabel());
        }

        List<String> packageNames = new ArrayList<>();
        for(Symbol s : grammar.symbols())
        {
            if(s.cls().getPackage() == null) continue;
            String pn = s.cls().getPackage().getName();
            if(!packageNames.contains(pn))
                packageNames.add(pn);
        }

        for(Clause c : grammar.symbolsWithPartialKeyword("+").get(2).rule().rhs().get(1).args().get(0).symbol().returnType().rule().rhs())
        {

                if(c.symbol().ludemeType() == Symbol.LudemeType.Primitive)
                    System.out.println("2: Primitive: " + c.symbol().grammarLabel());
                if(c.symbol().ludemeType() == Symbol.LudemeType.Predefined)
                    System.out.println("2: Predefined: " + c.symbol().grammarLabel());
        }

        System.out.println();

        List<Class<?>> gameClasses = ClassEnumerator.getClassesForPackage(Class.forName("game.Game").getPackage());
        HashMap<Symbol, HelpInformation> documentation = DocumentationReader.instance().documentation();
        System.out.println("Hello");

    }
}
