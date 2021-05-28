package app.display.dialogs.editor;

import java.awt.Color;

/**
 * All colour information in one place!
 * @author mrraow and cambolbro
 */
public class EditorLookAndFeel
{
	static final String PARAM_TABLE_START = "<table class=\"params\" border=\"0\" cellspacing=0 cellpadding=0>";
	static final String DOC_TABLE_START = "<table cellspacing=0 cellpadding=10 width='100%' >"; //"<table class=\"doc\" width=\"100%\" border=1 cellspacing=0 cellpadding=10>";
	static final String TABLE_END = "</table>";
	static final String ROW_START = "<tr>";
	static final String ROW_END = "</tr>";
	static final String DOC_ROW_START = "<tr style='border: 1px silver solid;'>";
	static final String DOC_ROW_END = "</tr>";
	static final String CELL_START = "<td>";
	static final String CELL_END = "</td>";
	static final String TABLE_HEADER_START = "<th>";
	static final String TABLE_HEADER_END = "</th>";
	static final String HEADING_START = "<b>";
	static final String HEADING_END = "</b>";
	static final String REMARK_START = "";  //"<i>";
	static final String REMARK_END = "";  //"</i>";
	static final String KEYWORD_START = "<b>";
	static final String KEYWORD_END = "</b>";
	static final String BR = "<br/>";
	static final String PAD = "&nbsp;";
	static final String MIN_CELL_DISTANCE = PAD+PAD+PAD+PAD+PAD+PAD;
	static final String HORIZONTAL_LINE3 = "<hr width=\"100%\"/>";

	static final Color STRING_COLOUR  = new Color( 79, 126,  97);
	static final Color FLOAT_COLOUR   = new Color( 70,  95, 185);
	static final Color INT_COLOUR     = new Color( 70,  95, 185);
	static final Color LABEL_COLOUR   = new Color(160, 160, 160);
	static final Color CLASS_COLOUR   = new Color(125,  35,  94);
	static final Color ENUM_COLOUR    = new Color(100,  64,  63);
	static final Color RULE_COLOUR    = new Color(220,   0,   0);
	static final Color DEFAULT_COLOUR = Color.BLACK;

	private static final Color[] BRACKET_COLOURS_BY_DEPTH = { new Color(0, 40, 150), new Color(50, 120, 240) };

	private static final Color[] CURLY_BRACKET_COLOURS_BY_DEPTH = { new Color(50, 50, 50), new Color(140, 140, 140) };

	private static final Color BAD_BRACKET_COLOUR = new Color(200, 0, 0);
	
	/**
	 * @param type type of bracket
	 * @param depth nesting depth
	 * @return colour for this combination of type and depth
	 */
	public static Color bracketColourByDepthAndType(final EditorTokenType type, final int depth)
	{
		if (depth < 0)
			return BAD_BRACKET_COLOUR;
		
		final int index = Math.max(0, depth % CURLY_BRACKET_COLOURS_BY_DEPTH.length);
		
		switch (type)
		{
		case OPEN_ANGLE:
		case CLOSE_ANGLE:
			return RULE_COLOUR;
			
		case OPEN_CURLY:
		case CLOSE_CURLY:
			return CURLY_BRACKET_COLOURS_BY_DEPTH[index];

		case OPEN_ROUND:
		case CLOSE_ROUND:
		case OPEN_SQUARE:
		case CLOSE_SQUARE:
			return BRACKET_COLOURS_BY_DEPTH[index];

		default:
			System.out.println("Unexpected bracket type received!");
			return DEFAULT_COLOUR;
		}
	}
}
