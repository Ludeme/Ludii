package app.display.dialogs.visual_editor.view.designPalettes;

import app.display.dialogs.visual_editor.handler.Handler;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.net.URISyntaxException;
import java.util.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Stores colors and fonts for objects in the visual editor
 * @author Filipp Dokienko
 */

public class DesignPalette
{

    private static String name = "";
    private static DesignPalette instance = null;

    public static final Dimension DEFAULT_FRAME_SIZE = new Dimension(1200,800);
    public static final Dimension DEFAULT_GRAPHPANEL_SIZE = new Dimension(10000,10000);

    public static float SCALAR = 1f;

    public static final float MAX_SCALAR = 2.5f;
    public static final float MIN_SCALAR = 0.3f;





    // SIZES ================================================================
    private static int DEFAULT_NODE_WIDTH = 220; // small: 200, default: 220, bigger: 250 big: 250
    private static int DEFAULT_TERMINAL_INPUT_HEIGHT = 20; // small: 17, default, bigger: 20, big: 24

    private static int DEFAULT_LUDEME_TITLE_FONT_SIZE = 14; // small: 10, default, bigger 14, big: 18
    private static int DEFAULT_LUDEME_INPUT_FONT_SIZE = 13; // small: 10, default, bigger: 13, big: 16


    public static void makeSizeSmall()
    {
        DEFAULT_NODE_WIDTH = 180;
        DEFAULT_TERMINAL_INPUT_HEIGHT = 17;
        DEFAULT_LUDEME_TITLE_FONT_SIZE = 10;
        DEFAULT_LUDEME_INPUT_FONT_SIZE = 10;
        SCALAR = 1f;
        scale(SCALAR);
    }

    public static void makeSizeMedium()
    {
        DEFAULT_NODE_WIDTH = 220;
        DEFAULT_TERMINAL_INPUT_HEIGHT = 20;
        DEFAULT_LUDEME_TITLE_FONT_SIZE = 14;
        DEFAULT_LUDEME_INPUT_FONT_SIZE = 13;
        SCALAR = 1f;
        scale(SCALAR);
    }

    public static void makeSizeLarge()
    {
        DEFAULT_NODE_WIDTH = 250;
        DEFAULT_TERMINAL_INPUT_HEIGHT = 24;
        DEFAULT_LUDEME_TITLE_FONT_SIZE = 18;
        DEFAULT_LUDEME_INPUT_FONT_SIZE = 16;
        SCALAR = 1f;
        scale(SCALAR);
    }


    public static int NODE_WIDTH = (int) (DEFAULT_NODE_WIDTH * SCALAR);
    public static int TERMINAL_INPUT_HEIGHT = (int) (DEFAULT_TERMINAL_INPUT_HEIGHT * SCALAR);
    private static int LUDEME_INPUT_FONT_SIZE = (int) (DEFAULT_LUDEME_INPUT_FONT_SIZE * (1.0/SCALAR));
    public static int LUDEME_TITLE_FONT_SIZE = (int) (DEFAULT_LUDEME_TITLE_FONT_SIZE * SCALAR);

    // COLOURS ================================================================

    private static final String PALETTE_FILE_PATH = "/lve_palettes/";
    private static final String DEFAULT_PALETTE_FILE_NAME = "Pastel";

    // PANELS //
    private static Color BACKGROUND_EDITOR;
    private static Color BACKGROUND_VISUAL_HELPER;
    private static Color BACKGROUND_HEADER_PANEL;

    // NODE BACKGROUNDS //
    private static Color BACKGROUND_LUDEME_BODY;
    private static Color BACKGROUND_LUDEME_BODY_EQUIPMENT;
    private static Color BACKGROUND_LUDEME_BODY_FUNCTIONS;
    private static Color BACKGROUND_LUDEME_BODY_RULES;
    private static Color BACKGROUND_LUDEME_BODY_DEFINE;

    private static Color INPUT_FIELD_BACKGROUND;

    // NODE BORDERS //
    private static Color LUDEME_BORDER_COLOR;
    private static Color LUDEME_SELECTION_COLOR;
    private static Color LUDEME_UNCOMPILABLE_COLOR;
    private static Color INPUT_FIELD_BORDER_COLOUR;

