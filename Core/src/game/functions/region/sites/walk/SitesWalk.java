package game.functions.region.sites.walk;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.ints.iterator.From;
import game.functions.region.BaseRegionFunction;
import game.types.board.SiteType;
import game.types.board.StepType;
import game.util.directions.DirectionFacing;
import game.util.equipment.Region;
import game.util.graph.Step;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.ContainerId;
import other.context.Context;

/**
 * Returns all the sites of a walk got by a graphic turtle walk.
 * 
 * @author Eric Piette
 */
@Hide
public final class SitesWalk extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The starting location of the walk. */
	private final IntFunction startLocationFn;

	/** Steps for walking. */
	private final StepType[][] possibleSteps;

	/** If the walk includes all the rotations. */
	private final BooleanFunction rotations;

	//-------------------------------------------------------------------------

	/**
	 * @param type            Type of graph element.
	 * @param startLocationFn The starting location of the walk.
	 * @param possibleSteps   The different turtle steps defining a graphic turtle
	 *                        walk.
	 * @param rotations       True if the move includes all the rotations of the
	 *                        walk [True].
	 */
	public SitesWalk
	(
			@Opt       final SiteType        type, 
			@Opt       final IntFunction     startLocationFn, 
			           final StepType[][]    possibleSteps,
			@Opt @Name final BooleanFunction rotations
	)
	{ 
		this.startLocationFn = (startLocationFn == null) ? new From(null) : startLocationFn;
		this.type = type;
		this.possibleSteps = possibleSteps;
		this.rotations = (rotations == null) ? new BooleanConstant(true) : rotations;
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		final int from = startLocationFn.eval(context);

		if (from == Constants.OFF)
			return new Region(new TIntArrayList().toArray());

		final int cid = new ContainerId(null,  null,  null, null, new IntConstant(from)).eval(context);
		final other.topology.Topology graph = context.containers()[cid].topology();
		final boolean allRotations = rotations.eval(context);
		final SiteType realType = (type == null) ? context.board().defaultSite() : type;
		final List<DirectionFacing> orthogonalSupported = graph.supportedOrthogonalDirections(realType);
		List<DirectionFacing> walkDirection;

		if (allRotations)
			walkDirection = graph.supportedOrthogonalDirections(realType);
		else
		{
			walkDirection = new ArrayList<DirectionFacing>();
			walkDirection.add(graph.supportedOrthogonalDirections(realType).get(0));
		}

		final TIntArrayList sitesAfterWalk = new TIntArrayList();

		for (final DirectionFacing startDirection : walkDirection)
		{
			for (final StepType[] steps : possibleSteps)
			{
				int currentLoc = from;
				DirectionFacing currentDirection = startDirection;

				for (final StepType step : steps)
				{
					if (step == StepType.F)
					{
						final List<Step> stepsDirection = graph.trajectories().steps(realType, currentLoc,
								currentDirection.toAbsolute());

						int to = Constants.UNDEFINED;
						for (final Step stepDirection : stepsDirection)
						{
							if (stepDirection.from().siteType() != stepDirection.to().siteType())
								continue;

							to = stepDirection.to().id();
						}

						currentLoc = to;

						// No correct walk with that state.
						if (to == Constants.UNDEFINED)
							break;

					}
					else if (step == StepType.R)
					{
						currentDirection = currentDirection.right();
						while (!orthogonalSupported.contains(currentDirection))
							currentDirection = currentDirection.right();
					}

					else if (step == StepType.L)
					{
						currentDirection = currentDirection.left();
						while (!orthogonalSupported.contains(currentDirection))
							currentDirection = currentDirection.left();
					}
				}

				if (currentLoc != Constants.UNDEFINED)
					sitesAfterWalk.add(currentLoc);
			}
		}

		return new Region(sitesAfterWalk.toArray());
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = 0;

		gameFlags |= SiteType.gameFlags(type);

		gameFlags |= startLocationFn.gameFlags(game);
		gameFlags |= rotations.gameFlags(game);

		return gameFlags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.or(startLocationFn.concepts(game));
		concepts.or(rotations.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(startLocationFn.writesEvalContextRecursive());
		writeEvalContext.or(rotations.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(startLocationFn.readsEvalContextRecursive());
		readEvalContext.or(rotations.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);

		startLocationFn.preprocess(game);
		rotations.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= startLocationFn.missingRequirement(game);
		missingRequirement |= rotations.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= startLocationFn.willCrash(game);
		willCrash |= rotations.willCrash(game);
		return willCrash;
	}
}
