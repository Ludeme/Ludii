package app.display.dialogs.visual_editor;

import app.display.dialogs.visual_editor.recs.codecompletion.controller.NGramController;
import app.display.dialogs.visual_editor.view.MainFrame;
import app.display.dialogs.visual_editor.view.panels.editor.EditorPanel;

import javax.swing.*;

public class Main {

    private static EditorPanel editPanel;
    private static NGramController controller;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception ignored){}

        controller = new NGramController(7);
        editPanel = new EditorPanel(5000,5000);
        MainFrame f = new MainFrame(editPanel);
        //MainFrame f2 = new MainFrame(new EditorPanel(5000,5000));
    }

    public static void updatePanel()
    {
        editPanel.repaint();
        editPanel.revalidate();
    }

    public static NGramController controller() {
        return controller;
    }

}