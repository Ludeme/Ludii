package compiler.exceptions; 

import main.StringRoutines;

/**
 * 
 * @author cambolbro
 */
public class BadImportException extends CompilerException
{
	private static final long serialVersionUID = 1L;
	private final String badFile;
	
	/**
	 * @param badFile
	 */
	public BadImportException(final String badFile)
	{
		super();
		this.badFile = badFile;
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
		sb.append(StringRoutines.highlightText(safeDescription, badFile, "font", "red"));
		sb.append("</p>");
		sb.append("</html>");
		
		System.out.println(sb);
		
		return sb.toString();
	}

	@Override
	public String getMessageTitle()
	{
		return "Failed to import " + badFile + ".";
	}

}
