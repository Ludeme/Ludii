package game.functions.ints.match;

import java.util.BitSet;

import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.types.play.RoleType;
import main.Constants;
import other.context.Context;

/**
 * Returns the match score of a player.
 * 
 * @author Eric.Piette
 */
public final class MatchScore extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The match score of the player. */
	private final IntFunction idPlayerFn;

	//-------------------------------------------------------------------------

	/**
	 * @param role The roleType of the player.
	 * @example (matchScore P1)
	 */
	public MatchScore(final RoleType role)
	{
		idPlayerFn = RoleType.toIntFunction(role);
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final int pid = idPlayerFn.eval(context);
		
		if (context.parentContext() != null)
			return context.parentContext().score(pid);
		else if (context.isAMatch())
			return context.score(pid);

		return Constants.UNDEFINED;
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
		return idPlayerFn.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(idPlayerFn.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(idPlayerFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(idPlayerFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		// nothing to do
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (!game.hasSubgames())
		{
			game.addRequirementToReport("The ludeme (matchScore) is used but the game is not a match.");
			missingRequirement = true;
		}
		missingRequirement |= idPlayerFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= idPlayerFn.willCrash(game);
		return willCrash;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "MatchScore()";
	}
}
