package main.grammar;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import main.grammar.Token.TokenType;

//-----------------------------------------------------------------------------

/**
 * An item in the parse tree corresponding to a token in the game description.
 * 
 * @author cambolbro
 */
public class ParseItem
{	
	// Parse token that this item corresponds to.
	private final Token token;
	
	private final List<ParseItem> arguments = new ArrayList<ParseItem>();
	
	// Parent item. Will be null for the root of the parse tree.
	private final ParseItem parent;

	// List of symbols and grammar clauses that match this item.
	private final List<Instance> instances = new ArrayList<Instance>();
	
	// Whether this token and its arguments all parse
	private boolean doesParse = false;
	
	// Whether token was visited during parse
	private boolean visited = false;
	
	// Depth in tree
	private final int depth;
	
	//-------------------------------------------------------------------------

	public ParseItem(final Token token, final ParseItem parent)
	{
		this.token  = token;
		this.parent = parent;
		depth = (parent == null) ? 0 : parent.depth() + 1;
	}
	
	//-------------------------------------------------------------------------

	public Token token()
	{
		return token;
	}

	public List<ParseItem> arguments()
	{
		return Collections.unmodifiableList(arguments);
	}

	public ParseItem parent()
	{
		return parent;
	}

	public List<Instance> instances()
	{
		return Collections.unmodifiableList(instances);
	}

	public boolean doesParse()
	{
		return doesParse;
	}
	
	public boolean visited()
	{
		return visited;
	}
	
	public int depth()
	{
		return depth;
	}
	
	//-------------------------------------------------------------------------

	public void clearInstances()
	{
		instances.clear();
	}
	
	public void add(final Instance instance)
	{
		instances.add(instance);
	}
	
	public void add(final ParseItem arg)
	{
		arguments.add(arg);
	}

	//-------------------------------------------------------------------------

//	private static int DEBUG = 0;
//	
//	public void matchSymbols(final Grammar grammar, final Report report)
//	{
//		instances.clear();
//		
//		Arg arg;
//		switch (token.type())
//		{
//		case Terminal:  // String, number, enum, True, False
//			arg = new ArgTerminal(token.name(), token.parameterLabel());
//			arg.matchSymbols(grammar, report);  // instantiates actual object(s), not need for clauses
//			
//			for (final Instance instance : arg.instances())
//				//if (!instance.symbol().isAbstract())
//					instances.add(instance);
//
//			if (DEBUG != 0)
//			{
//				System.out.println("\nTerminal token '" + token.name() + "' has " + instances.size() + " instances:");
//				for (final Instance instance : instances)
//				{
//					System.out.println(":: " + instance.toString());
//						
//					if (instance.object() != null)
//						System.out.println("=> " + instance.object());
//						
//					if (instance.clauses() != null)
//						for (final Clause clause : instance.clauses())
//							System.out.println("---- " + clause.toString());
//				}
//			}
//			break;
//		case Class:
//			// Find matching symbols and their clauses
//			arg = new ArgClass(token.name(), token.parameterLabel());
//			arg.matchSymbols(grammar, report);
//
//			for (final Instance instance : arg.instances())
//				//if (!instance.symbol().isAbstract())
//					instances.add(instance);
//
//			// Associate clauses with each instance of arg
//			for (final Instance instance : instances)
//			{
//				final GrammarRule rule = instance.symbol().rule();
//				if (rule == null)
//				{
//					// **
//					// ** This should not occur!
//					// ** Possibly ludeme has same name as a predefined Grammar ludeme, e.g. State.
//					// **
//					System.out.println("** ParseItem.matchSymbols(): Null rule for symbol " + instance.symbol() + 
//							", parent is " + (parent == null ? "null" : parent.token()) + ".");
//				}
//				else
//				{
//					//final List<Clause> clauses = rule.rhs();
//					instance.setClauses(rule.rhs());
//				}
//			}
//	
//			if (DEBUG != 0)
//			{	
//				System.out.println("\nClass token '" + token.name() + "' has " + instances.size() + " instances:");
//				for (final Instance instance : instances)
//				{
//					System.out.println(":: " + instance.toString());
//						
//					if (instance.object() != null)
//						System.out.println("=> " + instance.object());
//	
//					if (instance.clauses() != null)
//						for (final Clause clause : instance.clauses())
//							System.out.println("---- " + clause.toString());
//				}
//			}
//			break;
//		case Array:
//			// Do nothing: use instances of each element on a case-by-case basis
//			break;
//		}
//	}

