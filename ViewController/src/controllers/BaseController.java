package controllers;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import bridge.Bridge;
import game.equipment.container.Container;
import game.types.board.SiteType;
import main.Constants;
import main.math.MathRoutines;
import metadata.graphics.util.PieceStackType;
import metadata.graphics.util.StackPropertyType;
import other.context.Context;
import other.location.FullLocation;
import other.location.Location;
import other.move.Move;
import other.state.container.ContainerState;
import other.topology.TopologyElement;
import other.topology.Vertex;
import util.StackVisuals;
import util.WorldLocation;
import view.container.ContainerStyle;

/**
 * Implementation of container controllers.
 * 
 * @author matthew.stephenson and mrraow and cambolbro
 */
public abstract class BaseController implements Controller
{
	protected final Container container;
	protected Bridge bridge;

	//-------------------------------------------------------------------------

	/**
	 * @param container
	 */
	public BaseController(final Bridge bridge, final Container container)
	{
		this.container = container;
		this.bridge = bridge;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The nearest Location for a given point, based on all possible locations.
	 */
	@Override
	public Location calculateNearestLocation(final Context context, final Point pt, final List<Location> legalLocations)
	{
		// Calculate information for all moves that could be made
		final ArrayList<WorldLocation> allLocations = new ArrayList<>();
		
		final ContainerStyle containerStyle = bridge.getContainerStyle(container.index());
		final ContainerState cs = context.state().containerStates()[container.index()];
		
		for (final Location location : legalLocations)
		{
			try
			{
				final int stackSize = cs.sizeStack(location.site(), location.siteType());
				final TopologyElement graphElement = bridge.getContainerStyle(container.index()).drawnGraphElement(location.site(), location.siteType());
				final PieceStackType componentStackType = PieceStackType.getTypeFromValue((int) context.metadata().graphics().stackMetadata(context, container, location.site(), location.siteType(), cs.state(location.site(), location.level(), location.siteType()), cs.value(location.site(), location.level(), location.siteType()), StackPropertyType.Type));
				final Point2D.Double offsetDistance = StackVisuals.calculateStackOffset(bridge, context,container, componentStackType, containerStyle.cellRadiusPixels(), location.level(), location.site(), location.siteType(), stackSize, cs.state(location.site(), location.level(), location.siteType()), cs.value(location.site(), location.level(), location.siteType()));
				final Point2D clickablePosition = new Point2D.Double(graphElement.centroid().getX() + offsetDistance.getX()/containerStyle.placement().getWidth(), graphElement.centroid().getY() - offsetDistance.getY()/containerStyle.placement().getHeight());
				if (legalLocations == null || legalLocations.contains(new FullLocation(location.site(), location.level(), location.siteType())))
					allLocations.add(new WorldLocation(new FullLocation(location.site(), location.level(), location.siteType()), clickablePosition));
			}
			catch (final Exception E)
			{
				// Probably just an invalid location for this container.
			}
		}
		
		return translateClicktoSite(pt, context, allLocations);
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Return the maximum distance that a click can be from a graph element.
	 */
	private double calculateFurthestDistance(final Context context)
	{
		double furthestPossibleDistance = 0;
		ContainerStyle containerStyle = bridge.getContainerStyle(container.index());
		
		if (containerStyle.ignorePieceSelectionLimit())
		{
			if (containerStyle.placement() != null)
				furthestPossibleDistance = Math.max(containerStyle.placement().getWidth(), containerStyle.placement().getHeight());
		}
		else
		{
			final double furthestDistanceMultiplier = bridge.settingsVC().furthestDistanceMultiplier();
			containerStyle = bridge.getContainerStyle(container.index());
			final double cellDistance = containerStyle.cellRadiusPixels() * furthestDistanceMultiplier;
			furthestPossibleDistance = Math.max(furthestPossibleDistance, cellDistance);
		}
		return furthestPossibleDistance;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Returns the Location for the site closest to pt, which was also in the legal Moves.
	 */
	protected Location translateClicktoSite(final Point pt, final Context context, final ArrayList<WorldLocation> validLocations) 
	{
		Location location = bridge.graphicsRenderer().locationOfClickedImage(pt);
		for (final WorldLocation w : validLocations)
			if (w.location().equals(location))
				return location;

		location = new FullLocation(Constants.UNDEFINED);
		final ContainerStyle containerStyle = bridge.getContainerStyle(container.index());
		final double furthestPossibleDistance = calculateFurthestDistance(context);
		
		// if image not selected, then determine the closest site
		double minDist = 1000.0;
		for (int i = 0; i < validLocations.size(); i++)
		{
			
			double dist = 99999;
			final int site = validLocations.get(i).location().site();
			
			if (validLocations.get(i).location().siteType() == SiteType.Edge)
			{
				if (validLocations.get(i).location().site() < context.board().topology().edges().size())
				{
					final Vertex va = context.board().topology().edges().get(validLocations.get(i).location().site()).vA();
					final Vertex vb = context.board().topology().edges().get(validLocations.get(i).location().site()).vB();
					final Point vaPoint = containerStyle.screenPosn(containerStyle.drawnVertices().get(va.index()).centroid());
					final Point vbPoint = containerStyle.screenPosn(containerStyle.drawnVertices().get(vb.index()).centroid());
					final Point2D.Double vaPointDouble = new Point2D.Double(vaPoint.getX(),vaPoint.getY());
					final Point2D.Double vbPointDouble = new Point2D.Double(vbPoint.getX(),vbPoint.getY());
					final Point2D.Double clickedPoint = new Point2D.Double(pt.getX(), pt.getY());
					dist = MathRoutines.distanceToLineSegment(clickedPoint, vaPointDouble, vbPointDouble);
					dist += bridge.getContainerStyle(container.index()).cellRadiusPixels() / 4;
				}
			}
			else
			{		
				final Point sitePosn = containerStyle.screenPosn(validLocations.get(i).position());
				final int dx = pt.x - sitePosn.x;
				final int dy = pt.y - sitePosn.y;
				dist = Math.sqrt(dx * dx + dy * dy);

				// Check if any large pieces are selected
//				final ContainerState cs = context.state().containerStates()[container.index()];
//				if (validLocations.get(i).location().siteType().equals(SiteType.Cell) && validLocations.get(i).location().level() == 0)
//				{
//					final int localState = cs.state(site, 0, SiteType.Cell);
//					for (final Component component : context.equipment().components())
//					{
//						for (final Integer cellIndex : ContainerUtil.cellsCoveredByPiece(context, container, component, site, localState))
//						{
//							sitePosn = containerStyle.screenPosn(context.board().topology().cells().get(cellIndex).centroid());
//							dx = pt.x - sitePosn.x;
//							dy = pt.y - sitePosn.y;
//							dist = Math.min(dist, Math.sqrt(dx * dx + dy * dy));
//						}
//					}
//				}
			}

			if (dist < minDist && dist < furthestPossibleDistance)
			{
				location = new FullLocation(site, validLocations.get(i).location().level(), validLocations.get(i).location().siteType());
				minDist = dist;
			}
		}

		return location;	
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @param m
	 * @return
	 * Returns whether the move involves a single edge
	 */
	public static boolean isEdgeMove(final Move m)
	{
		return (m.fromType() == SiteType.Edge && m.toType() == SiteType.Edge && m.from() == m.to());
	}
	
	//-------------------------------------------------------------------------
	
}
