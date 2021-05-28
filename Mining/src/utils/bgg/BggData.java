package utils.bgg;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

//-----------------------------------------------------------------------------

/**
 * Parse BGG user ratings data in CSV format (tab separated).
 * 
 * @author cambolbro
 */
public class BggData
{
	private final List<BggGame> games = new ArrayList<BggGame>();
	private final Map<String, List<BggGame>> gamesByName  = new HashMap<String, List<BggGame>>();
	private final Map<Integer, BggGame> gamesByBggId = new HashMap<Integer, BggGame>();
	private final Map<String, User> usersByName = new HashMap<String, User>();
	
	//-------------------------------------------------------------------------

	public List<BggGame> games()
	{
		return games;
	}
	
	public Map<String, List<BggGame>> gamesByName()
	{
		return gamesByName;
	}

	public Map<Integer, BggGame> gamesByBggId()
	{
		return gamesByBggId;
	}
	
	public Map<String, User> usersByName()
	{
		return usersByName;
	}

	//-------------------------------------------------------------------------
	
	void loadGames(final String filePath)
	{
		final long startAt = System.currentTimeMillis();
		
		games.clear();
		gamesByName.clear();
		gamesByBggId.clear();
		
		try 
		(
			BufferedReader reader = new BufferedReader
			(
				new InputStreamReader(new FileInputStream(filePath), "UTF-8")
			)
		)
		{
			String line;
			while (true)
			{
				line = reader.readLine();
				if (line == null)
					break;
			
				final String[] subs = line.split("\t");
				
				final int bggId = Integer.parseInt(subs[0].trim());
				final String name = subs[1].trim();
				final String date = subs[2].trim();
				
				final BggGame game = new BggGame(games.size(), bggId, name, date, subs);
				
				// Add game to reference list
				games.add(game);
				
				// Add game to map of names
				List<BggGame> nameList = gamesByName.get(name.toLowerCase());
				if (nameList == null)
				{
					nameList = new ArrayList<BggGame>();
					gamesByName.put(name.toLowerCase(), nameList);
				}
				nameList.add(game);
				
				// Add game to map of ids -- should be unique
				gamesByBggId.put(Integer.valueOf(bggId), game);
				
				if (name.equals("scrabble"))
					System.out.println(game.name() + " has BGG id " + game.bggId() + ".");
			}
		} 
		catch (final IOException e) 
		{
			e.printStackTrace();
		}
		
		final long stopAt = System.currentTimeMillis();
		final double secs = (stopAt - startAt) / 1000.0;
		
		System.out.println(games.size() + " games loaded in " + secs + "s.");
		System.out.println(gamesByName.size() + " entries by name and " + gamesByBggId.size() + " by BGG id.");
	}
	
	//-------------------------------------------------------------------------

