package game.rules.start.set.player;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.ints.IntFunction;
import game.rules.start.StartRule;
import game.types.play.RoleType;
import game.types.state.GameType;
import other.action.state.ActionSetScore;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

/**
 * Initialises the score of the players.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SetScore extends StartRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The players. */
	protected final IntFunction players[];

	/** The score. */
	protected final IntFunction scores[];

	/** To init the same score to each player. */
	protected final boolean InitSameScoreToEachPlayer;

	//-------------------------------------------------------------------------

	/**
	 * @param role  The roleType of a player.
	 * @param score The new score of a player.
	 * 
	 * @example (set Score P1 100)
	 */
	public SetScore
	(
		     final RoleType    role,
		@Opt final IntFunction score
	)
	{
		if (role == RoleType.Each || role == RoleType.All)
		{
			InitSameScoreToEachPlayer = true;
			players = new IntFunction[0];
		}
		else
		{
			players = new IntFunction[]
			{ RoleType.toIntFunction(role) };
			InitSameScoreToEachPlayer = false;
		}

		scores = new IntFunction[]
		{ score };
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void eval(final Context context)
	{
		// To handle the Each roleType.
		if (InitSameScoreToEachPlayer)
		{
			final int score = scores[0].eval(context);
			for (int pid = 1; pid < context.game().players().size(); pid++)
			{
				final Move move;
				final ActionSetScore actionScore = new ActionSetScore(pid, score, Boolean.FALSE);
				actionScore.apply(context, true);
				move = new Move(actionScore);
				context.trial().addMove(move);
				context.trial().addInitPlacement();
			}
		}
		else
		{
			final int length = Math.min(players.length, scores.length);
			for (int i = 0; i < length; i++)
			{
				final int playerId = players[i].eval(context);
				final int score = scores[i].eval(context);
				final Move move;
				final ActionSetScore actionScore = new ActionSetScore(playerId, score, Boolean.FALSE);
				actionScore.apply(context, true);
				move = new Move(actionScore);
				context.trial().addMove(move);
				context.trial().addInitPlacement();
			}
		}
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
		return GameType.Score;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		if (players != null)
			for (final IntFunction player : players)
				concepts.or(player.concepts(game));

		if (scores != null)
			for (final IntFunction score : scores)
				concepts.or(score.concepts(game));

		concepts.set(Concept.Scoring.id(), true);
		concepts.set(Concept.InitialScore.id(), true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (players != null)
			for (final IntFunction player : players)
				writeEvalContext.or(player.writesEvalContextRecursive());

		if (scores != null)
			for (final IntFunction score : scores)
				writeEvalContext.or(score.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (players != null)
			for (final IntFunction player : players)
				readEvalContext.or(player.readsEvalContextRecursive());

		if (scores != null)
			for (final IntFunction score : scores)
				readEvalContext.or(score.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		// Do nothing.
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString() 
	{
		String str = "(initScore ";
		final int length = Math.min(players.length, scores.length);
		for (int i = 0 ; i < length ; i++) 
		{
			str += players[i] + " = " + scores[i];
			if(i != length-1)
				str+=",";
		}
		str+=")";
		return str;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		String englishString = "set the scores of the players as follows, ";
		
		for (int i = 0; i < players.length; i++)
			englishString += players[i].toEnglish(game) + "=" + scores[i].toEnglish(game) + ", ";
		
		return englishString.substring(0, englishString.length()-2);
	}
	
	//-------------------------------------------------------------------------

}
