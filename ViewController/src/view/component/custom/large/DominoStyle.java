package view.component.custom.large;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import game.equipment.component.Component;
import game.equipment.container.board.Board;
import gnu.trove.list.array.TIntArrayList;
import other.context.Context;
import util.HiddenUtil;

/**
 * Implementation of domino component style. 
 * 
 * @author matthew.stephenson
 */
public class DominoStyle extends LargePieceStyle
{
	public DominoStyle(final Bridge bridge, final Component component) 
	{
		super(bridge, component);
	}
	
	//----------------------------------------------------------------------------
	
	@Override
	protected SVGGraphics2D drawLargePieceVisuals(final SVGGraphics2D g2dOriginal, final TIntArrayList cellLocations, final int imageSize,
			final int imageX, final int imageY, final int state, final int value, final Context context, final boolean secondary, 
			final int hiddenValue, final int rotation, final Board boardForLargePiece, final int containerIndex)
	{
		final SVGGraphics2D g2d = super.drawLargePieceVisuals(g2dOriginal, cellLocations, imageSize, imageX, imageY, state, value, 
				context, secondary, hiddenValue, rotation, boardForLargePiece, containerIndex);

		Point2D currentPoint = new Point2D.Double();
		
		double minCellLocationX = 99999;
		double maxCellLocationX = -99999;
		double minCellLocationY = 99999;
		double maxCellLocationY = -99999;
		for (int i = 0; i < cellLocations.size(); i++)
		{
			currentPoint = boardForLargePiece.topology().cells().get(cellLocations.get(i)).centroid();
			if (currentPoint.getX() < minCellLocationX)
			{
				minCellLocationX = currentPoint.getX();
			}
			if (currentPoint.getX() > maxCellLocationX)
			{
				maxCellLocationX = currentPoint.getX();
			}
			if (currentPoint.getY() < minCellLocationY)
			{
				minCellLocationY = currentPoint.getY();
			}
			if (currentPoint.getY() > maxCellLocationY)
			{
				maxCellLocationY = currentPoint.getY();
			}
		}

		maxCellLocationX -= minCellLocationX;
		minCellLocationX -= minCellLocationX;
		maxCellLocationY -= minCellLocationY;
		minCellLocationY -= minCellLocationY;
		
		final int strokeWidth = imageSize/5;
		
		maxCellLocationX = maxCellLocationX + imageSize;
		maxCellLocationY = maxCellLocationY + imageSize;
		g2d.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
		final GeneralPath path = new GeneralPath();
		
		// domino outline
		path.moveTo(minCellLocationX + strokeWidth/2, minCellLocationY + strokeWidth/2);
		path.lineTo(minCellLocationX + strokeWidth/2, maxCellLocationY - strokeWidth/2);
		path.lineTo(maxCellLocationX - strokeWidth/2, maxCellLocationY - strokeWidth/2);
		path.lineTo(maxCellLocationX - strokeWidth/2, minCellLocationY + strokeWidth/2);
		path.lineTo(minCellLocationX + strokeWidth/2, minCellLocationY + strokeWidth/2);
		path.closePath();
		
		final double xDistance = maxCellLocationX-minCellLocationX;
		final double yDistance = maxCellLocationY-minCellLocationY;
		
		// domino center line
		if (xDistance > yDistance)
		{
			path.moveTo(minCellLocationX + (xDistance)/2, minCellLocationY);
			path.lineTo(minCellLocationX + (xDistance)/2, maxCellLocationY);
		}
		else
		{
			path.moveTo(minCellLocationX, minCellLocationY + (yDistance)/2);
			path.lineTo(maxCellLocationX, minCellLocationY + (yDistance)/2);
		}
		
		g2d.setColor(Color.BLACK);
		g2d.draw(path);

		final Point2D.Double[] dominoSides = {new Point2D.Double(minCellLocationX + imageSize, minCellLocationY + imageSize), new Point2D.Double(maxCellLocationX - imageSize, maxCellLocationY - imageSize)};
		final int[] dominoValues = {0, 0};
		
		if (state < 2)
		{
			dominoValues[0] = component.getValue();
			dominoValues[1] = component.getValue2();
		}
		else
		{
			dominoValues[1] = component.getValue();
			dominoValues[0] = component.getValue2();
		}
		
		for (int i = 0; i < dominoSides.length; i++)
		{
			if (!HiddenUtil.intToBitSet(hiddenValue).get(HiddenUtil.hiddenWhatIndex))
			{
				drawPips((int) dominoSides[i].x, (int) dominoSides[i].y, dominoValues[i], imageSize*2, g2d);
			}
			else
			{
				// If the what of the dominoe is hidden, draw a question mark.
				final Font valueFont = new Font("Arial", Font.BOLD, (imageSize));
				g2d.setFont(valueFont);
				final Rectangle2D rect = valueFont.getStringBounds("?", g2d.getFontRenderContext());
				g2d.drawString("?", (int)(dominoSides[i].x - rect.getWidth()/2) , (int)(dominoSides[i].y + rect.getHeight()/3));
			}
		}
		
		return g2d;
	}
	
