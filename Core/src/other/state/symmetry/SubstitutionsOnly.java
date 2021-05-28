package other.state.symmetry;

/**
 * Only player substitutions are valid
 * @author mrraow
 */
public class SubstitutionsOnly implements SymmetryValidator 
{
	@Override
	public boolean isValid(final SymmetryType type, final int symmetryIndex, final int symmetryCount) 
	{
		switch(type) {
		case REFLECTIONS: return false;
		case ROTATIONS: return symmetryIndex==0;	// Identity (element 0) only
		case SUBSTITUTIONS: return true; 
		}
		return true;
	}

}
