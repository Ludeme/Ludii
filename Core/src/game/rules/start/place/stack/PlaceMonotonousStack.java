package game.rules.start.place.stack;

import java.util.Arrays;
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
import game.types.state.GameType;
import main.Constants;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Places a stack with the same pieces on all the stack.
 * 
 * @author Eric.Piette
 */
@Hide
public final class PlaceMonotonousStack extends StartRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which stack to add. */
	protected final String[] items;

	/** Which container. */
	protected final String container;

	/** Number to add. */
	protected final IntFunction countFn;

	/** State of the item. */
	protected final IntFunction stateFn;

	/** State of the item. */
	protected final IntFunction rotationFn;

	/** Piece value of the item. */
	private final IntFunction valueFn;

	/** Cell, Edge or Vertex. */
	private SiteType type;

	//-----------------Data to fill a region------------------------------------

	/** Which cells. */
	protected final IntFunction[] locationIds;

	/** Which region. */
	protected final RegionFunction region;

	/** Which coords. */
	protected final String[] coords;

	/** Numbers to add for each position. */
	protected final IntFunction[] countsFn;

	//-------------------------------------------------------------------------

	/**
	 * @param item     The item to place.
	 * @param type     The graph element type [default SiteType of the board].
	 * @param locs     The sites to fill.
	 * @param region   The region to fill.
	 * @param coords   The coordinates of the sites to fill.
	 * @param count    The number of pieces on the stack to place.
	 * @param counts   The number of each piece on the stack to place.
	 * @param state    The local state value to put on each site.
	 * @param rotation The rotation value to put on each site.
	 * @param value    The piece value to place [Undefined].
	 */
	public PlaceMonotonousStack
	(
					   final String 	    item,
			@Opt 	   final SiteType       type,
			@Opt 	   final IntFunction[] 	locs,
			@Opt 	   final RegionFunction region,
			@Opt 	   final String[] 	    coords,
			@Opt @Name final IntFunction    count,
			@Opt @Name final IntFunction[]  counts,
			@Opt @Name final IntFunction 	state,
			@Opt @Name final IntFunction 	rotation,
			@Opt @Name final IntFunction    value
	)
	{
		items = new String[]
		{ item };
		container = null;
		locationIds 	= (locs == null) 	? null 	: locs;
		this.region = (region == null) ? null : region;
		this.coords = (coords == null) 	? null 	: coords;
		countFn = (counts == null) ? ((count != null) ? count : new IntConstant(1)) : counts[0];
		
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
		this.type = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public void eval(final Context context)
	{	 
		final String item = items[0];
		final Component component = context.game().getComponent(item);
		if (component == null)
			throw new RuntimeException("In the starting rules (place) the component " + item + " is not defined.");

		final int what = component.index();
		final int count = countFn.eval(context);
		final int state = stateFn.eval(context);
		final int rotation = rotationFn.eval(context);
		final int value = valueFn.eval(context);

		if (container != null)
		{
			final Container c = context.game().mapContainer().get(container);
			final int siteFrom = context.game().equipment().sitesFrom()[c.index()];
			for (int pos = siteFrom; pos < siteFrom + c.numSites(); pos++)
				Start.placePieces(context, pos, what, count, state, rotation, value, true, type);
		}
		else
		{
			final int[] locs = region.eval(context).sites();
			if (countsFn.length != 0 && locs.length != countsFn.length)
				throw new RuntimeException(
						"In the starting rules (place) the region size is greater than the size of the array counts.");
			for (int k = 0; k < locs.length; k++)
			{
				final int loc = locs[k];
				for (int i = 0; i < ((countsFn.length == 0) ? countFn.eval(context) : countsFn[k].eval(context)); i++)
					Start.placePieces(context, loc, what, count, state, rotation, value, true, type);
			}
		}
	}

	//-------------------------------------------------------------------------

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
		long flags = GameType.Stacking;

		flags |= SiteType.gameFlags(type);

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

		if (container != null)
			if (container.contains("Hand"))
				concepts.set(Concept.PiecesPlacedOutsideBoard.id(), true);

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
		for (final String it : items)
		{
			boolean found = false;
			for (int i = 1; i < game.equipment().components().length; i++)
			{
				final String nameComponent = game.equipment().components()[i].name();
				if (nameComponent.contains(it))
				{
					found = true;
					break;
				}
			}

			if (!found)
			{
				throw new RuntimeException("Place: The component " + it
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
		return "";
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		String regionString = "";
		if (locationIds != null)
		{
			regionString = "[";
			for (final IntFunction i : locationIds)
				regionString += i.toEnglish(game) + ",";
			regionString = regionString.substring(0,regionString.length()-1) + "]";
		}
		else if (coords != null)
		{
			regionString = "[";
			for (final String s : coords)
				regionString += s + ",";
			regionString = regionString.substring(0,regionString.length()-1) + "]";
		}
		else if (region != null)
		{
			regionString = region.toEnglish(game);
		}
		
		return "place stack of " + 
				Arrays.toString(items) + 
				" at " + type.name().toLowerCase() + 
				" " + regionString;
	}
	
	//-------------------------------------------------------------------------
	
}
