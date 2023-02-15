package parser; 

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import exception.UnusedOptionException;
import main.FileHandling;
import main.StringRoutines;
import main.grammar.Define;
import main.grammar.DefineInstances;
import main.grammar.Description;
import main.grammar.Report;
import main.options.Option;
import main.options.OptionArgument;
import main.options.OptionCategory;
import main.options.Ruleset;
import main.options.UserSelections;

//-----------------------------------------------------------------------------

/**
 * Expands raw user game description (.lud) into full game description with 
 * defines, options, rulesets and ranges realised for compilation.
 * 
 * @author cambolbro
 */
public class Expander
{
	private static final int MAX_EXPANSIONS  = 1000;
	private static final int MAX_CHARACTERS  = 1000000;
	private static final int MAX_RANGE       = 1000;
	private static final int MAX_DEFINE_ARGS = 20;

	/** Placeholder character for null define parameters, e.g. ("Jump" ~ (from) ~ ~) */
	public static final String DEFINE_PARAMETER_PLACEHOLDER = "~";
   	
	//-------------------------------------------------------------------------

	/**
	 * Private constructor; don't allow class to be constructed.
	 */
	private Expander()
	{
	}
	
	//-------------------------------------------------------------------------
		
	/**
	 * Expands options and defines in user string to give full description. Pre:
	 * userDescription has already been set. Post: Result is stored in
	 * gameDescription.
	 * 
	 * @param description     The description.
	 * @param userSelections  The user selections.
	 * @param report          The report.
	 * @param isVerbose       True if this is verbose.
	 */
	public static void expand
	(
		final Description    	description,
		final UserSelections  	userSelections,
		final Report 		  	report,
		final boolean         	isVerbose
	)
	{
		if (isVerbose)
		{
			//System.out.println("+++++++++++++++++++++\nExpanding:\n" + description.raw());
			report.addLogLine("+++++++++++++++++++++\nExpanding:\n" + description.raw());
		}

		String str = new String(description.raw());
		
		// Remove comments before any expansions
		str = removeComments(str);

		checkDefineCase(str, report);
		
		// Must realise options before expanding defines -- not sure why
		str = realiseOptions(str, description, userSelections, report);
		if (report.isError())
			return;
		
		str = realiseRulesets(str, description, report);
		if (report.isError())
			return;
		
//		// Store interim result as the "options expanded" description
//		String strOptions = cleanUp(str, report); 
//		if (report.isError())
//			return;
//
//		final int c = strOptions.indexOf("(metadata");
//		if (c >= 0)
//		{
//			// Remove metadata
//			strOptions = strOptions.substring(0, c).trim();
//		}
//
//		System.out.println("strOptions expanded:\n" + strOptions);
//
//		tokenise(strOptions, description, report, isVerbose);
//		description.setOptionsExpanded(new String(description.tokenForest().toString()));  // format tokens nicely
//		
//		System.out.println("Options expanded:\n" + description.optionsExpanded());
		
		// Continue expanding defines for full description
		str = expandDefines(str, report, description.defineInstances());
		if (report.isError())
			return;
		
		// Do again after expanding defines, as external defines could have comments
		str = removeComments(str);

		// Do after expanding defines, as external defines could have ranges
		str = expandRanges(str, report);
		if (report.isError())
			return;

		str = expandSiteRanges(str, report);
		if (report.isError())
			return;
		
		// Do metadata string extraction here rather than in compiler,
		// as the metadata text needs defines and options expanded.
		// Result is stored in expandedMetadataString.
		str = extractMetadata(str, description, userSelections, report);		
		if (report.isError())
			return;
		
		str = cleanUp(str, report);
		if (report.isError())
			return;
				
		if (isVerbose)
		{
			//System.out.println("Cleaned up:\n" + str);
			report.addLogLine("Cleaned up:\n" + str);
		}
			
		// Tokenise expanded string to get full description
		if (str == null || str.trim().isEmpty())
			str = description.metadata();  // no game description, try tokenising metadata instead
		
		tokenise(str, description, report, isVerbose);
		
//		System.out.println("Token errors = " + report.isError());
		
		description.setExpanded(new String(description.tokenForest().toString()));  // format tokens nicely
		
//		System.out.println(description.tokenTree().dump(""));
	}
	
	//-------------------------------------------------------------------------

