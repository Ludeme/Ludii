package other;

//-----------------------------------------------------------------------------

/**
 * Class for loading base card images ONCE per deck.
 * @author cambolbro
 */
public class BaseCardImages
{
	//-------------------------------------------------------------------------
	// Playing card constants

	/**
	 * A large suit.
	 */
	public static final int SUIT_LARGE  = 0;

	/**
	 * A small suit.
	 */
	public static final int SUIT_SMALL  = 1;

	/**
	 * A royal black card.
	 */
	public static final int BLACK_ROYAL = 2;

	/**
	 * A red royal card.
	 */
	public static final int RED_ROYAL   = 3;

	/**
	 * Clubs suit.
	 */
	public static final int CLUBS    = 1;

	/**
	 * Spades suit.
	 */
	public static final int SPADES   = 2;

	/**
	 * Diamonds suit.
	 */
	public static final int DIAMONDS = 3;

	/**
	 * Hearts suit.
	 */
	public static final int HEARTS   = 4;

	/**
	 * Joker card.
	 */
	public static final int JOKER =  0;

	/**
	 * Ace card.
	 */
	public static final int ACE   =  1;

	/**
	 * Jack card.
	 */
	public static final int JACK  = 11;

	/**
	 * Queen card.
	 */
	public static final int QUEEN = 12;

	/**
	 * King card.
	 */
	public static final int KING  = 13;

	// Playing card images
	private String[][] baseCardImagePaths = null;

	private int cardSize;

	//-------------------------------------------------------------------------

	/**
	 * @return The small size of the suit.
	 */
	public int getSuitSizeSmall() {
		return getSuitSizeSmall(cardSize);
	}

	/**
	 * @param cardSizeInput The size of the card in input.
	 * @return The small size of the suit.
	 */
	@SuppressWarnings("static-method")
	public int getSuitSizeSmall(final int cardSizeInput) {
		return (int)(0.100 * cardSizeInput);
	}

	/**
	 * @return The big size of the suit.
	 */
	public int getSuitSizeBig() {
		return getSuitSizeBig(cardSize);
	}

	/**
	 * @param cardSizeInput The size of the card in input.
	 * @return The big size of the suit.
	 */
	@SuppressWarnings("static-method")
	public int getSuitSizeBig(final int cardSizeInput) {
		return (int)(0.160 * cardSizeInput);
	}

	//-------------------------------------------------------------------------

	/**
	 * @param type  The type of the card.
	 * @param which The which of the card.
	 * 
	 * @return The path to get the card file.
	 */
	public String getPath(final int type, final int which)
	{
		if (baseCardImagePaths == null || type >= baseCardImagePaths.length || which >= baseCardImagePaths[type].length)
		{
			System.out.println("** Failed to find base card image type " + type + " value " + which + ".");
			return null;
		}
		return baseCardImagePaths[type][which];
	}

	//-------------------------------------------------------------------------

	/**
	 * Clear.
	 */
	public void clear()
	{
		baseCardImagePaths = null;
	}

	/**
	 * @return True if they are load.
	 */
	public boolean areLoaded()
	{
		return baseCardImagePaths != null;
	}

	//-------------------------------------------------------------------------

	/**
	 * Load the images.
	 * 
	 * @param cardSizeInput The card size in input.
	 */
	public void loadImages(final int cardSizeInput)
	{
		baseCardImagePaths = new String[4][15];

		// Load the four suit images
		cardSize = cardSizeInput;

		// Load the relevant large suit image from file
		baseCardImagePaths[SUIT_LARGE][CLUBS]    = "/svg/cards/card-suit-club.svg";
		baseCardImagePaths[SUIT_LARGE][SPADES]   = "/svg/cards/card-suit-spade.svg";
		baseCardImagePaths[SUIT_LARGE][DIAMONDS] = "/svg/cards/card-suit-diamond.svg";
		baseCardImagePaths[SUIT_LARGE][HEARTS]   = "/svg/cards/card-suit-heart.svg";

		baseCardImagePaths[SUIT_SMALL][CLUBS]    = "/svg/cards/card-suit-club.svg";
		baseCardImagePaths[SUIT_SMALL][SPADES]   = "/svg/cards/card-suit-spade.svg";
		baseCardImagePaths[SUIT_SMALL][DIAMONDS] = "/svg/cards/card-suit-diamond.svg";
		baseCardImagePaths[SUIT_SMALL][HEARTS]   = "/svg/cards/card-suit-heart.svg";

		// Load the royal card figures
		baseCardImagePaths[BLACK_ROYAL][JACK]  = "/svg/cards/card-jack.svg";
		baseCardImagePaths[BLACK_ROYAL][QUEEN] = "/svg/cards/card-queen.svg";
		baseCardImagePaths[BLACK_ROYAL][KING]  = "/svg/cards/card-king.svg";

		baseCardImagePaths[RED_ROYAL][JACK]  = "/svg/cards/card-jack.svg";
		baseCardImagePaths[RED_ROYAL][QUEEN] = "/svg/cards/card-queen.svg";
		baseCardImagePaths[RED_ROYAL][KING]  = "/svg/cards/card-king.svg";
	}

	//-------------------------------------------------------------------------

}
