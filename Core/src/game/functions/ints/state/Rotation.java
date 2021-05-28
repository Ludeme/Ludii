package game.functions.ints.state;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.types.board.SiteType;
import game.types.state.GameType;
import main.Constants;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;
import other.state.stacking.BaseContainerStateStacking;

/**
 * Returns the rotation value of a specified site.
 * 
 * @author Eric Piette
 */
public final class Rotation extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	/** Which location. */
	private final IntFunction locn;

	/** Which level (for a stacking game). */
	private final IntFunction level;

	/** Cell/Edge/Vertex. */
	private SiteType type;

	// -------------------------------------------------------------------------

	/**
	 * @param type  The graph element type [default SiteType of the board].
	 * @param at    The location to check.
	 * @param level The level to check [0].
	 * 
	 * @example (rotation at:(last To))
	 */
	public Rotation
	(
		@Opt 	   final SiteType    type,
			 @Name final IntFunction at,
		@Opt @Name final IntFunction level
	)
	{
		this.locn  = at;
		this.level = level;
		this.type  = type;
	}

	// -------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final int loc = locn.eval(context);
		if (loc == Constants.OFF)
			return 0;

		final int containerId = context.containerId()[loc];

		if (context.game().isStacking() && containerId == 0)
		{
			// Is stacking game
			final BaseContainerStateStacking state = (BaseContainerStateStacking) context.state()
					.containerStates()[containerId];

			if (level == null)
				return state.rotation(loc, type);

			return state.rotation(loc, level.eval(context), type);
		}
		final ContainerState cs = context.state().containerStates()[containerId];

		return cs.rotation(loc, type);
	}

	// -------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		// we're looking at state in a specific context, so not static
		return false;

		// return site.isStatic() && level.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		long stateFlag = locn.gameFlags(game) | GameType.Rotation;

		stateFlag |= SiteType.gameFlags(type);

		if (level != null)
			stateFlag |= level.gameFlags(game);

		return stateFlag;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.or(locn.concepts(game));
		concepts.set(Concept.Rotation.id(), true);

		if (level != null)
			concepts.or(level.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(locn.writesEvalContextRecursive());

		if (level != null)
			writeEvalContext.or(level.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(locn.readsEvalContextRecursive());

		if (level != null)
			readEvalContext.or(level.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		locn.preprocess(game);
		if (level != null)
			level.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		missingRequirement |= locn.missingRequirement(game);

		if (level != null)
			missingRequirement |= level.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;

		willCrash |= locn.willCrash(game);

		if (level != null)
			willCrash |= level.willCrash(game);
		return willCrash;
	}
}
