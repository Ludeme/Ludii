package game.functions.region.foreach.player;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.intArray.IntArrayFunction;
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
public final class ForEachPlayer extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Region to return for each player. */
	private final RegionFunction region;
	
	/** The list of players. */
	private final IntArrayFunction playersFn;
	
	//-------------------------------------------------------------------------

	/**
	 * @param players The list of players.
	 * @param region  The region.
	 */
	public ForEachPlayer
	(
	    final IntArrayFunction players,
	    final RegionFunction   region
	)
	{
		playersFn = players;
		this.region = region;
	}

	//-------------------------------------------------------------------------

	@Override
	public final Region eval(final Context context)
	{
		final TIntArrayList returnSites = new TIntArrayList();
		final int savedPlayer = context.player();

		if (playersFn == null)
		{
			for (int pid = 1; pid < context.game().players().size(); pid++)
			{
				context.setPlayer(pid);
				final int[] sites = region.eval(context).sites();
				for (final int site : sites)
					if (!returnSites.contains(site))
						returnSites.add(site);
			}
		}
		else
		{
			final int[] players = playersFn.eval(context);
			for (int i = 0 ; i < players.length ;i++)
			{
				final int pid = players[i];

				if (pid < 0 || pid > context.game().players().size())
					continue;

				context.setPlayer(pid);
				final int[] sites = region.eval(context).sites();
				for (final int site : sites)
					if (!returnSites.contains(site))
						returnSites.add(site);
			}
		}

		context.setPlayer(savedPlayer);

		return new Region(returnSites.toArray());
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = region.gameFlags(game);

		if (playersFn != null)
			gameFlags |= playersFn.gameFlags(game);

			return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		if (playersFn != null)
			concepts.or(playersFn.concepts(game));

		concepts.or(region.concepts(game));
		concepts.set(Concept.ControlFlowStatement.id(), true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();

		if (playersFn != null)
			writeEvalContext.or(playersFn.writesEvalContextRecursive());

		writeEvalContext.or(region.writesEvalContextRecursive());
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
		if (playersFn != null)
			readEvalContext.or(playersFn.readsEvalContextRecursive());

		readEvalContext.or(region.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (playersFn != null)
			missingRequirement |= (playersFn.missingRequirement(game));

		missingRequirement |= region.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;

		if (playersFn != null)
			willCrash |= (playersFn.willCrash(game));

		willCrash |= region.willCrash(game);
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
		region.preprocess(game);

		if (playersFn != null)
			playersFn.preprocess(game);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "for each player in " + playersFn.toEnglish(game) + " " + region.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------
		
}
