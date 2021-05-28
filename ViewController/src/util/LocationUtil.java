package util;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bridge.Bridge;
import game.equipment.container.Container;
import main.Constants;
import other.context.Context;
import other.location.FullLocation;
import other.location.Location;
import other.move.Move;
import other.state.container.ContainerState;
import other.topology.Cell;
import other.topology.Edge;
import other.topology.Vertex;
import view.container.ContainerStyle;

/**
 * Functions relating to Locations.
 * 
 * @author Matthew.Stephenson
 */
public class LocationUtil 
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Get all locations across all containers.
	 */
	public static List<Location> getAllLocations(final Context context, final Bridge bridge)
	{
		final List<Location> allLocations = new ArrayList<>();
		for (final Container container : context.containers())
		{
			final ContainerState cs = context.state().containerStates()[container.index()];
			final ContainerStyle containerStyle = bridge.getContainerStyle(container.index());
			
			// if is an edge game, then vertices can also be selected
			if (container.index() == 0 && (context.isVertexGame() || context.isEdgeGame()))
				for (final Vertex v : containerStyle.drawnVertices())
					for (int i = 0; i <= cs.sizeStack(v.index(), v.elementType()); i++)
						allLocations.add(new FullLocation(v.index(), i, v.elementType()));
	
			if (container.index() == 0 && context.isEdgeGame())
				for (final Edge e : containerStyle.drawnEdges())
					for (int i = 0; i <= cs.sizeStack(e.index(), e.elementType()); i++)
						allLocations.add(new FullLocation(e.index(), i, e.elementType()));
	
			if (context.isCellGame())
				for (final Cell c : containerStyle.drawnCells())
					for (int i = 0; i <= cs.sizeStack(c.index(), c.elementType()); i++)
						allLocations.add(new FullLocation(c.index(), i, c.elementType()));
		}
		
		return allLocations;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Get all valid From locations in the set of legal moves.
	 */
	public static List<Location> getLegalFromLocations(final Context context)
	{
		final Set<Location> allLocations = new HashSet<>();
		for (final Move m : context.moves(context).moves())
			allLocations.add(m.getFromLocation());
		return new ArrayList<>(allLocations);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Get all valid To locations in the set of legal moves.
	 */
	public static List<Location> getLegalToLocations(final Bridge bridge, final Context context)
	{
		final Set<Location> allLocations = new HashSet<>();
		for (final Move m : context.moves(context).moves())
			if (m.getFromLocation().equals(bridge.settingsVC().selectedFromLocation()))
				allLocations.add(m.getToLocation());
		return new ArrayList<>(allLocations);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Get the nearest location to the released point.
	 */
	public static Location calculateNearestLocation(final Context context, final Bridge bridge, final Point pt, final List<Location> legalLocations)
	{
		Location location = new FullLocation(Constants.UNDEFINED);
		
		for (final Container container : context.equipment().containers())
		{
			location = bridge.getContainerController(container.index()).calculateNearestLocation(context, pt, legalLocations);			
			if (!location.equals(new FullLocation(Constants.UNDEFINED)))
				return location;
		}

		return location;
	}
	
	//-------------------------------------------------------------------------

}
