package completer;

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

import main.StringRoutines;
//import main.StringRoutines;
import main.grammar.Report;
import parser.Expander;

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

	public static boolean needsCompleting(final String desc)
	{
		// Remove comments first, so that recon syntax can be commented out 
		// to not trigger a reconstruction without totally removing it.
		final String str = Expander.removeComments(desc);
		return str.contains("[") && str.contains("]");
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param raw       Partial raw game description.
	 * @param firstOnly Stop after the first completion (for Travis tests).
	 * @param report    Report log for warnings and errors.
	 * @return List of completed (raw) game descriptions ready for expansion and parsing.        
	 */
	public static List<Completion> complete(final String raw, final boolean firstOnly, final Report report)
	{
//		System.out.println("Completing description...");

		final List<Completion> completions = new ArrayList<Completion>();

		// Create list of alternative Descriptions, as each will need to be expanded
		final Map<String, String> ludMap = getAllLudContents();
		final Map<String, String> defMap = getAllLudContents();
		
		final List<Completion> queue = new ArrayList<Completion>();
		queue.add(new Completion(new String(raw)));
				
		while (!queue.isEmpty())
		{
			final Completion comp = queue.remove(0);
			if (!needsCompleting(comp.raw()))
			{
				// Completed!
				completions.add(comp);
				
				if (firstOnly)
					return completions;

				continue;
			}
			
			// Complete the next completion clause
			nextCompletion(comp, queue, ludMap, defMap, report);		
		}
				
		return completions;
	}

	//-------------------------------------------------------------------------

	/**
	 * Process next completion and add results to queue.
	 * @param queue
	 * @param report
	 */
	public static void nextCompletion
	(
		final Completion comp, final List<Completion> queue, 
		final Map<String, String> ludMap, final Map<String, String> defMap, 
		final Report report
	)
	{
//		System.out.println("Completing next completion for raw string:\n" + raw);
		
		final String raw = comp.raw();
		
		// Find opening and closing bracket locations
		final int from = raw.indexOf("[");
		final int to = StringRoutines.matchingBracketAt(raw, from);

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
			enumerateMatches(left, right, parent, ludMap, queue, comp.score());
			enumerateMatches(left, right, parent, defMap, queue, comp.score());
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
				
				final String str = raw.substring(0, from) + choice + raw.substring(to + 1);
				final Completion completion = new Completion(str);
				
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
		final Map<String, String> map, final List<Completion> queue, 
		final double confidence
	)
	{
		for (Map.Entry<String, String> entry : map.entrySet()) 
		{
			final String mapString = entry.getValue();
			
			// **
			// ** TODO: Determine distance between map entry and this completion
			// **
			final double distance = 0.1;  // dummy value for testing
			
			final int l = mapString.indexOf(parent[0]);
			if (l < 0)
				continue;  // not a match
			
			final String secondPart = mapString.substring(l + parent[0].length());
			
			final int r = secondPart.indexOf(parent[1]);
			
			if (r >= 0)
			{
				// Is a match
				final String match = mapString.substring(l, l + parent[0].length() + r + parent[1].length());
				//System.out.println("match is: " + match);
				
				final String str = 
						left.substring(0, left.length() - parent[0].length())
						+
						match
						+
						right.substring(parent[1].length());
				final Completion completion = new Completion(str);
				//System.out.println("completion is:\n" + completion);
				
				if (!queue.contains(completion))
				{
					//System.out.println("Adding completion:\n" + completion);
					completion.setScore(confidence * (1 - distance));
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
	 * @throws IOException 
	 */
	public static void saveReconstruction
	(
		final String name, final Completion completion
	) throws IOException
	{
		final String outFileName = "../Common/res/out/recons/" + name + "-" + 
									String.format("%.3f", Double.valueOf(completion.score())) + ".lud";	
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
			writer.write(completion.raw());
		}
	}
	
	//-------------------------------------------------------------------------

}
