package compiler.exceptions; 

import main.StringRoutines;

/**
 * 
 * @author cambolbro
 */
public class BadRootException extends CompilerException
{
	private static final long serialVersionUID = 1L;

	private final String badRoot;
	private final String expectedRoot;
	
	/**
	 * @param badRoot
	 * @param expectedRoot
	 */
	public BadRootException(final String badRoot, final String expectedRoot)
	{
		super();
		this.badRoot = badRoot;
		this.expectedRoot = expectedRoot;
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
		sb.append(StringRoutines.highlightText(safeDescription, badRoot, "font", "red"));
		sb.append("</p>");
		sb.append("</html>");
		
		System.out.println(sb);
		
		return sb.toString();
	}

	@Override
	public String getMessageTitle()
	{
		return "Root " + badRoot + " found rather than expected root " + expectedRoot + ".";
	}

}
