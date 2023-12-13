package reconstruction.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import main.options.Ruleset;
import manager.utils.game_logs.MatchRecord;
import metrics.Utils;
import other.GameLoader;
import other.context.Context;
import other.move.Move;
import other.topology.Edge;
import other.trial.Trial;

/***
 * To apply some transformation to the edge usage vectors of the museum game rulesets.
 */
public class MuseumXPSymetries {
 
	/** The trials. */
	private static List<Trial> trials = new ArrayList<Trial>();

	/** The folder with the trials to use. */
	private static String folderTrials = "/res/trials/";

	// The RNGs of each trial.
	private static List<RandomProviderState> allStoredRNG = new ArrayList<RandomProviderState>();
	
	// The transformations (left/right for the moment).
	final static TIntIntHashMap transformations = new TIntIntHashMap();
	
	final static String rulesetName = "Ruleset/Haretavl Switch Three Dogs Two Hares Starting Position - Both Extensions Joined Diagonal (Suggested)";
	
	// -----------------------------------------------------------------------------------
	
	/**
	 * Main method
	 * @param args
	 */
	public static void main(final String[] args)
	{
		transformations.put(0,1);
		transformations.put(2,3);
		transformations.put(15,16);
		transformations.put(12,13);
		transformations.put(18,17);
		transformations.put(5,4);
		transformations.put(6,7);
		
		computeEdgeSymetries(rulesetName);
	}
	
	// -----------------------------------------------------------------------------------
	
	/**
	 * Compute the edge usage results in taking into account the transformations
	 * @param rulesetExpected
	 */
	public static void computeEdgeSymetries(final String rulesetExpected)
	{
		final String gameName = "/lud/board/hunt/Ludus Coriovalli.lud";
		final Game game = GameLoader.loadGameFromName(gameName);
		
		final List<Ruleset> rulesetsInGame = game.description().rulesets();
		
		Game rulesetGame = null;
		
		// Code for games with many rulesets
		if (rulesetsInGame != null && !rulesetsInGame.isEmpty()) 
		{
			for (int rs = 0; rs < rulesetsInGame.size(); rs++)
			{
				final Ruleset ruleset = rulesetsInGame.get(rs);

				// We check if we want a specific ruleset.
				if (!rulesetExpected.isEmpty() && !rulesetExpected.equals(ruleset.heading()))
					continue;

				rulesetGame = GameLoader.loadGameFromName(gameName, ruleset.optionSettings());
			}
		}

		if(rulesetGame == null)
			System.err.println("Game or Ruleset unknown");
		
		System.out.println("Game Name = " + rulesetGame.name());
		System.out.println("Ruleset Name = " + rulesetGame.getRuleset().heading());
		
		getTrials(rulesetGame);
		
		System.out.println("trial size = " + trials.size());
		
		// The iterative vector and the final vector to obtain.
		final TDoubleArrayList edgesUsageMinisingSymetryDistance = new TDoubleArrayList();	
		for(int i = 0; i < rulesetGame.board().topology().edges().size(); i++)
			edgesUsageMinisingSymetryDistance.add(0.0);
		
		for (int trialIndex = 0; trialIndex < trials.size() ; trialIndex++)
		{
			// The edge usage on the current trial.
			final TDoubleArrayList edgesUsageCurrentTrial = new TDoubleArrayList();	
			for(int i = 0; i < rulesetGame.board().topology().edges().size(); i++)
				edgesUsageCurrentTrial.add(0.0);
			
			final Trial trial = trials.get(trialIndex);
			final RandomProviderState rngState = allStoredRNG.get(trialIndex);

			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(rulesetGame, rngState);
			int totalEdgesUsage = 0;
			
			// Run the playout.
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				// We go to the next move.
				context.game().apply(context, trial.getMove(i));
				
				// FOR THE MUSEUM GAME
				// To count the frequency/usage of each edge on the board.
				final Move lastMove = context.trial().lastMove();
				final int vertexFrom = lastMove.fromNonDecision();
				// To not take in account moves coming from the hand.
				if(vertexFrom < 0 || vertexFrom >= rulesetGame.board().topology().vertices().size())
					continue;
				final int vertexTo = lastMove.toNonDecision();

				for(int j = 0; j < rulesetGame.board().topology().edges().size(); j++)
				{
					final Edge edge = rulesetGame.board().topology().edges().get(j);
					if((edge.vertices().get(0).index() == vertexFrom && edge.vertices().get(1).index() == vertexTo) ||
							(edge.vertices().get(0).index() == vertexTo && edge.vertices().get(1).index() == vertexFrom))
						edgesUsageCurrentTrial.set(j, edgesUsageCurrentTrial.get(j) + 1);
				}
				totalEdgesUsage++;
			}
			
			for(int i = 0; i < rulesetGame.board().topology().edges().size(); i++)
				edgesUsageCurrentTrial.set(i, edgesUsageCurrentTrial.get(i) / (double) totalEdgesUsage*100);
			
			if(trialIndex == 0)
			{
				for(int i = 0; i < rulesetGame.board().topology().edges().size(); i++)
					edgesUsageMinisingSymetryDistance.set(i, edgesUsageCurrentTrial.get(i));
				
				for(int i = 0; i < rulesetGame.board().topology().edges().size(); i++)
					System.out.println(i + "," + edgesUsageMinisingSymetryDistance.get(i));
			}
			else // Minimise vector edge usage on current trial and the vector edge usage of all previous trials.
			{
				// Compute transformation of current edge
				final TDoubleArrayList edgesUsageCurrentTrialAfterTransformation = new TDoubleArrayList();
				for(int i = 0; i < rulesetGame.board().topology().edges().size(); i++)
					edgesUsageCurrentTrialAfterTransformation.add(0.0);
					
				for(int i = 0; i < edgesUsageCurrentTrial.size(); i++)
					edgesUsageCurrentTrialAfterTransformation.set(i, edgesUsageCurrentTrial.get(i));
				for(int i = 0; i < edgesUsageCurrentTrial.size(); i++)
				{
					if(transformations.get(i) != -99) // -99 is the value returned if the key does not exist.
					{
						edgesUsageCurrentTrialAfterTransformation.set(i, edgesUsageCurrentTrial.get(transformations.get(i)));
						edgesUsageCurrentTrialAfterTransformation.set(transformations.get(i), edgesUsageCurrentTrial.get(i));
					}
				}
				
				// Compute distance with vector edge usage of all previous trials
				final double distanceCurrent = euclidianDistance(edgesUsageMinisingSymetryDistance, edgesUsageCurrentTrial);
				final double distanceCurrentWithTransformation = euclidianDistance(edgesUsageMinisingSymetryDistance, edgesUsageCurrentTrialAfterTransformation);
				
//				System.out.println("current trial edge usage");
//				for(int i = 0; i < rulesetGame.board().topology().edges().size(); i++)
//					System.out.println(i + "," + edgesUsageCurrentTrial.get(i));
//				System.out.println("distance with previous = " + distanceCurrent);
//				
//				System.out.println("current trial edge usage after transformation");
//				for(int i = 0; i < rulesetGame.board().topology().edges().size(); i++)
//					System.out.println(i + "," + edgesUsageCurrentTrialAfterTransformation.get(i));
//				System.out.println("distance with previous = " + distanceCurrentWithTransformation);
				
				// Keep the transformation only if they make the distance smaller.
				if(distanceCurrent > distanceCurrentWithTransformation)
				{
					//System.out.println("transformation applied");
					for(int i = 0; i < edgesUsageCurrentTrial.size(); i++)
						edgesUsageCurrentTrial.set(i, edgesUsageCurrentTrialAfterTransformation.get(i));
				}
				
				// Compute the new avg usage after applying or not the transformation
				for(int i = 0; i < rulesetGame.board().topology().edges().size(); i++)
					edgesUsageMinisingSymetryDistance.set(i, (edgesUsageCurrentTrial.get(i) + trialIndex * edgesUsageMinisingSymetryDistance.get(i))/ (trialIndex +1));
				
//				for(int i = 0; i < rulesetGame.board().topology().edges().size(); i++)
//					System.out.println(i + "," + edgesUsageMinisingSymetryDistance.get(i));
			}
		}
		
