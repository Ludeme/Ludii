package game.rules.play.moves.nonDecision.effect.set.player;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.ints.IntFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.types.play.RoleType;
import game.types.state.GameType;
import other.action.state.ActionSetScore;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

//-----------------------------------------------------------------------------

/**
 * Sets the score of a player.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SetScore extends Effect
{
	private static final long serialVersionUID = 1L;
	
	/** The player. */
	private final IntFunction playerFn;

	/** The score. */
	private final IntFunction scoreFn;

	//-------------------------------------------------------------------------

	/**
	 * @param player The index of the player.
	 * @param role   The roleType of the player.
	 * @param score  The new score.
	 * @param then   The moves applied after that move is applied.
	 */
	public SetScore
	(
			 @Or final game.util.moves.Player player,
		     @Or final RoleType               role,
			     final IntFunction            score,
		@Opt     final Then                   then
	)
	{
		super(then);

		if (player != null)
			this.playerFn = player.index();
		else
			this.playerFn = RoleType.toIntFunction(role);

		this.scoreFn = score;
	} 
	
	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());
		
		final int playerId = playerFn.eval(context);
		final int score = scoreFn.eval(context);
		final ActionSetScore actionScore = new ActionSetScore(playerId, score, Boolean.FALSE);
		final Move move = new Move(actionScore);
		moves.moves().add(move);
		
		if (then() != null)
			for (int j = 0; j < moves.moves().size(); j++)
				moves.moves().get(j).then().add(then().moves());

		// Store the Moves in the computed moves.
		for (int j = 0; j < moves.moves().size(); j++)
			moves.moves().get(j).setMovesLudeme(this);

		return moves;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public boolean canMoveTo(final Context context, final int target)
	{
		return false;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = GameType.Score | GameType.HashScores | super.gameFlags(game);
		gameFlags |= playerFn.gameFlags(game);
		gameFlags |= scoreFn.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.Scoring.id(), true);
		concepts.or(playerFn.concepts(game));
		concepts.or(scoreFn.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(playerFn.writesEvalContextRecursive());
		writeEvalContext.or(scoreFn.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(playerFn.readsEvalContextRecursive());
		readEvalContext.or(scoreFn.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= playerFn.missingRequirement(game);
		missingRequirement |= scoreFn.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= playerFn.willCrash(game);
		willCrash |= scoreFn.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
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
		playerFn.preprocess(game);
		scoreFn.preprocess(game);

		super.preprocess(game);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "SetScore";
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The player to set the score.
	 */
	public IntFunction player()
	{
		return playerFn;
	}

	/**
	 * @return The score to set.
	 */
	public IntFunction score()
	{
		return scoreFn;
	}
}