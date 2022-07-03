package app.display.dialogs.visual_editor.view;

import app.display.dialogs.visual_editor.StartVisualEditor;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.filehandling.DocHandler;
import app.display.dialogs.visual_editor.view.designPalettes.DesignPalette;
import app.display.dialogs.visual_editor.view.panels.menus.EditorMenuBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class VisualEditorFrame extends JFrame
{

    // Frame Properties
    private static final String TITLE = "Ludii Visual Editor";
    private static final Dimension DEFAULT_FRAME_SIZE = DesignPalette.DEFAULT_FRAME_SIZE;
    private static final ImageIcon FRAME_ICON = DesignPalette.LUDII_ICON;
    private final VisualEditorPanel panel;
    private final EditorMenuBar menuBar = new EditorMenuBar();


    public VisualEditorFrame()
    {
        // load fonts
        DesignPalette.initializeFonts();

        // set frame properties
        setTitle(TITLE);
        setIconImage(FRAME_ICON.getImage());
        setSize(DEFAULT_FRAME_SIZE);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        panel = new VisualEditorPanel(this);
        add(panel);

        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                DocHandler.getInstance().close();
                StartVisualEditor.controller().close();

                // TODO: For Filip's testing
                /*
                String header = "latency_nano,selected_index";
                List<Long> latencies = editor_panel.latencies();
                List<Integer> selectedCompletion = editor_panel.selectedCompletion();
                List<String> lines = new ArrayList<>();

                for(int i = 0; i < latencies.size() && i < selectedCompletion.size(); i++) {
                    lines.add(latencies.get(i)+","+selectedCompletion.get(i));
                }

                String path = "src/app/display/dialogs/visual_editor/resources/recs/validation/user_tests/";
                String fileName = "test_"+System.currentTimeMillis()+".csv";

                CSVUtils.writeCSV(path+fileName,header,lines);
                */
                super.windowClosing(e);
            }
        });

        setJMenuBar(menuBar);
        setVisible(true);
    }
}