    // CONNECTIONS
    private static Color LUDEME_CONNECTION_POINT;
    private static Color LUDEME_CONNECTION_POINT_INACTIVE;
    private static Color LUDEME_CONNECTION_EDGE;

    // FONTS
    private static Color FONT_LUDEME_TITLE_COLOR;
    private static Color FONT_LUDEME_INPUTS_COLOR;
    private static Color INPUT_FIELD_FOREGROUND;


    // PLAY BUTTON
    private static Color PLAY_BUTTON_FOREGROUND ;
    private static Color COMPILABLE_COLOR;
    private static Color NOT_COMPILABLE_COLOR;


    // TOOL PANEL
    private static Color HEADER_BUTTON_ACTIVE_COLOR;
    private static Color HEADER_BUTTON_INACTIVE_COLOR;
    private static Color HEADER_BUTTON_HOVER_COLOR = new Color(127,191,255);



    public DesignPalette()
    {
        loadPalette(DEFAULT_PALETTE_FILE_NAME);
    }

    public static DesignPalette instance()
    {
        if (instance == null)
            instance = new DesignPalette();
        return instance;
    }

    public static List<String> palettes()
    {
        List<String> names = new ArrayList<>();
        try
        {
            File dir = new File(DesignPalette.class.getResource(PALETTE_FILE_PATH).toURI());
            for (File file : dir.listFiles())
            {
                if(file.getName().endsWith(".json"))
                {
                    names.add(file.getName().replace(".json", ""));
                }
            }
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
        return names;
    }

    public static void loadPalette(String paletteName)
    {
        System.out.println("Loading palette: " + paletteName);
        // read json
        JSONObject palette = readPaletteJSON(paletteName);
        if(palette == null)
        {
            System.out.println("Palette not found");
            return;
        }
        // set colours
        setColours(palette.toMap());
    }

    private static JSONObject readPaletteJSON(String paletteName)
    {
        InputStream is = DesignPalette.class.getResourceAsStream(PALETTE_FILE_PATH + paletteName + ".json");
        if(is == null)
            return null;
        JSONObject palette = new JSONObject(new JSONTokener(is));
        return palette;
    }

    private static void setColours(Map<String, Object> palette)
    {
        for(Map.Entry<String, Object> entry : palette.entrySet())
        {
            String key = entry.getKey();
            String value = (String) entry.getValue();

            if(key.equals("name"))
            {
                name = value;
            }
            else if(key.equals("icons"))
            {
                setIcons(value);
            }
            else
            {
                setColour(key, Color.decode(value));
            }
        }
    }

    private static void setColour(String name, Color colour)
    {
        switch(name.toLowerCase())
        {
            case "editor_background":
                BACKGROUND_EDITOR = colour;
                break;
            case "background_grid_colour":
                BACKGROUND_VISUAL_HELPER = colour;
                break;
            case "header_background":
                BACKGROUND_HEADER_PANEL = colour;
                break;
            case "default_node_background":
                BACKGROUND_LUDEME_BODY = colour;
                break;
            case "rule_node_background":
                BACKGROUND_LUDEME_BODY_RULES = colour;
                break;
            case "equipment_node_background":
                BACKGROUND_LUDEME_BODY_EQUIPMENT = colour;
                break;
            case "functions_node_background":
                BACKGROUND_LUDEME_BODY_FUNCTIONS = colour;
                break;
            case "define_node_background":
                BACKGROUND_LUDEME_BODY_DEFINE = colour;
                break;
            case "input_field_background":
                INPUT_FIELD_BACKGROUND = colour;
                break;
            case "connection_point_colour":
                LUDEME_CONNECTION_POINT = colour;
                break;
            case "connection_point_inactive_colour":
                LUDEME_CONNECTION_POINT_INACTIVE = colour;
                break;
            case "edge_colour":
                LUDEME_CONNECTION_EDGE = colour;
                break;
            case "play_button_background":
                COMPILABLE_COLOR = colour;
                break;
            case "uncompilable_button_background":
                NOT_COMPILABLE_COLOR = colour;
                break;
            case "play_button_font":
                PLAY_BUTTON_FOREGROUND = colour;
                break;
            case "node_border":
                LUDEME_BORDER_COLOR = colour;
                LUDEME_NODE_BORDER = BorderFactory.createLineBorder(LUDEME_BORDER_COLOR(), NODE_BORDER_WIDTH);
                break;
            case "node_selected_border":
                LUDEME_SELECTION_COLOR = colour;
                LUDEME_NODE_BORDER_SELECTED = BorderFactory.createLineBorder(LUDEME_SELECTION_COLOR(), NODE_BORDER_WIDTH);
                break;
            case "node_uncompilable_border":
                LUDEME_UNCOMPILABLE_COLOR = colour;
                LUDEME_NODE_BORDER_UNCOMPILABLE = BorderFactory.createLineBorder(LUDEME_UNCOMPILABLE_COLOR(), NODE_BORDER_WIDTH);
                break;
            case "node_title_font":
                FONT_LUDEME_TITLE_COLOR = colour;
                break;
            case "node_body_font":
                FONT_LUDEME_INPUTS_COLOR = colour;
                break;
            case "input_field_border":
                INPUT_FIELD_BORDER_COLOUR = colour;
                break;
            case "input_field_font":
                INPUT_FIELD_FOREGROUND = colour;
                break;
            case "tool_panel_active_font":
                HEADER_BUTTON_ACTIVE_COLOR = colour;
                break;
            case "tool_panel_inactive_font":
                HEADER_BUTTON_INACTIVE_COLOR = colour;
                break;
            default:
                System.out.println("Colour not found : " + name);
                break;
        }
    }

    private static void setIcons(String style)
    {
        switch(style)
        {
            case "dark":
                CHOICE_ICON_ACTIVE = getIcon("node/dark/active/choice.png");
                COLLECTION_ICON_ACTIVE = getIcon("node/dark/active/collection_add.png");
                COLLECTION_REMOVE_ICON_ACTIVE = getIcon("node/dark/active/collection_remove.png");
                DOWN_ICON = getIcon("node/dark/active/down.png");
                UNCOLLAPSE_ICON = getIcon("node/dark/active/uncollapse.png");
                COLLAPSE_ICON = getIcon("node/dark/active/collapse.png");

                GAME_EDITOR_INACTIVE = getIcon("editor/active/game_editor.png");
                GAME_EDITOR_ACTIVE = getIcon("editor/inactive/game_editor.png");
                DEFINE_EDITOR_INACTIVE = getIcon("editor/active/define_editor.png");
                DEFINE_EDITOR_ACTIVE = getIcon("editor/inactive/define_editor.png");
                TEXT_EDITOR_INACTIVE = getIcon("editor/active/text_editor.png");
                TEXT_EDITOR_ACTIVE = getIcon("editor/inactive/text_editor.png");
                SELECT_INACTIVE = getIcon("editor/active/select.png");
                SELECT_ACTIVE = getIcon("editor/inactive/select.png");
                UNDO_INACTIVE = getIcon("editor/active/undo.png");
                UNDO_ACTIVE = getIcon("editor/inactive/undo.png");
                REDO_INACTIVE = getIcon("editor/active/redo.png");
                REDO_ACTIVE = getIcon("editor/inactive/redo.png");

                break;
            case "light": ;
            default:
                CHOICE_ICON_ACTIVE = getIcon("node/active/choice.png");
                COLLECTION_ICON_ACTIVE = getIcon("node/active/collection_add.png");
                COLLECTION_REMOVE_ICON_ACTIVE = getIcon("node/active/collection_remove.png");
                DOWN_ICON = getIcon("node/active/down.png");
                UNCOLLAPSE_ICON = getIcon("node/active/uncollapse.png");
                COLLAPSE_ICON = getIcon("node/active/collapse.png");

                GAME_EDITOR_ACTIVE = getIcon("editor/active/game_editor.png");
                GAME_EDITOR_INACTIVE = getIcon("editor/inactive/game_editor.png");
                DEFINE_EDITOR_ACTIVE = getIcon("editor/active/define_editor.png");
                DEFINE_EDITOR_INACTIVE = getIcon("editor/inactive/define_editor.png");
                TEXT_EDITOR_ACTIVE = getIcon("editor/active/text_editor.png");
                TEXT_EDITOR_INACTIVE = getIcon("editor/inactive/text_editor.png");
                SELECT_ACTIVE = getIcon("editor/active/select.png");
                SELECT_INACTIVE = getIcon("editor/inactive/select.png");
                UNDO_ACTIVE = getIcon("editor/active/undo.png");
                UNDO_INACTIVE = getIcon("editor/inactive/undo.png");
                REDO_ACTIVE = getIcon("editor/active/redo.png");
                REDO_INACTIVE = getIcon("editor/inactive/redo.png");
                break;
        }
    }

    public String name()
    {
        return name;
    }






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


    public static void scale(float scalar)
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
        LUDEME_NODE_BORDER = BorderFactory.createLineBorder(DesignPalette.LUDEME_BORDER_COLOR(), NODE_BORDER_WIDTH);
        LUDEME_NODE_BORDER_SELECTED = BorderFactory.createLineBorder(DesignPalette.LUDEME_SELECTION_COLOR(), NODE_BORDER_WIDTH);
        LUDEME_NODE_BORDER_UNCOMPILABLE = BorderFactory.createLineBorder(DesignPalette.LUDEME_UNCOMPILABLE_COLOR(), NODE_BORDER_WIDTH);

        INPUT_AREA_PADDING_BORDER = new EmptyBorder(0,0,DesignPalette.INPUTAREA_PADDING_BOTTOM,0);
        HEADER_PADDING_BORDER = new EmptyBorder(DesignPalette.HEADER_PADDING_TOP,0,DesignPalette.HEADER_PADDING_BOTTOM,0);

    }


