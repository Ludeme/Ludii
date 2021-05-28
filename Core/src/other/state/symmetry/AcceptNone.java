package other.state.symmetry;

/**
 * Universal rejector; no symmetries are valid, except the identity
 * @author mrraow
 */
public class AcceptNone implements SymmetryValidator 
{
	@Override
	public boolean isValid(final SymmetryType type, final int symmetryIndex, final int symmetryCount) 
	{
		switch (type)
		{
		case REFLECTIONS: return false;
		case ROTATIONS: return symmetryIndex==0;
		case SUBSTITUTIONS: return symmetryIndex==0;
		}
		return false;
	}

}
