package gameDistance.datasets;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import game.Game;
import manager.utils.game_logs.MatchRecord;
import other.GameLoader;
import other.trial.Trial;

public class DatasetUtils
{

	//-------------------------------------------------------------------------
	
	private final static Map<String, List<Trial>> gameTrials = new HashMap<>();
	
	//-------------------------------------------------------------------------
	
	/**
	 * TODO currently the loaded trial doesn't consider rulesetStrings
	 * @param candidateGameName
	 * @param rulesetStrings
	 */
	private static void loadSavedTrials(final String candidateGameName, final List<String> rulesetStrings)
	{
		final String ludPath = candidateGameName.replaceAll(Pattern.quote("\\"), "/");
		final String trialDirPath = "random_trials" + ludPath.replace(".lud", "");

		final File trialsDir = new File(trialDirPath);

		if (!trialsDir.exists())
			System.err.println("WARNING: No directory of trials exists at: " + trialsDir.getAbsolutePath());

		final File[] trialFiles = trialsDir.listFiles();

		if (trialFiles.length == 0)
			System.err.println("WARNING: No trial files exist in directory: " + trialsDir.getAbsolutePath());

		// Parse and compile the game
		final Game game = GameLoader.loadGameFromName(candidateGameName, rulesetStrings);
		if (game == null)
			fail("COMPILATION FAILED for the file : " + ludPath);

		final String keyName = candidateGameName + rulesetStrings.toString();
		gameTrials.put(keyName, new ArrayList<>());
		
		for (final File trialFile : trialFiles)
		{
			System.out.println("Testing re-play of trial: " + trialFile);
			try
			{
				final MatchRecord loadedRecord = MatchRecord.loadMatchRecordFromTextFile(trialFile, game);
				final Trial loadedTrial = loadedRecord.trial();
				gameTrials.get(keyName).add(new Trial(loadedTrial));
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
		
		if (!gameTrials.containsKey(gamePath + rulesetOptions.toString()))
			loadSavedTrials(gamePath, rulesetOptions);
		
		return gameTrials.get(gamePath + rulesetOptions.toString());
	}
	
	//-------------------------------------------------------------------------
	
}
