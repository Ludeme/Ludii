package compiler.exceptions; 

import main.StringRoutines;

/**
 * 
 * @author cambolbro
 */
public class TerminalNotFoundException extends CompilerException
{
	private static final long serialVersionUID = 1L;
	private final String badTerminal;
	
	/**
	 * 
	 * @param badTerminal
	 */
	public TerminalNotFoundException(final String badTerminal)
	{
		super();
		this.badTerminal = badTerminal;
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
		sb.append(StringRoutines.highlightText(safeDescription, badTerminal, "font", "red"));
		sb.append("</p>");
		sb.append("</html>");
		
		System.out.println(sb);
		
		return sb.toString();
	}

	@Override
	public String getMessageTitle()
	{
		return "The keyword " + badTerminal + " cannot be recognised.";
	}

}
