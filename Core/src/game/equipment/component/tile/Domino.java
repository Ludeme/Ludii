package game.equipment.component.tile;

import java.io.Serializable;
import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.equipment.component.Component;
import game.rules.play.moves.Moves;
import game.types.board.StepType;
import game.types.play.RoleType;
import main.StringRoutines;
import metadata.graphics.util.ComponentStyleType;
import other.concept.Concept;

/**
 * Defines a single domino.
 *
 * @author Eric.Piette
 * 
 * @remarks The domino defined with this ludeme will be not included in the
 *          dominoes container by default and so cannot be shuffled with other
 *          dominoes.
 */
public class Domino extends Component implements Serializable
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** First Piece Value. */
	private final int value;

	/** Second Piece Value. */
	private final int value2;

	/**
	 * @param name      The name of the domino.
	 * @param role      The owner of the domino.
	 * @param value     The first value of the domino.
	 * @param value2    The second value of the domino.
	 * @param generator The moves associated with the component.
	 * @example (domino "Domino45" Shared value:4 value2:5)
	 */
	public Domino
	(
			       final String   name,
			       final RoleType role, 
			 @Name final Integer  value,
			 @Name final Integer  value2,
		@Opt       final Moves    generator
	)
	{
		super(name, role, new StepType[][]
		{ new StepType[]
				{ StepType.F, StepType.R, StepType.F, StepType.R, StepType.F, StepType.L, StepType.F, StepType.L,
						StepType.F, StepType.R, StepType.F, StepType.R, StepType.F } },
				null,
				generator, null, null, null);

		this.value = value.intValue();
		this.value2 = value2.intValue();

		nameWithoutNumber = StringRoutines.removeTrailingNumbers(name);
		
		style = ComponentStyleType.Domino;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toEnglish(final Game game)
	{
		return "<Domino>";
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
	protected Domino(final Domino other)
	{
		super(other);
		value = other.value;
		value2 = other.value2;
	}

	@Override
	public Domino clone()
	{
		return new Domino(this);
	}

	@Override
	public int getValue()
	{
		return value;
	}

	@Override
	public int getValue2()
	{
		return value2;
	}

	@Override
	public boolean isDoubleDomino()
	{
		return getValue() == getValue2();
	}

	@Override
	public boolean isDomino()
	{
		return true;
	}

	@Override
	public int numSides()
	{
		return 4;
	}

	@Override
	public boolean isTile()
	{
		return true;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.CanNotMove.id(), true);
		concepts.set(Concept.LargePiece.id(), true);
		concepts.set(Concept.Tile.id(), true);
		concepts.or(super.concepts(game));
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
						"A domino is defined in the equipment with an incorrect owner: " + role() + ".");
				missingRequirement = true;
			}
		}
		return missingRequirement;
	}
}
