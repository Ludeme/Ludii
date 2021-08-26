package game.functions.region.math;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import annotations.Or2;
import game.Game;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.region.BaseRegionFunction;
import game.functions.region.RegionFunction;
import game.types.board.SiteType;
import game.types.state.GameType;
import game.util.directions.AbsoluteDirection;
import game.util.equipment.Region;
import other.ContainerId;
import other.IntArrayFromRegion;
import other.context.Context;

/**
 * Expands a given region/site in all directions the specified number of steps.
 * 
 * @author cambolbro and Eric.Piette
 */
public final class Expand extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** Which container. */
	private final ContainerId containerId;

	/** Which container. */
	private final IntArrayFromRegion baseRegion;
	
	/** How many steps to expand. */
	private final IntFunction numSteps;
	
	/** How many steps to expand. */
	private final AbsoluteDirection direction;

	/** If we can, we'll precompute once and cache. */
	private Region precomputedRegion = null;

	//-------------------------------------------------------------------------

	/**
	 * @param containerIdFn The index of the container.
	 * @param containerName The name of the container.
	 * @param region        The region.
	 * @param origin        The site.
	 * @param steps         The distance to expand [steps:1].
	 * @param dirn          The absolute direction to expand.
	 * @param type          The graph element type [default SiteType of the board].
	 * 
	 * @example (expand (sites Bottom) steps:2)
	 */
	public Expand
	(
		@Opt @Or  		final IntFunction 	 	containerIdFn, 
		@Opt @Or  		final String 		 	containerName,
			 @Or2 		final RegionFunction 	region,
	         @Or2 @Name final IntFunction 	 	origin,
	   @Opt       @Name final IntFunction 	 	steps,
	   @Opt 			final AbsoluteDirection dirn, 
	   @Opt 			final SiteType 			type
	) 
	{
		int numNonNull = 0;
		if (containerIdFn != null)
			numNonNull++;
		if (containerName != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException("Zero or one Or parameter must be non-null.");
	
		numNonNull = 0;
		if (region != null)
			numNonNull++;
		if (origin != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Exactly one Or2 parameter must be non-null.");

		containerId = new ContainerId(containerIdFn, containerName, null, null, null);
		baseRegion = new IntArrayFromRegion(origin, region);
		numSteps = (steps == null) ? new IntConstant(1) : steps;
		direction = dirn;
		this.type = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		if (precomputedRegion != null)
			return precomputedRegion;
		
		final int cid = containerId.eval(context);

		final Region region = new Region(baseRegion.eval(context));
			
		final int num = numSteps.eval(context);
		if (num > 0)
		{
			final other.topology.Topology graph = context.containers()[cid].topology();
			if (direction == null)
				Region.expand(region, graph, num, type);
			else
				Region.expand(region, graph, num, direction, type);
		}
		
		return region;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic() 
	{
		return baseRegion.isStatic() && numSteps.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flags = baseRegion.gameFlags(game) | numSteps.gameFlags(game);

		if (type != null)
		{
			if (type.equals(SiteType.Edge) || type.equals(SiteType.Vertex))
				flags |= GameType.Graph;
		}

		return flags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(baseRegion.concepts(game));
		concepts.or(numSteps.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(baseRegion.writesEvalContextRecursive());
		writeEvalContext.or(numSteps.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(baseRegion.readsEvalContextRecursive());
		readEvalContext.or(numSteps.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= baseRegion.missingRequirement(game);
		missingRequirement |= numSteps.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= baseRegion.willCrash(game);
		willCrash |= numSteps.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);

		baseRegion.preprocess(game);
		numSteps.preprocess(game);

		if (isStatic())
			precomputedRegion = eval(new Context(game, null));
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return baseRegion.toEnglish(game);
	}
}
