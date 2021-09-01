package game.equipment.component.tile;

import java.io.Serializable;

import annotations.Name;
import annotations.Opt;
import other.BaseLudeme;

/**
 * Defines the internal path of a tile component.
 * 
 * @author Eric.Piette
 * @remarks To define the path of the internal connection of a tile component.
 *          The number side 0 = the first direction of the tiling, in general 0
 *          = North.
 */
public final class Path extends BaseLudeme implements Serializable
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The from side. */
	private final Integer from;

	/** The first terminus of the path. */
	private final Integer slotsFrom;

	/** The second side of the path. */
	private final Integer to;

	/** The second terminus of the path. */
	private final Integer slotsTo;

	/** The colour of the path. */
	private final Integer colour;

	//-------------------------------------------------------------------------

	/**
	 * @param from      The "from" side of the connection.
	 * @param slotsFrom The slot of the "from" side [0].
	 * @param to        The "to" side of the connection.
	 * @param slotsTo   The slot of the "to" side [0].
	 * @param colour    The colour of the connection.
	 * @example (path from:0 to:2 colour:1)
	 */
	public Path
	(
		@Name 	   final Integer from,
		@Name @Opt final Integer slotsFrom, 
		@Name 	   final Integer to, 
		@Name @Opt final Integer slotsTo,
		@Name 	   final Integer colour
	)
	{
		this.from = from;
		this.slotsFrom = (slotsFrom == null) ? Integer.valueOf(0) : slotsFrom;
		this.to = to;
		this.slotsTo = (slotsTo == null) ? Integer.valueOf(0) : slotsTo;
		this.colour = colour;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Side 1
	 */
	public Integer side1()
	{
		return from;
	}

	/**
	 * @return Side 2
	 */
	public Integer side2()
	{
		return to;
	}

	/**
	 * @return Terminus 1
	 */
	public Integer terminus1()
	{
		return slotsFrom;
	}

	/**
	 * @return Terminus 2
	 */
	public Integer terminus2()
	{
		return slotsTo;
	}

	/**
	 * @return the colour of the path.
	 */
	public Integer colour()
	{
		return colour;
	}

	/**
	 * @param rotation         The rotation.
	 * @param maxOrthoRotation The max number of orthogonal rotations.
	 * @return The index of the first side with the rotation.
	 */
	public int side1(final int rotation, final int maxOrthoRotation)
	{
		return (from.intValue() + rotation) % maxOrthoRotation;
	}

	/**
	 * @param rotation         The rotation.
	 * @param maxOrthoRotation The max number of orthogonal rotations.
	 * @return The index of the second side with the rotation.
	 */
	public int side2(final int rotation, final int maxOrthoRotation)
	{
		return (to.intValue() + rotation) % maxOrthoRotation;
	}

}
