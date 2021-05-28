package game.rules.start.split;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import game.Game;
import game.equipment.component.Component;
import game.equipment.container.Container;
import game.equipment.container.other.Deck;
import game.rules.start.StartRule;
import game.types.board.SiteType;
import game.types.state.GameType;
import main.Constants;
import other.action.BaseAction;
import other.action.move.ActionMove;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
import other.state.stacking.BaseContainerStateStacking;

/**
 * Splits a deck of cards.
 * 
 * @author Eric.Piette and cambolbro
 * 
 * @remarks This ludeme is used for card games.
 */
public final class Split extends StartRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @param type The type of object to split.
	 * @example (split Deck)
	 */
	public Split(final SplitType type)
	{
		// Nothing to do here until we have many types.
		switch (type)
		{
		case Deck:
			break;
		default:
			break;
		}
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void eval(final Context context)
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
		final BaseContainerStateStacking cs = (BaseContainerStateStacking) context.containerState(deck.index());
		final int indexSiteDeck = context.sitesFrom()[deck.index()];
		final int sizeDeck = cs.sizeStackCell(indexSiteDeck);

		int hand = 0;
		for (int indexCard = 0; indexCard < sizeDeck; indexCard++)
		{
			final BaseAction actionAtomic = new ActionMove(SiteType.Cell, indexSiteDeck, 0, SiteType.Cell,
					handIndex.get(hand).intValue(), Constants.OFF, Constants.OFF, Constants.OFF, Constants.OFF, false);
			actionAtomic.apply(context, true);
			context.trial().addMove(new Move(actionAtomic));
			context.trial().addInitPlacement();
			if (hand == context.game().players().count() - 1)
				hand = 0;
			else
				hand++;
		}
		
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return true;
	}

	@Override
	public long gameFlags(final Game game)
	{
		return GameType.Card;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.Card.id(), true);
		return concepts;
	}

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
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
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
			game.addRequirementToReport("The ludeme (split Deck ...) is used but the equipment has no cards.");
			missingRequirement = true;
		}
		return missingRequirement;
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
		final String str = "(splitDeck)";
		return str;
	}
}
