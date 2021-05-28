package main.options;

//-----------------------------------------------------------------------------

/**
 * Record of an option argument, which may be named.
 * @author cambolbro
 */
public class OptionArgument
{
	/** Optional name in definition. Format is: name:<expression>. */
	private final String name;

	/** This argument's expression to expand in the game description. */ 
	private final String expression;
		
	//------------------------------------------------------------------------

	public OptionArgument(final String name, final String expression)
	{
		this.name       = (name == null) ? null : new String(name);
		this.expression = new String(expression);
	}
	
	//------------------------------------------------------------------------
	
	public String name()
	{
		return name;
	}

	public String expression()
	{
		return expression;
	}
	
	//------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		if (name != null)
			sb.append(name + ":");
		sb.append("<" + expression + ">");
		return sb.toString();
	}
	
	//------------------------------------------------------------------------
	
}
