package compiler.exceptions; 

import main.StringRoutines;

/**
 * 
 * @author cambolbro
 */
public class CantDecomposeException extends CompilerException
{
	private static final long serialVersionUID = 1L;
	
	private final String source;
	
	/**
	 * @param source
	 */
	public CantDecomposeException(final String source)
	{
		super();
		this.source = source;
	}

	@Override
	public String getMessageBody(final String gameDescription)
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
		return source + ": The game description could not be decomposed into parts.";
	}

}
