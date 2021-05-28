package exception;

/**
 * Exception indicating that a String description of an option was
 * not used at all in a game.
 *
 * @author Dennis Soemers
 */
public class UnusedOptionException extends RuntimeException
{
	
	/** */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * @param optionString
	 */
	public UnusedOptionException(final String optionString)
	{
		super("Unused option: " + optionString);
	}

}
