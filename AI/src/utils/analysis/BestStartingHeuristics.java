package utils.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Wrapper around collected data on the best starting heuristics
 * to start training Alpha-Beta agents with.
 * 
 * TODO could probably make sure we only ever load the data once...
 *
 * @author Dennis Soemers
 */
public class BestStartingHeuristics
{
	
	//-------------------------------------------------------------------------
	
	/** Map of entries (mapping from cleaned game names to entries of data) */
	private final Map<String, Entry> entries;
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Loads and returns the analysed data as stored so far.
	 */
	public static BestStartingHeuristics loadData()
	{
		final Map<String, Entry> entries = new HashMap<String, Entry>();
		final File file = new File("../AI/resources/Analysis/BestStartingHeuristics.csv");
		
		try (final BufferedReader reader = new BufferedReader(new FileReader(file)))
		{
			reader.readLine();	// headers line, which we don't use
			
			for (String line; (line = reader.readLine()) != null; /**/)
			{
				final String[] lineSplit = line.split(Pattern.quote(","));
				entries.put(lineSplit[2], new Entry
						(
							lineSplit[0],
							lineSplit[1],
							lineSplit[2],
							lineSplit[3],
							Float.parseFloat(lineSplit[4])
						));
			}
		} 
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		
		return new BestStartingHeuristics(entries);
	}
	
	/**
	 * Constructor
	 * @param entries
	 */
	private BestStartingHeuristics(final Map<String, Entry> entries)
	{
		this.entries = entries;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param cleanGameName
	 * @return Stored entry for given game name
	 */
	public Entry getEntry(final String cleanGameName)
	{
		return entries.get(cleanGameName);
	}
	
	/**
	 * @return Set of all game keys in our file
	 */
	public Set<String> keySet()
	{
		return entries.keySet();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * An entry with data for one game in our collected data.
	 *
	 * @author Dennis Soemers
	 */
	public static class Entry
	{
		
		/** Name of game for which we stored data */
		private final String gameName;
		
		/** Name of ruleset for which we stored data */
		private final String rulesetName;
		
		/** Name of game+ruleset for which we stored data */
		private final String gameRulesetName;
		
		/** String description of top starting heuristic */
		private final String topHeuristic;
		
		/** Win percentage of the Alpha-Beta agent with the top starting heuristic */
		private final float topScore;
		
		/**
		 * Constructor
		 * @param cleanGameName
		 * @param rulesetName
		 * @param gameRulesetName
		 * @param topHeuristic
		 * @param topScore
		 */
		protected Entry
		(
			final String gameName, 
			final String rulesetName,
			final String gameRulesetName,
			final String topHeuristic, 
			final float topScore
		)
		{
			this.gameName = gameName;
			this.rulesetName = rulesetName;
			this.gameRulesetName = gameRulesetName;
			this.topHeuristic = topHeuristic;
			this.topScore = topScore;
		}
		
		/**
		 * @return Name of game for which we stored data
		 */
		public String gameName()
		{
			return gameName;
		}
		
		/**
		 * @return Name of ruleset for which we stored data
		 */
		public String rulesetName()
		{
			return rulesetName;
		}
		
		/**
		 * @return Name of game+ruleset for which we stored data
		 */
		public String gameRulesetName()
		{
			return gameRulesetName;
		}
		
		/**
		 * @return String description of top starting heuristic
		 */
		public String topHeuristic()
		{
			return topHeuristic;
		}
		
		/**
		 * @return Win percentage of the Alpha-Beta agent with the top starting heuristic
		 */
		public float topScore()
		{
			return topScore;
		}
		
	}
	
	//-------------------------------------------------------------------------

}
