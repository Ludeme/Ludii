package view.component.custom;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import game.equipment.component.Component;
import graphics.ImageUtil;
import other.context.Context;
import view.component.custom.types.XiangqiType;

/**
 * Implementation of extended Xiangqi piece style
 * Used for games where the Xiangqi characters are drawn on top of blank SVGs (e.g. Qi Guo Xiangxi)
 * 
 * @author matthew.stephenson
 */
public class ExtendedXiangqiStyle extends PieceStyle
{
	public ExtendedXiangqiStyle(final Bridge bridge, final Component component) 
	{
		super(bridge, component);
	}

	//----------------------------------------------------------------------------
	
	@Override
	protected SVGGraphics2D getSVGImageFromFilePath(final SVGGraphics2D g2dOriginal, final Context context, final int imageSize, final String filePath, 
			final int containerIndex, final int localState, final int value, final int hiddenValue, final int rotation, final boolean secondary)
	{
		final String outlinePath = ImageUtil.getImageFullPath("disc");
		SVGGraphics2D g2d = super.getSVGImageFromFilePath(g2dOriginal, context, imageSize, outlinePath, containerIndex, localState, value, hiddenValue, rotation, secondary);
		final int g2dSize = g2d.getWidth();
		Font valueFont = null;
		
		// Temporarily rotate graphics object so that the drawn text is also rotated correctly.
		final AffineTransform originalTransform = g2d.getTransform();
		g2d.rotate(Math.toRadians(rotation), g2dSize/2, g2dSize/2);
		
		for (int i = 0; i < XiangqiType.values().length; i++)
		{
			if 
			(
				XiangqiType.values()[i].englishName().equals(svgName) 
				|| 
				XiangqiType.values()[i].kanji().equals(svgName) 
				|| 
				XiangqiType.values()[i].romaji().toLowerCase().equals(svgName.toLowerCase()) 
				|| 
				XiangqiType.values()[i].name().toLowerCase().equals(svgName.toLowerCase())
			)
			{
				if (XiangqiType.values()[i].kanji().length() == 1)
				{
					valueFont = new Font("Arial", Font.PLAIN, g2dSize/2);
					g2d.setColor(Color.BLACK);
					g2d.setFont(valueFont);
					
					final Rectangle2D rect = valueFont.getStringBounds(Character.toString(XiangqiType.values()[i].kanji().charAt(0)), g2d.getFontRenderContext());
					g2d.drawString( Character.toString(XiangqiType.values()[i].kanji().charAt(0)), (int)(g2dSize/2 - rect.getWidth()/2) , (int)(g2dSize/2 + rect.getHeight()/3));
					break;
				}
				else if (XiangqiType.values()[i].kanji().length() == 2)
				{
					valueFont = new Font("Arial", Font.PLAIN, g2dSize/3);
					g2d.setColor(Color.BLACK);
					g2d.setFont(valueFont);
					
					Rectangle2D rect = valueFont.getStringBounds(Character.toString(XiangqiType.values()[i].kanji().charAt(0)), g2d.getFontRenderContext());
					g2d.drawString( Character.toString(XiangqiType.values()[i].kanji().charAt(0)), (int)(g2dSize/2 - rect.getWidth()/2) , g2dSize/2);

					rect = valueFont.getStringBounds(Character.toString(XiangqiType.values()[i].kanji().charAt(1)), g2d.getFontRenderContext());
					g2d.drawString( Character.toString(XiangqiType.values()[i].kanji().charAt(1)), (int)(g2dSize/2 - rect.getWidth()/2) , (int)(g2dSize/2 + rect.getHeight()/1.5));

					break;
				}
			}
		}
		
		g2d.setTransform(originalTransform);
		
		// Couldn't find the name you were after, try to find an SVG instead (used to force western style).
		if (valueFont == null) 
			g2d = super.getSVGImageFromFilePath(g2dOriginal, context, (int)(imageSize/1.5), filePath, containerIndex, localState, value, hiddenValue, rotation, secondary);
		
		return g2d;
	}
	
	//----------------------------------------------------------------------------

}
