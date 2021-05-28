package main.options;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import main.StringRoutines;

//-----------------------------------------------------------------------------

/**
 * Record of an "(option ...)" instance.
 * @author cambolbro
 */
public class Option
{
	/** Tag for this option used to identify it in the game description, e.g. "1" or "BoardSize". */
	private String tag = "";
	
	/** Description for showing in help etc. */
	private String description = "";
	
	/** List of arguments for this option, to be instantiated when option is expanded. */
	private final List<OptionArgument> arguments = new ArrayList<OptionArgument>();

	/** 
	 * Headings to be shown in the menu, split by level of nesting: 
	 * - headings[0] is main menu item heading (e.g. "Board Size")
	 * - headings[1] is child menu item heading (e.g. "7x7")
	 */
	private List<String> headings = new ArrayList<String>();
	
	/** Option's priority within its category. */
	private int priority = 0;
	
	//------------------------------------------------------------------------

//	public Option(final String str)
//	{
//		interpret(str);
//	}
	
	public Option(final String str, final OptionCategory category)
	{
		try { interpret(str, category); } 
		catch (Exception e) { e.printStackTrace(); }
	}
	
	public Option()
	{
	}
	
	//------------------------------------------------------------------------
	
	public String tag()
	{
		return tag;
	}
	
	public String description()
	{
		return description;
	}
	
	public List<String> menuHeadings()
	{
		return Collections.unmodifiableList(headings);
	}
	
	public void setHeadings(final List<String> headings)
	{
		this.headings = headings;
		
		System.out.println("Headings are now:");
		for (String heading : this.headings)
			System.out.println("-- " + heading);
	}
		
	public List<OptionArgument> arguments()
	{
		return Collections.unmodifiableList(arguments);
	}
	
	public int priority()
	{
		return priority;
	}
	
	//------------------------------------------------------------------------

//	/**
//	 * @return Whether any option arguments are named.
//	 */
//	public boolean argumentsAreNamed()
//	{
//		for (final OptionArgument arg : arguments)
//			if (arg.name() != null)
//				return true;
//		return false;
//	}
		
	//------------------------------------------------------------------------

	/**
	 * Interprets options from a given string in the new format, based on its category.
	 */
	void interpret(final String strIn, final OptionCategory category) throws Exception
	{
		// New format:  ("2x2" <2 2>  <4>  <2> "Played on a square 2x2 board.")
		
		String str = strIn.trim();
		
		if (!str.contains("(item ") || !str.contains(")"))
			throw new Exception("Option not bracketed properly: " + str);
		
		tag = category.tag();

		// Extract priority (number of asterisks appended)
		priority = 0;
		while (str.charAt(str.length()-1) == '*')
		{
			priority++;
			str = str.substring(0, str.length()-1);  // strip of rightmost asterisk
		}
		str = str.substring(1, str.length()-1).trim();
				
//		System.out.println("priority: " + priority);
		
//		System.out.println("Reduced option string: " + str);
		
		// Extract option heading
		int c = str.indexOf('"');
		if (c < 0)
			throw new Exception("Failed to find option heading: " + str);	
		
		int cc = c+1;
		while (cc < str.length() && str.charAt(cc) != '"')
			cc++;
		if (cc < 0 || cc >= str.length())
			throw new Exception("Failed to find option heading: " + str);		
		
		final String heading = str.substring(c+1, cc);
		headings.add(new String(category.heading()));
		headings.add(heading);
		
//		System.out.println("Options headings are: " + headings);
	
		str = str.substring(cc+1).trim();
		
//		System.out.println("Reduced option string: " + str);
		
		// Extract option description (search backwards from end)
		cc = str.length()-1;
		while (cc >= 0 && str.charAt(cc) != '"')
			cc--;
		if (cc < 0)
			throw new Exception("Failed to find option description: " + str);	
		
		c = cc-1;
		while (c >= 0 && str.charAt(c) != '"')
			c--;
		if (c < 0)
			throw new Exception("Failed to find option description: " + str);		
		
		description = str.substring(c+1, cc);
		
//		System.out.println("Option description is: " + description);
	
		str = str.substring(0, c).trim();
		
//		System.out.println("Option arguments are: " + str);
				
		// Extract option arguments
		final List<String> argTags = category.argTags();
		while (true)
		{
			c = str.indexOf("<");
			if (c < 0)
				break;  // no more options
			
//			System.out.println("c=" + c);
			
			if (c > 0 && str.charAt(c-1) == '(')
			{
				// Is an embedded alias "(< a b)" or "(<= a b)"
				str = str.substring(c+1).trim();
				continue;
			}
			
			cc = StringRoutines.matchingBracketAt(str, c, false);
			if (cc < 0 || cc >= str.length())
				throw new Exception("No closing bracket '>' for option argument: " + str);
			cc++;
			
			final String arg = (c+1 >= cc-1) ? "" : str.substring(c+1, cc-1);
		
//			System.out.println("-- arg is: " + arg);
			
//			final Option option = new Option(optionString, this);
//			options.add(option);
//			
//			System.out.println("Option is: " + option);
			
			if (arguments.size() >= argTags.size())
			{
//				System.out.println("arguments.size() is " + arguments.size() + ", argTags.size() is " + argTags.size() + ".");
				throw new Exception("Not enough tags for option arguments: " + strIn);
			}
			
			final String name = argTags.get(arguments.size());
			final OptionArgument optArg = new OptionArgument(name, arg);
			arguments.add(optArg);

//			System.out.println("-- optArg: " + optArg);
			
//			System.out.println("cc=" + cc);
			
			//str = str.substring(c+1).trim();
			str = str.substring(cc).trim();
		}
	}
	
	//------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("[" + tag + ", \"");
		for (int n = 0; n < headings.size(); n++)
		{
			if (n > 0)
				sb.append("/");  //" > ");
			sb.append(headings.get(n));
		}
		sb.append("\",");
		for (final OptionArgument arg : arguments)
		{
			//sb.append(" " + (arg.name() == null ? "" : (arg.name() + ":")) + "<" + arg.expression() + ">");
			sb.append(" ");
			if (arg.name() != null)
				sb.append(arg.name() + ":");
			sb.append("<" + arg.expression() + ">");
		}
		sb.append(", priority " + priority + "]");
		return sb.toString();
	}
	
	//------------------------------------------------------------------------
	
}
