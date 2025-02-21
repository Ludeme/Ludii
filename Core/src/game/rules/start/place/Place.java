package game.rules.start.place;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import annotations.Or2;
import game.Game;
import game.functions.booleans.BooleanConstant;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.rules.Rule;
import game.rules.start.StartRule;
import game.rules.start.place.item.PlaceItem;
import game.rules.start.place.random.PlaceRandom;
import game.rules.start.place.stack.PlaceCustomStack;
import game.rules.start.place.stack.PlaceMonotonousStack;
import game.types.board.SiteType;
import other.context.Context;

/**
 * Sets some aspect of the initial game state.
 * 
 * @author Eric.Piette
 */
@SuppressWarnings("javadoc")
public final class Place extends StartRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * For placing an item to a site.
	 * 
	 * @param item      The name of the item.
	 * @param container The name of the container.
	 * @param type      The graph element type [default SiteType of the board].
	 * @param loc       The location to place a piece.
	 * @param coord     The coordinate of the location to place a piece.
	 * @param count     The number of the same piece to place [1].
	 * @param state     The local state value of the piece to place [Off].
	 * @param rotation  The rotation value of the piece to place [Off].
	 * @param value     The piece value to place [Undefined].
	 * 
	 * @example (place "Pawn1" 0)
	 */
	public static Rule construct
	(
			           final String      item,
		@Opt           final String      container,
		@Opt 		   final SiteType    type,
		@Opt	       final IntFunction loc,
		@Opt     @Name final String      coord,
		@Opt     @Name final IntFunction count,
		@Opt     @Name final IntFunction state,
		@Opt     @Name final IntFunction rotation,
		@Opt     @Name final IntFunction value
	)
	{
		return new PlaceItem(item, container, type, loc, coord, count, state, rotation, value);
	}

	//-------------------------------------------------------------------------

	/**
	 * For placing item(s) to sites.
	 * 
	 * @param item        The item to place.
	 * @param type        The graph element type [default SiteType of the board].
	 * @param locs        The sites to fill.
	 * @param region      The region to fill.
	 * @param coords      The coordinates of the sites to fill.
	 * @param counts      The number of pieces on the state.
	 * @param state       The local state value to put on each site.
	 * @param rotation    The rotation value to put on each site.
	 * @param value       The piece value to place [Undefined].
	 * @param invisibleTo The list of the players where these locations will be
	 *                    invisible.
	 * 
	 * @example (place "Pawn1" (sites Bottom))
	 */
	public static Rule construct
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
		return new PlaceItem(item, type, locs, region, coords, counts, state, rotation, value);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For placing items into a stack.
	 * 
	 * @param placeType The property to place.
	 * @param item      The item to place on the stack.
	 * @param items     The name of the items on the stack to place.
	 * @param container The name of the container.
	 * @param type      The graph element type [default SiteType of the board].
	 * @param loc       The location to place the stack.
	 * @param locs      The locations to place the stacks.
	 * @param region    The region to place the stacks.
	 * @param coord     The coordinate of the location to place the stack.
	 * @param coords    The coordinates of the sites to place the stacks.
	 * @param count     The number of the same piece to place on the stack [1].
	 * @param counts    The number of pieces on the stack.
	 * @param state     The local state value of the piece on the stack to place
	 *                  [Undefined].
	 * @param rotation  The rotation value of the piece on the stack to place
	 *                  [Undefined].
	 * @param value     The piece value to place [Undefined].
	 * 
	 * @example (place Stack items:{"Counter2" "Counter1"} 0)
	 * 
	 * @example (place Stack "Disc1" coord:"A1" count:5)
	 */
	public static Rule construct
	(
			     	    final PlaceStackType placeType,
		@Or             final String         item, 
		@Or       @Name final String[]       items,
		     @Opt       final String         container,
		     @Opt 		final SiteType       type,
		@Or  @Opt	    final IntFunction    loc,
		@Or  @Opt 	    final IntFunction[]  locs,
		@Or  @Opt 	    final RegionFunction region,
		@Or  @Opt @Name final String         coord,
		@Or  @Opt 	    final String[] 	     coords,
		@Or2 @Opt @Name final IntFunction    count,
		@Or2 @Opt @Name final IntFunction[]  counts,
		     @Opt @Name final IntFunction 	 state,
		     @Opt @Name final IntFunction    rotation,
			 @Opt @Name final IntFunction    value
	)
	{
		int numNonNull = 0;
		if (item != null)
			numNonNull++;
		if (items != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException(
					"Place(): With PlaceStackType exactly one item or items parameter must be non-null.");
		
		numNonNull = 0;
		if (count != null)
			numNonNull++;
		if (counts != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException(
					"Place(): With PlaceStackType zero or one count or counts parameter must be non-null.");
		
		numNonNull = 0;
		if (coord != null)
			numNonNull++;
		if (coords != null)
			numNonNull++;
		if (loc != null)
			numNonNull++;
		if (locs != null)
			numNonNull++;
		if (region != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException(
					"Place(): With PlaceStackType zero or one coord or coords or loc or locs or region parameter must be non-null.");
		
		if (items == null && (locs != null || region != null || coord != null || counts != null))
			return new PlaceMonotonousStack(item, type, locs, region, coords, count, counts, state, rotation, value);
		else
			return new PlaceCustomStack(item, items, container, type, loc, coord, count, state, rotation, value);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For placing randomly pieces.
	 * 
	 * @param placeType 	The property to place.
	 * @param region    	The region in which to randomly place piece(s).
	 * @param item      	The names of the item to place.
	 * @param count     	The number of items to place [1].
	 * @param state     	The state value to place [Undefined].
	 * @param value     	The piece value to place [Undefined].
	 * @param type      	The graph element type [default SiteType of the board].
	 * @param randPiecOrder Whether the given 'items' should be selected in random order for placement.
	 * 
	 * @example (place Random {"Pawn1" "Pawn2"})
	 */
	public static Rule construct
	(
			     	   final PlaceRandomType placeType,
			@Opt 	   final RegionFunction  region,
			           final String[]        item,
			@Opt @Name final IntFunction     count,
			@Opt @Name final IntFunction 	 state,
			@Opt @Name final IntFunction 	 value,
		    @Opt 	   final SiteType        type,
		    @Opt 	   final BooleanConstant randPiecOrder
	)
	{
		return new PlaceRandom(region, item, count, value, state, type, randPiecOrder);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For placing randomly a stack.
	 * 
	 * @param placeType The property to place.
	 * @param pieces    The names of each type of piece in the stack.
	 * @param count     The number of pieces of each piece in the stack.
	 * @param state     The state value to place [Undefined].
	 * @param value     The piece value to place [Undefined].
	 * @param where     The site on which to place the stack.
	 * @param type      The graph element type [default SiteType of the board].
	 *
	 * @example (place Random {"Ball1"} count:29)
	 */
	public static Rule construct
	(
			     	   final PlaceRandomType placeType,
					   final String[]        pieces,
			@Opt @Name final IntFunction[]   count,
			@Opt @Name final IntFunction 	 state,
			@Opt @Name final IntFunction     value,
					   final IntFunction     where,
			@Opt 	   final SiteType        type
	)
	{
		return new PlaceRandom(pieces, count, value, state, where, type);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For placing randomly a stack with specific number of each type of pieces.
	 * 
	 * @param placeType The property to place.
	 * @param items     The items to be placed, with counts.
	 * @param where     The site on which to place the stack.
	 * @param type      The graph element type [default SiteType of the board].
	 * 
	 * @example (place Random { (count "Pawn1" 8) (count "Rook1" 2) (count "Knight1"
	 *          2) (count "Bishop1" 2) (count "Queen1" 1) (count "King1" 1) }
	 *          (handSite 1) )
	 */
	public static Rule construct
	(
			     	   final PlaceRandomType         placeType,
					   final game.util.math.Count[]  items,
					   final IntFunction             where,
			@Opt 	   final SiteType                type
	)
	{
		return new PlaceRandom(items, where, type);
	}
	
	private Place()
	{
		// Ensure that compiler does pick up default constructor
	}

	@Override
	public void eval(final Context context)
	{
		// Should not be called, should only be called on subclasses
		throw new UnsupportedOperationException("Place.eval(): Should never be called directly.");
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		// Should never be there
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		// Should never be there
		return 0L;
	}

	@Override
	public void preprocess(final Game game)
	{
		// Nothing to do.
	}
}