	//-------------------------------------------------------------------------

	public boolean parse(final Symbol expected, final Report report, final String tab)
	{
		if (tab != null)
			System.out.println("\n" + tab + "Parsing token " + token.name() + ", expected type is " + 
								(expected == null ? "null" : expected.name()) + ".");
	
		visited = true;
		
		switch (token.type())
		{
		case Terminal:
			return parseTerminal(expected, tab);
		case Array:
			return parseArray(expected, report, tab);
		case Class:
			return parseClass(expected, report, tab);
		default:
			// Do nothing
		}
		
		return doesParse;
	}

	//-------------------------------------------------------------------------

	private boolean parseTerminal(final Symbol expected, final String tab)
	{
		// Handle terminal
		if (tab != null)
			System.out.println(tab + "Handling terminal...");

		for (final Instance instance : instances)
		{
			if (tab != null)
			{
				System.out.print(tab + "Instance: " + instance.symbol().name());
				System.out.println(" => "  + instance.symbol().returnType().name() + "... ");
			}

			if (instance.clauses() != null)
				continue;  // terminals don't have clauses

			// Check whether symbol is of expected return type
			if (expected != null && !expected.compatibleWith(instance.symbol()))
			{
				if (tab != null)
					System.out.println(tab + "No match, move onto next instance...");
				continue;
			}
		
			if (tab != null)
				System.out.println(tab + "++++ Terminal '" + instance.symbol().name() + "' parses. ++++");

			doesParse = true;
			return true;
		}
		
		return false;
	}

	//-------------------------------------------------------------------------

	private boolean parseArray(final Symbol expected, final Report report, final String tab)
	{
		// Handle array
		if (tab != null)
			System.out.println(tab + "Handling array...");
		
		if (tab != null)
		{
			System.out.println(tab + "> Expected: " + expected.name());
			for (final ParseItem element : arguments)
				System.out.println(tab + "> " + element.token().name());
		}
		
		// Check that elements can be parsed with the expected type
		for (int e = 0; e < arguments.size(); e++)
		{
			final ParseItem element = arguments.get(e);
			if (!element.parse(expected, report, (tab == null ? null : tab + "   ")))
			{
				// Array fails if any element fails
				if (tab != null)
					System.out.println(tab + "   X: Couldn't parse array element " + e + ".");
				return false;
			}
		}
		
		// All array elements parsed without error
		if (tab != null)
			System.out.println(tab + "++++ Array of '" + expected.name() + "' parses. ++++");

		doesParse = true;
		return true;
	}
	
	//-------------------------------------------------------------------------

