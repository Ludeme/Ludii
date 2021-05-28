package other.state.symmetry;

/**
 * Universal acceptor; all symmetries are valid
 * @author mrraow
 */
public class AcceptAll implements SymmetryValidator 
{
	@Override
	public boolean isValid(SymmetryType type, int angleIndex, int maxAngles) 
	{
		return true;
	}

}
