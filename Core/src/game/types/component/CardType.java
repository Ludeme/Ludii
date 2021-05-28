package game.types.component;

/**
 * Defines possible rank values of cards.
 * 
 * @author Eric.Piette and cambolbro
 */
public enum CardType
{
	/** Joker rank. */
	Joker( 0, "?"),
	
	/** Ace rank. */
	Ace(   1, "A"),
	
	/** Two rank. */
	Two(   2, "2"),
	
	/** Three rank. */
	Three( 3, "3"),
	
	/** Four rank. */
	Four(  4, "4"),
	
	/** Five rank. */
	Five(  5, "5"),
	
	/** Six rank. */
	Six(   6, "6"),
	
	/** Seven rank. */
	Seven( 7, "7"),
	
	/** Eight rank. */
	Eight( 8, "8"),
	
	/** Nine rank. */
	Nine(  9, "9"),
	
	/** Ten rank. */
	Ten(  10, "10"),
	
	/** Jack rank. */
	Jack( 10, "J"),
	
	/** Queen rank. */
	Queen(10, "Q"),
	
	/** King rank. */
	King( 10, "K"),
	;

	//-------------------------------------------------------------------------

	private int    number;  // number shown on the card, if any
	private String label;   // common name of the card
	
	//-------------------------------------------------------------------------

	CardType(final int number, final String label)
	{
		this.number = number;
		this.label  = label;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return The common name of the card.
	 */
	public String label() 
	{
		return label;
	}

	/**
	 * @return The default number shown on the card, if any.
	 */
	public int number() 
	{
		return number;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return True if the card is royal.
	 */
	public boolean isRoyal()
	{
		boolean result = this == Jack || this == Queen || this == King;
		
		result = result || (this == Joker);  // add Joker so picture is shown correctly
		
		return result;
	}
	
	//-------------------------------------------------------------------------

}
