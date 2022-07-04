package app.display.dialogs.visual_editor.view.panels.header;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.UserActions.IUserAction;
import app.display.dialogs.visual_editor.view.designPalettes.DesignPalette;

import javax.swing.*;
import java.awt.*;
import java.util.Stack;

public class ToolsPanel extends JPanel {

    private final HeaderButton selectBtn = new HeaderButton(Handler.currentPalette().SELECT_ACTIVE(), Handler.currentPalette().SELECT_INACTIVE(), Handler.currentPalette().SELECT_HOVER(), "Select", false, true);

    private final HeaderButton undoBtn = new HeaderButton(Handler.currentPalette().UNDO_ACTIVE(), Handler.currentPalette().UNDO_INACTIVE(), Handler.currentPalette().UNDO_HOVER(), "Undo", false, false);
    private final HeaderButton redoBtn = new HeaderButton(Handler.currentPalette().REDO_ACTIVE(), Handler.currentPalette().REDO_INACTIVE(), Handler.currentPalette().REDO_HOVER(), "Redo", false, false);
    public final PlayButton play = new PlayButton();

    public ToolsPanel() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        setOpaque(false);

        undoBtn.addActionListener(e -> Handler.undo());
        redoBtn.addActionListener(e -> Handler.redo());

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
        add(play);
        add(Box.createHorizontalStrut(20));

    }

    public void updateUndoRedoBtns(Stack<IUserAction> performedActions, Stack<IUserAction> undoneActions)
    {
        undoBtn.setEnabled(!performedActions.isEmpty());
        redoBtn.setEnabled(!undoneActions.isEmpty());

        if(!performedActions.isEmpty())
            undoBtn.setToolTipText(performedActions.peek().actionType().toString());
        else
            undoBtn.setToolTipText(null);

        if(!undoneActions.isEmpty())
            redoBtn.setToolTipText(undoneActions.peek().actionType().toString());
        else
            redoBtn.setToolTipText(null);
    }

    public void deactivateSelection()
    {
        selectBtn.setInactive();
        selectBtn.repaint();
        selectBtn.revalidate();
    }

    @Override
    public void repaint()
    {
        super.repaint();
        if(selectBtn == null)
            return;
        if(selectBtn.ACTIVE_COLOR != Handler.currentPalette().HEADER_BUTTON_ACTIVE_COLOR())
        {
            selectBtn.ACTIVE_ICON = Handler.currentPalette().SELECT_ACTIVE();
            selectBtn.INACTIVE_ICON = Handler.currentPalette().SELECT_INACTIVE();
            selectBtn.HOVER_ICON = Handler.currentPalette().SELECT_HOVER();
            selectBtn.ACTIVE_COLOR = Handler.currentPalette().HEADER_BUTTON_ACTIVE_COLOR();
            selectBtn.INACTIVE_COLOR = Handler.currentPalette().HEADER_BUTTON_INACTIVE_COLOR();
            selectBtn.HOVER_COLOR = Handler.currentPalette().HEADER_BUTTON_HOVER_COLOR();
            selectBtn.updateDP();

            undoBtn.ACTIVE_ICON = Handler.currentPalette().UNDO_ACTIVE();
            undoBtn.INACTIVE_ICON = Handler.currentPalette().UNDO_INACTIVE();
            undoBtn.HOVER_ICON = Handler.currentPalette().UNDO_HOVER();
            undoBtn.ACTIVE_COLOR = Handler.currentPalette().HEADER_BUTTON_ACTIVE_COLOR();
            undoBtn.INACTIVE_COLOR = Handler.currentPalette().HEADER_BUTTON_INACTIVE_COLOR();
            undoBtn.HOVER_COLOR = Handler.currentPalette().HEADER_BUTTON_HOVER_COLOR();
            undoBtn.updateDP();

            redoBtn.ACTIVE_ICON = Handler.currentPalette().REDO_ACTIVE();
            redoBtn.INACTIVE_ICON = Handler.currentPalette().REDO_INACTIVE();
            redoBtn.HOVER_ICON = Handler.currentPalette().REDO_HOVER();
            redoBtn.ACTIVE_COLOR = Handler.currentPalette().HEADER_BUTTON_ACTIVE_COLOR();
            redoBtn.INACTIVE_COLOR = Handler.currentPalette().HEADER_BUTTON_INACTIVE_COLOR();
            redoBtn.HOVER_COLOR = Handler.currentPalette().HEADER_BUTTON_HOVER_COLOR();
            redoBtn.updateDP();
        }
    }

}
