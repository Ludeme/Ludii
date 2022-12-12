package game.equipment.component;

import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.rules.play.moves.Moves;
import game.types.play.RoleType;
import game.util.directions.DirectionFacing;
import main.StringRoutines;
import metadata.graphics.util.ComponentStyleType;
import other.concept.Concept;
import other.context.Context;

/**
 * Defines a single non-stochastic die used as a piece.
 * 
 * @author Eric.Piette and cambolbro
 * 
 * @remarks The die defined with this ludeme will be not included in a dice
 *          container and cannot be rolled with the roll ludeme, but can
 *          be turned to show each of its faces.
 */
public class Die extends Component implements Serializable
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The number of faces of the die. */
	private final int numFaces;

	/** The faces values. */
	private int[] faces;

	/**
	 * @param name      The name of the die.
	 * @param role      The owner of the die.
	 * @param numFaces  The number of faces of the die.
	 * @param dirn      The direction of the component.
	 * @param generator The moves associated with the component.
	 * 
	 * @example (die "Die6" All numFaces:6)
	 */
	public Die
	(
				   final String          name, 
				   final RoleType        role, 
		     @Name final Integer         numFaces, 
		@Opt	   final DirectionFacing dirn, 
		@Opt	   final Moves           generator
	)
	{
		super(name, role, null, dirn, generator, null,null,null);
		this.numFaces = numFaces.intValue();		
		style = ComponentStyleType.Die;
	}
	
	
	/**
	 * Copy constructor.
	 *
	 * Protected because we do not want the compiler to detect it, this is called
	 * only in Clone method.
	 * 
	 * @param other
	 */
	protected Die(final Die other)
	{
		super(other);
		numFaces = other.numFaces;
		if (other.faces != null)
		{
			faces = new int[other.faces.length];
			for (int i = 0; i < other.faces.length; i++)
				faces[i] = other.faces[i];
		}
		else
			other.faces = null;
	}

	@Override
	public Die clone()
	{
		return new Die(this);
	}

	@Override
	public boolean isDie()
	{
		return true;
	}

	@Override
	public int[] getFaces()
	{
		return faces;
	}

	@Override
	public int getNumFaces()
	{
		return numFaces;
	}

	@Override
	public int roll(final Context context)
	{
		return (context.rng().nextInt(faces.length));
	}

	@Override
	public void setFaces(final Integer[] faces, final Integer start)
	{
		if (start != null)
		{
			this.faces = new int[numFaces];
			for (int i = start.intValue(); i < start.intValue() + numFaces; i++)
				this.faces[i - start.intValue()] = i;
		}
		else if (faces != null)
		{
			this.faces = new int[faces.length];
			for (int i = 0; i < faces.length; i++)
				this.faces[i] = faces[i].intValue();
		}
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.Dice.id(), true);
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
			if (((indexOwnerPhase < 1 && !role().equals(RoleType.Shared)) && !role().equals(RoleType.Neutral)
					&& !role().equals(RoleType.All)) || indexOwnerPhase > game.players().count())
			{
				game.addRequirementToReport(
						"A die is defined in the equipment with an incorrect owner: " + role() + ".");
				missingRequirement = true;
			}
		}
		return missingRequirement;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		String string = nameWithoutNumber == null ? "Die" : nameWithoutNumber;

		String plural = StringRoutines.getPlural(string);
		string += plural;
		
		string += " with " + numFaces + " faces valued " + Arrays.toString(faces);
		
		return string;
	}
	
	//-------------------------------------------------------------------------

}
