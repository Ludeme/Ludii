package compiler.exceptions; 

import main.StringRoutines;

/**
 * 
 * @author cambolbro
 */
public class UnclosedClauseException extends CompilerException
{
	private static final long serialVersionUID = 1L;
	private final String badClause;
	
	/**
	 * @param badClause
	 */
	public UnclosedClauseException(final String badClause)
	{
		super();
		this.badClause = badClause;
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
		sb.append(StringRoutines.highlightText(safeDescription, badClause, "font", "red"));
		sb.append("</p>");
		sb.append("</html>");
		
		System.out.println(sb);
		
		return sb.toString();
	}

	@Override
	public String getMessageTitle()
	{
		return "The clause \"" + badClause + "\" was not closed.";
	}

}
