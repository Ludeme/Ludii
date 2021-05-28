package graphics.svg.element;

import java.awt.Color;

import graphics.svg.SVGParser;

//-----------------------------------------------------------------------------

/**
 * Paint style for SVG element.
 * @author cambolbro
 */
public class Style
{
	private Color stroke = null;
	private Color fill = null;
	private double strokeWidth = 0;

	//-------------------------------------------------------------------------
	
	public Style()
	{
	}
	
	//-------------------------------------------------------------------------

	public Color stroke()
	{
		return stroke;
	}
	
	public void setStroke(final Color clr)
	{
		stroke = clr;
	}
	
	public Color fill()
	{
		return fill;
	}
	
	public void setFill(final Color clr)
	{
		fill = clr;
	}
	
	public double strokeWidth()
	{
		return strokeWidth;
	}
	
	public void setStrokeWidth(final double val)
	{
		strokeWidth = val;
	}

	//-------------------------------------------------------------------------

	public boolean load(final String expr)
	{
		boolean okay = true;
		
		String str = expr.replaceAll(":", "=");
		
		str = str.replaceAll("\"",  " ");
		str = str.replaceAll(",",  " ");
		str = str.replaceAll(";",  " ");
		
//		System.out.println("Loading style from expression: " + str);
		
		int pos = str.indexOf("stroke=");
		if (pos != -1)
		{
			final String result = SVGParser.extractStringAt(str, pos+7);
//			System.out.println("-- stroke is: " + result);
		
			if (result != null)
			{
				if (result.equals("red"))
					stroke = new Color(255, 0, 0);
				else if (result.equals("green"))
					stroke = new Color(0, 175, 0);
				else if (result.equals("blue"))
					stroke = new Color(0, 0, 255);
				else if (result.equals("white"))
					stroke = new Color(255, 255, 255);
				else if (result.equals("black"))
					stroke = new Color(0, 0, 0);
				else if (result.equals("orange"))
					stroke = new Color(255, 175, 0);
				else if (result.equals("yellow"))
					stroke = new Color(255, 240, 0);
				else if (result.contains("#"))
					stroke = colourFromCode(result);
			}
		}

		pos = str.indexOf("fill=");
		if (pos != -1)
		{
			final String result = SVGParser.extractStringAt(str, pos+5);
//			System.out.println("-- fill is: " + result);
		
			if (result != null)
			{
				if (result.equals("transparent"))
					fill = null;
				else if (result.equals("red"))
					fill = new Color(255, 0, 0);
				else if (result.equals("green"))
					fill = new Color(0, 175, 0);
				else if (result.equals("blue"))
					fill = new Color(0, 0, 255);
				else if (result.equals("white"))
					fill = new Color(255, 255, 255);
				else if (result.equals("black"))
					fill = new Color(0, 0, 0);
				else if (result.equals("orange"))
					fill = new Color(255, 175, 0);
				else if (result.equals("yellow"))
					fill = new Color(255, 240, 0);
				else if (result.contains("#"))
					fill = colourFromCode(result);
			}
		}

		if (str.contains("stroke-width="))
		{
			final Double result = SVGParser.extractDouble(str, "stroke-width=");
//			System.out.println("-- stroke-width is: " + result);
		
			if (result != null)
			{
				strokeWidth = result.doubleValue();
			}
		}

		return okay;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Color object defined in format #RRGGBB
	 */
	public static Color colourFromCode(final String strIn)
	{
		final String str = strIn.replaceAll("\"", "").trim();
		
		if (str.charAt(0) != '#' || str.length() != 7)
			return null;
		
		final int[] values = new int[7];
		for (int c = 1; c < str.length(); c++)
		{
			final char ch = Character.toLowerCase(str.charAt(c));
			if (ch >= '0' && ch <= '9')
				values[c] = ch - '0';
			else if (ch >= 'a' && ch <= 'f')
				values[c] = ch - 'a' + 10;
			else
				return null;  // not a numeric value
		}
		
		final int r = (values[1] << 4) | values[2]; 	
		final int g = (values[3] << 4) | values[4]; 	
		final int b = (values[5] << 4) | values[6]; 	
		
		return new Color(r, g, b);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("<");
		sb.append("fill=(" + fill + ")");
		sb.append(" stroke=(" + stroke + ")");
		sb.append(" strokeWidth=(" + strokeWidth + ")");
		sb.append(">");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------

}
