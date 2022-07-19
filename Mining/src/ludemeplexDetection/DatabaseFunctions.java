package ludemeplexDetection;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import game.Game;
import main.grammar.Call;
import main.grammar.LudemeInfo;
import main.grammar.Report;
import other.GameLoader;
import utils.DBGameInfo;

/**
 * Provides functions for reading/saving DB information from/to external CSV files.
 *
 * @author matthew.stephenson
 */
public class DatabaseFunctions 
{

	//-------------------------------------------------------------------------
	
	// Output
	private static String ludemesOutputFilePath = "./res/ludemeplexDetection/output/ludemes.csv";
	private static String ludemeplexesOutputFilePath = "./res/ludemeplexDetection/output/ludemeplexes.csv";
	private static String defineLudemeplexesOutputFilePath = "./res/ludemeplexDetection/output/defineLudemeplexes.csv";
	private static String rulesetLudemeplexesOutputFilePath = "./res/ludemeplexDetection/output/rulesetLudemeplexes.csv";
	private static String defineRulesetludemeplexesOutputFilePath = "./res/ludemeplexDetection/output/rulesetDefineLudemeplexes.csv";
	private static String ludemeplexesLudemesOutputFilePath = "./res/ludemeplexDetection/output/ludemeplexLudemes.csv";
	private static String rulesetLudemesOutputFilePath = "./res/ludemeplexDetection/output/rulesetLudemes.csv";
	private static String notFoundLudemesFilePath = "./res/ludemeplexDetection/output/NOTFOUNDLUDEMES.csv";

	//-------------------------------------------------------------------------
	
