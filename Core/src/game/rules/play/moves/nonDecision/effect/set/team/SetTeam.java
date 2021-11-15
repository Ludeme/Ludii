package game.rules.play.moves.nonDecision.effect.set.team;

import java.util.ArrayList;
import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.ints.IntFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.types.play.RoleType;
import game.types.state.GameType;
import main.Constants;
import other.action.Action;
import other.action.state.ActionAddPlayerToTeam;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;

/**
 * Sets a team.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SetTeam extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The index of the team. */
	final IntFunction teamIdFn;

	/** The index of the players on the team. */
	final IntFunction[] players;

	/** The roletypes used to check them in the required warning method. */
	final RoleType[] roles;

	//-------------------------------------------------------------------------

	/**
	 * @param team  The index of the team.
	 * @param roles The roleType of each player on the team.
	 * @param then  The moves applied after that move is applied.
	 */
	public SetTeam
	(
		     final IntFunction team, 
		     final RoleType[]  roles, 
		@Opt final Then        then
	)
	{
		super(then);

		this.teamIdFn = team;

		this.players = new IntFunction[roles.length];
		for (int i = 0; i < roles.length; i++)
		{
			final RoleType role = roles[i];
			this.players[i] = RoleType.toIntFunction(role);
		}

		this.roles = roles;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final BaseMoves moves = new BaseMoves(super.then());

		final int teamId = teamIdFn.eval(context);

		final Move move = new Move(new ArrayList<Action>());

		for (final IntFunction player : players)
		{
			final int playerIndex = player.eval(context);

			// We ignore all the player indices which are not real players.
			if (playerIndex < 1 || playerIndex > context.game().players().count())
				continue;

			final ActionAddPlayerToTeam actionTeam = new ActionAddPlayerToTeam(teamId, playerIndex);
			move.actions().add(actionTeam);
			move.setFromNonDecision(Constants.OFF);
			move.setToNonDecision(Constants.OFF);
			move.setMover(context.state().mover());
		}

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
		long gameFlags = GameType.Team;
		for (final IntFunction player : players)
			gameFlags |= player.gameFlags(game);
		gameFlags |= teamIdFn.gameFlags(game);
		if (then() != null)
			gameFlags |= then().gameFlags(game);
		return gameFlags;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		// We check if the roles are corrects.
		if (roles != null)
		{
			for (final RoleType role : roles)
			{
				final int indexOwnerPhase = role.owner();
				if (indexOwnerPhase < 1 || indexOwnerPhase > game.players().count())
				{
					game.addRequirementToReport(
							"At least a roletype is wrong in a starting rules (set Team ...): " + role + ".");
					missingRequirement = true;
					break;
				}
			}
		}

		final int teamId = teamIdFn.eval(new Context(game, new Trial(game)));
		if (teamId < 1 || teamId > game.players().count())
		{
			game.addRequirementToReport("In (set Team ...), the index of the team is wrong.");
			missingRequirement = true;
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
		willCrash |= teamIdFn.willCrash(game);

		for (final IntFunction player : players)
			willCrash |= player.willCrash(game);
		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.Team.id(), true);
		concepts.set(Concept.Coalition.id(), true);
		concepts.or(super.concepts(game));
		concepts.or(teamIdFn.concepts(game));

		for (final IntFunction player : players)
			concepts.or(player.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(teamIdFn.writesEvalContextRecursive());

		for (final IntFunction player : players)
			writeEvalContext.or(player.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(teamIdFn.readsEvalContextRecursive());

		for (final IntFunction player : players)
			readEvalContext.or(player.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
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
		teamIdFn.preprocess(game);

		for (final IntFunction player : players)
			player.preprocess(game);
	}

}
