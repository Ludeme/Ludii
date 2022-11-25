package reconstruction.completer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import completer.Completion;
import contextualiser.ContextualSimilarity;
import gameDistance.utils.DistanceUtils;
import gnu.trove.list.array.TIntArrayList;
import main.StringRoutines;
import main.UnixPrintWriter;
import main.collections.FVector;
import main.grammar.Description;
import main.grammar.Report;
import main.options.UserSelections;
import parser.Expander;

/**
 * Completes partial game descriptions ready for expansion.
 * @author cambolbro and Eric.Piette
 */
public class CompleterWithPrepro
{
	public static final char CHOICE_DIVIDER_CHAR = '|';
	
	private static final int MAX_PARENTS = 10;
	
	private static final int MAX_RANGE = 1000;
	
	/** The path of the csv with the id of the rulesets for each game and its description on one line. */
	private static final String RULESETS_PATH = "/recons/input/RulesetFormatted.csv";
	
	/** The ruleset id and their corresponding description formatted on one line */
	private final Map<Integer, String> ludMap;
	
	/** The ruleset ids greater than the threshold. */
	private Map<Integer, String> ludMapUsed;
	
	/** The weight of the expected concepts. */
	private final double conceptualWeight;

	/** The weight of the historical similarity. */
	private final double historicalWeight;
	
	/** The list of completions already tried. */
	private final List<Completion> history = new ArrayList<Completion>();
	
	/** threshold used to look first the top similarities scores. */
	private double threshold = 0.99;
	
	//-------------------------------------------------------------------------
		
