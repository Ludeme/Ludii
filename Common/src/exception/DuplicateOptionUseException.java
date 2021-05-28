package exception;

/**
 * Exception indicating that a String description of an option was
 * used more than once (matched multiple distinct options).
 *
 * @author Dennis Soemers
 */
public class DuplicateOptionUseException extends RuntimeException
{
	
	/** */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * @param optionString
	 */
	public DuplicateOptionUseException(final String optionString)
	{
		super("Option with duplicate matches: " + optionString);
	}

}
