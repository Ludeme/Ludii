package compiler.exceptions; 

import main.StringRoutines;

/**
 * 
 * @author cambolbro
 */
public class BadArrayElementException extends CompilerException
{
	private static final long serialVersionUID = 1L;
	private final String expectedType;
	private final String elementType;
	
	/**
	 * @param expectedType
	 * @param elementType
	 */
	public BadArrayElementException(final String expectedType, final String elementType)
	{
		super();
		this.expectedType = expectedType;
		this.elementType  = elementType;
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
		sb.append(StringRoutines.highlightText(safeDescription, elementType, "font", "red"));
		sb.append("</p>");
		sb.append("</html>");
		
		System.out.println(sb);
		
		return sb.toString();
	}

	@Override
	public String getMessageTitle()
	{
		return "Array element of type " + elementType + " but type " + expectedType + " expected.";
	}

}
