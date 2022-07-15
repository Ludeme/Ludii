package app.display.dialogs.visual_editor.model;

import grammar.Grammar;
import main.grammar.Clause;
import main.grammar.Symbol;

import java.util.ArrayList;
import java.util.List;

public class Test2 {

    public static void main(String[] args) {
        Symbol s = Grammar.grammar().symbolsWithPartialKeyword("string").get(0);


        List<PossibleArgument> possibleArguments = new ArrayList<>();
        for(Clause c : s.rule().rhs()) {
            possibleArguments.add(new PossibleArgument(c));
        }


        System.out.println("Hello World!");
    }

}
