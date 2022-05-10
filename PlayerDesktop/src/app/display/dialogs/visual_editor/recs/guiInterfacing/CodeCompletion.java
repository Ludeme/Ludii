package app.display.dialogs.visual_editor.recs.guiInterfacing;

import app.display.dialogs.visual_editor.model.grammar.Ludeme;

import java.util.List;

public class CodeCompletion {
    public static List<Ludeme> getRecommendations(String gameDescription, List<Ludeme> possibleLudemes) {
        System.out.println(gameDescription);
        return possibleLudemes;
    }
}
