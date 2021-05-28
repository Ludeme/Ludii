package game.functions.ints.card;

/**
 * Defines the types of properties which can be returned for the Card super
 * ludeme according an index and optionally a level.
 * 
 * @author Eric.Piette
 */
public enum CardSiteType
{
	/** To return the rank of a card. */
	Rank,

	/** To return the suit of a card. */
	Suit,

	/** To return the value of the trump of a card. */
	TrumpValue,

	/** To return the rank of the trump of a card. */
	TrumpRank,
}
