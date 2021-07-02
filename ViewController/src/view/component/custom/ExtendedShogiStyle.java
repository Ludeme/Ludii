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
import view.component.custom.types.ShogiType;

/**
 * Implementation of extended Shogi piece style
 * Used for games where the Shogi characters are drawn on top of blank SVGs (e.g. Taikyoku Shogi)
 * 
 * @author matthew.stephenson
 */
public class ExtendedShogiStyle extends PieceStyle
{
	public ExtendedShogiStyle(final Bridge bridge, final Component component) 
	{
		super(bridge, component);
	}

	//----------------------------------------------------------------------------
	
	@Override
	protected SVGGraphics2D getSVGImageFromFilePath(final SVGGraphics2D g2dOriginal, final Context context, final int imageSize, final String filePath, 
			final int containerIndex, final int localState, final int value, final int hiddenValue, final int rotation, final boolean secondary)
	{
		final String outlinePath = ImageUtil.getImageFullPath("shogi_blank");
		final SVGGraphics2D g2d = super.getSVGImageFromFilePath(g2dOriginal, context, imageSize, outlinePath, containerIndex, localState, value, hiddenValue, rotation, secondary);
		final int g2dSize = g2d.getWidth();
		
		// Temporarily rotate graphics object so that the drawn text is also rotated correctly.
		final AffineTransform originalTransform = g2d.getTransform();
		g2d.rotate(Math.toRadians(rotation), g2dSize/2, g2dSize/2);
		
		for (int i = 0; i < ShogiType.values().length; i++)
		{
			if 
			(
				ShogiType.values()[i].englishName().equals(svgName) 
				|| 
				ShogiType.values()[i].kanji().equals(svgName) 
				|| 
				ShogiType.values()[i].romaji().toLowerCase().equals(svgName.toLowerCase()) 
				|| 
				ShogiType.values()[i].name().toLowerCase().equals(svgName.toLowerCase())
			)
			{
				final Font valueFont = new Font("Arial", Font.PLAIN, g2dSize/4);
				g2d.setColor(Color.BLACK);
				g2d.setFont(valueFont);

				if (ShogiType.values()[i].kanji().length() == 1)
				{
					final Rectangle2D rect = valueFont.getStringBounds(Character.toString(ShogiType.values()[i].kanji().charAt(0)), g2d.getFontRenderContext());
					g2d.drawString( Character.toString(ShogiType.values()[i].kanji().charAt(0)), (int)(g2dSize/2 - rect.getWidth()/2) , (int)(g2dSize/2 + rect.getHeight()/2));
					break;
				}
				else if (ShogiType.values()[i].kanji().length() == 2)
				{
					Rectangle2D rect = valueFont.getStringBounds(Character.toString(ShogiType.values()[i].kanji().charAt(0)), g2d.getFontRenderContext());
					g2d.drawString( Character.toString(ShogiType.values()[i].kanji().charAt(0)), (int)(g2dSize/2 - rect.getWidth()/2) , g2dSize/2);

					rect = valueFont.getStringBounds(Character.toString(ShogiType.values()[i].kanji().charAt(1)), g2d.getFontRenderContext());
					g2d.drawString( Character.toString(ShogiType.values()[i].kanji().charAt(1)), (int)(g2dSize/2 - rect.getWidth()/2) , (int)(g2dSize/2 + rect.getHeight()));

					break;
				}
				else if (ShogiType.values()[i].kanji().length() == 3)
				{
					Rectangle2D rect = valueFont.getStringBounds(Character.toString(ShogiType.values()[i].kanji().charAt(0)), g2d.getFontRenderContext());
					g2d.drawString( Character.toString(ShogiType.values()[i].kanji().charAt(0)), (int)(g2dSize/2 - rect.getWidth()/2) , (int) (g2dSize/2 - rect.getHeight()/4));

					rect = valueFont.getStringBounds(Character.toString(ShogiType.values()[i].kanji().charAt(1)), g2d.getFontRenderContext());
					g2d.drawString( Character.toString(ShogiType.values()[i].kanji().charAt(1)), (int)(g2dSize/2 - rect.getWidth()/2) , (int)(g2dSize/2 + rect.getHeight()/2));

					rect = valueFont.getStringBounds(Character.toString(ShogiType.values()[i].kanji().charAt(2)), g2d.getFontRenderContext());
					g2d.drawString( Character.toString(ShogiType.values()[i].kanji().charAt(2)), (int)(g2dSize/2 - rect.getWidth()/2) , (int)(g2dSize/2 + rect.getHeight()*1.3));

					break;
				}
			}
		}
		
		g2d.setTransform(originalTransform);
		
		return g2d;
	}
	
	//----------------------------------------------------------------------------

}
