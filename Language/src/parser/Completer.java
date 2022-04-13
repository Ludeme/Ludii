package parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.grammar.Report;

//-----------------------------------------------------------------------------

/**
 * Completes partial game descriptions ready for expansion.
 * @author cambolbro
 */
public class Completer
{
	public static final char CHOICE_DIVIDER_CHAR = '|';
	
	private static final int MAX_PARENTS = 10;
	
	private static final int MAX_RANGE = 1000;
		
	//-------------------------------------------------------------------------

	public static boolean needsCompleting(final String str)  //Description description)
	{
		return str.contains("[") && str.contains("]");
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param raw    Partial raw game description.
	 * @param report Report log for warnings and errors.
	 * @return List of completed (raw) game descriptions ready for expansion and parsing.        
	 */
	public static List<String> complete(final String raw, final Report report)
	{
//		System.out.println("Completing description...");
		
		// Create list of alternative Descriptions, as each will need to be expanded
		final List<String> completions = new ArrayList<String>();
		
		final Map<String, String> ludMap = getAllLudContents();
		final Map<String, String> defMap = getAllLudContents();
		
		final List<String> queue = new ArrayList<String>();
		queue.add(raw);
		
		while (!queue.isEmpty())
		{
			final String str = queue.remove(0);
			if (!needsCompleting(str))
			{
				// Completed!
				completions.add(str);
				continue;
			}
			
			// Complete the next completion clause
			nextCompletion(str, queue, ludMap, defMap, report);
		}
				
		return completions;
	}

	//-------------------------------------------------------------------------

	/**
	 * Process next completion and add results to queue.
	 * @param raw
	 * @param queue
	 * @param report
	 */
	public static void nextCompletion
	(
		final String raw, final List<String> queue, 
		final Map<String, String> ludMap, final Map<String, String> defMap, 
		final Report report
	)
	{
//		System.out.println("Completing next completion for raw string:\n" + raw);
		
		// Find opening and closing bracket locations
		final int from = raw.indexOf("[");
		//final int to = StringRoutines.matchingBracketAt(raw, from, true);
		final int to = matchingBracketAt(raw, from, true);

		// Get reconstruction clause (substring within square brackets)
		final String left   = raw.substring(0, from);
		final String clause = raw.substring(from + 1, to);
		final String right  = raw.substring(to + 1);
		
		//System.out.println("left  is: " + left);
		//System.out.println("clause is: " + clause);
		//System.out.println("right is: " + right);

		// Determine left and right halves of parents
		final List<String[]> parents = determineParents(left, right);
		
//		System.out.println("Parents:\n");
//		for (final String[] parent : parents)
//			System.out.println(parent[0] + "?" + parent[1] + "\n");
		
		final List<String> choices      = extractChoices(clause);
		final List<String> inclusions   = new ArrayList<String>();
		final List<String> exclusions   = new ArrayList<String>();
		//final List<String> enumerations = new ArrayList<String>();
		int enumeration = 0;

		for (int c = choices.size() - 1; c >= 0; c--)
		{
			final String choice = choices.get(c);
			//System.out.println("choice is: " + choice);
			if 
			(
				choice.length() > 3 && choice.charAt(0) == '[' 
				&& 
				choice.charAt(1) == '+' && choice.charAt(choice.length() - 1) == ']'
			)
			{
				// Is an inclusion
				inclusions.add(choice.substring(2, choice.length() - 1).trim());
				choices.remove(c);
			}
			else if 
			(
				choice.length() > 3 && choice.charAt(0) == '[' 
				&& 
				choice.charAt(1) == '-' && choice.charAt(choice.length() - 1) == ']'
			)
			{
				// Is an exclusion
				exclusions.add(choice.substring(2, choice.length() - 1).trim());
				choices.remove(c);
			}
			else if 
			(
				choice.length() >= 1 && choice.charAt(0) == '#' 
				|| 
				choice.length() >= 3 && choice.charAt(0) == '[' 
				&& 
				choice.charAt(1) == '#' && choice.charAt(choice.length() - 1) == ']'
			)
			{
				// Is an enumeration
				//enumerations.add(choice.substring(1, choice.length() - 1).trim());
				enumeration = numHashes(choice);
				//System.out.println("Enumeration has " + enumeration + " hashes...");
				choices.remove(c);
			}
		}
			
		if (enumeration > 0)
		{
			// Enumerate on parents
			final String[] parent = parents.get(enumeration - 1);
			//System.out.println("Enumerating on parent " + enumeration + ": " + parent[0] + "?" + parent[1]);
			enumerateMatches(left, right, parent, ludMap, queue);
			enumerateMatches(left, right, parent, defMap, queue);
		}
		else
		{
			// Handle choices as usual
			for (int n = 0; n < choices.size(); n++)
			{
				final String choice = choices.get(n);
				
				if (!exclusions.isEmpty())
				{
					// Check whether this choice contains excluded text
					boolean found = false;
					for (final String exclusion : exclusions)
						if (choice.contains(exclusion))
						{
							found = true;
							break;
						}
					if (found)
						continue;  // excluded text is present
				}
				
				if (!inclusions.isEmpty())
				{
					// Check that this choice contains included text
					boolean found = false;
					for (final String inclusion : inclusions)
						if (choice.contains(inclusion))
						{
							found = true;
							break;
						}
					if (!found)
						continue;  // included text is not present
				}
				
				final String completion = raw.substring(0, from) + choice + raw.substring(to + 1);
				
//				System.out.println("\n**********************************************************");
//				System.out.println("completion " + n + "/" + choices.size() + " is:\n" + completion);
				
				queue.add(completion);
			}
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Enumerate all parent matches in the specified map.
	 * @param parent
	 * @param map
	 * @param queue
	 */
	private static void enumerateMatches
	(
		final String left, final String right, final String[] parent, 
		final Map<String, String> map, final List<String> queue
	)
	{
		for (Map.Entry<String, String> entry : map.entrySet()) 
		{
			final String str = entry.getValue();
			
			final int l = str.indexOf(parent[0]);
			if (l < 0)
				continue;  // not a match
			
			final String secondPart = str.substring(l + parent[0].length());
			
			final int r = secondPart.indexOf(parent[1]);
			
			if (r >= 0)
			{
				// Is a match
				final String match = str.substring(l, l + parent[0].length() + r + parent[1].length());
				//System.out.println("match is: " + match);
				
				final String completion = 
						left.substring(0, left.length() - parent[0].length() - 1)
						+
						match
						+
						right.substring(parent[1].length());
				//System.out.println("completion is:\n" + completion);
				
				if (!queue.contains(completion))
				{
					//System.out.println("Adding completion:\n" + completion);
					queue.add(completion);
				}
			}	
		}	
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Number of hash characters '#' in string.
	 */
	private static int numHashes(final String str)
	{
		int numHashes = 0;
		for (int c = 0; c < str.length(); c++)
			if (str.charAt(c) == '#')
				numHashes++;
		return numHashes;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return List of successively nested parents based on left and right substrings.
	 */
	private static final List<String[]> determineParents
	(
		final String left, final String right
	)
	{
		final List<String[]> parents = new ArrayList<String[]>();
		
		// Step backwards to previous bracket on left side
		int l = left.length() - 1;
		int r = 0;
		boolean lZero = false;
		boolean rEnd = false;
		
		for (int p = 0; p < MAX_PARENTS; p++)
		{	
			// Step backwards to previous "(" 
			while (l > 0 && left.charAt(l) != '(' && left.charAt(l) != '{')
			{
				// Step past embedded clauses
				if (left.charAt(l) == ')' || left.charAt(l) == '}')
				{
					int depth = 1;
					while (l >= 0 && depth > 0)
					{
						l--;
						if (l < 0)
							break;
						
						if (left.charAt(l) == ')' || left.charAt(l) == '}')
							depth++;
						else if (left.charAt(l) == '(' || left.charAt(l) == '{')
							depth--;
					}
				}
				else
				{
					l--;
				}
			}
			if (l > 0)
				l--;
			
			if (l == 0)
			{
				if (lZero)
					break;
				lZero = true;
			}
			
			if (l < 0)
				break;
			
			// Step forwards to next bracket on right side
			boolean curly = left.charAt(l + 1) == '{';
			while 
			(
				r < right.length() 
				&& 
				(
					!curly && right.charAt(r) != ')'
					||
					curly && right.charAt(r) != '}'
				)
			)
			{
				// Step past embedded clauses
				if (right.charAt(r) == '(' || right.charAt(r) == '{')
				{
					int depth = 1;
					while (r < right.length() && depth > 0)
					{
						r++;
						if (r >= right.length())
							break;
						
						if (right.charAt(r) == '(' || right.charAt(r) == '{')
							depth++;
						else if (right.charAt(r) == ')' || right.charAt(r) == '}')
							depth--;
					}
				}
				else
				{
					r++;
				}
			}
			if (r < right.length() - 1)
				r++;
			
			if (r == right.length() - 1)
			{
				if (rEnd)
					break;
				rEnd = true;
			}
			
			if (r >= right.length())
				break;
			
			// Store the two halves of the parent
			final String[] parent = new String[2];
			parent[0] = left.substring(l);
			parent[1] = right.substring(0, r);
			
			//System.out.println("Parent " + p + " is " + parent[0] + "?" + parent[1]);
			
			parents.add(parent);
		}
		
		return parents;
	}

	//-------------------------------------------------------------------------

	/**
	 * Extract completion choices from reconstruction clause.
	 * @param clause
	 * @return
	 */
	static List<String> extractChoices(final String clause)
	{
		final List<String> choices = new ArrayList<String>();
		
		if (clause.length() >= 1 && clause.charAt(0) == '#')
		{
			// Is an enumeration, just return as is
			choices.add(clause);
			return choices;
		}
		
		final StringBuilder sb = new StringBuilder();
		int depth = 0;
		
		for (int c = 0; c < clause.length(); c++)
		{
			final char ch = clause.charAt(c);
			if (depth == 0 && (c >= clause.length() - 1 || ch == CHOICE_DIVIDER_CHAR))
			{
				// Store this choice and reset sb
				final String choice = sb.toString().trim();
//				System.out.println("extractChoices choice is: " + choice);
				 
				if (choice.contains(".."))
				{
					// Handle range
					final List<String> rangeChoices = expandRanges(new String(choice), null);
					if (!rangeChoices.isEmpty() && !rangeChoices.get(0).contains(".."))
					{
						// Is a number range
						//System.out.println(rangeChoices.size() + " range choices:" + rangeChoices);
						choices.addAll(rangeChoices);
					}
					else
					{
						// Check for site ranges
						final List<String> siteChoices = expandSiteRanges(new String(choice), null);
						if (!siteChoices.isEmpty())
							choices.addAll(siteChoices);
					}
				}
				else
				{
					choices.add(choice);
				}
				
				// Reset to accumulate next choice
				sb.setLength(0);
			}
			else
			{
				if (ch == '[')
					depth++;
				else if (ch == ']')
					depth--;
				
				sb.append(ch);
			}
		}
		return choices;
	}
		
	//-------------------------------------------------------------------------

	/**
	 * @param str
	 * @param from
	 * @param doNesting
	 * @return Location of matching closing bracket, else -1 if none.
	 */
	public static int matchingBracketAt(final String str, final int from, final boolean doNesting)
	{
		final char[][] brackets = 
		{
			{ '(', ')' },	
			{ '{', '}' },	
			{ '[', ']' },	
			{ '<', '>' },	
		};
	
		final int Opening = 0;
		final int Closing = 1;

		// Check is actually opening bracket
		int c = from;
		final char ch = str.charAt(c);
		
		// Find which bracket 
		int bid = -1;
		for (int n = 0; n < brackets.length; n++)
			if (brackets[n][0] == ch)
				bid = n;

		if (bid == -1)
		{
			System.out.println("** Specified char '" + ch + "' is not an open bracket.");
			return -1;
		}
		
		// Check for matching closing bracket
		int bracketDepth = 0;
		boolean inString = false;
		while (c < str.length())
		{
			final char chB = str.charAt(c);
			
			if (chB == '"')
				inString = !inString;
			
			if (!inString)
			{
				final char chA = (c == 0) ? '?' : str.charAt(c - 1);
				if (chB == brackets[bid][Opening])
				{
					if (chA != '(' || chB != '<')  // check is not a (< ...) or (<= ...) ludeme
						bracketDepth++;
				}
				else if (chB == brackets[bid][Closing])
				{
					if (chA != '(' || chB != '>')  // check is not a (> ...) or (>= ...) ludeme
					{
						if (!doNesting)
							break;  // stop on first matching closing bracket, e.g. option "<<>"
						bracketDepth--;
					}
				}
			}
			
			if (bracketDepth == 0)
				break;  // found bracket that closes opening bracket	
			c++;
		}
			
		if (c >= str.length())
			return -1;  // no matching closing bracket found
			//throw new IllegalArgumentException("No matching closing bracket " + brackets[bid][Closing] + " found.");
			
		return c;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param strIn
	 * @return Game description with all number range occurrences expanded.
	 */
	private static List<String> expandRanges
	(
		final String strIn,
		final Report report
	)
	{
		final List<String> choices = new ArrayList<String>();
		
		if (!strIn.contains(".."))
			return choices;  // nothing to do
		
		String str = new String(strIn);

		int ref = 1;
		while (ref < str.length() - 2)
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
					if (report == null)
					{
						System.out.println("** Range exceeded maximum of " + MAX_RANGE + ".");
					}
					else
					{
						report.addError("Range exceeded maximum of " + MAX_RANGE + ".");
						return null;
					}
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
			ref++;
		}
		
		final String[] subs = str.split(" ");
		for (final String sub : subs)
			choices.add(sub);
		
		return choices;
	}
	
	/**
	 * @param strIn
	 * @return Game description with all site range occurrences expanded.
	 */
	private static List<String> expandSiteRanges
	(
		final String strIn,
		final Report report
	)
	{
		final List<String> choices = new ArrayList<String>();
	
		//System.out.println("** Warning: Completer: Site ranges not expanded yet.");
		//return choices;
		
		if (!strIn.contains(".."))
			return choices;  // nothing to do
		
		String str = new String(strIn);

		int ref = 1;
		while (ref < str.length() - 2)
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
				
				if (strC.length() < 2 || !isLetter(strC.charAt(0)))
				{
					if (report != null)
						report.addError("Bad 'from' coordinate in site range: " + str.substring(c, d));
					return null;
				}			
				final int fromChar = Character.toUpperCase(strC.charAt(0)) - 'A';
				
				if (strD.length() < 2 || !isLetter(strD.charAt(0)))
				{
					if (report != null)
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
			ref++;
		}
		
		//System.out.println("str is: " + str);
		
		final String[] subs = str.split(" ");
		for (final String sub : subs)
			choices.add(sub);

		return choices;
	}

	public static boolean isLetter(final char ch)
	{
		return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z';
	}

	//-------------------------------------------------------------------------
	// Ludeme loader code from Matthew
	
	/**
	 * @return Names and contents of all files within the lud/board path.
	 */
	public static Map<String, String> getAllLudContents()
	{
		return getAllDirectoryContents("../Common/res/lud/board/");
	}
	
	/**
	 * @return Names and contents of all files within the define path.
	 */
	public static Map<String, String> getAllDefContents()
	{
		return getAllDirectoryContents("../Common/res/def/");
	}
	
	/**
	 * @return Names and contents of all files within the specific directory path.
	 */
	public static Map<String, String> getAllDirectoryContents(final String dir)
	{
		final File startFolder = new File(dir);
		final List<File> gameDirs = new ArrayList<>();
		gameDirs.add(startFolder);
		final Map<String, String> fileContents = new HashMap<>();

		for (int i = 0; i < gameDirs.size(); ++i)
		{
			final File gameDir = gameDirs.get(i);

			for (final File fileEntry : gameDir.listFiles())
			{
				if (fileEntry.isDirectory())
				{
					gameDirs.add(fileEntry);
				}
				else
				{
					try(BufferedReader br = new BufferedReader(new FileReader(fileEntry))) 
					{
					    final StringBuilder sb = new StringBuilder();
					    String line = br.readLine();

					    while (line != null) 
					    {
					        sb.append(line);
					        sb.append(System.lineSeparator());
					        line = br.readLine();
					    }
					    
					    final String everything = sb.toString();
					    fileContents.put(fileEntry.getName(), everything);
					} 
					catch (final FileNotFoundException e) 
					{
						e.printStackTrace();
					} 
					catch (final IOException e) 
					{
						e.printStackTrace();
					}
				}
			}
		}
		
		return fileContents;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Save reconstruction to file.
	 * @param name    Reconstruction name for file.
	 * @param content Completed string to print.
	 * @throws IOException 
	 */
	public static void saveReconstruction
	(
		final String name, final String content
	) throws IOException
	{
		final String outFileName = "../Common/res/out/recons/" + name + ".lud";	
		
		// Prepare the output file
		final File file = new File(outFileName);
		if (!file.exists())
			file.createNewFile();

		try 
		(
			final PrintWriter writer = 
				new PrintWriter
				(
					new BufferedWriter(new FileWriter(outFileName, false))
				)
		)
		{
			writer.write(content);
		}
	}
	
	//-------------------------------------------------------------------------

}
