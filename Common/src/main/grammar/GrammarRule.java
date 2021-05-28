package main.grammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import main.grammar.Symbol.LudemeType;

//-----------------------------------------------------------------------------

/**
 * Rule within the grammar.
 * @author cambolbro
 */
public class GrammarRule 
{
	/** LHS symbol, which must be non-terminal or primitive -- not constant! */
	private Symbol lhs = null;

	/** RHS expression, consisting of clauses separated by "|". */
	private List<Clause> rhs = new ArrayList<Clause>();

	//-------------------------------------------------------------------------
	// Formatting
	
	/** Maximum number of chars per line. */
	public static final int MAX_LINE_WIDTH = 80;

	/** Separator to delineate main types. */
	//public static final String SEPARATOR = " ";

	/** Separator to delineate main types. */
	public static final String IMPLIES = " ::= ";

	/** Tab for lining up "::=" in grammar. */
	public static final int TAB_LHS = 10;

	/** Tab for lining up expressions in grammar. */
	public static final int TAB_RHS = TAB_LHS + IMPLIES.length();

	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 * @param lhs
	 */
	public GrammarRule(final Symbol lhs)
	{
		this.lhs = lhs;
		lhs.setRule(this);
	}

	//-------------------------------------------------------------------------

	public Symbol lhs() 
	{
		return lhs;
	}

	public List<Clause> rhs() 
	{
		if (rhs == null)
			return null;
		return Collections.unmodifiableList(rhs);
	}

	//-------------------------------------------------------------------------

	public void addToRHS(final Clause clause)
	{
		rhs.add(clause);
	}
	
	public void removeFromRHS(final int n)
	{
		rhs.remove(n);
	}
	
	public void clearRHS()
	{
		rhs.clear();
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param clause
	 * @return Whether this rule's RHS expression already contains the specified clause.
	 */
	public boolean containsClause(final Clause clause) 
	{
		final String str = clause.toString();
		for (Clause clauseR : rhs)
			if (clauseR.toString().equals(str))
				return true;
		return false;
	}

	//-------------------------------------------------------------------------

	public void alphabetiseClauses()
	{
		Collections.sort(rhs, new Comparator<Clause>() 
		{
			@Override
            public int compare(final Clause a, final Clause b) 
            {
//            	return a.symbol().name().compareTo(b.symbol().name());
//            	return a.symbol().className().compareTo(b.symbol().className());
            	return a.symbol().token().compareTo(b.symbol().token());
            }
        });
		
		// Move constructor clauses to front of list
		for (int n = 0; n < rhs.size(); n++)
		{
			final Clause clause = rhs.get(n);
			if (clause.args() != null)
			{
				// Clause is for a constructor
				rhs.remove(n);
				rhs.add(0, clause);
			
				//System.out.println("Clause is constructor: " + clause);
			}
		}
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString() 
	{
		String ruleStr = "";

		if (lhs == null)
			return "** No LHS. **";

		//ruleStr += (lhs.type() == SymbolType.Constant) ? lhs.name() : lhs.toString(true);  // force lowerCamelCase on LHS
		ruleStr += (lhs.ludemeType() == LudemeType.Constant) ? lhs.grammarLabel() : lhs.toString(true);  // force lowerCamelCase on LHS

		//ruleStr += " (" + lhs.cls().getName() + ")";		
		
		// Special handling for IntArrayFunction
		final boolean isInts = ruleStr.equals("<int>{<int>}");
		if (isInts)	
			ruleStr = "<ints>";
		
		while (ruleStr.length() < TAB_LHS)
			ruleStr += " ";

		ruleStr += IMPLIES;

		// Assemble string description for RHS
		String rhsStr = "";
		
		if (isInts)
			rhsStr = "{<int>}";
		
		for (final Clause clause : rhs)			
		{
//			if (clause.isHidden())
//				continue;  // denoted with a @Hide annotation
			
			if (!rhsStr.isEmpty())
				rhsStr += " | ";
			
			String expStr = clause.toString();
			rhsStr += expStr;
			
//			System.out.println("Clause is: " + clause);
		}

		ruleStr += rhsStr;

		// Prepare tab if needed
		String tab = "";
		for (int c = 0; c < TAB_RHS; c++)
			tab += " ";

		// Split line as needed
		int lastBreakAt = 0;
		for (int c = 0; c < ruleStr.length(); c++) 
		{
			if (c - lastBreakAt > MAX_LINE_WIDTH)  // - 1) 
			{
				// Break the line
				//
				// Backtrack to previous '|' symbol (if any)
				int barAt = c;
				while (barAt > 2 && ruleStr.charAt(barAt - 2) != '|')
					barAt--;

				if (barAt < lastBreakAt + tab.length()) 
				{
					// Look for next '|' symbol
					barAt = c;
					while (barAt < ruleStr.length() && ruleStr.charAt(barAt) != '|')
						barAt++;
				}
				
				if (barAt > 0 && barAt < ruleStr.length()) 
					ruleStr = ruleStr.substring(0, barAt) + "\n" + tab + ruleStr.substring(barAt);
				lastBreakAt = barAt;
			}
		}

		return ruleStr;
	}

	//-------------------------------------------------------------------------

}
