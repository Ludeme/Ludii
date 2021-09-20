package game.rules.play.moves.nonDecision.effect.state;

import java.util.BitSet;

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

/**
 * Adds a value to the score of a player.
 * 
 * @author Eric.Piette
 */
public final class AddScore extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** The players. */
	private final IntFunction players[];

	/** The score. */
	private final IntFunction scores[];

	/** The roleTypes used to check in the requiredLudeme */
	private final RoleType[] roles;

	//-------------------------------------------------------------------------

	/**
	 * For adding a score to a player.
	 * 
	 * @param player The index of the player.
	 * @param role   The roleType of the player.
	 * @param score  The score of the player.
	 * @param then   The moves applied after that move is applied.
	 * 
	 * @example (addScore Mover 50)
	 */
	public AddScore
	(
		     @Or final game.util.moves.Player player,
		     @Or final RoleType               role,
			     final IntFunction            score,
		@Opt     final Then                   then
	)
	{
		super(then);

		int numNonNull = 0;
		if (player != null)
			numNonNull++;
		if (role != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Exactly one Or parameter must be non-null.");

		players = new IntFunction[1];
		players[0] = (player == null) ? RoleType.toIntFunction(role) : player.index();
		
		if (score != null)
		{
			scores = new IntFunction[1];
			scores[0] = score;
		}
		else
		{
			scores = null;
		}

		roles = (role != null) ? new RoleType[] { role } : null;
	} 
	
	/**
	 * For adding a score to many players.
	 * 
	 * @param players The indices of the players.
	 * @param roles   The roleType of the players.
	 * @param scores  The scores to add.
	 * @param then    The moves applied after that move is applied.
	 * 
	 * 
	 * @example (addScore {P1 P2} {50 10})
	 */
	public AddScore
	(
		     @Or final IntFunction[] players,
		     @Or final RoleType[]    roles,
			     final IntFunction[] scores,
		@Opt     final Then then
	)
	{
		super(then);
		int numNonNull = 0;
		if (players != null)
			numNonNull++;
		if (roles != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Exactly one Or parameter must be non-null.");

		if (players != null)
		{
			this.players = players;
		}
		else
		{
			this.players = new IntFunction[roles.length];
			for (int i = 0; i < roles.length; i++)
			{
				final RoleType role = roles[i];
				this.players[i] = RoleType.toIntFunction(role);
			}
		}
		this.scores = scores;
		this.roles = roles;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());
		
		final int length = Math.min(players.length, scores.length);
		if(roles != null)
		{
			for (int i = 0; i < length; i++)
			{
				final RoleType role = roles[i];
				final int score = scores[i].eval(context);
				final TIntArrayList idPlayers = PlayersIndices.getIdRealPlayers(context, role);
				for(int j = 0; j < idPlayers.size();j++)
				{
					final int pid = idPlayers.get(j);
					final ActionSetScore actionScore = new ActionSetScore(pid, score, Boolean.TRUE);
					final Move move = new Move(actionScore);
					moves.moves().add(move);
				}
			}
		}
		else
		{
			for (int i = 0; i < length; i++)
			{
				final int playerId = players[i].eval(context);
				final int score = scores[i].eval(context);
				final ActionSetScore actionScore = new ActionSetScore(playerId, score, Boolean.TRUE);
				final Move move = new Move(actionScore);
				moves.moves().add(move);
			}
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

		for (final IntFunction player : players)
			gameFlags |= player.gameFlags(game);

		for (final IntFunction score : scores)
			gameFlags |= score.gameFlags(game);

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

		for (final IntFunction player : players)
			concepts.or(player.concepts(game));

		for (final IntFunction score : scores)
			concepts.or(score.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());

		for (final IntFunction player : players)
			writeEvalContext.or(player.writesEvalContextRecursive());

		for (final IntFunction score : scores)
			writeEvalContext.or(score.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());

		for (final IntFunction player : players)
			readEvalContext.or(player.readsEvalContextRecursive());

		for (final IntFunction score : scores)
			readEvalContext.or(score.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		missingRequirement |= super.missingRequirement(game);

		if (roles != null)
		{
			for (final RoleType role : roles)
			{
				final int indexOwnerPhase = role.owner();

				if (role.equals(RoleType.Mover) || role.equals(RoleType.Next) || role.equals(RoleType.Prev))
					continue;

				if (indexOwnerPhase < 1 || indexOwnerPhase > game.players().count())
				{
					game.addRequirementToReport(
							"An incorrect roletype is used in the ludeme (addScore ...): " + role + ".");
					missingRequirement = true;
				}
			}
		}

		for (final IntFunction player : players)
			missingRequirement |= player.missingRequirement(game);

		for (final IntFunction score : scores)
			missingRequirement |= score.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);

		for (final IntFunction player : players)
			willCrash |= player.willCrash(game);

		for (final IntFunction score : scores)
			willCrash |= score.willCrash(game);

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
		super.preprocess(game);

		for (final IntFunction player : players)
			player.preprocess(game);

		if (scores != null)
			for (final IntFunction score : scores)
				score.preprocess(game);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		String text = "";
		
		for (int i = 0; i < players.length; i++)
			text += "add score " + scores[i].toEnglish(game) + " to player " + players[i].toEnglish(game) + "\n";
			
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		return text + thenString;
	}
	
}
