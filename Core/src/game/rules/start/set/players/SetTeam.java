package game.rules.start.set.players;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.ints.IntFunction;
import game.rules.start.StartRule;
import game.types.play.RoleType;
import game.types.state.GameType;
import other.action.state.ActionAddPlayerToTeam;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;

/**
 * Creates a team.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SetTeam extends StartRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The index of the team. */
	final IntFunction teamIdFn;

	/** The index of the players on the team. */
	final IntFunction[] players;

	/** The roletypes used to check them in the required warning method. */
	final RoleType[] roles;

	/**
	 * @param team  The index of the team.
	 * @param roles The roleType of each player on the team.
	 */
	public SetTeam
	(
	     final IntFunction team,
		 final RoleType[] roles
	)
	{
		this.teamIdFn = team;

		this.players = new IntFunction[roles.length];
		for (int i = 0; i < roles.length; i++)
		{
			final RoleType role = roles[i];
			this.players[i] = RoleType.toIntFunction(role);
		}

		this.roles = roles;
	}

	@Override
	public void eval(final Context context)
	{
		final int teamId = teamIdFn.eval(context);
		
		for (final IntFunction player : players)
		{
			final int playerIndex = player.eval(context);

			// We ignore all the player indices which are not real players.
			if (playerIndex < 1 || playerIndex > context.game().players().count())
				continue;

			final ActionAddPlayerToTeam actionTeam = new ActionAddPlayerToTeam(teamId, playerIndex);
			actionTeam.apply(context, true);
			context.trial().addMove(new Move(actionTeam));
			context.trial().addInitPlacement();
		}

	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		for (final IntFunction player : players)
			if (!player.isStatic())
				return false;

		if (!teamIdFn.isStatic())
			return false;

		return true;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = GameType.Team;
		for (final IntFunction player : players)
			gameFlags |= player.gameFlags(game);
		gameFlags |= teamIdFn.gameFlags(game);
		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.Team.id(), true);
		for (final IntFunction player : players)
			concepts.or(player.concepts(game));
		concepts.or(teamIdFn.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		for (final IntFunction player : players)
			writeEvalContext.or(player.writesEvalContextRecursive());
		writeEvalContext.or(teamIdFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		for (final IntFunction player : players)
			readEvalContext.or(player.readsEvalContextRecursive());
		readEvalContext.or(teamIdFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		for (final IntFunction player : players)
			player.preprocess(game);
		teamIdFn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		// We check if the roles are corrects.
		if (roles != null)
		{
			for(final RoleType role : roles)
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
			game.addRequirementToReport(
					"In (set Team ...), the index of the team is wrong.");
			missingRequirement = true;
		}

		return missingRequirement;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final String str = "(SetTeam)";
		return str;
	}
}
