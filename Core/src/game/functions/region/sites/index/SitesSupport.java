package game.functions.region.sites.index;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.ints.IntFunction;
import game.functions.region.BaseRegionFunction;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.equipment.Region;
import other.context.Context;
import other.state.container.ContainerState;
import other.topology.TopologyElement;
import gnu.trove.list.array.TIntArrayList;

/**
 * Returns the sites which are supporting other pieces on sites on top of them.
 * 
 * @author Cedric Antoine
 */

@Hide
public final class SitesSupport extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;
	
	/** piece type to be supported. */
	private IntFunction locnFn;

	//-------------------------------------------------------------------------

	/**
	 * @param type Type of graph element [default SiteType of the board].
	 * @param what The type of pieces to be supported by the sites looked for.
	 * 
	 * @example (sites Support)
	 */
	
	public SitesSupport
	(
		@Opt final SiteType type,
		@Opt final IntFunction what
	)
	{
		this.type = type;
		locnFn = (what != null) ? what : null;
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		
		int value = (locnFn!= null) ? locnFn.eval(context) : -1;
		final TopologyElement vertexLoc = context.topology().vertices().get(0);
		final ContainerState state = context.state().containerStates()[context.containerId()[vertexLoc.index()]];
		final List<? extends TopologyElement> sites = context.topology().getGraphElements(type);
		TIntArrayList sites_free = new TIntArrayList();
		
		for (final TopologyElement site : sites) {
			List<game.util.graph.Step> unw = context.game().board().topology().trajectories()
					.steps(type, site.index(), type, AbsoluteDirection.UNW);
			List<game.util.graph.Step> usw = context.game().board().topology().trajectories()
					.steps(type, site.index(), type, AbsoluteDirection.USW);
			List<game.util.graph.Step> une = context.game().board().topology().trajectories()
					.steps(type, site.index(), type, AbsoluteDirection.UNE);
			List<game.util.graph.Step> use = context.game().board().topology().trajectories()
					.steps(type, site.index(), type, AbsoluteDirection.USE);
			
			boolean hasValidDirection = false;

			if (value == -1) {
			    // Original logic when value is -1
			    if (!unw.isEmpty() && state.what(unw.get(0).to().id(), type) != 0) {
			        hasValidDirection = true;
			    }
			    if (!usw.isEmpty() && state.what(usw.get(0).to().id(), type) != 0) {
			        hasValidDirection = true;
			    }
			    if (!une.isEmpty() && state.what(une.get(0).to().id(), type) != 0) {
			        hasValidDirection = true;
			    }
			    if (!use.isEmpty() && state.what(use.get(0).to().id(), type) != 0) {
			        hasValidDirection = true;
			    }
			} else if (value >= 0) {
			    // New logic when value is positive
			    if (!unw.isEmpty() && state.what(unw.get(0).to().id(), type) == value) {
			        hasValidDirection = true;
			    }
			    if (!usw.isEmpty() && state.what(usw.get(0).to().id(), type) == value) {
			        hasValidDirection = true;
			    }
			    if (!une.isEmpty() && state.what(une.get(0).to().id(), type) == value) {
			        hasValidDirection = true;
			    }
			    if (!use.isEmpty() && state.what(use.get(0).to().id(), type) == value) {
			        hasValidDirection = true;
			    }
			}

			// Add to sites_free if at least one valid direction exists
			if (hasValidDirection) {
			    sites_free.add(site.index());
			}
		}
		
		int[] free_s = sites_free.toArray();
		
		return new Region(free_s);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		// we're looking at "free" in a specific context, so never static
		return false;
	}

	@Override
	public String toString()
	{
		if (type == null)
			return "Null type in Free.";
		
		return "FreeVertex()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = SiteType.gameFlags(type);
		if (locnFn != null)
			gameFlags |= locnFn.gameFlags(game);

		return gameFlags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		if (locnFn != null)
			concepts.or(locnFn.concepts(game));
		
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (locnFn != null)
			writeEvalContext.or(locnFn.writesEvalContextRecursive());
		
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (locnFn != null)
			readEvalContext.or(locnFn.readsEvalContextRecursive());
		
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (locnFn != null)
			missingRequirement |= locnFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (locnFn != null)
			willCrash |= locnFn.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		if (locnFn != null)
			locnFn.preprocess(game);
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "support " + type.name();
	}

}
	