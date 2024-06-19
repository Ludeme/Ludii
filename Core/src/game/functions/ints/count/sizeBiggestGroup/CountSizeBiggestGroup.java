package game.functions.ints.count.sizeBiggestGroup;

import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
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
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.state.container.ContainerState;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Returns the number of groups.
 * 
 * @author Eric.Piette & Cedric.Antoine
 */
@Hide
public final class CountSizeBiggestGroup extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The graph element type. */
	private SiteType type;

	/** The condition */
	private final BooleanFunction condition;
	
	/** The visibility condition */
	private final BooleanFunction isVisibleFn;

	/** Direction chosen. */
	private final DirectionsFunction dirnChoice;

	//-------------------------------------------------------------------------

	/**
	 * @param type       The graph element type [default SiteType of the board].
	 * @param directions The directions of the connection between elements in the
	 *                   group [Adjacent].
	 * @param If         The condition on the pieces to include in the group.
	 * @param isVisible  Visibility condition for group pieces
	 */
	public CountSizeBiggestGroup
	(
		@Opt 	        final SiteType        type,
		@Opt            final Direction       directions,
		@Opt @Or @Name  final BooleanFunction If,
		@Opt @Or @Name  final BooleanFunction isVisible
	)
	{
		this.type = type;
		dirnChoice = (directions != null) ? directions.directionsFunctions()
				: new Directions(AbsoluteDirection.Adjacent, null);
		condition = (If != null) ? If : new IsOccupied(type, To.construct());
		isVisibleFn = isVisible;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final Topology topology = context.topology();
		final List<? extends TopologyElement> sites = context.topology().getGraphElements(type);
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
			
			if (isVisibleFn != null && isVisibleFn.eval(context) == true && ((from == 8 && cs.what(9, type) != 0) || (from == 10 && cs.what(11, type) != 0) || (from == 14 && cs.what(15, type) != 0) || (from == 18 && cs.what(19, type) != 0) || (from == 20 && cs.what(21, type) != 0)))
				continue;
			else if (condition.eval(context))
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
					
					
					TIntArrayList locnUpwards = new TIntArrayList(); // ced
					TIntArrayList indexUpwards = new TIntArrayList(); // ced
					
					if (isVisibleFn != null && isVisibleFn.eval(context) == true) { // ced
						final List<game.util.graph.Step> steps = context.game().board().topology().trajectories() // ced
								.steps(SiteType.Vertex, site, SiteType.Vertex, AbsoluteDirection.Upward); // ced

						for (final Step step : steps) // ced
						{
							final int toSite = step.to().id(); // ced
							if (cs.what(toSite, SiteType.Vertex) != 0) { // ced
								locnUpwards.add(toSite); // ced
							}
						}
					}

//					System.out.println(directions);
					for (final AbsoluteDirection direction : directions)
					{
						
						final List<game.util.graph.Step> steps = topology.trajectories().steps(type,
								siteElement.index(), type, direction);
//						System.out.println(topology.trajectories().toString());
						for (final game.util.graph.Step step : steps)
						{
							indexUpwards = new TIntArrayList();
							final int to = step.to().id();
							
							int[] locnUp = new int[0];
							int[] indexUp = new int[0];
							
							if (isVisibleFn != null && isVisibleFn.eval(context) == true) { // ced
								final List<game.util.graph.Step> stepsbis = context.game().board().topology().trajectories() // ced
										.steps(SiteType.Vertex, to, SiteType.Vertex, AbsoluteDirection.Upward); // ced
								
								for (final Step stepbis : stepsbis) // ced
								{
									final int toSite = stepbis.to().id(); // ced
									if (cs.what(toSite, type) != 0) // ced
										indexUpwards.add(toSite); // ced
								}
								
								locnUp = locnUpwards.toArray();	// ced
								indexUp = indexUpwards.toArray();	// ced
								
//								System.out.println("locA: " + Arrays.toString(locnUp));
//								System.out.println("indexA: " + Arrays.toString(indexUp));
							}

							// If we already have it we continue to look the others.
							if (groupSitesBS.get(to))
								continue;
							
//							System.out.println("-------------");
//							System.out.println("From: " + site);
//							System.out.println("To: " + to);
//							System.out.println("inter: " + getIntersectionLength(locnUp, indexUp));

							context.setTo(to);
							if (condition.eval(context) && !(getIntersectionLength(locnUp, indexUp) >= 2) && !((to == 8 && cs.what(9, type) != 0) || (to == 10 && cs.what(11, type) != 0) || (to == 14 && cs.what(15, type) != 0) || (to == 18 && cs.what(19, type) != 0) || (to == 20 && cs.what(21, type) != 0)))
							{
//								System.out.println(to);
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
		return "Groups()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = condition.gameFlags(game);
		gameFlags |= SiteType.gameFlags(type);
		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.set(Concept.Group.id(), true);
		concepts.or(condition.concepts(game));
		if (dirnChoice != null)
			concepts.or(dirnChoice.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(condition.writesEvalContextRecursive());
		if (dirnChoice != null)
			writeEvalContext.or(dirnChoice.writesEvalContextRecursive());
		return writeEvalContext;
	}
	
	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.set(EvalContextData.To.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(condition.readsEvalContextRecursive());
		if (dirnChoice != null)
			readEvalContext.or(dirnChoice.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		condition.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= condition.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (condition != null)
			willCrash |= condition.willCrash(game);
		return willCrash;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toEnglish(final Game game) 
	{
		String conditionString = "";
		if (condition != null)
			conditionString = " where " + condition.toEnglish(game);
		
		return "the number of " + type.name() + " groups" + conditionString;
	}
	
	//-------------------------------------------------------------------------
		
}
