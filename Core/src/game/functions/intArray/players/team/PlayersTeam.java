package game.functions.intArray.players.team;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.intArray.BaseIntArrayFunction;
import game.functions.intArray.players.PlayersTeamType;
import gnu.trove.list.array.TIntArrayList;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;

/**
 * Returns an array of the sizes of all the groups.
 * 
 * @author Eric.Piette
 */
@Hide
public final class PlayersTeam extends BaseIntArrayFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The team type. */
	private final PlayersTeamType team;
	
	/** The condition. */
	private final BooleanFunction cond;

	//-------------------------------------------------------------------------

	/**
	 * @param playerType The player type to return.
	 * @param If         The condition to keep the players.
	 */
	public PlayersTeam
	(
		         final PlayersTeamType playerType,
	  @Opt @Name final BooleanFunction If
	)
	{
		this.team = playerType;
		this.cond = If;
	}

	//-------------------------------------------------------------------------

	@Override
	public int[] eval(final Context context)
	{
		final TIntArrayList indices = new TIntArrayList();
		final boolean requiresTeam = context.game().requiresTeams();
		final int numPlayers = context.game().players().size();
		final int teamIndex = team.index();
		final int savedPlayer = context.player();

		if (requiresTeam)
		{
			for (int pid = 1; pid < numPlayers; pid++)
			{
				context.setPlayer(pid);
				if (cond.eval(context))
					if (context.state().playerInTeam(pid, teamIndex))
						indices.add(pid);
			}
		}
		else if (numPlayers > teamIndex)
			indices.add(teamIndex);

		context.setPlayer(savedPlayer);
		return indices.toArray();
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public String toString()
	{
		return "Players()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		return cond.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.Team.id(), true);
		concepts.or(cond.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(cond.writesEvalContextRecursive());
		return writeEvalContext;
	}
	
	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.set(EvalContextData.Player.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(cond.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		cond.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= cond.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= cond.willCrash(game);
		return willCrash;
	}
}

