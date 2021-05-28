package other.state.symmetry;

import java.util.EnumSet;

/**
 * All supported types of symmetry
 * @author mrraow
 */
public enum SymmetryType 
{
	/**
	 * Rotation symmetries.
	 */
    ROTATIONS,

	/**
	 * Reflections symmetries.
	 */
    REFLECTIONS,

	/**
	 * Substitutions symmetries.
	 */
    SUBSTITUTIONS;
	
	/**
	 * All symmetries
	 */
	public static final EnumSet<SymmetryType> ALL = EnumSet.allOf(SymmetryType.class);
	
	/**
	 * No symmetries
	 */
	public static final EnumSet<SymmetryType> NONE = EnumSet.noneOf(SymmetryType.class);
}
