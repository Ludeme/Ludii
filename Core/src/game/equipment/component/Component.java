
package game.equipment.component;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import game.Game;
import game.equipment.Item;
import game.equipment.component.tile.Path;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.types.board.StepType;
import game.types.component.CardType;
import game.types.play.RoleType;
import game.util.directions.DirectionFacing;
import game.util.graph.Step;
import game.util.moves.Flips;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import main.StringRoutines;
import metadata.graphics.util.ComponentStyleType;
import other.ItemType;
import other.concept.Concept;
import other.context.Context;
import other.topology.Topology;

/**
 * Defines a component.
 *
 * @author cambolbro and Eric.Piette
 */
@SuppressWarnings("static-method")
public class Component extends Item implements Cloneable
{
	/** Current direction that piece is facing. */
	private DirectionFacing dirn;

	/** Optional generator which creates moves for this piece on demand */
	private Moves generator;

	/** The pre-generation of the moves of the generator in an empty board. */
	private boolean[][] potentialMoves;

	/** In case of large Piece, the walk to describe the shape of the piece. */
	private final StepType[][] walk;
	
	/** Use to keep the label without the role of the player for graphics. */
	protected String nameWithoutNumber;
	
	/** The style of the component. */
	protected ComponentStyleType style;

	/** Bias values for dice, cards, etc. */
	protected int[] bias;
	
	/** The maximum local state the game should check. */
	private final int maxState;

	/** The maximum count the game should check. */
	private final int maxCount;

	/** The maximum value the game should check. */
	private final int maxValue;

	//-------------------------------------------------------------------------

	/**
	 * @param label     The name of the component.
	 * @param role      The owner of the component.
	 * @param walk      The walk used to generate a large piece.
	 * @param dirn      The direction where face the component.
	 * @param generator The moves associated with the component.
	 * @param maxState  To set the maximum local state the game should check.
	 * @param maxCount  To set the maximum count the game should check.
	 * @param maxValue  To set the maximum value the game should check.
	 */
	@Hide
	public Component
	(
		final String          label,
		final RoleType        role,
		final StepType[][]    walk,
		final DirectionFacing dirn,
		final Moves           generator,
		final Integer         maxState,
		final Integer         maxCount,
		final Integer         maxValue
	)
	{
		super(label, Constants.UNDEFINED, role);
		
		this.walk = walk;
		this.dirn = (dirn == null) ? null : dirn;
		this.generator = generator;
		setType(ItemType.Component);
		this.maxState = (maxState != null) ? maxState.intValue() : Constants.OFF;
		this.maxCount = (maxCount != null) ? maxCount.intValue() : Constants.OFF;
		this.maxValue = (maxValue != null) ? maxValue.intValue() : Constants.OFF;
	}

	//-------------------------------------------------------------------------


	/**
	 * @return direction.
	 */
	public DirectionFacing getDirn()
	{
		return dirn;
	}

	/**
	 * @return The first value (for dominoes).
	 */
	public int getValue()
	{
		return Constants.UNDEFINED;
	}

	/**
	 * @return The flips values.
	 */
	public Flips getFlips()
	{
		return null;
	}

	/**
	 * @return Optional generator of moves for pieces
	 */
	public Moves generator()
	{
		return generator;
	}

	/**
	 * @return The maximum local state for that component.
	 */
	public int maxState()
	{
		return maxState;
	}

	/**
	 * @return The maximum count for that component.
	 */
	public int maxCount()
	{
		return maxCount;
	}

	/**
	 * @return The maximum value for that component.
	 */
	public int maxValue()
	{
		return maxValue;
	}

