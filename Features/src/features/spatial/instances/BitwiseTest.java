package features.spatial.instances;

import game.types.board.SiteType;
import other.state.State;

/**
 * Interface for classes that can perform bitwise tests on game states.
 * 
 * The primary class implementing this interface is the general Feature
 * Instance, but there are also some more specific subclasses that only
 * need a small part of the full Feature Instance functionality, and can
 * run their tests more efficiently.
 * 
 * @author Dennis Soemers
 */
public interface BitwiseTest 
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param state
	 * @return True if this test matches the given game state
	 */
	public boolean matches(final State state);
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return True if and only if this test automatically returns true
	 */
	public boolean hasNoTests();
	
	/**
	 * @return True if and only if this test only requires a single chunk to 
	 * be empty.
	 */
	public boolean onlyRequiresSingleMustEmpty();
	
	/**
	 * @return True if and only if this test only requires a single chunk to be
	 * owned by a specific player.
	 */
	public boolean onlyRequiresSingleMustWho();
	
	/**
	 * @return True if and only if this test only requires a single chunk to 
	 * contain a specific component.
	 */
	public boolean onlyRequiresSingleMustWhat();
	
	/**
	 * @return GraphElementType that this test applies to
	 */
	public SiteType graphElementType();
	
	//-------------------------------------------------------------------------

}
