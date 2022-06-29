package app.display.dialogs.visual_editor.recs;

import app.display.dialogs.visual_editor.recs.codecompletion.domain.filehandling.LudiiGameDatabase;
import app.display.dialogs.visual_editor.recs.display.TextEditor;

public class CodeCompletionMain {
    public static void main(String[] args) {
        LudiiGameDatabase db = LudiiGameDatabase.getInstance();
        String gameDescription = db.getDescription(367);
        TextEditor.createInstance(7);
        TextEditor textEditor = TextEditor.getInstance();
    }
}
