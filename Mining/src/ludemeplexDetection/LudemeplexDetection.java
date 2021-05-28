package ludemeplexDetection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import game.Game;
import main.FileHandling;
import main.grammar.Call;
import main.options.Ruleset;
import other.GameLoader;
import utils.DBGameInfo;

/**
 * Detects all ludemeplexes within all games in Ludii.
 *
 * @author matthew.stephenson
 */
public class LudemeplexDetection 
{
	
	//-------------------------------------------------------------------------
	// Stored results
	
	// Map of all ludemeplexes and the games they are in.
	final static Map<Call, Set<String>> allLudemeplexes = new HashMap<>();
	
	// Map of all ludemeplexes and the number of times they occur across all games.
	final static Map<Call, Integer> allLudemeplexesCount = new HashMap<>();
	
	//-------------------------------------------------------------------------
	
	/**
	 * Records all ludemeplexes across all Games/Rulesets.
	 */
	private static String[] recordLudemeplexesAcrossAllGames() 
	{
		final String[] choices = FileHandling.listGames();
		//final String[] choices = IntStream.range(0, 500).mapToObj(i -> FileHandling.listGames()[i]).toArray(String[]::new);
		//final String[] choices = {"Chess.lud"};
		
		for (final String s : choices)
		{
			if (!FileHandling.shouldIgnoreLudAnalysis(s))
			{
				final String gameName = s.split("\\/")[s.split("\\/").length-1];
				final Game tempGame = GameLoader.loadGameFromName(gameName);
				final List<Ruleset> rulesets = tempGame.description().rulesets();
				if (rulesets != null && !rulesets.isEmpty())
				{
					// Record ludemeplexes for each ruleset
					for (int rs = 0; rs < rulesets.size(); rs++)
						if (!rulesets.get(rs).optionSettings().isEmpty())
							recordLudemeplexesInGame(GameLoader.loadGameFromName(gameName, rulesets.get(rs).optionSettings()));
				}
				else
				{
					// Record ludemeplexes just for the base game
					recordLudemeplexesInGame(tempGame);
				}
			}
		}
		
		return choices;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Records all ludemeplexes for a given Game.
	 */
	private static void recordLudemeplexesInGame(final Game game)
	{
		final Call callTree = game.description().callTree();
		final String gameName = DBGameInfo.getUniqueName(game);
		System.out.println(gameName);

		// Convert callTree for the game into a list tokens, with unique Ids for each token.
		storeludemeplexes(callTree, gameName);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Stores all ludemeplexes found within a Call object, for an associated game name.
	 */
	private static void storeludemeplexes(final Call c, final String gameName)
	{		
		// Don't store Arrays.
		final String ludemeplexString = c.toString();
		if (ludemeplexString.charAt(0) != '{')
		{
			Set<String> gameNameArray = new HashSet<>();
			if (allLudemeplexes.containsKey(c))
				gameNameArray = allLudemeplexes.get(c);
	
			gameNameArray.add(gameName);
			allLudemeplexes.put(c, gameNameArray);
			
			Integer count = Integer.valueOf(0);
			if (allLudemeplexesCount.containsKey(c))
				count = allLudemeplexesCount.get(c);
			count = Integer.valueOf((count.intValue() + 1));
			allLudemeplexesCount.put(c, count);
		}
		
		for (final Call arg : c.args())
			if (arg.args().size() > 0)
				storeludemeplexes(arg, gameName);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns the count for every ludemeplex in a call object.
	 */
	@SuppressWarnings("unused")  // DO NOT KILL: May be used in future.
	private static Map<Call, Integer> countLudemeplexes(final Call c, final Map<Call, Integer> currentCount)
	{
		if (currentCount.containsKey(c))
			currentCount.put(c, Integer.valueOf(currentCount.get(c).intValue()+1));
		else
			currentCount.put(c, Integer.valueOf(1));
		
		for (final Call arg : c.args())
			if (arg.args().size() > 0)
				countLudemeplexes(arg, currentCount);
		
		return currentCount;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Predicts the win-rate for a variety of games, AI agents and prediction algorithms.
	 */
	public static void main(final String[] args)
	{		
		// Record the ludemeplexes across all lud files.
		final String[] chosenGameNames = recordLudemeplexesAcrossAllGames();
		
		System.out.println("//-------------------------------------------------------------------------");
	
		// Store relevant details in output csv files for uploading to DB
		DatabaseFunctions.storeLudemeInfo();
		System.out.println("Ludemes Recorded");
//		DatabaseFunctions.storeLudemeplexInfo(allLudemeplexes, allLudemeplexesCount);
//		System.out.println("Ludemeplexes Recorded");
//		DatabaseFunctions.storeLudemesInLudemeplex(allLudemeplexes);
//		System.out.println("Ludemes in Ludemeplexes Recorded");
//		DatabaseFunctions.storeLudemeplexRulesetPairs(allLudemeplexes);
//		System.out.println("Ruleset Ludemeplexes Recorded");
//		
//		final Map<String, Set<String>> allDefineLudemeplexes = DatabaseFunctions.storeDefineLudemeplexInfo(allLudemeplexes, allLudemeplexesCount, 4);
//		System.out.println("Define Ludemeplexes Recorded");
//		DatabaseFunctions.storeDefineLudemeplexRulesetPairs(allDefineLudemeplexes);
//		System.out.println("Define Ruleset Ludemeplexes Recorded");
		
		DatabaseFunctions.storeLudemesInGames(GetLudemeInfo.getLudemeInfo(), chosenGameNames);
//		DatabaseFunctions.storeTokensInGames(chosenGameNames);
		
		System.out.println("//-------------------------------------------------------------------------");
	}

	//-------------------------------------------------------------------------
	
}
