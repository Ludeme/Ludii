package app.display.dialogs.visual_editor.view;


import app.display.dialogs.visual_editor.view.panels.MainPanel;

import javax.swing.*;

public class MainFrame extends JFrame {

    private MainPanel main_panel;

    public MainFrame(JPanel editor_panel){
        initialize(editor_panel);
    }

    private void initialize(JPanel editor_panel){
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception ignored){}
        setTitle("Ludii Visual Editor");
        setIconImage(new ImageIcon("resources/icons/logo-clover-c.png").getImage());
        setSize(1200,800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        main_panel = new MainPanel(editor_panel);
        add(main_panel);

        //setLayout(new FlowLayout());
        //add(new AddLudemeWindow(100,100,new Parser().getLudemes()));

        setVisible(true);

    }



}
