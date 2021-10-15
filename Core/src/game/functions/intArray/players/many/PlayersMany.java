package game.functions.intArray.players.many;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.intArray.BaseIntArrayFunction;
import game.functions.intArray.players.PlayersManyType;
import game.functions.ints.IntFunction;
import gnu.trove.list.array.TIntArrayList;
import other.context.Context;
import other.context.EvalContextData;

/**
 * Returns an array of the sizes of all the groups.
 * 
 * @author Eric.Piette
 */
@Hide
public final class PlayersMany extends BaseIntArrayFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The team type. */
	private final PlayersManyType team;

	/** The index of the related player. */
	private final IntFunction ofFn;
	
	/** The condition. */
	private final BooleanFunction cond;

	//-------------------------------------------------------------------------

	/**
	 * @param playerType The player type to return.
	 * @param of         The index of the related player.
	 * @param If         The condition to keep the players.
	 */
	public PlayersMany
	(
			       final PlayersManyType playerType,
		@Opt @Name final IntFunction     of,
		@Opt @Name final BooleanFunction If
	)
	{
		team = playerType;
		ofFn = of;
		cond = If;
	}

	//-------------------------------------------------------------------------

	@Override
	public int[] eval(final Context context)
	{
		final TIntArrayList indices = new TIntArrayList();
		final boolean requiresTeam = context.game().requiresTeams();
		final int numPlayers = context.game().players().size();
		final int savedPlayer = context.player();
		final int of = (ofFn != null) ? ofFn.eval(context) : context.state().mover();
		
		// If the related player is defined and not a real players we return an empty list.
		if((ofFn != null) && (of == 0 || of >= numPlayers))
			return indices.toArray();
		
		
		switch(team)
		{
		case All:
			for (int pid = 0; pid <= context.game().players().size(); ++pid)
			{
				context.setPlayer(pid);
				if (cond.eval(context))
					indices.add(pid);
			}
			break;
		case Ally:
			if (requiresTeam)
			{
				final int teamOf = context.state().getTeam(of);
				for (int pid = 1; pid < context.game().players().size(); ++pid)
				{
					context.setPlayer(pid);
					if (cond.eval(context))
						if (pid != of && context.state().playerInTeam(pid, teamOf))
							indices.add(pid);
			}
			}
			break;
		case Enemy:
			if (requiresTeam)
			{
				final int teamOf = context.state().getTeam(of);
				for (int pid = 1; pid < context.game().players().size(); ++pid)
				{
					context.setPlayer(pid);
					if (cond.eval(context))
						if (pid != context.state().mover() && !context.state().playerInTeam(pid, teamOf))
							indices.add(pid);
			}
			}
			else
			{
				for (int pid = 1; pid < context.game().players().size(); ++pid)
				{
					context.setPlayer(pid);
					if (cond.eval(context))
						if (pid != of)
							indices.add(pid);
			}
			}
			break;
		case Friend:
			if (requiresTeam)
			{
				final int teamOf = context.state().getTeam(of);
				for (int pid = 1; pid < context.game().players().size(); ++pid)
				{
					context.setPlayer(pid);
					if (cond.eval(context))
						if (context.state().playerInTeam(pid, teamOf))
							indices.add(pid);
			}
			}
			else
				indices.add(of);
			break;
		case NonMover:
			for (int pid = 0; pid < context.game().players().size(); ++pid)
			{
				context.setPlayer(pid);
				if (cond.eval(context))
					if (pid != context.state().mover())
						indices.add(pid);
		}
			break;
		default:
			break;
		
		}
		
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
		long gameFlags = cond.gameFlags(game);
		if (ofFn != null)
			gameFlags |= ofFn.gameFlags(game);
		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(cond.concepts(game));
		if (ofFn != null)
			concepts.or(ofFn.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(cond.writesEvalContextRecursive());
		if (ofFn != null)
			writeEvalContext.or(ofFn.writesEvalContextRecursive());
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
		if (ofFn != null)
			readEvalContext.or(ofFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		cond.preprocess(game);
		if (ofFn != null)
			ofFn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= cond.missingRequirement(game);
		if (ofFn != null)
			missingRequirement |= ofFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= cond.willCrash(game);
		if (ofFn != null)
			willCrash |= ofFn.willCrash(game);
		return willCrash;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "the sizes of all " + team.name() + " groups";
	}
	
	//-------------------------------------------------------------------------
}

