package view.component.custom;

import java.awt.Color;
import java.awt.Rectangle;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import game.equipment.component.Component;
import other.context.Context;
import view.component.custom.types.NativeAmericanDiceType;

/**
 * Implementation of the Native American Dice style
 * Used for games which use special styles for dice, mostly native american games (e.g. Kints)
 * 
 * @author matthew.stephenson
 */
public class NativeAmericanDiceStyle extends DieStyle
{
	public NativeAmericanDiceStyle(final Bridge bridge, final Component component) 
	{
		super(bridge, component);
		setDefaultDiceDesign();
	}

	//----------------------------------------------------------------------------
	
	@Override
	protected SVGGraphics2D getSVGImageFromFilePath(final SVGGraphics2D g2d, final Context context, final int imageSize, final String filePath, 
			final int containerIndex, final int localState, final int value, final int hiddenValue, final int rotation, final boolean secondary)
	{
		// Rectangle that defines the position and size of the rectangle background.
		final Rectangle rect = new Rectangle(0, imageSize/5, imageSize - imageSize/7, imageSize - imageSize/3 - imageSize/5);
		g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
		
		NativeAmericanDiceType nativeAmericanDiceType = null;
		for (int i = 0; i < NativeAmericanDiceType.values().length; i++)
			if (NativeAmericanDiceType.values()[i].englishName().equals(svgName) || NativeAmericanDiceType.values()[i].name().toLowerCase().equals(svgName.toLowerCase()))
				nativeAmericanDiceType = NativeAmericanDiceType.values()[i];				
		
		if (nativeAmericanDiceType == null) 
			return g2d;
		
		switch(nativeAmericanDiceType)
		{
		case Patol1:
			if (localState == 0)
				break;
			else if (localState == 1)
			{
				g2d.drawLine(rect.x + rect.width/2, rect.y, rect.x, rect.y + rect.height);
				g2d.drawLine(rect.x + rect.width, rect.y, rect.x + rect.width/2, rect.y + rect.height);
				break;
			}	
			break;
		case Patol2:
			if (localState == 0)
				break;
			else if (localState == 1)
			{
				g2d.drawLine(rect.x, rect.y, rect.width, rect.y + rect.height);
				g2d.drawLine(rect.x + rect.width/2, rect.y, rect.x, rect.y + rect.height);
				g2d.drawLine(rect.x + rect.width, rect.y, rect.x + rect.width/2, rect.y + rect.height);
				break;
			}
			break;
		case Notched:
			if (localState == 0)
				break;
			else if (localState == 1)
			{
				g2d.drawLine(rect.x + rect.width/5, rect.y, rect.x + rect.width/5, rect.y + rect.height/5);
				g2d.drawLine(rect.x + rect.width/5 * 2, rect.y, rect.x + rect.width/5 * 2, rect.y + rect.height/5);
				g2d.drawLine(rect.x + rect.width/5 * 3, rect.y, rect.x + rect.width/5 * 3, rect.y + rect.height/5);
				g2d.drawLine(rect.x + rect.width/5 * 3, rect.y + rect.height, rect.x + rect.width/5 * 3, rect.y + rect.height - rect.height/5);
				g2d.drawLine(rect.x + rect.width/5 * 4, rect.y + rect.height, rect.x + rect.width/5 * 4, rect.y + rect.height - rect.height/5);
				break;
			}
			break;
		case SetDilth:
			if (localState == 0)
				break;
			else if (localState == 1)
			{
				g2d.drawLine(rect.x + rect.width/2, rect.y, rect.x + rect.width/3, rect.y + rect.height);
				g2d.drawLine(rect.x + rect.width/2 + rect.width/6, rect.y, rect.x + rect.width/2, rect.y + rect.height);
				break;
			}	
			break;
		case Nebakuthana1:
			if (localState == 0)
				break;
			else if (localState == 1)
			{
				g2d.setColor(Color.RED);
				g2d.drawPolygon(new int[] {rect.x + rect.width/10, rect.x + rect.width/5 + rect.width/10, rect.x + rect.width/5}, new int[] {rect.y, rect.y, rect.y + rect.height/5}, 3);
				g2d.drawPolygon(new int[] {rect.x + rect.width - rect.width/10, rect.x + rect.width - rect.width/5 - rect.width/10, rect.x + rect.width - rect.width/5}, new int[] {rect.y, rect.y, rect.y + rect.height/5}, 3);
				g2d.drawPolygon(new int[] {rect.x + rect.width/10, rect.x + rect.width/5 + rect.width/10, rect.x + rect.width/5}, new int[] {rect.y + rect.height, rect.y + rect.height, rect.y + rect.height - rect.height/5}, 3);
				g2d.drawPolygon(new int[] {rect.x + rect.width - rect.width/10, rect.x + rect.width - rect.width/5 - rect.width/10, rect.x + rect.width - rect.width/5}, new int[] {rect.y + rect.height, rect.y + rect.height, rect.y + rect.height - rect.height/5}, 3);
				break;
			}	
			break;
		case Nebakuthana2:
			if (localState == 0)
				break;
			else if (localState == 1)
			{
				g2d.setColor(Color.RED);
				g2d.drawLine(rect.x, rect.y + rect.height/2, rect.x + rect.width, rect.y + rect.height/2);
				g2d.drawLine(rect.x + rect.width/3, rect.y, rect.x + rect.width/3*2, rect.y + rect.height);
				g2d.drawLine(rect.x + rect.width/3, rect.y + rect.height, rect.x + rect.width/3*2, rect.y);
				break;
			}	
			break;
		case Nebakuthana3:
			if (localState == 0)
				break;
			else if (localState == 1)
			{
				g2d.setColor(Color.RED);
				g2d.drawLine(rect.x + rect.width/2, rect.y + rect.height/2, rect.x + rect.width/2, rect.y + rect.height/2);
				g2d.drawPolygon(new int[] {rect.x + rect.width/2, rect.x + rect.width/3, rect.x + rect.width/2, rect.x + rect.width/3*2}, new int[] {rect.y + rect.height/10, rect.y + rect.height/2, rect.y + rect.height - rect.height/10, rect.y + rect.height/2}, 4);
				break;
			}	
			break;
		case Nebakuthana4:
			if (localState == 0)
			{
				g2d.drawLine(rect.x + rect.width/8, rect.y, rect.x + rect.width/8, rect.y + rect.height/5);
				g2d.drawLine(rect.x + rect.width/8 * 2, rect.y, rect.x + rect.width/8 * 2, rect.y + rect.height/5);
				g2d.drawLine(rect.x + rect.width/2, rect.y, rect.x + rect.width/2 , rect.y + rect.height);
				g2d.drawLine(rect.x + rect.width/8 * 6, rect.y + rect.height, rect.x + rect.width/8 * 6, rect.y + rect.height - rect.height/5);
				g2d.drawLine(rect.x + rect.width/8 * 7, rect.y + rect.height, rect.x + rect.width/8 * 7, rect.y + rect.height - rect.height/5);
				break;
			}
			else if (localState == 1)
			{
				g2d.setColor(Color.GREEN);
				g2d.drawLine(rect.x + rect.width/2, rect.y + rect.height/2, rect.x + rect.width/4, rect.y + rect.height/2);
				g2d.drawLine(rect.x + rect.width/2, rect.y + rect.height/2, rect.x + rect.width/2 + rect.width/4, rect.y + rect.height/2);
				g2d.drawLine(rect.x + rect.width/2, rect.y + rect.height/2, rect.x + rect.width/2, rect.y + rect.height);
				g2d.drawLine(rect.x + rect.width/2, rect.y + rect.height/2, rect.x + rect.width/2, rect.y);
				g2d.drawLine(rect.x + rect.width/2, rect.y + rect.height/2, rect.x + rect.width/4, rect.y);
				g2d.drawLine(rect.x + rect.width/2, rect.y + rect.height/2, rect.x + rect.width/2 + rect.width/4, rect.y);
				g2d.drawLine(rect.x + rect.width/2, rect.y + rect.height/2, rect.x + rect.width/2 + rect.width/4, rect.y + rect.height);
				g2d.drawLine(rect.x + rect.width/2, rect.y + rect.height/2, rect.x + rect.width/4, rect.y + rect.height);
				break;
			}	
			break;
		case Kints1:
			if (localState == 0)
				break;
			else if (localState == 1)
			{
				g2d.drawLine(rect.x , rect.y, rect.x + rect.width/6, rect.y + rect.height);
				g2d.drawLine(rect.x + rect.width/3 , rect.y, rect.x + rect.width/6, rect.y + rect.height);
				g2d.drawLine(rect.x + rect.width/3, rect.y, rect.x + rect.width/2, rect.y + rect.height);
				g2d.drawLine(rect.x + rect.width/3*2, rect.y, rect.x + rect.width/2, rect.y + rect.height);
				g2d.drawLine(rect.x + rect.width/3*2, rect.y, rect.x + rect.width/6*5, rect.y + rect.height);
				g2d.drawLine(rect.x + rect.width, rect.y, rect.x + rect.width/6*5, rect.y + rect.height);
				break;
			}	
			break;
		case Kints2:
			if (localState == 0)
				break;
			else if (localState == 1)
			{
				g2d.drawLine(rect.x + rect.width/2 - rect.width/6, rect.y, rect.x + rect.width/2 - rect.width/6, rect.y + rect.height);
				g2d.drawLine(rect.x + rect.width/2 - rect.width/3, rect.y, rect.x + rect.width/2 - rect.width/3, rect.y + rect.height);
				g2d.drawLine(rect.x + rect.width/2 + rect.width/6, rect.y, rect.x + rect.width/2 + rect.width/6, rect.y + rect.height);
				g2d.drawLine(rect.x + rect.width/2 + rect.width/3, rect.y, rect.x + rect.width/2 + rect.width/3, rect.y + rect.height);
				break;
			}	
			break;
		case Kints3:
			if (localState == 0)
				break;
			else if (localState == 1)
			{
				g2d.drawLine(rect.x, rect.y + rect.height, rect.x + rect.width/6, rect.y);
				g2d.drawLine(rect.x + rect.width/3, rect.y + rect.height, rect.x + rect.width/6, rect.y);
				g2d.drawLine(rect.x + rect.width, rect.y, rect.x + rect.width - rect.width/6, rect.y + rect.height);
				g2d.drawLine(rect.x + rect.width - rect.width/3, rect.y, rect.x + rect.width - rect.width/6, rect.y + rect.height);
				break;
			}	
			break;
		case Kints4:
			if (localState == 0)
				break;
			else if (localState == 1)
			{
				g2d.drawLine(rect.x + rect.width/3, rect.y, rect.x + rect.width/3*2, rect.y + rect.height);
				g2d.drawLine(rect.x + rect.width/3, rect.y + rect.height, rect.x + rect.width/3*2, rect.y);
				break;
			}	
			break;
		case Kolica1:
			if (localState == 1)
				break;
			else if (localState == 0)
			{
				g2d.drawPolygon(new int[] {rect.x + rect.width/10, rect.x + rect.width/5 + rect.width/10, rect.x + rect.width/5}, new int[] {rect.y, rect.y, rect.y + rect.height/5}, 3);
				g2d.drawPolygon(new int[] {rect.x + rect.width - rect.width/10, rect.x + rect.width - rect.width/5 - rect.width/10, rect.x + rect.width - rect.width/5}, new int[] {rect.y, rect.y, rect.y + rect.height/5}, 3);
				g2d.drawPolygon(new int[] {rect.x + rect.width/10, rect.x + rect.width/5 + rect.width/10, rect.x + rect.width/5}, new int[] {rect.y + rect.height, rect.y + rect.height, rect.y + rect.height - rect.height/5}, 3);
				g2d.drawPolygon(new int[] {rect.x + rect.width - rect.width/10, rect.x + rect.width - rect.width/5 - rect.width/10, rect.x + rect.width - rect.width/5}, new int[] {rect.y + rect.height, rect.y + rect.height, rect.y + rect.height - rect.height/5}, 3);
				break;
			}	
			break;
		case Kolica2:
			if (localState == 1)
				break;
			else if (localState == 0)
			{
				g2d.drawLine(rect.x, rect.y + rect.height/2, rect.x + rect.width, rect.y + rect.height/2);
				g2d.drawLine(rect.x + rect.width/3, rect.y, rect.x + rect.width/3*2, rect.y + rect.height);
				g2d.drawLine(rect.x + rect.width/3, rect.y + rect.height, rect.x + rect.width/3*2, rect.y);
				break;
			}	
			break;
		case Kolica3:
			if (localState == 0)
				break;
			else if (localState == 1)
			{
				g2d.drawLine(rect.x + rect.width/2, rect.y, rect.x + rect.width/3, rect.y + rect.height);
				g2d.drawLine(rect.x + rect.width/2 + rect.width/6, rect.y, rect.x + rect.width/2, rect.y + rect.height);
				break;
			}	
			break;
		case Kolica4:
			if (localState == 0)
				break;
			else if (localState == 1)
			{
				g2d.drawLine(rect.x + rect.width/2, rect.y, rect.x + rect.width/3, rect.y + rect.height);
				g2d.drawLine(rect.x + rect.width/2 + rect.width/6, rect.y, rect.x + rect.width/2, rect.y + rect.height);
				break;
			}	
			break;
		default:
			break; 
		}
		
		return g2d;
	}
	
	//----------------------------------------------------------------------------

}
