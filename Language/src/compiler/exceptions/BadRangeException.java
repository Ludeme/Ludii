package compiler.exceptions; 

import main.StringRoutines;

/**
 * 
 * @author cambolbro
 */
public class BadRangeException extends CompilerException
{
	private static final long serialVersionUID = 1L;
	private final int limit;
	
	/**
	 * @param limit
	 */
	public BadRangeException(final int limit)
	{
		super();
		this.limit = limit;
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
		sb.append(StringRoutines.highlightText(safeDescription, "..", "font", "red"));
		sb.append("</p>");
		sb.append("</html>");
		
		System.out.println(sb);
		
		return sb.toString();
	}

	@Override
	public String getMessageTitle()
	{
		return "A range \"..\" has exceeded the limit " + limit + ".";
	}

}
