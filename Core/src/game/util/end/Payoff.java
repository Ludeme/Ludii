package game.util.end;

import java.util.BitSet;

import game.Game;
import game.functions.floats.FloatFunction;
import game.types.play.RoleType;
import game.types.state.GameType;
import other.BaseLudeme;

/**
 * Defines a payoff to set when using the {\tt (payoffs ...)} end rule.
 * 
 * @author Eric.Piette
 */
public class Payoff extends BaseLudeme
{
	final RoleType role;
	final FloatFunction payoff;

	//-------------------------------------------------------------------------

	/**
	 * @param role   The role of the player.
	 * @param payoff The payoff of the player.
	 * 
	 * @example (payoff P1 5.5)
	 */
	public Payoff(final RoleType role, final FloatFunction payoff)
	{
		this.role = role;
		this.payoff = payoff;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The role.
	 */
	public RoleType role()
	{
		return role;
	}

	/**
	 * @return The payoff.
	 */
	public FloatFunction payoff()
	{
		return payoff;
	}

	/**
	 * @param game The game.
	 * @return The game flags.
	 */
	public long gameFlags(final Game game)
	{
		long gameFlags = 0L;

		gameFlags |= GameType.Payoff;

		gameFlags |= payoff.gameFlags(game);

		return gameFlags;
	}

	/**
	 * To preprocess.
	 * 
	 * @param game The game.
	 */
	public void preprocess(final Game game)
	{
		payoff.preprocess(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(payoff.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		// We check if the role is correct.
		if (role != null)
		{
			final int indexOwnerPhase = role().owner();
			if (((indexOwnerPhase < 1 && !role().equals(RoleType.Mover)) && !role().equals(RoleType.Next)
					&& !role().equals(RoleType.Prev)) || indexOwnerPhase > game.players().count())
			{
				game.addRequirementToReport(
						"The ludeme (payoff ...) is used with an incorrect RoleType: " + role() + ".");
				missingRequirement = true;
			}
		}

		if (payoff != null)
			missingRequirement |= payoff.missingRequirement(game);

		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (payoff != null)
			willCrash |= payoff.willCrash(game);
		return willCrash;
	}
}