	void loadUserData(final String filePath)
	{
		final long startAt = System.currentTimeMillis();

		usersByName.clear();

		int items = 0;
		int kept  = 0;

		try 
		(
			BufferedReader reader = new BufferedReader
			(
				new InputStreamReader(new FileInputStream(filePath), "UTF-8")
			)
		)
		{
			String line;
			int lineIndex = 0;
			while (true)
			{
				line = reader.readLine();
				if (line == null)
					break;
											
				final BggGame game = games.get(lineIndex);				
				final String[] subs = line.split("\t");
			
				items += subs.length;
				
				for (final String sub : subs)
					kept += processUserData(sub, game) ? 1 : 0;
				
				lineIndex++;
			}
		} 
		catch (final IOException e) 
		{
			e.printStackTrace();
		}
		
		final long stopAt = System.currentTimeMillis();
		final double secs = (stopAt - startAt) / 1000.0;

		System.out.println(kept + "/" + items + " items processed for " + usersByName.size() + " users in " + secs + "s.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Whether data item was kept.
	 */
	boolean processUserData(final String entry, final BggGame game)
	{
		boolean kept = false;
				
		int c = 0;
		while (c < entry.length() && entry.charAt(c) != '\'')
			c++;

		int cc = c + 1;
		while (cc < entry.length() && entry.charAt(cc) != '\'')
			cc++;
		
		if (c >= entry.length() || cc >= entry.length())
			return false;
	
		final String name = entry.substring(c+1, cc);
	
		// Ensure that user is in database
		User user = null;
		if (usersByName.containsKey(name))
		{
			user = usersByName.get(name);
		}
		else
		{
			user = new User(name);
			usersByName.put(name,  user);
		}
	
		// Create the actual rating object and cross-reference it
		final Rating rating = new Rating(game, user, entry);
		
		if (rating.score() != 0)
		{
			// A lot of ratings seem to be placeholder 0s
			kept = true;
			game.add(rating);
			user.add(rating);
		}

		return kept;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Sanity test based on my ratings.
	 */
	void testCamb()
	{
		final User camb = usersByName.get("camb");
		if (camb == null)
		{
			System.out.println("camb not found.");
		}
		else
		{
			System.out.println("camb ratings:");
			for (final Rating rating : usersByName.get("camb").ratings())
				System.out.println(" " + rating.game().name() + "=" + rating.score());
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * List games rated only by the specified user.
	 */
	public void findUniqueRatings(final String userName)
	{
		final User user = usersByName.get(userName);		
		if (user == null)
		{
			System.out.println("Couldn't find user '" + userName + "'.");
			return;
		}
		System.out.println(user.name() + " has " + user.ratings().size() + " ratings, and is the only person to have rated:");

		for (final Rating rating : user.ratings())
		{
			final BggGame game = rating.game();
			if (game.ratings().size() == 1)
				System.out.println(game.name() + " (" + game.date() + ")");
		}
	}
	
	//-------------------------------------------------------------------------

	public void runDialog()
	{
		while (true)
		{
			final Object[] options = 
				{
					"Similar Games",
					"Similar Games (rating)",
					"Similar Games (binary)",
                    "Similar Users",
                    "Suggestions for User",
					"Similar Games (by user)",
                };
			final int searchType = JOptionPane.showOptionDialog
			(
				null,
			    "Query BGG Data",
			    "Query Type",
			    JOptionPane.YES_NO_CANCEL_OPTION,
			    JOptionPane.QUESTION_MESSAGE,
			    null,
			    options,
			    Integer.valueOf(-1)
			);
			
			String message = "";
			
			if (searchType == 0)
			{
				final String selection = JOptionPane.showInputDialog("Game Name or Id (Optionally Comma, Year)");
				final String[] subs = selection.split(",");
				final String name = (subs.length < 1) ? "" : subs[0].trim().toLowerCase();
				final String year = (subs.length < 2) ? "" : subs[1].trim().toLowerCase();
				message = Recommender.recommendCBB(this, name, year);
			}
			else if (searchType == 1)
			{
				final String selection = JOptionPane.showInputDialog("Game Name or Id (Optionally Comma, Year)");
				final String[] subs = selection.split(",");
				final String name = (subs.length < 1) ? "" : subs[0].trim().toLowerCase();
				final String year = (subs.length < 2) ? "" : subs[1].trim().toLowerCase();
				message = Recommender.ratingSimilarityRecommendFor(this, name, year);
			}
			else if (searchType == 2)
			{
				final String selection = JOptionPane.showInputDialog("Game Name or Id (Optionally Comma, Year)");
				final String[] subs = selection.split(",");
				final String name = (subs.length < 1) ? "" : subs[0].trim().toLowerCase();
				final String year = (subs.length < 2) ? "" : subs[1].trim().toLowerCase();
				message = Recommender.binaryRecommendFor(this, name, year);
			}
			else if (searchType == 3)
			{
				final String selection = JOptionPane.showInputDialog("BGG User Name");
				message = Recommender.findMatchingUsers(this, selection);
			}
			else if (searchType == 4)
			{
				final String selection = JOptionPane.showInputDialog("BGG User Name");
				message = Recommender.recommendFor(this, selection, false);
			}
			else if (searchType == 5)
			{
				final String selection = JOptionPane.showInputDialog("Game Name or Id (Optionally Comma, Year)");
				final String[] subs = selection.split(",");
				final String name = (subs.length < 1) ? "" : subs[0].trim().toLowerCase();
				final String year = (subs.length < 2) ? "" : subs[1].trim().toLowerCase();
				message = Recommender.recommendGameByUser(this, name, year);
			}
				
			System.out.println(message);
			JOptionPane.showMessageDialog(null,message);  
		}
	}
	
	//-------------------------------------------------------------------------

	public void run()
	{
		loadGames("../Mining/res/bgg/input/BGG_dataset.csv");		
		loadUserData("../Mining/res/bgg/input/user_rating.csv");		
		
		final String dbGamesFilePath = "../Mining/res/bgg/input/Games.csv";
		final String outputFilePath = "../Mining/res/bgg/output/Results.csv";
		
		/** Uncomment this line to only include results for Ludii games. */
		//Database.saveValidGameIds(dbGamesFilePath);
		
		//runDialog();
		
		/** Used for generating useful database information. */
		Database.findDBGameMatches(this, false, dbGamesFilePath, outputFilePath);
	}
	
	//-------------------------------------------------------------------------

	public static void main(final String[] args)
	{
		final BggData bgg = new BggData();
		bgg.run();
	}
	
	//-------------------------------------------------------------------------
	
}
