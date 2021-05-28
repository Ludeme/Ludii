package main.grammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import main.StringRoutines;

//-----------------------------------------------------------------------------

/**
 * Game description with full details after expansion.
 * 
 * @author cambolbro
 */
public class TokenForest
{
  	private List<Token> tokenTrees = new ArrayList<Token>();
   	
  	//-------------------------------------------------------------------------
	
	public List<Token> tokenTrees()
	{
		return Collections.unmodifiableList(tokenTrees);
	}

	public Token tokenTree()
	{
		// First token tree is the most important
		return tokenTrees.isEmpty() ? null : tokenTrees.get(0);
	}

	public void clearTokenTrees()
	{
		tokenTrees.clear();
	}

	//-------------------------------------------------------------------------

	public void populate(final String strIn, final Report report) 
	{
		tokenTrees.clear();
		
		if (strIn == null || strIn.isEmpty())
		{
			report.addError("Empty string in TokenForest.populate().");
			return;
		}
		
		String str = new String(strIn).trim();
		while (true)
		{
			final int c = str.indexOf("(");
			if (c < 0)
				break;
			
			final int cc = StringRoutines.matchingBracketAt(str, c);
			if (cc < 0)
			{
				report.addError("Couldn't close clause '" + Report.clippedString(str.substring(c), 20) + "'.");
				return;
			}
			
			tokenTrees.add(new Token(str.substring(c), report));
			
			str = str.substring(cc+1).trim();
		}
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		for (final Token token : tokenTrees)
		{
			if (sb.length() > 0)
				sb.append("\n");
			sb.append(token.toString());
		}
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------

}
