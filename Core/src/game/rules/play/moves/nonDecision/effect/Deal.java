package game.rules.play.moves.nonDecision.effect;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.equipment.component.Component;
import game.equipment.container.Container;
import game.equipment.container.other.Deck;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.types.component.DealableType;
import game.types.state.GameType;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.action.BaseAction;
import other.action.move.ActionAdd;
import other.action.move.ActionMove;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
import other.state.container.ContainerState;

/**
 * Deals cards or dominoes to each player.
 * 
 * @author Eric.Piette
 */
public final class Deal extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The number to deal. */
	private final IntFunction countFn;

	/** The number to deal. */
	private final DealableType type;

	/** To start to deal with a specific player. */
	private final IntFunction beginWith;

	//-------------------------------------------------------------------------

	/**
	 * @param type      Type of deal.
	 * @param count     The number of components to deal [1].
	 * @param beginWith To start to deal with a specific player.
	 * @param then      The moves applied after that move is applied.
	 * 
	 * @example (deal Cards 3 beginWith:(mover))
	 */
	public Deal
	(
			       final DealableType type,
		@Opt       final IntFunction  count,
		@Opt @Name final IntFunction  beginWith,
		@Opt       final Then         then
	)
	{
		super(then);
		this.type = type;
		countFn = (count == null) ? new IntConstant(1) : count;
		this.beginWith = beginWith;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		if (type == DealableType.Cards)
			return evalCards(context);
		else if (type == DealableType.Dominoes)
			return evalDominoes(context);

		return new BaseMoves(super.then());
	}

	//-------------------------------------------------------------------------

	/**
	 * @param context
	 * @return The moves to deal cards.
	 */
	public Moves evalCards(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());

		// If no deck nothing to do.
		if (context.game().handDeck().isEmpty())
			return moves;

		final List<Integer> handIndex = new ArrayList<>();		// TODO make this a TIntArrayList
		for (final Container c : context.containers())
			if (c.isHand() && !c.isDeck() && !c.isDice())
				handIndex.add(Integer.valueOf(context.sitesFrom()[c.index()]));

		// If each player does not have a hand, nothing to do.
		if (handIndex.size() != context.game().players().count())
			return moves;

		final Deck deck = context.game().handDeck().get(0);
		final ContainerState cs = context.containerState(deck.index());
		final int indexSiteDeck = context.sitesFrom()[deck.index()];
		final int sizeDeck = cs.sizeStackCell(indexSiteDeck);
		final int count = countFn.eval(context);

		if (sizeDeck < count * handIndex.size())
			throw new IllegalArgumentException("You can not deal so much cards.");

		int hand = (beginWith == null) ? 0 : (beginWith.eval(context) - 1);
		int counter = 0;
		for (int indexCard = 0; indexCard < count * handIndex.size(); indexCard++)
		{
			final BaseAction dealAction = new ActionMove(SiteType.Cell, indexSiteDeck,
					cs.sizeStackCell(indexSiteDeck) - 1 - counter, SiteType.Cell,
					handIndex.get(hand).intValue(),
					Constants.OFF, Constants.OFF, Constants.OFF, Constants.OFF, false);
			final Move move = new Move(dealAction);
//			for (int pid = 1; pid < context.game().players().size(); pid++)
//			{
//				final BaseAction makeMaskedAction = new ActionSetMasked(SiteType.Cell, handIndex.get(hand).intValue(),
//						Constants.UNDEFINED, pid);
//				move.actions().add(makeMaskedAction);
//			}
//			final BaseAction makeVisibleAction = new ActionSetVisible(SiteType.Cell, handIndex.get(hand).intValue(),
//					Constants.UNDEFINED, hand + 1);
//			move.actions().add(makeVisibleAction);
			moves.moves().add(move);

			if (hand == context.game().players().count() - 1)
				hand = 0;
			else
				hand++;
			counter++;
		}

		// The subsequents to add to the moves
		if (then() != null)
			for (int j = 0; j < moves.moves().size(); j++)
				moves.moves().get(j).then().add(then().moves());

		// Store the Moves in the computed moves.
		for (int j = 0; j < moves.moves().size(); j++)
			moves.moves().get(j).setMovesLudeme(this);

		return moves;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param context
	 * @return The moves to deal dominoes.
	 */
	public Moves evalDominoes(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());

		final TIntArrayList handIndex = new TIntArrayList();
		for (final Container c : context.containers())
			if (c.isHand() && !c.isDeck() && !c.isDice())
				handIndex.add(context.sitesFrom()[c.index()]);

		// If each player does not have a hand, nothing to do.
		if (handIndex.size() != context.game().players().count())
			return moves;

		final Component[] components = context.components();
		final int count = countFn.eval(context);

		if (components.length < count * handIndex.size())
			throw new IllegalArgumentException("You can not deal so much dominoes.");

		final TIntArrayList toDeal = new TIntArrayList();
		for (int i = 1; i < components.length; i++)
			toDeal.add(i);

		final int nbPlayers = context.players().size() - 1;

		final ArrayList<boolean[]> masked = new ArrayList<>();
		for (int i = 1; i <= nbPlayers; i++)
		{
			masked.add(new boolean[nbPlayers]);
			for (int j = 1; j <= nbPlayers; j++)
			{
				if (i == j)
					masked.get(i - 1)[j - 1] = false;
				else
					masked.get(i - 1)[j - 1] = true;
			}
		}

		int dealed = 0;
		while (dealed < (count * 2))
		{
			final int index = context.rng().nextInt(toDeal.size());
			final int indexComponent = toDeal.getQuick(index);
			final Component component = components[indexComponent];
			final int currentPlayer = dealed % nbPlayers;
			final BaseAction actionAtomic = new ActionAdd(SiteType.Cell,
					handIndex.getQuick(currentPlayer) + (dealed / nbPlayers),
					component.index(), count, Constants.OFF, Constants.OFF, Constants.UNDEFINED,
					null);
			final Move move = new Move(actionAtomic);
			moves.moves().add(move);
			toDeal.removeAt(index);
			dealed++;
		}

		// The subsequents to add to the moves.
		if (then() != null)
			for (int j = 0; j < moves.moves().size(); j++)
				moves.moves().get(j).then().add(then().moves());

		return moves;
	}

	@Override
	public boolean canMove(final Context context)
	{
		return false;
	}

	@Override
	public boolean canMoveTo(final Context context, final int target)
	{
		return false;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = countFn.gameFlags(game) | super.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		if (type == DealableType.Cards)
			return GameType.Card | gameFlags;
		else if (type == DealableType.Dominoes)
			return GameType.LargePiece | GameType.Dominoes | GameType.Stochastic | GameType.HiddenInfo
					| gameFlags;
		else
			return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.or(countFn.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		if (type == DealableType.Cards)
			concepts.set(Concept.Card.id(), true);
		else if (type == DealableType.Dominoes)
			concepts.set(Concept.Domino.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(countFn.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(countFn.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (type == DealableType.Cards)
		{
			boolean gameHasCard = false;
			for (int i = 1; i < game.equipment().components().length; i++)
			{
				final Component component = game.equipment().components()[i];
				if (component.isCard())
				{
					gameHasCard = true;
					break;
				}

			}

			if (!gameHasCard)
			{
				game.addRequirementToReport("The ludeme (deal Cards ...) is used but the equipment has no cards.");
				missingRequirement = true;
			}
		}
		else if (type == DealableType.Dominoes)
		{
			if (!game.hasDominoes())
			{
				game.addRequirementToReport(
						"The ludeme (deal Dominoes ...) is used but the equipment has no dominoes.");
				missingRequirement = true;
			}
		}

		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= countFn.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= countFn.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public void preprocess(final Game game)
	{
		super.preprocess(game);
		countFn.preprocess(game);
		if (beginWith != null)
			beginWith.preprocess(game);
	}

	//-------------------------------------------------------------------------

}
