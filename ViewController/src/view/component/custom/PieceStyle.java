package view.component.custom;

import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import game.equipment.component.Component;
import graphics.ImageConstants;
import graphics.ImageProcessing;
import graphics.svg.SVGtoImage;
import metadata.graphics.util.ValueLocationType;
import other.context.Context;
import util.StringUtil;
import view.component.BaseComponentStyle;

/**
 * Implementation of regular piece style, no additional code from the base component style.
 * 
 * @author matthew.stephenson
 */
public class PieceStyle extends BaseComponentStyle
{
	public PieceStyle(final Bridge bridge, final Component component) 
	{
		super(bridge, component);
	}
	
	//----------------------------------------------------------------------------
	
	@Override
	protected SVGGraphics2D getSVGImageFromFilePath(final SVGGraphics2D g2dOriginal, final Context context, final int imageSize, final String filePath, 
			final int containerIndex, final int localState, final int value, final int hiddenValue, final int rotation, final boolean secondary)
	{		
		SVGGraphics2D g2d = g2dOriginal;

		final int scaledImageSizeX = (int) (imageSize * scaleX);
		final int scaledImageSizeY = (int) (imageSize * scaleY);
		final int scaledGraphicsSize = (int) (imageSize * scale(context, containerIndex, localState, value));
		
		g2d = getBackground(g2d, context, containerIndex, localState, value, imageSize);
		
		if (filePath != null)
		{
			if (Arrays.asList(ImageConstants.customImageKeywords).contains(filePath))
			{
				int posnX = (imageSize - scaledImageSizeX)/2;
				int posnY = (imageSize - scaledImageSizeX)/2;
				
				// TODO not sure exactly why this needs to be done for scale > 1.0
				if (posnX < 0)
					posnX = 0;
				if (posnY < 0)
					posnY = 0;
				
				if (filePath.equalsIgnoreCase("ball") || filePath.equalsIgnoreCase("seed"))
				{
					if (scaledImageSizeX > 1)
						ImageProcessing.ballImage(g2d, posnX, posnY, scaledImageSizeX / 2, fillColour);
				}
				else if (filePath.equalsIgnoreCase("marker"))
				{
					if (scaledImageSizeX > 1)
						ImageProcessing.markerImage(g2d, posnX, posnY, scaledImageSizeX / 2, fillColour);
				}
				else if (filePath.equalsIgnoreCase("ring"))
				{
					if (scaledImageSizeX > 1)
						ImageProcessing.ringImage(g2d, posnX, posnY, scaledImageSizeX, fillColour);
				}
				else if (filePath.equalsIgnoreCase("chocolate"))
				{
					if (scaledImageSizeX > 1)
						ImageProcessing.chocolateImage(g2d, scaledImageSizeX, 4, fillColour);
				}
			} 
			else 
			{
				int offsetDistance = 0;
				if (scaledImageSizeX < imageSize)
					offsetDistance = (imageSize-scaledImageSizeX)/2;
				
				if (showValue.isOffsetImage() || showLocalState.isOffsetImage())
				{
					if (showLocalState.getLocationType() == ValueLocationType.Corner || showValue.getLocationType() == ValueLocationType.Corner)
						SVGtoImage.loadFromFilePath
						(
							g2d, filePath, new Rectangle(offsetDistance + (int)(scaledGraphicsSize * 0.1), offsetDistance + (int)(scaledGraphicsSize * 0.15), scaledImageSizeX, scaledImageSizeY),
							edgeColour, fillColour, rotation
						);
					else if (showLocalState.getLocationType() == ValueLocationType.Top || showValue.getLocationType() == ValueLocationType.Top)
						SVGtoImage.loadFromFilePath
						(
							g2d, filePath, 
							new Rectangle(offsetDistance, offsetDistance + (int)(scaledGraphicsSize * 0.15), scaledImageSizeX, scaledImageSizeY), 
							edgeColour, fillColour, rotation
						);
				}
				else
				{
					SVGtoImage.loadFromFilePath
					(
						g2d, filePath, new Rectangle(offsetDistance, offsetDistance, scaledImageSizeX, scaledImageSizeY), 
						edgeColour, fillColour, rotation
					);
				}
			}
		}
		else
		{
			final Font valueFont = new Font("Arial", Font.BOLD, (int) (scaledGraphicsSize * 0.7));
			g2d.setColor(fillColour);
			g2d.setFont(valueFont);
			StringUtil.drawStringAtPoint(g2d, svgName, null, new Point(g2d.getWidth()/2,g2d.getHeight()/2), true);
		}
		
		g2d = getForeground(g2d, context, containerIndex, localState, value, imageSize);
		
		// Draw local state or value on piece
		g2d = displayNumberOnPiece(g2d, localState, scaledGraphicsSize, showLocalState.getLocationType(), showLocalState.isValueOutline());
		g2d = displayNumberOnPiece(g2d, value, scaledGraphicsSize, showValue.getLocationType(), showValue.isValueOutline());
		
		return g2d;
	}
	
