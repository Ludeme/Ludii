package compiler.exceptions; 

//-----------------------------------------------------------------------------

/**
 * 
 * @author cambolbro
 */
public class CompilerErrorWithMessageException extends CompilerException
{
	private static final long serialVersionUID = 1L;
	private final String message;
	
	/**
	 * @param message
	 */
	public CompilerErrorWithMessageException(final String message)
	{
		super();
		this.message = message;
	}

	@Override
	public String getMessageBody(String gameDescription)
	{
		final StringBuilder sb = new StringBuilder();

//		final String safeDescription = StringRoutines.escapeText(gameDescription);

//		sb.append("<html>");
//		sb.append("<h2>");
//		sb.append(getMessageTitle());
//		sb.append("</h2>");
//		sb.append("<br/>");
//		sb.append("<p>");
//		sb.append(safeDescription);
//		sb.append("</p>");
//		sb.append("</html>");
		
		sb.append(message);
		
		System.out.println(sb);
		
		return sb.toString();
	}

	@Override
	public String getMessageTitle()
	{
		return message;
	}

}
