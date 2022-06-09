package app.display.dialogs.visual_editor.recs;

import app.display.dialogs.visual_editor.recs.codecompletion.domain.filehandling.DocHandler;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.filehandling.ModelLibrary;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.model.NGram;
import app.display.dialogs.visual_editor.recs.validation.controller.ValidationController;

import java.util.List;

public class CodeCompletionValidationMain {
    private static final String COMMA = ",";
    public static final String TRAINING = "res/crossvalidation/trainingIDs.txt";
    public static final String VALIDATION = "res/crossvalidation/validationIDs.txt";

    public static void main(String[] args) {
        ValidationController controller = new ValidationController();
        controller.validate(2,15);
    }

    public static void getMultipleModels(int maxN) {
        for(int N = 2; N <= maxN; N++) {
            ModelLibrary.getInstance().getModel(N);
            DocHandler.getInstance().writeDocumentsFile();
        }
    }

    public static void testModelCreation() {
        ModelLibrary lib = ModelLibrary.getInstance();
        NGram model = lib.getModel(5);
        DocHandler.getInstance().close();
    }

    public static void print(Object o) {
        System.out.println(o);
    }

    public static void print(int i) {
        System.out.println(i);
    }

    public static void print(String s) {
        System.out.println(s);
    }

    public static <T> void print(List<T> list) {
        System.out.println(list);
    }
}
