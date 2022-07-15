package app.display.dialogs.visual_editor.recs.utils;

import main.grammar.Clause;
import main.grammar.Symbol;

public class HumanReadable {
    public static String makeReadable(Clause clause) {
        return makeReadable(clause.toString());
    }

    public static String makeReadable(Symbol symbol) {
        String symbolString = symbol.toString(true);
        // remove <>
        symbolString = symbolString.replaceAll("<","");
        symbolString = symbolString.replaceAll(">","");
        //remove package names as in e.g. rules.rules -> rules
        if(symbolString.contains(".")) {
            int lastDotPos = symbolString.lastIndexOf(".");
            symbolString = symbolString.substring(lastDotPos + 1);
        }
        return symbolString;
    }

    public static String makeReadable(String clauseString) {
        // remove <>
        clauseString = clauseString.replaceAll("<","");
        clauseString = clauseString.replaceAll(">","");
        //remove package names as in e.g. rules.rules -> rules
        String[] split = clauseString.split(" ");
        String reassembled = split[0];
        if(split.length > 1) {
            reassembled +=":";
            for(int i = 1; i < split.length; i++) {
                String curParameter = split[i];
                if(curParameter.contains(".")) {
                    int lastDotPos = curParameter.lastIndexOf(".");
                    curParameter = curParameter.substring(lastDotPos + 1,curParameter.length());
                }
                reassembled += " " + curParameter+",";
            }
            clauseString = reassembled.substring(0,reassembled.length()-1);
        }

        return clauseString;
    }
}
