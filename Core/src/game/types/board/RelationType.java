package game.types.board;

import game.util.directions.AbsoluteDirection;

/**
 * Defines the possible relation types between graph elements.
 * 
 * @author Eric.Piette
 */
public enum RelationType
{
	/** Orthogonal relation. */
	Orthogonal,

	/** Diagonal relation. */
	Diagonal,

	/** Diagonal-off relation. */
	OffDiagonal,

	/** Adjacent relation. */
	Adjacent,

	/** Any relation. */
	All;
	
	//-------------------------------------------------------------------------

	/**
	 * @param relation The relation to convert.
	 * @return The equivalent in absolute direction.
	 */
	public static AbsoluteDirection convert(final RelationType relation)
	{
		switch (relation)
		{
		case Adjacent:
			return AbsoluteDirection.Adjacent;
		case Diagonal:
			return AbsoluteDirection.Diagonal;
		case All:
			return AbsoluteDirection.All;
		case OffDiagonal:
			return AbsoluteDirection.OffDiagonal;
		case Orthogonal:
			return AbsoluteDirection.Orthogonal;
		
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("RelationType.convert(): a RelationType is not implemented.");
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns true if this is equal to or a subset of rA
	 * 
	 * @param rA
	 * @return True of this is super set of the entry.
	 */
	public boolean supersetOf(final RelationType rA)
	{
		if (this.equals(rA))
			return true;
		if (this.equals(All))
			return true;
		if (this.equals(Adjacent) && rA.equals(Orthogonal))
			return true;
		
		return false;
	}
}
