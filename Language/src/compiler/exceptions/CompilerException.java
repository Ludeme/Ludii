package compiler.exceptions; 

/**
 * Compiler-specific exception hierarchy.
 * @author cambolbro, mrraow
 */
public class CompilerException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/**
	 * @param messageBody
	 * @param e
	 */
	public CompilerException(String messageBody, CompilerException e)
	{
		super(messageBody, e);
	}

	/**
	 * 
	 */
	public CompilerException()
	{
		super();
	}

	/**
	 * @param gameDescription The description of the game.
	 * 
	 * @return The body message.
	 */
	public String getMessageBody(final String gameDescription)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("<html>");
		sb.append("<h2>");
		sb.append(getMessageTitle());
		sb.append("</h2>");
		sb.append("<br/>");
		sb.append("<p>");
		sb.append(gameDescription);
		sb.append("</p>");
		sb.append("</html>");
		
		return sb.toString();
	}
	
	/**
	 * @return The message of the title.
	 */
	@SuppressWarnings("static-method")
	public String getMessageTitle() 
	{
		return "A compiler error has occurred.";
	}

}
