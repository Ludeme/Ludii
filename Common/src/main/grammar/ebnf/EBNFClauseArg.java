package main.grammar.ebnf;

/**
 * EBNF style clause argument for interpreting grammar.
 * 
 * @author cambolbro
 */
public class EBNFClauseArg extends EBNFClause
{
	private boolean isOptional = false;
	private int 	orGroup = 0;
	private String 	parameterName = null;
	private int     nesting = 0;
	
	//-------------------------------------------------------------------------
	
	public EBNFClauseArg
	(
		final String input, final boolean isOptional, final int orGroup
	)
	{
		this.isOptional = isOptional;
		this.orGroup = orGroup;
		
		decompose(input);
	}
	
	//-------------------------------------------------------------------------
	
	public boolean isOptional()
	{
		return isOptional;
	}
	
	public int orGroup()
	{
		return orGroup;
	}

	public String parameterName()
	{
		return parameterName;
	}

	public int nesting()
	{
		return nesting;
	}
	
	//-------------------------------------------------------------------------

	void decompose(final String input)
	{
		String str = input.trim();
		
		// Assume that optional status has already been set and brackets '[...]' removed

		// Strip parameter name, if any
		final int colonAt = str.indexOf(":");
		if (colonAt >= 0)
		{
			parameterName = str.substring(0, colonAt).trim();
			str = str.substring(colonAt + 1).trim();
		}
				
		// Strip array braces, if any
		while (str.charAt(0) == '{')
		{
			if (str.charAt(str.length() - 1) != '}')
			{
				System.out.println("** No closing brace for array in: " + str);
				return;
			}
			nesting++;
			str = str.substring(1, str.length() - 1).trim();
		}
		
		token = str.trim();
	}
		
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();

//		if (isOptional)
//			sb.append("[");

		if (parameterName != null)
			sb.append(parameterName + ":");
		
		for (int n = 0; n < nesting; n++)
			sb.append("{");
		
		sb.append(token);

		for (int n = 0; n < nesting; n++)
			sb.append("}");

//		if (isOptional)
//			sb.append("]");

//		if (orGroup != 0)
//			sb.append("=" + orGroup);
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
}
