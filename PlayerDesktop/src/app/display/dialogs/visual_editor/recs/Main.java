import codecompletion.controller.Controller;
import codecompletion.domain.filehandling.LudiiGameDatabase;
import display.TextEditor;

public class Main {
    public static void main(String[] args) {
        LudiiGameDatabase db = LudiiGameDatabase.getInstance();
        String gameDescription = db.getDescription(367);
        TextEditor.createInstance(7);
        TextEditor textEditor = TextEditor.getInstance();
    }
}
