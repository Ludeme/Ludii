package game.functions.region.sites.player;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.equipment.other.Regions;
import game.functions.ints.IntFunction;
import game.functions.region.BaseRegionFunction;
import game.functions.region.RegionFunction;
import game.types.play.RoleType;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.context.Context;

/**
 * Returns all the sites of a region defined in the equipment.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SitesEquipmentRegion extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** If we can, we'll precompute once and cache. */
	private Region precomputedRegion = null;

	/**
	 * If we can precompute on a per-player basis (and not just once above), we
	 * store here
	 */
	private Region[] precomputedPerPlayer = null;
	
	/**
	 * For every player, a precomputed list of regions that satisfy the name check.
	 * If index == null, we only just index 0.
	 * 
	 * NOTE: this "optimisation" works even if we're not static!
	 */
	private List<game.equipment.other.Regions>[] regionsPerPlayer = null;

	//-------------------------------------------------------------------------

	/** The index of the column. */
	private final IntFunction index;
	
	/** The name of the region. */
	private final String name;
	
	//-------------------------------------------------------------------------

	/**
	 * @param player Index of the row, column or phase to return.
	 * @param role   The Role type corresponding to the index.
	 * @param name   The name of the region to return.
	 */
	public SitesEquipmentRegion
	(
		@Or @Opt final game.util.moves.Player player,
		@Or @Opt final RoleType	              role,
		    @Opt final String                 name
	)
	{
		index = (role != null) ? RoleType.toIntFunction(role) : (player != null) ? player.index() : null;
		this.name = (name == null) ? "" : name;
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		if (precomputedRegion != null)
			return precomputedRegion;
		
		// NOTE: if index == null, we'll use index 0 in our regionsPerPlayer array
		final int who = (index != null) ? index.eval(context) : 0;
		
		if (precomputedPerPlayer != null)
			return precomputedPerPlayer[who];
		
		return computeForWho(context, who);
	}
	
	@Override
	public boolean contains(final Context context, final int location)
	{
		if (precomputedRegion != null || precomputedPerPlayer != null)
			return super.contains(context, location);
		
		// NOTE: if index == null, we'll use index 0 in our regionsPerPlayer array
		final int who = (index != null) ? index.eval(context) : 0;

		for (final game.equipment.other.Regions region : regionsPerPlayer[who])
		{
			if (region.contains(context, location))
				return true;
		}

		return false;
	}
	
	/**
	 * @param context The context.
	 * @param player  The player.
	 * @return Region for given player (use 0 also for no index given)
	 */
	private Region computeForWho(final Context context, final int player)
	{
		final List<TIntArrayList> siteLists = new ArrayList<TIntArrayList>();
		int totalNumSites = 0;

		for (final game.equipment.other.Regions region : regionsPerPlayer[player])
		{
			final TIntArrayList wrapped = TIntArrayList.wrap(region.eval(context));
			siteLists.add(wrapped);
			totalNumSites += wrapped.size();
		}

		final int[] sites = new int[totalNumSites];
		int startIdx = 0;

		for (final TIntArrayList wrapped : siteLists)
		{
			wrapped.toArray(sites, 0, startIdx, wrapped.size());
			startIdx += wrapped.size();
		}

		return new Region(sites);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		if (index != null)
			return index.isStatic();

		return true;
	}

	@Override
	public String toString()
	{
		return "EquipmentRegion()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flags = 0L;

		if (index != null)
			flags = index.gameFlags(game);

		return flags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		if (index != null)
			concepts.or(index.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (index != null)
			writeEvalContext.or(index.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (index != null)
			readEvalContext.or(index.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (game.equipment().regions().length == 0)
		{
			game.addRequirementToReport(
					"The ludeme (sites ...) to get a region of the equipment is used but the equipment has no defined region.");
			missingRequirement = true;
		}
		if (index != null)
			missingRequirement |= index.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (index != null)
			willCrash |= index.willCrash(game);
		return willCrash;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void preprocess(final Game game)
	{
		if (regionsPerPlayer != null)
			return;		// We've already done our preprocessing
		
		if (index != null)
		{
			index.preprocess(game);

			// Finally we have access to game, now we can compute if we were
			// actually static all along
			final boolean whoStatic = index.isStatic();

			boolean regionsStatic = true;
			final game.equipment.other.Regions[] regions = game.equipment().regions();
			
			regionsPerPlayer = new List[Constants.MAX_PLAYERS + 1];
			for (int p = 0; p < regionsPerPlayer.length; ++p)
			{
				regionsPerPlayer[p] = new ArrayList<game.equipment.other.Regions>();
			}
			
			for (final game.equipment.other.Regions region : regions)
			{
				if (region.name().contains(name))
				{
					regionsPerPlayer[region.owner()].add(region);
					
					// Make sure that this region itself also gets preprocessed
					region.preprocess(game);
				}
			}

			for (final game.equipment.other.Regions region : regions)
				if (region.region() != null)
					for (final RegionFunction regionFunction : region.region())
						regionsStatic &= regionFunction.isStatic();

			if (whoStatic && regionsStatic)
			{
				precomputedRegion = eval(new Context(game, null));
			}
			else if (regionsStatic)
			{
				precomputedPerPlayer = new Region[Constants.MAX_PLAYERS + 1];

				for (int p = 0; p < precomputedPerPlayer.length; ++p)
				{
					precomputedPerPlayer[p] = computeForWho(new Context(game, null), p);
				}
			}
		}
		else
		{
			final game.equipment.other.Regions[] regions = game.equipment().regions();
			regionsPerPlayer = new List[1];
			regionsPerPlayer[0] = (new ArrayList<game.equipment.other.Regions>());
			for (final game.equipment.other.Regions region : regions)
			{
				if (region.name().equals(name))
				{
					regionsPerPlayer[0].add(region);
					
					// Make sure that this region itself also gets preprocessed
					region.preprocess(game);
				}
			}
		}
		
		if (index == null && isStatic())
			precomputedRegion = eval(new Context(game, null));
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		if (precomputedRegion != null)
			return precomputedRegion.toEnglish(game);
		
		String text = "";
		
		if(regionsPerPlayer == null)
			return text;
		
		for (final List<Regions> regions : regionsPerPlayer) 
		{
			if (!regions.isEmpty()) 
			{
				for (final Regions region : regions) 
				{					
					text += region.name();
					text += " or ";
				}
			}
		}
		
		if (text.length() > 4)
			text = text.substring(0, text.length()-4);
		
		return text;
	}

}
