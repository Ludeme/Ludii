package game.rules.start.place.item;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.equipment.component.Component;
import game.equipment.container.Container;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.rules.start.Start;
import game.rules.start.StartRule;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.types.state.GameType;
import game.util.equipment.Region;
import main.Constants;
import other.action.BaseAction;
import other.action.puzzle.ActionSet;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
import other.state.container.ContainerState;
import other.topology.SiteFinder;
import other.topology.TopologyElement;
import other.translation.LanguageUtils;
import other.trial.Trial;

/**
 * Places a piece at a particular site or to a region.
 * 
 * @author Eric.Piette
 */
@Hide
public final class PlaceItem extends StartRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which item to add. */
	private final String item;
	
	/** Which container. */
	private final String container;

	/** Which cell. */
	private final IntFunction siteId;
	
	/** Which coord. */
	private final String coord;

	/** Number to add. */
	private final IntFunction countFn;

	/** State of the item. */
	private final IntFunction stateFn;

	/** State of the item. */
	private final IntFunction rotationFn;
	
	/** Piece value of the item. */
	private final IntFunction valueFn;

	/** Cell, Edge or Vertex. */
	private SiteType type;

	//-----------------Data to fill a region------------------------------------

	/** Which cells. */
	private final IntFunction[] locationIds;

	/** Which region. */
	private final RegionFunction region;

	/** Which coords. */
	private final String[] coords;

	/** Numbers to add for each position. */
	private final IntFunction[] countsFn;

	//-------------------------------------------------------------------------

	/**
	 * @param item      The name of the item.
	 * @param container The name of the container.
	 * @param type      The graph element type [default SiteType of the board].
	 * @param loc       The location to place a piece.
	 * @param coord     The coordinate of the location to place a piece.
	 * @param count     The number of the same piece to place [1].
	 * @param state     The local state value of the piece to place [Undefined].
	 * @param rotation  The rotation value of the piece to place [Undefined].
	 * @param value     The piece value to place [Undefined].
	 */
	public PlaceItem
	(
				   final String      item,
		@Opt       final String      container,
		@Opt 	   final SiteType    type,
		@Opt	   final IntFunction loc,
		@Opt @Name final String      coord,
		@Opt @Name final IntFunction count,
		@Opt @Name final IntFunction state,
		@Opt @Name final IntFunction rotation,
		@Opt @Name final IntFunction value
	)
	{
		this.item 	= (item == null) 	? null 	: item;
		this.container = (container == null) ? null : container;
		siteId = (loc == null) ? null : loc;
		this.coord = (coord == null) ? null : coord;
		countFn = (count == null) ? new IntConstant(1) : count;
		stateFn = (state == null) ? new IntConstant(Constants.OFF) : state;
		rotationFn = (rotation == null) ? new IntConstant(Constants.OFF) : rotation;
		valueFn = (value == null) ? new IntConstant(Constants.OFF) : value;

		locationIds = null;
		region = null;
		coords = null;
		countsFn = null;
		this.type = type;
	}

	/**
	 * @param item     The item to place.
	 * @param type     The graph element type [default SiteType of the board].
	 * @param locs     The sites to fill.
	 * @param region   The region to fill.
	 * @param coords   The coordinates of the sites to fill.
	 * @param counts   The number of pieces on the state.
	 * @param state    The local state value to put on each site.
	 * @param rotation The rotation value to put on each site.
	 * @param value    The piece value to place [Undefined].
	 */
	public PlaceItem
	(
					   final String 	    item,
			@Opt 	   final SiteType       type,
			@Opt 	   final IntFunction[] 	locs,
			@Opt 	   final RegionFunction region,
			@Opt 	   final String[] 	    coords,
			@Opt @Name final IntFunction[]  counts,
			@Opt @Name final IntFunction 	state,
			@Opt @Name final IntFunction 	rotation,
			@Opt @Name final IntFunction    value
	)
	{
		this.item 	= (item == null) 	? null 	: item;
		container = null;
		locationIds 	= (locs == null) 	? null 	: locs;
		this.region = (region == null) ? null : region;
		this.coords = (coords == null) 	? null 	: coords;
		countFn = (counts == null) ? new IntConstant(1) : counts[0];
		
		if (counts == null)
		{
			countsFn = new IntFunction[0];
		}
		else
		{
			countsFn = new IntFunction[counts.length];
			for (int i = 0; i < counts.length; i++)
				countsFn[i] = counts[i];
		}

		stateFn = (state == null) ? new IntConstant(Constants.OFF) : state;
		rotationFn = (rotation == null) ? new IntConstant(Constants.OFF) : rotation;
		valueFn = (value == null) ? new IntConstant(Constants.OFF) : value;

		coord = null;
		siteId = null;
		this.type = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public void eval(final Context context)
	{	 
		// Check if the goal is to fill a region
		if (locationIds != null || region != null || coords != null || countsFn != null)
		{
			evalFill(context);
		}
		else if (context.game().isDeductionPuzzle())
		{
			evalPuzzle(context);
		}
		else
		{
			final int count = countFn.eval(context);
			final int state = stateFn.eval(context);
			final int rotation = rotationFn.eval(context);
			final int value = valueFn.eval(context);

			final Component testComponent = context.game().getComponent(item);
			if 
			(
				stringWitoutNumber(item) && container != null && container.equals("Hand")
				&& 
				(testComponent == null || !testComponent.role().equals(RoleType.Shared))
			)
			{
//				System.out.println("Placing items for player in hand, count is " + count + "...");
				
				for (int pid = 1; pid <= context.game().players().count(); pid++)
				{
					final String itemPlayer = item + pid;
					final String handPlayer = container + pid;

					final Component component = context.game().getComponent(itemPlayer);
					if (component == null)
						throw new RuntimeException
						(
							"In the starting rules (place) the component " + itemPlayer + " is not defined (A)."
						);

					final Container c = context.game().mapContainer().get(handPlayer);
					final ContainerState cs = context.containerState(c.index());
					
					int site = context.game().equipment().sitesFrom()[c.index()];
					while (!cs.isEmpty(site, type))
						site++;
					
					Start.placePieces(context, site, component.index(), count, state, rotation, value, false, type);
				}
				return;
			}

			final Component component = context.game().getComponent(item);
			if (component == null)
				throw new RuntimeException
				(
					"In the starting rules (place) the component " + item + " is not defined (B)."
				);

			final int what = component.index();

			if (container != null)
			{
				// Place on a specific container
				// Place with coords
				final Container c = context.game().mapContainer().get(container);
				final int siteFrom = context.game().equipment().sitesFrom()[c.index()];
				final Component comp = context.game().equipment().components()[what];

				// SPECIAL STATE INIT FOR DICE
				if (comp.isDie())
				{
					for (int pos = siteFrom; pos < siteFrom + c.numSites(); pos++)
					{
						if (context.state().containerStates()[c.index()].what(pos, type) == 0)
						{
							Start.placePieces(context, pos, what, count, state, rotation, value, false, type);
							break;
						}
					}
				}
				else if (container.contains("Hand"))
				{ 
					Start.placePieces(context, siteFrom, c.index(), count, state, rotation, value, false, type);
					return;
				}
				else
				{
					Start.placePieces(context, siteId.eval(context) + siteFrom, what, count, state, rotation, value,
							false, type);
				}
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
						throw new RuntimeException(
								"In the starting rules (place) the coordinate " + coord + " not found.");
					site = element.index();
				}
				else if (siteId != null)
				{
					site = siteId.eval(context);
				}

				Start.placePieces(context, site, what, count, state, rotation, value, false, type);
			}
		}
	}

	/**
	 * To eval the place ludeme for a region/list of sites.
	 * 
	 * @param context The context of the game.
	 */
	private void evalFill(final Context context)
	{	
		final Component component = context.game().getComponent(item);
		if (component == null)
			throw new RuntimeException("In the starting rules (place) the component " + item + " is not defined (C).");

		final int what = component.index();
		final int count = countFn.eval(context);
		final int state = stateFn.eval(context);
		final int rotation = rotationFn.eval(context);
		final int value = valueFn.eval(context);

		// place on a specific container
		if (container != null)
		{
			final Container c = context.game().mapContainer().get(container);
			final int siteFrom = context.game().equipment().sitesFrom()[c.index()];

			if (region != null)
			{
				final int[] locs = region.eval(context).sites();
				for (final int loc : locs)
				{
					Start.placePieces(context, loc + siteFrom, what, count, state, rotation, value, false, type);
				}
			}
			else if (locationIds != null)
			{
				for (final IntFunction loc : locationIds)
				{
					Start.placePieces(context, loc.eval(context) + siteFrom, what, count, state, rotation, value, false, type);
				}
			}
			else
			{
				for (int pos = siteFrom; pos < siteFrom + c.numSites(); pos++)
				{
					if (context.state().containerStates()[c.index()].what(pos, type) == 0)
					{
						Start.placePieces(context, pos, what, count, state, rotation, value, false, type);
						break;

					}
				}
			}
		}
		else
		{
			// place with coords
			if (coords != null)
			{
				for (final String coordinate : coords)
				{
					final TopologyElement element = SiteFinder.find(context.board(), coordinate, type);
					if (element == null)
						System.out.println("** Coord " + coordinate + " not found.");
					else
						Start.placePieces(context, element.index(), what, count, state, rotation, value, false, type);
				}
			}
			// place with regions
			else if (region != null)
			{
				final Region regionEval = region.eval(context);
				if(regionEval != null)
				{
					final int[] locs = regionEval.sites();
					for (final int loc : locs)
						Start.placePieces(context, loc, what, count, state, rotation, value, false, type);
				}
			}
			// place with locs
			else if (locationIds != null)
				for (final IntFunction loc : locationIds)
					Start.placePieces(context, loc.eval(context), what, count, state, rotation, value, false, type);
		}
	}

	/**
	 * @param str
	 * @return True if str does not have any number.
	 */
	private static boolean stringWitoutNumber(final String str)
	{
		for (int i = 0; i < str.length(); i++)
			if (str.charAt(i) >= '0' && str.charAt(i) <= '9')
				return false;
		return true;
	}

	private void evalPuzzle(final Context context)
	{
		final Component component = context.game().getComponent(item);
		if (component == null)
			throw new RuntimeException(
					"In the starting rules (place) the component " + item + " is not defined.");
		
		final int what = component.index();
		final BaseAction actionAtomic = new ActionSet(SiteType.Cell, siteId.eval(context), what);
		
		actionAtomic.apply(context, true);
		context.trial().addMove(new Move(actionAtomic));
		context.trial().addInitPlacement();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return item
	 */
	public String item()
	{
		return item;
	}

	/**
	 * @return posn
	 */
	public IntFunction posn()
	{
		return siteId;
	}

	/**
	 * @return containerName
	 */
	public String container()
	{
		return container;
	}

	@Override
	public int count(final Game game)
	{
		return countFn.eval(new Context(game, new Trial(game)));
	}

	@Override
	public int state(final Game game)
	{
		return stateFn.eval(new Context(game, new Trial(game)));
	}

	@Override
	public int howManyPlace(final Game game)
	{
		if (region != null)
		{
			// region may not yet have been preprocessed, so do that first
//			if (region.isStatic())
//			{
//				region.preprocess(game);
//				return region.eval(new Context(game, null)).sites().length;
//			}
//			else
				return game.board().numSites();
		}
		
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
			flags |= siteId.gameFlags(game);

		if (stateFn.eval(new Context(game, new Trial(game))) != Constants.UNDEFINED)
			flags |= GameType.SiteState;

		if (rotationFn.eval(new Context(game, new Trial(game))) != Constants.UNDEFINED)
			flags |= GameType.Rotation;
		
		if (valueFn.eval(new Context(game, new Trial(game))) != Constants.UNDEFINED)
			flags |= GameType.Value;

		if (countFn.eval(new Context(game, new Trial(game))) > 1)
			flags |= GameType.Count;

		if (region != null)
			flags |= region.gameFlags(game);

		flags |= countFn.gameFlags(game);
		flags |= rotationFn.gameFlags(game);
		flags |= stateFn.gameFlags(game);
		flags |= valueFn.gameFlags(game);

		if (countsFn != null)
			for (final IntFunction function : countsFn)
				flags |= function.gameFlags(game);

		return flags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));

		if (countFn.eval(new Context(game, new Trial(game))) > 1)
			concepts.set(Concept.PieceCount.id(), true);

		if (valueFn.eval(new Context(game, new Trial(game))) != Constants.UNDEFINED)
			concepts.set(Concept.PieceValue.id(), true);

		final int maxSiteOnBoard = (type == null)
				? game.board().topology().getGraphElements(game.board().defaultSite()).size()
				: (type.equals(SiteType.Cell)) ? game.board().topology().getGraphElements(SiteType.Cell).size()
						: (type.equals(SiteType.Vertex))
								? game.board().topology().getGraphElements(SiteType.Vertex).size()
								: game.board().topology().getGraphElements(SiteType.Edge).size();

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
			final Region regionEval = region.eval(new Context(game, new Trial(game)));
			if(regionEval != null)
			{
				final int[] sitesRegion = regionEval.sites();
				for (final int site : sitesRegion)
				{
					if (site < maxSiteOnBoard)
						concepts.set(Concept.PiecesPlacedOnBoard.id(), true);
					else
						concepts.set(Concept.PiecesPlacedOutsideBoard.id(), true);
				}
			}
		}

		if (siteId != null)
		{
			concepts.or(siteId.concepts(game));
			final int site = siteId.eval(new Context(game, new Trial(game)));
			if (site < maxSiteOnBoard)
				concepts.set(Concept.PiecesPlacedOnBoard.id(), true);
			else
				concepts.set(Concept.PiecesPlacedOutsideBoard.id(), true);
		}

		if (coords != null)
			concepts.set(Concept.PiecesPlacedOnBoard.id(), true);

		if (container != null)
			if (container.contains("Hand"))
				concepts.set(Concept.PiecesPlacedOutsideBoard.id(), true);

		if (region != null)
			concepts.or(region.concepts(game));

		if (stateFn.eval(new Context(game, new Trial(game))) > Constants.UNDEFINED)
			concepts.set(Concept.SiteState.id(), true);

		if (rotationFn.eval(new Context(game, new Trial(game))) > Constants.UNDEFINED)
		{
			concepts.set(Concept.PieceRotation.id(), true);
			concepts.set(Concept.SetRotation.id(), true);
		}

		concepts.or(countFn.concepts(game));
		concepts.or(rotationFn.concepts(game));
		concepts.or(stateFn.concepts(game));
		concepts.or(valueFn.concepts(game));

		if (countsFn != null)
			for (final IntFunction function : countsFn)
				concepts.or(function.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (locationIds != null)
			for (final IntFunction loc : locationIds)
				writeEvalContext.or(loc.writesEvalContextRecursive());

		if (region != null)
			writeEvalContext.or(region.writesEvalContextRecursive());

		if (siteId != null)
			writeEvalContext.or(siteId.writesEvalContextRecursive());

		writeEvalContext.or(countFn.writesEvalContextRecursive());
		writeEvalContext.or(rotationFn.writesEvalContextRecursive());
		writeEvalContext.or(stateFn.writesEvalContextRecursive());
		writeEvalContext.or(valueFn.writesEvalContextRecursive());

		if (countsFn != null)
			for (final IntFunction function : countsFn)
				writeEvalContext.or(function.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (locationIds != null)
			for (final IntFunction loc : locationIds)
				readEvalContext.or(loc.readsEvalContextRecursive());

		if (region != null)
			readEvalContext.or(region.readsEvalContextRecursive());

		if (siteId != null)
			readEvalContext.or(siteId.readsEvalContextRecursive());

		readEvalContext.or(countFn.readsEvalContextRecursive());
		readEvalContext.or(rotationFn.readsEvalContextRecursive());
		readEvalContext.or(stateFn.readsEvalContextRecursive());
		readEvalContext.or(valueFn.readsEvalContextRecursive());

		if (countsFn != null)
			for (final IntFunction function : countsFn)
				readEvalContext.or(function.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		// Some tests about the expected items of that ludeme.
		if (item != null)
		{
			boolean found = false;
			for (int i = 1; i < game.equipment().components().length; i++)
			{
				final String nameComponent = game.equipment().components()[i].name();
				if (nameComponent.contains(item))
				{
					found = true;
					break;
				}
			}

			if (!found)
			{
				throw new RuntimeException("Place: The component " + item
						+ " is expected but the corresponding component is not defined in the equipment.");
			}
		}

		if (container != null)
		{
			boolean found = false;
			for (int i = 1; i < game.equipment().containers().length; i++)
			{
				final String nameContainer = game.equipment().containers()[i].name();
				if (nameContainer.contains(container))
				{
					found = true;
					break;
				}
			}

			if (!found)
			{
				throw new RuntimeException("Place: The container " + container
						+ " is expected but the corresponding container is not defined in the equipment.");
			}
		}

		type = SiteType.use(type, game);

		if (siteId != null)
			siteId.preprocess(game);

		if (locationIds != null)
			for (final IntFunction locationId : locationIds)
				locationId.preprocess(game);

		if (region != null)
			region.preprocess(game);

		countFn.preprocess(game);
		rotationFn.preprocess(game);
		stateFn.preprocess(game);
		valueFn.preprocess(game);

		if (countsFn != null)
			for (final IntFunction function : countsFn)
				function.preprocess(game);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		String str = "(place " + item;

		if(container != null)
			str +=  " on cont: " + container;

		if(siteId != null)
			str +=  " at: " + siteId;

		str += " count: " + countFn;
		str += " state: " + stateFn;
		
		str+=")";

		return str;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		String text = "";
		
		final String[] splitPieceName = LanguageUtils.SplitPieceName(item);
		final String pieceName = splitPieceName[0];
		final int piecePlayer = Integer.parseInt(splitPieceName[1]);
		
		String pieceText = pieceName;
		if(piecePlayer != -1)
			pieceText += " for player " + LanguageUtils.NumberAsText(piecePlayer);
		
		if(coord != null) 
		{
			text += "Place a " + pieceText + " on site " + coord + ".";
		} 
		else if(coords != null && coords.length > 0) 
		{
			final int count = coords.length;
			
			text += "Place a " + pieceText + " on site" + (count == 1 ? " " : "s: ");
			for (int i = 0; i < count; i++) 
			{
				if(i == count - 1)
					text += " and ";
				else if(i > 0)
					text += ", ";
				
				text += coords[i];
			}
			
			text += ".";
		} 
		else if (region != null) 
		{
			text += "Place a " + pieceText + " at " + region.toEnglish(game) + ".";
		}
		
		return text;
	}
	
	//-------------------------------------------------------------------------

}