	public static String cleanUp(final String strIn, final Report report)
	{
		String str = strIn;
		
		// Remove unneeded internal spaces
		str = str.replaceAll("\n",  " ");
		str = str.replaceAll("\r",  " ");
		str = str.replaceAll("\t",  " ");
		
		while (str.contains("    "))
			str = str.replaceAll("    ", " ");

		while (str.contains("  "))
			str = str.replaceAll("  ", " ");
		
		str = str.replaceAll(" \\)", "\\)");  // regex doesn't like ")"
		str = str.replaceAll("\\( ", "\\(");  // regex doesn't like "("
		str = str.replaceAll(" \\}", "\\}");
		str = str.replaceAll("\\{ ", "\\{");

		while (str.contains(": "))
			str = str.replaceAll(": ", ":");

//		// Hack so that ItemCount() objects don't need to be be named. 
//		// Do this AFTER all defines have been expanded!
//		str = str.replaceAll("\\(\"", "\\(itemCount \"");		

		str = handleDoubleBrackets(str, report);  // do this after reducing spaces
		if (report != null && report.isError())
			return null;
		
		return str;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Create token tree from the specified (expanded) string.
	 * 
	 * @param str         The string.
	 * @param description The description.
	 * @param report      The report.
	 * @param isVerbose   True if this is verbose.
	 */
	public static void tokenise
	(
		final String      str, 
		final Description description,
		final Report 	  report,
		final boolean     isVerbose
	)
	{
		//description.setTokenTree(new Token(str, report));
		description.tokenForest().populate(str, report);
		
		if (isVerbose)
		{
			//System.out.println("\nToken tree:\n" + description.tokenForest().tokenTree());
			report.addLogLine("\nToken tree:\n" + description.tokenForest().tokenTree());
		}

		if 
		(
			description.tokenForest().tokenTree() == null 
			|| 
			description.tokenForest().tokenTree().type() == null
		)
		{
			//System.out.println(description.tokenTree());
			//throw new CantDecomposeException("Expander.tokenise()");
			report.addError("Expander can't tokenise the game description.");
			return;  // not necessary but good practice
		}
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Check for lowercase define labels.
	 */
	public static void checkDefineCase(final String str, final Report report)
	{
		// System.out.println(str);
		
		int c = 0;
		while (true)
		{
			c = str.indexOf("(define \"", c);
			if (c < 0)
				break;
			
			final char ch = str.charAt(c + 9);
			if (!Character.isUpperCase(ch))
			{
				// First char in define is not an uppercase letter
				int cc = c + 9;
				while (cc < str.length() && StringRoutines.isTokenChar(str.charAt(cc)))
					cc++;
				
				final String name = str.substring(c+9, cc);
				
				final String msg = "Lowercase define name \"" + name + ".";
				report.addLogLine(msg);
				report.addWarning(msg);
				report.addNote(msg);
				//report.addError(msg);
				System.out.println(msg);
			}
			c++;
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Handle double brackets. 
	 * These will typically occur when a bracketed ("Define") is expanded.
	 * @return Game description with double brackets removed.
	 */
	private static String handleDoubleBrackets
	(
		final String strIn, 
		final Report report
	)
	{
		if (!strIn.contains("(("))
			return new String(strIn);  // nothing to do
			
		String str = new String(strIn);
		
		while (true)
		{
			final int c = str.indexOf("((");
			if (c < 0)
				break;  // all done
			
			final int cc = StringRoutines.matchingBracketAt(str, c);
			if (cc < 0 || cc >= str.length())
			{
				//throw new UnclosedClauseException(str.substring(c));
				if (report != null)
					report.addError("Couldn't close clause '" + Report.clippedString(str.substring(c), 20) + "'.");
				return null;
			}
		
			if (str.charAt(cc) == ')' && str.charAt(cc-1) == ')')
			{
				// Remove outer brackets (front and back)
				str = str.substring(0, cc) + str.substring(cc+1);  // remove trailing duplicates
				str = str.substring(0, c) + str.substring(c+1);    // remove leading duplicates
			}
			else
			{
				// Opening pair do not have closing pair, should never occur
				if (report != null)
					report.addError("Opening bracket pair '((' in '" + Report.clippedString(str.substring(c), 20) + "' does not have closing pair.");
				return null;
			}
		}
		return str;
	}

	//-------------------------------------------------------------------------

	/**
	 * Realise options.
	 * @return Game description with defines expanded in-place.
	 */
	public static String realiseOptions
	(
		final String          strIn, 
		final Description     description,
		final UserSelections  userSelections,
		final Report          report
	)
	{
		description.gameOptions().clear();
		
		if (!strIn.contains("(option \""))
			return new String(strIn);  // nothing to do

		String str = new String(strIn);		
		str = extractOptions(str, description, report);
		if (report.isError() || str == null)
			return null;
		
		int[] optionSelections;
		try
		{
			optionSelections = description.gameOptions().computeOptionSelections(userSelections.selectedOptionStrings());
		}
		catch (final UnusedOptionException e)
		{
			// Revert to default options for this game
			//e.printStackTrace();
			System.err.println("Reverting to default options for game due to unrecognised option being specified!");
			userSelections.setSelectOptionStrings(new ArrayList<String>());
			optionSelections = description.gameOptions().computeOptionSelections(userSelections.selectedOptionStrings());
		}
		
		// Expand the user-selected choice (or default) in each option group
		for (int cat = 0; cat < description.gameOptions().numCategories(); cat++)
		{
			final OptionCategory category = description.gameOptions().categories().get(cat);
			if (category.options().size() > 0)
			{
				final Option option = category.options().get(optionSelections[cat]);				
				if (option.arguments().isEmpty())
				{
					// Can be valid if options are being used to define headings in the metadata
					return str;
				}
				str = expandOption(str, option, report);
				if (report.isError() || str == null)
					return null;
			}
		}
		return str;
	}

	//-------------------------------------------------------------------------

	/**
	 * Stores a copy of each option and removes their definition from the game description.
	 * @return Game description with options removed and stored in list.
	 */
	private static final String extractOptions
	(
		final String      strIn,
		final Description description,
		final Report      report
	)
	{
		// Extracts options in this format:
		//
		//   (option "Board Size" <Size> args:{ <dim> <start> <end> }
		//     {
		//       ("3x3" <3 3>  <9>  <4> "Played on a square 3x3 board.")
		//       ("4x4" <4 4> <16>  <8> "Played on a square 4x4 board.")*
		//       ("5x5" <5 5> <25> <12> "Played on a square 5x5 board.")**
		//     }
		//   )
		
		String str = new String(strIn);		
		
		while (str.contains("(option \""))
		{
			final int c = str.indexOf("(option \"");
			
			// Find matching closing bracket
			int cc = StringRoutines.matchingBracketAt(str, c);
			if (cc < 0 || cc >= str.length())
			{
				//throw new UnclosedClauseException(str.substring(c));
				report.addError("Couldn't close clause '" + Report.clippedString(str.substring(c), 20) + "'.");
				return null;
			}
			cc++;
			
			final OptionCategory category = new OptionCategory(str.substring(c, cc));
			
			// Check whether option args are well formed, e.g. "Step/hop move" is a bad label
			for (final Option option : category.options())
			{
				for (final String header : option.menuHeadings())
					if (header.contains("/"))
					{
						report.addError("Bad '/' in option header \"" + header + "\".");
						return null;
					}
			}
			description.gameOptions().add(category);

			str = str.substring(0, c) + str.substring(cc);  // remove from source string
		}
		return str;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param strIn
	 * @return Game description with all occurrences of option expanded.
	 */
	private static String expandOption
	(
		final String strIn, 
		final Option option,
		final Report report
	)
	{
		String str = new String(strIn);

		// Pass 1: Match all <Category:arg> instances first
		for (final OptionArgument arg : option.arguments())
		{
			final String name = arg.name();
			if (name == null)
			{
				//throw new IllegalArgumentException("Some option arguments are named but this one is not.");
				report.addError("Some option arguments are named but this one is not.");
				return null;
			}
			final String marker = "<" + option.tag() + ":" + name + ">";
	
			int iterations = 0;
			while (str.contains(marker))
			{
				if (++iterations > MAX_EXPANSIONS)
				{
					//throw new InvalidOptionException("An option has more than " + MAX_EXPANSIONS + " expansions.");	
					report.addError("An option has more than " + MAX_EXPANSIONS + " expansions.");
					return null;
				}

				if (str.length() > MAX_CHARACTERS)
				{
					//throw new InvalidOptionException("The option " + option.toString() + " has more than " + MAX_CHARACTERS + " characters.");	
					report.addError("The option " + option.toString() + " has more than " + MAX_CHARACTERS + " characters.");
					return null;
				}
			
				// Expand this instance in-place
				final int c = str.indexOf(marker);
				str = str.substring(0, c) + arg.expression() + str.substring(c + marker.length());
			}
		}

		// **
		// ** Don't do this test: rulesets can have primary tags without secondary tags!
		// **
//			// Check that author didn't forget to use names in instances.
//			final String marker = "<" + option.label() + ">";
//			if (str.contains(marker))
//			{
//				System.out.println("Marker " + marker + " but named markers expected, in: " + str);
//				throw new InvalidOptionException("Option <label> should be in <label:name> format.");		
//			}
		
		// Pass 2: Expand instances with <Category> as arg 0 (was modulus order)
		final String marker = "<" + option.tag() + ">";
	
//		final int numArgs = option.arguments().size();
//		int count = 0;

		int iterations = 0;
		while (str.contains(marker))
		{
			if (++iterations > MAX_EXPANSIONS)
			{
				//throw new InvalidOptionException("An option has more than " + MAX_EXPANSIONS + " expansions.");	
				report.addError("An option has more than " + MAX_EXPANSIONS + " expansions.");
				return null;
			}

			if (str.length() > MAX_CHARACTERS)
			{
				//throw new InvalidOptionException("The option " + option.toString() + " has more than " + MAX_CHARACTERS + " characters.");	
				report.addError("The option " + option.toString() + " has more than " + MAX_CHARACTERS + " characters.");
				return null;
			}
			
			// Expand this instance in-place
			final int c = str.indexOf(marker);
			final int index = 0;  //count++ % numArgs;
			str = str.substring(0, c) + option.arguments().get(index).expression() + str.substring(c + marker.length());
		}
		return str;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Realise rulesets.
	 */
	public static String realiseRulesets
	(
		final String      strIn,
		final Description description,
		final Report      report
	)
	{
		if (!strIn.contains("(rulesets"))
			return strIn;  // nothing to do
	
		final String str = extractRulesets(strIn, description, report);
		
		return str;
	}

	/**
	 * Stores a copy of each ruleset and removes their definition from the game description.
	 * @return Game description with rulesets removed and stored in list.
	 */
	private static final String extractRulesets
	(
		final String      strIn,
		final Description description,
		final Report      report
	)
	{
		description.clearRulesets();
		
		String str = new String(strIn);
		
		int c = str.indexOf("(rulesets");
		if (c < 0)
		{
			//throw new BadSyntaxException("Rulesets not found.", str);
			report.addError("Rulesets not found.");
			return null;
		}
			
		int cc = StringRoutines.matchingBracketAt(str, c);
		if (cc < 0)
		{
			//throw new BadSyntaxException("No closing bracket ')' in rulesets.", str);
			report.addError("No closing bracket ')' in rulesets '" + Report.clippedString(str.substring(c), 20) + "'.");		
			return null;
		}

		String rulesetsStr = str.substring(c+8, cc-1).trim();
		str = str.substring(0, c) + str.substring(cc);  // remove rulesets from game description

		// Process this rulesets string
		while (true)
		{
			c = rulesetsStr.indexOf("(ruleset ");
			if (c < 0)
				break;  // no more rulesets
			
			cc = StringRoutines.matchingBracketAt(rulesetsStr, c);
			if (cc < 0)
			{
				//throw new BadSyntaxException("No closing bracket ')' in ruleset.", rulesetsStr);
				report.addError("No closing bracket ')' in ruleset '" + Report.clippedString(rulesetsStr.substring(c), 20) + "'.");
				return null;
			}
			String rulesetStr = rulesetsStr.substring(c, cc+1);
			
			int priorityIndex = cc + 1;
			while (priorityIndex < rulesetsStr.length() && rulesetsStr.charAt(priorityIndex) == '*')
			{
				rulesetStr += '*';  // preserve priority markers
				priorityIndex++;
			}
			final Ruleset ruleset = new Ruleset(rulesetStr);
			description.add(ruleset);
			rulesetsStr = rulesetsStr.substring(cc+1);
		}
		
		// Just delete any remaining rulesets in the game description
		while (true)
		{
			c = str.indexOf("(rulesets");
			if (c < 0)
				break;  // no more rulesets
			
			cc = StringRoutines.matchingBracketAt(str, c);
			if (cc < 0)
			{
				//throw new BadSyntaxException("No closing bracket ')' in extra rulesets.", str);
				report.addError("No closing bracket ')' in extra rulesets '" + Report.clippedString(str.substring(c), 20) + "'.");
				return null;
			}
			str = str.substring(0, c) + str.substring(cc);  // remove this extra set of rulesets
		}
		return str;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Expand defines iteratively until no more expansions occur.
	 */
	public static String expandDefines
	(
		final String strIn,
		final Report report, 
		final Map<String, DefineInstances> defineInstances
	)
	{
		// Load game AI metadata define (if any)
		final Define knownAIDefine = loadKnownAIDefine(strIn, report);
		if (report.isError())
			return null;
		
		// Expand defines
		int defineIterations = 0;
		String str = strIn;
		while (true)
		{
			final String strDef = expandDefinesPass(str, knownAIDefine, report, defineInstances);
			if (report.isError())
				return null;
			
			if (str.equals(strDef))
				break;  // no more defines
			str = strDef;

			if (++defineIterations > MAX_EXPANSIONS || str.length() > MAX_CHARACTERS)
			{
				// Probably an infinite loop, e.g. (define "A" "B") (define "B" "A")
				//throw new DefineExpansionException("A suspected infinitely recursive define.");
				report.addError("Suspected infinitely recursive define.");
				return null;
			}
		}
		return str;
	}
	
	/**
	 * Expand current set of defines, which may expand into more defines.
	 */
	private static String expandDefinesPass
	(
		final String strIn, 
		final Define knownAIDefine,
		final Report report, 
		final Map<String, DefineInstances> defineInstances
	)
	{
		final List<Define> defines = new ArrayList<>();
		
		final Map<String, Define> knownDefines = KnownDefines.getKnownDefines().knownDefines();
		
		String str = extractDefines(strIn, defines, report);
		if (report.isError())
			return null;

		// Repeatedly: expand in-file defines as thoroughly as possible,
		//             then all predefined defines as thoroughly as possible.
		boolean didExpandAny;  	
		do 
		{
			didExpandAny = false;
		
			// First, expand all in-file defines as thoroughly as possible
			final boolean[] didExpand = new boolean[1]; 	
			do 
			{
				didExpand[0] = false;
				for (final Define def : defines)
				{	
					if (str.contains(def.tag()))
					{
						str = expandDefine(str, def, didExpand, report, defineInstances);
						if (report.isError())
							return null;
					}
				}		
				if (didExpand[0])
					didExpandAny = true;
			} while (didExpand[0]);
	
			// Secondly, expand all predefined defines as thoroughly as possible except in the metadata
			String metadata = "";
			final int c = str.indexOf("(metadata");
			if (c >= 0)
			{
				metadata = str.substring(c, StringRoutines.matchingBracketAt(str, c) + 1);
				str = str.substring(0, c);
			}
			do 
			{
				didExpand[0] = false;
				for (final Map.Entry<String, Define> entry : knownDefines.entrySet()) 
				{
					final Define def = entry.getValue();
					if (str.contains(def.tag()))
					{
						str = expandDefine(str, def, didExpand, report, defineInstances);
						if (report.isError())
							return null;
					}
				}
				if (didExpand[0])
					didExpandAny = true;
			} while (didExpand[0]);
			str = str + metadata;

			// Thirdly, expand known game AI metadata define (if found)
			if (knownAIDefine != null)
			{
				if (str.contains(knownAIDefine.tag()))
				{
					didExpand[0] = false;
					str = expandDefine(str, knownAIDefine, didExpand, report, defineInstances);
					if (report.isError())
						return null;
			
					if (didExpand[0])
						didExpandAny = true;
				}
			}
		} while (didExpandAny);
		
		return str;
	}

	/**
	 * Stores a copy of each define and removes their definition from the game description.
	 * @param strIn
	 * @return Game description with defines removed and stored in list.
	 */
	public static String extractDefines
	(
		final String strIn, 
		final List<Define> defines,
		final Report report
	)
	{
		final int[] extent = new int[2];  // char indices of opening and closing brackets
		
		String str = new String(strIn);
		while (str.contains("(define "))
		{		
			final Define define = interpretDefine(str, extent, report, false);
			if (report != null && report.isError())
				return null;
				
			if (define == null)
			{
				System.out.println("** Failed to load define:\n" + str);
				continue;
			}
			
			defines.add(define);
			str = str.substring(0, extent[0]) + str.substring(extent[1]+1);
		}
		return str;
	}

	/**
	 * @param str
	 * @param extent
	 * @param report
	 * 
	 * @return The real define.
	 */
	public static Define interpretDefine
	(
		final String str, 
		final int[] extent, 
		final Report report, 
		final boolean isKnown
	)
	{
		// 1. Remove opening and closing brackets
		int c = str.indexOf("(define ");
		if (c < 0)
			return null;  // not a define
		
		final int cc = StringRoutines.matchingBracketAt(str, c);
		if (cc < 0)
		{
//			System.out.println("** Bad define string: " + str);
//			throw new BadSyntaxException("define", "Badly formed define. Should start and end with `(define' and end with a bracket.");
			if (report != null)
				report.addError("Could not close '(define ...' in '" + Report.clippedString(str.substring(c), 20) + "'.");
			return null;
		}		

		String desc = str.substring(c+1, cc).trim();
	
		if (extent != null)
		{
			// Store extent of define (char indices of opening and closing brackets) 
			extent[0] = c;
			extent[1] = cc;
		}
			
		// 2. Remove header '(define "name"' which contains two quotes
		c = 0;
		int numQuotes = 0;
		while (c < desc.length())
		{
			if (desc.charAt(c) == '"')
				numQuotes++;
			if (numQuotes >= 2)
				break;
			c++;
		}
		if (numQuotes < 2)
		{
//			throw new BadSyntaxException("define", "Badly formed define. Should be start (define \"name\".");
			if (report != null)
				report.addError("Badly fomred '(define \"name\"...' in '" + Report.clippedString(desc, 20) + "'.");
			return null;
		}
			
		// Find the label for the define (first String in quotes)
		final int openingQuoteIdx = desc.indexOf("\"");
		final int closingQuoteIdx = desc.indexOf("\"", openingQuoteIdx + 1);
		final String key = desc.substring(openingQuoteIdx, closingQuoteIdx + 1);
	
		desc = desc.substring(c+1).trim();
	
		final Define define = new Define(key, desc, isKnown);
		
		//System.out.println("Define is: " + define);
		
		return define;
	}

	/**
	 * @param strIn
	 * @return Game description with all occurrences of define expanded.
	 */
	private static String expandDefine
	(
		final String    strIn, 
		final Define    define, 
		final boolean[] didExpand,
		final Report report, 
		final Map<String, DefineInstances> defineInstances
	)
	{
		String str = new String(strIn);

//		System.out.println("Expanding define: " + define.tag() + "...");
		
		final int len = define.tag().length();

		int iterations = 0;
		int c = 0;
		
		while (true)
		{
			if (++iterations > MAX_EXPANSIONS)
			{
//				throw new DefineExpansionException("A define has more than " + MAX_EXPANSIONS + " expansions.");	
				report.addError("Define has more than " + MAX_EXPANSIONS + " expansions '" + Report.clippedString(str, 20) + "'.");
				return null;
			}

			if (str.length() > MAX_CHARACTERS)
			{
//				throw new DefineExpansionException("A define has more than " + MAX_CHARACTERS + " characters.");	
				report.addError("Define has more than " + MAX_CHARACTERS + " characters '" + Report.clippedString(str, 20) + "'.");
				return null;
			}

			// Check for next occurrence of define label
			c = str.indexOf(define.tag(), c+1);
			if (c == -1)
				break;  // no more occurrences
			
			if (protectedSubstring(str, c))
			{
				// Protected substring found
				continue;
			}
			
			int cc = c + len-1;  // if no brackets
			if (str.charAt(c-1) == '(')
			{
				// Take brackets into account
				c--;
				cc = StringRoutines.matchingBracketAt(str, c);					
				if (cc < 0 || cc >= str.length())
				{
//					throw new BadSyntaxException("define", "Failed to handle parameter in define.");
					report.addError("Failed to handle parameter in define '" + Report.clippedString(str.substring(c), 20) + "'.");
					return null;
				}
			}
			final String argString  = str.substring(c, cc+1).trim();	
			final List<String> args = extractDefineArgs(argString, report);
			if (report.isError())
				return null;
				
			final String exprn = expandDefineArgs(define, args, report);
			if (report.isError())
				return null;
			
			if (defineInstances != null)
			{
				// Add this expression to the relevant DefineInstances record
				DefineInstances defIn = defineInstances.get(define.tag());
				if (defIn == null)
				{
					defIn = new DefineInstances(define);
					defineInstances.put(define.tag(), defIn);
				}
				defIn.addInstance(new String(exprn));
			}
			
			// Do the expansion
			str = str.substring(0, c) + exprn + str.substring(cc+1);
			didExpand[0] = true;
		}
		str = str.replace("<DELETE_ME>", "");  // remove null parameter placeholders

		return str;
	}
	
	//-------------------------------------------------------------------------

	private static final List<String> extractDefineArgs
	(
		final String argString,
		final Report report
	)
	{
		final List<String> args = new ArrayList<String>();

		String str = new String(argString.trim());
		
		if (str.charAt(0) == '(')
		{
			final int cc = StringRoutines.matchingBracketAt(str, 0); 
			if (cc == -1)
			{
//				throw new BadSyntaxException("define", "Failed to read bracketed clause from: " + str + ".");
				report.addError("Failed to read bracketed clause '(...)' from '" + Report.clippedString(str, 20) + "'.");
				return null;
			}
			str = str.substring(1, cc);
		}
		
		// Move to first arg after label
		int a = 0;
		while (a < str.length() && !Character.isWhitespace(str.charAt(a)))
			a++;

		if (a >= str.length())
			return args;  // no arguments
		
		str = str.substring(a).trim();
		
		while (!str.isEmpty())
		{
			// Extract next parameter from head of list
			int aTo = 0;
			
			if (StringRoutines.isOpenBracket(str.charAt(0)))
			{
				// Read in bracketed clause
				aTo = StringRoutines.matchingBracketAt(str, 0);
				if (aTo == -1)
				{
//					throw new BadSyntaxException("define", "Failed to read bracketed clause from: " + str + ".");
					report.addError("Failed to read bracketed clause from '" + Report.clippedString(str, 20) + "'.");
					return null;
				}
				aTo++;  // step past closing bracket
			}
			else if (str.charAt(0) == '"')
			{
				// Read in named bracketed clause
				aTo = StringRoutines.matchingQuoteAt(str, 0);
				if (aTo == -1)
				{
//					throw new BadSyntaxException("define", "Failed to read quoted clause \"...\" from: " + str + ".");
					report.addError("Failed to read quoted clause '\"...\"' from '" + Report.clippedString(str, 20) + "'.");
					return null;
				}
				aTo++;  // step past closing bracket
			}
			else
			{
				// Read in next symbol
				aTo = 0;
				while (aTo < str.length() && !Character.isWhitespace(str.charAt(aTo)))
				{
					if (str.charAt(aTo) == ':')
					{
						// Named parameter, check if remainding of expression is bracketed
						if (StringRoutines.isOpenBracket(str.charAt(aTo+1)))
						{
							aTo = StringRoutines.matchingBracketAt(str, aTo+1);
							if (aTo == -1)
							{
//								throw new BadSyntaxException("define", "Failed to read bracketed clause '{...}' from: " + str + ".");
								report.addError("Failed to read bracketed clause '{...}' from '" + 
													Report.clippedString(str, 20) + "'.");
								return null;
							}
							aTo++;  // step past closing bracket
							break;
						}
					}
					aTo++;
				}
			}
			
			if (aTo >= str.length())
				aTo = str.length();
			
			final String arg = str.substring(0, aTo);
			
			args.add(new String(arg));
			str = str.substring(aTo).trim();
		}
		return args;
	}
	
	private static String expandDefineArgs
	(
		final Define       define, 
		final List<String> args,
		final Report       report
	)
	{
		String exprn = new String(define.expression());

		for (int n = 0; n < MAX_DEFINE_ARGS; n++)
		{
			final String marker = "#" + (n+1);

			if (!exprn.contains(marker))
				break;  // no more args
				
			int innerIterations = 0;
			while (exprn.contains(marker))
			{
				if (++innerIterations > MAX_EXPANSIONS)
				{
//					throw new DefineExpansionException("A define has more than " + MAX_EXPANSIONS + " expansions (inner loop).");				
					report.addError("Define has more than " + MAX_EXPANSIONS + " expansions '" + 
										Report.clippedString(exprn, 20) + "'.");
					return null;
				}

				if (exprn.length() > MAX_CHARACTERS)
				{
//					throw new DefineExpansionException("A define has more than " + MAX_CHARACTERS + " characters (inner loop exprn).");	
					report.addError("Define has more than " + MAX_CHARACTERS + " characters '" + 
										Report.clippedString(exprn, 20) + "'.");
					return null;
				}

				final int m = exprn.indexOf(marker);

				String arg = "<DELETE_ME>";  // mark for deletion
				if (n < args.size() && !args.get(n).equals(DEFINE_PARAMETER_PLACEHOLDER))
					arg = args.get(n);

				exprn = exprn.substring(0, m) + arg + exprn.substring(m+2);
				
//				System.out.println("- expanding exprn: " + exprn);
				
				if (arg.charAt(0) == '#')
					break;  // arg is an arg itself
			}
		}
		return exprn;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param str String to check.
	 * @param fromIndex Index of substring.
	 * @return Whether the specified substring within str should be protected 
	 *         from expansion by a define. 
	 *         These include "(game", "(match", "(instance", etc.    
	 */
	public static boolean protectedSubstring
	(
		final String str, 
		final int fromIndex
	)
	{
		// Tokens that protect the following string from being expanded as a define, 
		// in case the string accidentally mathces a known define.
		final String[] safeTokens = { "game", "match", "instance" };
		
		// Step back to previous token (might be a bracket)
		int c = fromIndex-1;
		while (c >= 0) 
		{
			if (StringRoutines.isTokenChar(str.charAt(c)))
				break;
			c--;
		}
		
		if (c < 0)
		{
			System.out.println("** Warning: Failed to find previous token (probably from define).");
			System.out.println("** fromIndex=" + fromIndex + ", str:=\n" + str);
			return false;
		}
		
		// Get token string
		String token = "";
		while (c >= 0)
		{
			final char ch = str.charAt(c);
			if (!StringRoutines.isTokenChar(ch))
				break;
			token = ch + token;
			c--;
		}
		if (c < 0)
		{
			System.out.println("** Warning: Failed to read previous token (probably from define).");
			System.out.println("** fromIndex=" + fromIndex + ", str:=\n" + str);
			return false;
		}
		
		for (final String safeToken : safeTokens)
		if (token.equals(safeToken))
			return true;  // this substring is protected
		
		return false;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Load known AI metadata define from file.
	 */
	private static Define loadKnownAIDefine
	(
		final String strIn, 
		final Report report
	)	
	{
		if (!strIn.contains("(ai") || !strIn.contains("_ai"))
			return null;
		
		Define knownAIDefine = null;
		
		final String gameName = StringRoutines.gameName(strIn);
		String aiName = gameName;
		
		// Check that the ai file name, if any, matches the game name
		final int c = strIn.indexOf("_ai\"");
		if (c >= 0)
		{
			int cc = c;
			while (cc >= 0 && strIn.charAt(cc) != '"')
				cc--;
			
			aiName = strIn.substring(cc + 1, c);
//			if (!aiName.equals(gameName))
//			{
//				report.addWarning("Define '" + aiName + "_ai' found in AI metadata; use '" + gameName + "_ai' or remove it.");
//				return Expander.interpretDefine("(define \"" + aiName + "_ai\")", null, report, true);
//			}
		}
	
		final String[] defs = FileHandling.getResourceListingSingle(Expander.class, "def_ai/", aiName + "_ai.def");
 		if (defs == null)
        {
        	// Not a JAR
        	try 
        	{ 		
        		// Start with known _ai.def file
				final URL url = Expander.class.getResource("/def_ai/Chess_ai.def");
        		String path = new File(url.toURI()).getPath();
				path = path.substring(0, path.length() - "Chess_ai.def".length());
				
		       final File root = new File(path);
		       final File[] list = root.listFiles();

		       if (list == null) 
		    	   return null;

		       for (final File file : list)
		    	   if 
		    	   (
		    	       file != null 
		    	       && 
		    	       !file.isDirectory() 
		    	       && 
		    	       file.getName() != null 
		    	       && 
		    	       file.getName().equals(aiName + "_ai.def")
		    	   )
		    	   {
		    		   // Found the file
		    		   final String filePath = path + file.getName();
		    		   knownAIDefine = 	KnownDefines.processDefFile
		    				   			(
		    				   				filePath.replaceAll(Pattern.quote("\\"), "/"), 
		    				   				"/def_ai/", 
		    				   				report
		    				   			);
		    		   if (report.isError())
		    			   return null;
		    	   }
        	}
        	catch (final Exception e)
			{
				e.printStackTrace();
			}
        }
        else
        {
        	for (String def : defs)
        	{
        		def = def.replaceAll(Pattern.quote("\\"), "/");
        		final String[] defSplit = def.split(Pattern.quote("/"));
        		final String filename = defSplit[defSplit.length - 1];
        		
        		if (filename.equals(aiName + "_ai.def"))
        		{
        			knownAIDefine = KnownDefines.processDefFile(def, "/def_ai/", report);
        			if (report.isError())
		    			return null;
         		}
        	}
        }
		
 		if (knownAIDefine == null)
 		{
 			knownAIDefine = Expander.interpretDefine("(define \"" + aiName + "_ai\")", null, report, false);
 			report.addWarning("Failed to load AI define specified in metadata; reverting to default AI.");
 		}
 		
        return knownAIDefine;
 	}

	//-------------------------------------------------------------------------

	/**
	 * @param strIn
	 * @return Game description with all number range occurrences expanded.
	 */
	public static String expandRanges
	(
		final String strIn,
		final Report report
	)
	{
		if (!strIn.contains(".."))
			return strIn;  // nothing to do
		
		String str = new String(strIn);

		int ref = 1;
		boolean inCompletion = false;
		while (ref < str.length() - 2)
		{
			if(str.charAt(ref) == '[')
				inCompletion = true;

			if(str.charAt(ref) == ']')
				inCompletion = false;
			
			if(!inCompletion)
			{
				if
				(
					str.charAt(ref) == '.' && str.charAt(ref+1) == '.'
					&&
					Character.isDigit(str.charAt(ref-1))
					&&
					Character.isDigit(str.charAt(ref+2))
				)
				{
					// Is a range: expand it
					int c = ref - 1;
					while (c >= 0 && Character.isDigit(str.charAt(c)))
						c--;
					c++;
					final String strM = str.substring(c, ref);
					final int m = Integer.parseInt(strM);
	
					c = ref + 2;
					while (c < str.length() && Character.isDigit(str.charAt(c)))
						c++;
					final String strN = str.substring(ref+2, c);
					final int n = Integer.parseInt(strN);
	
					if (Math.abs(n - m) > MAX_RANGE)
					{
						//throw new BadRangeException(MAX_RANGE);
						report.addError("Range exceeded maximum of " + MAX_RANGE + ".");
						return null;
					}
					
					// Generate the expanded range substring
					String sub = " ";
	
					final int inc = (m <= n) ? 1 : -1;
					for (int step = m; step != n; step += inc)
					{
						if (step == m || step == n)
							continue;  // don't include end points
	
						sub += step + " ";
					}
	
					str = str.substring(0, ref) + sub + str.substring(ref+2);
					ref += sub.length();
				}
			}
			ref++;
		}
		return str;
	}
	
	/**
	 * @param strIn
	 * @return Game description with all site range occurrences expanded.
	 */
	public static String expandSiteRanges
	(
		final String strIn,
		final Report report
	)
	{
		if (!strIn.contains(".."))
			return strIn;  // nothing to do
		
		String str = new String(strIn);

		boolean inCompletion = false;
		int ref = 1;
		while (ref < str.length() - 2)
		{
			if(str.charAt(ref) == '[')
				inCompletion = true;

			if(str.charAt(ref) == ']')
				inCompletion = false;
			
			if(!inCompletion)
			{
				if
				(
					str.charAt(ref) == '.' && str.charAt(ref+1) == '.'
					&&
					str.charAt(ref-1) == '"' && str.charAt(ref+2) == '"'
				)
				{
					// Must be a site range
					int c = ref - 2;
					while (c >= 0 && str.charAt(c) != '"')
						c--;
									
					final String strC = str.substring(c+1, ref-1);
	//				System.out.println("strC: " + strC);
					
					int d = ref + 3;
					while (d < str.length() && str.charAt(d) != '"')
						d++;
					d++;
									
					final String strD = str.substring(ref+3, d-1);
	//				System.out.println("strD: " + strD);
					
	//				System.out.println("Range: " + str.substring(c, d));
					
					if (strC.length() < 2 || !StringRoutines.isLetter(strC.charAt(0)))
					{
						report.addError("Bad 'from' coordinate in site range: " + str.substring(c, d));
						return null;
					}			
					final int fromChar = Character.toUpperCase(strC.charAt(0)) - 'A';
					
					if (strD.length() < 2 || !StringRoutines.isLetter(strD.charAt(0)))
					{
						report.addError("Bad 'to' coordinate in site range: " + str.substring(c, d));
						return null;
					}			
					final int toChar = Character.toUpperCase(strD.charAt(0)) - 'A';
					
	//				System.out.println("fromChar=" + fromChar + ", toChar=" + toChar + ".");
					
					final int fromNum = Integer.parseInt(strC.substring(1));
					final int toNum   = Integer.parseInt(strD.substring(1));
	
	//				System.out.println("fromNum=" + fromNum + ", toNum=" + toNum + ".");
	
					// Generate the expanded range substring
					String sub = "";
					
					for (int m = fromChar; m < toChar + 1; m++)
						for (int n = fromNum; n < toNum + 1; n++)
							sub += "\"" + (char)('A' + m) + (n) + "\" ";
	
					str = str.substring(0, c) + sub.trim() + str.substring(d);
					ref += sub.length();
				}
			}
			ref++;
		}
		
//		System.out.println(str);
		
		return str;
	}

	//-------------------------------------------------------------------------

	/**
	 * Extracts metadata and stores it in expandedMetadataString.
	 * @param strIn   The complete expanded game description. 
	 * @param options The Game options with which we're compiling.
	 * @return Game description with metadata removed and stored in expandedMetadataString.
	 */
	private static String extractMetadata
	(
		final String         strIn, 
		final Description    description,
		final UserSelections userSelections,
		final Report         report
	)
	{
		final int c = strIn.indexOf("(metadata");
		if (c < 0)
			return strIn;  // no metadata to extract
		
		// Find matching closing bracket
		final int cc = StringRoutines.matchingBracketAt(strIn, c);
		if (cc < 0)
		{
			//throw new BadSyntaxException("metadata", "Failed to close (metadata...");
			report.addError("Failed to close '(metadata' in '" + 
								Report.clippedString(strIn.substring(c), 20) + "'.");
			return null;
		}

		String str = strIn.substring(c, cc+1);
		
//		// Check if next char is '{'
//		int b = 9;  // skip "(metadata "
//		char ch = ' ';
//		while (b < str.length())
//		{
//			ch = str.charAt(b);
//			if (ch == '{' || ch == '(' || StringRoutines.isNameChar(ch))
//				break;
//			b++;
//		}
//		if (ch == '{')
//		{
//			final int bb = StringRoutines.matchingBracketAt(str, b);
//			str = str.substring(0, b) + str.substring(bb+1);
//		}
		
		str = removeUnselectedOptionParts(str, description, userSelections, report);
		if (report.isError())
			return null;
	
		description.setMetadata(new String(str));

		final String removed = strIn.substring(0, c) + strIn.substring(cc+1);
		return removed;
	}

	/**
	 * Remove parts that require different game options from those selected.
	 * @return game description with unselected option parts removed.
	 */
	private static String removeUnselectedOptionParts
	(
		final String         strIn,
		final Description    description,
		final UserSelections userSelections,
		final Report         report
	)
	{
		String str = new String(strIn);
		
		// First thing we'll do; try to auto-select ruleset based on options if
		// we do not already have a selected ruleset
		int activeRuleset = userSelections.ruleset();
					
		if (activeRuleset < 0)
		{
			activeRuleset = description.autoSelectRuleset(userSelections.selectedOptionStrings());
			if (activeRuleset >= 0)
				userSelections.setRuleset(activeRuleset);
		}
		
		final List<String> active = description.gameOptions().allOptionStrings
									(
										userSelections.selectedOptionStrings()
									);
		while (true)
		{
			final int optsCondStartIdx = str.indexOf("(useFor ");
			
			if (optsCondStartIdx < 0)
				break;  // no more chunks of metadata that are restricted by options
			
			final int optsCondClosingBracketIdx = StringRoutines.matchingBracketAt(str, optsCondStartIdx);
			if (optsCondClosingBracketIdx < 0)
			{
//				throw new BadSyntaxException("metadata", "Failed to close (useFor ...");
				report.addError("Failed to close '(useFor' in '" + 
									Report.clippedString(str.substring(optsCondStartIdx), 20) + "'.");
				return null;
			}
			
			// Find the substring that specifies all the required rulesets / options
			final int requirementsSubstrIdxStart;
			final int requirementsSubstrIdxEnd;
			
			final int nextOpeningCurlyIdx = str.indexOf('{', optsCondStartIdx);
			final int nextQuoteIdx = str.indexOf('"', optsCondStartIdx);
			
			if (nextQuoteIdx < 0)
			{
//				throw new BadSyntaxException("metadata", "There must always be a quote somewhere after (useFor ");
				report.addError("No quote after '(useFor' in '" + 
									Report.clippedString(str.substring(optsCondStartIdx), 20) + "'.");
				return null;
			}
			
			if (nextOpeningCurlyIdx >= 0 && nextOpeningCurlyIdx < nextQuoteIdx)
			{
				// We have curly braces
				final int openCurlyBracketIdx = nextOpeningCurlyIdx;
				final int closCurlyBracketIdx = StringRoutines.matchingBracketAt(str, openCurlyBracketIdx);
				
				if (closCurlyBracketIdx < 0)
				{
//					throw new BadSyntaxException("metadata", "Failed to close curly bracket of (useFor {...");
					report.addError("Failed to close curly bracket '{' in '" + 
										Report.clippedString(str.substring(optsCondStartIdx), 20) + "'.");
					return null;
				}
				
				requirementsSubstrIdxStart = openCurlyBracketIdx + 1;
				requirementsSubstrIdxEnd = closCurlyBracketIdx;
			}
			else
			{
				// We don't have curly braces
				final int openingQuoteIdx = nextQuoteIdx;
				int closingQuoteIdx = str.indexOf('"', openingQuoteIdx + 1);
				
				while (closingQuoteIdx >= 0 && str.charAt(closingQuoteIdx - 1) == '\\')
				{
					// Escaped quote, so keep moving on
					closingQuoteIdx = str.indexOf('"', closingQuoteIdx + 1);
				}
				
				if (closingQuoteIdx < 0)
				{
					//throw new BadSyntaxException("metadata", "Failed to close quote after (useFor \"...");
					report.addError("Failed to close quote after '(useFor' in '" + 
										Report.clippedString(str.substring(openingQuoteIdx), 20) + "'.");
					return null;
				}
				
				requirementsSubstrIdxStart = openingQuoteIdx;
				requirementsSubstrIdxEnd = closingQuoteIdx + 1;
			}

			// Extract the required options
			final List<String> requiredOptions = new ArrayList<String>();
			final String requirementsSubstr = str.substring(requirementsSubstrIdxStart, requirementsSubstrIdxEnd);
			int requiredOptOpenQuoteIdx = requirementsSubstr.indexOf('"');
			
			while (requiredOptOpenQuoteIdx >= 0)
			{
				int requiredOptClosingQuoteIdx = requirementsSubstr.indexOf('"', requiredOptOpenQuoteIdx + 1);
				while (requirementsSubstr.charAt(requiredOptClosingQuoteIdx - 1) == '\\')
					requiredOptClosingQuoteIdx = requirementsSubstr.indexOf('"', requiredOptClosingQuoteIdx + 1);
				
				if (requiredOptClosingQuoteIdx < 0)
				{
					//throw new BadSyntaxException("metadata", "Failed to close quote of a String in (useFor ...)");
					report.addError("Failed to close String quote in '" + 
										Report.clippedString(requirementsSubstr.substring(requiredOptClosingQuoteIdx), 20) + "'.");
					return null;
				}
				
				requiredOptions.add(requirementsSubstr.substring(requiredOptOpenQuoteIdx + 1, requiredOptClosingQuoteIdx));
				
				// Move on to next pair of quotes (if it exists)
				requiredOptOpenQuoteIdx = requirementsSubstr.indexOf('"', requiredOptClosingQuoteIdx + 1);
			}
			
			// Safety check to make sure that all the required options also actually really exist (as options or rulesets)
			for (final String requiredOption : requiredOptions)
			{
				if (!description.gameOptions().optionExists(requiredOption))
				{
					boolean foundMatch = false;
					for (final Ruleset ruleset : description.rulesets())
					{
						if (requiredOption.equals(ruleset.heading()))
						{
							foundMatch = true;
							break;
						}
					}
					
					if (!foundMatch)
					{
//						throw new RuntimeException("Metadata has option requirement for option or ruleset that does not exist: " + requiredOption);
						report.addError("Metadata has option requirement for option or ruleset that does not exist: " + requiredOption);
						return null;
					}
				}
			}
			
			// Check if fail any of the requirements for options
			boolean failedRequirement = false;
			for (final String requiredOpt : requiredOptions)
			{
				// Each of these requiredOpt strings is quoted, need to remove those when checking condition
				if (!active.contains(requiredOpt.replaceAll(Pattern.quote("\""), "")))
				{
					// Also try ruleset
					if 
					(
						activeRuleset < 0 
						|| 
						!description.rulesets().get(activeRuleset).heading().equals
						(
							requiredOpt.replaceAll(Pattern.quote("\""), "")
						)
					)
					{
						failedRequirement = true;
						break;
					}
				}
			}
			
			final StringBuffer stringBuffer = new StringBuffer(str);
			if (failedRequirement)
			{
				// We have to delete this entire block of metadata
				stringBuffer.replace(optsCondStartIdx, optsCondClosingBracketIdx + 1, "");
			}
			else
			{
				// We satisfy option requirements, so should only cut out the little
				// parts used to describe those requirements: (useFor {...} and the closing bracket )
				//
				// Note: we first cut away stuff at the end, and afterwards stuff in the beginning,
				// because if we cut in the beginning first, the later indices would need shifting
				stringBuffer.replace(optsCondClosingBracketIdx, optsCondClosingBracketIdx + 1, "");
				stringBuffer.replace(optsCondStartIdx, requirementsSubstrIdxEnd + 1, "");
			}
			str = stringBuffer.toString();
		}
		
		return str;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Remove user comments from file (but not from within strings).
	 * 
	 * @param strIn The string in entry.
	 * @return The new comment.
	 */
	public static String removeComments(final String strIn)
	{
		// TODO: Only remove comments outside strings!
		
		String str = new String(strIn);
		int c = 0;
		while (c < str.length()-1)
		{
			if (str.charAt(c) == '"')
			{
				c = StringRoutines.matchingQuoteAt(str, c) + 1;
				if (c <= 0)
				{
					// Something went wrong: probably not a comment, might be  
					// an embedded string or ruleset name in quotes
					return str;
				}
			} 
			else if (str.charAt(c) == '/' && str.charAt(c+1) == '/')
			{
				// Is a comment
				int cc = c + 2;
				while (cc < str.length() && str.charAt(cc) != '\n')
					cc++;
				str = str.substring(0, c) + str.substring(cc);  // remove to end of line
			}
			else
			{
				c++;
			}
		}
		return str;
	}

	//-------------------------------------------------------------------------
			
}
