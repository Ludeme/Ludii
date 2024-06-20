package game.functions.ints.count.sitesPlatformBelow;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.iterator.To;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.types.state.GameType;
import game.util.directions.AbsoluteDirection;
import game.util.graph.Step;
import main.Constants;
import other.context.Context;
import other.state.container.ContainerState;
import other.topology.Topology;

/**
 * Returns the number of groups.
 * 
 * @author Eric.Piette & Cedric.Antoine
 */
@Hide
public final class CountSitesPlatformBelow extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** The graph element type. */
	private SiteType type;

	/** The owner of the pieces to make a line */
	private final IntFunction whoFn;
	
	/** The site to test. */
	private final IntFunction siteFn;

	//-------------------------------------------------------------------------

	/**
	 * @param type       		The graph element type [default SiteType of the board].
	 * @param site 		The site to check [(to)].
	 * @param who  		Player id the counted items belong to
	 */
	public CountSitesPlatformBelow
	(
		@Opt        final SiteType type,
		@Opt 		final IntFunction site,
					final RoleType who
	)
	{
		this.type = type;
		siteFn = (site == null) ? To.instance() : site;
		whoFn = RoleType.toIntFunction(who);
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final int site = siteFn.eval(context);

		if (site == Constants.OFF && site >= context.topology().vertices().size())
			return -1;

		final other.topology.Vertex v = context.topology().vertices().get(site);
		final Topology topology = context.topology();

		if (v.layer() == 0)
			return 0;
		
		final ContainerState cs = context.containerState(context.containerId()[site]);
		
		final List<game.util.graph.Step> steps = topology.trajectories().steps(type, site, type,
				AbsoluteDirection.Downward);
		
		int count = 0;

		for (final Step step : steps) {
			if (cs.who(step.to().id(), SiteType.Vertex) == whoFn.eval(context)) {
				count++;
			}
		}

		return count;
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
		return "SitesPlatformBelow()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flags = siteFn.gameFlags(game);
		if (type != null && (type.equals(SiteType.Edge) || type.equals(SiteType.Vertex)))
			flags |= GameType.Graph;
		flags |= whoFn.gameFlags(game);
		return flags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(siteFn.concepts(game));
		concepts.or(whoFn.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(siteFn.writesEvalContextRecursive());
		writeEvalContext.or(whoFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(siteFn.writesEvalContextRecursive());
		readEvalContext.or(whoFn.writesEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		siteFn.preprocess(game);
		whoFn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= siteFn.missingRequirement(game);
		missingRequirement |= whoFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= siteFn.willCrash(game);
		willCrash |= whoFn.willCrash(game);
		return willCrash;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toEnglish(final Game game) 
	{		
		return "site " + siteFn.toEnglish(game) + " is counted sites bellow it bellonging to " + whoFn.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------
		
}