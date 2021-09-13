package game.functions.ints.state;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.types.board.SiteType;
import main.Constants;
import other.context.Context;
import other.state.container.ContainerState;
import other.state.stacking.BaseContainerStateStacking;

/**
 * Returns the index of the component at a specific location/level.
 * 
 * @author Eric Piette and cambolbro
 */
public final class What extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which loc. */
	private final IntFunction loc;

	/** Which level (for a stacking game). */
	private final IntFunction level;

	/** Cell/Edge/Vertex. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param type  The graph element type [default SiteType of the board].
	 * @param at    The location to check.
	 * @param level The level to check [0].
	 * 
	 * @example (what at:(last To))
	 */
	public What
	(
		@Opt 	    final SiteType    type,
			 @Name	final IntFunction at,
		@Opt @Name	final IntFunction level
	)
	{
		loc = at;
		this.level = (level == null) ? new IntConstant(0) : level;
		this.type = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final int siteIdx = loc.eval(context);
		
		if (siteIdx == Constants.OFF)
			return Constants.NO_PIECE;
		
		final int containerId = context.containerId()[siteIdx];

		if (context.game().isStacking())
		{
			// Is stacking game
			final BaseContainerStateStacking state = 
					(BaseContainerStateStacking)context.state().containerStates()[containerId];
			
			if (level.eval(context) == -1)
				return state.what(siteIdx, type);
			
			return state.what(siteIdx, level.eval(context), type);
		}

		final ContainerState cs = context.state().containerStates()[containerId];
		return cs.what(siteIdx, type);
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The location to check.
	 */
	public IntFunction loc()
	{
		return loc;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		// we're looking at the "what" in a specific context, so not static
		return false;
		
		//return site.isStatic() && level.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = loc.gameFlags(game) | level.gameFlags(game);
		gameFlags |= SiteType.gameFlags(type);
		return gameFlags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.or(loc.concepts(game));
		concepts.or(level.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(loc.writesEvalContextRecursive());
		writeEvalContext.or(level.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(loc.readsEvalContextRecursive());
		readEvalContext.or(level.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		loc.preprocess(game);
		level.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= loc.missingRequirement(game);
		missingRequirement |= level.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= loc.willCrash(game);
		willCrash |= level.willCrash(game);
		return willCrash;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString() {
		String str="(What ";
		str += loc;
		str += "," + level;
		str += ")";
		return str;
	}
	
	@Override
	public String toEnglish(final Game game)
	{
		return "Piece at " + loc.toEnglish(game);
	}
}
