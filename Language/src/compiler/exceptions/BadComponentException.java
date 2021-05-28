package compiler.exceptions; 

import main.StringRoutines;

/**
 * 
 * @author cambolbro
 */
public class BadComponentException extends CompilerException
{
	private static final long serialVersionUID = 1L;
	private final String badComponentName;
	
	/**
	 * @param badComponentName
	 */
	public BadComponentException(final String badComponentName)
	{
		super();
		this.badComponentName = StringRoutines.toDromedaryCase(badComponentName);
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
		sb.append(StringRoutines.highlightText(safeDescription, badComponentName, "font", "red"));
		sb.append("</p>");
		sb.append("</html>");
		
		System.out.println(sb);
		
		return sb.toString();
	}

	@Override
	public String getMessageTitle()
	{
		return "The component " + badComponentName + " is not defined. Try appending the player index.";
	}

}
