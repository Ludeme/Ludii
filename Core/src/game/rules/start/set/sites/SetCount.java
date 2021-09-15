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
import game.types.state.GameType;
import main.StringRoutines;
import other.IntArrayFromRegion;
import other.action.BaseAction;
import other.action.state.ActionSetCount;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;

/**
 * Sets the count at a site or a region.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SetCount extends StartRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The region to set the count. */
	private final IntArrayFromRegion region;

	/** The site to modify the count. */
	private final IntFunction countFn;

	/** Add on Cell/Edge/Vertex. */
	protected SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param count  The value of the count.
	 * @param type   The graph element type [default SiteType of the board].
	 * @param site   The site to modify the count.
	 * @param region The region to modify the count.
	 */
	public SetCount
	(
			     final IntFunction    count,
			@Opt final SiteType       type,
		@Or      final IntFunction    site,
		@Or      final RegionFunction region
	)

	{

		this.region = new IntArrayFromRegion(site, region);
		countFn  = count;
		this.type = type;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void eval(final Context context)
	{
		if (context.components().length == 1)
		{
			System.err.println(
					"Start Rule (set Count ...): At least a piece has to be defined to set the count of a site");
			return;
		}

		final int what = context.components()[context.components().length - 1].index();

		final int[] locs = region.eval(context);
		for (final int loc : locs)
		{
			final BaseAction actionAtomic = new ActionSetCount(type, loc, what, countFn.eval(context));
			actionAtomic.apply(context, true);
			context.trial().addMove(new Move(actionAtomic));
			context.trial().addInitPlacement();
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return region.isStatic() && countFn.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = GameType.Count;

		gameFlags |= SiteType.gameFlags(type);
		
		gameFlags |= region.gameFlags(game);
		gameFlags |= countFn.gameFlags(game);

		return gameFlags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();

		final int maxSiteOnBoard = (type == null)
				? game.board().topology().getGraphElements(game.board().defaultSite()).size()
				: (type.equals(SiteType.Cell)) ? game.board().topology().getGraphElements(SiteType.Cell).size()
						: (type.equals(SiteType.Vertex))
								? game.board().topology().getGraphElements(SiteType.Vertex).size()
								: game.board().topology().getGraphElements(SiteType.Edge).size();

		if (region != null)
		{
			concepts.or(region.concepts(game));
			final int[] sitesRegion = region.eval(new Context(game, new Trial(game)));
			for (final int site : sitesRegion)
			{
				if (site < maxSiteOnBoard)
					concepts.set(Concept.PiecesPlacedOnBoard.id(), true);
				else
					concepts.set(Concept.PiecesPlacedOutsideBoard.id(), true);
			}
		}

		concepts.or(SiteType.concepts(type));
		concepts.set(Concept.PieceCount.id(), true);
		concepts.or(countFn.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (region != null)
			writeEvalContext.or(region.writesEvalContextRecursive());

		writeEvalContext.or(countFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (region != null)
			readEvalContext.or(region.readsEvalContextRecursive());

		readEvalContext.or(countFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		region.preprocess(game);
		countFn.preprocess(game);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString() 
	{
		final String str = "(set " + region + " " + countFn + ")";
		return str;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public int howManyPlace(final Game game)
	{
		// region may not yet have been preprocessed, so do that first
		region.preprocess(game);
		return region.eval(new Context(game, null)).length;
	}
	
	@Override
	public int count(final Game game)
	{
		return countFn.eval(new Context(game, new Trial(game)));
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "set the count of the " + type.name().toLowerCase() + StringRoutines.getPlural(type.name()) + " in " + region.toEnglish(game) + " to " + countFn.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------
	
}
