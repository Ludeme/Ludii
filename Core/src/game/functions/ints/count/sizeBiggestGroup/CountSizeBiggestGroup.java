package game.functions.ints.count.sizeBiggestGroup;

import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.booleans.is.site.IsOccupied;
import game.functions.directions.Directions;
import game.functions.directions.DirectionsFunction;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.iterator.To;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.Direction;
import game.util.graph.Step;
import gnu.trove.list.array.TIntArrayList;
import other.context.Context;
import other.state.container.ContainerState;
import other.topology.Topology;
import other.topology.TopologyElement;
import game.functions.region.RegionFunction;
import java.util.ArrayList;


/**
 * Returns the size of the biggest Group on the board
 * 
 * @author Eric.Piette & Cedric.Antoine
 */
public final class CountSizeBiggestGroup extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The graph element type. */
	private SiteType type;
	
	/** The condition */
	private final BooleanFunction condition;
	
	/** Direction chosen. */
	private final DirectionsFunction dirnChoice;
	
	/** The visibility condition */
	private final BooleanFunction isVisibleFn;
	
	/** The locations to look for the groups. */
	private final RegionFunction throughAny;

	//-------------------------------------------------------------------------

	/**
	 * @param type       The graph element type [default SiteType of the board].
	 * @param directions The directions of the connection between elements in the
	 *                   group [Adjacent].
	 * @param If         The condition on the pieces to include in the group.
	 * @param isVisible  Visibility condition for group pieces
	 * @param throughAny The locations to look for the groups.
	 */
	public CountSizeBiggestGroup
	(
		@Opt 	        final SiteType        type,
		@Opt            final Direction       directions,
		@Opt			final RegionFunction  throughAny,
		@Opt @Or @Name  final BooleanFunction If,
		@Opt @Or @Name  final BooleanFunction isVisible
	)
	{
		this.type = type;
		dirnChoice = (directions != null) ? directions.directionsFunctions()
				: new Directions(AbsoluteDirection.Adjacent, null);
		condition = (If != null) ? If : new IsOccupied(type, To.construct());
		isVisibleFn = (isVisible == null) ? new BooleanConstant(false) : isVisible;
		this.throughAny = throughAny;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final Topology topology = context.topology();
		List<? extends TopologyElement> sites; 
		if(throughAny == null) {
			sites = context.topology().getGraphElements(type);
		}
		else {
			final TIntArrayList listPivots = new TIntArrayList(throughAny.eval(context).sites());
			List<TopologyElement> tempSites = new ArrayList<>();
			for(int i : listPivots.toArray()) {		
				tempSites.add(context.topology().getGraphElement(type, i));
			}
			sites = tempSites;
			
		}
		
		final ContainerState cs = context.containerState(0);
		final int origTo = context.to();

		int count = 0;

		final BitSet sitesChecked = new BitSet(sites.size());
		final TIntArrayList sitesToCheck = new TIntArrayList();

		if (context.game().isDeductionPuzzle())
		{
			for (int site = 0; site < sites.size(); site++)
				if (cs.what(site, type) != 0)
					sitesToCheck.add(site);
		}
		else
		{
			for(final TopologyElement element: sites)
			{
				context.setTo(element.index());
				if(condition.eval(context))
					sitesToCheck.add(element.index());
			}
		}

		for (int k = 0; k < sitesToCheck.size(); k++)
		{
			final int from = sitesToCheck.getQuick(k);
			
			if (sitesChecked.get(from))
				continue;
			
			// Good to use both list and BitSet here at the same time for different advantages
			final TIntArrayList groupSites = new TIntArrayList();
			final BitSet groupSitesBS = new BitSet(sites.size());
			
			context.setTo(from);
			
			if (isVisibleFn != null && isVisibleFn.eval(context) == true && !context.equipment().containers()[context.containerId()[from]].isHand()) {
				boolean covered = false;
				for(final TopologyElement temp_elem : sites) {
					TopologyElement from_elem = sites.get(from);
					if (temp_elem.centroid3D().x() == from_elem.centroid3D().x() && temp_elem.centroid3D().y() == from_elem.centroid3D().y() && temp_elem.index() > from_elem.index()) {
						if(cs.what(temp_elem.index(), type) != 0)
							covered = true;
					}
				}
				if (covered)
					continue;
			}
			
			if (condition.eval(context))
			{
				groupSites.add(from);
				groupSitesBS.set(from);
			}
			
			if (groupSites.size() > 0)
			{
				int i = 0;
				while (i != groupSites.size())
				{
					final int site = groupSites.getQuick(i);
					
					final TopologyElement siteElement = topology.getGraphElements(type).get(site);
					final List<AbsoluteDirection> directions = dirnChoice.convertToAbsolute(type, siteElement, null,
							null, null, context);
					
					
					TIntArrayList locnUpwards = new TIntArrayList(); 
					TIntArrayList indexUpwards = new TIntArrayList(); 
					
					if (isVisibleFn != null && isVisibleFn.eval(context) == true) { 
						final List<game.util.graph.Step> steps = context.game().board().topology().trajectories() 
								.steps(SiteType.Vertex, site, SiteType.Vertex, AbsoluteDirection.Upward); 

						for (final Step step : steps) 
						{
							final int toSite = step.to().id(); 
							if (cs.what(toSite, SiteType.Vertex) != 0) { 
								locnUpwards.add(toSite); 
							}
						}
					}

					for (final AbsoluteDirection direction : directions)
					{
						
						final List<game.util.graph.Step> steps = topology.trajectories().steps(type,
								siteElement.index(), type, direction);

						for (final game.util.graph.Step step : steps)
						{
							indexUpwards = new TIntArrayList();
							final int to = step.to().id();
							
							int[] locnUp = new int[0];
							int[] indexUp = new int[0];
							
							if (isVisibleFn != null && isVisibleFn.eval(context) == true) { 
								final List<game.util.graph.Step> stepsbis = context.game().board().topology().trajectories() 
										.steps(type, to, type, AbsoluteDirection.Upward); 
								
								for (final Step stepbis : stepsbis) 
								{
									final int toSite = stepbis.to().id(); 
									if (cs.what(toSite, type) != 0) 
										indexUpwards.add(toSite); 
								}
								
								locnUp = locnUpwards.toArray();	
								indexUp = indexUpwards.toArray();
								
							}

							// If we already have it we continue to look the others.
							if (groupSitesBS.get(to))
								continue;

							context.setTo(to);
							
							boolean covered = false;
							if (isVisibleFn != null && isVisibleFn.eval(context) == true && !context.equipment().containers()[context.containerId()[to]].isHand()) {
								
								for(final TopologyElement temp_elem : sites) {
									TopologyElement to_elem = sites.get(to);
									if (temp_elem.centroid3D().x() == to_elem.centroid3D().x() && temp_elem.centroid3D().y() == to_elem.centroid3D().y() && temp_elem.index() > to_elem.index()) {
										if(cs.what(temp_elem.index(), type) != 0)
											covered = true;
									}
								}
							}
							
							if (condition.eval(context) && !(getIntersectionLength(locnUp, indexUp) >= 2) && !covered)
							{
								groupSites.add(to);
								groupSitesBS.set(to);
							}
						}
					}

					++i;
				}

				if (groupSites.size() > count)
					count = groupSites.size();

				sitesChecked.or(groupSitesBS);
			}
		}

		context.setTo(origTo);

		return count;
	}
	
	//-------------------------------------------------------------------------
	
		/**
	     * function to compute length of intersection of two int arrays
	     */
		private static int getIntersectionLength(int[] array1, int[] array2) {
	        Set<Integer> set1 = new HashSet<>();
	        Set<Integer> intersection = new HashSet<>();
	        
	        // Add elements of the first array to the set
	        for (int num : array1) {
	            set1.add(num);
	        }
	        
	        // Check elements of the second array against the set
	        for (int num : array2) {
	            if (set1.contains(num)) {
	                intersection.add(num);
	            }
	        }
	        
	        return intersection.size();
	    }
		
	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public String toString()
	{
		return "SizeBiggestGroup()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = condition.gameFlags(game);
		gameFlags |= SiteType.gameFlags(type);
		if (isVisibleFn != null)
			gameFlags |= isVisibleFn.gameFlags(game);
		if (dirnChoice != null)
			gameFlags |= dirnChoice.gameFlags(game);
		if (throughAny != null)
			gameFlags |= throughAny.gameFlags(game);
		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.or(condition.concepts(game));
		if (dirnChoice != null)
			concepts.or(dirnChoice.concepts(game));
		if (isVisibleFn != null)
			concepts.or(isVisibleFn.concepts(game));
		if (throughAny != null)
			concepts.or(throughAny.concepts(game));
		
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(condition.writesEvalContextRecursive());
		if (dirnChoice != null)
			writeEvalContext.or(dirnChoice.writesEvalContextRecursive());
		if (isVisibleFn != null)
			writeEvalContext.or(isVisibleFn.writesEvalContextRecursive());
		if (throughAny != null)
			writeEvalContext.or(throughAny.writesEvalContextRecursive());
		
		return writeEvalContext;
	}
	
	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(condition.readsEvalContextRecursive());
		if (dirnChoice != null)
			readEvalContext.or(dirnChoice.readsEvalContextRecursive());
		if (isVisibleFn != null)
			readEvalContext.or(isVisibleFn.readsEvalContextRecursive());
		if (throughAny != null)
			readEvalContext.or(throughAny.readsEvalContextRecursive());
		
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		condition.preprocess(game);
		if (dirnChoice != null)
			dirnChoice.preprocess(game);
		if (isVisibleFn != null)
			isVisibleFn.preprocess(game);
		if (throughAny != null)
			throughAny.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= condition.missingRequirement(game);
		if (dirnChoice != null)
			missingRequirement |= dirnChoice.missingRequirement(game);
		if (isVisibleFn != null)
			missingRequirement |= isVisibleFn.missingRequirement(game);
		if (throughAny != null)
			missingRequirement |= throughAny.missingRequirement(game);
		
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (condition != null)
			willCrash |= condition.willCrash(game);
		if (isVisibleFn != null)
			willCrash |= isVisibleFn.willCrash(game);
		if (throughAny != null)
			willCrash |= throughAny.willCrash(game);
		
		return willCrash;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toEnglish(final Game game) 
	{
		String conditionString = "";
		if (condition != null)
			conditionString = " where " + condition.toEnglish(game);
		
		return "the size of " + type.name() + " biggest group" + conditionString + "and is visible " + isVisibleFn.toString();
	}
	
	//-------------------------------------------------------------------------
		
}
