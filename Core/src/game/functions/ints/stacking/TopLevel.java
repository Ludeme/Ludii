package game.functions.ints.stacking;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.types.board.SiteType;
import game.types.state.GameType;
import main.Constants;
import other.context.Context;
import other.state.container.ContainerState;

/**
 * Returns the top level of a stack.
 * 
 * @author Eric.Piette
 * 
 * @remarks If the game is not a stacking game, then level 0 is returned.
 */
public final class TopLevel extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The location of the stack. */
	private final IntFunction locn;

	/** The graph element type. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param type The graph element type [default SiteType of the board].
	 * @param at   The site of the stack.
	 * 
	 * @example (topLevel at:(last To))
	 */
	public TopLevel
	(
			  @Opt final SiteType    type,
		@Name      final IntFunction at 
	)
	{
		locn = at;
		this.type = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		if (!context.game().isStacking())
			return 0;
		
		final int loc = locn.eval(context);

		if (loc == Constants.UNDEFINED)
			return 0;

		final int cid = loc >= context.containerId().length ? 0 : context.containerId()[loc];
		
		SiteType realType = type;
		if (cid > 0)
		{
			realType = SiteType.Cell;
			if ((loc - context.sitesFrom()[cid]) >= context.containers()[cid].topology().getGraphElements(realType).size())
				return Constants.OFF;
		}
		else
		{
			if (loc >= context.containers()[cid].topology().getGraphElements(realType).size())
				return Constants.OFF;
			if (realType == null)
				realType = context.board().defaultSite();
		}
		
		final ContainerState cs = context.state().containerStates()[cid];
			
		final int sizeStack = cs.sizeStack(loc, realType);
		if (sizeStack != 0)
			return sizeStack - 1;
				
		return 0;
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
		long gameFlags = locn.gameFlags(game) | GameType.Stacking;

		gameFlags |= SiteType.gameFlags(type);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(locn.concepts(game));
		concepts.or(SiteType.concepts(type));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(locn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(locn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		locn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= locn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= locn.willCrash(game);
		return willCrash;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "Top(" + locn + ")";
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "the top level of the stack on " + type.name().toLowerCase() + " " + locn.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------
}
