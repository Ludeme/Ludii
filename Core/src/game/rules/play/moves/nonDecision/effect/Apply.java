package game.rules.play.moves.nonDecision.effect;

import java.util.BitSet;

import annotations.Name;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.NonDecision;
import other.context.Context;

/**
 * Returns the effect to apply only if the condition is satisfied.
 * 
 * @author Eric.Piette
 */
public final class Apply extends Moves
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which condition. */
	final BooleanFunction cond;

	/** If the condition is true. */
	final NonDecision effect;

	//-------------------------------------------------------------------------

	/**
	 * For checking a condition before to apply the default effect.
	 * 
	 * @param If The condition to satisfy to get the effect moves.
	 * 
	 * @example (apply if:(is Mover P1))
	 */
	public Apply
	(
		@Name final BooleanFunction If
	)
	{
		super(null);
		this.cond = If;
		this.effect = null;
	}
	
	/**
	 * For applying an effect.
	 * 
	 * @param effect The moves to apply to make the effect.
	 * 
	 * @example (apply (moveAgain))
	 */
	public Apply
	(
		final NonDecision    effect
	)
	{
		super(null);
		this.cond = null;
		this.effect = effect;
	}
	
	/**
	 * For applying an effect if a condition is verified.
	 * 
	 * @param If     The condition to satisfy to get the effect moves.
	 * @param effect The moves to apply to make the effect.
	 * 
	 * @example (apply if:(is Mover P1) (moveAgain))
	 */
	public Apply
	(
	  @Name final BooleanFunction If,
		    final NonDecision    effect
	)
	{
		super(null);
		this.cond = If;
		this.effect = effect;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		if (cond == null || cond.eval(context))
			return effect.eval(context);

		return new BaseMoves(super.then());
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean canMove(final Context context)
	{
		if (cond == null || cond.eval(context))
			return effect.canMove(context);

		return false;
	}

	@Override
	public boolean canMoveTo(final Context context, final int target)
	{
		if (cond == null || cond.eval(context))
			return effect.canMoveTo(context, target);
		else
			return false;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long flags = super.gameFlags(game);

		if (cond != null)
			flags |= cond.gameFlags(game);

		if (effect != null)
			flags |= effect.gameFlags(game);

		return flags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));

		if (cond != null)
			concepts.or(cond.concepts(game));

		if (effect != null)
			concepts.or(effect.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());

		if (cond != null)
			writeEvalContext.or(cond.writesEvalContextRecursive());

		if (effect != null)
			writeEvalContext.or(effect.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());

		if (cond != null)
			readEvalContext.or(cond.readsEvalContextRecursive());

		if (effect != null)
			readEvalContext.or(effect.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);

		if (cond != null)
			missingRequirement |= cond.missingRequirement(game);

		if (effect != null)
			missingRequirement |= effect.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);

		if (cond != null)
			willCrash |= cond.willCrash(game);

		if (effect != null)
			willCrash |= effect.willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		if (cond != null && !cond.isStatic())
			return false;

		if (effect != null && !effect.isStatic())
			return false;

		return true;
	}

	@Override
	public void preprocess(final Game game)
	{
		super.preprocess(game);

		if (cond != null)
			cond.preprocess(game);

		if (effect != null)
			effect.preprocess(game);
	}

	/**
	 * @return The condition.
	 */
	public BooleanFunction condition()
	{
		return cond;
	}

	/**
	 * @return The effect.
	 */
	public Moves effect()
	{
		return effect;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toEnglish(final Game game)
	{
		return "Effect";
	}
}
