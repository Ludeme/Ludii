package game.equipment.component;

import java.io.Serializable;
import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.rules.play.moves.Moves;
import game.types.play.RoleType;
import game.util.directions.DirectionFacing;
import game.util.moves.Flips;
import main.StringRoutines;
import other.concept.Concept;

/**
 * Defines a piece.
 *
 * @author Eric.Piette and cambolbro
 * 
 * @remarks Useful to create a pawn, a disc or a representation of an animal for
 *          example.
 */
public class Piece extends Component implements Serializable
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The flips value of a piece. */
	private final Flips flips;
	
	/**
	 * @param name      The name of the piece.
	 * @param role      The owner of the piece [Each].
	 * @param dirn      The direction of the piece.
	 * @param flips     The corresponding values to flip, e.g. (flip 1 2) 1 is
	 *                  flipped to 2 and 2 is flipped to 1.
	 * @param generator The moves associated with the piece.
	 * @param maxState  To set the maximum local state the game should check.
	 * @param maxCount  To set the maximum count the game should check.
	 * @param maxValue  To set the maximum value the game should check.
	 * 
	 * @example (piece "Pawn" Each)
	 * 
	 * @example (piece "Disc" Neutral (flips 1 2))
	 * 
	 * @example (piece "Dog" P1 (step (to if:(is Empty (to)))))
	 */
	public Piece
	(
			       final String          name,
		@Opt 	   final RoleType        role,
		@Opt 	   final DirectionFacing dirn,
		@Opt       final Flips           flips,
		@Opt 	   final Moves           generator,
		@Opt @Name final Integer         maxState,
		@Opt @Name final Integer         maxCount,
		@Opt @Name final Integer         maxValue
	)
	{
		super(name, (role == null ? RoleType.Each : role), null, dirn, generator, maxState, maxCount, maxValue);

		// To remove any numbers at the end of the name of the component to find the correct image. (e.g. Tower of Hanoi)
		nameWithoutNumber = StringRoutines.removeTrailingNumbers(name);
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
	protected Piece(final Piece other)
	{
		super(other);
		flips = (other.getFlips() != null)
				? new Flips(Integer.valueOf(other.getFlips().flipA()), Integer.valueOf(other.getFlips().flipB()))
				: null;
	}

	@Override
	public Piece clone()
	{
		return new Piece(this);
	}

	@Override
	public Flips getFlips()
	{
		return flips;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.Piece.id(), true);
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
						"A piece is defined in the equipment with an incorrect owner: " + role() + ".");
				missingRequirement = true;
			}
		}
		
		if(generator() != null)
			if(generator().missingRequirement(game))
				missingRequirement = true;
		
		return missingRequirement;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		String string = nameWithoutNumber;
		
		String plural = StringRoutines.getPlural(nameWithoutNumber);
		string += plural;
		
		if (flips != null)
			string += ", " + flips.toEnglish(game);
		
		return string;
	}
	
	//-------------------------------------------------------------------------
	
}