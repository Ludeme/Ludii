package compiler.exceptions; 

import main.StringRoutines;

/**
 * 
 * @author cambolbro
 */
public class BadSyntaxException extends CompilerException
{
	private static final long serialVersionUID = 1L;
	private final String keyword;
	private final String message;
	
	/**
	 * @param keyword
	 * @param message
	 */
	public BadSyntaxException(final String keyword, final String message)
	{
		super();
		this.keyword = keyword;
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
		sb.append(keyword == null ? safeDescription : StringRoutines.highlightText(safeDescription, keyword, "font", "red"));
		sb.append("</p>");
		sb.append("</html>");
		
		System.out.println(sb);
		
		return sb.toString();
	}

	@Override
	public String getMessageTitle()
	{
		return "Syntax error: " + message;
	}

}
