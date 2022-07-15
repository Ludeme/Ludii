package app.display.dialogs.visual_editor.recs;

import app.display.dialogs.visual_editor.recs.codecompletion.domain.filehandling.DocHandler;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.filehandling.ModelLibrary;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.model.NGram;
import app.display.dialogs.visual_editor.recs.validation.controller.ValidationController;

import java.util.List;

public class CodeCompletionValidationMain {

    public static void main(String[] args) {
        ValidationController vc = new ValidationController();
        vc.validate(0.66,5,1000,2);
        /*int maxN = 20;
        int[] NValues = new int[maxN];
        long[] creationDurations = new long[maxN];
        String storageLocation = "PlayerDesktop/src/app/display/dialogs/visual_editor/resources/recs/validation/modelcreation/creation_duration.csv";
        for(int i = 9; i <= maxN; i++) {
            long start = System.nanoTime();
            ModelLibrary.getInstance().getModel(i);
            long finish = System.nanoTime();

            long duration = finish - start;

            creationDurations[i - 2] = duration;
            NValues[i - 2] = i;
            System.out.println("N:"+i+",creation_duration:"+duration+"\n");
            FileWriter fw = FileUtils.writeFile(storageLocation);
            try {
                fw.write("N,creation_duration");
                for(int j = 2; j <= maxN; j++) {
                    fw.write(NValues[j - 2]+","+creationDurations[j - 2]+"\n");
                }
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/





        //ValidationController controller = new ValidationController();
        //controller.validate(2,15);
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
