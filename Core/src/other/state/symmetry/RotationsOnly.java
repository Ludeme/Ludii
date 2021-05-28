package other.state.symmetry;

/**
 * Only rotational symmetries and identity operators are valid
 * @author mrraow
 */
public class RotationsOnly implements SymmetryValidator 
{
	@Override
	public boolean isValid(final SymmetryType type, final int symmetryIndex, final int symmetryCount) 
	{
		switch(type) {
		case REFLECTIONS: return false;
		case ROTATIONS: return true;
		case SUBSTITUTIONS: return symmetryIndex==0;	// Identity (element 0) only 
		}
		return true;
	}

}
