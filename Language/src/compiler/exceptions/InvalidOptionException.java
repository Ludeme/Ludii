package compiler.exceptions; 

import main.StringRoutines;

/**
 * @author mrraow
 */
public class InvalidOptionException extends CompilerException
{
	private static final long serialVersionUID = 1L;
	private final String message;
	
	/**
	 * @param message The message.
	 */
	public InvalidOptionException(final String message)
	{
		super();
		this.message = message;
	}

	@Override
	public String getMessageBody(String gameDescription)
	{
		final String safeDescription = StringRoutines.escapeText(gameDescription);
		final StringBuilder sb = new StringBuilder();

		sb.append("<html>");
		sb.append("<h2>");
		sb.append(getMessageTitle());
		sb.append("</h2>");
		sb.append("<br/>");
		sb.append("<p>");
		sb.append(safeDescription);
		sb.append("</p>");
		sb.append("</html>");
		
		System.out.println(sb);
		
		return sb.toString();
	}

	@Override
	public String getMessageTitle()
	{
		return message;
	}

}
