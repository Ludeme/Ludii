package game.rules.end;

import java.io.Serializable;
import java.util.BitSet;

import game.Game;
import game.types.play.ResultType;
import game.types.play.RoleType;
import other.BaseLudeme;
import other.concept.Concept;
import other.context.Context;
import other.translation.LanguageUtils;

/**
 * Gives the result when an ending rule is reached for a specific player/team.
 * 
 * @author cambolbro and Eric.Piette
 */
public class Result extends BaseLudeme implements Serializable
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The player or the team. */
	private final RoleType who;

	/** The result type of the player or team. */
	private final ResultType result;

	//-------------------------------------------------------------------------

	/**
	 * @param who    The player or the team.
	 * @param result The result type of the player or team.
	 * @example (result Mover Win)
	 */
	public Result
	(
		final RoleType   who, 
		final ResultType result
	)
	{
		this.who    = who;
		this.result = result;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return role of player, else None or All.
	 */
	public RoleType who()
	{
		return who;
	}

	/**
	 * @return Result type.
	 */
	public ResultType result()
	{
		return result;
	}

	/**
	 * Allow eval for iterators.
	 * 
	 * @param context
	 */
	public void eval(final Context context)
	{
		// Nothing to do.
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();

		if (result != null)
			if (result.equals(ResultType.Draw))
				concepts.set(Concept.Draw.id(), true);

		return concepts;
	}

	/**
	 * @param game The game.
	 * @return The long value corresponding of the state flags.
	 */
	@SuppressWarnings("static-method")
	public long gameFlags(final Game game)
	{
		return 0;
	}

	/**
	 * @param game
	 */
	public void preprocess(final Game game)
	{
		// Placeholder for derived classes to implement.
	}
	
	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		// We check if the role is correct.
		final int indexOwnerPhase = who.owner();
		if ((indexOwnerPhase < 1 && !who.equals(RoleType.All) && !who.equals(RoleType.Player)
				&& !who.equals(RoleType.Mover) && !who.equals(RoleType.TeamMover)
				&& !who.equals(RoleType.Next) && !who.equals(RoleType.Prev))
				|| indexOwnerPhase > game.players().count())
		{
			game.addRequirementToReport(
					"A result is defined in the ending rules with an incorrect player: " + who + ".");
			missingRequirement = true;
		}

		if (result.equals(ResultType.Crash))
		{
			game.addRequirementToReport("A crash result is used in an ending condition.");
			missingRequirement = true;
		}

		if (result.equals(ResultType.Abandon))
		{
			game.addRequirementToReport("An abandon result is used in an ending condition.");
			missingRequirement = true;
		}

		return missingRequirement;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return "[Result: " + who + " " + result + "]";
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		switch(result) 
		{
		case Win:
			return LanguageUtils.RoleTypeAsText(who, false) + " wins";
		case Loss:
			return LanguageUtils.RoleTypeAsText(who, false) + " loses";
		case Draw:
			return "it's a draw";
		default:
			throw new RuntimeException("Not implemented yet! [ResultType=" + result.name() + "]");
		}
	}
}
