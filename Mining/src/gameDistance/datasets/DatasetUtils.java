package gameDistance.datasets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.Game;
import manager.utils.game_logs.MatchRecord;
import other.GameLoader;
import other.trial.Trial;

/**
 * Game dataset utility functions.
 * 
 * @author matthew.stephenson
 */
public class DatasetUtils
{

	private final static Map<String, List<Trial>> gameTrials = new HashMap<>();
	
	//-------------------------------------------------------------------------
	
	/**
	 * TODO currently the loaded trial doesn't consider rulesetStrings
	 * @param candidateGameName
	 * @param rulesetStrings
	 */
	private static void loadSavedTrials(final Game game, final String keyName)
	{
		final String folderTrials = "/../Trials/TrialsRandom/";
		final File currentFolder = new File(".");

		final File folder = new File(currentFolder.getAbsolutePath() + folderTrials);
		final String gameName = game.name();
		final String rulesetName = game.getRuleset() == null ? "" : game.getRuleset().heading();

		String trialFolderPath = folder + "/" + gameName;
		if(!rulesetName.isEmpty())
			trialFolderPath += File.separator + rulesetName.replace("/", "_");

		final File trialFolder = new File(trialFolderPath);
		
		if(!trialFolder.exists())
			System.out.println("DO NOT FOUND IT - Path is " + trialFolder);

		gameTrials.put(keyName, new ArrayList<>());
		
		for(final File trialFile : trialFolder.listFiles())
		{
			MatchRecord loadedRecord;
			try
			{
				loadedRecord = MatchRecord.loadMatchRecordFromTextFile(trialFile, game);
				final Trial loadedTrial = loadedRecord.trial();
				gameTrials.get(keyName).add(new Trial(loadedTrial));
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
		

//		final String ludPath = candidateGameName.replaceAll(Pattern.quote("\\"), "/");
//		final String trialDirPath = "random_trials" + ludPath.replace(".lud", "");
//
//		final File trialsDir = new File(trialDirPath);
//
//		if (!trialsDir.exists())
//			System.err.println("WARNING: No directory of trials exists at: " + trialsDir.getAbsolutePath());
//
//		final File[] trialFiles = trialsDir.listFiles();
//
//		if (trialFiles.length == 0)
//			System.err.println("WARNING: No trial files exist in directory: " + trialsDir.getAbsolutePath());
//
//		// Parse and compile the game
//		final Game game = GameLoader.loadGameFromName(candidateGameName, rulesetStrings);
//		if (game == null)
//			fail("COMPILATION FAILED for the file : " + ludPath);
//
//		final String keyName = candidateGameName + rulesetStrings.toString();
//		gameTrials.put(keyName, new ArrayList<>());
//		
//		for (final File trialFile : trialFiles)
//		{
//			System.out.println("Testing re-play of trial: " + trialFile);
//			try
//			{
//				final MatchRecord loadedRecord = MatchRecord.loadMatchRecordFromTextFile(trialFile, game);
//				final Trial loadedTrial = loadedRecord.trial();
//				gameTrials.get(keyName).add(new Trial(loadedTrial));
//			}
//			catch (final IOException e)
//			{
//				e.printStackTrace();
//			}
//		}
	}
	
	//-------------------------------------------------------------------------
	
	public static List<Trial> getSavedTrials(Game game)
	{
		final String gamePath = GameLoader.getFilePath(game.name());
		final List<String> rulesetOptions = game.getOptions();
		final String keyName = gamePath + rulesetOptions.toString();
		
		if (!gameTrials.containsKey(keyName))
			loadSavedTrials(game, keyName);
		
		return gameTrials.get(keyName);
	}
	
	//-------------------------------------------------------------------------
	
}
