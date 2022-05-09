package app.display.dialogs.visual_editor.view.panels.header;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.view.components.DesignPalette;

import javax.swing.*;
import java.awt.*;

public class ToolsPanel extends JPanel {
    public ToolsPanel() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        HeaderButton selectBtn = new HeaderButton(DesignPalette.SELECT_ACTIVE, DesignPalette.SELECT_INACTIVE, DesignPalette.SELECT_HOVER, "Select", false);
        HeaderButton undoBtn = new HeaderButton(DesignPalette.UNDO_ACTIVE, DesignPalette.UNDO_INACTIVE, DesignPalette.UNDO_HOVER, "Undo", false);
        HeaderButton redoBtn = new HeaderButton(DesignPalette.REDO_ACTIVE, DesignPalette.REDO_INACTIVE, DesignPalette.REDO_HOVER, "Redo", false);

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
