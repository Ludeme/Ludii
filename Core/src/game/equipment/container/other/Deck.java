package game.equipment.container.other;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.equipment.component.Card;
import game.equipment.component.Component;
import game.equipment.container.Container;
import game.functions.dim.DimConstant;
import game.functions.graph.generators.basis.hex.RectangleOnHex;
import game.functions.graph.generators.basis.square.RectangleOnSquare;
import game.functions.graph.generators.basis.tri.RectangleOnTri;
import game.types.board.SiteType;
import game.types.component.CardType;
import game.types.play.RoleType;
import game.util.graph.Face;
import game.util.graph.Graph;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import metadata.graphics.util.ContainerStyleType;
import other.ItemType;
import other.concept.Concept;
import other.topology.Cell;
import other.topology.Topology;
import other.topology.Vertex;

/**
 * Generates a deck of cards.
 * 
 * @author Eric.Piette
 */
public class Deck extends Container
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The number of cards by suit. */
	private final Integer cardsBySuit;
	
	/** The number of suits. */
	private final Integer suits;

	/** The ranks of each card. */
	private Integer[] ranks;

	/** The values of each card. */
	private Integer[] values;

	/** The trump ranks of each card. */
	private Integer[] trumpRanks;

	/** The trump values of each card. */
	private Integer[] trumpValues;

	/** The biased value of the deck. */
	private Integer[] biased;

	/** The type of the card. */
	private CardType[] types;

	/** The indices of the components in the deck. */
	private final TIntArrayList indexComponent;

	/** The number of locations in this container. */
	protected int numLocs;

	//-------------------------------------------------------------------------

	/**
	 * @param role        The owner of the deck [Shared].
	 * @param cardsBySuit The number of cards per suit [13].
	 * @param suits       The number of suits in the deck [4].
	 * @param cards       Specific data about each kind of card for each suit.
	 * 
	 * @example (deck)
	 * 
	 * @example (deck { (card Seven rank:0 value:0 trumpRank:0 trumpValue:0) (card
	 *          Eight rank:1 value:0 trumpRank:1 trumpValue:0) (card Nine rank:2
	 *          value:0 trumpRank:6 trumpValue:14) (card Ten rank:3 value:10
	 *          trumpRank:4 trumpValue:10) (card Jack rank:4 value:2 trumpRank:7
	 *          trumpValue:20) (card Queen rank:5 value:3 trumpRank:2 trumpValue:3)
	 *          (card King rank:6 value:4 trumpRank:3 trumpValue:4) (card Ace rank:7
	 *          value:11 trumpRank:5 trumpValue:11) })
	 */
	public Deck
	(
		@Opt 	   final RoleType                   role,
		@Opt @Name final Integer                    cardsBySuit,
		@Opt @Name final Integer                    suits,
		@Opt       final game.util.equipment.Card[] cards
	)
	{
		super(null, Constants.UNDEFINED, (role == null) ? RoleType.Shared : role);

		final String className = this.getClass().toString();
		final String containerName = className.substring(className.lastIndexOf('.') + 1, className.length());

		final RoleType realRole = (role == null) ? RoleType.Shared : role;

		if (realRole.owner() > 0 && realRole.owner() <= Constants.MAX_PLAYERS)
		{
			if (name() == null)
				this.setName(containerName + realRole.owner());
		}
		else if (realRole == RoleType.Neutral)
		{
			if (name() == null)
				this.setName(containerName + realRole.owner());
		}
		else if (realRole == RoleType.Shared)
		{
			if (name() == null)
				this.setName(containerName + realRole.owner());
		}

		this.numLocs = 1;

		this.style = ContainerStyleType.Hand;
		setType(ItemType.Hand);

		this.cardsBySuit = (cardsBySuit == null && cards == null) ? Integer.valueOf(13)
				: ((cards != null) ? Integer.valueOf(cards.length) : cardsBySuit);

		this.suits = (suits == null) ? Integer.valueOf(4) : suits;

		final Integer[] valuesOfCards = (cards == null) ? null : new Integer[cards.length];
		final Integer[] trumpValuesOfCards = (cards == null) ? null : new Integer[cards.length];
		final Integer[] ranksOfCards = (cards == null) ? null : new Integer[cards.length];
		final Integer[] trumpRanksOfCards = (cards == null) ? null : new Integer[cards.length];
		final Integer[] biasedValuesOfCards = (cards == null) ? null : new Integer[cards.length];
		CardType[] typeOfCards = (cards == null) ? null : new CardType[cards.length];

		if (cards != null)
			for (int i = 0; i < cards.length; i++)
			{
				final game.util.equipment.Card card = cards[i];
				valuesOfCards[i] = Integer.valueOf(card.value());
				trumpValuesOfCards[i] = Integer.valueOf(card.trumpValue());
				ranksOfCards[i] = Integer.valueOf(card.rank());
				biasedValuesOfCards[i] = Integer.valueOf(card.biased());
				trumpRanksOfCards[i] = Integer.valueOf(card.trumpRank());
				typeOfCards[i] = card.type();
			}

		final Integer[] val = (valuesOfCards == null) ? new Integer[this.cardsBySuit.intValue()] : valuesOfCards;
		if (val[0] == null)
		{
			for (int i = 1; i <= this.cardsBySuit.intValue(); i++)
				val[i - 1] = Integer.valueOf(i);
		}

		if (typeOfCards == null)
		{
			typeOfCards = new CardType[this.cardsBySuit.intValue()];
			for (int i = 1; i <= this.cardsBySuit.intValue(); i++)
				typeOfCards[i - 1] = CardType.values()[i];
		}

		this.types = typeOfCards;
		this.values = val;
		this.trumpValues = (trumpValuesOfCards == null) ? val : trumpValuesOfCards;
		this.trumpRanks = (trumpRanksOfCards == null) ? val : trumpRanksOfCards;
		this.ranks = (ranksOfCards == null) ? val : ranksOfCards;
		this.biased = biasedValuesOfCards;
		this.indexComponent = new TIntArrayList();
	}

	//-------------------------------------------------------------------------

	/**
	 * Copy constructor.
	 *
	 * Protected because we do not want the compiler to detect it, this is called
	 * only in Clone method.
	 * 
	 * @param other
	 */
	protected Deck(final Deck other)
	{
		super(other);
		cardsBySuit = other.cardsBySuit;
		suits = other.suits;
		indexComponent = other.indexComponent;

		if (other.biased != null)
		{
			biased = new Integer[other.biased.length];
			for (int i = 0; i < other.biased.length; i++)
				biased[i] = other.biased[i];
		}
		else
			biased = null;

		if (other.ranks != null)
		{
			ranks = new Integer[other.ranks.length];
			for (int i = 0; i < other.ranks.length; i++)
				ranks[i] = other.ranks[i];
		}
		else
			ranks = null;

		if (other.values != null)
		{
			values = new Integer[other.values.length];
			for (int i = 0; i < other.values.length; i++)
				values[i] = other.values[i];
		}
		else
			values = null;

		if (other.trumpRanks != null)
		{
			trumpRanks = new Integer[other.trumpRanks.length];
			for (int i = 0; i < other.trumpRanks.length; i++)
				trumpRanks[i] = other.trumpRanks[i];
		}
		else
			trumpRanks = null;

		if (other.trumpValues != null)
		{
			trumpValues = new Integer[other.trumpValues.length];
			for (int i = 0; i < other.trumpValues.length; i++)
				trumpValues[i] = other.trumpValues[i];
		}
		else
			trumpValues = null;

		if (other.types != null)
		{
			types = new CardType[other.types.length];
			for (int i = 0; i < other.types.length; i++)
				types[i] = other.types[i];
		}
		else
			types = null;
	}

	@Override
	public Deck clone()
	{
		return new Deck(this);
	}

	/**
	 * @param indexCard
	 * @param cid
	 * @return The list of cards generated according to the deck.
	 */
	public List<Component> generateCards(final int indexCard, final int cid)
	{
		final List<Component> cards = new ArrayList<Component>();

		int i = cid;
		int cardIndex = indexCard;

		for (int indexSuit = 1; indexSuit <= suits().intValue(); indexSuit++)
			for (int indexCardSuit = 0; indexCardSuit < cardsBySuits().intValue(); indexCardSuit++)
			{
				final Card card = new Card("Card" + cardIndex, role(), types()[indexCardSuit],
						ranks()[indexCardSuit], values()[indexCardSuit], trumpRanks()[indexCardSuit],
						trumpValues()[indexCardSuit], Integer.valueOf(indexSuit), null, null, null, null);
				card.setBiased(getBiased());
				cards.add(card);
				indexComponent().add(i);
				i++;
				cardIndex++;
			}

		return cards;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Biased values.
	 */
	public Integer[] getBiased()
	{
		return biased;
	}

	/**
	 * @return Ranks of each card.
	 */
	public Integer[] ranks()
	{
		return ranks;
	}

	/**
	 * @return Values of each card.
	 */
	public Integer[] values()
	{
		return values;
	}

	/**
	 * @return The number of suits.
	 */
	public Integer suits()
	{
		return suits;
	}

	/**
	 * @return The card types.
	 */
	public CardType[] types()
	{
		return types;
	}

	/**
	 * @return The number of cards by suit.
	 */
	public Integer cardsBySuits()
	{
		return cardsBySuit;
	}

	/**
	 * @return The trump values of the cards.
	 */
	public Integer[] trumpValues()
	{
		return trumpValues;
	}

	/**
	 * @return The trump ranks of the cards.
	 */
	public Integer[] trumpRanks()
	{
		return trumpRanks;
	}

	/**
	 * @return The index of all the cards components.
	 */
	public TIntArrayList indexComponent()
	{
		return indexComponent;
	}

	@Override
	public boolean isDeck()
	{
		return true;
	}

	@Override
	public boolean isHand()
	{
		return true;
	}

	//-------------------------------------------------------------------------

	@Override
	public void createTopology(final int beginIndex, final int numEdge)
	{
		final double unit = 1.0 / numLocs;

		topology = new Topology();
		final int realNumEdge = (numEdge == Constants.UNDEFINED) ? 4 : numEdge;

		Graph graph = null;

		if (realNumEdge == 6)
			graph = new RectangleOnHex(new DimConstant(1), new DimConstant(this.numLocs)).eval(null, SiteType.Cell);
		else if (realNumEdge == 3)
			graph = new RectangleOnTri(new DimConstant(1), new DimConstant(this.numLocs)).eval(null, SiteType.Cell);
		else
			graph = new RectangleOnSquare(new DimConstant(1), new DimConstant(this.numLocs), null, null).eval(null, SiteType.Cell);

		// Add the cells to the topology.
		for (int i = 0; i < graph.faces().size(); i++)
		{
			final Face face = graph.faces().get(i);
			final Cell cell = new Cell(face.id() + beginIndex, face.pt().x() + (i * unit), face.pt().y(), face.pt().z());
			cell.setCoord(cell.row(), cell.col(), 0);
			cell.setCentroid(face.pt().x(), face.pt().y(), 0);
			topology.cells().add(cell);

			// We add the vertices of the cells and vice versa.
			for (final game.util.graph.Vertex v : face.vertices())
			{
				final double x = v.pt().x();
				final double y = v.pt().y();
				final double z = v.pt().z();
				final Vertex vertex = new Vertex(Constants.UNDEFINED, x, y, z);
				cell.vertices().add(vertex);
			}
		}

		numSites = topology.cells().size();
	}

	/**
	 * @return The number of sites on this deck container.
	 */
	public static int numLocs()
	{
		return 1;
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
						"A deck is defined in the equipment with an incorrect owner: " + role() + ".");
				missingRequirement = true;
			}
		}
		return missingRequirement;
	}
}