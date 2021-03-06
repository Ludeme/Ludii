package app.display.dialogs.visual_editor.view;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.Objects;

/**
 * Stores colors and fonts for objects in the visual editor
 * @author Filipp Dokienko
 */

public class DesignPalette
{


    public static final Dimension DEFAULT_FRAME_SIZE = new Dimension(1200,800);

    public static float SCALAR = 1f;

    public static final float MAX_SCALAR = 2.5f;
    public static final float MIN_SCALAR = 0.3f;

    private static final int DEFAULT_NODE_WIDTH = 250;
    public static int NODE_WIDTH = (int) (DEFAULT_NODE_WIDTH * SCALAR);

    private static final int DEFAULT_LUDEME_INPUT_FONT_SIZE = 12;
    private static int LUDEME_INPUT_FONT_SIZE = (int) (DEFAULT_LUDEME_INPUT_FONT_SIZE * (1.0/SCALAR));

    private static final int DEFAULT_LUDEME_TITLE_FONT_SIZE = 14;
    private static int LUDEME_TITLE_FONT_SIZE = (int) (DEFAULT_LUDEME_TITLE_FONT_SIZE * SCALAR);

    private static final int DEFAULT_INPUTAREA_PADDING_BOTTOM = 10;
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
    public static int BACKGROUND_DOT_DIAMETER = (int) (DEFAULT_BACKGROUND_DOT_DIAMETER * SCALAR);

    private static final int DEFAULT_BACKGROUND_DOT_PADDING = 25;
    public static int BACKGROUND_DOT_PADDING = (int) (DEFAULT_BACKGROUND_DOT_PADDING * SCALAR);


