package app.display.dialogs.visual_editor.view.designPalettes;

import app.display.dialogs.visual_editor.handler.Handler;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;
import java.util.Objects;

/**
 * Stores colors and fonts for objects in the visual editor
 * @author Filipp Dokienko
 */

public class DesignPalette
{
    public static final Dimension DEFAULT_FRAME_SIZE = new Dimension(1200,800);
    public static final Dimension DEFAULT_GRAPHPANEL_SIZE = new Dimension(10000,10000);

    public static float SCALAR = 1f;

    public static final float MAX_SCALAR = 2.5f;
    public static final float MIN_SCALAR = 0.3f;

    private static final int DEFAULT_NODE_WIDTH = 200; // small: 195, default: 200, bigger: 250 big: 250
    public static int NODE_WIDTH = (int) (DEFAULT_NODE_WIDTH * SCALAR);

    private static final int DEFAULT_TERMINAL_INPUT_HEIGHT = 20; // small: 17, default, bigger: 20, big: 24
    public static int TERMINAL_INPUT_HEIGHT = (int) (DEFAULT_TERMINAL_INPUT_HEIGHT * SCALAR);

    private static final int DEFAULT_LUDEME_INPUT_FONT_SIZE = 13; // small: 10, default, bigger: 13, big: 16
    private static int LUDEME_INPUT_FONT_SIZE = (int) (DEFAULT_LUDEME_INPUT_FONT_SIZE * (1.0/SCALAR));

    private static final int DEFAULT_LUDEME_TITLE_FONT_SIZE = 15; // small: 10, default, bigger 15, big: 18
    private static int LUDEME_TITLE_FONT_SIZE = (int) (DEFAULT_LUDEME_TITLE_FONT_SIZE * SCALAR);

    private static final int DEFAULT_INPUTAREA_PADDING_BOTTOM = 12;
    public static int INPUTAREA_PADDING_BOTTOM = (int) (DEFAULT_INPUTAREA_PADDING_BOTTOM * SCALAR);

    private static final int DEFAULT_HEADER_PADDING_BOTTOM = 3;
    public static int HEADER_PADDING_BOTTOM = (int) (DEFAULT_HEADER_PADDING_BOTTOM * SCALAR);

    private static final int DEFAULT_HEADER_PADDING_TOP = 10;
    public static int HEADER_PADDING_TOP = (int) (DEFAULT_HEADER_PADDING_TOP * SCALAR);

    private static final int DEFAULT_INPUTFIELD_PADDING_LEFT_TERMINAL = 10;
    public static int INPUTFIELD_PADDING_LEFT_TERMINAL = (int) (DEFAULT_INPUTFIELD_PADDING_LEFT_TERMINAL * SCALAR);

    private static final int DEFAULT_INPUTFIELD_PADDING_RIGHT_NONTERMINAL = 5;
    public static int INPUTFIELD_PADDING_RIGHT_NONTERMINAL = (int) (DEFAULT_INPUTFIELD_PADDING_RIGHT_NONTERMINAL * SCALAR);

    private static final int DEFAULT_HEADER_TITLE_CONNECTION_SPACE = 5;
    public static int HEADER_TITLE_CONNECTION_SPACE = (int) (DEFAULT_HEADER_TITLE_CONNECTION_SPACE * SCALAR);

    private static final float DEFAULT_CONNECTION_STROKE_WIDTH = 2f;
    public static float CONNECTION_STROKE_WIDTH = DEFAULT_CONNECTION_STROKE_WIDTH * SCALAR;

    private static final int DEFAULT_NODE_BORDER_WIDTH = 2;
    public static int NODE_BORDER_WIDTH = (int) (DEFAULT_NODE_BORDER_WIDTH * SCALAR);

    private static final int DEFAULT_BACKGROUND_DOT_DIAMETER = 4;
    private static final int DEFAULT_BACKGROUND_LINE_WIDTH = 1;
    public static int BACKGROUND_DOT_DIAMETER = (int) (DEFAULT_BACKGROUND_DOT_DIAMETER * SCALAR);
    public static final int BACKGROUND_LINE_WIDTH = DEFAULT_BACKGROUND_LINE_WIDTH;

