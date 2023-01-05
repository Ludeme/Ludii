package game.functions.ints.value.piece;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.types.board.SiteType;
import game.types.state.GameType;
import main.Constants;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;
import other.state.stacking.BaseContainerStateStacking;

/**
 * Returns the value of a component.
 * 
 * @author Eric Piette
 * @remarks For any game with a value associated with a component.
 */
@Hide
public final class ValuePiece extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** Which location. */
	private final IntFunction loc;

	/** Which level (for a stacking game). */
	private final IntFunction level;

	/** Cell/Edge/Vertex. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param type  The graph element type [default SiteType of the board].
	 * @param at    The location to check.
	 * @param level The level to check.
	 */
	public ValuePiece
	(
		@Opt       final SiteType    type,
		     @Name final IntFunction at,
		@Opt @Name final IntFunction level
	)
	{
		loc = at;
		this.level = (level == null) ? new IntConstant(Constants.UNDEFINED) : level;
		this.type = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final int location = loc.eval(context);
		if (location == Constants.OFF)
			return Constants.NOBODY;

		final int containerId = context.containerId()[location];
		
		if ((context.game().gameFlags() & GameType.Stacking) != 0)
		{
			// Is stacking game
			final BaseContainerStateStacking state = (BaseContainerStateStacking) context.state()
					.containerStates()[containerId];
			if (level.eval(context) == -1)
				return state.value(loc.eval(context), type);
			else
				return state.value(loc.eval(context), level.eval(context), type);
		}

		final ContainerState cs = context.state().containerStates()[containerId];
		return cs.value(loc.eval(context), type);
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
		long gameFlags = loc.gameFlags(game) | level.gameFlags(game) | GameType.Value;

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
		concepts.set(Concept.PieceValue.id(), true);
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

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		loc.preprocess(game);
		level.preprocess(game);
	}
	
	//-------------------------------------------------------------------------

	
	@Override
	public String toEnglish(final Game game)
	{		
		String levelString = "";
		if (level != null)
			levelString = " at level " + level.toEnglish(game);
		
		return "the level of the piece on " + ((type == null) ? game.board().defaultSite().name().toLowerCase() : type.name().toLowerCase())+ " " + loc.toEnglish(game) + levelString;
	}
	
	//-------------------------------------------------------------------------
		
}