	private boolean parseClass(final Symbol expected, final Report report, final String tab)
	{
		// Handle class
		if (tab != null)
			System.out.println(tab + "Handling class...");

		// Find possible instances that match expected return type
		for (final Instance instance : instances)
		{
			if (tab != null)
			{
				System.out.print(tab + "Class instance: " + instance.symbol().name());
				System.out.print(" => "  + instance.symbol().returnType().name() + "... ");
			}
			
			// Check whether symbol is of expected return type
			if (expected != null && !expected.compatibleWith(instance.symbol()))
			{
				if (tab != null)
					System.out.println("no match, move onto next instance...");
				continue;
			}
			
			if (tab != null)
				System.out.println("possible match.");
			
			// Try possible clauses of this instance
			//if (tab != null)
			//	System.out.println(tab + "Symbol match, checking possible clauses...");
			
			if (instance.clauses() == null)
			{
				//System.out.println("** No clauses for instance: " + instance);
				continue;
			}
			
			int c;
			for (c = 0; c < instance.clauses().size(); c++) 
			{
				final Clause clause = instance.clauses().get(c);
				
				if (tab != null)
					System.out.println(tab + (c+1) + ". Trying clause: " + clause);
		
				if (arguments.size() > 0 && clause.args() == null)
				{
					if (tab != null)
						System.out.println(tab + "   X: Item has arguments but clauses does not.");
					continue;
				}				
				
				if (arguments.size() > clause.args().size())
				{
					if (tab != null)
						System.out.println(tab + "   X: Too many arguments for this clause.");
					continue;
				}
			
//				if (clause.args().size() > ArgCombos.MAX_ARGS)
//				{
//					if (tab != null)
//						System.out.println(tab + "   X: " + clause.symbol().name() + " has more than " + ArgCombos.MAX_ARGS + " args.");
//					report.addWarning(clause.symbol().name() + " has more than " + ArgCombos.MAX_ARGS + " args.");
//					continue;
//				}
			
				// **
				// ** Don't check each argument individually, as position in list can dictate what type it is 
				// **
				
				final int numSlots = clause.args().size();  //comboLength = combo.size();
				
				final int clauseSize = clause.args().size();
				final int argsSize  = arguments.size();
				
				for (int seed = 0; seed < (0x1 << clauseSize); seed++)
				{
					if (Integer.bitCount(seed) != argsSize)
						continue;  // wrong number of on-bits
	
					final BitSet combo = BitSet.valueOf(new long[] { seed });
					
					// Try this arg combo
					final BitSet subset = (BitSet)combo.clone();
					subset.and(clause.mandatory());
					if (!subset.equals(clause.mandatory()))
						continue;  // some mandatory arguments are null
					
					if (tab != null)
					{
						System.out.print(tab + "   Trying arg combo: ");
						int index = 0;
						for (int n = 0; n < numSlots; n++) 
							System.out.print((combo.get(n) ? "-" : ++index) + " ");
						System.out.println();
					}
					
					final BitSet orGroups = new BitSet();
					
					int index = 0;
					int a;
					for (a = 0; a < numSlots; a++)
					{
						if (!combo.get(a))
						{
							// Arg will be null; check that's optional (or an @Or)
							final ClauseArg clauseArg = clause.args().get(a);
							final boolean canSkip = clauseArg.optional() || clauseArg.orGroup() > 0;
							if (!canSkip)
							{
								//System.out.println(tab + "Arg " + a + " can't be null.");
								break;  // this arg can't be null
							}
						}
						else
						{
							final ParseItem arg = arguments.get(index++);
							final ClauseArg clauseArg = clause.args().get(a);
							
							// Check that @Or group is valid (if any) 
							final int orGroup = clauseArg.orGroup();
							if (orGroup > 0)
							{
								//System.out.println("orGroup is " + orGroup);
								if (orGroups.get(orGroup))
									break;  // too many non-null args in this @Or group 
								
								orGroups.set(orGroup, true);
							}
							
							// Check that names are valid (if any) 
							final String argName = arg.token().parameterLabel();
							final String clauseArgName = clauseArg.label();
							
							//System.out.println("\narg.token().parameterLabel()=" + arg.token().parameterLabel());
							//System.out.println("clauseArg.label()=" + clauseArg.label());

							if 
							(
								argName == null && clauseArgName != null
								||
								argName != null && clauseArgName == null
								||
								argName != null && !argName.equalsIgnoreCase(clauseArgName)
							)
							{
								// **
								// ** FIXME: This still allows users to misspell argument names,
								// **        by capitalising badly.
								// **
								
								//System.out.println("Name clash.";
								break;  // name mismatch
							}
							
							if (!arg.parse(clauseArg.symbol(), report, (tab == null ? null : tab + "   ")))
							{
								if (tab != null)
									System.out.println(tab + "   X: Couldn't parse arg " + index + ".");
								break;
							}
						}
					}
					
					if (a >= numSlots)
					{
						// This arg combo matches this clause
						if (tab != null)
							System.out.println(tab + "++++ Class '" + instance.symbol().name() + "' parses. ++++");
						doesParse = true;
						return true;
					}
					
					if (tab != null)
						System.out.println(tab + "Failed to parse this combo, trying next...");
				}	
			}
		}
		
		return false;
	}

	//-------------------------------------------------------------------------	

	/**
	 * @return Depth of deepest item that does not parse.
	 */
	public int deepestFailure()
	{
		int result = (doesParse || !visited) ? -1 : depth;
		
		for (final ParseItem arg : arguments)
		{
			final int argResult = arg.deepestFailure();
			if (argResult > result)
				result = argResult;
		}
		
		return result;
	}