		System.out.println("Final results are");
		for(int i = 0; i < rulesetGame.board().topology().edges().size(); i++)
			System.out.println(i + "," + edgesUsageMinisingSymetryDistance.get(i));
	}
	
	/**
	 * @param game The game.
	 */
	private static void getTrials(final Game game)
	{
		final File currentFolder = new File(".");
		final File folder = new File(currentFolder.getAbsolutePath() + folderTrials);
		final String gameName = game.name();
		final String rulesetName = game.getRuleset() == null ? "" : game.getRuleset().heading();

//		System.out.println("GAME NAME = " + gameName);
//		System.out.println("RULESET NAME = " + rulesetName);

		String trialFolderPath = folder + "/" + gameName;
		if (!rulesetName.isEmpty())
			trialFolderPath += File.separator + rulesetName.replace("/", "_");

		final File trialFolder = new File(trialFolderPath);

		if (trialFolder.exists())
			System.out.println("TRIALS FOLDER EXIST");
		else
			System.out.println("DO NOT FOUND IT - Path is " + trialFolder);

		for (final File trialFile : trialFolder.listFiles())
		{
			//System.out.println(trialFile.getName());
			if(trialFile.getName().contains(".txt"))
			{
				MatchRecord loadedRecord;
				try
				{
					loadedRecord = MatchRecord.loadMatchRecordFromTextFile(trialFile, game);
					final Trial loadedTrial = loadedRecord.trial();
					trials.add(loadedTrial);
					allStoredRNG.add(loadedRecord.rngState());
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
	
	/**
	 * Note: We assume both vectors have the same size.
	 * @param vector1
	 * @param vector2
	 */
	private static double euclidianDistance(final TDoubleArrayList vector1, final TDoubleArrayList vector2)
	{
		double sumDifferenceSquared = 0.0;
		for(int i = 0 ; i < vector1.size(); i++)
		{
			final double x = vector1.get(i);
			final double y = vector2.get(i);
			sumDifferenceSquared += (x - y) * (x - y);
		}
		return Math.sqrt(sumDifferenceSquared);
	}
	
}
