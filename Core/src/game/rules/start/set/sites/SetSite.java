package game.rules.start.set.sites;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.equipment.component.Component;
import game.functions.ints.IntFunction;
import game.functions.ints.board.Id;
import game.functions.region.RegionFunction;
import game.rules.start.Start;
import game.rules.start.StartRule;
import game.types.board.SiteType;
import game.types.play.RoleType;
import main.Constants;
import other.concept.Concept;
import other.context.Context;
import other.topology.SiteFinder;
import other.topology.TopologyElement;
import other.trial.Trial;

/**
 * Sets a site to the first piece of a player.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SetSite extends StartRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The role of the owned of the piece to set. */
	private final RoleType role;
	
	/** Which cell. */
	private final IntFunction siteId;
	
	/** Which coord. */
	private final String coord;

	/** Cell, Edge or Vertex. */
	private SiteType type;

	//-----------------Data to fill a region------------------------------------

	/** Which cells. */
	private final IntFunction[] locationIds;

	/** Which region. */
	private final RegionFunction region;

	/** Which coords. */
	private final String[] coords;

	//-------------------------------------------------------------------------

	/**
	 * @param role  The owner of the site.
	 * @param type  The graph element type [default SiteType of the board].
	 * @param loc   The location to place a piece.
	 * @param coord The coordinate of the location to place a piece.
	 */
	public SetSite
	(
                       final RoleType    role,
		@Opt 		   final SiteType    type,
		@Opt	       final IntFunction loc,
		@Opt     @Name final String      coord
	)
	{
		this.siteId = (loc == null) ? null : loc;
		this.coord = (coord == null) ? null : coord;
		this.locationIds = null;
		this.region = null;
		this.coords = null;
		this.type = type;
		this.role = role;
	}

	/**
	 * @param role   The roleType.
	 * @param type   The graph element type [default SiteType of the board].
	 * @param locs   The sites to fill.
	 * @param region The region to fill.
	 * @param coords The coordinates of the sites to fill.
	 */
	public SetSite
	(
                       final RoleType       role,
			@Opt 	   final SiteType       type,
			@Opt 	   final IntFunction[] 	locs,
			@Opt 	   final RegionFunction region,
			@Opt       final String[]       coords
	)
	{
		this.locationIds 	= (locs == null) 	? null 	: locs;
		this.region = (region == null) ? null : region;
		this.coords = (coords == null) 	? null 	: coords;
		this.coord = null;
		this.siteId = null;
		this.type = type;
		this.role = role;
	}

	//-------------------------------------------------------------------------

	@Override
	public void eval(final Context context)
	{	 
		int what = new Id(null, role).eval(context);
		
		if (role == RoleType.Neutral)
		{
			for (int i = 1; i < context.components().length; i++)
			{
				final Component component = context.components()[i];
				if (component.owner() == 0)
				{
					what = component.index();
					break;
				}
			}
		}
		else if (role == RoleType.Shared || role == RoleType.All)
		{
			for (int i = 1; i < context.components().length; i++)
			{
				final Component component = context.components()[i];
				if (component.owner() == context.game().players().size())
				{
					what = component.index();
					break;
				}
			}
			
//			if (context.game().usesUnionFindAdjacent())
//			{				
//				final int[] locs = region.eval(context).sites();
//				for (final int loc : locs)
//				{
//					UnionFindD.evalSetGT(context, loc, role, AbsoluteDirection.Adjacent);
//				}
//			}
//			else if (context.game().usesUnionFindOrthogonal())
//			{				
//				final int[] locs = region.eval(context).sites();
//				for (final int loc : locs)
//				{
//					UnionFindD.evalSetGT(context, loc, role, AbsoluteDirection.Orthogonal);
//				}
//			}
		}
		else
		{
			boolean find = false;
			for (int i = 1; i < context.components().length; i++)
			{
				final Component component = context.components()[i];
				if (component.index() == what)
				{
					find = true;
					break;
				}
			}
			if (!find)
				what = Constants.UNDEFINED;
		}

		// If we try to set something else than a player piece, the starting rule do
		// nothing.
		if (what < 1 || what >= context.components().length)
		{
			System.err.println("Warning: A piece which not exist is trying to be set in the starting rule.");
			return;
		}

		// Check if the goal is to fill a region
		if (locationIds != null || region != null || coords != null)
		{
			evalFill(context, what);
		}
		else
		{
			if (siteId == null && coord == null)
				return;

			int site = Constants.UNDEFINED;

			if (coord != null)
			{
				final TopologyElement element = SiteFinder.find(context.board(), coord, type);
				if (element == null)
					throw new RuntimeException("In the starting rules (place) the coordinate " + coord + " not found.");
				site = element.index();
			}
			else if (siteId != null)
			{
				site = siteId.eval(context);
			}

			Start.placePieces(context, site, what, 1, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
					false, type);
		}
	}

	/**
	 * To eval the place ludeme for a region/list of sites.
	 * 
	 * @param context The context of the game.
	 */
	private void evalFill(final Context context, final int what)
	{	
		// place with coords
		if (coords != null)
		{
			for (final String coordinate : coords)
			{
				final TopologyElement element = SiteFinder.find(context.board(), coordinate, type);
				if (element == null)
				{
					System.out.println("** SetSite.evalFill(): Coord " + coordinate + " not found.");
				}
				else
				{
					Start.placePieces(context, element.index(), what, 1, Constants.UNDEFINED, Constants.UNDEFINED,
							Constants.UNDEFINED,
							false, type);
				}
			}
		}
		// place with regions
		else if (region != null)
		{
			final int[] locs = region.eval(context).sites();
			for (final int loc : locs)
			{
				Start.placePieces(context, loc, what, 1, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
						false, type);
			}
		}
		// place with locs
		else if (locationIds != null)
		{
			for (final IntFunction loc : locationIds)
			{
				Start.placePieces(context, loc.eval(context), what, 1, Constants.UNDEFINED, Constants.UNDEFINED,
						Constants.UNDEFINED, false,
						type);
			}
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * @return posn
	 */
	public IntFunction posn()
	{
		return siteId;
	}

	@Override
	public int count(final Game game)
	{
		return 1;
	}

	@Override
	public int howManyPlace(final Game game)
	{
		if (region != null)
			return region.eval(new Context(game, null)).sites().length;
		if (locationIds != null)
			return locationIds.length;
		else
			return 1;
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
		long flags = 0L;

		flags |= SiteType.gameFlags(type);

		if (siteId != null)
			flags = siteId.gameFlags(game);

		if (region != null)
			flags |= region.gameFlags(game);

		return flags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));

		final int maxSiteOnBoard = (type == null)
				? game.board().topology().getGraphElements(game.board().defaultSite()).size()
				: (type.equals(SiteType.Cell)) ? game.board().topology().getGraphElements(SiteType.Cell).size()
						: (type.equals(SiteType.Vertex))
								? game.board().topology().getGraphElements(SiteType.Vertex).size()
								: game.board().topology().getGraphElements(SiteType.Edge).size();

		if (siteId != null)
		{
			concepts.or(siteId.concepts(game));
			final int site = siteId.eval(new Context(game, new Trial(game)));
			if (site < maxSiteOnBoard)
				concepts.set(Concept.PiecesPlacedOnBoard.id(), true);
			else
				concepts.set(Concept.PiecesPlacedOutsideBoard.id(), true);
		}

		if (locationIds != null)
			for (final IntFunction loc : locationIds)
			{
				concepts.or(loc.concepts(game));
				final int site = loc.eval(new Context(game, new Trial(game)));
				if (site < maxSiteOnBoard)
					concepts.set(Concept.PiecesPlacedOnBoard.id(), true);
				else
					concepts.set(Concept.PiecesPlacedOutsideBoard.id(), true);
			}

		if (region != null)
		{
			concepts.or(region.concepts(game));
			final int[] sitesRegion = region.eval(new Context(game, new Trial(game))).sites();
			for (final int site : sitesRegion)
			{
				if (site < maxSiteOnBoard)
					concepts.set(Concept.PiecesPlacedOnBoard.id(), true);
				else
					concepts.set(Concept.PiecesPlacedOutsideBoard.id(), true);
			}
		}

		if (coords != null)
			concepts.set(Concept.PiecesPlacedOnBoard.id(), true);

		concepts.or(SiteType.concepts(type));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (region != null)
			writeEvalContext.or(region.writesEvalContextRecursive());
		if (locationIds != null)
			for (final IntFunction loc : locationIds)
				writeEvalContext.or(loc.writesEvalContextRecursive());
		if (siteId != null)
			writeEvalContext.or(siteId.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (region != null)
			readEvalContext.or(region.readsEvalContextRecursive());
		if (locationIds != null)
			for (final IntFunction loc : locationIds)
				readEvalContext.or(loc.readsEvalContextRecursive());
		if (siteId != null)
			readEvalContext.or(siteId.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);

		if (siteId != null)
			siteId.preprocess(game);

		if (locationIds != null)
			for (final IntFunction locationId : locationIds)
				locationId.preprocess(game);

		if (region != null)
			region.preprocess(game);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final String str = "(set)";

		return str;
	}
}
