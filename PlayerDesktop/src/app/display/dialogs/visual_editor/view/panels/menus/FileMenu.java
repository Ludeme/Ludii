package app.display.dialogs.visual_editor.view.panels.menus;

import app.DesktopApp;
import app.display.dialogs.GameLoaderDialog;
import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.GameParser;
import app.display.dialogs.visual_editor.recs.display.ProgressBar;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import main.FileHandling;
import other.GameLoader;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * File menu for visual editor
 * @author nic0gin
 */
public class FileMenu extends JMenu
{

    /**
	 * 
	 */
	private static final long serialVersionUID = -6694747737057742537L;

	/**
     * Constructor
     */
    public FileMenu(EditorMenuBar menuBar)
    {
        super("File");
        // TODO: unfortunately we did not managed to complete all functionalities for this menu in time;

        // adding file menu items
        // menuBar.addJMenuItem(this, "New", null);
        // menuBar.addJMenuItem(this, "Open...", e -> openDescriptionFile());
        // menuBar.addJMenuItem(this, "Open recent", null);
        // menuBar.addJMenuItem(this, "Close file", null);

        // add(new JSeparator());

        // menuBar.addJMenuItem(this, "Save", null);
        // menuBar.addJMenuItem(this, "Save as...", null);
        menuBar.addJMenuItem(this, "Export as .lud", e -> exportAsLud());

        // add(new JSeparator());

        // menuBar.addJMenuItem(this, "Exit", null);
    }

    /**
     * Opens selection menu of .lud game description to be imported into graph panel
     * TODO: utilized due to parsing issues
     */
    private static void openDescriptionFile()
    {
        // reused code from class GameLoading method loadGameFromMemory
        final String[] choices = FileHandling.listGames();
        String initialChoice = choices[0];
        final String choice = GameLoaderDialog.showDialog(DesktopApp.frame(), choices, initialChoice);

        if (choice != null)
        {
            String path = choice.replaceAll(Pattern.quote("\\"), "/");
            path = path.substring(path.indexOf("/lud/"));

            path = Objects.requireNonNull(GameLoader.class.getResource(path)).getPath();
            path = path.replaceAll(Pattern.quote("%20"), " ");

            File file = new File(path);
            ProgressBar progressBar = new ProgressBar("Loading game from file",
                    null, 8);
            StartGraphParsingThread(file, Handler.gameGraphPanel, progressBar);
        }
    }

    /**
     * Export game graph to .lud
     */
    private static void exportAsLud()
    {
        String lud = Handler.toLud();


        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export as .lud");
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Ludii Game", "lud"));
        int userSelection = fileChooser.showSaveDialog(Handler.visualEditorFrame);
        if(userSelection == JFileChooser.APPROVE_OPTION)
        {
            File file = fileChooser.getSelectedFile();
            String path = file.getAbsolutePath();
            if(!file.getName().endsWith(".lud"))
                path += ".lud";

            FileHandling.saveStringToFile(lud, path, "");
        }

        System.out.println("\n\n## GAME DESCRIPTION .lud ###");
        System.out.println(lud);
        System.out.println("############################\n\n");
    }

    /**
     * Initiates parsing of ludeme description to graph
     */
    private static void StartGraphParsingThread(File file, IGraphPanel panel, ProgressBar progressBar)
    {

        SwingWorker<?, ?> swingWorker = new SwingWorker<Object, Object>()
        {
            @Override
            protected Object doInBackground()
            {
                GameParser.ParseFileToGraph(file, Handler.gameGraphPanel, progressBar);
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
