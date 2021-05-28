package main.options;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import main.StringRoutines;

//-----------------------------------------------------------------------------

/**
 * Maintains a named category of game options.
 * @author cambolbro
 */
public class OptionCategory
{
	/** Tag for this option category used to identify it in the game description, e.g. "1" or "BoardSize". */
	private String tag;
	
	/** Heading for this category in the memu. This is not necessary, but provided for completeness. */
	private String heading;
		
	/** 
	 * List of argument tags for each option in this category. Theses are already stored in
	 * the argument of each option, but also kept here in a single list for convenience.
	 */
	private final List<String> argTags;
	
	/** The list of actual options for this category. */
	private List<Option> options;

	// **
	// ** TODO: Move the selection elsewhere! 
	// **       These values need to be decoupled from the Game and kept e.g. in user preferences.
	// **
	
	/** User selection for this option category. */
//	private int selection = 0;

	//-------------------------------------------------------------------------

	public OptionCategory(final String description)
	{
		this.argTags = new ArrayList<String>();
		this.options = new ArrayList<Option>();
		
		try { extractOptions(description); } 
		catch (Exception e) { e.printStackTrace(); }
	}
	
	public OptionCategory(final Option option)
	{
		this.tag     = new String(option.tag());
		this.heading = new String(option.menuHeadings().get(0));
		this.argTags = null;
		
		this.options = new ArrayList<Option>();
		this.options.add(option);
		
//		System.out.println("Tag=" + tag + ", menuHeading=" + menuHeading);
	}

	public OptionCategory(final List<Option> options)
	{
		this.tag     = new String(options.get(0).tag());
		this.heading = new String(options.get(0).menuHeadings().get(0));
		this.argTags = null;
		
		this.options = options;
	}
	
	//-------------------------------------------------------------------------
	
	public String tag()
	{
		return tag;
	}
	
	public String heading()
	{
		return heading;
	}
	
	public List<Option> options()
	{
		return Collections.unmodifiableList(options);
	}

	public List<String> argTags()
	{
		if (argTags == null)
			return argTags;
		return Collections.unmodifiableList(argTags);
	}

//	public int selection()
//	{
//		return selection;
//	}
//
//	public void setSelection(final int value)
//	{
//		selection = value;
//	}
//		
//	public Option selectedOption()
//	{
//		return options.get(selection);
//	}
		
	//-------------------------------------------------------------------------

	public void add(final Option option)
	{
		if (!tag.equals(option.tag()))
		{
			System.out.println("** Option label does not match option category label.");
		}
		options.add(option);
	}

	//-------------------------------------------------------------------------

	void extractOptions(final String strIn) throws Exception
	{
		// Extract an option set in this format:
		//
		//   (options "Board Size" <Size> args:{ <dim> <start> <end> }
		//     {
		//       ("2x2" <2 2>  <4>  <2> "Played on a square 2x2 board.")
		//       ("3x3" <3 3>  <9>  <4> "Played on a square 3x3 board.")
		//       ("4x4" <4 4> <16>  <8> "Played on a square 4x4 board.")*
		//       ("5x5" <5 5> <25> <12> "Played on a square 5x5 board.")**
		//       ("6x6" <6 6> <36> <18> "Played on a square 6x6 board.")
		//     }
		//   )
		//
		//   (options "End Rule" <End>  args:{ <result> }
		//     {
		//       ("Standard" <Win> "Win by making a line of four pieces.")*
		//       ("Misere"  <Lose> "Lose by making a line of four pieces.")
		//     }
		
		String str = new String(strIn);
		
		str = extractHeading(str);
		str = extractTag(str);
		str = extractArgTags(str);
		
//		System.out.println("Option category so far: " + toString());
//		System.out.println("Now processing:\n" + str);

		// Extract list of options
		int c = str.indexOf('{');
		if (c < 0)
			throw new Exception("Couldn't find opening bracket '{' for option list " + str.substring(c)); 
		
		int cc = StringRoutines.matchingBracketAt(str, c);
		if (cc < 0 || cc >= str.length())
			throw new Exception("Couldn't close option bracket '>' in " + str.substring(c));
		
		String optionList = str.substring(c+1, cc);
//		System.out.println("Option list: " + optionList);
		
		while (true)
		{
			c = optionList.indexOf("(item ");
			if (c < 0)
				break;  // no more options
			
			cc = StringRoutines.matchingBracketAt(optionList, c);
			if (cc < 0 || cc >= optionList.length())
				throw new Exception("No closing bracket ')' for option: " + optionList.substring(c));
			cc++;
			while (cc < optionList.length() && optionList.charAt(cc) == '*')
				cc++;
			
			final String optionString = optionList.substring(c, cc);
//			System.out.println("Option string is: " + optionString);
			
			final Option option = new Option(optionString, this);
			options.add(option);
			
//			System.out.println("Option is: " + option);
			
			optionList = optionList.substring(c+1).trim();
		}
	}
	
