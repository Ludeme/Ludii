package game.functions.ints.board;

import java.util.BitSet;
import java.util.List;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.directions.Directions;
import game.functions.directions.DirectionsFunction;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.Direction;
import game.util.directions.DirectionFacing;
import game.util.directions.RelativeDirection;
import game.util.graph.Radial;
import main.Constants;
import other.context.Context;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Returns the site in a given direction from a specified site.
 * 
 * @author Eric.Piette
 *
 * @remarks If there is no site in the specified direction, then the index of
 *          the source site is returned.
 */
public final class Ahead extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The site. */
	private final IntFunction siteFn;
	
	/** The number of steps in this direction. */
	private final IntFunction stepsFn;

	/** Direction chosen. */
	private final DirectionsFunction dirnChoice;
	
	/** Add on Cell/Edge/Vertex. */
	private SiteType type;
	
	//-------------------------------------------------------------------------

	/**
	 * @param type       The graph element type [default SiteType of the board].
	 * @param site       Source site.
	 * @param steps      Distance to travel [1].
	 * @param directions The direction.
	 *
	 * @example (ahead (centrePoint) E)
	 *
	 * @example (ahead (last To) steps:3 N)
	 */
	public Ahead
	(
		@Opt       final SiteType    type, 
			       final IntFunction site, 
		@Opt @Name final IntFunction steps,
		@Opt       final Direction   directions
	) 
	{
		siteFn = site;
		dirnChoice = (directions != null) ? directions.directionsFunctions()
				: new Directions(RelativeDirection.Forward, null, null, null);
		stepsFn = (steps == null) ? new IntConstant(1) : steps;
		this.type = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final int site = siteFn.eval(context);
		final int distance = stepsFn.eval(context);

		if (site < 0)
			return Constants.UNDEFINED;

		final Topology topology = context.topology();

		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();
		final TopologyElement fromV = topology.getGraphElements(realType).get(site);

		AbsoluteDirection direction = null;

		if (dirnChoice.getRelativeDirections() != null
				&& (dirnChoice.getRelativeDirections()[0].equals(RelativeDirection.SameDirection)
						|| dirnChoice.getRelativeDirections()[0].equals(RelativeDirection.OppositeDirection)))
		{
			final RelativeDirection relativeDirection = dirnChoice.getRelativeDirections()[0];

			// Special case for Opposite Direction
			if (relativeDirection.equals(RelativeDirection.OppositeDirection))
			{
				final int from = (context.from() == Constants.UNDEFINED ? context.trial().lastMove().fromNonDecision() : context.from());
				final int to = (context.to() == Constants.UNDEFINED ? context.trial().lastMove().toNonDecision() : context.to());

				final List<DirectionFacing> directionsSupported = topology.supportedDirections(realType);

				for (final DirectionFacing facingDirection : directionsSupported)
				{
					final AbsoluteDirection absDirection = facingDirection.toAbsolute();
					final List<Radial> radials = topology.trajectories().radials(realType, to, absDirection);
					boolean found = false;

					for (final Radial radial : radials)
					{
						for (int toIdx = 1; toIdx < radial.steps().length; toIdx++)
						{
							final int toRadial = radial.steps()[toIdx].id();
							if (toRadial == from)
							{
								direction = absDirection;
								found = true;
								break;
							}
						}

						if (found)
							break;
					}
				}
			}
			else // Special case for Same Direction
			{
				final int from = (context.from() == Constants.UNDEFINED ? context.trial().lastMove().fromNonDecision() : context.from());
				final int to = (context.to() == Constants.UNDEFINED ? context.trial().lastMove().toNonDecision() : context.to());

				final List<DirectionFacing> directionsSupported = topology.supportedDirections(realType);

				for (final DirectionFacing facingDirection : directionsSupported)
				{
					final AbsoluteDirection absDirection = facingDirection.toAbsolute();
					final List<Radial> radials = topology.trajectories().radials(realType, from, absDirection);
					boolean found = false;

					for (final Radial radial : radials)
					{
						for (int toIdx = 1; toIdx < radial.steps().length; toIdx++)
						{
							final int toRadial = radial.steps()[toIdx].id();
							if (toRadial == to)
							{
								direction = absDirection;
								found = true;
								break;
							}
						}

						if (found)
							break;
					}
				}
			}
		}
		else
		{
			final List<AbsoluteDirection> directions = dirnChoice.convertToAbsolute(realType, fromV, null, null, null,
					context);

			if (directions.isEmpty())
				return site;

			direction = directions.get(0);
		}

		final List<Radial> radialList = topology.trajectories().radials(realType, fromV.index(), direction);

		for (final Radial radial : radialList)
		{
			for (int toIdx = 1; toIdx < radial.steps().length && toIdx <= distance; toIdx++)
			{
				final int to = radial.steps()[toIdx].id();
				if (toIdx == distance)
					return to;
			}
		}
		
		return site;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return siteFn.isStatic() && stepsFn.isStatic() && dirnChoice.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = siteFn.gameFlags(game) | stepsFn.gameFlags(game);
		gameFlags |= SiteType.gameFlags(type) | dirnChoice.gameFlags(game);
		return gameFlags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(siteFn.concepts(game));
		concepts.or(SiteType.concepts(type));
		concepts.or(stepsFn.concepts(game));
		concepts.or(dirnChoice.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(siteFn.writesEvalContextRecursive());
		writeEvalContext.or(stepsFn.writesEvalContextRecursive());
		writeEvalContext.or(dirnChoice.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(siteFn.readsEvalContextRecursive());
		readEvalContext.or(stepsFn.readsEvalContextRecursive());
		readEvalContext.or(dirnChoice.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		siteFn.preprocess(game);
		stepsFn.preprocess(game);
		dirnChoice.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= siteFn.missingRequirement(game);
		missingRequirement |= stepsFn.missingRequirement(game);
		missingRequirement |= dirnChoice.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= siteFn.willCrash(game);
		willCrash |= stepsFn.willCrash(game);
		willCrash |= dirnChoice.willCrash(game);
		return willCrash;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "ForwardSite(" + siteFn + ")";
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		final SiteType realType = (type != null) ? type : game.board().defaultSite();
		return " the " + realType.name() + " " + stepsFn.toEnglish(game) + " steps ahead of " + siteFn.toEnglish(game) + " in the direction " + dirnChoice.toEnglish(game);
	}
}
