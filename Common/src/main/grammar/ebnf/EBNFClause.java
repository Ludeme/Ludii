package main.grammar.ebnf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//-----------------------------------------------------------------------------

/**
 * EBNF style clause for interpreting grammar.
 * 
 * @author cambolbro
 */
public class EBNFClause
{
	protected String token = "?";
	
	private boolean isConstructor = false;
	private boolean isRule        = false;
	private boolean isTerminal    = false;
	
	private List<EBNFClauseArg> args = null;

	//-------------------------------------------------------------------------
	
	public EBNFClause()
	{
	}

	public EBNFClause(final String input)
	{
		decompose(input);
	}
	
	//-------------------------------------------------------------------------

	public String token()
	{
		return token;
	}
	
	public boolean isConstructor()
	{
		return isConstructor;
	}

	public boolean isRule()
	{
		return isRule;
	}

	public boolean isTerminal()
	{
		return isTerminal;
	}

	public List<EBNFClauseArg> args()
	{
		if (args == null)
			return null;
		return Collections.unmodifiableList(args);
	}
		
	//-------------------------------------------------------------------------

	void decompose(final String input)
	{
		String str = input.trim();
		
		switch (str.charAt(0))
		{
		case '(':
			isConstructor = true;
			break;
		case '<':
			isRule = true;
			token = str;
			return;
		default: 
			isTerminal = true;
			token = str;
			return;
		}
		
		// Must be constructor: strip opening and closing brackets
		if (str.charAt(0) != '(' || str.charAt(str.length() - 1) != ')')
		{
			System.out.println("** Bad bracketing of constructor: " + str);
			return;
		}
		str = str.substring(1, str.length() - 1);
		
		// Extract leading token
		int c = 0;
		while (c < str.length() && str.charAt(c) != ' ')
			c++;
		token = str.substring(0, c).trim();
		str = (c >= str.length()) ? "" : str.substring(c + 1).trim();
		
		// Create args
		args = new ArrayList<EBNFClauseArg>();
		if (str == "")
			return;
		
		final String[] subs = str.split(" ");
		final int[] orGroups = new int[subs.length];
		final boolean [] optional = new boolean[subs.length];
		
		// Determine 'or' groups
		int orGroup = 0;
		for (int n = 1; n < subs.length - 1; n++)
		{
			if (!subs[n].equals("|"))
				continue;
			
			if (n < 2 || !subs[n - 2].equals("|"))
				orGroup++;
			
			orGroups[n - 1] = orGroup;
			orGroups[n + 1] = orGroup;
		}
		
		// Determine optional items
		boolean on = false;
		for (int n = 0; n < subs.length; n++)
		{
			final boolean isOpen  = subs[n].contains("[");
			final boolean isClose = subs[n].contains("]");
				
			if (isOpen || isClose || on)
				optional[n] = true;
			
			if (isOpen)
				on = true;
			
			if (isClose)
				on = false;
		}
		
		// Create args, stripping of optional '[' and ']' brackets and 'or' brackets '(' and ')'
		for (int n = 0; n < subs.length; n++)
		{
			if (subs[n].equals("|"))
				continue;
			
			final String strArg = subs[n].replace("[", "").replace("]", "").replace("(", "").replace(")", "");
			final EBNFClauseArg arg = new EBNFClauseArg(strArg, optional[n], orGroups[n]);
			args.add(arg);		
		}
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		if (isConstructor)
			sb.append("(");
		
		sb.append(token);
		
		if (args != null)
			for (int a = 0; a < args.size(); a++)
			{						
				final EBNFClauseArg arg = args.get(a);
				sb.append(" ");
				
				// Check opening bracket
				if (arg.orGroup() != 0)
				{
					// Check if must show opening bracket
					if (a == 0 || args.get(a - 1).orGroup() != arg.orGroup())
					{
						// Must show opening bracket, determine what type
						if (arg.isOptional())
							sb.append("[");
						else
							sb.append("(");
					}
				
					// Check if must show 'or' separator
					if (a > 0 && args.get(a - 1).orGroup() == arg.orGroup())
						sb.append("| ");
				}
				else
				{
					// Individual item
					if (arg.isOptional())
						sb.append("[");
				}
								
				sb.append(arg);
				
				// Check closing bracket
				if (arg.orGroup() != 0)
				{
					// Check if must show closing bracket
					if (a == args.size() - 1 || args.get(a + 1).orGroup() != arg.orGroup())
					{
						// Must show closing bracket, determine what type
						if (arg.isOptional())
							sb.append("]");
						else
							sb.append(")");
					}
				}
				else
				{
					// Individual item
					if (arg.isOptional())
						sb.append("]");
					
				}
				
				
				
			}
		
		if (isConstructor)
			sb.append(")");

		return sb.toString();
	}
	
	//-------------------------------------------------------------------------

}