    public static Color BACKGROUND_EDITOR()
    {
        return BACKGROUND_EDITOR;
    }

    public static Color BACKGROUND_VISUAL_HELPER()
    {
        return BACKGROUND_VISUAL_HELPER;
    }

    public static Color BACKGROUND_HEADER_PANEL()
    {
        return BACKGROUND_HEADER_PANEL;
    }

    public static Color FONT_LUDEME_INPUTS_COLOR()
    {
        return FONT_LUDEME_INPUTS_COLOR;
    }

    public static Color FONT_LUDEME_TITLE_COLOR()
    {
        return FONT_LUDEME_TITLE_COLOR;
    }

    public static Color BACKGROUND_LUDEME_BODY()
    {
        return BACKGROUND_LUDEME_BODY;
    }

    public static Color BACKGROUND_LUDEME_BODY_EQUIPMENT()
    {
        return BACKGROUND_LUDEME_BODY_EQUIPMENT;
    }

    public static Color BACKGROUND_LUDEME_BODY_FUNCTIONS()
    {
        return BACKGROUND_LUDEME_BODY_FUNCTIONS;
    }

    public static Color BACKGROUND_LUDEME_BODY_RULES()
    {
        return BACKGROUND_LUDEME_BODY_RULES;
    }

    public static Color BACKGROUND_LUDEME_BODY_DEFINE()
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

