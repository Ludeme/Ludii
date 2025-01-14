package game.functions.ints.count.sizeBiggestLine;

import java.util.BitSet;
import java.util.List;
import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import other.context.EvalContextData;
import game.functions.booleans.BooleanFunction;
import game.functions.booleans.is.site.IsOccupied;
import game.functions.directions.Directions;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.iterator.To;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.graph.Radial;
import gnu.trove.list.array.TIntArrayList;
import other.concept.Concept;
import other.context.Context;
import other.topology.TopologyElement;


/**
 * Returns the size of the biggest Line
 * 
 * @author Eric.Piette & Cedric.Antoine
 */
@Hide
public final class CountSizeBiggestLine extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The graph element type. */
	private SiteType type;

	/** The condition */
	private final BooleanFunction condition;

	/** Direction chosen. */
	private final Directions dirnChoice;

	//-------------------------------------------------------------------------

	/**
	 * @param type       The graph element type [default SiteType of the board].
	 * @param directions The directions of the connection between elements in the
	 *                   group [Adjacent].
	 * @param If         The condition on the pieces to include in the line.
	 */
	public CountSizeBiggestLine
	(
		@Opt 	        final SiteType        	type,
		@Opt            final AbsoluteDirection directions,
		@Opt @Or @Name  final BooleanFunction	If
	)
	{
		this.type = type;
		dirnChoice = (directions == null) ? new Directions(AbsoluteDirection.Adjacent, null) : new Directions(directions, null);
		condition = (If != null) ? If : new IsOccupied(type, To.construct());
	}

	//-------------------------------------------------------------------------
	@Override
	public int eval(final Context context)
	{

//		final Topology topology = context.topology();
		final List<? extends TopologyElement> sites = context.topology().getGraphElements(type);
//		final ContainerState cs = context.containerState(0);
		
		final TIntArrayList pivotsFn = new TIntArrayList();
		
		int biggest = 0;
		
		for(final TopologyElement element: sites)
		{
			context.setTo(element.index());
			if(condition.eval(context))
				pivotsFn.add(element.index());
		}
	
		final other.topology.Topology graph = context.topology();
		final boolean playOnCell = (type != null && type.equals(SiteType.Cell)
				|| (type == null && (context.game().board().defaultSite() != SiteType.Vertex)));
		
		
		final int[] pivots = pivotsFn.toArray();
//		System.out.println("Pivots: " + Arrays.toString(pivots));
		
		for (int p = 0; p < pivots.length; p++)
		{
			
			final int locn = pivots[p];
			if (locn < 0) {
				return 0;
			}
			
			final int origTo = context.to(); 
			context.setTo(locn);

			if (playOnCell && locn >= graph.cells().size()) {
				return -1;
			}

			if (!playOnCell && locn >= graph.vertices().size()) {
				return -1;
			}
	
			final TIntArrayList whats = pivotsFn;

			final List<Radial> radials = graph.trajectories().radials(type, locn)
					.distinctInDirection(dirnChoice.absoluteDirection());  // 
			
			for (final Radial radial : radials)
			{
//				System.out.println("Radial: " + radial);
				int count = 1;
				for (int indexPath = 1; indexPath < radial.steps().length; indexPath++)
				{
					final int index = radial.steps()[indexPath].id();
					context.setTo(index);
//					System.out.println("index neigh: " + index);
					
					if (whats.contains(index))
					{	
						count++;
					}
					else
					{
						break;
					}
				}
				
				final List<Radial> oppositeRadials = radial.opposites();
				int oppositeCount = count;
				
				if (oppositeRadials != null)
				{
					for (final Radial oppositeRadial : oppositeRadials)
					{
						for (int indexPath = 1; indexPath < oppositeRadial.steps().length; indexPath++)
						{
							final int index = oppositeRadial.steps()[indexPath].id();
							context.setTo(index);
//							System.out.println("index neigh: " + index);
							
							if (whats.contains(index))
							{	
								oppositeCount++;
							}
							else
							{
								break;
							}
						}
					}
	
				}
				if (oppositeCount > biggest)
				{
					biggest = oppositeCount;
				}
				
			}
			context.setTo(origTo);
//			System.out.println("--------------");
		}
		if (pivots.length == 0) {
			return 0;
		}
//		System.out.println(biggest);
		return biggest;
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
		if (dirnChoice != null)
			gameFlags |= dirnChoice.gameFlags(game);
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
		if (dirnChoice != null)
			dirnChoice.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= condition.missingRequirement(game);
		if (dirnChoice != null)
			missingRequirement |= dirnChoice.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (condition != null)
			willCrash |= condition.willCrash(game);
		if (dirnChoice != null)
			willCrash |= dirnChoice.willCrash(game);
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
