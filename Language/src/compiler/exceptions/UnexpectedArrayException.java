package compiler.exceptions; 

import main.StringRoutines;

/**
 * 
 * @author cambolbro
 */
public class UnexpectedArrayException extends CompilerException
{
	private static final long serialVersionUID = 1L;
	private final String expectedType;
	
	/**
	 * @param expectedType
	 */
	public UnexpectedArrayException(final String expectedType)
	{
		super();
		this.expectedType = expectedType;
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
		return "An array of type " + expectedType + " was found when expecting a different type.";
	}

}