	//----------------------------------------------------------------------------
	
	/** 
	 * Draws the local state or value on the piece if specified in metadata
	 */
	private SVGGraphics2D displayNumberOnPiece(final SVGGraphics2D g2d, final int value, final int scaledGraphicsSize, final ValueLocationType valueLocation, final boolean valueOutline)
	{
		if (value < 0)
			return g2d;
		
		final String printvalue = Integer.toString(value);
//		String printvalue = "?";
//		if (value >= 0)
//			printvalue = Integer.toString(value);
		
		// Draw the state/value of the piece in its top left corner.
		if (valueLocation == ValueLocationType.Corner)
		{
			final Font valueFontCorner = new Font("Arial", Font.BOLD, scaledGraphicsSize/4);
			g2d.setColor(secondaryColour);
			g2d.setFont(valueFontCorner);
			
			final Rectangle2D rect = g2d.getFont().getStringBounds(printvalue, g2d.getFontRenderContext());
			if (valueOutline)
				StringUtil.drawStringAtPoint(g2d, printvalue, null, new Point((int) (scaledGraphicsSize * 0.1 + rect.getWidth()/2.5),(int) (rect.getHeight()/2)), true);
			else
				g2d.drawString(printvalue, (int)(scaledGraphicsSize * 0.1), (int) (rect.getHeight()));
		}
		// Draw the state/value of the piece above it.
		else if (valueLocation == ValueLocationType.Top)
		{
			final Font valueFontCorner = new Font("Arial", Font.BOLD, scaledGraphicsSize/4);
			g2d.setColor(secondaryColour);
			g2d.setFont(valueFontCorner);
			
			final Rectangle2D rect = g2d.getFont().getStringBounds(printvalue, g2d.getFontRenderContext());
			if (valueOutline)
				StringUtil.drawStringAtPoint(g2d, printvalue, null, new Point((int) (scaledGraphicsSize * 0.5),(int) (rect.getHeight()/1.5)), true);
			else
				g2d.drawString(printvalue, (int)(scaledGraphicsSize * 0.5 - rect.getWidth()/2 - 1), (int) (rect.getHeight()));
		}
		// Draw the state/value of the piece on top of it.
		else if (valueLocation == ValueLocationType.Middle)
		{
			final Font valueFontMiddle = new Font("Arial", Font.BOLD, scaledGraphicsSize/2);
			g2d.setColor(secondaryColour);
			g2d.setFont(valueFontMiddle);

			final Rectangle2D rect = g2d.getFont().getStringBounds(printvalue, g2d.getFontRenderContext());
			if (valueOutline)
				StringUtil.drawStringAtPoint(g2d, printvalue, null, new Point((int) (scaledGraphicsSize * 0.5),(int) (scaledGraphicsSize * 0.5)), true);
			else
				g2d.drawString(printvalue, (int) (scaledGraphicsSize * 0.5 - rect.getWidth()/2 - 1), (int) (scaledGraphicsSize * 0.5 + rect.getHeight()/3 - 1));
		}
		
		return g2d;
	}
	
	//----------------------------------------------------------------------------
	
}
