package view.container.aspects.tracks;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import bridge.Bridge;
import game.Game;
import game.equipment.container.board.Track;
import game.types.board.SiteType;
import main.Constants;
import other.context.Context;
import other.topology.TopologyElement;
import util.ArrowUtil;
import view.container.ContainerStyle;

public class ContainerTrack 
{
	/**
	 * Draw all tracks on the board, that the user has opted to show.
	 */
	public void drawTracks(final Bridge bridge, final Graphics2D g2d, final Context context, final ContainerStyle containerStyle) 
	{
		for (int i = 0; i < bridge.settingsVC().trackNames().size(); i++)
			if (bridge.settingsVC().trackShown().get(i).booleanValue())
				drawTrackArrow(bridge, g2d, bridge.settingsVC().trackNames().get(i), context, containerStyle);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Draw arrows representing a specific track on the board.
	 */
	@SuppressWarnings("static-method")
	public void drawTrackArrow(final Bridge bridge, final Graphics2D g2d, final String trackName, final Context context, final ContainerStyle containerStyle)
	{
		final Game game = context.game();
		
		final int cellRadiusPixels = containerStyle.cellRadiusPixels();
		final Rectangle placement = containerStyle.placement();
		
		final int totalTrackNumber = context.board().tracks().size();
		final double shiftAmount = cellRadiusPixels / (totalTrackNumber+2) / 2.0;
		int pushAmount = -(totalTrackNumber)+2;
		boolean startFromHand = false;
		boolean endToHand = false;
		
		for (final Track track : context.board().tracks())
		{
			pushAmount++;

			if (track.name().equals(trackName.substring(11)))
			{
				for (int e = 0; e < track.elems().length; e++)
				{
					int from = track.elems()[e].site;
					int to = track.elems()[e].next;

					if (to <= game.equipment().totalDefaultSites()
							&& from <= game.equipment().totalDefaultSites() && to > -1 && from > -1)
					{
						final int fromContainerIdx = context.containerId()[from];
						from -= context.sitesFrom()[fromContainerIdx];
						final int toContainerIdx = context.containerId()[to];
						to -= context.sitesFrom()[toContainerIdx];
						
						List<? extends TopologyElement> fromCells = new ArrayList<>();
						List<? extends TopologyElement> toCells = new ArrayList<>();
						
						if (context.board().defaultSite() == SiteType.Vertex)
						{
							fromCells = game.equipment().containers()[fromContainerIdx].topology().vertices();
							toCells = game.equipment().containers()[toContainerIdx].topology().vertices();
						}
						else if (context.board().defaultSite() == SiteType.Cell)
						{
							fromCells = game.equipment().containers()[fromContainerIdx].topology().cells();
							toCells = game.equipment().containers()[toContainerIdx].topology().cells();
						}
						else
						{
							fromCells = game.equipment().containers()[fromContainerIdx].topology().edges();
							toCells = game.equipment().containers()[toContainerIdx].topology().edges();
						}
						
						if (context.containers()[fromContainerIdx].isHand())
						{
							startFromHand = true;
						}
						if (context.containers()[toContainerIdx].isHand())
						{
							endToHand = true;
						}

						// don't display arrow if from or to hand
						if (!context.containers()[fromContainerIdx].isHand()
								&& !context.containers()[toContainerIdx].isHand())
						{

							final int maxRadius = cellRadiusPixels;
							final int minRadius = maxRadius / 4;

							int fromX = (int) (placement.x + fromCells.get(from).centroid().getX() * placement.width);
							int fromY = (int) (placement.y + placement.height - (fromCells.get(from).centroid().getY() * placement.height));
							
							int toX = (int) (placement.x + toCells.get(to).centroid().getX() * placement.width);
							int toY = (int) (placement.y + placement.height - (toCells.get(to).centroid().getY() * placement.height));
							
							int x1 = fromX;
							int x2 = toX;
							int y1 = fromY;
							int y2 = toY;

							double L = Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
							fromX = (int) (x1 + pushAmount * shiftAmount * (y2-y1) / L);
							toX = (int) (x2 + pushAmount * shiftAmount * (y2-y1) / L);
							fromY = (int) (y1 + pushAmount * shiftAmount * (x1-x2) / L);
							toY = (int) (y2 + pushAmount * shiftAmount * (x1-x2) / L);
							
							final Point2D.Double toPoint = new Point2D.Double(toX,toY);
							final Point2D.Double fromPoint = new Point2D.Double(fromX,fromY);
							
							final double arrowThicknessScale = 0.3;

							final int arrowWidth = Math
									.max((int) ((minRadius + arrowThicknessScale * (maxRadius - minRadius)) / 2.5), 1);

							try
							{
								g2d.setColor(bridge.settingsColour().playerColour(context, track.owner()));
							}
							catch (final Exception E)
							{
								g2d.setColor(new Color(0, 0, 0));
							}

							if (track.owner() == 0)
							{
								g2d.setColor(new Color(0, 0, 0));
							}

							int fromNext = -1;
							int toNext = -1;
							int fromPrev = -1;
							int toPrev = -1;
							
							if ((e < track.elems().length - 1 && !endToHand) || e < track.elems().length - 2)
							{
								fromNext = track.elems()[e + 1].site;
								toNext = track.elems()[e + 1].next;
								
								if (toNext < context.board().topology().cells().size()
										&& toNext > Constants.OFF)
								{
									final int fromContainerIdxNext = context.containerId()[fromNext];
									fromNext -= context.sitesFrom()[fromContainerIdxNext];

									final int toContainerIdxNext = context.containerId()[toNext];
									toNext -= context.sitesFrom()[toContainerIdxNext];
								}
							}
							
							if ((e > 0 && !startFromHand) || e > 1)
							{
								fromPrev = track.elems()[e - 1].site;
								toPrev = track.elems()[e - 1].next;
								
								if (toPrev < context.board().topology().cells().size()
										&& toPrev > Constants.OFF)
								{
									final int fromContainerIdxPrev = context.containerId()[fromPrev];
									fromPrev -= context.sitesFrom()[fromContainerIdxPrev];

									final int toContainerIdxPrev = context.containerId()[toPrev];
									toPrev -= context.sitesFrom()[toContainerIdxPrev];
								}
							}
							
							Point2D.Double intersectionPointNext = new Point2D.Double();
							Point2D.Double intersectionPointPrev = new Point2D.Double();
							
							if (fromNext != -1 && toNext != -1)
							{
								try
								{									
									int fromNextX = (int) (placement.x + fromCells.get(fromNext).centroid().getX() * placement.width);
									int fromNextY = (int) (placement.y + placement.height - (fromCells.get(fromNext).centroid().getY() * placement.height));
									
									int toNextX = (int) (placement.x + toCells.get(toNext).centroid().getX() * placement.width);
									int toNextY = (int) (placement.y + placement.height - (toCells.get(toNext).centroid().getY() * placement.height));
									
									x1 = fromNextX;
									x2 = toNextX;
									y1 = fromNextY;
									y2 = toNextY;

									L = Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
									fromNextX = (int) (x1 + pushAmount * shiftAmount * (y2-y1) / L);
									toNextX = (int) (x2 + pushAmount * shiftAmount * (y2-y1) / L);
									fromNextY = (int) (y1 + pushAmount * shiftAmount * (x1-x2) / L);
									toNextY = (int) (y2 + pushAmount * shiftAmount * (x1-x2) / L);
									
									final Point2D.Double toNextPoint = new Point2D.Double(toNextX,toNextY);
									final Point2D.Double fromNextPoint = new Point2D.Double(fromNextX,fromNextY);
									
									intersectionPointNext = lineLineIntersection(fromPoint,toPoint,fromNextPoint,toNextPoint);
	
									if (intersectionPointNext != null)
									{
										toX = (int) intersectionPointNext.x;
										toY = (int) intersectionPointNext.y;
									}
								}
								catch (final Exception E)
								{
									//carry on
								}
							}
							if (fromPrev != -1 && toPrev != -1)
							{
								try
								{
									int fromPrevX = (int) (placement.x + fromCells.get(fromPrev).centroid().getX() * placement.width);
									int fromPrevY = (int) (placement.y + placement.height - (fromCells.get(fromPrev).centroid().getY() * placement.height));
									
									int toPrevX = (int) (placement.x + toCells.get(toPrev).centroid().getX() * placement.width);
									int toPrevY = (int) (placement.y + placement.height - (toCells.get(toPrev).centroid().getY() * placement.height));
									
									x1 = fromPrevX;
									x2 = toPrevX;
									y1 = fromPrevY;
									y2 = toPrevY;

									L = Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
									fromPrevX = (int) (x1 + pushAmount * shiftAmount * (y2-y1) / L);
									toPrevX = (int) (x2 + pushAmount * shiftAmount * (y2-y1) / L);
									fromPrevY = (int) (y1 + pushAmount * shiftAmount * (x1-x2) / L);
									toPrevY = (int) (y2 + pushAmount * shiftAmount * (x1-x2) / L);
									
									final Point2D.Double toPrevPoint = new Point2D.Double(toPrevX,toPrevY);
									final Point2D.Double fromPrevPoint = new Point2D.Double(fromPrevX,fromPrevY);
									
									intersectionPointPrev = lineLineIntersection(fromPoint,toPoint,fromPrevPoint,toPrevPoint);
	
									if (intersectionPointPrev != null)
									{
										fromX = (int) intersectionPointPrev.x;
										fromY = (int) intersectionPointPrev.y;
									}
								}
								catch (final Exception E)
								{
									//carry on
								}
							}

							ArrowUtil.drawArrow(g2d, fromX, fromY, toX, toY, arrowWidth, (Math.max(arrowWidth, 3)),
									(int) (1.75 * (Math.max(arrowWidth, 5))));
						}
					}
				}
			}

			pushAmount++;
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Return the point where two lines intersect
	 */
	protected static Point2D.Double lineLineIntersection(final Point2D.Double A, final Point2D.Double B, final Point2D.Double C, final Point2D.Double D) 
    { 
        // Line AB represented as a1x + b1y = c1 
        final double a1 = B.y - A.y; 
        final double b1 = A.x - B.x; 
        final double c1 = a1*(A.x) + b1*(A.y); 
       
        // Line CD represented as a2x + b2y = c2 
        final double a2 = D.y - C.y; 
        final double b2 = C.x - D.x; 
        final double c2 = a2*(C.x)+ b2*(C.y); 
       
        final double determinant = a1*b2 - a2*b1; 
       
        if (determinant == 0) 
        { 
            // The lines are parallel.
            return null;
        } 
        else
        { 
            final double x = (b2*c1 - b1*c2)/determinant; 
            final double y = (a1*c2 - a2*c1)/determinant; 
            return new Point2D.Double(x, y); 
        } 
    }
	
	//-------------------------------------------------------------------------
	
}
