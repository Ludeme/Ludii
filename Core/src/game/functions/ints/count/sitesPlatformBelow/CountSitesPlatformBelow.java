package game.functions.ints.count.sitesPlatformBelow;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.iterator.To;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.types.state.GameType;
import game.util.directions.AbsoluteDirection;
import game.util.graph.Step;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.context.Context;
import other.state.container.ContainerState;
import other.topology.Topology;

/**
 * Returns the number of specific pieces on sites below a given site.
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

	/** The owner of the pieces to count on the lower platform */
	private final IntFunction whoFn;
	
	/** To simulate this kind of piece is on the pivot. (e.g. Dara) */
	private final IntFunction[] whatFn;
	
	/** The site to test. */
	private final IntFunction siteFn;

	//-------------------------------------------------------------------------

	/**
	 * @param type      The graph element type [default SiteType of the board].
	 * @param site 		The site to check [(to)].
	 * @param who  		Player id the counted items belong to
	 * @param what  	Piece id of the counted items
	 * @param whats  	Piece id's of the counted items
	 */
	public CountSitesPlatformBelow
	(
		@Opt        	final SiteType type,
		@Opt    	final IntFunction site,
		@Opt	@Or	@Name	final RoleType who,
			@Opt	@Or @Name   final IntFunction what,
			@Opt	@Or @Name 	final IntFunction[] whats
	)
	{
		this.type = type;
		siteFn = (site == null) ? To.instance() : site;
		if (whats != null)
		{
			whatFn = whats;
		}
		else if (what != null)
		{
			whatFn = new IntFunction[1];
			whatFn[0] = what;
		}
		else
		{
			whatFn = null;
		}

		whoFn = (who != null) ? RoleType.toIntFunction(who) : null;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final int site = siteFn.eval(context);
		final TIntArrayList whats  = new TIntArrayList();
		
		if (whatFn != null)
		{
			for (final IntFunction what : whatFn)
				whats.add(what.eval(context));
		}

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
			if (whatFn == null) {
				if (cs.who(step.to().id(), SiteType.Vertex) == whoFn.eval(context)) {
					count++;
				}
			}
			else {
				if (whats.contains(cs.what(step.to().id(), SiteType.Vertex))) {
					count++;
				}
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
		if (whoFn != null)
			flags |= whoFn.gameFlags(game);
		if (whatFn != null)
		{
			for (final IntFunction what : whatFn)
				flags |= what.gameFlags(game);
		}
		return flags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(siteFn.concepts(game));
		if (whoFn != null)
			concepts.or(whoFn.concepts(game));
		if (whatFn != null)
			for (final IntFunction what : whatFn)
				concepts.or(what.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(siteFn.writesEvalContextRecursive());
		if (whoFn != null)
			writeEvalContext.or(whoFn.writesEvalContextRecursive());
		if (whatFn != null)
			for (final IntFunction what : whatFn)
				writeEvalContext.or(what.writesEvalContextRecursive());
		if (siteFn != null)
			writeEvalContext.or(siteFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(siteFn.writesEvalContextRecursive());
		if (whoFn != null)
			readEvalContext.or(whoFn.writesEvalContextRecursive());
		if (whatFn != null)
			for (final IntFunction what : whatFn)
				readEvalContext.or(what.readsEvalContextRecursive());

		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		siteFn.preprocess(game);
		if (whoFn != null)
			whoFn.preprocess(game);
		if (whatFn != null)
		{
			for (final IntFunction what : whatFn)
				what.preprocess(game);
		}
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= siteFn.missingRequirement(game);
		if (whoFn != null)
			missingRequirement |= whoFn.missingRequirement(game);
		if (whatFn != null)
			for (final IntFunction what : whatFn)
				missingRequirement |= what.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= siteFn.willCrash(game);
		if (whoFn != null)
			willCrash |= whoFn.willCrash(game);
		if (whatFn != null)
			for (final IntFunction what : whatFn)
				willCrash |= what.willCrash(game);
		return willCrash;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toEnglish(final Game game) 
	{		
		String whoString = "of their";
		if (whatFn != null)
			whoString = whatFn.toString();
		if (whoFn != null)
			whoString = whoFn.toString();
		
		return "site " + siteFn.toEnglish(game) + " is counted sites bellow it bellonging to " + whoString;
	}
	
	//-------------------------------------------------------------------------
		
}