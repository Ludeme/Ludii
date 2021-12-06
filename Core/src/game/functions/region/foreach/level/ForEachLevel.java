package game.functions.region.foreach.level;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.IntFunction;
import game.functions.region.BaseRegionFunction;
import game.types.board.SiteType;
import game.util.directions.StackDirection;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import other.context.Context;
import other.context.EvalContextData;
import other.state.container.ContainerState;

/**
 * Iterates through the players, generating moves based on the indices of the
 * players.
 * 
 * @author Eric.Piette
 */
@Hide
public final class ForEachLevel extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Region to return for each player. */
	private final IntFunction siteFn;
	
	/** The list of players. */
	private final BooleanFunction cond;

	/** To start from the bottom or the top of the stack. */
	private final StackDirection stackDirection;
	
	//-------------------------------------------------------------------------

	/**
	 * @param type           The type
	 * @param at             The site.
	 * @param stackDirection The direction to count in the stack [FromTop].
	 * @param If             The condition to satisfy.
	 */
	public ForEachLevel
	(
		@Opt       final SiteType         type,
	         @Name final IntFunction      at,
	 	@Opt       final StackDirection   stackDirection,
		@Opt @Name final BooleanFunction  If
	)
	{
		this.type = type;
		this.siteFn = at;
		this.cond   = If;
		this.stackDirection = (stackDirection == null) ? StackDirection.FromTop : stackDirection;
	}

	//-------------------------------------------------------------------------

	@Override
	public final Region eval(final Context context)
	{
		final TIntArrayList returnLevels = new TIntArrayList();
		final int site = siteFn.eval(context);
		
		final int cid = site >= context.containerId().length ? 0 : context.containerId()[site];
		SiteType realType = type;
		if (cid > 0)
			realType = SiteType.Cell;
		else if (realType == null)
			realType = context.board().defaultSite();
		final ContainerState cs = context.containerState(cid);
		final int stackSize = cs.sizeStack(site, type);
		final int originLevel = context.level();
		
		if(stackDirection.equals(StackDirection.FromBottom))
		{
			for(int lvl = 0; lvl < stackSize; lvl++)
			{
				context.setLevel(lvl);
				if(cond == null || cond.eval(context))
					returnLevels.add(lvl);
			}
		}
		else
		{
			for(int lvl = stackSize -1 ; lvl >= 0; lvl--)
			{
				context.setLevel(lvl);
				if(cond == null || cond.eval(context))
					returnLevels.add(lvl);
			}
		}

		context.setLevel(originLevel);
		return new Region(returnLevels.toArray());
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = siteFn.gameFlags(game);
		gameFlags |= SiteType.gameFlags(type);
		if (cond != null)
			gameFlags |= cond.gameFlags(game);
		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.or(SiteType.concepts(type));
		concepts.or(siteFn.concepts(game));
		if (cond != null)
			concepts.or(cond.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(super.writesEvalContextRecursive());

		if (cond != null)
			writeEvalContext.or(cond.writesEvalContextRecursive());

		writeEvalContext.or(siteFn.writesEvalContextRecursive());
		return writeEvalContext;
	}
	
	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.set(EvalContextData.Level.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (cond != null)
			readEvalContext.or(cond.readsEvalContextRecursive());

		readEvalContext.or(siteFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (cond != null)
			missingRequirement |= (cond.missingRequirement(game));
		missingRequirement |= siteFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (cond != null)
			willCrash |= (cond.willCrash(game));
		willCrash |= siteFn.willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (type == null)
			type = game.board().defaultSite();
		siteFn.preprocess(game);
		if (cond != null)
			cond.preprocess(game);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "for each level at " + siteFn.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------
		
}
