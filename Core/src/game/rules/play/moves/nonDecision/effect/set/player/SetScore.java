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
import gnu.trove.list.array.TIntArrayList;
import other.PlayersIndices;
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

	/** The roleType. */
	private final RoleType role;

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

		playerFn = (player == null) ? RoleType.toIntFunction(role) : player.index();
		this.role = role;
		scoreFn = score;
	} 
	
	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());
		final int playerId = playerFn.eval(context);
		final int score = scoreFn.eval(context);
		
		if(role != null)
		{
			// Code to handle specific roleType.
			final TIntArrayList idPlayers = PlayersIndices.getIdRealPlayers(context, role);
			for(int i = 0; i < idPlayers.size();i++)
			{
				final int pid = idPlayers.get(i);
				final ActionSetScore actionScore = new ActionSetScore(pid, score, Boolean.FALSE);
				final Move move = new Move(actionScore);
				moves.moves().add(move);
			}
		}
		else
		{
			final ActionSetScore actionScore = new ActionSetScore(playerId, score, Boolean.FALSE);
			final Move move = new Move(actionScore);
			moves.moves().add(move);
		}
		
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

		if (role != null && !game.requiresTeams())
		{
			if (RoleType.isTeam(role) && !game.requiresTeams())
			{
				game.addRequirementToReport(
						"(sites Occupied ...): A roletype corresponding to a team is used but the game has no team: "
								+ role + ".");
				missingRequirement = true;
			}
		}
		
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
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		return "set score of " + playerFn.toEnglish(game) + " to " + scoreFn.toEnglish(game) + thenString;
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