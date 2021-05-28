package game.functions.booleans.is.edge;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.IntFunction;
import game.types.state.GameType;
import other.context.Context;

/**
 * Checks if two edges are crossing.
 * 
 * @author Eric.Piette
 */
@Hide
public final class IsCrossing extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------

	/** First edge. */
	private final IntFunction edge1Fn;

	/** Second edge. */
	private final IntFunction edge2Fn;

	/** Precomputed boolean. */
	private Boolean precomputedBoolean;

	//-------------------------------------------------------------------------
 
	/**
	 * @param edge1 The index of the first edge.
	 * @param edge2 The index of the second edge.
	 */
	public IsCrossing
	(
		final IntFunction edge1, 
		final IntFunction edge2
	)
	{
		this.edge1Fn = edge1;
		this.edge2Fn = edge2;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		if (precomputedBoolean != null)
			return precomputedBoolean.booleanValue();

		final int edge1 = edge1Fn.eval(context);
		final int edge2 = edge2Fn.eval(context);

		if (
			edge1 < 0 
			|| 
			edge2 < 0 
			|| 
			edge1 >= context.topology().edges().size()
			|| 
			edge2 >= context.topology().edges().size()
		   )
			return false;

		return context.topology().edges().get(edge1).doesCross(edge2);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return this.edge1Fn.isStatic() && this.edge2Fn.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		return edge1Fn.gameFlags(game) | edge2Fn.gameFlags(game) | GameType.Edge | GameType.Graph;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(edge1Fn.concepts(game));
		concepts.or(edge2Fn.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(edge1Fn.writesEvalContextRecursive());
		writeEvalContext.or(edge2Fn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(edge1Fn.readsEvalContextRecursive());
		readEvalContext.or(edge2Fn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		edge1Fn.preprocess(game);
		edge2Fn.preprocess(game);

		if (isStatic())
			precomputedBoolean = Boolean.valueOf(eval(new Context(game, null)));
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= edge1Fn.missingRequirement(game);
		missingRequirement |= edge2Fn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= edge1Fn.willCrash(game);
		willCrash |= edge2Fn.willCrash(game);
		return willCrash;
	}
}