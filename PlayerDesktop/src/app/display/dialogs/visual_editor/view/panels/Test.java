package app.display.dialogs.visual_editor.view.panels;

import app.display.dialogs.visual_editor.view.DocumentationReader;
import app.display.dialogs.visual_editor.view.HelpInformation;
import grammar.ClassEnumerator;
import grammar.Grammar;
import main.grammar.Symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Test {
    public static void main(String[] args) throws ClassNotFoundException {
        Grammar grammar = Grammar.grammar();
        System.out.println(");");

        List<String> packageNames = new ArrayList<>();
        for(Symbol s : grammar.symbols())
        {
            if(s.cls().getPackage() == null) continue;
            String pn = s.cls().getPackage().getName();
            if(!packageNames.contains(pn))
                packageNames.add(pn);
        }

        System.out.println();

        List<Class<?>> gameClasses = ClassEnumerator.getClassesForPackage(Class.forName("game.Game").getPackage());
        HashMap<Symbol, HelpInformation> documentation = DocumentationReader.instance().documentation();
        System.out.println("Hello");

    }
}
