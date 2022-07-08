package app.display.dialogs.visual_editor.view.panels.header;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.view.VisualEditorPanel;
import app.display.dialogs.visual_editor.view.designPalettes.DesignPalette;

import javax.swing.*;

public class EditorPickerPanel extends JPanel
{

    private final HeaderButton gameEditorBtn;
    private final HeaderButton defineEditorBtn;
    private final HeaderButton textEditorBtn;

    public EditorPickerPanel(VisualEditorPanel visualEditorPanel)
    {
        super();

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        gameEditorBtn = new HeaderButton(DesignPalette.GAME_EDITOR_ACTIVE(), DesignPalette.GAME_EDITOR_INACTIVE(), DesignPalette.GAME_EDITOR_HOVER(), "Game Editor", true, true);
        defineEditorBtn = new HeaderButton(DesignPalette.DEFINE_EDITOR_ACTIVE(), DesignPalette.DEFINE_EDITOR_INACTIVE(), DesignPalette.DEFINE_EDITOR_HOVER(), "Define Editor", false, true);
        textEditorBtn = new HeaderButton(DesignPalette.TEXT_EDITOR_ACTIVE(), DesignPalette.TEXT_EDITOR_INACTIVE(), DesignPalette.TEXT_EDITOR_HOVER(), ".lud", false, true);

        gameEditorBtn.setClickListenerOn(false);
        defineEditorBtn.setClickListenerOn(false);
        textEditorBtn.setClickListenerOn(false);

        gameEditorBtn.addActionListener(e ->
        {
            if(gameEditorBtn.isActive())
                return;
            visualEditorPanel.openGameEditor();
            gameEditorBtn.setActive();
            defineEditorBtn.setInactive();
            textEditorBtn.setInactive();
        });

        defineEditorBtn.addActionListener(e ->
        {
            if(defineEditorBtn.isActive())
                return;
            visualEditorPanel.openDefineEditor();
            defineEditorBtn.setActive();
            gameEditorBtn.setInactive();
            textEditorBtn.setInactive();
        });

        textEditorBtn.addActionListener(e ->
        {
            if(textEditorBtn.isActive())
                return;
            visualEditorPanel.openTextEditor();
            textEditorBtn.setActive();
            gameEditorBtn.setInactive();
            defineEditorBtn.setInactive();
        });

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
        if(gameEditorBtn.ACTIVE_ICON != DesignPalette.GAME_EDITOR_ACTIVE())
        {
            gameEditorBtn.ACTIVE_ICON = DesignPalette.GAME_EDITOR_ACTIVE();
            gameEditorBtn.INACTIVE_ICON = DesignPalette.GAME_EDITOR_INACTIVE();
            gameEditorBtn.HOVER_ICON = DesignPalette.GAME_EDITOR_HOVER();
            gameEditorBtn.ACTIVE_COLOR = DesignPalette.HEADER_BUTTON_ACTIVE_COLOR();
            gameEditorBtn.INACTIVE_COLOR = DesignPalette.HEADER_BUTTON_INACTIVE_COLOR();
            gameEditorBtn.HOVER_COLOR = DesignPalette.HEADER_BUTTON_HOVER_COLOR();
            gameEditorBtn.updateDP();

            defineEditorBtn.ACTIVE_ICON = DesignPalette.DEFINE_EDITOR_ACTIVE();
            defineEditorBtn.INACTIVE_ICON = DesignPalette.DEFINE_EDITOR_INACTIVE();
            defineEditorBtn.HOVER_ICON = DesignPalette.DEFINE_EDITOR_HOVER();
            defineEditorBtn.ACTIVE_COLOR = DesignPalette.HEADER_BUTTON_ACTIVE_COLOR();
            defineEditorBtn.INACTIVE_COLOR = DesignPalette.HEADER_BUTTON_INACTIVE_COLOR();
            defineEditorBtn.HOVER_COLOR = DesignPalette.HEADER_BUTTON_HOVER_COLOR();
            defineEditorBtn.updateDP();

            textEditorBtn.ACTIVE_ICON = DesignPalette.TEXT_EDITOR_ACTIVE();
            textEditorBtn.INACTIVE_ICON = DesignPalette.TEXT_EDITOR_INACTIVE();
            textEditorBtn.HOVER_ICON = DesignPalette.TEXT_EDITOR_HOVER();
            textEditorBtn.ACTIVE_COLOR = DesignPalette.HEADER_BUTTON_ACTIVE_COLOR();
            textEditorBtn.INACTIVE_COLOR = DesignPalette.HEADER_BUTTON_INACTIVE_COLOR();
            textEditorBtn.HOVER_COLOR = DesignPalette.HEADER_BUTTON_HOVER_COLOR();
            textEditorBtn.updateDP();
        }
    }

}
