package compiler.exceptions; 

import main.StringRoutines;

/**
 * 
 * @author cambolbro
 */
public class BadSymbolException extends CompilerException
{
	private static final long serialVersionUID = 1L;
	private final String badSymbol;
	
	/**
	 * @param badSymbol
	 */
	public BadSymbolException(final String badSymbol)
	{
		super();
		this.badSymbol = badSymbol;
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
		sb.append(StringRoutines.highlightText(safeDescription, badSymbol, "font", "red"));
		sb.append("</p>");
		sb.append("</html>");
		
		System.out.println(sb);
		
		return sb.toString();
	}

	@Override
	public String getMessageTitle()
	{
		return "An error occurred when matching " + badSymbol + ".";
	}

}
