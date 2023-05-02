package metadata.graphics.util.colour;

import java.awt.Color;
import java.util.BitSet;

import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Specifies the colour of the user.
 */
public enum UserColourType implements GraphicsItem
{
	//-------------------------------------------------------------------------

	/** Plain white. */
	White("White", 255, 255, 255),
	
	/** Plain black. */
	Black("Black", 0, 0, 0),
	
	/** Medium grey. */
	Grey("Grey", 150, 150, 150),
	
	/** Light grey. */
	LightGrey("Light Grey", 200, 200, 200),
	
	/** Very light grey. */
	VeryLightGrey("Very Light Grey", 230, 230, 230),
	
	/** Dark grey. */
	DarkGrey("Dark Grey", 100, 100, 100),
	
	/** Very dark grey. */
	VeryDarkGrey("Very Dark Grey", 50, 50, 50),
	
	/** Almost black. */
	Dark("Dark", 30, 30, 30),
	
	/** Plain red. */
	Red("Red", 255, 0, 0),
	
	/** Plain green. */
	Green("Green", 0, 200, 0),
	
	/** Blue. */
	Blue("Blue", 0, 127, 255),

	/** Yellow. */
	Yellow("Yellow", 255, 245, 0),
	
	/** Pink. */
	Pink("Pink", 255, 0, 255),
	
	/** Cyan. */
	Cyan("Cyan", 0, 255, 255),

	/** Medium brown. */
	Brown("Brown", 139, 69, 19),
	
	/** Dark brown. */
	DarkBrown("Dark Brown", 101, 67, 33),

	/** Very dark brown. */
	VeryDarkBrown("Very Dark Brown", 50, 33, 16),
	
	/** Purple. */
	Purple("Purple", 127, 0, 127),
	
	/** Magenta. */
	Magenta("Magenta", 255, 0, 255),

	/** Turquoise. */
	Turquoise("Turquoise", 0, 127, 127),
	
	/** Orange. */
	Orange("Orange", 255, 127, 0),
	
	/** Light orange. */
	LightOrange("Light Orange", 255, 191, 0),
	
	/** Light red. */
	LightRed("Light Red", 255, 127, 127),
	
	/** Dark red. */
	DarkRed("Dark Red", 127, 0, 0),

	/** Burgundy. */
	Burgundy("Burgundy", 63, 0, 0),
	
	/** Light green. */
	LightGreen("Light Green", 127, 255, 127),
	
	/** Dark green. */
	DarkGreen("Dark Green", 0, 127, 0),
	
	/** Light blue. */
	LightBlue("Light Blue", 127, 191, 255),
	
	/** Very light blue. */
	VeryLightBlue("Very Light Blue", 205, 234, 237),
	
	/** Dark blue. */
	DarkBlue("Dark Blue", 0, 0, 127),
	
	/** Light icy blue. */
	IceBlue("Ice Blue", 183, 226, 228),
	
	/** Gold. */
	Gold("Gold", 212, 175, 55),
	
	/** Silver. */
	Silver("Silver", 192, 192, 192),
	
	/** Bronze. */
	Bronze("Bronze", 205, 127, 50),
	
	/** Gun metal blue. */
	GunMetal("GunMetal", 44, 53, 57),

	/** Light human skin tone. */
	HumanLight("Human Light", 204, 182, 140),
	
	/** Dark human skin tone. */
	HumanDark("Human Dark", 108, 86, 60),

	/** Cream. */
	Cream("Cream", 255, 255, 230),
	
	/** Deep purple. */
	DeepPurple("Deep Purple", 127, 0, 127),
	
	/** Pink. */
	PinkFloyd("Pink Floyd", 255, 75, 150),
	
	/** Very dark bluish black. */
	BlackSabbath("Black Sabbath", 0, 0, 32),
	
	/** King of the crimsons. */
	KingCrimson("King Crimson", 220, 20, 60),
			
	/** Tangerine. */
	TangerineDream("Tangerine Dream", 242, 133, 0),
	
	/** Baby Blue. */
	BabyBlue("Baby Blue", 127, 191, 255),
	
	/** Light tan (as per Tafl boards). */
	LightTan("Light Tan", 250, 200, 100),
	
	/** Invisible. */
	Hidden("Hidden", 0, 0, 0, 0),
	;

	//-------------------------------------------------------------------------

	private final String label;
	private final int r;
	private final int g;
	private final int b;
	private final int a;

	//-------------------------------------------------------------------------

	UserColourType(final String label, final int r, final int g, final int b)
	{
		this.label = label;
		this.r = r;
		this.g = g;
		this.b = b;
		a = 255;
	}
	
	UserColourType(final String label, final int r, final int g, final int b, final int a)
	{
		this.label = label;
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The label of the colour.
	 */
	public String label()
	{
		return label;
	}

	/**
	 * @return The red part of the colour.
	 */
	public int r()
	{
		return r;
	}

	/**
	 * @return The Green part of the colour.
	 */
	public int g()
	{
		return g;
	}

	/**
	 * @return The Blue part of the colour.
	 */
	public int b()
	{
		return b;
	}
	
	/**
	 * @return The Alpha part of the colour.
	 */
	public int a()
	{
		return a;
	}
	
	/**
	 * @return the colour
	 */
	public Color colour()
	{
		return new Color(r, g, b, a);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param key The key of the colour.
	 * @return The UserColourType.
	 */
	public static UserColourType find(final String key)
	{
		for (final UserColourType uc : values())
			if (uc.label.equals(key))
				return uc;
		return null;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		return concepts;
	}

	@Override
	public long gameFlags(final Game game)
	{
		final long gameFlags = 0l;
		return gameFlags;
	}

	@Override
	public boolean needRedraw()
	{
		return false;
	}

}
