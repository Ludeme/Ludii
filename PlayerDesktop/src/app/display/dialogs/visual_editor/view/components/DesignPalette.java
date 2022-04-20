package app.display.dialogs.visual_editor.view.components;

import javax.swing.*;
import java.awt.*;

/**
 * Stores colors and fonts for objects in the visual editor
 * @author Filipp Dokienko
 */

public class DesignPalette {
    // ~~ COLORS ~~ //

    // PANELS //
    public static Color BACKGROUND_EDITOR = new Color(244,244,244);;

    // LUDEME BLOCK //
        // fonts
    public static Color FONT_LUDEME_INPUTS_COLOR = new Color(123,123,123);
    public static Color FONT_LUDEME_TITLE_COLOR = new Color(29,29,29);
        // backgrounds
    public static Color BACKGROUND_LUDEME_BODY = new Color(253,253,253);
    public static Color BACKGROUND_LUDEME_TITLE = new Color(253,253,253);
        // fills
    public static Color LUDEME_CONNECTION_POINT = new Color(112,112,112);
    public static Color LUDEME_CONNECTION_EDGE = new Color(112,112,112);

    // ~~ FONTS ~~ //

    // LUDEME BLOCK //
    public static final Font LUDEME_TITLE_FONT = new Font("Roboto Bold", 0,  14);
    public static final Font LUDEME_INPUT_FONT = new Font("Robot Regular", 0, 12);

    public static final ImageIcon CHOICE_ICON_ACTIVE = new ImageIcon("resources/icons/node/active/choice.png");
    public static final ImageIcon CHOICE_ICON_HOVER = new ImageIcon("resources/icons/node/hover/choice.png");
    public static final ImageIcon COLLECTION_ICON_ACTIVE = new ImageIcon("resources/icons/node/active/collection_add.png");
    public static final ImageIcon COLLECTION_ICON_HOVER = new ImageIcon("resources/icons/node/hover/collection_add.png");
    public static final ImageIcon COLLECTION_REMOVE_ICON_HOVER = new ImageIcon("resources/icons/node/hover/collection_remove.png");
    public static final ImageIcon COLLECTION_REMOVE_ICON_ACTIVE = new ImageIcon("resources/icons/node/active/collection_remove.png");
    public static final ImageIcon OPTIONAL_ICON_ACTIVE = new ImageIcon("resources/icons/node/active/optional.png");
    public static final ImageIcon OPTIONAL_ICON_HOVER = new ImageIcon("resources/icons/node/hover/optional.png");

}