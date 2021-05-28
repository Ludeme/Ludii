package game.functions.region.foreach.team;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.region.BaseRegionFunction;
import game.functions.region.RegionFunction;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;

/**
 * Iterates through the players, generating moves based on the indices of the
 * players.
 * 
 * @author Eric.Piette
 */
@Hide
public final class ForEachTeam extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	/** Region to return for each player. */
	private final RegionFunction region;

	// -------------------------------------------------------------------------

	/**
	 * @param region The region.
	 */
	public ForEachTeam(final RegionFunction region)
	{
		this.region = region;
	}

	// -------------------------------------------------------------------------

	@Override
	public final Region eval(final Context context)
	{
		final TIntArrayList returnSites = new TIntArrayList();
		final int[] savedTeam = context.team();

		for (int tid = 1; tid < context.game().players().size(); tid++)
		{
			final TIntArrayList team = new TIntArrayList();
			for (int pid = 1; pid < context.game().players().size(); pid++)
			{
				if (context.state().playerInTeam(pid, tid))
					team.add(pid);
			}
			if (!team.isEmpty())
			{
				context.setTeam(team.toArray());
				final int[] sites = region.eval(context).sites();
				for (final int site : sites)
					if (!returnSites.contains(site))
						returnSites.add(site);
			}
		}
		context.setTeam(savedTeam);
		return new Region(returnSites.toArray());
	}

	// -------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return region.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		return region.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(region.concepts(game));
		concepts.set(Concept.ControlFlowStatement.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(region.writesEvalContextRecursive());
		return writeEvalContext;
	}
	
	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.set(EvalContextData.Team.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(region.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		region.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= region.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= region.willCrash(game);
		return willCrash;
	}
}
