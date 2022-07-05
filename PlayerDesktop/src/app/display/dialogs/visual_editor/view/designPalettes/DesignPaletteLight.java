package app.display.dialogs.visual_editor.view.designPalettes;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class DesignPaletteLight extends DesignPalette
{

    private static DesignPaletteLight instance = null;

    public static DesignPaletteLight instance()
    {
        if (instance == null)
        {
            instance = new DesignPaletteLight();
        }
        return instance;
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



    // ~~ STROKES AND BORDERS ~~ //
    private static final Border LUDEME_NODE_BORDER = BorderFactory.createLineBorder(LUDEME_BORDER_COLOR(), NODE_BORDER_WIDTH);
    private static final Border LUDEME_NODE_BORDER_SELECTED = BorderFactory.createLineBorder(LUDEME_SELECTION_COLOR(), NODE_BORDER_WIDTH);
    private static final Border LUDEME_NODE_BORDER_UNCOMPILABLE = BorderFactory.createLineBorder(LUDEME_UNCOMPILABLE_COLOR(), NODE_BORDER_WIDTH);

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
