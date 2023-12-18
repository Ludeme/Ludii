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
import main.Constants;
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
	final static TIntIntHashMap transformationsLeftRight = new TIntIntHashMap();
	
	// The transformations (left/right for the moment).
	final static TIntIntHashMap transformationsTopBottom = new TIntIntHashMap();

	/** The path of the museum game. */
	private static String gameName = "/lud/board/hunt/Ludus Coriovalli.lud";
	
	//final static String rulesetName = "Ruleset/Haretavl Three Dogs Two Hares Starting Position - Both Extensions Joined Diagonal (Suggested)";
	//final static String rulesetName = "Ruleset/Haretavl Three Dogs Two Hares - Top Extension No Joined Diagonal (Suggested)";
	//final static String rulesetName = "Ruleset/Haretavl Switch Three Dogs Two Hares Starting Position 1 - Top Extension No Joined Diagonal (Suggested)";
	//final static String rulesetName = "Ruleset/Haretavl Three Dogs Two Hares Starting Position 2 - Top Extension No Joined Diagonal (Suggested)";
	//final static String rulesetName = "Ruleset/Haretavl - No Extension Joined Diagonal (Suggested)";
	final static String rulesetName = "Ruleset/Haretavl - Top Extension Joined Diagonal (Suggested)";
	
	// -----------------------------------------------------------------------------------
	
	/**
	 * Main method
	 * @param args
	 */
	public static void main(final String[] args)
	{
		switch(rulesetName)
		{
		case "Ruleset/Haretavl - Top Extension Joined Diagonal (Suggested)":
		transformationsLeftRight.put(0,1);
		transformationsLeftRight.put(2,3);
		transformationsLeftRight.put(10,11);
		transformationsLeftRight.put(5,4);
		transformationsLeftRight.put(13,14);
		transformationsLeftRight.put(16,15);
		transformationsLeftRight.put(6,7);
		transformationsLeftRight.put(8,9);
		break;
		
		case "Ruleset/Haretavl Three Dogs Two Hares Starting Position - Both Extensions Joined Diagonal (Suggested)":
		transformationsLeftRight.put(0,1);
		transformationsLeftRight.put(2,3);
		transformationsLeftRight.put(15,16);
		transformationsLeftRight.put(12,13);
		transformationsLeftRight.put(18,17);
		transformationsLeftRight.put(5,4);
		transformationsLeftRight.put(6,7);
		break;
		
		case "Ruleset/Haretavl Three Dogs Two Hares - Top Extension No Joined Diagonal (Suggested)":
		transformationsLeftRight.put(0,3);
		transformationsLeftRight.put(1,2);
		transformationsLeftRight.put(4,5);
		transformationsLeftRight.put(15,19);
		transformationsLeftRight.put(14,20);
		transformationsLeftRight.put(7,6);
		transformationsLeftRight.put(16,18);
		transformationsLeftRight.put(8,11);
		transformationsLeftRight.put(9,10);
		break;
		
		case "Ruleset/Haretavl Switch Three Dogs Two Hares Starting Position 1 - Top Extension No Joined Diagonal (Suggested)":
		transformationsLeftRight.put(0,3);
		transformationsLeftRight.put(1,2);
		transformationsLeftRight.put(4,5);
		transformationsLeftRight.put(15,19);
		transformationsLeftRight.put(14,20);
		transformationsLeftRight.put(7,6);
		transformationsLeftRight.put(16,18);
		transformationsLeftRight.put(8,11);
		transformationsLeftRight.put(9,10);
		break;
		
		case "Ruleset/Haretavl Three Dogs Two Hares Starting Position 2 - Top Extension No Joined Diagonal (Suggested)":
		transformationsLeftRight.put(0,3);
		transformationsLeftRight.put(1,2);
		transformationsLeftRight.put(4,5);
		transformationsLeftRight.put(15,19);
		transformationsLeftRight.put(14,20);
		transformationsLeftRight.put(7,6);
		transformationsLeftRight.put(16,18);
		transformationsLeftRight.put(8,11);
		transformationsLeftRight.put(9,10);
		break;

		case "Ruleset/Haretavl - No Extension Joined Diagonal (Suggested)":
		transformationsLeftRight.put(0,1);
		transformationsLeftRight.put(11,12);
		transformationsLeftRight.put(2,3);
		transformationsLeftRight.put(8,9);
		transformationsLeftRight.put(5,4);
		transformationsLeftRight.put(14,13);
		transformationsLeftRight.put(6,7);
		
		transformationsTopBottom.put(5,2);
		transformationsTopBottom.put(3,4);
		transformationsTopBottom.put(7,1);
		transformationsTopBottom.put(6,0);
		transformationsTopBottom.put(13,12);
		transformationsTopBottom.put(14,11);
		break;
		}
		
		computeEdgeSymetries(rulesetName);
	}
	
	// -----------------------------------------------------------------------------------
	
	/**
	 * Compute the edge usage results in taking into account the transformations
	 * @param rulesetExpected
	 */
	public static void computeEdgeSymetries(final String rulesetExpected)
	{
		Game rulesetGame = getRuleset(gameName, rulesetExpected);
		getTrials(rulesetGame);
		
		System.out.println("trial size = " + trials.size());
		
		// The vector used to get the edge usage after each trial.
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
				edgesUsageCurrentTrial.set(i, edgesUsageCurrentTrial.get(i) / (double) totalEdgesUsage * 100);
			
			if(trialIndex == 0)
			{
				for(int i = 0; i < rulesetGame.board().topology().edges().size(); i++)
					edgesUsageMinisingSymetryDistance.set(i, edgesUsageCurrentTrial.get(i));
				
				for(int i = 0; i < rulesetGame.board().topology().edges().size(); i++)
					System.out.println(i + "," + edgesUsageMinisingSymetryDistance.get(i));
			}
			else // Minimise vector edge usage on current trial and the vector edge usage of all previous trials.
			{
				// Compute transformation Left/Right of current edge
				final TDoubleArrayList edgesUsageCurrentTrialAfterTransformationLeftRight = new TDoubleArrayList();
				if(!transformationsLeftRight.isEmpty())
				{
					for(int i = 0; i < rulesetGame.board().topology().edges().size(); i++)
						edgesUsageCurrentTrialAfterTransformationLeftRight.add(0.0);
						
					for(int i = 0; i < edgesUsageCurrentTrial.size(); i++)
						edgesUsageCurrentTrialAfterTransformationLeftRight.set(i, edgesUsageCurrentTrial.get(i));
					for(int i = 0; i < edgesUsageCurrentTrial.size(); i++)
					{
						if(transformationsLeftRight.get(i) != -99) // -99 is the value returned if the key does not exist.
						{
							edgesUsageCurrentTrialAfterTransformationLeftRight.set(i, edgesUsageCurrentTrial.get(transformationsLeftRight.get(i)));
							edgesUsageCurrentTrialAfterTransformationLeftRight.set(transformationsLeftRight.get(i), edgesUsageCurrentTrial.get(i));
						}
					}
				}
				
				// Compute transformation Left/Right of current edge
				final TDoubleArrayList edgesUsageCurrentTrialAfterTransformationTopBottom = new TDoubleArrayList();
				if(!transformationsTopBottom.isEmpty())
				{
					for(int i = 0; i < rulesetGame.board().topology().edges().size(); i++)
						edgesUsageCurrentTrialAfterTransformationTopBottom.add(0.0);
					
					for(int i = 0; i < edgesUsageCurrentTrial.size(); i++)
						edgesUsageCurrentTrialAfterTransformationTopBottom.set(i, edgesUsageCurrentTrial.get(i));
					for(int i = 0; i < edgesUsageCurrentTrial.size(); i++)
					{
						if(transformationsTopBottom.get(i) != -99) // -99 is the value returned if the key does not exist.
						{
							edgesUsageCurrentTrialAfterTransformationTopBottom.set(i, edgesUsageCurrentTrial.get(transformationsTopBottom.get(i)));
							edgesUsageCurrentTrialAfterTransformationTopBottom.set(transformationsTopBottom.get(i), edgesUsageCurrentTrial.get(i));
						}
					}
				}
				
				// Compute distance with vector edge usage of all previous trials
				final double distanceCurrent = euclidianDistance(edgesUsageMinisingSymetryDistance, edgesUsageCurrentTrial);
				final double distanceCurrentWithTransformationLeftRight = (edgesUsageCurrentTrialAfterTransformationLeftRight.isEmpty()) ? Constants.INFINITY : euclidianDistance(edgesUsageMinisingSymetryDistance, edgesUsageCurrentTrialAfterTransformationLeftRight);
				final double distanceCurrentWithTransformationTopBottom = (edgesUsageCurrentTrialAfterTransformationTopBottom.isEmpty()) ? Constants.INFINITY : euclidianDistance(edgesUsageMinisingSymetryDistance, edgesUsageCurrentTrialAfterTransformationTopBottom);
				
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
				if(distanceCurrentWithTransformationTopBottom > distanceCurrentWithTransformationLeftRight)
				{
					if(distanceCurrent > distanceCurrentWithTransformationLeftRight)
					{
						//System.out.println("transformation applied");
						for(int i = 0; i < edgesUsageCurrentTrial.size(); i++)
							edgesUsageCurrentTrial.set(i, edgesUsageCurrentTrialAfterTransformationLeftRight.get(i));
					}
					
					if(distanceCurrent > distanceCurrentWithTransformationTopBottom)
					{
						//System.out.println("transformation applied");
						for(int i = 0; i < edgesUsageCurrentTrial.size(); i++)
							edgesUsageCurrentTrial.set(i, edgesUsageCurrentTrialAfterTransformationTopBottom.get(i));
					}
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
	
	// ---------------------------
	
	/**
	 * @param game The game.
	 */
	private static Game getRuleset(final String gameName, final String rulesetExpected)
	{
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
		
		return rulesetGame;
	}
	
	// ---------------------------
	
	/**
	 * @param game The game.
	 */
	private static void getTrials(final Game game)
	{
		final File currentFolder = new File(".");
		final File folder = new File(currentFolder.getAbsolutePath() + folderTrials);
		final String gameName = game.name();
		final String rulesetName = game.getRuleset() == null ? "" : game.getRuleset().heading();

		String trialFolderPath = folder + "/" + gameName;
		if (!rulesetName.isEmpty())
			trialFolderPath += File.separator + rulesetName.replace("/", "_");

		final File trialFolder = new File(trialFolderPath);

		if (!trialFolder.exists())
			System.out.println("DO NOT FOUND TRIALS - Path is " + trialFolder);

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
	
	// ---------------------------
	
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
