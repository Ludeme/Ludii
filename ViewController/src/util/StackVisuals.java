package util;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import bridge.Bridge;
import game.equipment.container.Container;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import metadata.graphics.util.PieceStackType;
import metadata.graphics.util.StackPropertyType;
import other.context.Context;
import other.location.Location;
import other.move.Move;

/**
 * Functions relating to the visuals of pieces in a stack.
 * 
 * @author Matthew.Stephenson
 */
public class StackVisuals 
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Get the offset distance to draw the piece as determined by its stack type.
	 */
	public static Point2D.Double calculateStackOffset(final Bridge bridge, final Context context, final Container container, final PieceStackType componentStackType, final int cellRadiusPixelsOriginal, final int level, final int site, final SiteType siteType, final int stackSize, final int state) 
	{
		double stackOffsetX = 0.0;
		double stackOffsetY = 0.0;
		
		final int cellRadiusPixels = (int) (cellRadiusPixelsOriginal * context.game().metadata().graphics().stackMetadata(context, container, site, siteType, state, StackPropertyType.Scale));
		final int stackLimit = (int) context.game().metadata().graphics().stackMetadata(context, container, site, siteType, state,  StackPropertyType.Limit);
		
		final int stackOffsetAmount = (int)(0.4 * cellRadiusPixels);
		final double fullPieceStackScale = 4.8;

		if (componentStackType == PieceStackType.Ground)
		{
			// Stack the pieces in a square arrangement around the middle of the site.
			if (level == 0)
			{
				stackOffsetX = cellRadiusPixels / 2;
				stackOffsetY = cellRadiusPixels / 2;
			}
			if (level == 1)
			{
				stackOffsetX = -cellRadiusPixels / 2;
				stackOffsetY = cellRadiusPixels / 2;
			}
			if (level == 2)
			{
				stackOffsetX = cellRadiusPixels / 2;
				stackOffsetY = -cellRadiusPixels / 2;
			}
			if (level == 3)
			{
				stackOffsetX = -cellRadiusPixels / 2;
				stackOffsetY = -cellRadiusPixels / 2;
			}
		}
		else if (componentStackType == PieceStackType.None || componentStackType == PieceStackType.Count || componentStackType == PieceStackType.CountColoured)
		{
			// do nothing
		}
		else if (componentStackType == PieceStackType.Fan)
		{
			// Stack the piece horizontally
			stackOffsetX = level * stackOffsetAmount;
		}
		else if (componentStackType == PieceStackType.TowardsCenter)
		{
			// Stack the piece towards the center of the board
			final Point2D currentPoint = container.topology().getGraphElement(siteType, ContainerUtil.getContainerSite(context, site, siteType)).centroid();
			final double xDistanceToCenter = container.topology().centrePoint().getX() - currentPoint.getX();
			final double yDistanceToCenter = container.topology().centrePoint().getY() - currentPoint.getY();
			final double currentPointAngle = Math.atan(Math.abs(xDistanceToCenter)/Math.abs(yDistanceToCenter));
			stackOffsetX = Math.sin(currentPointAngle) * stackOffsetAmount * fullPieceStackScale * level;
			stackOffsetY = Math.cos(currentPointAngle) * stackOffsetAmount * fullPieceStackScale * level;
			if (xDistanceToCenter < 0)
				stackOffsetX *= -1;
			if (yDistanceToCenter > 0)
				stackOffsetY *= -1;
		}
		else if (componentStackType == PieceStackType.FanAlternating)
		{
			// Stack the piece horizontally
			if (level%2 == 0)
				stackOffsetX = level * (stackOffsetAmount/2) + stackOffsetAmount/2;
			else
				stackOffsetX = (level+1) * -(stackOffsetAmount/2) + stackOffsetAmount/2;
		}
		else if (componentStackType == PieceStackType.Ring)
		{
			int cellRadiusStack = cellRadiusPixels;
			
			// If on a cell, recompute cell radius specifically for this cell.
			if (siteType.equals(SiteType.Cell))
				cellRadiusStack = (int) (GraphUtil.calculateCellRadius(container.topology().cells().get(site)) * bridge.getContainerStyle(container.index()).placement().getWidth());
			
			int stackSizeNew = stackSize;
			if (stackSizeNew == 0)
				stackSizeNew = 1;
			
			stackOffsetX = 0.7 * cellRadiusStack * Math.cos(Math.PI*2 * level / stackSizeNew);
			stackOffsetY = 0.7 * cellRadiusStack * Math.sin(Math.PI*2 * level / stackSizeNew);
		}
		else if (componentStackType == PieceStackType.Backgammon)
		{
			// Stack the pieces in columns of 5, repeated on top of each other.
			final int lineNumber = level%stackLimit;
			final int repeatNumber = level/stackLimit;
			if (site < container.numSites()/2)
			{
				stackOffsetY = -lineNumber * stackOffsetAmount*fullPieceStackScale - repeatNumber*stackOffsetAmount;
			}
			else
			{
				stackOffsetY = lineNumber * stackOffsetAmount*fullPieceStackScale - repeatNumber*stackOffsetAmount;
			}
		}
		else if (container.isHand())
		{
			// Stack the pieces in a deck like fashion
			stackOffsetX = level * stackOffsetAmount/30;
			stackOffsetY = level * stackOffsetAmount/30;
		}
		else
		{
			// Stack the piece normally
			stackOffsetY = -level * stackOffsetAmount;
		}
		
		return new Point2D.Double(stackOffsetX, stackOffsetY);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns the min and max level for a selected location, to be used when drawing stacked pieces.
	 */
	public static int[] getLevelMinAndMax(final Moves legal, final Location selectedLocation)
	{
		//System.out.println(selectedLocation.level());
		final ArrayList<Move> allMovesFromThisSite = new ArrayList<>();
		for (final Move m : legal.moves())
		{
			if 
			(
				m.getFromLocation().site() == selectedLocation.site() 
				&& 
				m.getFromLocation().siteType() == selectedLocation.siteType()
				&&
				m.levelMinNonDecision() == selectedLocation.level()
				&&
				m.levelMaxNonDecision() >= selectedLocation.level()
			)
			{
				allMovesFromThisSite.add(m);
			}
		}
		int levelMax = selectedLocation.level();
		if (allMovesFromThisSite.size() > 0) 
		{
			levelMax = allMovesFromThisSite.get(0).levelMaxNonDecision();
			for (final Move m : allMovesFromThisSite)
			{
				if (m.levelMaxNonDecision() != levelMax)
				{
					levelMax = selectedLocation.level();
					break;
				}
			}
		}
		
		final int[] levelMinMax = {selectedLocation.level(), levelMax};
		return levelMinMax;
	}
	
	//-------------------------------------------------------------------------
	
}
