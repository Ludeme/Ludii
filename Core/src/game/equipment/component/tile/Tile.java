package game.equipment.component.tile;

import java.io.Serializable;
import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.equipment.component.Component;
import game.rules.play.moves.Moves;
import game.types.board.StepType;
import game.types.play.RoleType;
import game.util.moves.Flips;
import main.Constants;
import main.StringRoutines;
import metadata.graphics.util.ComponentStyleType;
import other.concept.Concept;

/**
 * Defines a tile, a component following the tiling with internal connection.
 *
 * @author Eric.Piette
 */
public class Tile extends Component implements Serializable
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The terminus. */
	private int[] terminus;

	/** The number of terminus, if this is the same for each side. */
	private final Integer numTerminus;

	/** The paths. */
	private final Path[] paths;

	/** The number of sides of the tile. */
	private int numSides;

	/** The flips value of a piece. */
	private final Flips flips;

	/**
	 * @param name         The name of the tile.
	 * @param role         The owner of the tile.
	 * @param walk         A turtle graphics walk to define the shape of a large
	 *                     tile.
	 * @param walks        Many turtle graphics walks to define the shape of a large
	 *                     tile.
	 * @param numSides     The number of sides of the tile.
	 * @param slots        The number of slots for each side.
	 * @param slotsPerSide The number of slots for each side if this is the same
	 *                     number for each side [1].
	 * @param paths        The connection in the tile.
	 * @param flips        The corresponding values to flip, e.g. (flip 1 2) 1 is
	 *                     flipped to 2 and 2 is flipped to 1.
	 * @param generator    The associated moves of this component.
	 * @param maxState     To set the maximum local state the game should check.
	 * @param maxCount     To set the maximum count the game should check.
	 * @param maxValue     To set the maximum value the game should check.
	 * 
	 * @example (tile "TileX" numSides:4 { (path from:0 to:2 colour:1) (path from:1
	 *          to:3 colour:2) } )
	 */
	public Tile
	(
			  	   	   final String       name,
		@Opt 		   final RoleType     role,
		@Opt @Or       final StepType[]   walk,
		@Opt @Or   	   final StepType[][] walks,
		@Opt	 @Name final Integer      numSides,
		@Opt @Or @Name final Integer[]    slots,
		@Opt @Or @Name final Integer      slotsPerSide,
		@Opt 	       final Path[]       paths,
		@Opt           final Flips        flips,
		@Opt 	       final Moves        generator,
		@Opt     @Name final Integer      maxState,
		@Opt     @Name final Integer      maxCount,
		@Opt     @Name final Integer      maxValue
	)
	{
		super
		(
			name, 
			(role == null) ? RoleType.Shared : role, 
			(walk != null) ? new StepType[][] { walk } : walks, 
			null, 
			generator, maxState, maxCount, maxValue
		);

		// Limit on the max number of sides.
		if (numSides != null)
		{
			if (numSides.intValue() < 0 || numSides.intValue() > Constants.MAX_SIDES_TILE)
				throw new IllegalArgumentException(
						"The number of sides of a tile piece can not be negative or to exceed "
								+ Constants.MAX_SIDES_TILE + ".");
		}

		int numNonNull = 0;
		if (walk != null)
			numNonNull++;
		if (walks != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException("Only one of 'walk' and 'walks' can be specified.");

		numNonNull = 0;
		if (slots != null)
			numNonNull++;
		if (slotsPerSide != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException("Zero or one Or parameter can be non-null.");

		if (slots != null)
		{
			terminus = new int[slots.length];
			for (int i = 0; i < terminus.length; i++)
				terminus[i] = slots[i].intValue();
		}
		else
		{
			terminus = null;
		}

		numTerminus = (slots == null && slotsPerSide == null) ? Integer.valueOf(1) : slotsPerSide;
		this.paths = paths;
		nameWithoutNumber = StringRoutines.removeTrailingNumbers(name);
		
		if (walk() != null)
			style = ComponentStyleType.LargePiece;
		else
			style = ComponentStyleType.Tile;

		this.numSides = (numSides != null) ? numSides.intValue() : Constants.OFF;

		this.flips = flips;
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
	protected Tile(final Tile other)
	{
		super(other);
		terminus = other.terminus;
		numSides = other.numSides;

		if (other.terminus != null)
		{
			terminus = new int[other.terminus.length];
			for (int i = 0; i < other.terminus.length; i++)
				terminus[i] = other.terminus[i];
		}
		else
			terminus = null;

		numTerminus = other.numTerminus;

		if (other.paths != null)
		{
			paths = new Path[other.paths.length];
			for (int i = 0; i < other.paths.length; i++)
				paths[i] = other.paths[i];
		}
		else
			paths = null;

		flips = (other.getFlips() != null)
				? new Flips(Integer.valueOf(other.getFlips().flipA()), Integer.valueOf(other.getFlips().flipB()))
				: null;
	}

	@Override
	public Tile clone()
	{
		return new Tile(this);
	}

	@Override
	public boolean isTile()
	{
		return true;
	}

	@Override
	public int[] terminus()
	{
		return terminus;
	}

	@Override
	public Integer numTerminus()
	{
		return numTerminus;
	}

	@Override
	public Path[] paths()
	{
		return paths;
	}

	@Override
	public int numSides()
	{
		return numSides;
	}

	@Override
	public void setNumSides(final int numSides)
	{
		this.numSides = numSides;
	}

	@Override
	public Flips getFlips()
	{
		return flips;
	}

	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.Tile.id(), true);
		if (walk() != null)
			concepts.set(Concept.LargePiece.id(), true);
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
						"A tile is defined in the equipment with an incorrect owner: " + role() + ".");
				missingRequirement = true;
			}
		}
		return missingRequirement;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		String string = nameWithoutNumber;
		
		final String plural = StringRoutines.getPlural(nameWithoutNumber);
		string += plural;
		
		if (flips != null)
			string += ", " + flips.toEnglish(game);
		
		String pathString = "";
		if (paths != null && paths.length > 0)
		{
			pathString = " [";
			for (final Path p : paths)
				pathString += p.toEnglish(game) + ",";
			pathString = pathString.substring(pathString.length()-1) + "]";
		}
		
		String terminusString = "";
		if (terminus != null && terminus.length > 0)
		{
			terminusString = " [";
			for (final int i : terminus)
				terminusString += i + ",";
			terminusString = terminusString.substring(terminusString.length()-1) + "]";
		}
		
		string += ", with " + numSides + " sides and " + numTerminus + " terminus" + pathString + terminusString;
		
		return string;
	}
	
	//-------------------------------------------------------------------------
	
}