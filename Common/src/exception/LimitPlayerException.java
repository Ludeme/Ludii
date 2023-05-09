package exception;

/**
 * No way to recover. Runtime exception. An exception to prevent to create an
 * instance with more players than the max number of players or with less than 0
 * player.
 * 
 * @author Eric.Piette and cambolbro
 *
 */

public class LimitPlayerException extends RuntimeException 
{
	private static final long serialVersionUID = 1L;

	public LimitPlayerException(int numPlayers)
	{
		System.err.println("Instantiation of a play with " + numPlayers);
	}
	
}
