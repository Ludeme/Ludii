package app.display.dialogs.visual_editor.view.panels.header;

import app.display.dialogs.visual_editor.view.DesignPalette;

import javax.swing.*;
import java.awt.*;

public class EditorPickerPanel extends JPanel {
    public EditorPickerPanel() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));


        HeaderButton gameEditorBtn = new HeaderButton(DesignPalette.GAME_EDITOR_ACTIVE, DesignPalette.GAME_EDITOR_INACTIVE, DesignPalette.GAME_EDITOR_HOVER, "Game Editor", true, true);
        HeaderButton defineEditorBtn = new HeaderButton(DesignPalette.DEFINE_EDITOR_ACTIVE, DesignPalette.DEFINE_EDITOR_INACTIVE, DesignPalette.DEFINE_EDITOR_HOVER, "Define Editor", false, true);
        HeaderButton textEditorBtn = new HeaderButton(DesignPalette.TEXT_EDITOR_ACTIVE, DesignPalette.TEXT_EDITOR_INACTIVE, DesignPalette.TEXT_EDITOR_HOVER, "Text Editor", false, true);


        setBackground(Color.WHITE);

        add(Box.createHorizontalStrut(20));
        add(gameEditorBtn);
        add(Box.createHorizontalStrut(10));
        add(defineEditorBtn);
        add(Box.createHorizontalStrut(10));
        add(textEditorBtn);

    }
}
