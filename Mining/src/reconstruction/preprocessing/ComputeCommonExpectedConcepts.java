package reconstruction.preprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import compiler.Compiler;
import game.Game;
import grammar.Grammar;
import main.FileHandling;
import main.StringRoutines;
import main.UnixPrintWriter;
import main.grammar.Description;
import main.grammar.Report;
import main.grammar.Symbol;
import main.options.Ruleset;
import other.GameLoader;
import other.concept.Concept;
import reconstruction.completer.CompleterWithPrepro;

/**
 * Compute the average of common expected concepts between each reconstruction and each complete rulesets.
 * @author Eric.Piette
 */
public class ComputeCommonExpectedConcepts
{
	/**
	 * Generate the CSVs with the common expected concepts between each reconstruction and the complete rulesets.
	 */
	public static void generateCSVs()
	{
		System.out.println("Compute average common expected concepts between reconstruction and rulesets.");
		
		// Compute % Common Expected Concepts in each complete ruleset.
		final String[] gameNames = FileHandling.listGames();
		final Map<Integer, BitSet> conceptsNonBoolean = new HashMap<Integer, BitSet>();
		
		// Get the concepts of all complete description for each ruleset.
		for (int index = 0; index < gameNames.length; index++)
		{
			final String nameGame = gameNames[index];
			if (nameGame.replaceAll(Pattern.quote("\\"), "/").contains("/lud/bad/"))
				continue;

			if (nameGame.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wip/"))
				continue;

			if (nameGame.replaceAll(Pattern.quote("\\"), "/").contains("/lud/WishlistDLP/"))
				continue;

			if (nameGame.replaceAll(Pattern.quote("\\"), "/").contains("/lud/test/"))
				continue;

			if (nameGame.replaceAll(Pattern.quote("\\"), "/").contains("subgame"))
				continue;

			if (nameGame.replaceAll(Pattern.quote("\\"), "/").contains("reconstruction"))
				continue;

			final Game game = GameLoader.loadGameFromName(nameGame);
			final List<Ruleset> rulesetsInGame = game.description().rulesets();
			
			// Code for games with many rulesets
			if (rulesetsInGame != null && !rulesetsInGame.isEmpty()) 
			{
				for (int rs = 0; rs < rulesetsInGame.size(); rs++)
				{
					final Ruleset ruleset = rulesetsInGame.get(rs);
					
					if (!ruleset.optionSettings().isEmpty()) // We check if the ruleset is implemented.
					{
						final Game rulesetGame = GameLoader.loadGameFromName(nameGame, ruleset.optionSettings());
						
						final List<String> ids = rulesetGame.metadata().info().getId();
						if(ids == null || ids.isEmpty())
							continue;
						
						final int id = Integer.parseInt(ids.get(0));

						final BitSet concepts = rulesetGame.booleanConcepts();
						conceptsNonBoolean.put(Integer.valueOf(id), concepts);
						System.out.println("id = " + id + " Done.");
					}
				}
			}
			else // Code for games with a single ruleset.
			{
				final List<String> ids = game.metadata().info().getId();
				if(ids == null || ids.isEmpty())
					continue;
				
				final int id = Integer.parseInt(ids.get(0));
				
				final BitSet concepts = game.booleanConcepts();
				conceptsNonBoolean.put(Integer.valueOf(id), concepts);
				System.out.println("id = " + id + " Done.");
			}
		}

		System.out.println("Start compute Common Expected concepts for recons description.");
		// Check each recons description.
		final String[] choices = FileHandling.listGames();
		for (final String fileName : choices)
		{
			if (!fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/reconstruction/"))
				continue;
			
			final String gameName = fileName.substring(fileName.lastIndexOf("/")+1,fileName.length()-4);
	
			String path = fileName.replaceAll(Pattern.quote("\\"), "/");
			path = path.substring(path.indexOf("/lud/"));
	
			String desc = "";
			String line;
			try
			(
				final InputStream in = GameLoader.class.getResourceAsStream(path);
				final BufferedReader rdr = new BufferedReader(new InputStreamReader(in));
			)
			{
				while ((line = rdr.readLine()) != null)
					desc += line + "\n";
			}
			catch (final IOException e1)
			{
				e1.printStackTrace();
			}
			
			final String metadata = desc.contains("(metadata") ? desc.substring(desc.indexOf("(metadata")) : "";
			String idStr = metadata.contains("(id") ? metadata.substring(metadata.indexOf("(id")+ 5) : "";
			idStr = idStr.substring(0, idStr.indexOf(')')-1);
			final int idRulesetToRecons = Integer.valueOf(idStr).intValue();
			
			// Get game description from resource
			System.out.println("Game: " + gameName + " id = " + idRulesetToRecons);
			final List<Concept> commonExpectedConcepts = computeCommonExpectedConcepts(desc);
			
			final String beginOutput = "CommonExpectedConcept_";
			final String endOutput = ".csv";
			
			try (final PrintWriter writer = new UnixPrintWriter(new File(beginOutput + idRulesetToRecons + endOutput), "UTF-8"))
			{
				for (final Map.Entry<Integer, BitSet> entry : conceptsNonBoolean.entrySet())
				{
					final int rulesetId = entry.getKey().intValue();
					final BitSet conceptsRuleset = entry.getValue();
					
					int countCommonConcepts = 0;
					for(Concept concept: commonExpectedConcepts)
						if(conceptsRuleset.get(concept.id()))
							countCommonConcepts++;
					
					final double avgCommonConcepts = commonExpectedConcepts.size() == 0 ? 0.0 : ((double) countCommonConcepts / (double) commonExpectedConcepts.size());
	

					final List<String> lineToWrite = new ArrayList<String>();
					lineToWrite.add(rulesetId+"");
					lineToWrite.add(avgCommonConcepts+"");
					writer.println(StringRoutines.join(",", lineToWrite));
//					System.out.println("id = " + rulesetId);
//					System.out.println("% Common Expected Concepts = " + avgCommonConcepts);
					
				}
			}
			catch (final FileNotFoundException | UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
		}

		System.out.println("CommonExpectedConcepts CSVs Generated");
	}
	
	//-----------------------------------------------------------------------------
	
		/**
		 * @param desc The reconstruction description of the game.
		 * @return The list of concepts which are sure to be true for a reconstruction description.
		 */
		public static List<Concept> computeCommonExpectedConcepts(final String desc)
		{
			final List<Concept> commonExpectedConcepts = new ArrayList<Concept>();
			
			// Keep only the game description.
			String descNoMetadata = desc.substring(0,desc.lastIndexOf("(metadata"));
			descNoMetadata = descNoMetadata.substring(0, descNoMetadata.lastIndexOf(')'));

			Description description = new Description(descNoMetadata);
			CompleterWithPrepro.expandRecons(description, "");
			descNoMetadata = description.expanded();
			
			// Get all the ludemeplexes between parenthesis.
			final List<String> ludemeplexes = new ArrayList<String>();
			for(int i = 0; i < descNoMetadata.length(); i++)
			{
				final char c = descNoMetadata.charAt(i);
				if(c == '(')
				{
					int countParenthesis = 1;
					int indexCorrespondingParenthesis = i + 1;
					for(; indexCorrespondingParenthesis < descNoMetadata.length(); indexCorrespondingParenthesis++)
					{
						if(descNoMetadata.charAt(indexCorrespondingParenthesis) == '(')
							countParenthesis++;
						else
							if(descNoMetadata.charAt(indexCorrespondingParenthesis) == ')')
								countParenthesis--;
						if(countParenthesis == 0)
						{
							indexCorrespondingParenthesis++;
							break;
						}
					}
					final String ludemeplex = descNoMetadata.substring(i, indexCorrespondingParenthesis);
					
					// We keep the ludemeplexes with no completion point.
					if(!ludemeplex.contains("#") && !ludemeplex.contains("[") && !ludemeplex.contains("]"))
						ludemeplexes.add(ludemeplex);
				}
			}
			
			// Get the common concepts.
			for(String ludemeplex : ludemeplexes)
			{
				for(Concept concept: getCommonExpectedConcepts(ludemeplex))
				{
					if(!commonExpectedConcepts.contains(concept))
						commonExpectedConcepts.add(concept);
				}
			}
			
			return commonExpectedConcepts;
		}

		//----------------------CODE TO GET THE CONCEPTS OF A STRING (TO MOVE TO ANOTHER CLASS LATER)------------------------------------
		
		/**
		 * @param str The description of the ludemeplex.
		 * @return The common expected concepts of the ludemeplex.
		 */
		static List<Concept> getCommonExpectedConcepts(final String str)
		{
			final List<Concept> commonConcepts = new ArrayList<Concept>();
			
			if (str == null || str.equals(""))
				return commonConcepts;

			try
			{
				final Object compiledObject = compileString(str);
				if (compiledObject != null)
					commonConcepts.addAll(evalConceptCompiledObject(compiledObject));
			}
			catch (final Exception ex)
			{
				ex.getStackTrace();
				// Nothing to do.
			}
			
			return commonConcepts;
		}
		
		/**
		 * Attempts to get the concepts from a ludemeplex.
		 */
		static List<Concept> evalConceptCompiledObject(final Object obj)
		{
			// Default Game description to make the compiler happy, but not used except for this.
			final Game tempGame = (Game)Compiler.compileTest(new Description("(game \"Test\" (players 2) (equipment { (board (square 3)) "
					+ "	          (piece \"Disc\" Each) }) (rules (play (move Add (to "
					+ "	          (sites Empty)))) (end (if (is Line 3) (result Mover Win)))))"), false);
			
			final List<Concept> commonConcepts = new ArrayList<Concept>();
			
			// Need to preprocess the ludemes before to call the eval method.
			Method preprocess = null;
			try
			{
				preprocess = obj.getClass().getDeclaredMethod("preprocess", tempGame.getClass());
				if (preprocess != null)
					preprocess.invoke(obj, tempGame);
			}
			catch (final Exception e)
			{
				// Nothing to do.
				//e.printStackTrace();
			}

			// get the concepts by reflection.
			Method conceptMethod = null;
			BitSet concepts = new BitSet();
			try
			{
				conceptMethod = obj.getClass().getDeclaredMethod("concepts", tempGame.getClass());
				if (conceptMethod != null)
					concepts = ((BitSet) conceptMethod.invoke(obj, tempGame));
			}
			catch (final Exception e)
			{
				// Nothing to do.
				//e.printStackTrace();
			}
			
			for (int i = 0; i < Concept.values().length; i++)
			{
				final Concept concept = Concept.values()[i];
				if (concepts.get(concept.id()))
					commonConcepts.add(concept);
			}

			return commonConcepts;
		}
		
		/**
		 * Attempts to compile a given string for every possible symbol class.
		 * @return Compiled object if possible, else null.
		 */
		static Object compileString(final String str)
		{
			Object obj = null;
			
			final String token = StringRoutines.getFirstToken(str);
			final List<Symbol> symbols = Grammar.grammar().symbolsWithPartialKeyword(token);

			// Try each possible symbol for this token
			for (final Symbol symbol : symbols)
			{
				final String className = symbol.cls().getName();
				final Report report = new Report();
				
				try
				{
					obj = Compiler.compileObject(str, className, report);
				}
				catch (final Exception ex)
				{
					//System.out.println("Couldn't compile.");
				}
					
				if (obj != null)
					break;
			}
			
			return obj;
		}
}
