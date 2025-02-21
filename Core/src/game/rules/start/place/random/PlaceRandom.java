package game.rules.start.place.random;

import java.util.Arrays;
import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.equipment.component.Component;
import game.functions.ints.IntConstant;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.functions.region.sites.simple.SitesBoard;
import game.rules.start.Start;
import game.rules.start.StartRule;
import game.types.board.SiteType;
import game.types.state.GameType;
import game.util.math.Count;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;
import other.trial.Trial;
import java.util.ArrayList;
import java.util.List;

/**
 * Places pieces randomly in a specified container.
 * 
 * @author Eric.Piette
 */
@Hide
public final class PlaceRandom extends StartRule 
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------

	/** In which region. */
	private final RegionFunction region;

	/** Which item to add. */
	private final String[] item;

	/** How many to add. */
	private final IntFunction countFn;
	
	/** Piece value of the item. */
	private final IntFunction valueFn;

	/** Piece state of the item. */
	private final IntFunction stateFn;

	//------------------ For a stack ------------------------------------------

	/** To place a random stack to a specific site. */
	private final boolean stack;

	/** Which site */
	private final IntFunction where;

	/** The pieces to shuffle in the stack */
	private final String[] pieces;

	/** The number of each piece in the stack */
	private final IntFunction[] counts;

	/** Cell, Edge or Vertex. */
	private SiteType type;
	
	/** If order of piece types to be placed is random or not. */
	private BaseBooleanFunction randPiecOrderFn;

	//-------------------------------------------------------------------------

	/**
	 * @param region The region in which to randomly place piece(s).
	 * @param item   The names of the item to place.
	 * @param count  The number of items to place [1].
	 * @param value  The piece value to place [Undefined].
	 * @param state  The state value to place [Undefined].PlaceRandom
	 * @param type   The graph element type [default SiteType of the board].
	 * @param randPiecOrder   If order of piece types to be placed is random or not [False].
	 */
	public PlaceRandom
	(
		@Opt 	   final RegionFunction  region,
				   final String[]        item,
		@Opt @Name final IntFunction     count,
		@Opt @Name final IntFunction     value,
		@Opt @Name final IntFunction     state,
		@Opt 	   final SiteType        type,
		@Opt 	   final BooleanConstant randPiecOrder
	) 
	{
		this.region = (region == null ? new SitesBoard(type) : region);
		countFn = (count == null) ? new IntConstant(1) : count;
		this.item = item;
		where = null;
		pieces = null;
		counts = null;
		stack = false;

		stateFn = (state == null) ? new IntConstant(Constants.OFF) : state;
		valueFn = (value == null) ? new IntConstant(Constants.OFF) : value;
		randPiecOrderFn = (randPiecOrder == null) ? new BooleanConstant(false) : randPiecOrder;
		
		this.type = type;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param pieces The names of each type of piece in the stack.
	 * @param count  The number of pieces of each piece in the stack.
	 * @param value  The piece value to place [Undefined].
	 * @param state  The state value to place [Undefined].
	 * @param where  The site on which to place the stack.
	 * @param type   The graph element type [default SiteType of the board].
	 */
	public PlaceRandom
	(
				   final String[]      pieces,
		@Opt @Name final IntFunction[] count,
		@Opt @Name final IntFunction   value,
		@Opt @Name final IntFunction   state,
				   final IntFunction   where,
		@Opt 	   final SiteType      type
	)
	{
		region = new SitesBoard(type);
		item = null;
		countFn = new IntConstant(1);
		this.pieces = pieces;
		this.where = where;
		counts = count;
		stack = true;

		stateFn = (state == null) ? new IntConstant(Constants.OFF) : state;
		valueFn = (value == null) ? new IntConstant(Constants.OFF) : value;
		
		this.type = type;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param items The items to be placed, with counts.
	 * @param where The site on which to place the stack.
	 * @param type  The graph element type [default SiteType of the board].
	 */
	public PlaceRandom
	(
				   final Count[]     items,
				   final IntFunction where,
		@Opt 	   final SiteType    type
	)
	{
		region = new SitesBoard(type);
		item = null;
		countFn = new IntConstant(1);
		this.where = where;
		stack = true;
		this.type = type;
		
		stateFn = new IntConstant(Constants.OFF);
		valueFn = new IntConstant(Constants.OFF);

		pieces = new String[items.length];
		counts = new IntFunction[items.length];
		for (int i = 0; i < items.length; i++)
		{
			pieces[i] = items[i].item();
			counts[i] = items[i].count();
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public void eval(final Context context)
	{
		if (stack)
		{
			evalStack(context);
		}
		else if(randPiecOrderFn.eval(context))
		{
//			System.out.println("Test");
			final SiteType realType = (type == null) ? context.board().defaultSite() : type;
			final TIntArrayList sites = new TIntArrayList(region.eval(context).sites());
			List<String> Aitems = new ArrayList<>(Arrays.asList(item));		
			
			for (int iter=0; iter < Aitems.size()-1;iter ++)
			{
				int randomIndex = context.rng().nextInt(Aitems.size());
//				System.out.println(randomIndex);
	            String it = Aitems.remove(randomIndex);

				final Component component = context.game().getComponent(it);
				if (component == null)
					throw new RuntimeException("Component " + item + " is not defined.");

				final int what = component.index();

				// remove the non empty sites in that region.
				for (int index = sites.size() - 1; index >= 0; index--)
				{
					final int site = sites.get(index);
					final int cid = (realType.equals(SiteType.Cell) || realType.equals(SiteType.Vertex)) ? context.containerId()[site] : 0;
					final ContainerState cs = context.containerState(cid);
					if (cs.what(site, realType) != 0)
						sites.removeAt(index);
				}

				final int state = stateFn.eval(context);
				final int value = valueFn.eval(context);

				for (int i = 0; i < countFn.eval(context); i++)
				{
//						System.out.println("test4");
						final int[] emptySites = sites.toArray();
						// If no empty site we stop here.
						if (emptySites.length == 0)
							break;
						// We randomly take an empty site.
						final int site = emptySites[context.rng().nextInt(emptySites.length)];
						sites.remove(site);
						Start.placePieces(context, site, what, 1, state, Constants.OFF, value, false, realType);
//						System.out.println("test3");
				}
			}
		}
		else
		{
			final SiteType realType = (type == null) ? context.board().defaultSite() : type;
			for (final String it : item)
			{
				final TIntArrayList sites = new TIntArrayList(region.eval(context).sites());

				final Component component = context.game().getComponent(it);
				if (component == null)
					throw new RuntimeException("Component " + item + " is not defined.");

				final int what = component.index();

				// remove the non empty sites in that region.
				for (int index = sites.size() - 1; index >= 0; index--)
				{
					final int site = sites.get(index);
					final int cid = realType.equals(SiteType.Cell) ? context.containerId()[site] : 0;
					final ContainerState cs = context.containerState(cid);
					if (cs.what(site, realType) != 0)
						sites.removeAt(index);
				}

				final int state = stateFn.eval(context);
				final int value = valueFn.eval(context);

				for (int i = 0; i < countFn.eval(context); i++)
				{
						final int[] emptySites = sites.toArray();
						// If no empty site we stop here.
						if (emptySites.length == 0)
							break;
						// We randomly take an empty site.
						final int site = emptySites[context.rng().nextInt(emptySites.length)];
						sites.remove(site);
						Start.placePieces(context, site, what, 1, state, Constants.OFF, value, false, realType);
				}
			}
		}
	}

	//-------------------------------------------------------------------------

	private void evalStack(Context context)
	{
		final SiteType realType = (type == null) ? context.board().defaultSite() : type;
		final int site = where.eval(context);
		final TIntArrayList toPlace = new TIntArrayList();
		
		for (int i = 0; i < pieces.length; i++)
		{
			final String piece = pieces[i];
			for (int pieceIndex = 1; pieceIndex < context.components().length; pieceIndex++)
			{
				if (context.components()[pieceIndex].name().equals(piece))
				{
					if (counts == null)
					{
						toPlace.add(pieceIndex);
					}
					else
					{
						for (int j = 0; j < counts[i].eval(context); j++)
							toPlace.add(pieceIndex);
					}
					
					break;
				}
			}
		}
				
		final int state = stateFn.eval(context);
		final int value = valueFn.eval(context);

		while (!toPlace.isEmpty())
		{
			final int index = context.rng().nextInt(toPlace.size());
			final int what = toPlace.getQuick(index);
			Start.placePieces(context, site, what, 1, state, Constants.OFF, value, true, realType);
			toPlace.removeAt(index);
		}

	}

	//-------------------------------------------------------------------------

	@Override
	public int count(final Game game)
	{
		return 1;
	}

	@Override
	public int state(final Game game)
	{
		return stateFn.eval(new Context(game, new Trial(game)));
	}

	@Override
	public int howManyPlace(final Game game)
	{
		return 1;
	}

	@Override
	public boolean isStatic() 
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game) 
	{
		long flags = GameType.Stochastic;

		flags |= SiteType.gameFlags(type);

		if (region != null)
			flags |= region.gameFlags(game);
		
		if(stack)
			flags |= GameType.Stacking;
		
		if (where != null)
			flags |= where.gameFlags(game);
		
		if (counts != null)
			for (final IntFunction func : counts)
				flags |= func.gameFlags(game);

		if (valueFn.eval(new Context(game, new Trial(game))) != Constants.UNDEFINED)
			flags |= GameType.Value;

		if (stateFn.eval(new Context(game, new Trial(game))) != Constants.UNDEFINED)
			flags |= GameType.SiteState;
		
		if (randPiecOrderFn != null)
			flags |= randPiecOrderFn.gameFlags(game);

		flags |= stateFn.gameFlags(game);
		flags |= valueFn.gameFlags(game);
		flags |= countFn.gameFlags(game);

		return flags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.set(Concept.InitialRandomPlacement.id(), true);
		concepts.set(Concept.Stochastic.id(), true);

		if (valueFn.eval(new Context(game, new Trial(game))) != Constants.UNDEFINED)
			concepts.set(Concept.PieceValue.id(), true);

		if (stateFn.eval(new Context(game, new Trial(game))) > Constants.UNDEFINED)
			concepts.set(Concept.SiteState.id(), true);

		if (countFn.eval(new Context(game, new Trial(game))) > Constants.UNDEFINED)
			concepts.set(Concept.PieceCount.id(), true);

		final int maxSiteOnBoard = (type == null)
				? game.board().topology().getGraphElements(game.board().defaultSite()).size()
				: (type.equals(SiteType.Cell)) ? game.board().topology().getGraphElements(SiteType.Cell).size()
						: (type.equals(SiteType.Vertex))
								? game.board().topology().getGraphElements(SiteType.Vertex).size()
								: game.board().topology().getGraphElements(SiteType.Edge).size();

		if (where != null)
		{
			concepts.or(where.concepts(game));
			final int site = where.eval(new Context(game, new Trial(game)));
			if (site < maxSiteOnBoard)
				concepts.set(Concept.PiecesPlacedOnBoard.id(), true);
			else
				concepts.set(Concept.PiecesPlacedOutsideBoard.id(), true);
		}
		else if (region != null)
			concepts.or(region.concepts(game));

		if (counts != null)
			for (final IntFunction func : counts)
				concepts.or(func.concepts(game));
		
		if (randPiecOrderFn != null)
			concepts.or(randPiecOrderFn.concepts(game));

		concepts.or(countFn.concepts(game));
		concepts.or(stateFn.concepts(game));
		concepts.or(valueFn.concepts(game));

		return concepts;
	}
	
	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (where != null)
			writeEvalContext.or(where.writesEvalContextRecursive());
		else if (region != null)
			writeEvalContext.or(region.writesEvalContextRecursive());

		if (counts != null)
			for (final IntFunction func : counts)
				writeEvalContext.or(func.writesEvalContextRecursive());
		
		if (randPiecOrderFn != null)
			writeEvalContext.or(randPiecOrderFn.writesEvalContextRecursive());


		writeEvalContext.or(countFn.writesEvalContextRecursive());
		writeEvalContext.or(stateFn.writesEvalContextRecursive());
		writeEvalContext.or(valueFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (where != null)
			readEvalContext.or(where.readsEvalContextRecursive());
		else if (region != null)
			readEvalContext.or(region.readsEvalContextRecursive());

		if (counts != null)
			for (final IntFunction func : counts)
				readEvalContext.or(func.readsEvalContextRecursive());
		
		if (randPiecOrderFn != null)
			readEvalContext.or(randPiecOrderFn.readsEvalContextRecursive());

		readEvalContext.or(countFn.readsEvalContextRecursive());
		readEvalContext.or(stateFn.readsEvalContextRecursive());
		readEvalContext.or(valueFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (region != null)
			region.preprocess(game);

		type = SiteType.use(type, game);

		if (where != null)
			where.preprocess(game);
		
		if (counts != null)
		{
			for (final IntFunction func : counts)
				func.preprocess(game);
		}
		
		if (randPiecOrderFn != null)
			randPiecOrderFn.preprocess(game);

		countFn.preprocess(game);
		stateFn.preprocess(game);
		valueFn.preprocess(game);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toEnglish(final Game game)
	{
		String regionString = "";
		if (region != null)
			regionString = region.toEnglish(game);
		
		String valueString = "";
		if (valueFn != null)
			valueString = " with value " + valueFn;
		
		String stateString = "";
		if (stateFn != null)
			stateString = " with state " + stateFn;
		
		return "randomly place " + countFn.toEnglish(game) + " " + Arrays.toString(item) + " within " + type.name().toLowerCase() +  " " + regionString + valueString + stateString;
	}
	
	//-------------------------------------------------------------------------

}