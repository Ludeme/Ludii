package utils.bgg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import main.AliasesData;

public class Database
{

	/** valid game Ids for the recommendation process. */
	private final static List<Integer> validGameIds = new ArrayList<Integer>();
	
	//-------------------------------------------------------------------------
	
	/**
	 * Populates the validGameIds list will all BGGIds which have been associated with Ludii games (validBGGId.txt)
	 */
	public static void saveValidGameIds(final String dbGamesFilePath)
	{
		try (BufferedReader reader = new BufferedReader(new FileReader(dbGamesFilePath)))
		{
			String line = reader.readLine();
			while (line != null) 
			{
				try
				{
					final int gameId = Integer.parseInt(line.split(",")[1].trim().toLowerCase().replace("\"", ""));
					validGameIds.add(Integer.valueOf(gameId));
				}
				catch (final Exception E)
				{
					// probably null
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (final IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Finds BGGId or recommended games for all game names in Games.csv
	 * @param data
	 * @param getRecommendations
	 */
	public static void findDBGameMatches(final BggData data, final boolean getRecommendations, final String dbGamesFilePath, final String outputFilePath)
	{
		String outputString = "";
		try (BufferedReader reader = new BufferedReader(new FileReader(dbGamesFilePath)))
		{
			String line = reader.readLine();
			while (line != null) 
			{
				// Read in the DB data from csv.
				final String gameName = line.split(",")[0].trim().replace("\"", "");
				final String bggIdString = line.trim().split(",")[1].replace("\"", "");
				BggGame game = null;
				
				// Try to find the matching entry for each game in BGG, based on either its ID or Name
				if (!bggIdString.equals("NULL"))
				{
					game = data.gamesByBggId().get(Integer.valueOf(bggIdString));
				}
				else
				{
					// load in the aliases for this game
					final List<String> aliases = new ArrayList<String>();
					aliases.add(gameName.toLowerCase());
					final AliasesData aliasesData = AliasesData.loadData();
					final List<String> loadedAliases = aliasesData.aliasesForGameName(gameName);
					if (loadedAliases != null)
						for (final String alias : loadedAliases)
							aliases.add(alias);
					
					for (final String name : aliases)
					{
						final BggGame tempGame = Recommender.findGame(data, name, "", true, true);
						if (tempGame != null)
						{
							game = tempGame;
							break;
						}
					}
				}
				
				if (game != null)
				{
					if (getRecommendations)
					{
						// Give a set of recommended games
						System.out.print(gameName + ": ");
						Recommender.ratingSimilarityRecommendFor(data, String.valueOf(game.bggId()), "");
					}
					else
					{
						// Give BGGID
						final String outputLine = gameName.replace(" ", "_") + ", " + game.bggId() + ", " + game.averageRating();
						System.out.println(outputLine);
						outputString += outputLine + "\n";
					}	
				}
				
				line = reader.readLine();
			}
			reader.close();
		} catch (final IOException e) 
		{
			e.printStackTrace();
		}
		
		// Save results in output file
		try (final BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, false)))
		{
			writer.write("Name,BGGId,AverageRating\n" + outputString);
			writer.close();
		}
		catch (final IOException e1)
		{
			e1.printStackTrace();
		}
	}

	//-------------------------------------------------------------------------
	
	public static List<Integer> validGameIds()
	{
		return validGameIds;
	}
	
	//-------------------------------------------------------------------------
	
}
