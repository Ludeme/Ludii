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
