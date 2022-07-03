package app.display.dialogs.visual_editor.view.designPalettes;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class DesignPaletteDark extends DesignPalette
{

    private static DesignPaletteDark instance = null;

    public static DesignPaletteDark instance()
    {
        if (instance == null)
        {
            instance = new DesignPaletteDark();
        }
        return instance;
    }

    // ~~ COLORS ~~ //

    // PANELS //
    private static Color BACKGROUND_EDITOR = new Color(36,41,46);
    private static Color BACKGROUND_VISUAL_HELPER = new Color(66,66,66);
    private static Color BACKGROUND_HEADER_PANEL = new Color(47,53,58);


    // LUDEME BLOCK //
    // fonts
    private static Color FONT_LUDEME_INPUTS_COLOR = new Color(146,146,146);
    private static Color FONT_LUDEME_TITLE_COLOR = new Color(146,146,146);


    // backgrounds
    private static Color BACKGROUND_LUDEME_BODY = new Color(44,50,56);

    // there are 3 classes: game.equipment, game.functions, and game.rules
    // game.rules: game.rules.play, game.rules.start, game.rules.end
    // game.functions: .region, .ints, .graph, .floats, .dim, .booleans

    private static Color BACKGROUND_LUDEME_BODY_EQUIPMENT = new Color(51, 46, 45);
    private static Color BACKGROUND_LUDEME_BODY_FUNCTIONS = new Color(44, 56, 56);
    private static Color BACKGROUND_LUDEME_BODY_RULES = new Color(47, 44, 56);
    private static Color LUDEME_BORDER_COLOR = new Color(87,87,87);
    private static Color LUDEME_SELECTION_COLOR = new Color(106, 129, 151);
    private static Color LUDEME_UNCOMPILABLE_COLOR = new Color(172,57,57);

    private static Color LUDEME_CONNECTION_POINT = new Color(106, 129, 151);
    private static Color LUDEME_CONNECTION_POINT_INACTIVE = new Color(172,57,57);
    private static Color LUDEME_CONNECTION_EDGE = new Color(106, 129, 151);
    private static final Color COMPILABLE_COLOR = new Color(214, 234, 255);
    private static final Color NOT_COMPILABLE_COLOR = new Color(255,214,214);

    private static final Color INPUT_FIELD_BACKGROUND = new Color(54, 59, 65);
    private static final Color INPUT_FIELD_BORDER_COLOUR = new Color(97,97,97);
    private static final Color INPUT_FIELD_FOREGROUND = new Color(150, 150, 150);

    @Override
    public Color BACKGROUND_EDITOR()
    {
        return BACKGROUND_EDITOR;
    }
    @Override
    public Color BACKGROUND_VISUAL_HELPER()
    {
        return BACKGROUND_VISUAL_HELPER;
    }

    public Color BACKGROUND_HEADER_PANEL()
    {
        return BACKGROUND_HEADER_PANEL;
    }
    @Override
    public Color FONT_LUDEME_INPUTS_COLOR()
    {
        return FONT_LUDEME_INPUTS_COLOR;
    }
    @Override
    public Color FONT_LUDEME_TITLE_COLOR()
    {
        return FONT_LUDEME_TITLE_COLOR;
    }
    @Override
    public Color BACKGROUND_LUDEME_BODY()
    {
        return BACKGROUND_LUDEME_BODY;
    }
    @Override
    public Color BACKGROUND_LUDEME_BODY_EQUIPMENT()
    {
        return BACKGROUND_LUDEME_BODY_EQUIPMENT;
    }
    @Override
    public Color BACKGROUND_LUDEME_BODY_FUNCTIONS()
    {
        return BACKGROUND_LUDEME_BODY_FUNCTIONS;
    }
    @Override
    public Color BACKGROUND_LUDEME_BODY_RULES()
    {
        return BACKGROUND_LUDEME_BODY_RULES;
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
    @Override
    public Color LUDEME_CONNECTION_POINT()
    {
        return LUDEME_CONNECTION_POINT;
    }
    @Override
    public Color LUDEME_CONNECTION_POINT_INACTIVE()
    {
        return LUDEME_CONNECTION_POINT_INACTIVE;
    }

    @Override
    public Color LUDEME_CONNECTION_EDGE()
    {
        return LUDEME_CONNECTION_EDGE;
    }

    @Override
    public Color COMPILABLE_COLOR()
    {
        return COMPILABLE_COLOR;
    }

    @Override
    public Color NOT_COMPILABLE_COLOR()
    {
        return NOT_COMPILABLE_COLOR;
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



    // ~~ STROKES AND BORDERS ~~ //
    private static Border LUDEME_NODE_BORDER = BorderFactory.createLineBorder(LUDEME_BORDER_COLOR(), NODE_BORDER_WIDTH);
    private static Border LUDEME_NODE_BORDER_SELECTED = BorderFactory.createLineBorder(LUDEME_SELECTION_COLOR(), NODE_BORDER_WIDTH);
    private static Border LUDEME_NODE_BORDER_UNCOMPILABLE = BorderFactory.createLineBorder(LUDEME_UNCOMPILABLE_COLOR(), NODE_BORDER_WIDTH);

    @Override
    public Border LUDEME_NODE_BORDER()
    {
        return LUDEME_NODE_BORDER;
    }

    @Override
    public Border LUDEME_NODE_BORDER_SELECTED()
    {
        return LUDEME_NODE_BORDER_SELECTED;
    }

    @Override
    public Border LUDEME_NODE_BORDER_UNCOMPILABLE()
    {
        return LUDEME_NODE_BORDER_UNCOMPILABLE;
    }


}
