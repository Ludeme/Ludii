package view.component.custom;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import game.equipment.component.Component;
import other.context.Context;

/**
 * Implementation of die component style. 
 * 
 * @author matthew.stephenson
 */
public class DieStyle extends PieceStyle
{
	public DieStyle(final Bridge bridge, final Component component) 
	{
		super(bridge, component);
		setDefaultDiceDesign();
	}
	
	//----------------------------------------------------------------------------
	
	/**
	 * Sets the name of the dice component based on the number of faces it has.
	 */
	protected void setDefaultDiceDesign() 
	{
		if (component.getNumFaces() == 6 || component.getNumFaces() == 10 || component.getNumFaces() == 12)
			component.setNameWithoutNumber("square");
		else if (component.getNumFaces() == 4)
			component.setNameWithoutNumber("rectangle");
		else if (component.getNumFaces() == 2)
			component.setNameWithoutNumber("paddle");
		else
			component.setNameWithoutNumber("triangle");
	}

	//----------------------------------------------------------------------------
	
	@Override
	protected SVGGraphics2D getSVGImageFromFilePath(final SVGGraphics2D g2d, final Context context, final int imageSize, final String filePath, 
			final int containerIndex, final int localState, final int value, final int hiddenValue, final int rotation, final boolean secondary)
	{
		final SVGGraphics2D diceImage = super.getSVGImageFromFilePath(g2d, context, imageSize, filePath, containerIndex, localState, value, hiddenValue, rotation, secondary);
		final Point diceCenter = new Point(diceImage.getWidth()/2, diceImage.getHeight()/2);
		final int diceValue = component.getFaces()[localState];
		
		if (context.game().metadata().graphics().pieceForeground(context, component.owner(), component.name(), containerIndex, localState, value).size() == 0)
			drawPips(context, diceCenter.x, diceCenter.y, diceValue, imageSize, diceImage);
		
		return diceImage;
	}

	//----------------------------------------------------------------------------

	/**
	 * Draws pips (or number if too many) on the dice.
	 */
	public void drawPips(final Context context, final int positionX, final int positionY, final int pipValue, final int imageSize, final Graphics2D g2d)
	{
		final int maxDieValueForPips = 6;
		double pipSpacingMultiplier = 0.8;
		double pipSizeFraction = 0.15;
		Point2D pipTranslation = new Point2D.Double(0, 0);

		if (svgName.toLowerCase().equals("triangle"))
		{
			pipSpacingMultiplier = 0.4;
			pipSizeFraction = 0.1;
			pipTranslation = new Point2D.Double(0, 0.15);
		}
		if (svgName.toLowerCase().equals("rectangle"))
		{
			pipSpacingMultiplier = 0.4;
			pipSizeFraction = 0.1;
		}

		// draw pips on dice if 6 or less pips, unless metadata says otherwise.
		if (pipValue <= maxDieValueForPips && !context.game().metadata().graphics().noDicePips())
		{
			final double pipSize = (int) (imageSize * pipSizeFraction);
			
			final int dw = (int) (imageSize * pipSpacingMultiplier / 2 - pipSize);
			final int dh = (int) (imageSize * pipSpacingMultiplier / 2 - pipSize);

			final int dx = (int) (positionX + (imageSize * pipTranslation.getX()));
			final int dy = (int) (positionY + (imageSize * pipTranslation.getY()));

			final ArrayList<Point> pipPositions = new ArrayList<>();

			switch (pipValue)
			{
			case 1:
				pipPositions.add(new Point(dx, dy));
				break;
			case 2:
				pipPositions.add(new Point(dx + dw, dy + dh));
				pipPositions.add(new Point(dx - dw, dy - dw));
				break;
			case 3:
				pipPositions.add(new Point(dx, dy));
				pipPositions.add(new Point(dx + dw, dy + dh));
				pipPositions.add(new Point(dx - dw, dy - dw));
				break;
			case 4:
				pipPositions.add(new Point(dx + dw, dy + dh));
				pipPositions.add(new Point(dx - dw, dy - dw));
				pipPositions.add(new Point(dx - dw, dy + dh));
				pipPositions.add(new Point(dx + dw, dy - dw));
				break;
			case 5:
				pipPositions.add(new Point(dx + dw, dy + dh));
				pipPositions.add(new Point(dx - dw, dy - dw));
				pipPositions.add(new Point(dx - dw, dy + dh));
				pipPositions.add(new Point(dx + dw, dy - dw));
				pipPositions.add(new Point(dx, dy));
				break;
			case 6:
				pipPositions.add(new Point(dx + dw, dy + dh));
				pipPositions.add(new Point(dx - dw, dy - dw));
				pipPositions.add(new Point(dx - dw, dy + dh));
				pipPositions.add(new Point(dx + dw, dy - dw));
				pipPositions.add(new Point(dx, dy + dh));
				pipPositions.add(new Point(dx, dy - dw));
				break;
			}

			for (int numPips = 0; numPips < pipPositions.size(); numPips++)
			{
				final int pipX = pipPositions.get(numPips).x;
				final int pipY = pipPositions.get(numPips).y;

				g2d.setColor(Color.BLACK);
				g2d.fillOval(pipX - (int) pipSize / 2, pipY - (int) pipSize / 2, (int) pipSize, (int) pipSize);
			}
		}

		// if more than 6 pips, draw the number on the dice instead.
		else
		{
			final Font valueFont = new Font("Arial", Font.BOLD, imageSize / 3);
			g2d.setColor(Color.BLACK);
			g2d.setFont(valueFont);
			final Rectangle2D rect = valueFont.getStringBounds(Integer.toString(pipValue), g2d.getFontRenderContext());
			try
			{
				if (svgName.toLowerCase().equals("triangle"))
				{
					g2d.drawString(Integer.toString(pipValue), (int) (positionX - rect.getWidth() / 2),
						(int) (positionY + rect.getHeight() / 1.5));
				}	
				else
				{
					g2d.drawString(Integer.toString(pipValue), (int) (positionX - rect.getWidth() / 2),
							(int) (positionY + rect.getHeight() / 2));
				}
			}
			catch (final Exception e)
			{
				// carry on
			}
		}
	}
	
}
