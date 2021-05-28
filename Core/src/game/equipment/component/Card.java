package game.equipment.component;

import java.io.Serializable;
import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.rules.play.moves.Moves;
import game.types.component.CardType;
import game.types.play.RoleType;
import game.types.state.GameType;
import main.Constants;
import metadata.graphics.util.ComponentStyleType;
import other.concept.Concept;

/**
 * Defines a card with specific properties such as the suit or the rank of the
 * card in the deck.
 * 
 * @author Eric.Piette
 * 
 * @remarks This ludeme creates a specific card. If this ludeme is used with no
 *          deck defined, the generated card will be not included in a deck by
 *          default. See also Deck ludeme.
 */
public class Card extends Component implements Serializable
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Trump value. */
	private final int trumpValue;

	/** Suit value. */
	private final int suit;

	/** Trump rank. */
	private final int trumpRank;

	/** Rank. */
	private final int rank;

	/** Value of the card. */
	private final int value;

	/** The CardType. */
	private final CardType cardType;

	/**
	 * @param label      The name of the card.
	 * @param role       The owner of the card.
	 * @param cardType   The type of a card chosen from the possibilities in the
	 *                   CardType ludeme.
	 * @param rank       The rank of the card in the deck.
	 * @param value      The value of the card.
	 * @param trumpRank  The trump rank of the card in the deck.
	 * @param trumpValue The trump value of the card.
	 * @param suit       The suit of the card.
	 * @param generator  The moves associated with the component.
	 * @param maxState   To set the maximum local state the game should check.
	 * @param maxCount   To set the maximum count the game should check.
	 * @param maxValue   To set the maximum value the game should check.
	 * 
	 * @example (card "Card" Shared King rank:6 value:4 trumpRank:3 trumpValue:4
	 *          suit:1)
	 */
	public Card
	(
		           final String   label, 
		           final RoleType role,
		           final CardType cardType,
		@Name	   final Integer  rank,
		@Name      final Integer  value,
		@Name	   final Integer  trumpRank,
		@Name	   final Integer  trumpValue,
		@Name	   final Integer  suit,
		@Opt       final Moves    generator,
		@Opt @Name final Integer  maxState,
		@Opt @Name final Integer  maxCount,
		@Opt @Name final Integer  maxValue
	)
	{
		super(label, role, null, null, generator, maxState, maxCount, maxValue);
		this.trumpValue = (trumpValue == null) ? Constants.OFF : trumpValue.intValue();
		this.suit = (suit == null) ? Constants.OFF : suit.intValue();
		this.trumpRank = (trumpValue == null) ? Constants.OFF : trumpRank.intValue();
		this.rank = (suit == null) ? Constants.OFF : rank.intValue();
		this.cardType = cardType;
		this.value = value.intValue();
		
		style = ComponentStyleType.Card;
	}

	
	/**
	 * Copy constructor.
	 *
	 * Protected because we do not want the compiler to detect it, this is called
	 * only in Clone method.
	 * 
	 * @param other
	 */
	protected Card(final Card other)
	{
		super(other);
		cardType = other.cardType;
		suit = other.suit;
		trumpValue = other.trumpValue;
		rank = other.rank;
		value = other.value;
		trumpRank = other.trumpRank;
	}

	@Override
	public Card clone()
	{
		return new Card(this);
	}

	@Override
	public boolean isCard()
	{
		return true;
	}

	@Override
	public int suit()
	{
		return suit;
	}

	@Override
	public int getValue()
	{
		return value;
	}

	@Override
	public int trumpValue()
	{
		return trumpValue;
	}

	@Override
	public int rank()
	{
		return rank;
	}

	@Override
	public int trumpRank()
	{
		return trumpRank;
	}
	
	@Override
	public CardType cardType()
	{
		return cardType;
	}

	@Override
	public long gameFlags(final Game game)
	{
		return super.gameFlags(game) | GameType.Card;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.Card.id(), true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (role() != null)
		{
			final int indexOwnerPhase = role().owner();
			if (
					(
						indexOwnerPhase < 1 
						&& 
						!role().equals(RoleType.Shared)
						&& 
						!role().equals(RoleType.Neutral)
						&& 
						!role().equals(RoleType.All)
					) 
					||
					indexOwnerPhase > game.players().count()
			   )
			{
				game.addRequirementToReport(
						"A card is defined in the equipment with an incorrect owner: " + role() + ".");
				missingRequirement = true;
			}
		}
		return missingRequirement;
	}
}