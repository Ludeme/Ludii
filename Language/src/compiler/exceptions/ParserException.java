package compiler.exceptions; 

/**
 * Parser-specific exception hierarchy.
 * @author cambolbro, mrraow
 */
public class ParserException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/**
	 * @param messageBody The message.
	 * @param e           The exception.
	 */
	public ParserException(String messageBody, ParserException e)
	{
		super(messageBody, e);
	}

	/**
	 * 
	 */
	public ParserException()
	{
		super();
	}

	/**
	 * @param gameDescription The game description.
	 * @return The message to print.
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
	 * @return The title of the message.
	 */
	@SuppressWarnings("static-method")
	public String getMessageTitle() 
	{
		return "A parser error has occurred.";
	}

}
