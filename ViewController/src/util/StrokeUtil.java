package util;

import java.awt.BasicStroke;

import metadata.graphics.util.LineStyle;

/**
 * Routines for getting strokes with specified properties.
 * 
 * @author matthew.stephenson and cambolbro
 */
public class StrokeUtil 
{
	public static BasicStroke getStrokeFromStyle
	(
		final LineStyle lineStyle, final BasicStroke strokeThin, final BasicStroke strokeThick
	)
	{
		switch(lineStyle)
		{
		case Hidden:
			return new BasicStroke(0);
		case Thick:
			return strokeThick;
		case ThickDashed:
			return getDashedStroke(strokeThick.getLineWidth());
		case ThickDotted:
			return getDottedStroke(strokeThick.getLineWidth());
		case Thin:
			return strokeThin;
		case ThinDashed:
			return getDashedStroke(strokeThin.getLineWidth());
		case ThinDotted:
			return getDottedStroke(strokeThin.getLineWidth());
		}
		return null;
	}
	
	//-------------------------------------------------------------------------
	
	public static BasicStroke getDashedStroke(final float strokeWidth)
	{
		final float dash[] = { strokeWidth*3, strokeWidth*3 };
		return new 	BasicStroke
				   	(
				   		strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0.0f, dash, 0.0f
				   	);
	}
	
	//-------------------------------------------------------------------------
	
	public static BasicStroke getDottedStroke(final float strokeWidth)
	{
		final float dash[] = { 0.0f, strokeWidth * 2.5f };
		return new 	BasicStroke
					(
						strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0.0f, dash, 0.0f
					);
	}
	
	//-------------------------------------------------------------------------
	
}