	public void reportFailures(final Report report, final int failureDepth)
	{
		if (!doesParse && visited && depth == failureDepth)
		{
			// This token could not be parsed
			if (parent == null)
			{
				final String clause = Report.clippedString(tokenClause(), 32);
				report.addError("Unexpected syntax '" + clause + "'.");
			}
			else
			{
				final String clause = Report.clippedString(tokenClause(), 24);
				final String parentClause = Report.clippedString(parent.tokenClause(), 32);
				report.addError("Unexpected syntax '" + clause + "' in '" + parentClause +"'.");
			}
		}
		
		for (final ParseItem arg : arguments)
			arg.reportFailures(report, failureDepth);
	}
	
	//-------------------------------------------------------------------------
	
	public String dump(final String indent)
	{
		final String TAB = "    ";
		
		final StringBuilder sb = new StringBuilder();
		
		//final String label = "" + token.type().name().charAt(0) + token.type().name().charAt(1) + ": "; 
		final String label = (doesParse ? "+" : "-") + " "; 
		
		sb.append(label + indent);
		
		if (token.parameterLabel() != null)
			sb.append(token.parameterLabel() + ":");

		if (token.open() != 0)
			sb.append(token.open());

		if (token.name() != null)
			sb.append(token.name());
		
//		if (token.parameterLabel() != null)
//			sb.append(" (" + token.parameterLabel() + ":)");
		
		if (arguments.size() > 0)
		{
			sb.append("\n");
			
			for (final ParseItem arg : arguments)
				sb.append(arg.dump(indent + TAB));
			
			if (token.close() != 0)
				sb.append(label + indent + token.close());
		}
		else
		{
			if (token.close() != 0)
				sb.append(token.close());
		}	
		sb.append("\n");
		
		return sb.toString();
	}

	//-------------------------------------------------------------------------
	
	public String compare()
	{
		final StringBuilder sb = new StringBuilder();

//		if (token.type() != TokenType.Class)
//		{
//			System.out.println("** Class expected but " + token.name() + " found.");
//			return sb.toString();
//		}
		
//		final String label = "" + type().name().charAt(0) + type().name().charAt(type().name().length()-1) + ": "; 
//		final String label = "";  // + token.type().name().charAt(0) + token.type().name().charAt(1) + ": "; 
		
		sb.append("--------------------------\n");
		
		// 1. Show the token of this clause
		sb.append(tokenClause());
		
		// 2. Show the potential clauses
		if (token.type() == TokenType.Array)
		{
			sb.append(" => Array\n");
		}
		else if (token.type() == TokenType.Terminal)
		{
			if (instances == null || instances.size() < 1)
				return "** compare(): No instances for terminal " + token.name() + ".\n";
			
			sb.append(" => " + instances.get(0).symbol().cls().getSimpleName() + "\n");
		}
		else
		{
			sb.append("\n");
			for (final Instance instance : instances)
			{
				if (instance != null && instance.clauses() != null)
				{
					for (int c = 0; c < instance.clauses().size(); c++) 
					{
						final Clause clause = instance.clauses().get(c);
//						sb.append(clause.toString() + "  ==>  " + clause.symbol().grammarLabel() + "\n");
						sb.append("" + (c+1) + ". " + clause.symbol().grammarLabel() + ": " + clause.toString());
						sb.append(" => " + instance.symbol().cls().getSimpleName());
						sb.append("\n");
					}
				}
			}
		}
		
		for (final ParseItem arg : arguments)
			sb.append(arg.compare());
		
		return sb.toString();
	}

	//-------------------------------------------------------------------------

	public String tokenClause()
	{
		final StringBuilder sb = new StringBuilder();
		
		if (token.parameterLabel() != null)
			sb.append(token.parameterLabel() + ":");
		
		if (token.open() != 0)
			sb.append(token.open());
		
		if (token.name() != null)
			sb.append(token.name());
				
		for (int a = 0; a < arguments.size(); a++) 
		{
			final ParseItem arg = arguments.get(a);
			if (a > 0 || token.name() != null)
				sb.append(" ");
			sb.append(arg.tokenClause());
		}
		
		if (token.close() != 0)
			sb.append(token.close());
					
		return sb.toString();
	}

	//-------------------------------------------------------------------------	

}
