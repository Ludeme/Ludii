package view.container.aspects.placement;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.List;

import bridge.Bridge;
import other.context.Context;
import other.topology.Cell;
import other.topology.Vertex;
import view.container.BaseContainerStyle;

public class HandPlacement extends ContainerPlacement
{
	public HandPlacement(final Bridge bridge, final BaseContainerStyle containerStyle) 
	{
		super(bridge, containerStyle);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void setPlacement(final Context context, final Rectangle placement) 
	{
		Rectangle newPlacement = placement;
		
		// Metadata can override the placement of the hands.
		final Rectangle2D customPlacement = context.game().metadata().graphics().handPlacement(container().owner(), context);
		final boolean handVertical = context.game().metadata().graphics().handVertical(container().owner(), context);
		if (customPlacement != null)
		{
			final int boardViewWidth = (int) bridge.getContainerStyle(0).unscaledPlacement().getWidth();
			final int boardViewHeight = (int) bridge.getContainerStyle(0).unscaledPlacement().getHeight();
			
			final double x = customPlacement.getX();
			final double y = customPlacement.getY() * (handVertical ? 1 : -1);
			double width = customPlacement.getWidth();
			double height = 1.0;
			
			if (handVertical)
			{
				width = 1.0;
				height = customPlacement.getHeight();
			}
			
			newPlacement = new Rectangle((int)(boardViewWidth * x), (int)(boardViewHeight * y), (int)(boardViewWidth * width), (int)(boardViewHeight * height));
		}
		
		this.placement = newPlacement;
		
		// Longest and shortest sides are based on the orientation of the hand.
		int longestSide = (int) newPlacement.getWidth();
		int shortestSide = (int) newPlacement.getHeight();
		if (handVertical)
		{
			longestSide = (int) newPlacement.getHeight();
			shortestSide = (int) newPlacement.getWidth();
		}

		// Determine the cell radius of the hand.
		setCellRadiusPixels((int)(0.6 * shortestSide) / 2);
		setCellRadius((double)cellRadiusPixels() / longestSide);
		if (cellRadiusPixels() > longestSide / container().numSites() / 2)
		{
			setCellRadiusPixels(longestSide / container().numSites() / 2);
			setCellRadius(1.0 / container().numSites() / 2);
		}
		
		setHandLocations(context, customPlacement!=null, handVertical);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Sets the sites for the hand container associated with this placement aspect.
	 */
	protected void setHandLocations(final Context context, final boolean customPlacement, final boolean verticalPlacement)
	{
		int persistentSiteCount = 0;
		final List<Cell> sites = container().topology().cells();
		
		double xBuffer = 0;
		final double yBuffer = 0;
		
		// if this is a shared hand, center the components in the container width
		if (container().owner() > context.game().players().count() && !customPlacement)
		{
			final double totalContainerCellWidth = cellRadiusPixels() * 2.0 * sites.size();
			final double difference = placement.getWidth() - totalContainerCellWidth;
			xBuffer = (difference/2.0) / placement.getWidth();
		}
		
		// Check this site
		for (int site = 0; site < sites.size(); site++)
		{
			double xPosn = cellRadius() * 2 * (persistentSiteCount + 0.5) + xBuffer;
			double yPosn = 0;
			
			// If the hand is vertically oriented.
			if (verticalPlacement)
			{
				xPosn = 0;
				yPosn = cellRadius() * 2 * (persistentSiteCount + 0.5) + yBuffer;
			}
			
			topology().cells().get(site).setCentroid(xPosn, yPosn, 0);
			
			// Normalise all cell vertex positions.
			double minX = 99999;
			double minY = 99999;
			double maxX = -99999;
			double maxY = -99999;
			for (final Vertex vertex : topology().cells().get(site).vertices())
			{
				if (vertex.centroid().getX() < minX)
					minX = vertex.centroid().getX();
				if (vertex.centroid().getX() > maxX)
					maxX = vertex.centroid().getX();
				if (vertex.centroid().getY() < minY)
					minY = vertex.centroid().getY();
				if (vertex.centroid().getY() > maxY)
					maxY = vertex.centroid().getY();
			}
			for (final Vertex vertex : topology().cells().get(site).vertices())
			{
				final double normalisedVx = (vertex.centroid().getX() - minX) / (maxX - minX);
				final double normalisedVy = (vertex.centroid().getY() - minY) / (maxY - minY);
				vertex.setCentroid(normalisedVx, normalisedVy, 0);
			}
			
			final double widthToHeightRatio = cellRadius()*(placement.getWidth()/placement.getHeight());
			
			for (final Vertex vertex : topology().cells().get(site).vertices())
			{
				final double vx = vertex.centroid().getX();
				final double vy = vertex.centroid().getY();
				vertex.setCentroid(xPosn - cellRadius() + vx * cellRadius() * 2,
						yPosn + widthToHeightRatio - vy * widthToHeightRatio * 2, 0);
			}

			persistentSiteCount++;
		}
	}
	
	//-------------------------------------------------------------------------

}