    public static Color LUDEME_CONNECTION_POINT()
    {
        return LUDEME_CONNECTION_POINT;
    }

    public static Color LUDEME_CONNECTION_POINT_INACTIVE()
    {
        return LUDEME_CONNECTION_POINT_INACTIVE;
    }

    public static Color LUDEME_CONNECTION_EDGE()
    {
        return LUDEME_CONNECTION_EDGE;
    }


    public static Color COMPILABLE_COLOR()
    {
        return COMPILABLE_COLOR;
    }

    public static Color NOT_COMPILABLE_COLOR()
    {
        return NOT_COMPILABLE_COLOR;
    }

    public static Color PLAY_BUTTON_FOREGROUND()
    {
        return PLAY_BUTTON_FOREGROUND;
    }

    public static Color INPUT_FIELD_BACKGROUND()
    {
        return INPUT_FIELD_BACKGROUND;
    }

    public static Color INPUT_FIELD_BORDER_COLOUR()
    {
        return INPUT_FIELD_BORDER_COLOUR;
    }

    public static Color INPUT_FIELD_FOREGROUND()
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

    private static ImageIcon GAME_EDITOR_ACTIVE = getIcon("editor/active/game_editor.png");
    private static ImageIcon GAME_EDITOR_INACTIVE = getIcon("editor/inactive/game_editor.png");
    private static final ImageIcon GAME_EDITOR_HOVER = getIcon("editor/hover/game_editor.png");

