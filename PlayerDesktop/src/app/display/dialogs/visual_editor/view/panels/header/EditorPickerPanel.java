package app.display.dialogs.visual_editor.view.panels.header;

import javax.swing.*;
import java.awt.*;

public class EditorPickerPanel extends JPanel {
    public EditorPickerPanel() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));


        ImageIcon gameEditorActive = new ImageIcon("resources/icons/editor/active/game_editor.png");
        ImageIcon gameEditorInactive = new ImageIcon("resources/icons/editor/inactive/game_editor.png");
        ImageIcon gameEditorHover = new ImageIcon("resources/icons/editor/hover/game_editor.png");
        HeaderButton gameEditorBtn = new HeaderButton(gameEditorActive, gameEditorInactive, gameEditorHover, "Game Editor", true);


        ImageIcon defineEditorActive = new ImageIcon("resources/icons/editor/active/define_editor.png");
        ImageIcon defineEditorInactive = new ImageIcon("resources/icons/editor/inactive/define_editor.png");
        ImageIcon defineEditorHover = new ImageIcon("resources/icons/editor/hover/define_editor.png");
        HeaderButton defineEditorBtn = new HeaderButton(defineEditorActive, defineEditorInactive, defineEditorHover, "Define Editor", false);


        ImageIcon textEditorActive = new ImageIcon("resources/icons/editor/active/text_editor.png");
        ImageIcon textEditorInactive = new ImageIcon("resources/icons/editor/inactive/text_editor.png");
        ImageIcon textEditorHover = new ImageIcon("resources/icons/editor/hover/text_editor.png");
        HeaderButton textEditorBtn = new HeaderButton(textEditorActive, textEditorInactive, textEditorHover, "Text Editor", false);


        setBackground(Color.WHITE);

        add(Box.createHorizontalStrut(20));
        add(gameEditorBtn);
        add(Box.createHorizontalStrut(10));
        add(defineEditorBtn);
        add(Box.createHorizontalStrut(10));
        add(textEditorBtn);

    }
}
