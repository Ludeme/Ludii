package app.display.dialogs.visual_editor.view;


import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.view.panels.MainPanel;
import app.display.dialogs.visual_editor.view.panels.editor.EditorPanel;

import javax.swing.*;

public class MainFrame extends JFrame {

    private MainPanel main_panel;

    public MainFrame(EditorPanel editor_panel){
        initialize(editor_panel);
    }

    private void initialize(EditorPanel editor_panel){
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception ignored){}
        DesignPalette.initializeFonts();
        setTitle("Ludii Visual Editor");
        setIconImage((DesignPalette.LUDII_ICON).getImage());
        setSize(DesignPalette.DEFAULT_FRAME_SIZE);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        main_panel = new MainPanel(editor_panel);
        Handler.setMainPanel(main_panel);
        add(main_panel);

        //setLayout(new FlowLayout());
        //add(new AddLudemeWindow(100,100,new Parser().getLudemes()));

        setVisible(true);

    }



}
