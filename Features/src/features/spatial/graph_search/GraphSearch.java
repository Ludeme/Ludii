package features.spatial.graph_search;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import features.spatial.Walk;
import game.Game;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import other.topology.TopologyElement;

/**
 * Some useful graph search algorithms. These are intended to be used specifically
 * with features (e.g. for finding / constructing features), so they follow similar
 * rules as the Walks in features do (e.g. no diagonal connections, allow connections
 * to off-board "locations", ...)
 * 
 * @author Dennis Soemers
 */
public class GraphSearch 
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	private GraphSearch() 
	{
		// no need to instantiate
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Implemented using uniform-cost search, we don't really have meaningful
	 * costs or heuristics for Dijkstra's or A*
	 * 
	 * @param game
	 * @param startSite
	 * @param destination
	 * @return Shortest path from startSite to destination
	 */
	public static Path shortestPathTo(final Game game, final TopologyElement startSite, final TopologyElement destination)
	{
		final TIntSet alreadyVisited = new TIntHashSet();

		final Queue<Path> fringe = new ArrayDeque<Path>();
		List<TopologyElement> pathSites = new ArrayList<TopologyElement>();
		pathSites.add(startSite);
		fringe.add(new Path(pathSites, new Walk()));
		alreadyVisited.add(startSite.index());
		final List<? extends TopologyElement> sites = game.graphPlayElements();

		while (!fringe.isEmpty())
		{
			final Path path = fringe.remove();
			final TopologyElement pathEnd = path.destination();
			final int numOrthos = pathEnd.sortedOrthos().length;
			final TFloatArrayList rotations = Walk.rotationsForNumOrthos(numOrthos);
			
			for (int i = 0; i < rotations.size(); ++i)
			{
				final float nextStep = rotations.getQuick(i);
				
				// create a new walk with this new step
				final Walk newWalk = new Walk(path.walk());
				newWalk.steps().add(nextStep);
				
				// see where we would end up if we were to follow this walk
				final TIntArrayList destinations = newWalk.resolveWalk(game, startSite, 0.f, 1);
				
				if (destinations.size() != 1)
				{
					System.err.println("WARNING: GraphSearch.shortestPathTo() resolved "
							+ "a walk with " + destinations.size() + " destinations!");
				}
				
				final int endWalkIdx = destinations.getQuick(0);
				
				if (destination.index() == endWalkIdx)
				{
					// we're done
					pathSites = new ArrayList<TopologyElement>(path.sites);
					pathSites.add(destination);
					return new Path(pathSites, newWalk);
				}
				else if (endWalkIdx >= 0 && !alreadyVisited.contains(endWalkIdx))
				{
					// new path for fringe
					alreadyVisited.add(endWalkIdx);
					pathSites = new ArrayList<TopologyElement>(path.sites);
					pathSites.add(sites.get(endWalkIdx));
					fringe.add(new Path(pathSites, newWalk));
				}
			}
		}
		
		//System.out.println("startVertex = " + startVertex);
		//System.out.println("destination = " + destination);
		
		return null;
	}
	
	//-------------------------------------------------------------------------

}