package game.rules.start.set.sites;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.rules.start.StartRule;
import game.types.board.SiteType;
import other.IntArrayFromRegion;
import other.action.graph.ActionSetPhase;
import other.context.Context;
import other.move.Move;

/**
 * Sets the phase of a graph element.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SetPhase extends StartRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The region to set the pahse. */
	private final IntArrayFromRegion region;

	/** The phase. */
	private final IntFunction phaseFn;

	/** The type of the graph element. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param type   The type of the graph element.
	 * @param site   The site to set.
	 * @param region The region to set.
	 * @param phase  The new phase.
	 */
	public SetPhase
	(
			     final IntFunction    phase, 
			@Opt final SiteType       type, 
		@Or      final IntFunction    site,
		@Or      final RegionFunction region
	)
	{
		this.type = type;
		this.region = new IntArrayFromRegion(site, region);
		this.phaseFn = phase;
	}

	//-------------------------------------------------------------------------

	@Override
	public void eval(final Context context)
	{
		final int[] locs = region.eval(context);
		for (final int loc : locs)
		{
			final ActionSetPhase actionSetPhase = new ActionSetPhase(type, loc, phaseFn.eval(context));
			actionSetPhase.apply(context, true);
			context.trial().addMove(new Move(actionSetPhase));
			context.trial().addInitPlacement();
		}
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
		long gameFlags = SiteType.gameFlags(type);
		gameFlags |= phaseFn.gameFlags(game);
		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		if (region != null)
			concepts.or(region.concepts(game));
		concepts.or(SiteType.concepts(type));
		concepts.or(phaseFn.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (region != null)
			writeEvalContext.or(region.writesEvalContextRecursive());
		writeEvalContext.or(phaseFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (region != null)
			readEvalContext.or(region.readsEvalContextRecursive());
		readEvalContext.or(phaseFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		phaseFn.preprocess(game);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final String str = "(SetPhase)";
		return str;
	}
}
