package main.grammar.ebnf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import main.StringRoutines;

//-----------------------------------------------------------------------------

/**
 * EBNF style rule for interpreting grammar.
 * 
 * @author cambolbro
 */
public class EBNFRule
{
	private String lhs = "?";
	private final List<EBNFClause> rhs = new ArrayList<EBNFClause>();
	//private Class<?> cls = null;
	
	//-------------------------------------------------------------------------

	public EBNFRule(final String input)
	{
		decompose(input);
	}
	
	//-------------------------------------------------------------------------
	
	public String lhs()
	{
		return lhs;
	}

	public List<EBNFClause> rhs()
	{
		return Collections.unmodifiableList(rhs);
	}

//	public Class<?> cls()
//	{
//		return cls;
//	}
//	
//	public void setClass(final Class<?> c)
//	{
//		cls = c;
//	}
	
	//-------------------------------------------------------------------------

	void decompose(final String input)
	{
		// Store and trim LHS
		final String[] sides = input.split("::=");
		lhs = sides[0].trim();

		// Decompose RHS (sides[1])
		String str = sides[1].trim();
		
		// Avoid bad bracket interpretation in <boolean>: "<<> | <<=> | <=> | <>> | <>=>"
		str = str.replaceAll("<<", "<#");
		str = str.replaceAll("<>", "<@");
		str = str.replaceAll("<>", "#>");
		str = str.replaceAll(">>", "@>");
		
		if (str.length() == 0)
		{
			System.out.println("** Empty RHS for rule: " + input);
			return;
		}
			
		int c = 0;
		int cc;
		do
		{
			if (str.charAt(c) == '(' || str.charAt(c) == '<')
			{
				cc = StringRoutines.matchingBracketAt(str, c);
				if (cc < 0)
				{
					System.out.println("** Failed to load clause from: " + str);
					return;
				}
			}
			else
			{
				cc = c + 1;
				while (cc < str.length() && str.charAt(cc) != ' ')
					cc++;
			}
			
			if (cc >= str.length())
				cc--;
			
			String strClause = str.substring(c, cc + 1).trim();
			strClause = strClause.replaceAll("#", "<");
			strClause = strClause.replaceAll("@", ">");
			final EBNFClause clause = new EBNFClause(strClause);
			rhs.add(clause);
			
//			if (!StringRoutines.balancedBrackets(strClause))
//				System.out.println("** Unbalanced brackets: " + strClause);
			
			c = cc + 1;
			while (c < str.length() && (str.charAt(c) == ' ' ||str.charAt(c) == '|'))
				c++;
		} while (c < str.length());
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append(lhs);
		sb.append(" ::= ");
		for (int c = 0; c < rhs.size(); c++)
		{
			final EBNFClause clause = rhs.get(c);
			if (c > 0)
				sb.append(" | ");
			sb.append(clause);
		}
		
		return sb.toString();
	}

	//-------------------------------------------------------------------------
	
}
