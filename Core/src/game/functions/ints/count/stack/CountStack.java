package game.functions.ints.count.stack;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.BaseIntFunction;
import game.types.board.SiteType;
import game.types.state.GameType;
import game.util.directions.StackDirection;
import main.Constants;
import other.IntArrayFromRegion;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.state.container.ContainerState;

/**
 * Returns the number of pieces in stack(s) according to conditions.
 * 
 * @author Eric.Piette
 */
@Hide
public final class CountStack extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** If we can, we'll precompute once and cache. */
	private Integer preComputedInteger = null;

	//-------------------------------------------------------------------------

	/** Which region. */
	private final IntArrayFromRegion region;
	
	/** Which condition to count. */
	private final BooleanFunction condition;

	/** Which condition to stop to count. */
	private final BooleanFunction stopCondition;
	
	/** To start from the bottom or the top of the stack. */
	private final StackDirection stackDirection;

	/** Cell/Edge/Vertex. */
	private SiteType type;

	/**
	 * @param stackDirection The direction to count in the stack [FromBottom].
	 * @param type           The graph element type [default SiteType of the board].
	 * @param to             The region where are the stacks.
	 * @param If             The condition to count in the stack [True].
	 * @param stop           The condition to stop to count in the stack [False].
	 */
	public CountStack
	(
		    @Opt       final StackDirection     stackDirection,
		    @Opt       final SiteType           type,
		         @Name final IntArrayFromRegion to,
		    @Opt @Name final BooleanFunction    If,
		    @Opt @Name final BooleanFunction    stop
	)
	{
		region = to;
		this.type   = type;
		condition = (If == null) ?  new BooleanConstant(true) : If;
		stopCondition = (stop == null) ?  new BooleanConstant(false) : stop;
		this.stackDirection = (stackDirection == null) ? StackDirection.FromBottom : stackDirection;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		if (preComputedInteger != null)
			return preComputedInteger.intValue();

		final SiteType realSiteType = (type != null) ? type
				: context.board().defaultSite();
		
		final int[] sites = region.eval(context);

		final int origTo = context.to();
		final int origLevel = context.level();
		
		int count = 0;
		for(final int site : sites)
		{
			if(site > Constants.UNDEFINED)
			{
				final int containerId = context.containerId()[site];
				final ContainerState cs = context.state().containerStates()[containerId];
				if(cs.what(site, realSiteType) != 0)
				{
					final int topLevel = cs.sizeStack(site, realSiteType) - 1;
					final int fromLevel = (stackDirection == StackDirection.FromBottom) ? 0 : topLevel;
					final int toLevel = (stackDirection == StackDirection.FromBottom) ? topLevel : 0;

					if (stackDirection.equals(StackDirection.FromBottom))
					{
						for (int level = fromLevel; level <= toLevel; level++)
						{
							context.setTo(site);
							context.setLevel(level);
							if (!stopCondition.eval(context))
							{
								if (condition.eval(context))
									count++;
							}
							else
								break;
						}
					}
					else
					{
						for (int level = fromLevel; level >= toLevel; level--)
						{
							context.setTo(site);
							context.setLevel(level);
							if (!stopCondition.eval(context))
							{
								if (condition.eval(context))
									count++;
							}
							else
								break;
						}
					}
				}
			}
		}

		context.setTo(origTo);
		context.setLevel(origLevel);
		
		return count;
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
		long gameFlags = region.gameFlags(game) | condition.gameFlags(game) | stopCondition.gameFlags(game)
				| GameType.Stacking;
		
		gameFlags |= SiteType.gameFlags(type);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.or(region.concepts(game));
		concepts.or(condition.concepts(game));
		concepts.or(stopCondition.concepts(game));
		concepts.set(Concept.StackState.id(),true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(region.writesEvalContextRecursive());
		writeEvalContext.or(condition.writesEvalContextRecursive());
		writeEvalContext.or(stopCondition.writesEvalContextRecursive());
		return writeEvalContext;
	}
	
	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.set(EvalContextData.To.id(), true);
		writeEvalContext.set(EvalContextData.Level.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(region.readsEvalContextRecursive());
		readEvalContext.or(condition.readsEvalContextRecursive());
		readEvalContext.or(stopCondition.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		region.preprocess(game);
		condition.preprocess(game);
		stopCondition.preprocess(game);

		if (isStatic())
			preComputedInteger = Integer.valueOf(eval(new Context(game, null)));
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= region.missingRequirement(game);
		missingRequirement |= condition.missingRequirement(game);
		missingRequirement |= stopCondition.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= region.willCrash(game);
		willCrash |= condition.willCrash(game);
		willCrash |= stopCondition.willCrash(game);
		return willCrash;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{		
		String stopConditionString = "";
		if (stopCondition != null)
			stopConditionString = " stop when " + stopCondition.toEnglish(game);
		
		String conditionString = "";
		if (condition != null)
			conditionString = " if " + condition.toEnglish(game);
		
		return "the number of stacked pieces in " + region.toEnglish(game) + conditionString + stopConditionString;
	}
	
	//-------------------------------------------------------------------------
		
}
