package compiler.exceptions; 

import main.StringRoutines;

/**
 * 
 * @author cambolbro
 */
public class BadKeywordException extends CompilerException
{
	private static final long serialVersionUID = 1L;
	private final String badKeyword;
	private final String message;
	
	/**
	 * @param badKeyword
	 * @param message
	 */
	public BadKeywordException(final String badKeyword, final String message)
	{
		super();
		this.badKeyword = badKeyword;  //StringRoutines.toDromedaryCase(badKeyword);
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
		sb.append(StringRoutines.highlightText(safeDescription, badKeyword, "font", "red"));
		sb.append("</p>");
		sb.append("</html>");
		
		System.out.println(sb);
		
		return sb.toString();
	}

	@Override
	public String getMessageTitle()
	{
		String str = "The keyword \"" + badKeyword + "\" cannot be recognised.";
		if (message != null)
			str += " " + message;
		
		return str;
	}

}
