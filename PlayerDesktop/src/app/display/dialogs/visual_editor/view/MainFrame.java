package app.display.dialogs.visual_editor.view;


import app.display.dialogs.visual_editor.Main;
import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.filehandling.DocHandler;
import app.display.dialogs.visual_editor.recs.utils.CSVUtils;
import app.display.dialogs.visual_editor.view.panels.MainPanel;
import app.display.dialogs.visual_editor.view.panels.editor.EditorPanel;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    private MainPanel main_panel;

    public MainFrame(EditorPanel editor_panel){
        initialize(editor_panel);
    }

    private void initialize(EditorPanel editor_panel){
        DesignPalette.initializeFonts();
        setTitle("Ludii Visual Editor");
        setIconImage((DesignPalette.LUDII_ICON).getImage());
        setSize(DesignPalette.DEFAULT_FRAME_SIZE);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        main_panel = new MainPanel(editor_panel);
        Handler.setMainPanel(main_panel);
        add(main_panel);
        addWindowListener(new WindowAdapter() {
            /**
             * Invoked when a window is in the process of being closed.
             * The close operation can be overridden at this point.
             *
             * @param e
             */
            @Override
            public void windowClosing(WindowEvent e) {
                DocHandler.getInstance().close();
                Main.controller().close();
                String header = "latency_nano,selected_index";
                List<Long> latencies = editor_panel.getLatencies();
                List<Integer> selectedCompletion = editor_panel.getSelectedCompletion();
                List<String> lines = new ArrayList<>();

                for(int i = 0; i < latencies.size() && i < selectedCompletion.size(); i++) {
                    lines.add(latencies.get(i)+","+selectedCompletion.get(i));
                }

                String path = "PlayerDesktop/src/app/display/dialogs/visual_editor/resources/recs/validation/user_tests/";
                String fileName = "test_"+System.currentTimeMillis()+".csv";

                CSVUtils.writeCSV(path+fileName,header,lines);

                super.windowClosing(e);
            }
        });

        //setLayout(new FlowLayout());
        //add(new AddLudemeWindow(100,100,new Parser().getLudemes()));

        setJMenuBar(new EditorMenuBar());

        setVisible(true);

    }



}
