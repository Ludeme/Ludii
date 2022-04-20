package app.display.dialogs.visual_editor.view.panels.header;

import app.display.dialogs.visual_editor.handler.Handler;

import javax.swing.*;
import java.awt.*;

public class ToolsPanel extends JPanel {
    public ToolsPanel() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        ImageIcon selectActive = new ImageIcon("resources/icons/editor/active/select.png");
        ImageIcon selectInactive = new ImageIcon("resources/icons/editor/inactive/select.png");
        ImageIcon selectHover = new ImageIcon("resources/icons/editor/hover/select.png");
        HeaderButton selectBtn = new HeaderButton(selectActive, selectInactive, selectHover, "Select", false);


        ImageIcon undoActive = new ImageIcon("resources/icons/editor/active/undo.png");
        ImageIcon undoInactive = new ImageIcon("resources/icons/editor/inactive/undo.png");
        ImageIcon undoHover = new ImageIcon("resources/icons/editor/hover/undo.png");
        HeaderButton undoBtn = new HeaderButton(undoActive, undoInactive, undoHover, "Undo", false);

        ImageIcon redoActive = new ImageIcon("resources/icons/editor/active/redo.png");
        ImageIcon redoInactive = new ImageIcon("resources/icons/editor/inactive/redo.png");
        ImageIcon redoHover = new ImageIcon("resources/icons/editor/hover/redo.png");
        HeaderButton redoBtn = new HeaderButton(redoActive, redoInactive, redoHover, "Redo", false);

        setBackground(Color.WHITE);

        add(selectBtn);
        add(Box.createHorizontalStrut(30));
        add(undoBtn);
        add(Box.createHorizontalStrut(8));
        add(redoBtn);
        add(Box.createHorizontalStrut(30));
        JButton play = new JButton("Play");
        play.addActionListener((e) -> {
            // add dialog box here
            JDialog dialog = new JDialog();
            dialog.setTitle("Play");
            dialog.add(new JTextArea(Handler.getLudString(Handler.gameDescriptionGraph)));
            dialog.setSize(new Dimension(300, 200));
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);


        });
        add(play);
        add(Box.createHorizontalStrut(20));

    }
}
