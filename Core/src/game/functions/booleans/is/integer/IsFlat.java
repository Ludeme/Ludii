package game.functions.booleans.is.integer;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.iterator.To;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.graph.Step;
import main.Constants;
import other.context.Context;
import other.state.container.ContainerState;
import other.topology.Topology;

/**
 * Ensures that in a 3D board, all the pieces in the bottom layer have to be
 * placed so that they do not fall.
 * 
 * @author Eric.Piette
 * @remarks This is used, for example, in almost all Shibumi games.
 */
@Hide
public final class IsFlat extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The site to test. */
	private final IntFunction siteFn;

	//-------------------------------------------------------------------------

	/**
	 * @param site The site to check [(to)].
	 */
	public IsFlat(@Opt final IntFunction site)
	{
		siteFn = (site == null) ? To.instance() : site;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		final int site = siteFn.eval(context);

		if (site == Constants.OFF && site >= context.topology().vertices().size())
			return false;

		final other.topology.Vertex v = context.topology().vertices().get(site);
		final Topology topology = context.topology();

		if (v.layer() == 0)
			return true;
		
		final ContainerState cs = context.containerState(context.containerId()[site]);
		
		final List<game.util.graph.Step> steps = topology.trajectories().steps(SiteType.Vertex, site, SiteType.Vertex,
				AbsoluteDirection.Downward);

		for (final Step step : steps)
			if (cs.what(step.to().id(), SiteType.Vertex) == 0)
				return false;

		return true;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "IsFlat()";
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
		return siteFn.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		return siteFn.concepts(game);
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(siteFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(siteFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		siteFn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= siteFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= siteFn.willCrash(game);
		return willCrash;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "site " + siteFn.toEnglish(game) + " is flat";
	}
	
	//-------------------------------------------------------------------------
		
}
