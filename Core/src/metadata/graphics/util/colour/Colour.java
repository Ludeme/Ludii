package metadata.graphics.util.colour;

import java.awt.Color;
import java.util.BitSet;

import game.Game;
import metadata.graphics.GraphicsItem;

/**
 * Defines a colour for use in metadata items.
 * 
 * @author cambolbro
 * 
 * @remarks: Support formats: RGB, RGBA, HSV, Hex code and UserColourType.
 */
public class Colour implements GraphicsItem
{	
	private final Color colour;
	
	//-----------------------------------------------------------------------
	
	/**
	 * For defining a colour with the Red Green Blue values.
	 * 
	 * @param r Red component [0..255].
	 * @param g Green component [0..255].
	 * @param b Blue component [0..255].
	 * 
	 * @example (colour 255 0 0)
	 */
	public Colour(final Integer r, final Integer g, final Integer b)
	{
		colour = new Color(r.intValue(), g.intValue(), b.intValue());
	}

	/**
	 * For defining a colour with the Red Green Blue values and an alpha value.
	 * 
	 * @param r Red component [0..255].
	 * @param g Green component [0..255].
	 * @param b Blue component [0..255].
	 * @param a Alpha component [0..255].
	 * 
	 * @example (colour 255 0 0 127)
	 */
	public Colour(final Integer r, final Integer g, final Integer b, final Integer a)
	{
		colour = new Color(r.intValue(), g.intValue(), b.intValue(), a.intValue());
	}

//	/**
//	 * @param h Hue component [0..1].
//	 * @param s Saturation component [0..1].
//	 * @param v Value component [0..1].
//	 * 
//	 * @example (colour 0.5 0.5 0.2)
//	 */
//	public Colour(final Float h, final Float s, final Float v)
//	{
//		this.colour = HSVtoColor(h, s, v);
//	}
	
	/**
	 * For defining a colour with the hex code.
	 * 
	 * @param hexCode Six digit hexadecimal code.
	 * 
	 * @example (colour "#00ff1a")
	 */
	public Colour(final String hexCode)
	{
		colour = interpretHexCode(hexCode);
	}

	/**
	 * For defining a predefined colour.
	 * 
	 * @param type Predefined user colour type.
	 * 
	 * @example (colour DarkBlue)
	 */
	public Colour(final UserColourType type)
	{
		colour = type.colour();
	}
	
//	/**
//	 * For defining a colour with a player roletype.
//	 * Only works for real players, not shared or neutral.
//	 * 
//	 * @param roletype Roletype of the player colour.
//	 * 
//	 * @example (colour P2)
//	 */
//	public Colour(final RoleType roletype)
//	{
//		try
//		{
//			colour = graphics.settingsColour().playerColour(roletype.owner(), Constants.MAX_PLAYERS);
//		}
//		catch (final Exception e)
//		{
//			System.out.println("Invalid roletype colour specified: " + roletype.owner());
//			colour = Color.BLACK;
//		}
//	}
	
	//-----------------------------------------------------------------------
	
	/**
	 * @return The color.
	 */
	public Color colour()
	{
		return colour;
	}
	
	//-------------------------------------------------------------------------
	
	 /**
     * Converts HSV to RGB. From Foley & Van Dam.
     * @param hue Hue (0..360).
     * @param saturation Saturation (0..1).
     * @param value Value (0..1).
     * @return Java Color object corresponding to HSV colour.
     */
    public static Color HSVtoColor
    (
    	final double hue, final double saturation, final double value
    )
    {
    	double r, g, b;
    	
    	double h = hue;

    	if (saturation == 0.0)
    	{
    		if (h >= 0.0)
    		{
    			//throw new IllegalArgumentException("Bad HSV colour combination.");
    			System.out.println("** Colour.HSVtoColor(): Bad HSV colour combination.");
    			return Color.black;
    		}
     	
    		r = value;
    		g = value;
    		b = value;
    	}
    	else
    	{
    		while (h > 360)
    			h -= 360.0;

    		while (h < 0.0)
    			h += 360.0;

    		h /= 60.0;

    		final int i = (int)h;

    		final double f = h - i;

    		final double p = value * (1.0 -  saturation);
    		final double q = value * (1.0 - (saturation * f));
    		final double t = value * (1.0 - (saturation * (1.0 - f)));

    		switch (i)
    		{
    		case 0:	r = value;	g = t;	    b = p;		break;
    		case 1:	r = q;      g = value;	b = p;		break;
    		case 2:	r = p;	    g = value;	b = t;		break;
    		case 3:	r = p;	    g = q;	    b = value;	break;
    		case 4:	r = t;	    g = p;	    b = value;	break;
    		case 5:	r = value;	g = p;	    b = q;		break;
    		default:
    			//throw new IllegalArgumentException("Invalid HSV case, i=" + i + ".");
 	   			System.out.println("** Colour.HSVtoColor(): Invalid HSV case, i=" + i + ".");
    			return Color.black;
     		}
    	}

    	return new  Color
    				(
    					Math.max(0, Math.min(255, (int)(r * 255 + 0.5))),
    					Math.max(0, Math.min(255, (int)(g * 255 + 0.5))),
    					Math.max(0, Math.min(255, (int)(b * 255 + 0.5)))
    				);
    }
    
   	//-------------------------------------------------------------------------

	/**
	 * @param code The hex code.
	 * @return The colour from a hex code.
	 */
    public static Color interpretHexCode(final String code)
    {
    	return Color.decode(code);
    }

	/**
	 * @param value The integer corresponding to the hex code.
	 * @return The color from a hex code.
	 */
    public static Color interpretHexCode(final int value)
    {
    	return 	new Color
				(
					((value >> 16) & 0xff), 
					((value >>  8) & 0xff), 
					( value        & 0xff)
				);
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
