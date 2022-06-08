package app.display.dialogs.visual_editor.recs.guiInterfacing;

import app.display.dialogs.visual_editor.model.grammar.Ludeme;
import app.display.dialogs.visual_editor.model.grammar.parser.Grammar;
import app.display.dialogs.visual_editor.recs.model.Ludii.NGramInstanceLudii;
import app.display.dialogs.visual_editor.recs.utils.Pair;
import main.grammar.Symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CodeCompletion {
    /**
     * These words replace specific values like 3 and "Hello" with TERMINAL_FILLWORD_INTEGER and TERMINAL_FILLWORD_STRING
     */
    public final static String TERMINAL_FILLWORD_STRING = "STRING",
            TERMINAL_FILLWORD_INTEGER = "INTEGER",
            TERMINAL_FILLWORD_FLOAT = "REAL_NUMBER",
            TERMINAL_FILLWORD_BOOLEAN = "BOOLEAN",
            TERMINAL_FILLWORD_COLLECTION = "COLLECTION";

    /**
     * Inserted into a game description to indicate where the code completion should be performed.
     */
    public final String CODE_COMPLETION_MARKER = "--REC--";
    /**
     *
     * @param gameDescription of format: (game ... keyLudeme --REC--
     * @param possibleLudemes
     * @return
     */
    public static List<Symbol> getRecommendations(List<Symbol> allLudemes, String gameDescription, List<Symbol> possibleLudemes) {

        if(true) return possibleLudemes; // TODO: remove after implementation

        System.out.println(gameDescription);
        // List<Pair<NGramInstance, Integer>>:  pre sorted list of recommendations
        // get the NGramInstance: instance = pair.getR()
        // String prediction = instance.getKey(); --> e.g.: "(equipment" for ludeme, "COLLECTION, "INTEGER", "REAL NUMBER", "STRING"

        // Here I (Filipp) assume that the list "sorted_recommendations" is already sorted.
        List<Pair<NGramInstanceLudii, Integer>> sorted_recommendations = new ArrayList<>();
        String[] exampleRecs = {"(equipment","(players",TERMINAL_FILLWORD_STRING,"(rules"};
        for(int i = 0; i < exampleRecs.length; i++) {
            ArrayList<String> a = new ArrayList<>();
            a.add(exampleRecs[i]);
            a.add("Abc");
            Random r = new Random();
            int matchingWords =  r.nextInt(7);
            int multiplicity = r.nextInt(2000);
            sorted_recommendations.add(new Pair<>(new NGramInstanceLudii(a,multiplicity),matchingWords));
        }

        List<Symbol> recommendedLudemes = new ArrayList<>();
        for(Pair p : sorted_recommendations) {
            // get recommendation
            String recommendation = ((NGramInstanceLudii) p.getR()).getKey();
            String ludemeName = "";
            // if recommendation starts with '(' it's a ludeme
            if(recommendation.startsWith("(")) {
                ludemeName = recommendation.substring(1, recommendation.length());
            } else if(recommendation.equals(TERMINAL_FILLWORD_STRING)) {
                ludemeName = "string";
            } else if(recommendation.equals(TERMINAL_FILLWORD_INTEGER)) {
                ludemeName = "int";
            }
            else if(recommendation.equals(TERMINAL_FILLWORD_COLLECTION)) {
                // TODO: SPECIAL CASE, ADD ALL COLLECTIONS TO RECOMMENDATIONS
            }
            // TODO: Add others
            else {
                System.out.println("No ludeme name found for " + recommendation);
            }
            // find this ludeme in available ludemes and add it to recommendation list
            boolean foundLudeme = false;
            for(Symbol ludeme : possibleLudemes) {
                // remove periods from ludeme name // TODO: should be done in Ludeme.java
                String ludemeNameWithoutPeriods = ludeme.name();
                if(ludemeNameWithoutPeriods.contains(".")) {
                    ludemeNameWithoutPeriods = ludemeNameWithoutPeriods.substring(ludemeNameWithoutPeriods.lastIndexOf(".")+1);
                }
                System.out.println("-  " + ludeme.name());
                if(ludeme.name().equalsIgnoreCase(ludemeName)) {
                    System.out.println("Adding " + ludeme.name() + " as recommendation");
                    recommendedLudemes.add(ludeme);
                    foundLudeme = true;
                    break;
                }
            }
            if(!foundLudeme) {
                System.out.println("Could not find ludeme " + ludemeName + " in available ludemes ");
            }
        }

        // not recommended but available ludemes should be added to the list too
        for(Symbol ludeme : possibleLudemes) {
            if(!recommendedLudemes.contains(ludeme)) {
                recommendedLudemes.add(ludeme);
            }
        }

        //
        // get Multiplicity: instance.getMultiplicity()
        // # matching words: int matchingWords = pair.getS()
        // To get Ludeme name: ludeme.getName();
        return recommendedLudemes;
        //return possibleLudemes;
    }
}
