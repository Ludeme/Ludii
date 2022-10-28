package game.rules.start.place.stack;

import java.util.Arrays;
import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.equipment.component.Component;
import game.equipment.container.Container;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.rules.start.Start;
import game.rules.start.StartRule;
import game.types.board.SiteType;
import game.types.state.GameType;
import main.Constants;
import other.action.die.ActionUpdateDice;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
import other.topology.SiteFinder;
import other.topology.TopologyElement;
import other.trial.Trial;

/**
 * Places a stack with different pieces to a site.
 * 
 * @author Eric.Piette
 */
@Hide
public final class PlaceCustomStack extends StartRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which stack to add. */
	protected final String[] items;

	/** Which container. */
	protected final String container;

	/** Which cell. */
	protected final IntFunction siteId;

	/** Which coord. */
	protected final String coord;

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

	//-------------------------------------------------------------------------

	/**
	 * @param item      The item to place.
	 * @param items     The name of the items on the stack to place.
	 * @param container The name of the container.
	 * @param type      The graph element type [default SiteType of the board].
	 * @param loc       The location to place a piece.
	 * @param coord     The coordinate of the location to place a piece.
	 * @param count     The number of the same piece to place [1].
	 * @param state     The local state value of the piece to place [Off].
	 * @param rotation  The rotation value of the piece to place [Off].
	 * @param value     The piece value to place [Undefined].
	 */
	public PlaceCustomStack
	(
			@Or        final String      item,
			@Or  @Name final String[]    items,
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
		this.items = (items == null) ? new String[] {item} : items;
		this.container = (container == null) ? null : container;
		siteId = (loc == null) ? null : loc;
		this.coord = (coord == null) ? null : coord;
		countFn = (count == null) ? new IntConstant(1) : count;
		stateFn = (state == null) ? new IntConstant(Constants.OFF) : state;
		rotationFn = (rotation == null) ? new IntConstant(Constants.OFF) : rotation;
		valueFn = (value == null) ? new IntConstant(Constants.OFF) : value;
		this.type = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public void eval(final Context context)
	{
		final int count = countFn.eval(context);
		final int state = stateFn.eval(context);
		final int rotation = rotationFn.eval(context);
		final int value = valueFn.eval(context);

		if (items.length > 1)
		{
			for (final String it : items)
			{
				final Component component = context.game().getComponent(it);
				if (component == null)
					throw new RuntimeException(
							"In the starting rules (place) the component " + it + " is not defined.");
				final int what = component.index();

				if (container != null)
				{
					final Container c = context.game().mapContainer().get(container);
					final int siteFrom = context.game().equipment().sitesFrom()[c.index()];
					if (siteId != null)
						Start.placePieces(context, siteId.eval(context) + siteFrom, what, count, state, rotation, value,
								true, type);
					else
						for (int pos = siteFrom; pos < siteFrom + c.numSites(); pos++)
							Start.placePieces(context, pos, what, count, state, rotation, value, true, type);
				}
				else
				{
					int site = Constants.UNDEFINED;
					if (coord != null)
					{
						final TopologyElement element = SiteFinder.find(context.board(), coord, type);
						if (element == null)
							throw new RuntimeException(
									"In the starting rules (place) the Coordinates " + coord + " not found.");
						site = element.index();
					}
					else
						site = siteId.eval(context);

					for (int i = 0; i < count; i++)
						Start.placePieces(context, site, what, count, state, rotation, value, true, type);
				}
			}
		}
		else
		{
			final String item = items[0];
			final Component component = context.game().getComponent(item);
			if (component == null)
				throw new RuntimeException("In the starting rules (place) the component " + item + " is not defined.");

			final int what = component.index();

			if (container != null)
			{
				final Container c = context.game().mapContainer().get(container);
				final int siteFrom = context.game().equipment().sitesFrom()[c.index()];

				// SPECIAL STATE INIT FOR DICE
				if (component.isDie())
				{
					for (int pos = siteFrom; pos < siteFrom + c.numSites(); pos++)
					{
						if (context.state().containerStates()[c.index()].what(pos, type) == 0)
						{
							Start.placePieces(context, pos, what, count, state, rotation, value, true, type);
							final int newState = component.roll(context);
							final ActionUpdateDice actionChangeState = new ActionUpdateDice(pos,
									newState);
							actionChangeState.apply(context, true);
							context.trial().addMove(new Move(actionChangeState));
							context.trial().addInitPlacement();
							break;
						}
					}
				}
				else if (siteId != null)
				{
					Start.placePieces(context, siteId.eval(context) + siteFrom, what, count, state, rotation, value, true, type);
				}
				else
				{
					for (int pos = siteFrom; pos < siteFrom + c.numSites(); pos++)
						Start.placePieces(context, pos, what, count, state, rotation, value, true, type);
				}
			}
			else
			{
				int site = Constants.UNDEFINED;
				if (coord != null)
				{
					final TopologyElement element = SiteFinder.find(context.board(), coord, type);
					if (element == null)
						throw new RuntimeException(
								"In the starting rules (place) the Coordinates " + coord + " not found.");
					site = element.index();
				}
				else
					site = siteId.eval(context);

				for (int i = 0; i < count; i++)
					Start.placePieces(context, site, what, count, state, rotation, value, true, type);
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

		if (siteId != null)
			flags |= siteId.gameFlags(game);

		if (stateFn.eval(new Context(game, new Trial(game))) != Constants.UNDEFINED)
			flags |= GameType.SiteState;

		if (rotationFn.eval(new Context(game, new Trial(game))) != Constants.UNDEFINED)
			flags |= GameType.Rotation;

		if (valueFn.eval(new Context(game, new Trial(game))) != Constants.UNDEFINED)
			flags |= GameType.Value;

//		if (countFn.eval(new Context(game, new Trial(game))) > 1)
//			flags |= GameType.Count;

		flags |= countFn.gameFlags(game);
		flags |= rotationFn.gameFlags(game);
		flags |= stateFn.gameFlags(game);
		flags |= valueFn.gameFlags(game);

		return flags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));

		if (stateFn.eval(new Context(game, new Trial(game))) > Constants.UNDEFINED)
			concepts.set(Concept.SiteState.id(), true);

		if (valueFn.eval(new Context(game, new Trial(game))) != Constants.UNDEFINED)
			concepts.set(Concept.PieceValue.id(), true);

		if (countFn.eval(new Context(game, new Trial(game))) > 1)
			concepts.set(Concept.PieceCount.id(), true);

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

		if (coord != null)
			concepts.set(Concept.PiecesPlacedOnBoard.id(), true);

		if (container != null)
			if (container.contains("Hand"))
				concepts.set(Concept.PiecesPlacedOutsideBoard.id(), true);

		if (rotationFn.eval(new Context(game, new Trial(game))) > Constants.UNDEFINED)
		{
			concepts.set(Concept.PieceRotation.id(), true);
			concepts.set(Concept.SetRotation.id(), true);
		}

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (siteId != null)
			writeEvalContext.or(siteId.writesEvalContextRecursive());

		writeEvalContext.or(countFn.writesEvalContextRecursive());
		writeEvalContext.or(rotationFn.writesEvalContextRecursive());
		writeEvalContext.or(stateFn.writesEvalContextRecursive());
		writeEvalContext.or(valueFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (siteId != null)
			readEvalContext.or(siteId.readsEvalContextRecursive());

		readEvalContext.or(countFn.readsEvalContextRecursive());
		readEvalContext.or(rotationFn.readsEvalContextRecursive());
		readEvalContext.or(stateFn.readsEvalContextRecursive());
		readEvalContext.or(valueFn.readsEvalContextRecursive());
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

		if (siteId != null)
			siteId.preprocess(game);

		countFn.preprocess(game);
		rotationFn.preprocess(game);
		stateFn.preprocess(game);
		valueFn.preprocess(game);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "";
	}
	
	@Override
	public String toEnglish(final Game game)
	{
		return "place stack of " + 
				Arrays.toString(items) + 
				" at " + type.name().toLowerCase() + 
				" " + (siteId == null ? coord : siteId.toEnglish(game));
	}
	
	//-------------------------------------------------------------------------
	
}
