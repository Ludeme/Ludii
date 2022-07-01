package app.display.dialogs.visual_editor.handler;

import app.DesktopApp;
import app.display.dialogs.GameLoaderDialog;
import app.display.dialogs.visual_editor.model.GameParser;
import app.display.dialogs.visual_editor.recs.display.ProgressBar;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import main.FileHandling;
import other.GameLoader;

import javax.swing.*;
import java.io.File;
import java.util.Objects;
import java.util.regex.Pattern;

public class EditorMenuBarHandler
{

    public static void openDescriptionFile()
    {
        // reused code from class GameLoading method loadGameFromMemory
        final String[] choices = FileHandling.listGames();

        String initialChoice = choices[0];
        //for (final String choice : choices)
        //{
        //    if (app.manager().savedLudName() != null && app.manager().savedLudName().endsWith(choice.replaceAll(Pattern.quote("\\"), "/")))
        //    {
        //        initialChoice = choice;
        //        break;
        //    }
        //}
        final String choice = GameLoaderDialog.showDialog(DesktopApp.frame(), choices, initialChoice);

        if (choice != null)
        {
            String path = choice.replaceAll(Pattern.quote("\\"), "/");
            path = path.substring(path.indexOf("/lud/"));

            path = Objects.requireNonNull(GameLoader.class.getResource(path)).getPath();
            path = path.replaceAll(Pattern.quote("%20"), " ");

            File file = new File(path);
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
