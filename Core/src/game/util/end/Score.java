package game.util.end;

import java.util.BitSet;

import game.Game;
import game.functions.ints.IntFunction;
import game.types.play.RoleType;
import game.types.state.GameType;
import other.BaseLudeme;
import other.concept.Concept;

/**
 * Defines a score to set when using the {\tt (byScore ...)} end rule.
 * 
 * @author Eric.Piette
 */
public class Score extends BaseLudeme
{
	final RoleType    role;
	final IntFunction score;

	//-------------------------------------------------------------------------
	
	/**
	 * @param role  The role of the player.
	 * @param score The score of the player.
	 * 
	 * @example (score P1 100)
	 */
	public Score
	(
		final RoleType    role, 
		final IntFunction score
	)
	{
		this.role  = role;
		this.score = score;
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
	 * @return The score.
	 */
	public IntFunction score()
	{
		return score;
	}

	/**
	 * @param game The game.
	 * @return The game flags.
	 */
	public long gameFlags(final Game game)
	{
		long gameFlags = 0L;

		gameFlags |= GameType.Score;

		gameFlags |= score.gameFlags(game);

		return gameFlags;
	}

	/**
	 * To preprocess.
	 * 
	 * @param game The game.
	 */
	public void preprocess(final Game game)
	{
		score.preprocess(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(score.concepts(game));
		concepts.set(Concept.Scoring.id(), true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(score.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(score.readsEvalContextRecursive());
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
						"The ludeme (score ...) is used with an incorrect RoleType: " + role() + ".");
				missingRequirement = true;
			}
		}

		if (score != null)
			missingRequirement |= score.missingRequirement(game);

		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (score != null)
			willCrash |= score.willCrash(game);
		return willCrash;
	}
}