    public static void scale(float scalar)
    {
        SCALAR *= scalar;
        SCALAR = (float) Math.min(2.0, Math.max(0.85, SCALAR));
        System.out.println("[SCALING] SCALAR: " + SCALAR);
        NODE_WIDTH = (int) (DEFAULT_NODE_WIDTH * (1.0/SCALAR));
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

        LUDEME_TITLE_FONT = new Font("Roboto Bold", Font.PLAIN,  LUDEME_TITLE_FONT_SIZE);
        LUDEME_INPUT_FONT = new Font("Robot Regular", Font.PLAIN, LUDEME_INPUT_FONT_SIZE);
        LUDEME_EDGE_STROKE = new BasicStroke(CONNECTION_STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        LUDEME_NODE_BORDER = BorderFactory.createLineBorder(DesignPalette.LUDEME_BORDER_COLOR, NODE_BORDER_WIDTH);
        LUDEME_NODE_BORDER_SELECTED = BorderFactory.createLineBorder(DesignPalette.LUDEME_SELECTION_COLOR, NODE_BORDER_WIDTH);

        INPUT_AREA_PADDING_BORDER = new EmptyBorder(0,0,DesignPalette.INPUTAREA_PADDING_BOTTOM,0);
        HEADER_PADDING_BORDER = new EmptyBorder(DesignPalette.HEADER_PADDING_TOP,0,DesignPalette.HEADER_PADDING_BOTTOM,0);
    }


    // ~~ COLORS ~~ //

    // PANELS //
    public static Color BACKGROUND_EDITOR = new Color(244,244,244);
    public static Color BACKGROUND_VISUAL_HELPER = new Color(207,207,207);

    // LUDEME BLOCK //
        // fonts
    public static Color FONT_LUDEME_INPUTS_COLOR = new Color(123,123,123);
    public static Color FONT_LUDEME_TITLE_COLOR = new Color(29,29,29);
        // backgrounds
    public static Color BACKGROUND_LUDEME_BODY = new Color(253,253,253);


         // there are 3 classes: game.equipment, game.functions, and game.rules
                    // game.rules: game.rules.play, game.rules.start, game.rules.end
                    // game.functions: .region, .ints, .graph, .floats, .dim, .booleans

    public static Color BACKGROUND_LUDEME_BODY_EQUIPMENT = new Color(255, 249, 242);
    public static Color BACKGROUND_LUDEME_BODY_FUNCTIONS = new Color(242, 255, 254);
    public static Color BACKGROUND_LUDEME_BODY_RULES = new Color(253, 247, 255);


    public static Color LUDEME_BORDER_COLOR = new Color(233,233,233);
    public static Color LUDEME_SELECTION_COLOR = new Color(92, 150, 242);
        // fills
    public static Color LUDEME_CONNECTION_POINT = new Color(127,191,255);//new Color(112,112,112);
    public static Color LUDEME_CONNECTION_POINT_INACTIVE = new Color(238,60,60);
    public static Color LUDEME_CONNECTION_EDGE = new Color(127,191,255);//new Color(112,112,112);

    // ~~ FONTS ~~ //
    public static void initializeFonts() {
        // load fonts
        registerAllFonts("Roboto");
    }

    // LUDEME BLOCK //
    public static Font LUDEME_TITLE_FONT = new Font("Roboto Bold", Font.PLAIN,  LUDEME_TITLE_FONT_SIZE);
    public static Font LUDEME_INPUT_FONT = new Font("Robot Regular", Font.PLAIN, LUDEME_INPUT_FONT_SIZE);
    public static Font LUDEME_INPUT_FONT_ITALIC = new Font("Roboto Italic", Font.ITALIC, LUDEME_INPUT_FONT_SIZE);

    // ~~ ICONS ~~ //

    // FRAME //
    public static final ImageIcon LUDII_ICON = new ImageIcon(DesignPalette.class.getResource("/ludii-logo-64x64.png"));
    // HEADER EDITORS //
    public static final ImageIcon GAME_EDITOR_ACTIVE = getIcon("editor/active/game_editor.png");
    public static final ImageIcon GAME_EDITOR_INACTIVE = getIcon("editor/inactive/game_editor.png");
    public static final ImageIcon GAME_EDITOR_HOVER = getIcon("editor/hover/game_editor.png");

    public static final ImageIcon DEFINE_EDITOR_ACTIVE = getIcon("editor/active/define_editor.png");
    public static final ImageIcon DEFINE_EDITOR_INACTIVE = getIcon("editor/inactive/define_editor.png");
    public static final ImageIcon DEFINE_EDITOR_HOVER = getIcon("editor/hover/define_editor.png");

    public static final ImageIcon TEXT_EDITOR_ACTIVE = getIcon("editor/active/text_editor.png");
    public static final ImageIcon TEXT_EDITOR_INACTIVE = getIcon("editor/inactive/text_editor.png");
    public static final ImageIcon TEXT_EDITOR_HOVER = getIcon("editor/hover/text_editor.png");
    // HEADER TOOLS //
    public static final ImageIcon SELECT_ACTIVE = getIcon("editor/active/select.png");
    public static final ImageIcon SELECT_INACTIVE = getIcon("editor/inactive/select.png");
    public static final ImageIcon SELECT_HOVER = getIcon("editor/hover/select.png");

    public static final ImageIcon UNDO_ACTIVE = getIcon("editor/active/undo.png");
    public static final ImageIcon UNDO_INACTIVE = getIcon("editor/inactive/undo.png");
    public static final ImageIcon UNDO_HOVER = getIcon("editor/hover/undo.png");

    public static final ImageIcon REDO_ACTIVE = getIcon("editor/active/redo.png");
    public static final ImageIcon REDO_INACTIVE = getIcon("editor/inactive/redo.png");
    public static final ImageIcon REDO_HOVER =getIcon("editor/hover/redo.png");

    // LUDEME BLOCK //
    public static final ImageIcon CHOICE_ICON_ACTIVE = getIcon("node/active/choice.png");
    public static final ImageIcon CHOICE_ICON_HOVER = getIcon("node/hover/choice.png");
    public static final ImageIcon COLLECTION_ICON_ACTIVE = getIcon("node/active/collection_add.png");
    public static final ImageIcon COLLECTION_ICON_HOVER = getIcon("node/hover/collection_add.png");
    public static final ImageIcon COLLECTION_REMOVE_ICON_HOVER = getIcon("node/hover/collection_remove.png");
    public static final ImageIcon COLLECTION_REMOVE_ICON_ACTIVE = getIcon("node/active/collection_remove.png");
    public static final ImageIcon OPTIONAL_ICON_ACTIVE =getIcon("node/active/optional.png");
    public static final ImageIcon OPTIONAL_ICON_HOVER = getIcon("node/hover/optional.png");
    public static final ImageIcon DOWN_ICON = getIcon("node/active/down.png");

    //public static final ImageIcon COLLAPSE_ICON = getIcon("node/active/collapse.png");
    // static final ImageIcon COLLAPSE_ICON_HOVER = getIcon("node/hover/collapse.png");

    public static final ImageIcon UNCOLLAPSE_ICON = getIcon("node/active/uncollapse.png");
    public static final ImageIcon UNCOLLAPSE_ICON_HOVER = getIcon("node/hover/uncollapse.png");

    public static final ImageIcon COLLAPSE_ICON = getIcon("popup/collapse.png");
    public static final ImageIcon DELETE_ICON = getIcon("popup/delete.png");
    public static final ImageIcon COPY_ICON = getIcon("popup/copy.png");
    public static final ImageIcon PASTE_ICON = getIcon("popup/paste.png");
    public static final ImageIcon DUPLICATE_ICON = getIcon("popup/duplicate.png");
    public static final ImageIcon ADD_ICON = getIcon("popup/add.png");



    // ~~ STROKES AND BORDERS ~~ //
    public static BasicStroke LUDEME_EDGE_STROKE = new BasicStroke(CONNECTION_STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    public static Border LUDEME_NODE_BORDER = BorderFactory.createLineBorder(DesignPalette.LUDEME_BORDER_COLOR, NODE_BORDER_WIDTH);
    public static Border LUDEME_NODE_BORDER_SELECTED = BorderFactory.createLineBorder(DesignPalette.LUDEME_SELECTION_COLOR, NODE_BORDER_WIDTH);

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

    private static void registerAllFonts(String folderName)
    {
        String extension = ".ttf";
        try
        {
            File dir = new File(DesignPalette.class.getResource("/fonts/"+folderName).toURI());
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            for (File file : Objects.requireNonNull(dir.listFiles()))
            {
                if (file.getName().endsWith(extension))
                {
                    System.out.println("Registering font: " + file.getName());
                    ge.registerFont(Font.createFont(Font.TRUETYPE_FONT,file));
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}