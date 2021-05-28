package compiler.exceptions; 

import main.StringRoutines;

/**
 * @author cambolbro
 */
public class CantCompileException extends CompilerException
{
	private static final long serialVersionUID = 1L;
	private final String badKeyword;
	
	/**
	 * @param badKeyword
	 */
	public CantCompileException(final String badKeyword)
	{
		super();
		this.badKeyword = StringRoutines.toDromedaryCase(badKeyword);
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
		return "Can't compile the ludeme \"" + badKeyword + "\".";
	}

}
