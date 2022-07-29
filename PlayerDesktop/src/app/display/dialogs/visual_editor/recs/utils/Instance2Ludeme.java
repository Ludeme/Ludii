package app.display.dialogs.visual_editor.recs.utils;

import app.display.dialogs.visual_editor.recs.codecompletion.Ludeme;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.model.Instance;

import java.util.ArrayList;
import java.util.List;

public class Instance2Ludeme {
    /**
     * This method converts a Ngram instance into a ludeme object
     * @param instance
     */
    public static Ludeme instance2ludeme(Instance instance) {
        return new Ludeme(instance.getPrediction());
    }

    public static List<Ludeme> foreachInstance2ludeme(List<Instance> instances) {
        List<Ludeme> ludemes = new ArrayList<>();
        for(Instance instance : instances) {
            if(instance != null) {
                ludemes.add(instance2ludeme(instance));
            }
        }
        return ludemes;
    }
}
