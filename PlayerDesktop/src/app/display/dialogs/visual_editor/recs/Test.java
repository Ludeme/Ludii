package app.display.dialogs.visual_editor.recs;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.filehandling.LudiiGameDatabase;
import javax.swing.text.BadLocationException;

public class Test {
    public static void main(String[] args) throws BadLocationException {
        LudiiGameDatabase db = LudiiGameDatabase.getInstance();
        String gameDescription = db.getDescription(0);
        /*try {
            //reading in game description
            gameDescription = FileUtils.getContents(new File("PlayerDesktop/src/app/display/dialogs/visual_editor/resources/recs/FilipsGame.lud"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/
        System.out.println(gameDescription);
        System.out.println(Converter.toConstructor(gameDescription));
    }
}
