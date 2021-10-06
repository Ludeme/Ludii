package processing.similarity_matrix;

import java.awt.Color;

public abstract class ColorScheme
{
	

	private static final ColorScheme defaultCS = new ColorScheme()
		{//empty
		};

	public static ColorScheme getDefault() {
		return defaultCS; 
	}

	/**
	 * distance 0.0 is red. distance 1.0 is blue. distance 0.5 is green
	 * 
	 * @param d
	 * @return color relating to the distance
	 */
	public Color getColorFromDistance(final double d)
	{
		final float hue = (float) (d * 3.0 / 4.0);
		return Color.getHSBColor(hue, 1.f, 1.f);
	}

	public int getColorRGBFromDistance(final double d)
	{
		return getColorFromDistance(d).getRGB();
	}

}