    private static ImageIcon DEFINE_EDITOR_ACTIVE = getIcon("editor/active/define_editor.png");
    private static ImageIcon DEFINE_EDITOR_INACTIVE = getIcon("editor/inactive/define_editor.png");
    private static final ImageIcon DEFINE_EDITOR_HOVER = getIcon("editor/hover/define_editor.png");

    private static ImageIcon TEXT_EDITOR_ACTIVE = getIcon("editor/active/text_editor.png");
    private static ImageIcon TEXT_EDITOR_INACTIVE = getIcon("editor/inactive/text_editor.png");
    private static final ImageIcon TEXT_EDITOR_HOVER = getIcon("editor/hover/text_editor.png");
    // HEADER TOOLS //
    private static ImageIcon SELECT_ACTIVE = getIcon("editor/active/select.png");
    private static ImageIcon SELECT_INACTIVE = getIcon("editor/inactive/select.png");
    private static final ImageIcon SELECT_HOVER = getIcon("editor/hover/select.png");

    private static ImageIcon UNDO_ACTIVE = getIcon("editor/active/undo.png");
    private static ImageIcon UNDO_INACTIVE = getIcon("editor/inactive/undo.png");
    private static final ImageIcon UNDO_HOVER = getIcon("editor/hover/undo.png");

    private static ImageIcon REDO_ACTIVE = getIcon("editor/active/redo.png");
    private static ImageIcon REDO_INACTIVE = getIcon("editor/inactive/redo.png");
    private static final ImageIcon REDO_HOVER = getIcon("editor/hover/redo.png");

    public static ImageIcon GAME_EDITOR_ACTIVE()
    {
        return GAME_EDITOR_ACTIVE;
    }

    public static ImageIcon GAME_EDITOR_INACTIVE()
    {
        return GAME_EDITOR_INACTIVE;
    }

    public static ImageIcon GAME_EDITOR_HOVER()
    {
        return GAME_EDITOR_HOVER;
    }

    public static ImageIcon DEFINE_EDITOR_ACTIVE()
    {
        return DEFINE_EDITOR_ACTIVE;
    }

    public static ImageIcon DEFINE_EDITOR_INACTIVE()
    {
        return DEFINE_EDITOR_INACTIVE;
    }

    public static ImageIcon DEFINE_EDITOR_HOVER()
    {
        return DEFINE_EDITOR_HOVER;
    }

    public static ImageIcon TEXT_EDITOR_ACTIVE()
    {
        return TEXT_EDITOR_ACTIVE;
    }

    public static ImageIcon TEXT_EDITOR_INACTIVE()
    {
        return TEXT_EDITOR_INACTIVE;
    }

    public static ImageIcon TEXT_EDITOR_HOVER()
    {
        return TEXT_EDITOR_HOVER;
    }

    public static ImageIcon SELECT_ACTIVE()
    {
        return SELECT_ACTIVE;
    }

    public static ImageIcon SELECT_INACTIVE()
    {
        return SELECT_INACTIVE;
    }

    public static ImageIcon SELECT_HOVER()
    {
        return SELECT_HOVER;
    }

    public static ImageIcon UNDO_ACTIVE()
    {
        return UNDO_ACTIVE;
    }

    public static ImageIcon UNDO_INACTIVE()
    {
        return UNDO_INACTIVE;
    }

    public static ImageIcon UNDO_HOVER()
    {
        return UNDO_HOVER;
    }

