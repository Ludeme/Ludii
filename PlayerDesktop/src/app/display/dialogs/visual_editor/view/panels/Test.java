package app.display.dialogs.visual_editor.view.panels;

import app.display.dialogs.visual_editor.view.DocumentationReader;
import app.display.dialogs.visual_editor.view.HelpInformation;
import app.utils.GameSetup;
import approaches.random.Generator;
import compiler.Compiler;
import game.Game;
import grammar.ClassEnumerator;
import grammar.Grammar;
import main.FileHandling;
import main.StringRoutines;
import main.grammar.*;
import main.grammar.ebnf.EBNF;
import main.grammar.ebnf.EBNFClause;
import main.grammar.ebnf.EBNFClauseArg;
import main.grammar.ebnf.EBNFRule;
import main.options.UserSelections;
import parser.Parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Test {

    public static void main(String[] args) throws ClassNotFoundException {
        System.exit(1);

        for(EBNFRule r : Grammar.grammar().ebnf().rules().values())
        {
            System.out.println(r);
        }

        System.exit(2);

        Grammar grammar = Grammar.grammar();

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
