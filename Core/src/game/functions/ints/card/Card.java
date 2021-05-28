package game.functions.ints.card;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.card.simple.CardTrumpSuit;
import game.functions.ints.card.site.CardRank;
import game.functions.ints.card.site.CardSuit;
import game.functions.ints.card.site.CardTrumpRank;
import game.functions.ints.card.site.CardTrumpValue;
import other.context.Context;

/**
 * Returns a site related to the last move.
 * 
 * @author Eric Piette
 */
public final class Card extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * For the trump suit of a card.
	 * 
	 * @param cardType The property to return.
	 * 
	 * @example (card TrumpSuit)
	 */
	@SuppressWarnings("javadoc")
	public static IntFunction construct
	(
		final CardSimpleType cardType
	)
	{
		switch (cardType)
		{
		case TrumpSuit:
			return new CardTrumpSuit();
		default:
			break;
		}
		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Card(): A CardSimpleType is not implemented.");
	}

	//-------------------------------------------------------------------------
	
	/**
	 * For the rank, the suit, trump rank or the trump value of a card.
	 * 
	 * @param cardType The property to return.
	 * @param at       The site where the card is.
	 * @param level    The level where the card is.
	 * 
	 * @example (card TrumpValue at:(from) level:(level))
	 * @example (card TrumpRank at:(from) level:(level))
	 * @example (card Rank at:(from) level:(level))
	 * @example (card Suit at:(from) level:(level))
	 */
	@SuppressWarnings("javadoc")
	public static IntFunction construct
	(
			       final CardSiteType cardType,
		@Name	   final IntFunction  at,
		@Name @Opt final IntFunction  level
	)
	{
		switch (cardType)
		{
		case Rank:
			return new CardRank(at, level);
		case Suit:
			return new CardSuit(at, level);
		case TrumpRank:
			return new CardTrumpRank(at, level);
		case TrumpValue:
			return new CardTrumpValue(at, level);
		default:
			break;
		}
		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Card(): A CardSiteType is not implemented.");
	}

	private Card()
	{
		// Make grammar pick up construct() and not default constructor
	}

	// -------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		// Should not be called, should only be called on subclasses
		throw new UnsupportedOperationException("Card.eval(): Should never be called directly.");

		// return new Region();
	}

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