	/**
	 * @param context
	 * @return List of moves, possibly empty.
	 */
	public Moves generate(final Context context)
	{
		if (generator != null)
			return generator.eval(context);
		return new BaseMoves(null);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof Component))
			return false;
		
		final Component comp = (Component)o;
		return name().equals(comp.name());
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
	protected Component(final Component other)
	{
		super(other);
		dirn = other.dirn;
		generator = other.generator;

		if (other.bias != null)
		{
			bias = new int[other.bias.length];
			for (int i = 0; i < other.bias.length; i++)
				bias[i] = other.bias[i];
		}
		else
			bias = null;

		if (other.potentialMoves != null)
		{
			potentialMoves = new boolean[other.potentialMoves.length][];
			for (int i = 0; i < other.potentialMoves.length; i++)
			{
				potentialMoves[i] = new boolean[other.potentialMoves[i].length];
				for (final int j = 0; j < other.potentialMoves[i].length; i++)
					potentialMoves[i][j] = other.potentialMoves[i][j];
			}
		}
		else
			potentialMoves = null;

		if (other.walk != null)
		{
			walk = new StepType[other.walk.length][];
			for (int i = 0; i < other.walk.length; i++)
			{
				walk[i] = new StepType[other.walk[i].length];
				for (int j = 0; j < other.walk[i].length; j++)
					walk[i][j] = other.walk[i][j];
			}
		}
		else
			walk = null;
		
		style = other.style;

		nameWithoutNumber = other.nameWithoutNumber;
		maxState = other.maxState;
		maxCount = other.maxCount;
		maxValue = other.maxValue;
	}

	@Override
	public Component clone() 
	{
		return new Component(this);
	}

	//-------------------------------------------------------------------------
	


	/**
	 * Set the direction of the piece.
	 * 
	 * @param direction
	 */
	public void setDirection(final DirectionFacing direction)
	{
		dirn = direction;
	}

	/**
	 * To clone the generator.
	 * 
	 * @param generator
	 */
	public void setMoves(final Moves generator)
	{
		this.generator=generator;
	}

	@Override
	public int hashCode()
	{
		return super.hashCode();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return True if this is a large piece.
	 */
	public boolean isLargePiece()
	{
		return walk != null;
	}

	/**
	 * @return The walk in case of large piece.
	 */
	public StepType[][] walk()
	{
		return walk;
	}

	/**
	 * @param context  The context of the game.
	 * @param startLoc The root of the large piece.
	 * @param state    The local state of the root.
	 * @param topology The graph of the board.
	 * 
	 * @return The locs occupied by a large piece from a location according to the
	 *         state.
	 */
	public TIntArrayList locs(final Context context, final int startLoc, final int state, final Topology topology)
	{
		final int from = startLoc;

		if (from >= topology.cells().size())
			return new TIntArrayList();

		final TIntArrayList sitesAfterWalk = new TIntArrayList();
		final int realState = (state >= 0) ? state : 0;

		final List<DirectionFacing> orthogonalSupported = topology.supportedOrthogonalDirections(SiteType.Cell);
		
		if(orthogonalSupported.size() <= 0)
			return sitesAfterWalk;
		
		final DirectionFacing startDirection = orthogonalSupported.get(realState % orthogonalSupported.size());

		sitesAfterWalk.add(from);
		final int indexWalk = realState / orthogonalSupported.size();
		
		if(indexWalk >= walk.length)
			return sitesAfterWalk;
		
		final StepType[] steps = walk[indexWalk];

		int currentLoc = from;
		DirectionFacing currentDirection = startDirection;

		for (final StepType step : steps)
		{
			if (step == StepType.F)
			{
				final List<Step> stepsDirection = topology.trajectories().steps(SiteType.Cell, currentLoc, currentDirection.toAbsolute());

				int to = Constants.UNDEFINED;
				for (final Step stepDirection : stepsDirection)
				{
					if (stepDirection.from().siteType() != stepDirection.to().siteType())
						continue;

					to = stepDirection.to().id();
				}

				// No correct walk with that state.
				if (to == Constants.UNDEFINED)
					return new TIntArrayList();

				if (!sitesAfterWalk.contains(to))
					sitesAfterWalk.add(to);
				
				currentLoc = to;
			}
			else if (step == StepType.R)
			{
				currentDirection = currentDirection.right();
				while (!orthogonalSupported.contains(currentDirection))
					currentDirection = currentDirection.right();
			}

			else if (step == StepType.L)
			{
				currentDirection = currentDirection.left();
				while (!orthogonalSupported.contains(currentDirection))
					currentDirection = currentDirection.left();
			}
		}

		return sitesAfterWalk;
	}

	/**
	 * @return The biased values of a die.
	 */
	public int[] getBias()
	{
		return bias;
	}

	/**
	 * To set the biased values of a die.
	 * 
	 * @param biased The biased values.
	 */
	public void setBiased(final Integer[] biased)
	{
		if (biased != null)
		{
			bias = new int[biased.length];
			for(int i = 0; i < biased.length; i++)
				bias[i] = biased[i].intValue();
		}
	}

	/**
	 * @param from
	 * @param to
	 * @return True if a move is potentially legal.
	 */
	public boolean possibleMove(final int from, final int to)
	{
		return potentialMoves[from][to];
	}

	/**
	 * @return Full matrix of all potential moves for this component type.
	 */
	public boolean[][] possibleMoves()
	{
		return potentialMoves;
	}

	/**
	 * To set the possible moves.
	 *
	 * @param possibleMoves
	 */
	public void setPossibleMove(final boolean[][] possibleMoves)
	{
		potentialMoves = possibleMoves;
	}

	//---------------------DIE--------------------------------

	/**
	 * @return True if this component is a die.
	 */
	public boolean isDie()
	{
		return false;
	}

	/**
	 * @return The faces of a die.
	 */
	public int[] getFaces()
	{
		return new int[0];
	}

	/**
	 * @return The number of faces of a die.
	 */
	public int getNumFaces()
	{
		return Constants.UNDEFINED;
	}

	/**
	 * For rolling a die and returning its new value.
	 * 
	 * @param context The context.
	 * @return The new value.
	 */
	public int roll(final Context context)
	{
		return Constants.OFF;
	}

	/**
	 * To set the faces of a die.
	 * 
	 * @param faces The faces of the die.
	 * @param start The value corresponding to the first face.
	 */
	public void setFaces(final Integer[] faces, final Integer start)
	{
		// Nothing to do.
	}

	//---------------------CARD--------------------------------

	/**
	 * @return True if this component is a card.
	 */
	public boolean isCard()
	{
		return false;
	}

	/**
	 * @return The suit value of a component.
	 */
	public int suit()
	{
		return Constants.OFF;
	}

	/**
	 * @return The trump value of a component.
	 */
	public int trumpValue()
	{
		return Constants.OFF;
	}

	/**
	 * @return The rank value of a component.
	 */
	public int rank()
	{
		return Constants.OFF;
	}

	/**
	 * @return The trump rank of a component.
	 */
	public int trumpRank()
	{
		return Constants.OFF;
	}
	
	/**
	 * @return The cardtype of the component.
	 */
	public CardType cardType()
	{
		return null;
	}

	//---------------------TILE--------------------------------

	/**
	 * @return True if this is a tile piece.
	 */
	public boolean isTile()
	{
		return false;
	}

	/**
	 * @return The terminus of a tile pieces
	 */
	public int[] terminus()
	{
		return null;
	}

	/**
	 * @return The number of terminus of a tile pieces if this number is equal for
	 *         all the sides.
	 */

	public Integer numTerminus()
	{
		return Integer.valueOf(Constants.OFF);
	}

	/**
	 * @return The number of sides of a tile piece.
	 */
	public int numSides()
	{
		return Constants.OFF;
	}

	/**
	 * To set the number of sides of a tile piece.
	 * 
	 * @param numSides The number of sides.
	 */
	public void setNumSides(final int numSides)
	{
		// Nothing to do.
	}

	/**
	 * @return The paths of a Tile piece.
	 */

	public Path[] paths()
	{
		return null;
	}

	/**
	 * @param game The game.
	 * @return Accumulated flags for ludeme.
	 */
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		if (dirn != null)
			concepts.set(Concept.PieceDirection.id(), true);
		return new BitSet();
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (generator != null)
			writeEvalContext.or(generator.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (generator != null)
			readEvalContext.or(generator.readsEvalContextRecursive());
		return readEvalContext;
	}

	//---------------------DOMINO--------------------------------

	/**
	 * @return If the domino is a double
	 */
	public boolean isDoubleDomino()
	{
		return false;
	}

	/**
	 * @return The second value (for Domino).
	 */
	public int getValue2()
	{
		return Constants.OFF;
	}

	/**
	 * @return True if this component is a domino.
	 */
	public boolean isDomino()
	{
		return false;
	}
	
	/**
	 * @return name of the piece without the owner.
	 */
	public String getNameWithoutNumber()
	{
		return nameWithoutNumber;
	}
	
	/**
	 * Set name of the piece without the owner.
	 * 
	 * @param name The name of the piece.
	 */
	public void setNameWithoutNumber(final String name)
	{
		nameWithoutNumber = name;
	}
	
	/**
	 * @return The ComponentStyleType.
	 */
	public ComponentStyleType style()
	{
		return style;
	}

	/**
	 * @param st
	 */
	public void setStyle(final ComponentStyleType st)
	{
		style = st;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String credit()
	{
		//-----------------------------------------------------
		// Common/res/img/svg
		
		if (nameWithoutNumber.equalsIgnoreCase("Sandbox"))
			return nameWithoutNumber + " image from https://www.pngwing.com/en/free-png-nmodo.";

		//-----------------------------------------------------
		// Common/res/img/svg/animals
		
		if (nameWithoutNumber.equalsIgnoreCase("Bear"))
			return nameWithoutNumber + " image by Freepik from http://www.flaticon.com.";
			
		if (nameWithoutNumber.equalsIgnoreCase("Seal"))
			return nameWithoutNumber + " image by Freepik from http://www.flaticon.com.";
			
		if (nameWithoutNumber.equalsIgnoreCase("Camel"))
			return nameWithoutNumber + " image from https://www.pngrepo.com/svg/297513/camel.";
			
		if (nameWithoutNumber.equalsIgnoreCase("Cat"))
			return nameWithoutNumber + " image from http://getdrawings.com/cat-head-icon#cat-head-icon-8.png.";
			
		if (nameWithoutNumber.equalsIgnoreCase("Chicken"))
			return nameWithoutNumber + " image by Stila from https://favpng.com/png_view/.";
			
		if (nameWithoutNumber.equalsIgnoreCase("Cow"))
			return nameWithoutNumber + " image from https://www.nicepng.com/ourpic/u2w7o0t4e6y3a9u2_animals-chinese-new-year-icon/.";
			
		if (nameWithoutNumber.equalsIgnoreCase("Dog"))
			return nameWithoutNumber + " image from https://favpng.com/png_view/albatross-gray-wolf-clip-art-png/R6VmvfkC.";
			
		if (nameWithoutNumber.equalsIgnoreCase("Crab"))
			return nameWithoutNumber + " image by Freepik from http://www.flaticon.com.";
			
		if (nameWithoutNumber.equalsIgnoreCase("Dog"))
			return nameWithoutNumber + " image from https://www.pngwing.com/en/free-png-hwzbd.";
			
		if (nameWithoutNumber.equalsIgnoreCase("Dove"))
			return nameWithoutNumber + " image from https://www.pngwing.com/en/free-png-xxwye.";
			
		if (nameWithoutNumber.equalsIgnoreCase("Dragon"))
			return nameWithoutNumber + " image from https://ya-webdesign.com/imgdownload.html";
			
		if (nameWithoutNumber.equalsIgnoreCase("Duck"))
			return nameWithoutNumber + " image from https://ya-webdesign.com/imgdownload.html";
			
		if (nameWithoutNumber.equalsIgnoreCase("Eagle"))
			return nameWithoutNumber + " image from https://www.pngbarn.com/png-image-tgmlh.";
			
		if (nameWithoutNumber.equalsIgnoreCase("Elephant"))
			return nameWithoutNumber + " image from http://getdrawings.com/get-icon#elephant-icon-app-2.png.";
			
		if (nameWithoutNumber.equalsIgnoreCase("Fish"))
			return nameWithoutNumber + " image from https://www.svgrepo.com/svg/109765/fish."; 

		if (nameWithoutNumber.equalsIgnoreCase("Chick"))
			return nameWithoutNumber + " image from https://www.svgrepo.com/svg/123529/bird.";

		if (nameWithoutNumber.equalsIgnoreCase("Hyena"))
			return nameWithoutNumber + " image from https://www.svgrepo.com/svg/1841/hyena-head.";
			
		if (nameWithoutNumber.equalsIgnoreCase("Fox"))
			return nameWithoutNumber + " image from https://www.svgrepo.com/svg/40267/fox.";
			
		if (nameWithoutNumber.equalsIgnoreCase("Goat"))
			return nameWithoutNumber + " image from https://ya-webdesign.com/imgdownload.html.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Goose"))
			return nameWithoutNumber + " image from https://depositphotos.com/129413072/stock-illustration-web-goose-icon.html.";
			
		if (nameWithoutNumber.equalsIgnoreCase("Hare"))
			return nameWithoutNumber + " image by Freepik from https://www.flaticon.com/free-icon/.";
			
		if (nameWithoutNumber.equalsIgnoreCase("Horse"))
			return nameWithoutNumber + " image from https://commons.wikimedia.org/wiki/File:Chess_tile_nl.svg.";
			
		if (nameWithoutNumber.equalsIgnoreCase("Jaguar"))
			return nameWithoutNumber + " image from https://icons8.com/icons/set/jaguar.";

		if (nameWithoutNumber.equalsIgnoreCase("Lamb"))
			return nameWithoutNumber + " image from https://ya-webdesign.com/imgdownload.html.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Leopard"))
			return nameWithoutNumber + " image from https://www.svgrepo.com/svg/297517/leopard.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Lion"))
			return nameWithoutNumber + " image by Freepik from https://www.flaticon.com/free-icon/.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Lioness"))
			return nameWithoutNumber + " image by Freepik from https://www.flaticon.com/free-icon/.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Monkey"))
			return nameWithoutNumber + " image from https://www.pngbarn.com/png-image-eonln.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Mountainlion"))
			return nameWithoutNumber + " image by Tae S Yang from https://icon-icons.com/nl/pictogram/puma-dier/123525.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Mouse"))
			return nameWithoutNumber + " image by Freepik from https://www.flaticon.com/free-icon/mouse_235093.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Ox"))
			return nameWithoutNumber + " image from https://www.svgrepo.com/svg/19280/cattle-skull.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Panther"))
			return nameWithoutNumber + " image by Freepik from https://www.flaticon.com/free-icon/cat-face-outline_57104.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Penguin"))
			return nameWithoutNumber + " image from https://ya-webdesign.com/imgdownload.html.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Prawn"))
			return nameWithoutNumber + " image by Freepik from https://www.flaticon.com/free-icon/prawn_202274.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Puma"))
			return nameWithoutNumber + " image by Tae S Yang from https://icon-icons.com/nl/pictogram/puma-dier/123525.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Rabbit"))
			return nameWithoutNumber + " image from https://ya-webdesign.com/imgdownload.html.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Rat"))
			return nameWithoutNumber + " image from https://webstockreview.net/image/clipart-rat-head-cartoon/642646.html.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Rhino"))
			return nameWithoutNumber + " image by Freepik from https://www.flaticon.com/free-icon/.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Seal"))
			return nameWithoutNumber + " image by Freepik from https://www.flaticon.com/free-icon/.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Sheep"))
			return nameWithoutNumber + " image from https://www.pngwing.com/en/free-png-nirzv.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Snake"))
			return nameWithoutNumber + " image by Freepik from https://www.flaticon.com/free-icon/.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Tiger"))
			return nameWithoutNumber + " image from https://www.pngwing.com/en/free-png-hbgdy.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Wolf"))
			return nameWithoutNumber + " image by Freepik from https://www.flaticon.com.";
		
		
		//-----------------------------------------------------
		// Common/res/img/svg/cards
		
		if (nameWithoutNumber.equalsIgnoreCase("Jack"))
			return nameWithoutNumber + " image by popicon from https://www.shutterstock.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Joker"))
			return nameWithoutNumber + " image \"Joker Icon\" from https://icons8.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("King"))
			return nameWithoutNumber + " image from https://www.pngwing.com/en/free-png-ptuag.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Queen"))
			return nameWithoutNumber + " image from https://www.pngguru.com/free-transparent-background-png-clipart-tlaxu.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Card-suit-club"))
			return nameWithoutNumber + " image from https://en.wikipedia.org/wiki/File:Card_club.svg.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Card-suit-diamond"))
			return nameWithoutNumber + " image from https://en.wikipedia.org/wiki/File:Card_diamond.svg.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Card-suit-heart"))
			return nameWithoutNumber + " image from https://en.wikipedia.org/wiki/File:Card_heart.svg.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Card-suit-spade"))
			return nameWithoutNumber + " image from https://en.wikipedia.org/wiki/File:Card_spade.svg.";
		
		if (nameWithoutNumber.equalsIgnoreCase("CardBack"))
			return nameWithoutNumber + " image from Symbola TTF font.";

		//-----------------------------------------------------
		// Common/res/img/svg/checkers
		
		if 
		(
			nameWithoutNumber.equalsIgnoreCase("counter")
			||
			nameWithoutNumber.equalsIgnoreCase("counterstar")
			||
			nameWithoutNumber.equalsIgnoreCase("doublecounter")
		)
			return nameWithoutNumber + " image from Symbola TTF font.";

		//-----------------------------------------------------
		// Common/res/img/svg/chess
				
		if 
		(
			nameWithoutNumber.equalsIgnoreCase("Bishop")
			||
			nameWithoutNumber.equalsIgnoreCase("King")
			||
			nameWithoutNumber.equalsIgnoreCase("Knight")
			||
			nameWithoutNumber.equalsIgnoreCase("Pawn")
			||
			nameWithoutNumber.equalsIgnoreCase("Queen")
			||
			nameWithoutNumber.equalsIgnoreCase("Rook")
		)
			return 	nameWithoutNumber + " images from the Casefont, Arial Unicode MS, PragmataPro and Symbola TTF fonts.";
		
		//-----------------------------------------------------
		// Common/res/img/svg/faces
		
		if 
		(
			nameWithoutNumber.equalsIgnoreCase("symbola_cool")
			||
			nameWithoutNumber.equalsIgnoreCase("symbola_happy")
			||
			nameWithoutNumber.equalsIgnoreCase("symbola_neutral")
			||
			nameWithoutNumber.equalsIgnoreCase("symbola_pleased")
			||
			nameWithoutNumber.equalsIgnoreCase("symbola_sad")
			||
			nameWithoutNumber.equalsIgnoreCase("symbola_scared")
			||
			nameWithoutNumber.equalsIgnoreCase("symbola_worried")
		)
			return nameWithoutNumber + " image from Symbola TTF font.";

		//-----------------------------------------------------
		// Common/res/img/svg/fairyChess
		
		if (nameWithoutNumber.equalsIgnoreCase("Amazon"))
			return nameWithoutNumber + " image from images from the Arial Unicode MS, PragmataPro and Symbola \\n\"\n" +
					"TTF fonts and https://www.pngwing.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Bishop_noCross"))
			return nameWithoutNumber + " images from the Arial Unicode MS, PragmataPro and Symbola \\n\"\n" +
					"TTF fonts and https://www.pngwing.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Boat"))
			return nameWithoutNumber + " image by Freepik from https://www.flaticon.com/free-icon/viking-ship_22595.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Cannon"))
			return nameWithoutNumber + " image from https://www.pngbarn.com/.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Chariot"))
			return nameWithoutNumber + " image by Freepik from https://www.flaticon.com/free-icon/wheel_317722.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Commoner"))
			return nameWithoutNumber + " image by Sunny3113 from \n" + 
					"https://commons.wikimedia.org/wiki/File:Commoner_Transparent.svg \n" + 
					"under license https://creativecommons.org/licenses/by-sa/4.0/deed.en.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Ferz_noCross"))
			return 	nameWithoutNumber + " images from the Arial Unicode MS, PragmataPro and Symbola \n" +
					"TTF fonts and https://www.pngwing.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Ferz"))
			return 	nameWithoutNumber + " images from the Arial Unicode MS, PragmataPro and Symbola \n" +
					"TTF fonts and https://www.pngwing.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Flag"))
			return nameWithoutNumber + " image from https://www.pngwing.com/en/free-png-siuwt.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Fool"))
			return nameWithoutNumber + " image by Mykola Dolgalov based on Omega Chess Advanced from \n" + 
										"https://commons.wikimedia.org/wiki/File:Chess_tll45.svg under \n" + 
										"license https://creativecommons.org/licenses/by-sa/3.0/deed.en.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Giraffe"))
			return nameWithoutNumber + " image from https://www.pngfuel.com/free-png/tfali.";

		if (nameWithoutNumber.equalsIgnoreCase("King_noCross"))
			return nameWithoutNumber + " images from the Arial Unicode MS, PragmataPro and Symbola \\n\"\n" +
					"TTF fonts and https://www.pngwing.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Knight_bishop"))
			return nameWithoutNumber + " image by OMega Chess Fan derivative work of NikNaks93 from \n" + 
					"https://en.wikipedia.org/wiki/Princess_(chess)#/media/File:Chess_alt45.svg under \n" + 
					"license https://creativecommons.org/licenses/by-sa/3.0/.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Knight_king"))
			return nameWithoutNumber + " image from https://www.pngwing.com/en/free-png-ynnmd.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Knight_queen"))
			return nameWithoutNumber + " image bu NikNaks from https://commons.wikimedia.org/wiki/File:Chess_Alt26.svg \n" + 
					"under license https://creativecommons.org/licenses/by-sa/3.0/deed.en.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Knight_rook"))
			return nameWithoutNumber + " image byfrom https://en.wikipedia.org/wiki/Empress_(chess)#/media/File:Chess_clt45.svg.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Knight-rotated"))
			return nameWithoutNumber + " image from the Arial Unicode MS, PragmataPro and Symbola \\n\"\n" +
					"TTF fonts and https://www.pngwing.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Mann"))
			return nameWithoutNumber + " image by CheChe from the original by LithiumFlash from \n" + 
					"https://commons.wikimedia.org/wiki/File:Chess_Mlt45.svg.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Moon"))
			return nameWithoutNumber + " image from https://www.freeiconspng.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Unicorn"))
			return nameWithoutNumber + " image by CBurnett and Francois-Pier from \n" + 
					"https://commons.wikimedia.org/wiki/File:Chess_Ult45.svg under \n" + 
					"license https://www.gnu.org/licenses/gpl-3.0.html.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Wazir"))
			return nameWithoutNumber + " images from the Arial Unicode MS, PragmataPro and Symbola \\n\"\n" +
					"TTF fonts and https://www.pngwing.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Zebra-neck"))
			return nameWithoutNumber + " image from https://imgbin.com/png/qH6bNDwM/.";
		
		if (nameWithoutNumber.equalsIgnoreCase("Zebra"))
			return nameWithoutNumber + " image by Francois-PIer after CBurnett from \n" + 
					"https://commons.wikimedia.org/wiki/File:Chess_Zlt45.svg under \n" + 
					"license https://creativecommons.org/licenses/by-sa/3.0/deed.en.";
		
		//-----------------------------------------------------
		// Common/res/img/svg/hands
		
		if 
		(
			nameWithoutNumber.equalsIgnoreCase("hand0") 
			||
			nameWithoutNumber.equalsIgnoreCase("hand1") 
			||
			nameWithoutNumber.equalsIgnoreCase("hand2") 
			||
			nameWithoutNumber.equalsIgnoreCase("hand3") 
			||
			nameWithoutNumber.equalsIgnoreCase("hand4") 
			||
			nameWithoutNumber.equalsIgnoreCase("hand5") 
			||
			nameWithoutNumber.equalsIgnoreCase("paper") 
			||
			nameWithoutNumber.equalsIgnoreCase("rock") 
			||
			nameWithoutNumber.equalsIgnoreCase("scissors")
		)
			return nameWithoutNumber + " image based on \"Click - Index Finger Clip Art\" by Adanteh \n" + 
		     		"from https://favpng.com/png_view/click-index-finger-clip-art-png/NJXExGMM.";

		//-----------------------------------------------------
		// Common/res/img/svg/hieroglyphs
		
		if 
		(
			nameWithoutNumber.equalsIgnoreCase("2human_knee")
			||
			nameWithoutNumber.equalsIgnoreCase("2human")
			||
			nameWithoutNumber.equalsIgnoreCase("3ankh_side")
			||
			nameWithoutNumber.equalsIgnoreCase("3ankh")
			||
			nameWithoutNumber.equalsIgnoreCase("3bird")
			||
			nameWithoutNumber.equalsIgnoreCase("3nefer")
			||
			nameWithoutNumber.equalsIgnoreCase("ankh_waset")
			||
			nameWithoutNumber.equalsIgnoreCase("water")
			||
			nameWithoutNumber.equalsIgnoreCase("senetpiece")
			||
			nameWithoutNumber.equalsIgnoreCase("senetpiece2")
		)
			return nameWithoutNumber + " image part of the AegyptusSubset TTF font , from:\n" + 
					"https://mjn.host.cs.st-andrews.ac.uk/egyptian/fonts/newgardiner.html.";

		//-----------------------------------------------------
		// Common/res/img/svg/janggi
		
		if 
		(
			nameWithoutNumber.equalsIgnoreCase("Byeong")
			||
			nameWithoutNumber.equalsIgnoreCase("Cha")
			||
			nameWithoutNumber.equalsIgnoreCase("Cho")
			||
			nameWithoutNumber.equalsIgnoreCase("Han")
			||
			nameWithoutNumber.equalsIgnoreCase("Jol")
			||
			nameWithoutNumber.equalsIgnoreCase("Majanggi")
			||
			nameWithoutNumber.equalsIgnoreCase("Po")
			||
			nameWithoutNumber.equalsIgnoreCase("Sa")
			||
			nameWithoutNumber.equalsIgnoreCase("Sang")
		)
			return nameWithoutNumber + " image created by Matthew Stephenson for Ludii from the Casefont TTF font.";

		//-----------------------------------------------------
		// Common/res/img/svg/letters
		
		if (nameWithoutNumber.length() == 1)
		{
			final char ch = Character.toUpperCase(nameWithoutNumber.charAt(0));
			if (ch >= 'A' && ch <= 'Z')
				return nameWithoutNumber + " image from the Arial TTF font.";
			else if (ch >= '9' && ch <= '0')
				return nameWithoutNumber + " image from the Arial TTF font.";
		}
		
		//-----------------------------------------------------
		// Common/res/img/svg/mahjong
		
		if 
		(
			nameWithoutNumber.equalsIgnoreCase("BambooOne") 
			||
			nameWithoutNumber.equalsIgnoreCase("BambooTwo") 
			||
			nameWithoutNumber.equalsIgnoreCase("BambooThree") 
			||
			nameWithoutNumber.equalsIgnoreCase("BambooFour") 
			||
			nameWithoutNumber.equalsIgnoreCase("BambooFive") 
			||
			nameWithoutNumber.equalsIgnoreCase("BambooSix") 
			||
			nameWithoutNumber.equalsIgnoreCase("BambooSeven") 
			||
			nameWithoutNumber.equalsIgnoreCase("BambooEight") 
			||
			nameWithoutNumber.equalsIgnoreCase("BambooNine") 
			||
			nameWithoutNumber.equalsIgnoreCase("CharacterOne") 
			||
			nameWithoutNumber.equalsIgnoreCase("CharacterTwo") 
			||
			nameWithoutNumber.equalsIgnoreCase("CharacterThree") 
			||
			nameWithoutNumber.equalsIgnoreCase("CharacterFour") 
			||
			nameWithoutNumber.equalsIgnoreCase("CharacterFive") 
			||
			nameWithoutNumber.equalsIgnoreCase("CharacterSix") 
			||
			nameWithoutNumber.equalsIgnoreCase("CharacterSeven") 
			||
			nameWithoutNumber.equalsIgnoreCase("CharacterEight") 
			||
			nameWithoutNumber.equalsIgnoreCase("CharacterNine") 
			||
			nameWithoutNumber.equalsIgnoreCase("CircleOne") 
			||
			nameWithoutNumber.equalsIgnoreCase("CircleTwo") 
			||
			nameWithoutNumber.equalsIgnoreCase("CircleThree") 
			||
			nameWithoutNumber.equalsIgnoreCase("CircleFour") 
			||
			nameWithoutNumber.equalsIgnoreCase("CircleFive") 
			||
			nameWithoutNumber.equalsIgnoreCase("CircleSix") 
			||
			nameWithoutNumber.equalsIgnoreCase("CircleSeven") 
			||
			nameWithoutNumber.equalsIgnoreCase("CircleEight") 
			||
			nameWithoutNumber.equalsIgnoreCase("CircleNine") 
			||
			nameWithoutNumber.equalsIgnoreCase("DragonGreen") 
			||
			nameWithoutNumber.equalsIgnoreCase("DragonRed") 
			||
			nameWithoutNumber.equalsIgnoreCase("DragonWhite") 
			||
			nameWithoutNumber.equalsIgnoreCase("FlowerBamboo") 
			||
			nameWithoutNumber.equalsIgnoreCase("FlowerChrysanthemum") 
			||
			nameWithoutNumber.equalsIgnoreCase("FlowerOrchid") 
			||
			nameWithoutNumber.equalsIgnoreCase("FlowerPlum") 
			||
			nameWithoutNumber.equalsIgnoreCase("SeasonAutumn") 
			||
			nameWithoutNumber.equalsIgnoreCase("SeasonSpring") 
			||
			nameWithoutNumber.equalsIgnoreCase("SeasonSummer") 
			||
			nameWithoutNumber.equalsIgnoreCase("SeasonWinter") 
			||
			nameWithoutNumber.equalsIgnoreCase("TileBack") 
			||
			nameWithoutNumber.equalsIgnoreCase("TileJoker") 
			||
			nameWithoutNumber.equalsIgnoreCase("WindEast") 
			||
			nameWithoutNumber.equalsIgnoreCase("WindNorth") 
			||
			nameWithoutNumber.equalsIgnoreCase("WindSouth") 
			||
			nameWithoutNumber.equalsIgnoreCase("WindWest")
		)
			return nameWithoutNumber + " image from the Symbola TTF font.";

		//-----------------------------------------------------
		// Common/res/img/svg/misc
		
		if (nameWithoutNumber.equalsIgnoreCase("bean"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("crown"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("bike"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("bread"))
			return nameWithoutNumber + " image from svgrepo.com.";

		if (nameWithoutNumber.equalsIgnoreCase("car"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("castle"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("cone"))
			return nameWithoutNumber + " image from svgrepo.com.";

		if (nameWithoutNumber.equalsIgnoreCase("corn"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("cross"))
			return nameWithoutNumber + " image from svgrepo.com.";

		if (nameWithoutNumber.equalsIgnoreCase("minus"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("diamond"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("disc"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("discDouble"))
			return nameWithoutNumber + " edited image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("discDoubleStick"))
			return nameWithoutNumber + " edited image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("discStick"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("dot"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("egyptLion"))
			return nameWithoutNumber + " image part of the AegyptusSubset TTF font, from:\n" + 
					"https://mjn.host.cs.st-andrews.ac.uk/egyptian/fonts/newgardiner.html.";
		
		if (nameWithoutNumber.equalsIgnoreCase("fan"))
			return nameWithoutNumber + " image created by Dale Walton.";

		if (nameWithoutNumber.equalsIgnoreCase("flower"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("flowerHalf1"))
			return nameWithoutNumber + " edited image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("flowerHalf2"))
			return nameWithoutNumber + " edited image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("hex"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("hexE"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("heptagon"))
			return nameWithoutNumber + " image from https://commons.wikimedia.org/wiki/File:Heptagon.svg.";

		if (nameWithoutNumber.equalsIgnoreCase("none"))
			return nameWithoutNumber + " edited image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("octagon"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("paddle"))
			return nameWithoutNumber + " edited image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("pentagon"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("pyramid"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("rectangle"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("square"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("star"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("starOutline"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("thinCross"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("triangle"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("urpiece"))
			return nameWithoutNumber + " image created by Matthew Stephenson for Ludii.";
		
		if (nameWithoutNumber.equalsIgnoreCase("waves"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("oldMan"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("boy"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("theseus"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("minotaur"))
			return nameWithoutNumber + " image from https://www.flaticon.com/free-icon/minotaur_1483069.";
		
		if (nameWithoutNumber.equalsIgnoreCase("robot"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("door"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("human"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		if (nameWithoutNumber.equalsIgnoreCase("rubble"))
			return nameWithoutNumber + " image from svgrepo.com.";
		
		//-----------------------------------------------------
		// Common/res/img/svg/ploy
		
		if 
		(
			nameWithoutNumber.equalsIgnoreCase("Commander") 
			||
			nameWithoutNumber.equalsIgnoreCase("LanceT") 
			||
			nameWithoutNumber.equalsIgnoreCase("LanceW") 
			||
			nameWithoutNumber.equalsIgnoreCase("LanceY") 
			||
			nameWithoutNumber.equalsIgnoreCase("ProbeBigV") 
			||
			nameWithoutNumber.equalsIgnoreCase("ProbeMinV") 
			||
			nameWithoutNumber.equalsIgnoreCase("Shield")
		)
			return nameWithoutNumber + " image created by Matthew Stephenson for Ludii.";
			
		//-----------------------------------------------------
		// Common/res/img/svg/salta
		
		if 
		(
			nameWithoutNumber.equalsIgnoreCase("Salta1Dot") 
			||
			nameWithoutNumber.equalsIgnoreCase("Salta2Dot") 
			||
			nameWithoutNumber.equalsIgnoreCase("Salta3Dot") 
			||
			nameWithoutNumber.equalsIgnoreCase("Salta4Dot") 
			||
			nameWithoutNumber.equalsIgnoreCase("Salta5Dot") 
			||
			nameWithoutNumber.equalsIgnoreCase("Salta1Moon") 
			||
			nameWithoutNumber.equalsIgnoreCase("Salta2Moon") 
			||
			nameWithoutNumber.equalsIgnoreCase("Salta3Moon") 
			||
			nameWithoutNumber.equalsIgnoreCase("Salta4Moon") 
			||
			nameWithoutNumber.equalsIgnoreCase("Salta5Moon") 
			||
			nameWithoutNumber.equalsIgnoreCase("Salta1Star") 
			||
			nameWithoutNumber.equalsIgnoreCase("Salta2Star") 
			||
			nameWithoutNumber.equalsIgnoreCase("Salta3Star") 
			||
			nameWithoutNumber.equalsIgnoreCase("Salta4Star") 
			||
			nameWithoutNumber.equalsIgnoreCase("Salta5Star")
		)
			return nameWithoutNumber + " image created by Matthew Stephenson for Ludii.";
			
		//-----------------------------------------------------
		// Common/res/img/svg/shogi
		
		if 
		(
			nameWithoutNumber.equalsIgnoreCase("fuhyo") 
			||
			nameWithoutNumber.equalsIgnoreCase("ginsho") 
			||
			nameWithoutNumber.equalsIgnoreCase("hisha") 
			||
			nameWithoutNumber.equalsIgnoreCase("kakugyo") 
			||
			nameWithoutNumber.equalsIgnoreCase("keima") 
			||
			nameWithoutNumber.equalsIgnoreCase("kinsho") 
			||
			nameWithoutNumber.equalsIgnoreCase("kyosha") 
			||
			nameWithoutNumber.equalsIgnoreCase("narigin") 
			||
			nameWithoutNumber.equalsIgnoreCase("narikei") 
			||
			nameWithoutNumber.equalsIgnoreCase("narikyo") 
			||
			nameWithoutNumber.equalsIgnoreCase("osho") 
			||
			nameWithoutNumber.equalsIgnoreCase("osho1") 
			||
			nameWithoutNumber.equalsIgnoreCase("ryuma") 
			||
			nameWithoutNumber.equalsIgnoreCase("ryuo") 
			||
			nameWithoutNumber.equalsIgnoreCase("tokin")
		)
			return nameWithoutNumber + " image created by Matthew Stephenson for Ludii, using the Quivira and Arial TTF fonts.";
		
		if (nameWithoutNumber.equalsIgnoreCase("shogi_blank"))
			return nameWithoutNumber + " image from the Quivira TTF font.";
						
		//-----------------------------------------------------
		// Common/res/img/svg/stickDice
		
		if 
		(
			nameWithoutNumber.equalsIgnoreCase("oldMan0") 
			||
			nameWithoutNumber.equalsIgnoreCase("oldMan1")
			||
			nameWithoutNumber.equalsIgnoreCase("oldWoman0")
			||
			nameWithoutNumber.equalsIgnoreCase("oldWoman1")
			||
			nameWithoutNumber.equalsIgnoreCase("youngMan0")
			||
			nameWithoutNumber.equalsIgnoreCase("youngMan1")
			||
			nameWithoutNumber.equalsIgnoreCase("youngWoman0")
			||
			nameWithoutNumber.equalsIgnoreCase("youngWoman1")
		)
			return nameWithoutNumber + " image created by Matthew Stephenson for Ludii.";
			
		//-----------------------------------------------------
		// Common/res/img/svg/stratego
		
		if 
		(
			nameWithoutNumber.equalsIgnoreCase("bomb")
			||
			nameWithoutNumber.equalsIgnoreCase("captain")
			||
			nameWithoutNumber.equalsIgnoreCase("colonel")
			||
			nameWithoutNumber.equalsIgnoreCase("flag")
			||
			nameWithoutNumber.equalsIgnoreCase("general")
			||
			nameWithoutNumber.equalsIgnoreCase("lieutenant")
			||
			nameWithoutNumber.equalsIgnoreCase("major")
			||
			nameWithoutNumber.equalsIgnoreCase("marshal")
			||
			nameWithoutNumber.equalsIgnoreCase("miner")
			||
			nameWithoutNumber.equalsIgnoreCase("scout")
			||
			nameWithoutNumber.equalsIgnoreCase("sergeant")
			||
			nameWithoutNumber.equalsIgnoreCase("spy")
		)
			return nameWithoutNumber + " image courtesy of Sjoerd Langkemper.";
			
		//-----------------------------------------------------
		// Common/res/img/svg/tafl
		
		if 
		(
			nameWithoutNumber.equalsIgnoreCase("jarl")
			||
			nameWithoutNumber.equalsIgnoreCase("thrall")
		)
			return nameWithoutNumber + " image from chess.medium OTF font.";
		
		if (nameWithoutNumber.equalsIgnoreCase("knotSquare"))
			return nameWithoutNumber + " by Smeshinka from https://www.dreamstime.com/.";
		
		if (nameWithoutNumber.equalsIgnoreCase("knotTriangle"))
			return nameWithoutNumber + " image from https://www.flaticon.com/free-icon/triquetra_1151995.";
			
		//-----------------------------------------------------
		// Common/res/img/svg/war
		
		if 
		(
			nameWithoutNumber.equalsIgnoreCase("bow")
			||
			nameWithoutNumber.equalsIgnoreCase("catapult")
			||
			nameWithoutNumber.equalsIgnoreCase("crossbow")
			||
			nameWithoutNumber.equalsIgnoreCase("knife")
			||
			nameWithoutNumber.equalsIgnoreCase("scimitar")
			||
			nameWithoutNumber.equalsIgnoreCase("smallSword")
			||
			nameWithoutNumber.equalsIgnoreCase("sword")
		)
			return nameWithoutNumber + " image from svgrepo.com.";
		
		//-----------------------------------------------------
		// Common/res/img/svg/army
		
		if 
		(
			nameWithoutNumber.equalsIgnoreCase("antiair")
			||
			nameWithoutNumber.equalsIgnoreCase("artillery")
			||
			nameWithoutNumber.equalsIgnoreCase("battleship")
			||
			nameWithoutNumber.equalsIgnoreCase("bomber")
			||
			nameWithoutNumber.equalsIgnoreCase("boss")
			||
			nameWithoutNumber.equalsIgnoreCase("builder")
			||
			nameWithoutNumber.equalsIgnoreCase("cruiser")
			||
			nameWithoutNumber.equalsIgnoreCase("demolisher")
			||
			nameWithoutNumber.equalsIgnoreCase("fighter")
			||
			nameWithoutNumber.equalsIgnoreCase("helicopter")
			||
			nameWithoutNumber.equalsIgnoreCase("launcher")
			||
			nameWithoutNumber.equalsIgnoreCase("motorbike")
			||
			nameWithoutNumber.equalsIgnoreCase("shooter")
			||
			nameWithoutNumber.equalsIgnoreCase("solider")
			||
			nameWithoutNumber.equalsIgnoreCase("speeder")
			||
			nameWithoutNumber.equalsIgnoreCase("submarine")
			||
			nameWithoutNumber.equalsIgnoreCase("tank")
		)
			return nameWithoutNumber + " image from svgrepo.com.";
		
		//-----------------------------------------------------
		// Common/res/img/svg/xiangqi
		
		if 
		(
			nameWithoutNumber.equalsIgnoreCase("jiang")
			||
			nameWithoutNumber.equalsIgnoreCase("ju")
			||
			nameWithoutNumber.equalsIgnoreCase("ma")
			||
			nameWithoutNumber.equalsIgnoreCase("pao")
			||
			nameWithoutNumber.equalsIgnoreCase("shi")
			||
			nameWithoutNumber.equalsIgnoreCase("xiang")
			||
			nameWithoutNumber.equalsIgnoreCase("zu")
		)
			return nameWithoutNumber + " image from BabelStoneXiangqi, Casefont, Arial Unicode MS, PragmataPro and Symbola TTF fonts.";
		
		//-----------------------------------------------------
		// ToolButtons
		
		if 
		(
			nameWithoutNumber.equalsIgnoreCase("button-about")
			||
			nameWithoutNumber.equalsIgnoreCase("button-dots-c")
			||
			nameWithoutNumber.equalsIgnoreCase("button-dots-d")
			||
			nameWithoutNumber.equalsIgnoreCase("button-dots")
			||
			nameWithoutNumber.equalsIgnoreCase("button-end-a")
			||
			nameWithoutNumber.equalsIgnoreCase("button-end")
			||
			nameWithoutNumber.equalsIgnoreCase("button-match-end")
			||
			nameWithoutNumber.equalsIgnoreCase("button-match-start")
			||
			nameWithoutNumber.equalsIgnoreCase("button-next")
			||
			nameWithoutNumber.equalsIgnoreCase("button-pass")
			||
			nameWithoutNumber.equalsIgnoreCase("button-pause")
			||
			nameWithoutNumber.equalsIgnoreCase("button-play")
			||
			nameWithoutNumber.equalsIgnoreCase("button-previous")
			||
			nameWithoutNumber.equalsIgnoreCase("button-settings-a")
			||
			nameWithoutNumber.equalsIgnoreCase("button-settings-b")
			||
			nameWithoutNumber.equalsIgnoreCase("button-start-a")
			||
			nameWithoutNumber.equalsIgnoreCase("button-start")
			||
			nameWithoutNumber.equalsIgnoreCase("button-swap")
		)
			return nameWithoutNumber + " image from https://www.flaticon.com/.";
				
		//-----------------------------------------------------
		
		return null;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return The maximum number of forward steps for a walk of this piece
	 */
	public int maxStepsForward() 
	{
		int maxStepsForward = 0;
		for (int i = 0; i < walk().length; i++)
		{
			int stepsForward = 0;
			for (int j = 0; j < walk()[i].length; j++)
			{
				if (walk()[i][j] == StepType.F)
				{
					stepsForward++;
				}
			}
			if (stepsForward > maxStepsForward)
			{
				maxStepsForward = stepsForward;
			}
		}
		return maxStepsForward;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param game
	 * @return the rules of this component in an English language format.
	 */
	public String componentGeneratorRulesToEnglish(final Game game)
	{
		return nameWithoutNumber + StringRoutines.getPlural(nameWithoutNumber) + " " + generator().toEnglish(game);
	}
	
}
