package app.display.dialogs.editor;

import java.awt.Color;

public enum EditorTokenType
{
	STRING (EditorLookAndFeel.STRING_COLOUR, false),
//	OPEN_ROUND (EditorLookAndFeel.DEFAULT_COLOUR, true),
//	CLOSE_ROUND (EditorLookAndFeel.DEFAULT_COLOUR, true),
//	OPEN_SQUARE (EditorLookAndFeel.DEFAULT_COLOUR, true),
//	CLOSE_SQUARE (EditorLookAndFeel.DEFAULT_COLOUR, true),
//	OPEN_CURLY (EditorLookAndFeel.DEFAULT_COLOUR, true),
//	CLOSE_CURLY (EditorLookAndFeel.DEFAULT_COLOUR, true),
//	OPEN_ANGLE (EditorLookAndFeel.RULE_COLOUR, true),
//	CLOSE_ANGLE (EditorLookAndFeel.RULE_COLOUR, true),
	OPEN_ROUND (EditorLookAndFeel.DEFAULT_COLOUR, false),
	CLOSE_ROUND (EditorLookAndFeel.DEFAULT_COLOUR, false),
	OPEN_SQUARE (EditorLookAndFeel.DEFAULT_COLOUR, false),
	CLOSE_SQUARE (EditorLookAndFeel.DEFAULT_COLOUR, false),
	OPEN_CURLY (EditorLookAndFeel.DEFAULT_COLOUR, false),
	CLOSE_CURLY (EditorLookAndFeel.DEFAULT_COLOUR, false),
	OPEN_ANGLE (EditorLookAndFeel.RULE_COLOUR, false),
	CLOSE_ANGLE (EditorLookAndFeel.RULE_COLOUR, false),
	FLOAT (EditorLookAndFeel.FLOAT_COLOUR, false),
	INT (EditorLookAndFeel.INT_COLOUR, false),
	LABEL (EditorLookAndFeel.LABEL_COLOUR, false),
	WHITESPACE (EditorLookAndFeel.DEFAULT_COLOUR, false),
	CLASS (EditorLookAndFeel.CLASS_COLOUR, false),
	ENUM (EditorLookAndFeel.ENUM_COLOUR, false),
	RULE (EditorLookAndFeel.RULE_COLOUR, false),
	OTHER (EditorLookAndFeel.DEFAULT_COLOUR, false);
	
	private final Color fgColour;
	private final boolean isBold;
	private EditorTokenType (final Color fgColour, final boolean isBold) {
		this.fgColour = fgColour;
		this.isBold = isBold;
	}

	/**
	 * @return the foreground colour
	 */
	public Color fgColour() { return fgColour; }

	/**
	 * @return true if this should be rendered in bold text
	 */
	public boolean isBold() { return isBold; }
}