	String extractHeading(final String str) throws Exception
	{
		// Extract category heading
		int c = 0;
		while (c < str.length() && str.charAt(c) != '"')
			c++;
		if (c >= str.length())
			throw new Exception("Failed to find option category heading: " + str);	
		
		int cc = c+1;
		while (cc < str.length() && str.charAt(cc) != '"')
			cc++;
		if (cc < 0 || cc >= str.length())
			throw new Exception("Failed to find option category heading: " + str);		
		
		heading = str.substring(c+1, cc);

		//System.out.println("Category heading is: " + heading);
	
		return str.substring(cc+1);
	}
	
	String extractTag(final String str) throws Exception
	{	
		// Extract category tag
		int c = str.indexOf('<');
		if (c < 0)
			throw new Exception("Failed to find option category tag: " + str);	
		
		int cc = StringRoutines.matchingBracketAt(str, c);
		if (cc < 0 || cc >= str.length())
			throw new Exception("Couldn't close option bracket '>' in " + str.substring(c));
		cc++;

		tag = str.substring(c+1, cc-1);

		//System.out.println("Category tag is: " + primaryTag); 
	
		return str.substring(cc+1);
	}
	
	String extractArgTags(final String strIn) throws Exception
	{
		// Extract arg names
		if (!strIn.contains("args:"))
			throw new Exception("Option category must define args:{...}." + strIn);
		
		int c = strIn.indexOf("args:");
		if (c < 0)
			throw new Exception("No option argument tags of form args:{...}: " + strIn);
		
		c = strIn.indexOf("{");
		if (c < 0)
			throw new Exception("Couldn't find opening bracket '{' in option category " + strIn.substring(c)); 
		int cc = StringRoutines.matchingBracketAt(strIn, c);
		if (cc < 0 || cc >= strIn.length())
			throw new Exception("Couldn't find closing bracket '}' in option category " + strIn.substring(c));

		String str = strIn.substring(c, cc).trim();
		
		// Extract each arg, denoted <expression>
		while (true)
		{
//			System.out.println("str is: " + str);
			
			int a = str.indexOf("<");
			if (a < 0)
				break;  // no more options
			
			int aa = StringRoutines.matchingBracketAt(str, a);
			if (aa < 0 || aa >= str.length())
				throw new Exception("No closing bracket '>' for option argument: " + str);
			
			final String arg = str.substring(a+1, aa);
			argTags.add(arg);

//			System.out.println("-- arg is: " + arg);

			str = str.substring(aa+1).trim();
		}

//		System.out.println(argTags.size() + " argTags: " + argTags);

		return strIn.substring(cc+1);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("<" + tag + "> \"" + heading + "\"");
		if (argTags != null)
		{
			sb.append(" [ ");
			for (final String arg : argTags)
				sb.append(arg + " ");
			sb.append("]");
		}
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------

}
