package game.functions.region.sites.largePiece;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.equipment.component.Component;
import game.functions.ints.IntFunction;
import game.functions.region.BaseRegionFunction;
import game.types.board.SiteType;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import other.context.Context;
import other.state.container.ContainerState;

/**
 * Is used to return all the sites occupied by a large piece.
 *
 * @author Eric.Piette
 */
@Hide
public final class SitesLargePiece extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	/** The site to look */
	private final IntFunction at;

	/**
	 * @param type The type of the graph element [DefaultSite].
	 * @param at   The site to look.
	 */
	public SitesLargePiece
	(
		      @Opt  final SiteType    type, 
		@Name       final IntFunction at
	)
	{
		this.at = at;
		this.type = type;
	}

	// -------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		final int site = at.eval(context);
		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();
		final TIntArrayList sitesOccupied = new TIntArrayList();

		// If not on the main board.
		if (site >= context.board().topology().getGraphElements(realType).size())
			return new Region(sitesOccupied.toArray());

		final ContainerState cs = context.containerState(0);
		final int what = cs.what(site, realType);

		// If no piece.
		if (what == 0)
			return new Region(sitesOccupied.toArray());

		final Component piece = context.components()[what];

		// If not large piece
		if (!piece.isLargePiece())
		{
			sitesOccupied.add(site);
			return new Region(sitesOccupied.toArray());
		}

		final int localState = cs.state(site, type);
		final TIntArrayList locs = piece.locs(context, site, localState, context.topology());
		for (int j = 0; j < locs.size(); j++)
			if (!sitesOccupied.contains(locs.get(j)))
				sitesOccupied.add(locs.get(j));

		return new Region(sitesOccupied.toArray());
	}

	// -------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = 0;

		gameFlags |= SiteType.gameFlags(type);

		gameFlags |= at.gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.or(at.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(at.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(at.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		at.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (!game.hasLargePiece())
		{
			game.addRequirementToReport(
					"The ludeme (sites LargePiece ...) is used but the equipment has no large pieces.");
			missingRequirement = true;
		}
		missingRequirement |= at.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= at.willCrash(game);
		return willCrash;
	}
}
