package main.grammar;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

//-----------------------------------------------------------------------------

/**
 * Clause on RHS of rule delimited by "|".
 * @author cambolbro
 */
public class Clause 
{
	// Clauses can be either:
	//  1. Constructors    : denoted (name ...) with arg list (terminal except for args).
	//  2. Type references : denoted <name> with no arg list (non-terminal).
	
	/** Symbol describing base type. */
	private final Symbol symbol;

	/** List of constructor arguments (null if not constructor). */
	private final List<ClauseArg> args;

	/** Whether this clause is hidden from the grammar. */
	private final boolean isHidden;
	
	/** 
	 * Which arguments are mandatory, taking @Or groups into account. 
	 * This info can be obtained from the args themselves, but is useful 
	 * to store here in one BitSet for fast parsing.
	 */
	private BitSet mandatory = new BitSet();
	
	//-------------------------------------------------------------------------

	/**
	 * Constructor for non-Class clauses (no args).
	 * @param symbol
	 */
	public Clause(final Symbol symbol)
	{
		this.symbol   = symbol;
		this.args     = null;
		this.isHidden = false;
	}

	/**
	 * Constructor for Class clauses (with args).
	 * 
	 * @param symbol
	 * @param args
	 */
	public Clause(final Symbol symbol, final List<ClauseArg> args, final boolean isHidden) 
	{
		this.symbol = symbol;
		this.args   = new ArrayList<ClauseArg>();
		for (final ClauseArg arg : args)
			this.args.add(new ClauseArg(arg));
		this.isHidden  = isHidden;
		
		setMandatory();
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 */
	public Clause(final Clause other) 
	{
		this.symbol = other.symbol;

		if (other.args == null) 
		{
			args = null;
		} 
		else 
		{
			this.args = (other.args == null) ? null : new ArrayList<ClauseArg>();
			if (this.args != null)
				for (final ClauseArg arg : other.args)
					this.args.add(new ClauseArg(arg));
		}
		this.isHidden = other.isHidden;
		
		this.mandatory = (BitSet)other.mandatory.clone();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Symbol describing base type.
	 */
	public Symbol symbol() 
	{
		return symbol;
	}

	/**
	 * @return List of constructor arguments (null if not constructor).
	 */
	public List<ClauseArg> args() 
	{
		if (args == null)
			return null;
		return Collections.unmodifiableList(args);
	}

	/**
	 * @return Whether this clause is a constructor.
	 */
	public boolean isConstructor()
	{
		return args != null;
	}

	/**
	 * @return Whether this clause is hidden from the grammar.
	 */
	public boolean isHidden()
	{
		return isHidden;
	}
	
	public BitSet mandatory()
	{
		return mandatory;
	}
		
	//-------------------------------------------------------------------------

	public boolean matches(final Clause other)
	{
		return symbol.matches(other.symbol);
	}
	
	//-------------------------------------------------------------------------

	public void setMandatory()
	{
		mandatory.clear();
		for (int a = 0; a < args.size(); a++)
		{
			final ClauseArg arg = args.get(a);
			if (!arg.optional() && arg.orGroup() == 0)
				mandatory.set(a, true);  // this argument *must* exist
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param other
	 * @return Whether this sequence is a subset of the other.
	 */
	public boolean isSubsetOf(final Clause other) 
	{
		if (!symbol.path().equals(other.symbol().path()))
			return false;  // different base symbols
			
		for (final ClauseArg argA : args) 
		{
			int p;
			for (p = 0; p < other.args.size(); p++) 
			{
				final ClauseArg argB = other.args.get(p);
				if 
				(
					(argA.label() == null || argB.label() == null || argA.label().equals(argB.label()))
					&& 
					argA.symbol().path().equals(argB.symbol().path())
					&& 
					argA.nesting() == argB.nesting()
//					&& 
//					argA.isList() == argB.isList()
				)
					break;  // arg found in other clause
			}
			if (p >= other.args.size())
				return false;
		}
		return true;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString() 
	{
		String str = "";

		//System.out.println("symbol: " + symbol);
		
//		if (symbol == null)
//			System.out.println("** Clause.toString(): null symbol.");
		
		//System.out.println("\n" + symbol.info() + "\n");
		
		final String safeKeyword = symbol.grammarLabel();
			
		if (args != null)
		{
			// Clause is a constructor
			str += "(";
			
//			if (!symbol.hiddenName())
				str += symbol.token();  // + " ";
			
			// TODO: If all args in an @Or group are optional, then the entire group should be marked optional [(a | b ...)].
			
			// Show ([a] | [b] | [c]) as [a | b | c]

			ClauseArg prevArg = null;
			for (int p = 0; p < args.size(); p++) 
			{
				final ClauseArg arg = args.get(p);
				
				str += " ";
				
				final int orGroup  = arg.orGroup();
				final int andGroup = arg.andGroup();
				final boolean isAnd = arg.andGroup() != 0;
				
				if (orGroup != 0)
				{
					if (prevArg == null && !arg.optional())
					{
						// Open an @Or group at start
						str += "(";
					}
					else if (prevArg != null && orGroup != prevArg.orGroup())
					{
						// Open a new @Or group
						if (prevArg.orGroup() != 0 && !prevArg.optional())
						{
							// Close the previous @Or group
							str += ") ";
						}
						
						if (!arg.optional())
						{
							// Open the new group
							str += "(";
						}
					}
					else if (prevArg != null && orGroup == prevArg.orGroup() && (andGroup == 0 || andGroup != prevArg.andGroup()))
					{
						// Continue an @Or choice
						str += "| ";
					}
				}
				
				if (orGroup == 0 && prevArg != null && prevArg.orGroup() != 0 && !prevArg.optional())
				{
					// Close an @Or choice (in middle of clause)
					str += ") ";
				}
				
				boolean prevAnd = false;
				boolean nextAnd = false;
				
				if (prevArg != null)
					prevAnd = ((orGroup == 0 || orGroup != 0 && orGroup == prevArg.orGroup()) && andGroup == prevArg.andGroup());
									
				if (p < args.size()-1)
				{
					final ClauseArg nextArg = args.get(p+1);
					nextAnd = ((orGroup == 0 || orGroup != 0 && orGroup == nextArg.orGroup()) && andGroup == nextArg.andGroup());
				}
				
				String argString = arg.toString();
				
				if (prevAnd && orGroup != 0 && argString.charAt(0) == '[')
				{
					// Strip opening optional bracket '['
					argString = argString.substring(1);
				}
				
				if (nextAnd && orGroup != 0 && argString.charAt(argString.length()-1) == ']')
				{
					// Strip closing optional bracket ']'
					argString = argString.substring(0, argString.length()-1);
				}
						
				if (isAnd)
				{
					// Mark up @And args so that optional @And args can be processed below
					argString = "&" + argString + "&";
				}
				
				str += new String(argString);

				if (orGroup != 0 && p == args.size() - 1 && !arg.optional())
				{
					// Close an @Or choice (at end of clause)
					str += ")";
				}

				prevArg = arg;
			}
			
			// Close this clause
			str += ")";
			
			// Correct consecutive optional @And args
			str = str.replace("]& &[", " ");
			str = str.replace("&", "");
			
			// Tidy up constructors with no parameters
			str = str.replace(" )", ")");
		}
		else
		{
			// Clause is not a constructor
			switch (symbol.ludemeType()) 
			{
			case Primitive:
			case Predefined:
			case Constant:
				str = symbol.token();
				break;
			case Ludeme:
			case SuperLudeme:
			case SubLudeme:
			case Structural:
				str = "<" + safeKeyword + ">";
				break;
			default:
				str += "[UNKNOWN]";
			}
		}
		
		for (int n = 0; n < symbol.nesting(); n++)
			str = "{" + str + "}";
		
		return str;
	}

	//-------------------------------------------------------------------------

}
