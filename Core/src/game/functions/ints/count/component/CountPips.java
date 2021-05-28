package game.functions.ints.count.component;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.equipment.container.other.Dice;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.board.Id;
import game.types.play.RoleType;
import game.types.state.GameType;
import other.concept.Concept;
import other.context.Context;

/**
 * Returns the number of pips of all the dice.
 * 
 * @author Eric.Piette
 */
@Hide
public final class CountPips extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The index of the player. */
	private final IntFunction whoFn;

	/**
	 * @param role The role of the player [All].
	 * @param of   The index of the player.
	 */
	public CountPips
	(
		@Opt @Or	    final RoleType    role,
		@Opt @Or  @Name final IntFunction of
	)
	{

		this.whoFn = (of != null) ? of : (role != null) ? RoleType.toIntFunction(role) : new Id(null, RoleType.Shared);
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final int pid = whoFn.eval(context);

		// if that we return the sum of the handDice if exist
		for (int i = 0; i < context.game().handDice().size(); i++)
		{
			final Dice dice = context.game().handDice().get(i);
			if (pid == dice.owner())
				return context.state().sumDice(i);
		}

		// if no HandDice detected
		return 0;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public String toString()
	{
		return "Pips()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		return GameType.Stochastic | whoFn.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(whoFn.concepts(game));
		concepts.set(Concept.Dice.id(), true);
		concepts.set(Concept.SumDice.id(), true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(whoFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(whoFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		whoFn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		if (!game.hasHandDice())
		{
			game.addRequirementToReport("The ludeme (count Pips) is used but the equipment has no dice.");
			missingRequirement = true;
		}

		missingRequirement |= whoFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= whoFn.willCrash(game);
		return willCrash;
	}
}