	//----------------------------------------------------------------------------
	
	/**
	 * Draws pips (or number if too many) on the domino.
	 */
	private static void drawPips(final int positionX, final int positionY, final int pipValue, final int imageSize, final Graphics2D g2d)
	{
		final int maxDominoValueForPips = 9;
		final double pipSpacingMultiplier = 0.8;
		final double pipSizeFraction = 0.15;
		final Point2D pipTranslation = new Point2D.Double(0, 0);

		if (pipValue <= maxDominoValueForPips)
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
			case 7:
				pipPositions.add(new Point(dx + dw, dy + dh));
				pipPositions.add(new Point(dx - dw, dy - dw));
				pipPositions.add(new Point(dx - dw, dy + dh));
				pipPositions.add(new Point(dx + dw, dy - dw));
				pipPositions.add(new Point(dx, dy + dh));
				pipPositions.add(new Point(dx, dy - dw));
				pipPositions.add(new Point(dx, dy));
				break;
			case 8:
				pipPositions.add(new Point(dx + dw, dy + dh));
				pipPositions.add(new Point(dx - dw, dy - dw));
				pipPositions.add(new Point(dx - dw, dy + dh));
				pipPositions.add(new Point(dx + dw, dy - dw));
				pipPositions.add(new Point(dx, dy + dh));
				pipPositions.add(new Point(dx, dy - dw));
				pipPositions.add(new Point(dx + dw, dy));
				pipPositions.add(new Point(dx - dw, dy));
				break;
			case 9:
				pipPositions.add(new Point(dx + dw, dy + dh));
				pipPositions.add(new Point(dx - dw, dy - dw));
				pipPositions.add(new Point(dx - dw, dy + dh));
				pipPositions.add(new Point(dx + dw, dy - dw));
				pipPositions.add(new Point(dx, dy + dh));
				pipPositions.add(new Point(dx, dy - dw));
				pipPositions.add(new Point(dx + dw, dy));
				pipPositions.add(new Point(dx - dw, dy));
				pipPositions.add(new Point(dx, dy));
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

		else
		{
			final Font valueFont = new Font("Arial", Font.BOLD, imageSize / 2);
			g2d.setColor(Color.BLACK);
			g2d.setFont(valueFont);
			final Rectangle2D rect = valueFont.getStringBounds(Integer.toString(pipValue), g2d.getFontRenderContext());
			try
			{
				g2d.drawString(Integer.toString(pipValue), (int) (positionX - rect.getWidth() / 2),
						(int) (positionY + rect.getHeight() / 2));
			}
			catch (final Exception e)
			{
				// carry on
			}
		}
	}
	
	//----------------------------------------------------------------------------
	
}
