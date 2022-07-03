package app.display.dialogs.visual_editor.view.panels.header;

import app.display.dialogs.visual_editor.handler.Handler;

import javax.swing.*;
import java.awt.*;

public class EditorPickerPanel extends JPanel
{

    private HeaderButton gameEditorBtn;
    private HeaderButton defineEditorBtn;
    private HeaderButton textEditorBtn;

    public EditorPickerPanel()
    {
        super();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));


        gameEditorBtn = new HeaderButton(Handler.currentPalette().GAME_EDITOR_ACTIVE(), Handler.currentPalette().GAME_EDITOR_INACTIVE(), Handler.currentPalette().GAME_EDITOR_HOVER(), "Game Editor", true, true);
        defineEditorBtn = new HeaderButton(Handler.currentPalette().DEFINE_EDITOR_ACTIVE(), Handler.currentPalette().DEFINE_EDITOR_INACTIVE(), Handler.currentPalette().DEFINE_EDITOR_HOVER(), "Define Editor", false, true);
        textEditorBtn = new HeaderButton(Handler.currentPalette().TEXT_EDITOR_ACTIVE(), Handler.currentPalette().TEXT_EDITOR_INACTIVE(), Handler.currentPalette().TEXT_EDITOR_HOVER(), "Text Editor", false, true);


        setOpaque(false);

        add(Box.createHorizontalStrut(20));
        add(gameEditorBtn);
        add(Box.createHorizontalStrut(10));
        add(defineEditorBtn);
        add(Box.createHorizontalStrut(10));
        add(textEditorBtn);

    }

    @Override
    public void repaint()
    {
        super.repaint();
        if(gameEditorBtn == null)
            return;
        if(gameEditorBtn.ACTIVE_ICON != Handler.currentPalette().GAME_EDITOR_ACTIVE())
        {
            gameEditorBtn.ACTIVE_ICON = Handler.currentPalette().GAME_EDITOR_ACTIVE();
            gameEditorBtn.INACTIVE_ICON = Handler.currentPalette().GAME_EDITOR_INACTIVE();
            gameEditorBtn.HOVER_ICON = Handler.currentPalette().GAME_EDITOR_HOVER();
            gameEditorBtn.ACTIVE_COLOR = Handler.currentPalette().HEADER_BUTTON_ACTIVE_COLOR();
            gameEditorBtn.INACTIVE_COLOR = Handler.currentPalette().HEADER_BUTTON_INACTIVE_COLOR();
            gameEditorBtn.HOVER_COLOR = Handler.currentPalette().HEADER_BUTTON_HOVER_COLOR();
            gameEditorBtn.updateDP();

            defineEditorBtn.ACTIVE_ICON = Handler.currentPalette().DEFINE_EDITOR_ACTIVE();
            defineEditorBtn.INACTIVE_ICON = Handler.currentPalette().DEFINE_EDITOR_INACTIVE();
            defineEditorBtn.HOVER_ICON = Handler.currentPalette().DEFINE_EDITOR_HOVER();
            defineEditorBtn.ACTIVE_COLOR = Handler.currentPalette().HEADER_BUTTON_ACTIVE_COLOR();
            defineEditorBtn.INACTIVE_COLOR = Handler.currentPalette().HEADER_BUTTON_INACTIVE_COLOR();
            defineEditorBtn.HOVER_COLOR = Handler.currentPalette().HEADER_BUTTON_HOVER_COLOR();
            defineEditorBtn.updateDP();

            textEditorBtn.ACTIVE_ICON = Handler.currentPalette().TEXT_EDITOR_ACTIVE();
            textEditorBtn.INACTIVE_ICON = Handler.currentPalette().TEXT_EDITOR_INACTIVE();
            textEditorBtn.HOVER_ICON = Handler.currentPalette().TEXT_EDITOR_HOVER();
            textEditorBtn.ACTIVE_COLOR = Handler.currentPalette().HEADER_BUTTON_ACTIVE_COLOR();
            textEditorBtn.INACTIVE_COLOR = Handler.currentPalette().HEADER_BUTTON_INACTIVE_COLOR();
            textEditorBtn.HOVER_COLOR = Handler.currentPalette().HEADER_BUTTON_HOVER_COLOR();
            textEditorBtn.updateDP();
        }
    }

}