	/**
	 * Saves all relevant information about the set of identified ludemeplexes, in an output csv.
	 * @param allludemeplexescount 
	 */
	public static void storeLudemeplexInfo(final Map<Call, Set<String>> allLudemeplexes, final Map<Call, Integer> allludemeplexescount) 
	{
		try (final BufferedWriter writer = new BufferedWriter(new FileWriter(ludemeplexesOutputFilePath, false)))
		{
			int ludemeplexId = 1;
			
			for (final Map.Entry<Call, Set<String>> entry : allLudemeplexes.entrySet())
			{
				String outputLine = ludemeplexId + ",";
				
				final List<String> ludemeplexStringList = entry.getKey().ludemeFormat(0);
				
				final String ludemeplexString = String.join("", ludemeplexStringList);

				// Try to compile ludemeplex
				try
				{
					final Object compiledObject = 
						compiler.Compiler.compileObject
						(
							ludemeplexString, entry.getKey().cls().getName(), new Report()
						);
					if (compiledObject == null)
						throw new Exception();
				}
				catch (final Exception E)
				{
					System.out.println("Game " + entry.getValue());
					System.err.println("ERROR Failed to compile " + ludemeplexString);
					System.err.println("ERROR symbolName = " + entry.getKey().cls().getName());
					System.err.println("ERROR className = " + entry.getKey().cls().getName());
				}
				
				String defineLudemeplexString = ludemeplexString.trim();
				defineLudemeplexString = "(define \"\"DLP.Ludemeplexes." + ludemeplexId + "\"\" " + defineLudemeplexString + ")";
				
				outputLine += "\"" + defineLudemeplexString + "\"";					// define version in .lud format
				//outputLine += "\"" + entry.getKey().toString() + "\"";			// call tree string
				outputLine += "," + allludemeplexescount.get(entry.getKey());		// total count
				outputLine += "," + allLudemeplexes.get(entry.getKey()).size();		// # rulesets
				outputLine += "\n";
				ludemeplexId++;

				writer.write(outputLine);
			}
			writer.close();
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Records all Define Ludemeplexes (ludemplexes with a # in them).
	 * NOTE This loops over everything twice, so could be optimised more.
	 * @param allLudemeplexescount 
	 */
	public static Map<String, Set<String>> storeDefineLudemeplexInfo(final Map<Call, Set<String>> allLudemeplexes, final Map<Call, Integer> allLudemeplexescount, final int maxNumDifferences) 
	{		
		// Map of all ludemeplexes (lud format) and the games they are in.
		final Map<String, Set<String>> allDefineLudemeplexes = new HashMap<>();
		// Map of all ludemeplexes (lud format) and the original Ludemeplexes that they relate to.
		final Map<String, Set<Call>> allDefineLudemeplexesOriginalLudemeplexes = new HashMap<>();
	
		// Record all Define ludemeplexes
		int counter1 = 1;
		for (final Map.Entry<Call, Set<String>> ludemeplexEntry : allLudemeplexes.entrySet())
		{
			System.out.println("" + counter1 + " / " + allLudemeplexes.entrySet().size());
			counter1++;
			
			final List<String> ludemeplexStringList = ludemeplexEntry.getKey().ludemeFormat(0);

			int counter2 = 1;
			for (final Map.Entry<Call, Set<String>> entry : allLudemeplexes.entrySet())
			{
				// Skip any pairs of ludemeplexes that have already been compared.
				counter2++;
				if (counter1 > counter2)
					continue;
				
				final List<String> storedLudemeplexStringList = entry.getKey().ludemeFormat(0);
				
				if (storedLudemeplexStringList.size() != ludemeplexStringList.size())
					continue;
				
				final List<String> newDefineLudemeplexStringList = new ArrayList<>();
				int numDifferences = 0;
				
				for (int i = 0; i < ludemeplexStringList.size(); i++)
				{
					if (ludemeplexStringList.get(i).replaceAll("[(){}]", "").trim().length() == 0
						&& storedLudemeplexStringList.get(i).replaceAll("[(){}]", "").trim().length() == 0
						&& !ludemeplexStringList.get(i).equals(storedLudemeplexStringList.get(i)))
					{
						numDifferences = maxNumDifferences + 1;
						break;
					}
					
					if (!ludemeplexStringList.get(i).equals(storedLudemeplexStringList.get(i)))
					{
						if (i == 1)
						{
							numDifferences = maxNumDifferences + 1;
							break;
						}
						
						numDifferences++;
						newDefineLudemeplexStringList.add("#" + numDifferences + " ");
					}
					else
					{
						newDefineLudemeplexStringList.add(ludemeplexStringList.get(i));
					}
					
					if (numDifferences > maxNumDifferences)
						break;
				}
				
				if (numDifferences <= maxNumDifferences && numDifferences > 0)
				{
					final String newDefineString = String.join("", newDefineLudemeplexStringList);
					
					// Store the games that use this define ludemeplex.
					final Set<String> newSetOfGames = new HashSet<>();
					if (allDefineLudemeplexes.containsKey(newDefineString))
						newSetOfGames.addAll(allDefineLudemeplexes.get(newDefineString));
					newSetOfGames.addAll(entry.getValue());
					newSetOfGames.addAll(ludemeplexEntry.getValue());
					allDefineLudemeplexes.put(newDefineString, newSetOfGames);
					
					// Store all ludemeplexes that are associated with each define ludemeplex.
					final Set<Call> ludemeplexesThisDefineUses = new HashSet<>();
					if (allDefineLudemeplexesOriginalLudemeplexes.containsKey(newDefineString))
						ludemeplexesThisDefineUses.addAll(allDefineLudemeplexesOriginalLudemeplexes.get(newDefineString));
					ludemeplexesThisDefineUses.add(entry.getKey());
					ludemeplexesThisDefineUses.add(ludemeplexEntry.getKey());
					allDefineLudemeplexesOriginalLudemeplexes.put(newDefineString, ludemeplexesThisDefineUses);
				}
			}
		}
		
		// Write them to output file
		try (final BufferedWriter writer = new BufferedWriter(new FileWriter(defineLudemeplexesOutputFilePath, false)))
		{
			int ludemeplexId = 1;
			
			for (final Map.Entry<String, Set<String>> entry : allDefineLudemeplexes.entrySet())
			{
				String outputLine = ludemeplexId + ",";
				
				String defineLudemeplexString = entry.getKey().trim();
				defineLudemeplexString = "(define \"\"DLP.Ludemeplexes." + ludemeplexId + "\"\" " + defineLudemeplexString + ")";
				
				int totalCount = 0;
				for (final Call c : allDefineLudemeplexesOriginalLudemeplexes.get(entry.getKey()))
					totalCount += allLudemeplexescount.get(c).intValue();
				
				outputLine += "\"" + defineLudemeplexString + "\"";
				outputLine += "," + totalCount;													// total count
				outputLine += "," + allDefineLudemeplexes.get(entry.getKey()).size();			// # rulesets
				outputLine += "\n";
				ludemeplexId++;

				writer.write(outputLine);
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		
		return allDefineLudemeplexes;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Saves all relevant information about the set of identified ludemes in an output csv.
	 */
	public static void storeLudemeInfo() 
	{
		try (final BufferedWriter writer = new BufferedWriter(new FileWriter(ludemesOutputFilePath, false)))
		{
			for (final LudemeInfo ludeme : GetLudemeInfo.getLudemeInfo())
			{
				writer.write(ludeme.id() + "," + ludeme.getDBString() + "\n");
			}
			writer.close();
		}
		catch (final IOException e1)
		{
			e1.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Saves all LudemeplexID-RulesetID pairs, in an output csv.
	 */
	public static void storeLudemeplexRulesetPairs(final Map<Call, Set<String>> allLudemeplexes) 
	{
		int IdCounter = 1;
		int ludemeplexId = 1;
		
		try (final BufferedWriter writer = new BufferedWriter(new FileWriter(rulesetLudemeplexesOutputFilePath, false)))
		{
			for (final Map.Entry<Call, Set<String>> entry : allLudemeplexes.entrySet())
			{
				// Store the ID of all rulesets that use this ludemeplex
				for (final String name : entry.getValue())
				{
					if (DBGameInfo.getRulesetIds().containsKey(name))
					{
						writer.write(IdCounter + "," + DBGameInfo.getRulesetIds().get(name) + "," + ludemeplexId + "\n");
						IdCounter++;
					}
					else
					{
						System.out.println("could not find game name_1: " + name);
					}
				}
				ludemeplexId++;
			}
			writer.close();
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Saves all Define LudemeplexID-RulesetID pairs, in an output csv.
	 */
	public static void storeDefineLudemeplexRulesetPairs(final Map<String, Set<String>> allDefineLudemeplexes) 
	{
		int IdCounter = 1;
		int defineLudemeplexId = 1;
		
		try (final BufferedWriter writer = new BufferedWriter(new FileWriter(defineRulesetludemeplexesOutputFilePath, false)))
		{
			for (final Map.Entry<String, Set<String>> entry : allDefineLudemeplexes.entrySet())
			{
				// Store the ID of all rulesets that use this ludemeplex
				for (final String name : entry.getValue())
				{
					if (DBGameInfo.getRulesetIds().containsKey(name))
					{
						writer.write(IdCounter + "," + DBGameInfo.getRulesetIds().get(name) + "," + defineLudemeplexId + "\n");
						IdCounter++;
					}
					else
					{
						System.out.println("could not find game name_2: " + name);
					}
				}
				defineLudemeplexId++;
			}
			writer.close();
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Saves all LudemeplexID-LudemeID pairs, in an output csv.
	 */
	public static void storeLudemesInLudemeplex(final Map<Call, Set<String>> allLudemeplexes) 
	{
		// All ludeme strings found within at least one call tree (testing purposes)
		final Set<LudemeInfo> allFoundLudemes = new HashSet<>();
		
		int IdCounter = 1;
		int ludemeplexId = 1;
		
		try (final BufferedWriter writer = new BufferedWriter(new FileWriter(ludemeplexesLudemesOutputFilePath, false)))
		{
			for (final Map.Entry<Call, Set<String>> entry : allLudemeplexes.entrySet())
			{
				// Get all ludemes in this ludemeplex
				final Map<LudemeInfo, Integer> ludemesInLudemeplex = entry.getKey().analysisFormat(0, GetLudemeInfo.getLudemeInfo());
				ludemesInLudemeplex.remove(null);
				
				for (final LudemeInfo ludeme : ludemesInLudemeplex.keySet())
				{
					if (GetLudemeInfo.getLudemeInfo().contains(ludeme))
					{
						allFoundLudemes.add(ludeme);
						writer.write(IdCounter + "," + ludemeplexId + "," + ludeme.id() + "\n");
						IdCounter++;
					}
					else
					{
						System.out.println("could not find ludeme: " + ludeme);
					}
				}
				ludemeplexId++;
			}
			writer.close();
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------
	
	public static void storeLudemesInGames(final List<LudemeInfo> allValidLudemes, final List<String[]> gameRulesetNames)
	{
		int IdCounter = 1;
		final Set<LudemeInfo> allLudemesfound = new HashSet<LudemeInfo>();
		
		try (final BufferedWriter writer = new BufferedWriter(new FileWriter(rulesetLudemesOutputFilePath, false)))
		{			
			for (final String[] gameRulesetName : gameRulesetNames)
			{
				final Game game = GameLoader.loadGameFromName(gameRulesetName[0], gameRulesetName[1]);
				final String name = DBGameInfo.getUniqueName(game);
				
				final Map<LudemeInfo, Integer> ludemesInGame = game.description().callTree().analysisFormat(0, allValidLudemes);
				for (final LudemeInfo ludeme : ludemesInGame.keySet())
				{
					allLudemesfound.add(ludeme);
					if (DBGameInfo.getRulesetIds().containsKey(name))
					{
						writer.write(IdCounter + "," + DBGameInfo.getRulesetIds().get(name) + "," + ludeme.id() + "," + ludemesInGame.get(ludeme) +"\n");
						IdCounter++;
					}
					else
					{
						System.out.println("could not find game name_3: " + name);
					}
				}
			}
			writer.close();
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		
		// Record all ludemes that weren't found in any game.
		String notFoundLudemesString = "";
		for (final LudemeInfo ludeme : allValidLudemes)
			if (!allLudemesfound.contains(ludeme))
				notFoundLudemesString += ludeme.getDBString() + "\n";
		try (final BufferedWriter writer = new BufferedWriter(new FileWriter(notFoundLudemesFilePath, false)))
		{
			writer.write(notFoundLudemesString);
			writer.close();
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------

}
