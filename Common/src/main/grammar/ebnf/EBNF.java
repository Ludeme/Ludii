package main.grammar.ebnf;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//-----------------------------------------------------------------------------

/**
 * EBNF style interpreter for grammar.
 * 
 * @author cambolbro
 */
public class EBNF
{
	private Map<String, EBNFRule> rules = new HashMap<String, EBNFRule>();
	
	//-------------------------------------------------------------------------

	public EBNF(final String grammar)
	{
		interpret(grammar);
	}
	
	//-------------------------------------------------------------------------

	public Map<String, EBNFRule> rules()
	{
		return Collections.unmodifiableMap(rules);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Whether the given string is a terminal symbol, typically int, boolean, float, string.
	 */
	public static boolean isTerminal(final String token)
	{
		return 
			token.charAt(0) != '<' 
			&& 
			token.charAt(token.length() - 1) != '>'
			&&
			!token.trim().contains(" ");
	}
	
	//-------------------------------------------------------------------------

	public void interpret(final String grammar)
	{		
		final String[] split = grammar.trim().split("\n");
		
		// Remove comments
		for (int n = 0; n < split.length; n++)
		{
			final int c = split[n].indexOf("//");
			if (c >= 0)
				split[n] = split[n].substring(0, c);
		}

		// Merge split lines
		for (int n = split.length - 1; n >= 1; n--)
		{
			final int c = split[n].indexOf("::=");
			if (c < 0)
			{
				split[n - 1] += " " + split[n].trim();
				split[n] = "";
			}
		}
		
		for (int n = 0; n < split.length; n++)
		{
			if (split[n].contains("::="))
			{
				String strRule = split[n];
				while (strRule.contains("  "))
					strRule = strRule.replaceAll("  ", " ");
				
				final EBNFRule rule = new EBNFRule(strRule);
				rules.put(rule.lhs(), rule);
			}
		}
		
//		System.out.println(rules.size() + " EBNF rules found.");		
//		for (final EBNFRule rule : rules.values())
//			System.out.println(rule);		
	}
	
	//-------------------------------------------------------------------------
	
}
