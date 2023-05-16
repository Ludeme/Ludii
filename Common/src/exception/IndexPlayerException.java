package exception;

/**
 * Changed to RuntimeException because no way to recover, I think.
 * An exception to prevent to create an instance of a player with an index <= 0 or > maxPlayer
 *
 * @author Eric.Piette and cambolbro
 *
 */
public class IndexPlayerException extends RuntimeException 
{
	private static final long serialVersionUID = 1L;

	public IndexPlayerException(int index)
	{
		System.err.println("Instantiation of a player with the index " + index);
	}
	
}
