package other.state.symmetry;

/**
 * Only reflections are valid
 * @author mrraow
 */
public class ReflectionsOnly implements SymmetryValidator 
{
	@Override
	public boolean isValid (final SymmetryType type, final int symmetryIndex, final int symmetryCount) 
	{
		switch(type) {
		case REFLECTIONS: return true;
		case ROTATIONS: return symmetryIndex==0;		// Identity (element 0) only
		case SUBSTITUTIONS: return symmetryIndex==0;	// Identity (element 0) only 
		}
		return true;
	}

}
