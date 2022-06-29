package app.display.dialogs.visual_editor.handler;

import app.display.dialogs.visual_editor.model.GameParser;

import javax.swing.*;
import java.io.File;

public class EditorMenuBarHandler
{

    public static void openDescriptionFile()
    {
        final JFileChooser fc = new JFileChooser();
        int returnValue = fc.showOpenDialog(Handler.mainPanel);
        if (returnValue == JFileChooser.APPROVE_OPTION)
        {
            File file = fc.getSelectedFile();
            GameParser.ParseFileToGraph(file, Handler.editorPanel);
        }
    }

}
