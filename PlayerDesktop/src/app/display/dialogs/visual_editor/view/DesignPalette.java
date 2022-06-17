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
    private static int LUDEME_INPUT_FONT_SIZE = (int) (DEFAULT_LUDEME_INPUT_FONT_SIZE * SCALAR);

    private static final int DEFAULT_LUDEME_TITLE_FONT_SIZE = 14;
    private static int LUDEME_TITLE_FONT_SIZE = (int) (DEFAULT_LUDEME_TITLE_FONT_SIZE * SCALAR);

    private static final int DEFAULT_INPUTAREA_PADDING_BOTTOM = 10;
    public static int INPUTAREA_PADDING_BOTTOM = (int) (DEFAULT_INPUTAREA_PADDING_BOTTOM * SCALAR);

    private static final int DEFAULT_HEADER_PADDING_BOTTOM = 3;
    public static int HEADER_PADDING_BOTTOM = (int) (DEFAULT_HEADER_PADDING_BOTTOM * SCALAR);

    private static final int DEFAULT_HEADER_PADDING_TOP = 10;
    public static int HEADER_PADDING_TOP = (int) (DEFAULT_HEADER_PADDING_TOP * SCALAR);

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
        SCALAR = scalar;
        System.out.println("[SCALING] SCALAR: " + SCALAR);
        NODE_WIDTH = (int) (DEFAULT_NODE_WIDTH * SCALAR);
        LUDEME_INPUT_FONT_SIZE = (int) (DEFAULT_LUDEME_INPUT_FONT_SIZE * SCALAR);
        LUDEME_TITLE_FONT_SIZE = (int) (DEFAULT_LUDEME_TITLE_FONT_SIZE * SCALAR);
        INPUTAREA_PADDING_BOTTOM = (int) (DEFAULT_INPUTAREA_PADDING_BOTTOM * SCALAR);
        HEADER_PADDING_BOTTOM = (int) (DEFAULT_HEADER_PADDING_BOTTOM * SCALAR);
        HEADER_PADDING_TOP = (int) (DEFAULT_HEADER_PADDING_TOP * SCALAR);
        HEADER_TITLE_CONNECTION_SPACE = (int) (DEFAULT_HEADER_TITLE_CONNECTION_SPACE * SCALAR);
        CONNECTION_STROKE_WIDTH = DEFAULT_CONNECTION_STROKE_WIDTH * SCALAR;
        NODE_BORDER_WIDTH = (int) (DEFAULT_NODE_BORDER_WIDTH * SCALAR);
        BACKGROUND_DOT_DIAMETER = (int) (DEFAULT_BACKGROUND_DOT_DIAMETER * SCALAR);
        BACKGROUND_DOT_PADDING = (int) (DEFAULT_BACKGROUND_DOT_PADDING * SCALAR);

        LUDEME_TITLE_FONT = new Font("Roboto Bold", 0,  LUDEME_TITLE_FONT_SIZE);
        LUDEME_INPUT_FONT = new Font("Robot Regular", 0, LUDEME_INPUT_FONT_SIZE);
        LUDEME_EDGE_STROKE = new BasicStroke(CONNECTION_STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        LUDEME_NODE_BORDER = BorderFactory.createLineBorder(DesignPalette.LUDEME_BORDER_COLOR, NODE_BORDER_WIDTH);
        LUDEME_NODE_BORDER_SELECTED = BorderFactory.createLineBorder(DesignPalette.LUDEME_SELECTION_COLOR, NODE_BORDER_WIDTH);

        INPUT_AREA_PADDING_BORDER = new EmptyBorder(0,0,DesignPalette.INPUTAREA_PADDING_BOTTOM,0);
        HEADER_PADDING_BORDER = new EmptyBorder(DesignPalette.HEADER_PADDING_TOP,0,DesignPalette.HEADER_PADDING_BOTTOM,0);
    }


    // ~~ COLORS ~~ //

    // PANELS //
    public static Color BACKGROUND_EDITOR = new Color(244,244,244);;
    public static Color BACKGROUND_VISUAL_HELPER = new Color(207,207,207);

    // LUDEME BLOCK //
        // fonts
    public static Color FONT_LUDEME_INPUTS_COLOR = new Color(123,123,123);
    public static Color FONT_LUDEME_TITLE_COLOR = new Color(29,29,29);
        // backgrounds
    public static Color BACKGROUND_LUDEME_BODY = new Color(253,253,253);
    public static Color BACKGROUND_LUDEME_TITLE = new Color(253,253,253);
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
    public static Font LUDEME_TITLE_FONT = new Font("Roboto Bold", 0,  LUDEME_TITLE_FONT_SIZE);
    public static Font LUDEME_INPUT_FONT = new Font("Robot Regular", 0, LUDEME_INPUT_FONT_SIZE);
    public static Font LUDEME_INPUT_FONT_ITALIC = new Font("Roboto Italic", Font.ITALIC, LUDEME_INPUT_FONT_SIZE);

    // ~~ ICONS ~~ //

    // FRAME //
    public static final ImageIcon LUDII_ICON = new ImageIcon(getIconURL("logo-clover-c.png"));
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



    // ~~ STROKES AND BORDERS ~~ //
    public static BasicStroke LUDEME_EDGE_STROKE = new BasicStroke(CONNECTION_STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    public static Border LUDEME_NODE_BORDER = BorderFactory.createLineBorder(DesignPalette.LUDEME_BORDER_COLOR, NODE_BORDER_WIDTH);
    public static Border LUDEME_NODE_BORDER_SELECTED = BorderFactory.createLineBorder(DesignPalette.LUDEME_SELECTION_COLOR, NODE_BORDER_WIDTH);

    public static EmptyBorder INPUT_AREA_PADDING_BORDER = new EmptyBorder(0,0,DesignPalette.INPUTAREA_PADDING_BOTTOM,0);
    public static EmptyBorder HEADER_PADDING_BORDER = new EmptyBorder(DesignPalette.HEADER_PADDING_TOP,0,DesignPalette.HEADER_PADDING_BOTTOM,0);


    private static URL getIconURL(String path)
    {
        return DesignPalette.class.getResource("/icons/"+path);
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