	/**
	 * Constructor getting the rulesets expanded description on one line and rulesets ids.
	 */
	public CompleterWithPrepro
	(
		final double conceptualWeight,
		final double historicalWeight,
		final double threshold
	)
	{
		this.conceptualWeight = conceptualWeight;
		this.historicalWeight = historicalWeight;
		this.threshold = threshold;
		ludMap = new HashMap<Integer, String>();
		
		// Get the ids and descriptions of the rulesets.
		try (final InputStream in = CompleterWithPrepro.class.getResourceAsStream(RULESETS_PATH);
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));)
		{
			String line = reader.readLine();
			while (line != null)
			{
				String lineNoQuote = line;
				
				int separatorIndex = lineNoQuote.indexOf(',');
				final String gameName = lineNoQuote.substring(0, separatorIndex);
				lineNoQuote = lineNoQuote.substring(gameName.length() + 1);

				separatorIndex = lineNoQuote.indexOf(',');
				final String rulesetName = lineNoQuote.substring(0, separatorIndex);
				lineNoQuote = lineNoQuote.substring(rulesetName.length() + 1);

				separatorIndex = lineNoQuote.indexOf(',');
				final String rulesetIdStr = lineNoQuote.substring(0, separatorIndex);
				final int rulesetId = Integer.parseInt(rulesetIdStr);
				lineNoQuote = lineNoQuote.substring(rulesetIdStr.length() + 1);
				
				final String desc = lineNoQuote;

				// To check if a specific part of the ludemes appear in some games.
//				if(desc.contains("\"Marker1\""))
//				{
//					System.out.println(gameName + " HAS IT");
//					System.out.println(desc);
//				}
				
//				System.out.println("game = " + gameName);
//				System.out.println("ruleset = " + rulesetName);
//				System.out.println("rulesetId = " + rulesetId);
//				System.out.println("desc = " + desc);
				
				ludMap.put(Integer.valueOf(rulesetId), desc);
				
				line = reader.readLine();
			}
			reader.close();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Create a completion.
	 * @param raw            Incomplete raw game description.
	 * @param rulesetReconId Id of the ruleset to recons.
	 * @return A (raw) game description.        
	 */
	public Completion completeSampled
	(
		final String raw, 
		final int rulesetReconId
	)
	{
//		System.out.println("\nCompleter.complete(): Completing at most " + maxCompletions + " descriptions...");

		// Expand the defines of rulesets needed reconstruction.
		final Description description = new Description(raw);
		expandRecons(description, "");
		
		// Format the description.
		final String rulesetDescriptionOneLine = StringRoutines.formatOneLineDesc(description.expanded()); // Same format of all other rulesets.
				
		//System.out.println(rulesetDescriptionOneLine);
		
		Completion comp = new Completion(rulesetDescriptionOneLine);
		System.out.println("new threshold = " + threshold);
		applyThresholdToLudMap(rulesetReconId);
		while (needsCompleting(comp.raw()))
		{
			//System.out.println("before \n" + comp.raw());
			comp = nextCompletionSampled(comp, rulesetReconId);	
			//System.out.println("after \n" + comp.raw());
			
			if(comp == null)
			{
				if(threshold <= 0.0)
				{
					System.out.println("All combinations tried, no result.");
					return null;
				}
				threshold = threshold - 0.01;
				System.out.println("new threshold = " + threshold);
				comp = new Completion(rulesetDescriptionOneLine);
				applyThresholdToLudMap(rulesetReconId);
			}
		}
		
//		System.out.println("\nList of completions:");
//		for (final Completion comp : completions)
//			System.out.println(comp.raw());
//		System.out.println();
		
		history.add(comp);
		return comp;
	}
	
	//-------------------------------------------------------------------------

		/**
		 * Process next completion and add results to queue.
		 * Solves the next completion independently by sampling from candidates.
		 * @param rulesetReconId Id of the ruleset to recons.
		 */
		public Completion nextCompletionSampled
		(
			final Completion completion,
			final int        rulesetReconId
		)
		{
//			System.out.println("\nCompleting next completion for raw string:\n" + completion.raw());
			
			final List<Completion> completions = new ArrayList<Completion>();
			
			// Find opening and closing bracket locations
			final String raw = completion.raw();
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
			
//			System.out.println("Parents:\n");
//			for (final String[] parent : parents)
//				System.out.println(parent[0] + "?" + parent[1] + "\n");
			
			final List<String> choices      = extractChoices(clause);
			final List<String> inclusions   = new ArrayList<String>();
			final List<String> exclusions   = new ArrayList<String>();
			//final List<String> enumerations = new ArrayList<String>();
			int enumeration = 0;

			//System.out.println(choices);
			
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
				// To print the parents
//				System.out.println(parent[0]);
//				System.out.println(parent[1]);
				
//				System.out.println("\nEnumerating on parent " + enumeration + ": \"" + parent[0] + "\" + ? + \"" + parent[1] + "\"");
				enumerateMatches(completion, left, right, parent, completions, completion.score(), rulesetReconId);
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
					final Completion newCompletion = new Completion(str);
					newCompletion.setScore(completion.score());
					
//					System.out.println("\n**********************************************************");
//					System.out.println("completion " + n + "/" + choices.size() + " is:\n" + completion);
					
					//queue.add(newCompletion);
					
					newCompletion.setIdsUsed(completion.idsUsed());
					newCompletion.setScore(completion.score());
					newCompletion.setSimilarityScore(completion.similarityScore());
					newCompletion.setCommonTrueConceptsScore(completion.commonExpectedConceptsScore());
					
					completions.add(newCompletion);
				}
			}		
						
			if (completions.isEmpty())
				return null;

			// Get a random completion according to the score of each completion.
			final FVector vectorCompletions = new FVector(completions.size());
			for(int i = 0; i < completions.size(); i++)
				vectorCompletions.add((float) completions.get(i).score());
			final Completion returnCompletion = completions.get(vectorCompletions.sampleProportionally());
			
			return returnCompletion;
		}
	
		//-------------------------------------------------------------------------

		/**
		 * Enumerate all parent matches in the specified map.
		 * @param parent
		 * @param queue
		 * @param rulesetReconId Id of the ruleset to recons.
		 */
		private void enumerateMatches
		(
			final Completion       completion,
			final String           left, 
			final String           right, 
			final String[]         parent, 
			final List<Completion> queue,  
			final double           confidence,
			final int              rulesetReconId
		)
		{
				for (final Map.Entry<Integer, String> entry : ludMapUsed.entrySet()) 
				{
					final String otherDescription = entry.getValue();
					final int rulesetId = entry.getKey().intValue();
					final String candidate = new String(otherDescription);
					
					double culturalSimilarity = 0.0;
					double conceptualSimilarity = 0.0;
					if(rulesetReconId == -1) // We do not use the CSN.
						culturalSimilarity = 1.0;
					else
					{
						final String similaryFilePath = ContextualSimilarity.rulesetContextualiserFilePath;
						final File fileSimilarity1 = new File(similaryFilePath + rulesetReconId + ".csv");
						final File fileSimilarity2 = new File(similaryFilePath + entry.getKey().intValue() + ".csv");
						
						if(!fileSimilarity1.exists() || !fileSimilarity2.exists() || (rulesetReconId == rulesetId)) // If CSN not computing or comparing the same rulesets, similarity is 0.
							culturalSimilarity = 0.0;
						else
							culturalSimilarity = DistanceUtils.getRulesetCSNDistance(rulesetId, rulesetReconId);
						
						conceptualSimilarity = getAVGCommonExpectedConcept(rulesetReconId, rulesetId);
					}
					
					// We ignore all the ludemes coming from a negative similarity value or 0.
					if(culturalSimilarity <= 0)
						continue;
					
					final double score = historicalWeight * culturalSimilarity + conceptualWeight * conceptualSimilarity;
					
					final int l = candidate.indexOf(parent[0]);
					
					if (l < 0)
						continue;  // not a match
					
					final String secondPart = candidate.substring(l + parent[0].length());  //.trim();
					
	//				System.out.println("\notherDescription is: " + otherDescription);
	//				System.out.println("parent[0] is: " + parent[0]);
	//				System.out.println("parent[1] is: " + parent[1]);
	//				System.out.println("secondPart is: " + secondPart);
	//				System.out.println("left  is: " + left);
	//				System.out.println("right is: " + right);
					
	//				final int r = secondPart.indexOf(parent[1]);
					
					// We get the right parent index.
					int countParenthesis = 0;
					int r = 0;
					for(; r < secondPart.length(); r++)
					{
						if(secondPart.charAt(r) == '(')
							countParenthesis++;
						else
							if(secondPart.charAt(r) == ')')
								countParenthesis--;
						if(countParenthesis == -1)
						{
							r--;
							break;
						}
					}
					
	//				final int r = (StringRoutines.isBracket(secondPart.charAt(0)))
	//								? StringRoutines.matchingBracketAt(secondPart, 0)
	//								: secondPart.indexOf(parent[1]) - 1;
	
					if (r >= 0)
					{
						// Is a match
						//final String match = mapString.substring(l, l + parent[0].length() + r + parent[1].length());
						final String match = secondPart.substring(0, r + 1);  //l, l + parent[0].length() + r + parent[1].length());
						//System.out.println("match is: " + match);
						
	//					final String str = 
	//							left.substring(0, left.length() - parent[0].length())
	//							+
	//							match
	//							+
	//							right.substring(parent[1].length());
	
						//final String str = left + " " + match + " " + right;
						final String str = left + match + right;
						//System.out.println(right);
						final Completion newCompletion = new Completion(str);
						//System.out.println("completion is:\n" + completion.raw());
							
						//System.out.println("Adding completion:\n" + completion.raw());
						final double newScore = (completion.idsUsed().size() == 0) ? score : ((completion.score() * completion.idsUsed().size() + score) / (1 + completion.idsUsed().size()));
						final double newSimilarityScore = (completion.idsUsed().size() == 0) ? culturalSimilarity : ((completion.score() * completion.idsUsed().size() + culturalSimilarity) / (1 + completion.idsUsed().size()));
						final double newCommonTrueConceptsAvgScore = (completion.idsUsed().size() == 0) ? conceptualSimilarity : ((completion.score() * completion.idsUsed().size() + conceptualSimilarity) / (1 + completion.idsUsed().size()));
						
						newCompletion.setIdsUsed(completion.idsUsed());
						newCompletion.addId(rulesetId);
						newCompletion.setScore(newScore);
						newCompletion.setSimilarityScore(newSimilarityScore);
						newCompletion.setCommonTrueConceptsScore(newCommonTrueConceptsAvgScore);
						//System.out.println("SCORE IS " + completion.score());
						
						if (!queue.contains(newCompletion) && !historyContainIds(newCompletion))
							queue.add(newCompletion);
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
			final boolean curly = left.charAt(l + 1) == '{';
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
			parent[0] = left.substring(l);  //.trim();
			parent[1] = right.substring(0, r);  //.trim();
			
			// Strip leading spaces from parent[0]
			while (parent[0].length() > 0 && parent[0].charAt(0) == ' ')
				parent[0] = parent[0].substring(1);
			
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
				if(ch != CHOICE_DIVIDER_CHAR)
					sb.append(ch);
				// Store this choice and reset sb
				final String choice = sb.toString().trim();
				//System.out.println("extractChoices choice is: " + choice);
				 
				if (choice.contains("..") && !choice.contains("("))
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
				
				if (strC.length() < 2 || !Character.isLetter(strC.charAt(0)))
				{
					if (report != null)
						report.addError("Bad 'from' coordinate in site range: " + str.substring(c, d));
					return null;
				}			
				final int fromChar = Character.toUpperCase(strC.charAt(0)) - 'A';
				
				if (strD.length() < 2 || !Character.isLetter(strD.charAt(0)))
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

	//-------------------------------------------------------------------------
	
	/**
	 * Save reconstruction to file.
	 * @param path Path to save output file (will use default /Common/res/out/recons/ if null).
	 * @param name Output file name for reconstruction.
	 */
	public static void saveCompletion
	(
		final String path, 
		final String name, 
		final String completionRaw
	) 
	{
		final String savePath = (path != null) ? path : "../Common/res/out/recons/";
		final String outFileName = savePath + name + ".lud";
		
		// Create the file if it is not existing.
		final File folder = new File(path);
		if(!folder.exists())
			folder.mkdirs();
		
		try (final PrintWriter writer = new UnixPrintWriter(new File(outFileName), "UTF-8"))
		{
			writer.print(completionRaw);
		}
		catch (FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}
	
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
	 * Expands defines in user string to give full description of a description needed reconstruction.
	 * 
	 * @param description The description.
	 */
	public static void expandRecons(final Description description, final String selectedOptions)
	{
		final Report report = new Report();
		String str = new String(description.raw());

		// Remove comments before any expansions
		str = Expander.removeComments(str); // remove comment.

		final int c = str.indexOf("(metadata");
		if (c >= 0)
		{
			// Remove metadata
			str = str.substring(0, c).trim();
		}
		final List<String> selectedOptionStrings = new ArrayList<String>();
		if(!selectedOptions.isEmpty())
			selectedOptionStrings.add(selectedOptions);
		str = Expander.realiseOptions(str, description, new UserSelections(selectedOptionStrings), report);
		if (report.isError())
			return;
		
		if(str.contains("(rulesets"))
		{
			str = Expander.realiseRulesets(str, description, report);
			str = str.substring(0, str.length()-1);
			if (report.isError())
				return;
		}
		
		// Continue expanding defines for full description
		str = Expander.expandDefines(str, report, description.defineInstances());

		// Do again after expanding defines, as external defines could have comments
		str = Expander.removeComments(str); // remove comment.

		// Do after expanding defines, as external defines could have ranges
		str = Expander.expandRanges(str, report);
		str = Expander.expandSiteRanges(str, report);
		
		str = Expander.cleanUp(str, report);
		
		description.setExpanded(str); 
	}
	
	/**
	 * @return Map of rulesetId (key) to CSN distance (value) pairs, based on distance to specified rulesetId.
	 */
	public static double getAVGCommonExpectedConcept(final int reconsRulesetId, final int rulesetID)
	{
		// Load ruleset avg common true concepts from specific directory.
		final String commonExpectedConceptsFilePath = "./res/recons/input/commonExpectedConcepts/CommonExpectedConcept_" + reconsRulesetId + ".csv";
		final File fileTrueConcept = new File(commonExpectedConceptsFilePath);
		
		if(!fileTrueConcept.exists() || (reconsRulesetId == rulesetID)) // If TrueConcept not computing or comparing the same rulesets, trueConceptsAvg is 0.
			return 0.0;
		
		// Map of rulesetId (key) to common true concepts avg pairs.
		final Map<Integer, Double> rulesetCommonTrueConcept = new HashMap<>();	
		
		try (BufferedReader br = new BufferedReader(new FileReader(commonExpectedConceptsFilePath))) 
		{
		    String line = br.readLine();	// column names
		    
		    while ((line = br.readLine()) != null) 
		    {
		        final String[] values = line.split(",");
		        rulesetCommonTrueConcept.put(Integer.valueOf(Integer.parseInt(values[0])), Double.valueOf(Double.parseDouble(values[1])));
		    }
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		
		final Double mapValue = rulesetCommonTrueConcept.get(Integer.valueOf(rulesetID));
		final double avgCommonTrueConcepts = (mapValue == null) ? 0.0 : mapValue.doubleValue();
		return avgCommonTrueConcepts;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @param newCompletion The new completion computed
	 * @return True if this new completion is in the history list.
	 */
	public boolean historyContainIds(final Completion newCompletion)
	{
		final TIntArrayList idsUsedNewRecons = newCompletion.idsUsed();
		
		for(final Completion completion: history)
		{
			final TIntArrayList idsUsed = completion.idsUsed(); 
			if(idsUsed.size() == idsUsedNewRecons.size())
			{
				boolean equalIds = true;
				for(int i = 0; i < idsUsed.size(); i++)
				{
					if(idsUsed.get(i) != idsUsedNewRecons.get(i))
					{
						equalIds = false;
						break;
					}
				}
				if(equalIds)
					return true;
			}
		}
		
		return false;
	}
	
	/** To update the list of luds to use for recons after each update of the threshold.*/
	public void applyThresholdToLudMap(final int rulesetReconId)
	{
		ludMapUsed = new HashMap<Integer, String>();
		for (final Map.Entry<Integer, String> entry : ludMap.entrySet()) 
		{
			final int rulesetId = entry.getKey().intValue();
			double culturalSimilarity = 0.0;
			double conceptualSimilarity = 0.0;
			if(rulesetReconId == -1) // We do not use the CSN.
				culturalSimilarity = 1.0;
			else
			{
				final String similaryFilePath = ContextualSimilarity.rulesetContextualiserFilePath;
				final File fileSimilarity1 = new File(similaryFilePath + rulesetReconId + ".csv");
				final File fileSimilarity2 = new File(similaryFilePath + entry.getKey().intValue() + ".csv");
				
				if(!fileSimilarity1.exists() || !fileSimilarity2.exists() || (rulesetReconId == rulesetId)) // If CSN not computing or comparing the same rulesets, similarity is 0.
					culturalSimilarity = 0.0;
				else
					culturalSimilarity = DistanceUtils.getRulesetCSNDistance(rulesetId, rulesetReconId);
				
				conceptualSimilarity = getAVGCommonExpectedConcept(rulesetReconId, rulesetId);
			}

			// We ignore all the ludemes coming from a negative similarity value or 0.
			if(culturalSimilarity <= 0)
				continue;
			
			final double score = historicalWeight * culturalSimilarity + conceptualWeight * conceptualSimilarity;
			
			if(score >= threshold)
				ludMapUsed.put(entry.getKey(), entry.getValue());
		}
		System.out.println("num Rulesets used to recons = " + ludMapUsed.size());
	}
	
}
