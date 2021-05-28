package other.state.symmetry;

/**
 * Validates a symmetry, to see if it is useful in this context.
 * @author mrraow@gmail.com
 */
public interface SymmetryValidator 
{
	/**
	 * NOTE: This function should almost always return true when symmetryIndex==0 - this will probably be the identity operator 
	 * 
	 * @param type The type of symmetry, e.g. reflection, rotation
	 * @param symmetryIndex index of this symmetry, often an angle
	 * @param symmetryCount number of symmetries
	 * @return Whether the symmetry suits this context.
	 */
	public boolean isValid(final SymmetryType type, final int symmetryIndex, final int symmetryCount);
}
