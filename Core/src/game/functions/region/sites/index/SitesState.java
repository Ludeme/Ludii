package game.functions.region.sites.index;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.ints.IntFunction;
import game.functions.region.BaseRegionFunction;
import game.types.board.SiteType;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import other.context.Context;
import other.state.container.ContainerState;

/**
 * Returns all sites with a specified state value.
 * 
 * @author Eric Piette and cambolbro
 */
@Hide
public final class SitesState extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The local state. */
	private final IntFunction stateValue;
	
	//-------------------------------------------------------------------------

	/**
	 * @param elementType The graph element type.
	 * @param stateValue  The value of the local state.
	 */
	public SitesState
	(
		@Opt final SiteType    elementType, 
			 final IntFunction stateValue
	)
	{
		this.stateValue = stateValue;
		type = elementType;
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		// sites Owned
		final TIntArrayList sites = new TIntArrayList();

		final int stateId = stateValue.eval(context);
		
		final ContainerState cs = context.containerState(0);
		final int sitesTo = context.containers()[0].numSites();
			
		for (int site = 0; site < sitesTo; site++)
			if (cs.state(site, type) == stateId)
				sites.add(site);

		return new Region(sites.toArray());
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flags = 0;
		flags |= stateValue.gameFlags(game);

		flags |= SiteType.gameFlags(type);

		return flags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.or(stateValue.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(stateValue.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(stateValue.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		stateValue.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= stateValue.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= stateValue.willCrash(game);
		return willCrash;
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "all sites with a state value of " + stateValue.toEnglish(game);
	}
}
