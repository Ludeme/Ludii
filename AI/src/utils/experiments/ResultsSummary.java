package utils.experiments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.Game;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import main.math.statistics.Stats;

/**
 * A summary of results for multiple played games. Thread-safe.
 * 
 * @author Dennis Soemers
 */
public class ResultsSummary 
{
	
	//-------------------------------------------------------------------------
	
	/** List of names / descriptions / string-representations of agents */
	protected final List<String> agents;
	
	/** Points per agent (regardless of player number). Wins = 1, draws = 0.5, losses = 0. */
	protected Stats[] agentPoints;
	
	/** Points per agent per player number. Wins = 1, draws = 0.5, losses = 0. */
	protected Stats[][] agentPointsPerPlayer;
	
	/** For every game in which the agent played, the duration (in number of decisions) */
	protected Stats[] agentGameDurations;
	
	/** For every game in which the agent played per player number, the duration (in number of decisions) */
	protected Stats[][] agentGameDurationsPerPlayer;
	
	/** Map from matchup lists to arrays of sums of payoffs */
	protected Map<List<String>, double[]> matchupPayoffsMap;

	/** Map from matchup arrays to counts of how frequently we observed that matchup */
	protected TObjectIntMap<List<String>> matchupCountsMap;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Construct a new object to collect a summary of results for a
	 * larger number of games being played
	 * @param game
	 * @param agents
	 */
	public ResultsSummary(final Game game, final List<String> agents)
	{
		this.agents = agents;
		
		final int numPlayers = game.players().count();
		
		agentPoints = new Stats[agents.size()];
		agentPointsPerPlayer = new Stats[agents.size()][numPlayers + 1];
		
		agentGameDurations = new Stats[agents.size()];
		agentGameDurationsPerPlayer = new Stats[agents.size()][numPlayers + 1];
		
		for (int i = 0; i < agents.size(); ++i)
		{
			agentPoints()[i] = new Stats(agents.get(i) + " points");
			agentGameDurations[i] = new Stats(agents.get(i) + " game durations");
			
			for (int p = 1; p <= numPlayers; ++p)
			{
				agentPointsPerPlayer[i][p] = new Stats(agents.get(i) + " points as P" + p);
				agentGameDurationsPerPlayer[i][p] = new Stats(agents.get(i) + " game durations as P" + p);
			}
		}
		
		matchupPayoffsMap = new HashMap<List<String>, double[]>();
		matchupCountsMap = new TObjectIntHashMap<List<String>>();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Records the results from a played game
	 * @param agentPermutation 
	 * 	Array giving us the original agent index for every player index 1 <= p <= numPlayers
	 * @param utilities
	 * 	Array of utilities for the players
	 * @param gameDuration
	 * 	Number of moves made in the game
	 */
	public synchronized void recordResults
	(
		final int[] agentPermutation, 
		final double[] utilities, 
		final int gameDuration
	)
	{
		for (int p = 1; p < agentPermutation.length; ++p)
		{
			// Convert utility from [-1.0, 1.0] to [0.0, 1.0]
			final double points = (utilities[p] + 1.0) / 2.0;
			final int agentNumber = agentPermutation[p];
			
			agentPoints()[agentNumber].addSample(points);
			agentPointsPerPlayer[agentNumber][p].addSample(points);
			
			agentGameDurations[agentNumber].addSample(gameDuration);
			agentGameDurationsPerPlayer[agentNumber][p].addSample(gameDuration);
		}
		
		final List<String> agentsList = new ArrayList<String>(agentPermutation.length - 1);
		for (int p = 1; p < agentPermutation.length; ++p)
		{
			agentsList.add(agents.get(agentPermutation[p]));
		}
		
		if (!matchupPayoffsMap.containsKey(agentsList))
		{
			matchupPayoffsMap.put(agentsList, new double[utilities.length - 1]);
		}
		
		matchupCountsMap.adjustOrPutValue(agentsList, +1, 1);
		final double[] sumUtils = matchupPayoffsMap.get(agentsList);
		
		for (int p = 1; p < utilities.length; ++p)
		{
			sumUtils[p - 1] += utilities[p];
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param agentName
	 * @return Score averaged over all games, all agents with name equal to 
	 * given name.
	 */
	public synchronized double avgScoreForAgentName(final String agentName)
	{
		double sumScores = 0.0;
		int sumNumGames = 0;
		
		for (int i = 0; i < agents.size(); ++i)
		{
			if (agents.get(i).equals(agentName))
			{
				agentPoints()[i].measure();
				sumScores += agentPoints()[i].sum();
				sumNumGames += agentPoints()[i].n();
			}
		}
		
		return sumScores / sumNumGames;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Generates an intermediate summary of results.
	 * @return The generated summary
	 */
	public synchronized String generateIntermediateSummary()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("=====================================================\n");
		
		int totGamesPlayed = 0;
		for (int i = 0; i < agentPointsPerPlayer.length; ++i)
		{
			totGamesPlayed += agentPointsPerPlayer[i][1].n();
		}

		sb.append("Completed " + totGamesPlayed + " games.\n");
		sb.append("\n");
		
		for (int i = 0; i < agents.size(); ++i)
		{
			sb.append("Agent " + (i+1) + " (" + agents.get(i) + ")\n");
			
			agentPoints()[i].measure();
			sb.append("Winning score (between 0 and 1) " + agentPoints()[i] + "\n");
			
			for (int p = 1; p < agentPointsPerPlayer[i].length; ++p)
			{
				agentPointsPerPlayer[i][p].measure();
				sb.append("P" + p + agentPointsPerPlayer[i][p] + "\n");
			}
			
			agentGameDurations[i].measure();
			sb.append("Game Durations" + agentGameDurations[i] + "\n");
			
			for (int p = 1; p < agentGameDurationsPerPlayer[i].length; ++p)
			{
				agentGameDurationsPerPlayer[i][p].measure();
				sb.append("P" + p + agentGameDurationsPerPlayer[i][p] + "\n");
			}
			
			if (i < agents.size() - 1)
			{
				sb.append("\n");
			}
		}
		
		sb.append("=====================================================\n");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Writes results data for processing by the OpenSpiel implementation of alpha-rank,
	 * to a .csv file.
	 * 
	 * @param outFile
	 */
	public synchronized void writeAlphaRankData(final File outFile)
	{
		try (final PrintWriter writer = new PrintWriter(outFile, "UTF-8"))
		{
			writer.write("agents,scores\n");
			
			for (final List<String> matchup : matchupPayoffsMap.keySet())
			{
				final StringBuilder agentTuple = new StringBuilder();
				agentTuple.append("\"(");
				for (int i = 0; i < matchup.size(); ++i)
				{
					if (i > 0)
						agentTuple.append(", ");
					
					agentTuple.append("'");
					agentTuple.append(matchup.get(i));
					agentTuple.append("'");
				}
				agentTuple.append(")\"");
				
				final double[] scoreSums = matchupPayoffsMap.get(matchup);
				final int count = matchupCountsMap.get(matchup);
				
				final double[] avgScores = Arrays.copyOf(scoreSums, scoreSums.length);
				for (int i = 0; i < avgScores.length; ++i)
				{
					avgScores[i] /= count;
				}
				
				final StringBuilder scoreTuple = new StringBuilder();
				scoreTuple.append("\"(");
				for (int i = 0; i < avgScores.length; ++i)
				{
					if (i > 0)
						scoreTuple.append(", ");
					
					scoreTuple.append(avgScores[i]);
				}
				scoreTuple.append(")\"");
				
				writer.write(agentTuple + "," + scoreTuple + "\n");
			}
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------------
	
	public synchronized Stats[] agentPoints() 
	{
		return agentPoints;
	}
	
	//-------------------------------------------------------------------------
	
}
