package app.display.dialogs.visual_editor.view.panels.header;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.UserActions.IUserAction;
import app.display.dialogs.visual_editor.view.DesignPalette;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;

public class ToolsPanel extends JPanel {

    private final HeaderButton selectBtn = new HeaderButton(DesignPalette.SELECT_ACTIVE, DesignPalette.SELECT_INACTIVE, DesignPalette.SELECT_HOVER, "Select", false, true);

    private final HeaderButton undoBtn = new HeaderButton(DesignPalette.UNDO_ACTIVE, DesignPalette.UNDO_INACTIVE, DesignPalette.UNDO_HOVER, "Undo", false, false);
    private final HeaderButton redoBtn = new HeaderButton(DesignPalette.REDO_ACTIVE, DesignPalette.REDO_INACTIVE, DesignPalette.REDO_HOVER, "Redo", false, false);

    public ToolsPanel() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));


        undoBtn.addActionListener(e -> Handler.undo());
        redoBtn.addActionListener(e -> Handler.redo());

        setBackground(Color.WHITE);

        selectBtn.addActionListener(e -> {
            if (!selectBtn.isActive()) Handler.activateSelectionMode();
            // else Handler.deactivateSelectionMode();
        });
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

    public void updateUndoRedoBtns(Stack<IUserAction> performedActions, Stack<IUserAction> undoneActions)
    {
        undoBtn.setEnabled(!performedActions.isEmpty());

        redoBtn.setEnabled(!undoneActions.isEmpty());
    }

    public void deactivateSelection()
    {
        selectBtn.setInactive();
        selectBtn.repaint();
        selectBtn.revalidate();
    }

}
