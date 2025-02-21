package game.functions.booleans.is.component;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.graph.Step;
import gnu.trove.list.array.TIntArrayList;
import other.context.Context;
import other.state.container.ContainerState;
import java.util.ArrayList;

/**
 * Test if a specific region will still have Freedom (region must be visibly connected to at least one 
 * empty board hole by a chain of same colored touching balls) if a piece gets added to locn.
 * 
 * @author Cedric.Antoine
 */
@Hide
public final class IsFreedom extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** Cell/Edge/Vertex. */
	private SiteType type;

	/** Which region. */
	protected final RegionFunction region;
	
	/** location to look around. */
	private IntFunction locnFn;

	//-------------------------------------------------------------------------

	/**
	 * @param type    	The graph element type.
	 * @param in	  	The region to check for freedom.
	 * @param toPlace   The location where a piece will be added to (to ignore for freedom check).
	 */
	public IsFreedom
	(
		@Opt 		final SiteType       type,
					final RegionFunction in,
		@Opt		final IntFunction    toPlace
	)
	{
		
		if (toPlace != null)
			locnFn = toPlace;
		else
			locnFn = null;
				
		this.region = in;
		
		this.type = type;
	}
	
	@Override
	public final boolean eval(final Context context)
	{
		int pid = -1;
		if (locnFn != null)
			pid   = locnFn.eval(context);
		final TIntArrayList listPivots = new TIntArrayList(region.eval(context).sites());
		
		for (int i = 0; i < listPivots.size(); i++)
		{
			final int loc = listPivots.getQuick(i);
			
			List<game.util.graph.Step> stepsList = new ArrayList<>();
			stepsList.addAll(context.game().board().topology().trajectories()
					.steps(type, loc, type, AbsoluteDirection.N));
			stepsList.addAll(context.game().board().topology().trajectories()
					.steps(type, loc, type, AbsoluteDirection.S));
			stepsList.addAll(context.game().board().topology().trajectories()
					.steps(type, loc, type, AbsoluteDirection.W));
			stepsList.addAll(context.game().board().topology().trajectories()
					.steps(type, loc, type, AbsoluteDirection.E));

			for (final Step step : stepsList)
			{
				int neigh = step.to().id();
				final int contId = context.containerId()[neigh];
				final ContainerState state = context.state().containerStates()[contId];
				final int what = state.what(neigh, type);
				final other.topology.Vertex v = context.topology().vertices().get(neigh);
				final int lay = v.layer();
				if (neigh != pid && what == 0 && lay == 0) {
					return true;
				}				
			}
		}

		return false;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "IsWithin(" + locnFn + "," + region + ")";
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
		long gameFlags =  region.gameFlags(game);
		
		if (locnFn != null)
			gameFlags |= locnFn.gameFlags(game);

		gameFlags |= SiteType.gameFlags(type);

		return gameFlags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		if (locnFn != null)
			concepts.or(locnFn.concepts(game));
		concepts.or(region.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (locnFn != null)
			writeEvalContext.or(locnFn.writesEvalContextRecursive());
		writeEvalContext.or(region.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (locnFn != null)
			readEvalContext.or(locnFn.readsEvalContextRecursive());
		readEvalContext.or(region.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		if (locnFn != null)
			locnFn.preprocess(game);
		region.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (locnFn != null)
			missingRequirement |= locnFn.missingRequirement(game);
		missingRequirement |= region.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (locnFn != null)
			willCrash |= locnFn.willCrash(game);
		willCrash |= region.willCrash(game);
		return willCrash;
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return locnFn.toEnglish(game)+ " is freedom in " + region.toEnglish(game);

	}
}
