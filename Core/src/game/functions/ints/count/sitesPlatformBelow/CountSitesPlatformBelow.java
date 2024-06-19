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
import game.util.directions.AbsoluteDirection;
import game.util.graph.Step;
import main.Constants;
import other.context.Context;
import other.context.EvalContextData;
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
	 * @param who  		Player id the counted items belong to
	 * @param site 		The site to check [(to)].
	 */
	public CountSitesPlatformBelow
	(
		@Opt 		final IntFunction site,
					final RoleType who
	)
	{
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
		
		final List<game.util.graph.Step> steps = topology.trajectories().steps(SiteType.Vertex, site, SiteType.Vertex,
				AbsoluteDirection.Downward);
		
		int count = 0;
		
//		System.out.println("--------------");

		for (final Step step : steps) {
//			System.out.println("Field: " + step.to().id() + ", and id: " + cs.what(step.to().id(), SiteType.Vertex));
//			System.out.println("RoleType: " + whoFn.eval(context));
			if (cs.who(step.to().id(), SiteType.Vertex) == whoFn.eval(context)) {
//				System.out.println();
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
		return "Groups()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = SiteType.gameFlags(type);
		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		return writeEvalContext;
	}
	
	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.set(EvalContextData.To.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		return willCrash;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toEnglish(final Game game) 
	{
		String conditionString = "";
		
		return "the number of " + type.name() + " groups" + conditionString;
	}
	
	//-------------------------------------------------------------------------
		
}