    public static ImageIcon REDO_ACTIVE()
    {
        return REDO_ACTIVE;
    }

    public static ImageIcon REDO_INACTIVE()
    {
        return REDO_INACTIVE;
    }

    public static ImageIcon REDO_HOVER()
    {
        return REDO_HOVER;
    }

    public static Color HEADER_BUTTON_ACTIVE_COLOR()
    {
        return HEADER_BUTTON_ACTIVE_COLOR;
    }

    public static Color HEADER_BUTTON_INACTIVE_COLOR()
    {
        return HEADER_BUTTON_INACTIVE_COLOR;
    }

    public static Color HEADER_BUTTON_HOVER_COLOR()
    {
        return HEADER_BUTTON_HOVER_COLOR;
    }

    // LUDEME BLOCK //
    private static ImageIcon CHOICE_ICON_ACTIVE = getIcon("node/active/choice.png");
    private static ImageIcon CHOICE_ICON_HOVER = getIcon("node/hover/choice.png");
    private static ImageIcon COLLECTION_ICON_ACTIVE = getIcon("node/active/collection_add.png");
    private static ImageIcon COLLECTION_ICON_HOVER = getIcon("node/hover/collection_add.png");
    private static ImageIcon COLLECTION_REMOVE_ICON_HOVER = getIcon("node/hover/collection_remove.png");
    private static ImageIcon COLLECTION_REMOVE_ICON_ACTIVE = getIcon("node/active/collection_remove.png");
    private static ImageIcon OPTIONAL_ICON_ACTIVE =getIcon("node/active/optional.png");
    private static ImageIcon OPTIONAL_ICON_HOVER = getIcon("node/hover/optional.png");
    private static ImageIcon DOWN_ICON = getIcon("node/active/down.png");

    private static ImageIcon UNCOLLAPSE_ICON = getIcon("node/active/uncollapse.png");
    public static ImageIcon UNCOLLAPSE_ICON_HOVER = getIcon("node/hover/uncollapse.png");

    private static ImageIcon COLLAPSE_ICON = getIcon("popup/collapse.png");

    public static ImageIcon CHOICE_ICON_ACTIVE()
    {
        return CHOICE_ICON_ACTIVE;
    }

    public static ImageIcon CHOICE_ICON_HOVER()
    {
        return CHOICE_ICON_HOVER;
    }

    public static ImageIcon COLLECTION_ICON_ACTIVE()
    {
        return COLLECTION_ICON_ACTIVE;
    }

    public static ImageIcon COLLECTION_ICON_HOVER()
    {
        return COLLECTION_ICON_HOVER;
    }

    public static ImageIcon COLLECTION_REMOVE_ICON_ACTIVE()
    {
        return COLLECTION_REMOVE_ICON_ACTIVE;
    }

    public static ImageIcon COLLECTION_REMOVE_ICON_HOVER()
    {
        return COLLECTION_REMOVE_ICON_HOVER;
    }

    public static ImageIcon OPTIONAL_ICON_ACTIVE()
    {
        return OPTIONAL_ICON_ACTIVE;
    }

    public static ImageIcon OPTIONAL_ICON_HOVER()
    {
        return OPTIONAL_ICON_HOVER;
    }

    public static ImageIcon DOWN_ICON()
    {
        return DOWN_ICON;
    }

    public static ImageIcon COLLAPSE_ICON()
    {
        return COLLAPSE_ICON;
    }

    public static ImageIcon UNCOLLAPSE_ICON()
    {
        return UNCOLLAPSE_ICON;
    }

    public static ImageIcon COLLAPSE_ICON_HOVER()
    {
        return COLLAPSE_ICON;
    }

    public static ImageIcon UNCOLLAPSE_ICON_HOVER()
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

    public static Border LUDEME_NODE_BORDER()
    {
        return LUDEME_NODE_BORDER;
    }

    public static Border LUDEME_NODE_BORDER_SELECTED()
    {
        return LUDEME_NODE_BORDER_SELECTED;
    }

    public static Border LUDEME_NODE_BORDER_UNCOMPILABLE()
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