package compiler.exceptions; 

import main.StringRoutines;

/**
 * 
 * @author cambolbro
 */
public class UnknownArrayErrorException extends CompilerException
{
	private static final long serialVersionUID = 1L;
	private final String expectedType;
	private final String message;
	
	/**
	 * @param expectedType
	 * @param message
	 */
	public UnknownArrayErrorException(final String expectedType, final String message)
	{
		super();
		this.expectedType = expectedType;
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
		sb.append(StringRoutines.highlightText(safeDescription, expectedType, "font", "red"));
		sb.append("</p>");
		sb.append("</html>");
		
		System.out.println(sb);
		
		return sb.toString();
	}

	@Override
	public String getMessageTitle()
	{
		return "An array of type " + expectedType + " suffered an unexpected error " + message + ". We apologise for any inconvenience";
	}

}
