package game.util.equipment;

import annotations.Name;
import annotations.Opt;
import game.types.component.CardType;
import main.Constants;
import other.BaseLudeme;

/**
 * Defines an instance of a playing card.
 * 
 * @author Eric.Piette
 */
public class Card extends BaseLudeme
{
	/**
	 * The rank of the card.
	 */
	final private int rank;

	/**
	 * The value of the card.
	 */
	final private int value;
	
	/**
	 * The trump rank of the card.
	 */
	final private int trumpRank;
	
	/**
	 * The trump value of the card.
	 */
	final private int trumpValue;
	
	/**
	 * The trump value of the card.
	 */
	final private int biased;

	/**
	 * The type of the card.
	 */
	final private CardType type;

	//-------------------------------------------------------------------------

	/**
	 * @param type       The type of the card.
	 * @param rank       The rank of the card.
	 * @param value      The value of the card.
	 * @param trumpRank  The trump rank of the card.
	 * @param trumpValue The trump value of the card.
	 * @param biased     The biased value of the card.
	 * 
	 * @example (card Seven rank:0 value:0 trumpRank:0 trumpValue:0)
	 */
	public Card
	(
			       final CardType type,
		     @Name final Integer  rank, 
		     @Name final Integer  value,
		@Opt @Name final Integer  trumpRank,
		@Opt @Name final Integer  trumpValue,
		@Opt @Name final Integer  biased
	)
	{
		this.type = type;
		this.rank = rank.intValue();
		this.value = value.intValue();
		this.trumpRank = (trumpRank == null) ? rank.intValue() : trumpRank.intValue();
		this.trumpValue = (trumpValue == null) ? value.intValue() : trumpValue.intValue();
		this.biased = (biased == null) ? Constants.UNDEFINED : biased.intValue();
	}

	/**
	 * @return The type of the card.
	 */
	public CardType type()
	{
		return type;
	}

	/**
	 * @return The rank of the card.
	 */
	public int rank()
	{
		return rank;
	}

	/**
	 * @return The value of the card.
	 */
	public int value()
	{
		return value;
	}

	/**
	 * @return The trump rank of the card.
	 */
	public int trumpRank()
	{
		return trumpRank;
	}

	/**
	 * @return The trump value of the card.
	 */
	public int trumpValue()
	{
		return trumpValue;
	}
	
	/**
	 * @return The biased value of the card.
	 */
	public int biased()
	{
		return biased;
	}
}
