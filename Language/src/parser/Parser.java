package parser; 

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import compiler.Arg;
import compiler.ArgClass;
import compiler.ArgTerminal;
import compiler.exceptions.CompilerException;
import completer.Completer;
import completer.Completion;
import grammar.Grammar;
import main.Constants;
import main.StringRoutines;
import main.grammar.Description;
import main.grammar.GrammarRule;
import main.grammar.Instance;
import main.grammar.ParseItem;
import main.grammar.Report;
import main.options.UserSelections;

//-----------------------------------------------------------------------------

/**
 * Parses .lud game descriptions according to the current Ludii grammar.
 * 
 * @author cambolbro
 */
public class Parser
{
	//-------------------------------------------------------------------------

	/**
	 * Private constructor; don't allow class to be constructed.
	 */
	private Parser()
	{
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Compile option for testing purposes. Does not interact with UI settings. Not
	 * for release! Create and throws away GameOptions each time.
	 * 
	 * @param description
	 * @param userSelections
	 * @param report
	 * @param isVerbose
	 * @return Executable Game object if can be compiled, else null.
	 */
	public static boolean parseTest
	(
		final Description    description,
		final UserSelections userSelections,
		final Report         report,
		final boolean        isVerbose
	)
	{
		return expandAndParse(description, userSelections, report, isVerbose);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param description
	 * @param userSelections
	 * @param report
	 * @param isVerbose
	 * @return Whether the .lud can be parsed.
	 */
	public static boolean expandAndParse
	(
		final Description    description,
		final UserSelections userSelections,
		final Report         report,
		final boolean        isVerbose
	)
	{
		final boolean allowExamples = false;
		return expandAndParse(description, userSelections, report, allowExamples, isVerbose);
	}

	/**
	 * @param description
	 * @param userSelections
	 * @param report
	 * @param allowExamples Suppress warnings for ludeme examples, e.g. for LLR.
	 * @param isVerbose
	 * @return Whether the .lud can be parsed.
	 */
	public static boolean expandAndParse
	(
		final Description    description,
		final UserSelections userSelections,
		final Report         report,
		final boolean		 allowExamples,
		final boolean        isVerbose
	)
	{
		if (Completer.needsCompleting(description.rawGameDescription()))
		{
			final String rawGame = description.rawGameDescription();
			System.out.println("Raw game description is:\n" + rawGame);
		
			final List<Completion> completions = Completer.complete(rawGame, false, report);
			System.out.println(completions.size() + " completions found.");
			
			if (!completions.isEmpty())
			{
				// Replace raw description string passed in with best completion 
				description.setRaw(completions.get(0).raw());
			}
		}
		
		try
		{
			try
			{
				//report.clear();
				
				final Map<String, DefineInstances> defineInstances = new HashMap<String, DefineInstances>();
				
				Expander.expand(description, userSelections, report, defineInstances, isVerbose);				
				if (report.isError())
				{
//					System.out.println("Errors while expanding (A):");
//					for (final String error : report.errors())
//						System.out.println("* " + error);
					return false;  // failed to expand -- return error
				}
				
				if (defineInstances != null && isVerbose)
				{
					System.out.println("Define instances:");
					for (final DefineInstances defIn : defineInstances.values())
						System.out.println(defIn + "\n");
				}
			}
			catch (final Exception e)
			{
				if (report.isError())
				{
//					System.out.println("Errors while expanding (B):");
//					for (final String error : report.errors())
//						System.out.println("* " + error);
					return false;  // failed to expand -- return error
				}

				//errors.add("Catching exception from Expander...");  //new String(e.getMessage()));
				//errors.add("Could not expand game description. Maybe a misplaced bracket pair '(..)' or '{..}'?");
			}
			return parseExpanded(description, userSelections, report, allowExamples, isVerbose);
		} 
		catch (final CompilerException e)
		{
			if (isVerbose)
				e.printStackTrace();
			throw new CompilerException(e.getMessageBody(description.raw()), e);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			throw new IllegalArgumentException(e);
		}
	}
		
	//-------------------------------------------------------------------------
	
	/**
	 * Public function that calling code should use.
	 *
	 * Note: Assumes that Expander has already been called to expand the game description.
	 *
	 * @param allowExamples Suppress warnings for ludeme examples, e.g. for LLR.
	 * @return Whether .lud file parses the current grammar.
	 */
	private static boolean parseExpanded
	(
		final Description    description,
		final UserSelections userSelections,
		final Report         report,
		final boolean		 allowExamples,
		final boolean        isVerbose
	) 
	{
		//report.clear();
		
//		if (Constants.argCombos == null)
//			Constants.createCombos();

		//--------------- Check raw game text ----------------
		
		if (description.raw() == null || description.raw().isEmpty())
		{
			report.addError("Could not expand empty game description. This message was brought to you by Dennis.");
			return false;
		}
		
		// Extract the raw game description without metadata (assume metadata is after all game description)
		String rawGame = description.raw();
		
		//final boolean topLevelRaw = isTopLevelLudeme(rawGame);
		if (!allowExamples)  // || topLevelRaw)
		{
			// Check version info
			checkVersion(rawGame, report);
		}
		
		// Remove comments
		rawGame = Expander.removeComments(rawGame);
		
		// Remove metadata
		final int mdc = rawGame.indexOf("(metadata");
		if (mdc != -1)
			rawGame = rawGame.substring(0, mdc).trim();
		
		// Check for matching quotes in raw description
		checkQuotes(rawGame, report);
		if (report.isError())
			return false;

		// Check for matching brackets
		checkBrackets(rawGame, report);
		if (report.isError())
			return false;
				
		//--------------- Check expanded text ----------------

		if (description.expanded() == null)
		{
			// Allow more specific tests above to check the problem 
			report.addError("Could not expand. Check that bracket pairs '(..)' and '{..}' match.");
			return false;
		}

		// Check for matching quotes in expanded description
		checkQuotes(description.expanded(), report);
		if (report.isError())
			return false;

		// Check for matching brackets
		checkBrackets(description.expanded(), report);
		if (report.isError())
			return false;
		
		if (!allowExamples)
		{
			checkOptionsExpanded(description.expanded(), report);
			if (report.isError())
				return false;
		}
		
		//--------------- Check token tree ----------------

		// Check token tree was created successfully
		if 
		(
			description.tokenForest().tokenTree() == null 
			|| 
			description.tokenForest().tokenTree().type() == null
		)
		{
			//System.out.println("** Parser.parse(): No token tree."); 
			report.addLogLine("** Parser.parse(): No token tree.");
			report.addError("Couldn't generate token tree from expanded game description.");
			return false;
		}
		
		if (isVerbose)
		{
			//System.out.println("+++++++++++++++++++++\nParsing:\n" + description.expanded());
			report.addLogLine("+++++++++++++++++++++\nParsing:\n" + description.expanded());
		}
		
		//--------------- Check parse tree ----------------

		// Create parse tree
		description.createParseTree();
		if (description.parseTree() == null)
		{
			report.addError("Couldn't generate parse tree from token tree.");
			return false;
		}
		
		// Check can match tokens with symbols 
		matchTokensWithSymbols(description.parseTree(), Grammar.grammar(), report);		
		if (report.isError())
			return false;
		
		final boolean gameOrMatch = isGameOrMatch(description.expanded());
		if (!allowExamples || gameOrMatch)
		{
			checkStrings(description.expanded(), report);
			if (report.isError())
				return false;
		}
		
		//--------------- Check against grammar ----------------

		//System.out.println("\n" + parseTree.compare());
		//System.out.println("\n" + parseTree.dump(""));
		
		// Check against grammar
		description.parseTree().parse(null, report, null);
		//description.parseTree().parse(null, report, "");
		//System.out.println("\n" + description.parseTree().dump(""));

		// Look for deepest failure and report all errors at that level
		final int failureDepth = description.parseTree().deepestFailure();		
		if (failureDepth >= 0)
			description.parseTree().reportFailures(report, failureDepth);
		
		return !report.isError();
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Finds symbols that match this item's token.
	 */
	private static void checkQuotes(final String str, final Report report)
	{
		// Check quotations
		final int numQuotes = StringRoutines.numChar(str, '"');
//        System.out.println(numQuotes + " quotes.");
        
		if (numQuotes % 2 != 0)
		{
			report.addError("Mismatched quotation marks '\"'.");
			return;
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Finds symbols that match this item's token.
	 */
	private static void checkBrackets(final String str, final Report report)
	{
		// Check brackets
		int numOpen  = StringRoutines.numChar(str, '(');
        int numClose = StringRoutines.numChar(str, ')');
//        System.out.println("numOpen=" + numOpen + ", numClose=" + numClose + " '()'.");
        
		if (numOpen < numClose)
		{
			if (numClose - numOpen == 1)
				report.addError("Missing an open bracket '('.");
			else
				report.addError("Missing " + (numClose - numOpen) + " open brackets '('.");
			return;
		}

		if (numOpen > numClose)
		{
			if (numOpen - numClose == 1)
				report.addError("Missing a close bracket ')'.");
			else
				report.addError("Missing " + (numOpen - numClose) + " close brackets ')'.");
			return;
		}
		
		// Check braces
		numOpen  = StringRoutines.numChar(str, '{');
        numClose = StringRoutines.numChar(str, '}');
//        System.out.println("numOpen=" + numOpen + ", numClose=" + numClose + " '{}'.");
        
		if (numOpen < numClose)
		{
			if (numClose - numOpen == 1)
				report.addError("Missing an open brace '{'.");
			else
				report.addError("Missing " + (numClose - numOpen) + " open braces '{'.");
			return;
		}

		if (numOpen > numClose)
		{
			if (numOpen - numClose == 1)
				report.addError("Missing a close brace '}'.");
			else
				report.addError("Missing " + (numOpen - numClose) + " close braces '}'.");
			return;
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Check that all options have been expanded.
	 */
	private static void checkOptionsExpanded(final String expanded, final Report report)
	{
		int c = -1;  // start at char 0 (c+1)
		while (true)
		{
			c = expanded.indexOf("<", c+1);
			if (c == -1)
				break;
			
			int cc = StringRoutines.matchingBracketAt(expanded, c);
			if (cc == -1)
				cc = c + 5;
			
			final char ch = expanded.charAt(c+1);
			if (StringRoutines.isNameChar(ch))
			{
				report.addError("Option tag " + (expanded.substring(c, cc+1)) + " not expanded.");
				return;
			}
		}
	}
	
    //-------------------------------------------------------------------------
	
	/**
	 * Finds symbols that match this item's token.
	 */
	private static void matchTokensWithSymbols
	(
		final ParseItem item, 
		final Grammar   grammar, 
		final Report    report
	)
	{
		if (item.token() == null)
		{
			//System.out.println("Null token for item: " + item.dump(" "));
			report.addError("Null token for item: " + item.dump(" "));
			return;
		}
		
		try
		{
//			item.matchSymbols(grammar, report);
			matchSymbols(item, grammar, report);
		}
		catch (final Exception e)
		{
			//errors.add("Catching exception...");
			//System.out.println("- Catching matchSymbols() exception...");
		}
		
		if (item.instances().isEmpty())
		{
			switch (item.token().type())
			{
			case Terminal:
				String error = "Couldn't find token '" + item.token().name() + "'.";
				if (Character.isLowerCase(item.token().name().charAt(0)))
					error += " Maybe missing bracket '(" + item.token().name() + " ...)'?";
				report.addError(error);
				break;
			case Class:
				report.addError("Couldn't find ludeme class for token '" + item.token().name() + "'.");
				break;
			case Array:
				// Not an error
				break;
			}
		}
		
		for (final ParseItem arg : item.arguments())
			matchTokensWithSymbols(arg, grammar, report); 
	}
	
	/**
	 * @param item
	 * @param grammar
	 * @param report
	 */
	public static void matchSymbols(final ParseItem item, final Grammar grammar, final Report report)
	{
		item.clearInstances();
		
		Arg arg;
		switch (item.token().type())
		{
		case Terminal:  // String, number, enum, True, False
			arg = new ArgTerminal(item.token().name(), item.token().parameterLabel());
			arg.matchSymbols(grammar, report);  // instantiates actual object(s), not need for clauses
			
			final String str = arg.symbolName();
			if (!str.isEmpty() && Character.isLowerCase(str.charAt(0)))
			{
				final String msg = "Terminal constant '" + str + "' is lowercase.";
				report.addWarning(msg);
				System.out.println(msg);  // to remind us to tidy up the game descriptions
			}
			
			for (final Instance instance : arg.instances())
				item.add(instance);
			break;
		case Class:
			// Find matching symbols and their clauses
			arg = new ArgClass(item.token().name(), item.token().parameterLabel());
			arg.matchSymbols(grammar, report);

			for (final Instance instance : arg.instances())
				item.add(instance);

			// Associate clauses with each instance of arg
			for (final Instance instance : item.instances())
			{
				final GrammarRule rule = instance.symbol().rule();
				if (rule != null)
					instance.setClauses(rule.rhs());
			}
			break;
		case Array:
			// Do nothing: use instances of each element on a case-by-case basis
			break;
		}
	}

	//-------------------------------------------------------------------------

	private static void checkStrings(final String expanded, final Report report)
	{
		// Get number of players?
		int numPlayers = 0;
		
		final int playersFrom = expanded.indexOf("(players");
		if (playersFrom >= 0)
		{
			final int playersTo = StringRoutines.matchingBracketAt(expanded, playersFrom);
			if (playersTo >= 0)
			{
				// Check if defined in form (players { (player ...) (player ...) }) 
				int p = 0;
				while (true)
				{
					p = expanded.indexOf("(player", p+1);
					
					if (p == -1)
						break;
					
					// Check that "(player":
					// 1. is within scope of (players ...) as more than one Player class
					// 2. but doesn't match "(players"
					if (p > playersFrom && p < playersTo && expanded.charAt(p + 7) != 's')
						numPlayers++;
				}
				
				if (numPlayers == 0)
				{
					// Check if defined in form (players N) 
					p = expanded.indexOf("(players");
					if (p != -1)
					{
						final int pp = StringRoutines.matchingBracketAt(expanded, p);
						if (pp == -1)
						{
							report.addError("No closing bracket for '(players ...'.");
							return;
						}
						
						final String playerString = expanded.substring(p, pp+1).trim();
						final String countString  = expanded.substring(p+8, pp).trim();
						final String[] subs = countString.split(" ");
						
		//				System.out.println("countString is '" + countString + "', subs[0] is '" + subs[0] + "'.");				
						
						try
						{
							numPlayers = Integer.parseInt(subs[0]);
						}
						catch (final Exception e)
						{
		//					System.out.println(numPlayers + " players.");
							report.addError("Couldn't extract player count from '" + playerString + "'.");
							return;
						}
					}
				}
			}
		}
		//System.out.println(numPlayers + " players.");
		
		
		// Get list of known strings: player names, named items, phases, coordinates, etc. 
		final Map<Integer, String> knownStrings = new HashMap<Integer, String>();
		
		// Add known defaults
		final String[] defaults =
		{ "Player", "Board", "Hand", "Ball", "Bag", "Domino" };
		
		for (final String def : defaults)
			knownStrings.put(Integer.valueOf(def.hashCode()), def);

		extractKnownStrings(expanded, "(game",              true,  knownStrings, report);
		extractKnownStrings(expanded, "(match",             true,  knownStrings, report);
		extractKnownStrings(expanded, "(subgame",           true,  knownStrings, report);
		extractKnownStrings(expanded, "(subgame",           false, knownStrings, report);
		extractKnownStrings(expanded, "(players",           false, knownStrings, report);
		extractKnownStrings(expanded, "(equipment",         false, knownStrings, report);
		extractKnownStrings(expanded, "(phase",             true,  knownStrings, report);
		extractKnownStrings(expanded, "(vote",              true,  knownStrings, report);
		extractKnownStrings(expanded, "(move Vote",         true,  knownStrings, report);
		extractKnownStrings(expanded, "(is Proposed",       true,  knownStrings, report);
		extractKnownStrings(expanded, "(is Decided",        true,  knownStrings, report);
		extractKnownStrings(expanded, "(note",              true,  knownStrings, report);
		extractKnownStrings(expanded, "(trigger",           true,  knownStrings, report);
		extractKnownStrings(expanded, "(is Trigger",        true,  knownStrings, report);
		extractKnownStrings(expanded, "(trackSite",         false, knownStrings, report);
		extractKnownStrings(expanded, "(set Var",           false, knownStrings, report);
		extractKnownStrings(expanded, "(var",               false, knownStrings, report);
		extractKnownStrings(expanded, "(remember",          false, knownStrings, report);
		extractKnownStrings(expanded, "(set RememberValue", false, knownStrings, report);
		extractKnownStrings(expanded, "(values Remembered", false, knownStrings, report);
		
//		System.out.println("Known strings:");
//		for (final String known : knownStrings.values())
//			System.out.println("> " + known); 
			
		// Check rest of description for strings
		//int c = ee;  // start at end of (equipment ...) 
		int c = -1;
		while (true)
		{
			c = expanded.indexOf('"', c+1);
			if (c == -1)
				break;  // no more strings in description
			
			final int cc = StringRoutines.matchingQuoteAt(expanded, c);
			if (cc == -1)
			{
				report.addError("Couldn't close string: " + expanded.substring(c));
				return;
			}
			
			final String str = expanded.substring(c+1, cc);

//			System.out.println("Checking clause: " + str);
			
			if (!StringRoutines.isCoordinate(str))  //str.length() > 2)  // ignore trivial coordinates
			{
				// Check whether this string is known
				boolean match = false;
				
				final int key = str.hashCode();
				if (knownStrings.containsKey(Integer.valueOf(key)))
					match = true;
				
				if (!match)
				{
					// Compare each individual string
					//for (final String item : knownStrings)
					for (final String known : knownStrings.values()) 
					{
						if (known.equals(str))
						{
							match = true;
							break;
						}
	
//						System.out.println("str=" + str + ", known=" + known);
						
						if (!known.contains(str) && !str.contains(known))
							continue;  // not a possible match
	
						if (Math.abs(str.length() - known.length()) > 2)
							continue;  // not a numbered variant
						
						// Check player index is in player range
						final int pid = StringRoutines.numberAtEnd(str);
						//System.out.println("pid=" + pid + ", numPlayers=" + numPlayers);
//						if (pid < 0 || pid > numPlayers + 1)
//						{
//							errors.add("Bad player index " + pid + " on item '" + str + "'.");
//							return;
//						}
						
						if (pid > numPlayers && !str.contains("Hand"))
							report.addWarning("Item '" + str + "' is numbered " + pid + " but only " + numPlayers + " players.");
						
						match = true;
						break;
					}
				}
				
				if (!match && str.length() <= 5 && StringRoutines.isCoordinate(str))
					continue;  // allow any coordinate to match

				if (!match)
				{
					report.addError("Could not match string '" + str + "'. Misspelt define or item?");
					return;
				}
			}
			
			c = cc + 1;
		}
	}
	
	/**
	 * Extracts strings from within the specified clause(s).
	 * Set 'firstStringPerClause' to false if only want to pick up first String, e.g. game or phase name. 
	 */
	private static void extractKnownStrings
	(
		final String expanded, 
		final String targetClause, 
		final boolean firstStringPerClause, 
		final Map<Integer, String> knownStrings,
		final Report report
	)
	{
		int e = -1;
		while (true)
		{
			// Find next instance of target clause
			e = expanded.indexOf(targetClause, e+1);
			if (e == -1)
				return;
			
//			System.out.println("Found clause starting " + targetClause + "...");
			
			// Extract clause
			final int ee = StringRoutines.matchingBracketAt(expanded, e);
			if (ee == -1)
			{
				report.addError("Couldn't close string: " + expanded.substring(e));
				return;
			}
			final String clause = expanded.substring(e, ee+1);
			
//			System.out.println("Clause is: " + expanded.substring(e));
			
			// Get substrings within clause
			int i = -1;
			while (true)
			{
				i = clause.indexOf('"', i+1);
				if (i == -1)
					break;  // no more substrings in clause
			
//				System.out.println("substring from: " + clause.substring(i));
				
				final int ii = StringRoutines.matchingQuoteAt(clause, i);
				if (ii == -1)
				{
					report.addError("Couldn't close item string: " + clause.substring(i));
					return;
				}
				
				final String known = clause.substring(i+1, ii);	
				
//				System.out.println("known: " + known);
				
				if (!StringRoutines.isCoordinate(known))
				{
					// Ignore coordinates (e.g. "A1", "A123", "AA123", "123")
					
//					System.out.println("Putting: " + known);
					
					knownStrings.put(Integer.valueOf(known.hashCode()), known);
					if (firstStringPerClause)
						break;  // exit this clause, but still check for subsequent clauses
				}
				
				i = ii;  // + 1;
			}			
		}
	}
	
	//-------------------------------------------------------------------------

	private static void checkVersion(final String raw, final Report report)
	{
		final int v = raw.indexOf("(version");
		if (v == -1)
		{
//			System.out.println("** No version info.");
			report.addWarning("No version info.");
			return;  // no version info
		}
		
		int s = v; 
		while (s < raw.length() && raw.charAt(s) != '"')
			s++;
		
		if (s >= raw.length())
		{
			//System.out.println("** Parser.checkVersion(): Couldn't find version string.");
			report.addError("Couldn't find version string in (version ...) entry.");
			return;
		}
		
		final int ss = StringRoutines.matchingQuoteAt(raw, s);
		if (ss == -1)
		{
			report.addError("Couldn't close version string in (version ...) entry.");
			return;
		}
			
		final String version = raw.substring(s+1, ss);

		// Integer value representing the version number of the game description.
		final int gameVersionInteger = Integer.valueOf(version.split("\\.")[0]).intValue() * 1000000 
									+ Integer.valueOf(version.split("\\.")[1]).intValue() * 1000 
									+ Integer.valueOf(version.split("\\.")[2]).intValue();
		
		// Integer value representing the version number of the app.
		final int appVersionInteger = Integer.valueOf(Constants.LUDEME_VERSION.split("\\.")[0]).intValue() * 1000000 
				+ Integer.valueOf(Constants.LUDEME_VERSION.split("\\.")[1]).intValue() * 1000 
				+ Integer.valueOf(Constants.LUDEME_VERSION.split("\\.")[2]).intValue();
		
		final int result = appVersionInteger - gameVersionInteger;
		
		if (result < 0)
			report.addWarning("Game version (" + version + ") newer than app version (" + Constants.LUDEME_VERSION + ").");
		else if (result > 0)
			report.addWarning("Game version (" + version + ") older than app version (" + Constants.LUDEME_VERSION + ").");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param cls
	 * @return Formatted tooltip help string for the specified class. For use in the
	 *         ludeme editors.
	 */
	public static String tooltipHelp(final Class<?> cls)
	{
		if (cls.getSimpleName().equalsIgnoreCase("add"))
		{
			return 
				"add\n" +
				"Add a piece...\n" +
				"\n" +
				"Format\n" +
				"(add ...)\n" +
				"where:\n" +
				"• <int>: Minimum length of lines.\n" +
				"• [<absoluteDirection>]: Direction category that potential lines must belong to.\n" +
				"\n" +
				"Example\n" +
				"(add ...)\n";
		}
		
		return "No tooltip found for class " + cls.getName();
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param cls
	 * @return List of ludemes (or terminal objects) that could be used as
	 *         alternatives for the specified class. For use in the ludeme editors.
	 */
	public static List<Class<?>> alternativeClasses(final Class<?> cls)
	{
		final List<Class<?>> alternatives = new ArrayList<Class<?>>();
		
		if (cls.getSimpleName().equalsIgnoreCase("and"))
		{
			// A sampling of some ludemes that can be used instead of Line
			Class<?> alternative = null;
			try { alternative = Class.forName("game.functions.booleans.math.And"); }
			catch (final ClassNotFoundException e) { e.printStackTrace(); }		
			alternatives.add(alternative);
		}
		
		return alternatives;
	}

	/**
	 * @param cls
	 * @return List of ludemes (or terminal objects) that could be used as
	 *         alternatives for the specified class. For use in the ludeme editors.
	 */
	public static List<Object> alternativeInstances(final Class<?> cls)
	{
		final List<Object> alternatives = new ArrayList<Object>();
		
//		if (cls.getSimpleName().equalsIgnoreCase("and"))
//		{
//			// A sampling of some ludemes that can be used instead of Line
//			alternatives.add(new And(null, null));
//		}

		// TODO: Implement alternative ludemes list.
		
		System.out.println("* Note: Alternative ludemes list not implemented yet.");
		
		return alternatives;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param description Game description (may be expanded or not).
	 * @param cursorAt    Character position of cursor.
	 * @param isSelect    True if selected.
	 * @param type        Type of selection.
	 * @return Range of scope of current token.
	 */
	public static TokenRange tokenScope
	(
		final String description, final int cursorAt, final boolean isSelect,
		final SelectionType type
	)
	{
		System.out.println("Selection type: " + type);
		
		if (cursorAt <= 0 || cursorAt >= description.length())
		{
			System.out.println("** Grammar.classPaths(): Invalid cursor position " + cursorAt + " specified.");
			return null;
		}
		
		// Get full keyword at cursor
		int c = cursorAt - 1;
		char ch = description.charAt(c);
		
		if (!StringRoutines.isTokenChar(ch))
		{
			// Not currently on a token (or at the start of one): return nothing
			return null;
		}
		
		while (c > 0 && StringRoutines.isTokenChar(ch))
		{
			c--;
			ch = description.charAt(c);
			
			if (ch == '<' && StringRoutines.isLetter(description.charAt(c + 1)))
				break;  // should be a rule
		}
						
		// Get token
		int cc = cursorAt;
		char ch2 = description.charAt(cc);
		
		while (cc < description.length() && StringRoutines.isTokenChar(ch2))
		{
			cc++;
			if (cc < description.length())
				ch2 = description.charAt(cc);
		}
		
		if (cc >= description.length())
		{
			System.out.println("** Grammar.classPaths(): Couldn't find end of token scope from position " + cursorAt + ".");
			return null;
		}
		
//		if (ch2 == ':')
//		{
//			// Token is a parameter name: return nothing
//			return null;
//		}
		
		final String token = description.substring(c+1, cc);
		System.out.println("token: " + token);
		
		if (isSelect)
		{
			// Select entire entry
//			System.out.println("isSelect substring is: " + description.substring(c, cc));
			
			if (description.charAt(c) == ':' || description.charAt(c - 1) == ':')
			{
				// Is named parameter (second test is to catch "name:<rule>")
				c -= 2;
				while (c > 0 && StringRoutines.isTokenChar(description.charAt(c)))
					c--;
				c++;
				
//				System.out.println("....new substring ':' is: " + description.substring(c, cc));
			}
			
			if (description.charAt(cc) == ':' || description.charAt(cc + 1) == ':')
			{
				// Is named parameter (clicked on name, not value)
				cc++;
				while (cc < description.length() && StringRoutines.isTokenChar(description.charAt(cc)))
					cc++;
				
//				System.out.println("....new substring 'name:' is: " + description.substring(c, cc));
			}
			
			if (c > 0 && description.charAt(c - 1) == '[')
			{
				// Is an optional parameter
				c--;
				cc = StringRoutines.matchingBracketAt(description, c) + 1;
				
//				System.out.println("....new substring '[' A is: " + description.substring(c, cc));
			}
			else if (c > 0 && description.charAt(c) == '[')
			{
				// Is an optional parameter
				cc = StringRoutines.matchingBracketAt(description, c) + 1;
				
//				System.out.println("....new substring '[' B is: " + description.substring(c, cc));
			}
			else if (description.charAt(cc + 1) == ']')
			{
				cc++;
				c = cc - 1;
				while (c > 0 && description.charAt(c) != '[')
					c--;
				
//				System.out.println("....new substring ']' is: " + description.substring(c, cc));
			}
		}
		
		// Handle primitive and predefined types
		if (token.charAt(0) == '"')
		{
			// Is a string: note that quotes '"' are a valid token character 
			System.out.println("String scope includes: " + description.substring(c, cc));
			return new TokenRange(c, cc);
		}
		
		if (token.equalsIgnoreCase("true"))
		{
			if (description.charAt(c) == ':')
				c++;  // named parameter
			System.out.println("True scope includes: " + description.substring(c, cc));
			return new TokenRange(c, cc);
		}
		
		if (token.equalsIgnoreCase("false"))
		{
			if (description.charAt(c) == ':')
				c++;  // named parameter
			System.out.println("False scope includes: " + description.substring(c, cc));
			return new TokenRange(c, cc);
		}
		
		if (StringRoutines.isInteger(token))
		{
			System.out.println("Int scope includes: " + description.substring(c, cc));
			return new TokenRange(c, cc);
		}
		
		if (StringRoutines.isFloat(token) || StringRoutines.isDouble(token))
		{
			System.out.println("Float scope includes: " + description.substring(c, cc));
			return new TokenRange(c, cc);
		}	
		
		if (type == SelectionType.SELECTION || type == SelectionType.TYPING)
		{
			// Return scope of token, not entire ludeme
			if (ch == '(')
				c++;
			System.out.println("Selected token scope includes: '" + description.substring(c, cc) +"'");
			return new TokenRange(c, cc);			
		}
		
		// Filter out unwanted matches
		if (ch == '(')
		{
			// Is a class constructor: match to end of scope
//			System.out.println("Is a constructor '" + partialKeyword + "'...");
			final int closing = StringRoutines.matchingBracketAt(description, c);
			
			if (closing < 0)
			{
				System.out.println("** Couldn't close token: " + token);
				return null;
			}
			
			System.out.println("Class scope includes: " + description.substring(c, closing+1));
			return new TokenRange(c, closing+1);		
		}
				
		if (ch == '<')
		{
			// Is a rule
			System.out.println("Rule scope includes: " + description.substring(c, cc));
			return new TokenRange(c, cc);
		}

		if (ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t' || ch == ':' || ch == '{')
		{
			// Is a terminal (probably an enum)
			System.out.println("Enum constant scope includes: '" + description.substring(c+1, cc) +"'");
			return new TokenRange(c+1, cc);
		}

		//return null;
		return new TokenRange(c, cc);
	}
	
	//-------------------------------------------------------------------------

//	/**
//	 * @return Whether string describes a top-level ludeme (Game, Match or Metadata).
//	 */
//	public static boolean isTopLevelLudeme(final String str)
//	{
//		return str.contains("(game") || str.contains("(match") || str.contains("(metadata");
//	}
	
	/**
	 * @param str Game description.
	 * @return Whether string describes a Game or Match.
	 */
	public static boolean isGameOrMatch(final String str)
	{
		return str.contains("(game") || str.contains("(match");
	}
	
	//-------------------------------------------------------------------------
			
}
