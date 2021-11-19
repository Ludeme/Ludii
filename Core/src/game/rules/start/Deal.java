package game.rules.start;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import annotations.Opt;
import game.Game;
import game.equipment.component.Component;
import game.equipment.container.Container;
import game.equipment.container.other.Deck;
import game.types.board.SiteType;
import game.types.component.DealableType;
import game.types.state.GameType;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.action.BaseAction;
import other.action.move.move.ActionMove;
import other.context.Context;
import other.move.Move;
import other.state.container.ContainerState;

/**
 * To deal different components between players.
 * 
 * @author Eric.Piette
 */
public final class Deal extends StartRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The number to deal. */
	private final int count;

	/** The number to deal. */
	private final DealableType type;

	//-------------------------------------------------------------------------

	/**
	 * @param type Type of deal.
	 * @param count The number of components to deal [1].
	 * 
	 * @example (deal Dominoes 7)
	 */
	public Deal
	(
			 final DealableType type,
		@Opt final Integer      count
	)
	{
		this.type = type;
		this.count = (count == null) ? 1 : count.intValue();
	}

	//-------------------------------------------------------------------------

	@Override
	public void eval(final Context context)
	{
		if (type == DealableType.Cards)
		{
			evalCards(context);
		}
		else if (type == DealableType.Dominoes)
		{
			evalDominoes(context);
		}
	}

	/**
	 * To deal cards.
	 * 
	 * @param context
	 */
	public void evalCards(final Context context)
	{
		// If no deck nothing to do.
		if (context.game().handDeck().isEmpty())
			return;

		final List<Integer> handIndex = new ArrayList<>();
		for (final Container c : context.containers())
			if (c.isHand() && !c.isDeck() && !c.isDice())
				handIndex.add(Integer.valueOf(context.sitesFrom()[c.index()]));

		// If each player does not have a hand, nothing to do.
		if (handIndex.size() != context.game().players().count())
			return;

		final Deck deck = context.game().handDeck().get(0);
		final ContainerState cs = context.containerState(deck.index());
		final int indexSiteDeck = context.sitesFrom()[deck.index()];
		final int sizeDeck = cs.sizeStackCell(indexSiteDeck);

		 if (sizeDeck < count * handIndex.size())
			throw new IllegalArgumentException("You can not deal so much cards in the initial state.");
		
		int hand = 0;
//		int level = 0;
		for (int indexCard = 0; indexCard < count * handIndex.size(); indexCard++)
		{
			final BaseAction dealAction = new ActionMove(SiteType.Cell, indexSiteDeck,
					cs.sizeStackCell(indexSiteDeck) - 1, SiteType.Cell,
					handIndex.get(hand).intValue(),
					Constants.OFF, Constants.OFF, Constants.OFF, Constants.OFF, false);
			dealAction.apply(context, true);
			context.trial().addMove(new Move(dealAction));
			context.trial().addInitPlacement();
//			final BaseAction makeVisibleAction = new ActionSetVisible(SiteType.Cell, handIndex.get(hand).intValue(),
//					level, hand + 1);
//			makeVisibleAction.apply(context, true);
//			context.trial().addMove(new Move(makeVisibleAction));
//			context.trial().addInitPlacement();

			if (hand == context.game().players().count() - 1)
			{
				hand = 0;
//				level++;
			}
			else
				hand++;
		}
	}

	/**
	 * To deal dominoes.
	 * 
	 * @param context
	 */
	public void evalDominoes(final Context context)
	{
		final TIntArrayList handIndex = new TIntArrayList();
		for (final Container c : context.containers())
			if (c.isHand() && !c.isDeck() && !c.isDice())
				handIndex.add(context.sitesFrom()[c.index()]);

		// If each player does not have a hand, nothing to do.
		if (handIndex.size() != context.game().players().count())
			return;

		final Component[] components = context.components();

		if (components.length < count * handIndex.size())
			throw new IllegalArgumentException("You can not deal so much dominoes in the initial state.");

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
			Start.placePieces(context, handIndex.getQuick(currentPlayer) + (dealed / nbPlayers), component.index(), 1,
					Constants.OFF, Constants.OFF, Constants.UNDEFINED, false,
					SiteType.Cell);
			toDeal.removeAt(index);
			dealed++;
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		return readEvalContext;
	}

	@Override
	public boolean isStatic()
	{
		return true;
	}

	@Override
	public long gameFlags(final Game game)
	{
		if (type == DealableType.Cards)
			return GameType.Card;
		else if (type == DealableType.Dominoes)
			return GameType.LargePiece | GameType.Dominoes | GameType.Stochastic | GameType.HiddenInfo;
		else
			return 0L;
	}
	@Override
	public void preprocess(final Game game)
	{
		// Do nothing
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final String str = "(Deal" + type + ")";
		return str;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "deal " + count + " " + type.name().toLowerCase() + " to each player";
	}
	
	//-------------------------------------------------------------------------
		
}
