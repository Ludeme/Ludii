package metadata.graphics.util.colour;

import java.awt.Color;

/**
 * General routine to convert String into a colour.
 * @author anonymous
 */
public class ColourRoutines 
{
	/**
	 * Used by other metadata function to determine the colour that has been specified
	 * @param value
	 * @return Specified colour.
	 */
	public static Color getSpecifiedColour(final String value) 
	{
		Color colour;
		if (value == null || value.length() == 0)
		{
			return null;
		}
		else if (value.substring(0, 1).equals("#"))
 		{
 			try
 			{
 				colour = Color.decode(value);
 			}
 			catch (final Exception e)
 			{
 				colour = new Color(255, 255, 255);
 			}
 		}
 		else if (value.length() > 4 && value.substring(0, 4).equals("RGBA"))
 		{
 			try
 			{
 				colour = new Color(
 						Integer.valueOf(value.split(",")[0].replaceAll("RGBA", "").replaceAll("\\(", "").replaceAll("\\)", "")).intValue(), 
 						Integer.valueOf(value.split(",")[1].replaceAll("RGBA", "").replaceAll("\\(", "").replaceAll("\\)", "")).intValue(), 
 						Integer.valueOf(value.split(",")[2].replaceAll("RGBA", "").replaceAll("\\(", "").replaceAll("\\)", "")).intValue(),
 						Integer.valueOf(value.split(",")[3].replaceAll("RGBA", "").replaceAll("\\(", "").replaceAll("\\)", "")).intValue());
 			}
 			catch (final Exception e)
 			{
 				colour = new Color(255, 255, 255);
 			}
 		}
 		else if (value.length() > 3 && value.substring(0, 3).equals("RGB"))
 		{
 			try
 			{
 				colour = new Color(
 						Integer.valueOf(value.split(",")[0].replaceAll("RGB", "").replaceAll("\\(", "").replaceAll("\\)", "")).intValue(), 
 						Integer.valueOf(value.split(",")[1].replaceAll("RGB", "").replaceAll("\\(", "").replaceAll("\\)", "")).intValue(), 
 						Integer.valueOf(value.split(",")[2].replaceAll("RGB", "").replaceAll("\\(", "").replaceAll("\\)", "")).intValue());
 			}
 			catch (final Exception e)
 			{
 				colour = new Color(255, 255, 255);
 			}
 		}
 		else
 		{
     		final UserColourType userColour = UserColourType.find(value);
     		colour = (userColour == null)
								 ? new Color(255, 255, 255)
								 : new Color(userColour.r(), userColour.g(), userColour.b());
 		}
		return colour;
	}
	
	
	//-----------------------------------------------------------------------------
	
	/**
	 * @param color 
	 * @return The contrast colour (black of white) for a given colour, favouring white.
	 */
	public static Color getContrastColorFavourLight(final Color color) 
	{		
		final double y = (299 * color.getRed() + 587 * color.getGreen() + 114 * color.getBlue()) / 1000;
		return y >= 128 ? Color.black : Color.white;
	}
	
	/**
	 * @param color 
	 * @return The contrast colour (black of white) for a given colour, favouring black.
	 */
	public static Color getContrastColorFavourDark(final Color color) 
	{		
		final double y = color.getRed() + color.getGreen() + color.getBlue() / 3;
		return y >= 128 ? Color.black : Color.white;
	}
	
}
