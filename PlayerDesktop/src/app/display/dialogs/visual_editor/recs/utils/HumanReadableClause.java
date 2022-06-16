package app.display.dialogs.visual_editor.recs.utils;

import main.grammar.Clause;

public class HumanReadableClause {
    public static String makeReadable(Clause clause) {
        String clauseString = clause.toString();
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
