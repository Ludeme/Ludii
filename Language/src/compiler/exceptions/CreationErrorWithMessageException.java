package compiler.exceptions; 

/**
 * 
 * @author cambolbro
 */
public class CreationErrorWithMessageException extends CompilerException
{
	private static final long serialVersionUID = 1L;
	private final String message;
	
	/**
	 * @param message
	 */
	public CreationErrorWithMessageException(final String message)
	{
		super();
		this.message = message;
	}

	@Override
	public String getMessageBody(String gameDescription)
	{
		final StringBuilder sb = new StringBuilder();
		
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
