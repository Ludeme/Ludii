package app.display.dialogs.visual_editor.view.panels.header;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.view.designPalettes.DesignPalette;

import javax.swing.*;
import java.awt.*;

public class EditorPickerPanel extends JPanel {
    public EditorPickerPanel() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));


        HeaderButton gameEditorBtn = new HeaderButton(Handler.currentPalette().GAME_EDITOR_ACTIVE, Handler.currentPalette().GAME_EDITOR_INACTIVE, Handler.currentPalette().GAME_EDITOR_HOVER, "Game Editor", true, true);
        HeaderButton defineEditorBtn = new HeaderButton(Handler.currentPalette().DEFINE_EDITOR_ACTIVE, Handler.currentPalette().DEFINE_EDITOR_INACTIVE, Handler.currentPalette().DEFINE_EDITOR_HOVER, "Define Editor", false, true);
        HeaderButton textEditorBtn = new HeaderButton(Handler.currentPalette().TEXT_EDITOR_ACTIVE, Handler.currentPalette().TEXT_EDITOR_INACTIVE, Handler.currentPalette().TEXT_EDITOR_HOVER, "Text Editor", false, true);


        setOpaque(false);

        add(Box.createHorizontalStrut(20));
        add(gameEditorBtn);
        add(Box.createHorizontalStrut(10));
        add(defineEditorBtn);
        add(Box.createHorizontalStrut(10));
        add(textEditorBtn);

    }
}
