package app.display.dialogs.visual_editor.handler;

import app.display.dialogs.visual_editor.model.GameParser;
import app.display.dialogs.visual_editor.recs.display.ProgressBar;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

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
            ProgressBar progressBar = new ProgressBar("Loading game from file",
                    "Loading game from file", 8);
            StartGraphParsingThread(file, Handler.editorPanel, progressBar);
        }
    }

    private static void StartGraphParsingThread(File file, IGraphPanel panel, ProgressBar progressBar)
    {

        SwingWorker swingWorker = new SwingWorker()
        {
            @Override
            protected Object doInBackground()
            {
                GameParser.ParseFileToGraph(file, Handler.editorPanel, progressBar);
                return null;
            }

            @Override
            protected void done()
            {
                super.done();
                progressBar.close();
                panel.getLayoutHandler().executeLayout();
            }
        };

        swingWorker.execute();
    }

}
