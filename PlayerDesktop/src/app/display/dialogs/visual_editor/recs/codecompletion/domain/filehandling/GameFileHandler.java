package app.display.dialogs.visual_editor.recs.codecompletion.domain.filehandling;

import app.display.dialogs.visual_editor.recs.utils.FileUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class GameFileHandler {
    public static void writeGame(String gameDescription, String location) {
        FileWriter fw = FileUtils.writeFile(location);
        try {
            fw.write(gameDescription);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readGame(String location) {
        String gameDescription = "";
        //reads line by line
        Scanner sc = FileUtils.readFile(location);
        while (sc.hasNext()) {
            String nextLine = sc.nextLine();
            gameDescription += "\n"+nextLine;
        }
        sc.close();

        return gameDescription;
    }
}
