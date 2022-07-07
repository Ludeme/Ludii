package parser; 

/**
 * Record of a "(define ...)" instance
 * @author cambolbro
 */
public class Define
{
	private final String tag;
	private String expression;  // expression may be modified if recursive defines
	private final boolean parameterised;
	private final boolean isKnown;  // whether is a known define from Common/res/def
	
	//---------------------------------------------------- --------------------

	/**
	 * @param tag
	 * @param expression
	 */
	public Define(final String tag, final String expression, final boolean isKnown)
	{
		this.tag = new String(tag);
		this.expression = new String(expression);
		this.isKnown = isKnown;
		
		parameterised = expression.contains("#");
	}
	
	//---------------------------------------------------- --------------------
	
	/**
	 * @return The tag.
	 */
	public String tag()
	{
		return tag;
	}
	
	/**
	 * @return The expression.
	 */
	public String expression()
	{
		return expression;
	}
	
	/**
	 * Set the expression.
	 * 
	 * @param expr
	 */
	public void setExpression(final String expr)
	{
		expression = expr;
	}
	
	/**
	 * @return True if it is parameterised.
	 */
	public boolean parameterised()
	{
		return parameterised;
	}
	
	/**
	 * @return Whether this is a known define from Common/res/def.
	 */
	public boolean isKnown()
	{
		return isKnown;
	}
	
	//---------------------------------------------------- --------------------

	@Override
	public String toString()
	{
		String str = "";
		str += "{tag:" + tag + ", expression:" + expression + ", parameterised:" + parameterised + "}";
		return str;
	}
	
	
	//---------------------------------------------------- --------------------
	

}