    private static final int DEFAULT_BACKGROUND_DOT_PADDING = 25;
    private static final int DEFAULT_BACKGROUND_LINE_PADDING = 25;
    public static int BACKGROUND_DOT_PADDING = (int) (DEFAULT_BACKGROUND_DOT_PADDING * SCALAR);
    public static int BACKGROUND_LINE_PADDING = (int) (DEFAULT_BACKGROUND_LINE_PADDING * SCALAR);


    public void scale(float scalar)
    {
        SCALAR *= scalar;
        SCALAR = (float) Math.min(2.0, Math.max(0.85, SCALAR));
        System.out.println("[SCALING] SCALAR: " + SCALAR);
        NODE_WIDTH = (int) (DEFAULT_NODE_WIDTH * (1.0/SCALAR));
        TERMINAL_INPUT_HEIGHT = (int) (DEFAULT_TERMINAL_INPUT_HEIGHT * (1.0/SCALAR));
        LUDEME_INPUT_FONT_SIZE = (int) (DEFAULT_LUDEME_INPUT_FONT_SIZE * (1.0/SCALAR));
        LUDEME_TITLE_FONT_SIZE = (int) (DEFAULT_LUDEME_TITLE_FONT_SIZE * (1.0/SCALAR));
        INPUTAREA_PADDING_BOTTOM = (int) (DEFAULT_INPUTAREA_PADDING_BOTTOM * (1.0/SCALAR));
        HEADER_PADDING_BOTTOM = (int) (DEFAULT_HEADER_PADDING_BOTTOM * (1.0/SCALAR));
        HEADER_PADDING_TOP = (int) (DEFAULT_HEADER_PADDING_TOP * (1.0/SCALAR));
        INPUTFIELD_PADDING_LEFT_TERMINAL = (int) (DEFAULT_INPUTFIELD_PADDING_LEFT_TERMINAL * (1.0/SCALAR));
        INPUTFIELD_PADDING_RIGHT_NONTERMINAL = (int) (DEFAULT_INPUTFIELD_PADDING_RIGHT_NONTERMINAL * (1.0/SCALAR));
        HEADER_TITLE_CONNECTION_SPACE = (int) (DEFAULT_HEADER_TITLE_CONNECTION_SPACE * (1.0/SCALAR));
        CONNECTION_STROKE_WIDTH = (float) (DEFAULT_CONNECTION_STROKE_WIDTH * (1.0/SCALAR));
        NODE_BORDER_WIDTH = (int) (DEFAULT_NODE_BORDER_WIDTH * (1.0/SCALAR));
        BACKGROUND_DOT_DIAMETER = (int) (DEFAULT_BACKGROUND_DOT_DIAMETER * (1.0/SCALAR));
        BACKGROUND_DOT_PADDING = (int) (DEFAULT_BACKGROUND_DOT_PADDING * (1.0/SCALAR));
        BACKGROUND_LINE_PADDING = (int) (DEFAULT_BACKGROUND_LINE_PADDING * (1.0/SCALAR));

        LUDEME_TITLE_FONT = new Font("Arial", Font.BOLD,  LUDEME_TITLE_FONT_SIZE);
        LUDEME_INPUT_FONT = new Font("Arial", Font.PLAIN, LUDEME_INPUT_FONT_SIZE);
        LUDEME_INPUT_FONT_ITALIC = new Font("Arial", Font.ITALIC, LUDEME_INPUT_FONT_SIZE);
        LUDEME_EDGE_STROKE = new BasicStroke(CONNECTION_STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        LUDEME_NODE_BORDER = BorderFactory.createLineBorder(Handler.currentPalette().LUDEME_BORDER_COLOR(), NODE_BORDER_WIDTH);
        LUDEME_NODE_BORDER_SELECTED = BorderFactory.createLineBorder(Handler.currentPalette().LUDEME_SELECTION_COLOR(), NODE_BORDER_WIDTH);
        LUDEME_NODE_BORDER_UNCOMPILABLE = BorderFactory.createLineBorder(Handler.currentPalette().LUDEME_UNCOMPILABLE_COLOR(), NODE_BORDER_WIDTH);

        INPUT_AREA_PADDING_BORDER = new EmptyBorder(0,0,DesignPalette.INPUTAREA_PADDING_BOTTOM,0);
        HEADER_PADDING_BORDER = new EmptyBorder(DesignPalette.HEADER_PADDING_TOP,0,DesignPalette.HEADER_PADDING_BOTTOM,0);

    }


    // ~~ COLORS ~~ //

    // PANELS //
    private static final Color BACKGROUND_EDITOR = new Color(244,244,244);
    private static final Color BACKGROUND_VISUAL_HELPER = new Color(207,207,207);
    private static final Color BACKGROUND_HEADER_PANEL = new Color(250,250,250);

    // LUDEME BLOCK //
        // fonts
    private static final Color FONT_LUDEME_INPUTS_COLOR = new Color(123,123,123);
    private static final Color FONT_LUDEME_TITLE_COLOR = new Color(29,29,29);


        // backgrounds
    private static final Color BACKGROUND_LUDEME_BODY = new Color(253,253,253);

         // there are 3 classes: game.equipment, game.functions, and game.rules
                    // game.rules: game.rules.play, game.rules.start, game.rules.end
                    // game.functions: .region, .ints, .graph, .floats, .dim, .booleans

    private static final Color BACKGROUND_LUDEME_BODY_EQUIPMENT = new Color(255, 249, 242);
    private static final Color BACKGROUND_LUDEME_BODY_FUNCTIONS = new Color(242, 255, 254);
    private static final Color BACKGROUND_LUDEME_BODY_RULES = new Color(253, 247, 255);
    private static final Color BACKGROUND_LUDEME_BODY_DEFINE = new Color(255, 222, 168);
    private static final Color LUDEME_BORDER_COLOR = new Color(233,233,233);
    private static final Color LUDEME_SELECTION_COLOR = new Color(92, 150, 242);
    private static final Color LUDEME_UNCOMPILABLE_COLOR = new Color(238,60,60);

    private static final Color LUDEME_CONNECTION_POINT = new Color(127,191,255);//new Color(112,112,112);
    private static final Color LUDEME_CONNECTION_POINT_INACTIVE = new Color(238,60,60);
    private static final Color LUDEME_CONNECTION_EDGE = new Color(127,191,255);//new Color(112,112,112);
    private static final Color COMPILABLE_COLOR = new Color(214, 234, 255);
    private static final Color NOT_COMPILABLE_COLOR = new Color(255,214,214);

    private static final Color PLAY_BUTTON_FOREGROUND = new Color(69,69,69);

    private static final Color INPUT_FIELD_BACKGROUND = Color.WHITE;
    private static final Color INPUT_FIELD_BORDER_COLOUR = new Color(176,176,176);
    private static final Color INPUT_FIELD_FOREGROUND = new Color(51, 51, 51);


    public Color BACKGROUND_EDITOR()
    {
        return BACKGROUND_EDITOR;
    }

    public Color BACKGROUND_VISUAL_HELPER()
    {
        return BACKGROUND_VISUAL_HELPER;
    }

    public Color BACKGROUND_HEADER_PANEL()
    {
        return BACKGROUND_HEADER_PANEL;
    }

    public Color FONT_LUDEME_INPUTS_COLOR()
    {
        return FONT_LUDEME_INPUTS_COLOR;
    }

    public Color FONT_LUDEME_TITLE_COLOR()
    {
        return FONT_LUDEME_TITLE_COLOR;
    }

    public Color BACKGROUND_LUDEME_BODY()
    {
        return BACKGROUND_LUDEME_BODY;
    }

    public Color BACKGROUND_LUDEME_BODY_EQUIPMENT()
    {
        return BACKGROUND_LUDEME_BODY_EQUIPMENT;
    }

    public Color BACKGROUND_LUDEME_BODY_FUNCTIONS()
    {
        return BACKGROUND_LUDEME_BODY_FUNCTIONS;
    }

    public Color BACKGROUND_LUDEME_BODY_RULES()
    {
        return BACKGROUND_LUDEME_BODY_RULES;
    }

    public Color BACKGROUND_LUDEME_BODY_DEFINE()
    {
        return BACKGROUND_LUDEME_BODY_DEFINE;
    }

    public static Color LUDEME_BORDER_COLOR()
    {
        return LUDEME_BORDER_COLOR;
    }

    public static Color LUDEME_SELECTION_COLOR()
    {
        return LUDEME_SELECTION_COLOR;
    }

    public static Color LUDEME_UNCOMPILABLE_COLOR()
    {
        return LUDEME_UNCOMPILABLE_COLOR;
    }

    public Color LUDEME_CONNECTION_POINT()
    {
        return LUDEME_CONNECTION_POINT;
    }

    public Color LUDEME_CONNECTION_POINT_INACTIVE()
    {
        return LUDEME_CONNECTION_POINT_INACTIVE;
    }

    public Color LUDEME_CONNECTION_EDGE()
    {
        return LUDEME_CONNECTION_EDGE;
    }


    public Color COMPILABLE_COLOR()
    {
        return COMPILABLE_COLOR;
    }

    public Color NOT_COMPILABLE_COLOR()
    {
        return NOT_COMPILABLE_COLOR;
    }

    public Color PLAY_BUTTON_FOREGROUND()
    {
        return PLAY_BUTTON_FOREGROUND;
    }

    public Color INPUT_FIELD_BACKGROUND()
    {
        return INPUT_FIELD_BACKGROUND;
    }

    public Color INPUT_FIELD_BORDER_COLOUR()
    {
        return INPUT_FIELD_BORDER_COLOUR;
    }

    public Color INPUT_FIELD_FOREGROUND()
    {
        return INPUT_FIELD_FOREGROUND;
    }

    // LUDEME BLOCK //
    public static Font LUDEME_TITLE_FONT = new Font("Arial", Font.BOLD,  LUDEME_TITLE_FONT_SIZE);
    public static Font LUDEME_INPUT_FONT = new Font("Arial", Font.PLAIN, LUDEME_INPUT_FONT_SIZE);
    public static Font LUDEME_INPUT_FONT_ITALIC = new Font("Arial", Font.ITALIC, LUDEME_INPUT_FONT_SIZE);

    // ~~ ICONS ~~ //

    // FRAME //
    public static final ImageIcon LUDII_ICON = new ImageIcon(Objects.requireNonNull(DesignPalette.class.getResource("/ludii-logo-64x64.png")));
    // HEADER EDITORS //
    public static final ImageIcon COMPILABLE_ICON = getIcon("editor/play.png");
    public static final ImageIcon NOT_COMPILABLE_ICON = getIcon("editor/not_compilable.png");

    private static final ImageIcon GAME_EDITOR_ACTIVE = getIcon("editor/active/game_editor.png");
    private static final ImageIcon GAME_EDITOR_INACTIVE = getIcon("editor/inactive/game_editor.png");
    private static final ImageIcon GAME_EDITOR_HOVER = getIcon("editor/hover/game_editor.png");

    private static final ImageIcon DEFINE_EDITOR_ACTIVE = getIcon("editor/active/define_editor.png");
    private static final ImageIcon DEFINE_EDITOR_INACTIVE = getIcon("editor/inactive/define_editor.png");
    private static final ImageIcon DEFINE_EDITOR_HOVER = getIcon("editor/hover/define_editor.png");

    private static final ImageIcon TEXT_EDITOR_ACTIVE = getIcon("editor/active/text_editor.png");
    private static final ImageIcon TEXT_EDITOR_INACTIVE = getIcon("editor/inactive/text_editor.png");
    private static final ImageIcon TEXT_EDITOR_HOVER = getIcon("editor/hover/text_editor.png");
    // HEADER TOOLS //
    private static final ImageIcon SELECT_ACTIVE = getIcon("editor/active/select.png");
    private static final ImageIcon SELECT_INACTIVE = getIcon("editor/inactive/select.png");
    private static final ImageIcon SELECT_HOVER = getIcon("editor/hover/select.png");

    private static final ImageIcon UNDO_ACTIVE = getIcon("editor/active/undo.png");
    private static final ImageIcon UNDO_INACTIVE = getIcon("editor/inactive/undo.png");
    private static final ImageIcon UNDO_HOVER = getIcon("editor/hover/undo.png");

    private static final ImageIcon REDO_ACTIVE = getIcon("editor/active/redo.png");
    private static final ImageIcon REDO_INACTIVE = getIcon("editor/inactive/redo.png");
    private static final ImageIcon REDO_HOVER =getIcon("editor/hover/redo.png");

    public ImageIcon GAME_EDITOR_ACTIVE()
    {
        return GAME_EDITOR_ACTIVE;
    }

    public ImageIcon GAME_EDITOR_INACTIVE()
    {
        return GAME_EDITOR_INACTIVE;
    }

    public ImageIcon GAME_EDITOR_HOVER()
    {
        return GAME_EDITOR_HOVER;
    }

    public ImageIcon DEFINE_EDITOR_ACTIVE()
    {
        return DEFINE_EDITOR_ACTIVE;
    }

    public ImageIcon DEFINE_EDITOR_INACTIVE()
    {
        return DEFINE_EDITOR_INACTIVE;
    }

    public ImageIcon DEFINE_EDITOR_HOVER()
    {
        return DEFINE_EDITOR_HOVER;
    }

    public ImageIcon TEXT_EDITOR_ACTIVE()
    {
        return TEXT_EDITOR_ACTIVE;
    }

    public ImageIcon TEXT_EDITOR_INACTIVE()
    {
        return TEXT_EDITOR_INACTIVE;
    }

    public ImageIcon TEXT_EDITOR_HOVER()
    {
        return TEXT_EDITOR_HOVER;
    }

    public ImageIcon SELECT_ACTIVE()
    {
        return SELECT_ACTIVE;
    }

    public ImageIcon SELECT_INACTIVE()
    {
        return SELECT_INACTIVE;
    }

    public ImageIcon SELECT_HOVER()
    {
        return SELECT_HOVER;
    }

    public ImageIcon UNDO_ACTIVE()
    {
        return UNDO_ACTIVE;
    }

    public ImageIcon UNDO_INACTIVE()
    {
        return UNDO_INACTIVE;
    }

    public ImageIcon UNDO_HOVER()
    {
        return UNDO_HOVER;
    }

    public ImageIcon REDO_ACTIVE()
    {
        return REDO_ACTIVE;
    }

    public ImageIcon REDO_INACTIVE()
    {
        return REDO_INACTIVE;
    }

    public ImageIcon REDO_HOVER()
    {
        return REDO_HOVER;
    }

    private static final Color HEADER_BUTTON_ACTIVE_COLOR = new Color(69, 69, 69);
    private static final Color HEADER_BUTTON_INACTIVE_COLOR = new Color(165,165,165);
    private static final Color HEADER_BUTTON_HOVER_COLOR = new Color(127,191,255);

    public Color HEADER_BUTTON_ACTIVE_COLOR()
    {
        return HEADER_BUTTON_ACTIVE_COLOR;
    }

    public Color HEADER_BUTTON_INACTIVE_COLOR()
    {
        return HEADER_BUTTON_INACTIVE_COLOR;
    }

    public Color HEADER_BUTTON_HOVER_COLOR()
    {
        return HEADER_BUTTON_HOVER_COLOR;
    }

    // LUDEME BLOCK //
    private static final ImageIcon CHOICE_ICON_ACTIVE = getIcon("node/active/choice.png");
    private static final ImageIcon CHOICE_ICON_HOVER = getIcon("node/hover/choice.png");
    private static final ImageIcon COLLECTION_ICON_ACTIVE = getIcon("node/active/collection_add.png");
    private static final ImageIcon COLLECTION_ICON_HOVER = getIcon("node/hover/collection_add.png");
    private static final ImageIcon COLLECTION_REMOVE_ICON_HOVER = getIcon("node/hover/collection_remove.png");
    private static final ImageIcon COLLECTION_REMOVE_ICON_ACTIVE = getIcon("node/active/collection_remove.png");
    private static final ImageIcon OPTIONAL_ICON_ACTIVE =getIcon("node/active/optional.png");
    private static final ImageIcon OPTIONAL_ICON_HOVER = getIcon("node/hover/optional.png");
    private static final ImageIcon DOWN_ICON = getIcon("node/active/down.png");

    private static final ImageIcon UNCOLLAPSE_ICON = getIcon("node/active/uncollapse.png");
    public static final ImageIcon UNCOLLAPSE_ICON_HOVER = getIcon("node/hover/uncollapse.png");

    private static final ImageIcon COLLAPSE_ICON = getIcon("popup/collapse.png");

    public ImageIcon CHOICE_ICON_ACTIVE()
    {
        return CHOICE_ICON_ACTIVE;
    }

    public ImageIcon CHOICE_ICON_HOVER()
    {
        return CHOICE_ICON_HOVER;
    }

    public ImageIcon COLLECTION_ICON_ACTIVE()
    {
        return COLLECTION_ICON_ACTIVE;
    }

    public ImageIcon COLLECTION_ICON_HOVER()
    {
        return COLLECTION_ICON_HOVER;
    }

    public ImageIcon COLLECTION_REMOVE_ICON_ACTIVE()
    {
        return COLLECTION_REMOVE_ICON_ACTIVE;
    }

    public ImageIcon COLLECTION_REMOVE_ICON_HOVER()
    {
        return COLLECTION_REMOVE_ICON_HOVER;
    }

    public ImageIcon OPTIONAL_ICON_ACTIVE()
    {
        return OPTIONAL_ICON_ACTIVE;
    }

    public ImageIcon OPTIONAL_ICON_HOVER()
    {
        return OPTIONAL_ICON_HOVER;
    }

    public ImageIcon DOWN_ICON()
    {
        return DOWN_ICON;
    }

    public ImageIcon COLLAPSE_ICON()
    {
        return COLLAPSE_ICON;
    }

    public ImageIcon UNCOLLAPSE_ICON()
    {
        return UNCOLLAPSE_ICON;
    }

    public ImageIcon COLLAPSE_ICON_HOVER()
    {
        return COLLAPSE_ICON;
    }

    public ImageIcon UNCOLLAPSE_ICON_HOVER()
    {
        return UNCOLLAPSE_ICON_HOVER;
    }

    public static final ImageIcon DELETE_ICON = getIcon("popup/delete.png");
    public static final ImageIcon COPY_ICON = getIcon("popup/copy.png");
    public static final ImageIcon PASTE_ICON = getIcon("popup/paste.png");
    public static final ImageIcon DUPLICATE_ICON = getIcon("popup/duplicate.png");
    public static final ImageIcon ADD_ICON = getIcon("popup/add.png");



    // ~~ STROKES AND BORDERS ~~ //
    private static Border LUDEME_NODE_BORDER = BorderFactory.createLineBorder(LUDEME_BORDER_COLOR(), NODE_BORDER_WIDTH);
    private static Border LUDEME_NODE_BORDER_SELECTED = BorderFactory.createLineBorder(LUDEME_SELECTION_COLOR(), NODE_BORDER_WIDTH);
    private static Border LUDEME_NODE_BORDER_UNCOMPILABLE = BorderFactory.createLineBorder(LUDEME_UNCOMPILABLE_COLOR(), NODE_BORDER_WIDTH);

    public Border LUDEME_NODE_BORDER()
    {
        return LUDEME_NODE_BORDER;
    }

    public Border LUDEME_NODE_BORDER_SELECTED()
    {
        return LUDEME_NODE_BORDER_SELECTED;
    }

    public Border LUDEME_NODE_BORDER_UNCOMPILABLE()
    {
        return LUDEME_NODE_BORDER_UNCOMPILABLE;
    }


    public static BasicStroke LUDEME_EDGE_STROKE = new BasicStroke(CONNECTION_STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    public static EmptyBorder INPUT_AREA_PADDING_BORDER = new EmptyBorder(0,0,DesignPalette.INPUTAREA_PADDING_BOTTOM,0);
    public static EmptyBorder HEADER_PADDING_BORDER = new EmptyBorder(DesignPalette.HEADER_PADDING_TOP,0,DesignPalette.HEADER_PADDING_BOTTOM,0);


    private static URL getIconURL(String path)
    {
        return DesignPalette.class.getResource("/visual_editor/"+path);
    }

    private static ImageIcon getIcon(String path)
    {
        return new ImageIcon(getIconURL(path));
    }

}