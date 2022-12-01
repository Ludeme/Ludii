package app.loading;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import app.DesktopApp;
import app.PlayerApp;
import app.display.MainWindowDesktop;
import app.utils.GameUtil;
import app.utils.SettingsExhibition;
import main.Constants;
import manager.ai.AIUtil;
import manager.utils.game_logs.MatchRecord;
import other.context.Context;
import other.move.Move;

public class TrialLoading
{
	
	//-------------------------------------------------------------------------

	/**
	 * Select and Save the current trial of the current game to an external file.
	 */
	public static void saveTrial(final PlayerApp app)
	{
		final int fcReturnVal = DesktopApp.saveGameFileChooser().showSaveDialog(DesktopApp.frame());

		if (fcReturnVal == JFileChooser.APPROVE_OPTION)
		{
			File file = DesktopApp.saveGameFileChooser().getSelectedFile();
			String filePath = file.getAbsolutePath();
			if (!filePath.endsWith(".trl"))
			{
				filePath += ".trl";
				file = new File(filePath);
			}

			saveTrial(app, file);
			
			if (app.settingsPlayer().saveHeuristics())
			{
//				AIUtils.saveHeuristicScores
//				(
//					app.manager().ref().context().trial(), 
//					app.manager().ref().context(), 
//					app.manager().currGameStartRngState(),
//					new File(filePath.replaceAll(Pattern.quote(".trl"), "_heuristics.csv"))
//				);
			}
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Save the current trial of the current game, to the specified file.
	 */
	public static void saveTrial(final PlayerApp app, final File file)
	{
		try
		{
			final Context context = app.manager().ref().context();
			
			List<String> gameOptionStrings = new ArrayList<>();
			
			if (context.game().description().gameOptions() != null)
				gameOptionStrings = context.game().description().gameOptions().allOptionStrings
									(
										app.manager().settingsManager().userSelections().selectedOptionStrings()
									);

			context.trial().saveTrialToTextFile
			(
				file, app.manager().savedLudName(), gameOptionStrings, app.manager().currGameStartRngState(), false
			);
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Select and load an external trial file.
	 */
	public static void loadTrial(final PlayerApp app, final boolean debug)
	{
		final int fcReturnVal = DesktopApp.loadTrialFileChooser().showOpenDialog(DesktopApp.frame());
		if (fcReturnVal == JFileChooser.APPROVE_OPTION)
		{
			app.manager().ref().interruptAI(app.manager());
			final File file = DesktopApp.loadTrialFileChooser().getSelectedFile();
			loadTrial(app, file, debug);
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Load a specified trial file.
	 */
	public static void loadTrial(final PlayerApp app, final File file, final boolean debug)
	{
		try
		{
			// load game path and options from file first
			try (final BufferedReader reader = new BufferedReader(new FileReader(file)))
			{
				final String gamePathLine = reader.readLine();
				final String loadedGamePath = gamePathLine.substring("game=".length());
				final List<String> gameOptions = new ArrayList<>();

				String nextLine = reader.readLine();
				boolean endOptionsFound = false;
				while (true)
				{
					if (nextLine == null)
						break;
					
					if (nextLine.startsWith("END GAME OPTIONS"))
						endOptionsFound = true;

					if (!nextLine.startsWith("START GAME OPTIONS") && !endOptionsFound)
						gameOptions.add(nextLine);
					
					if (nextLine.startsWith("END GAME OPTIONS"))
						endOptionsFound = true;
					
					if (nextLine.startsWith("LUDII_VERSION") && !nextLine.substring(14).equals(Constants.LUDEME_VERSION))
					{
						System.out.println("Warning! Trial is of version " + nextLine.substring(14));
						MainWindowDesktop.setVolatileMessage(app, "Warning! Trial is of version " + nextLine.substring(14));
					}
					nextLine = reader.readLine();
				}
				
				app.manager().settingsManager().userSelections().setRuleset(Constants.UNDEFINED);
				app.manager().settingsManager().userSelections().setSelectOptionStrings(gameOptions);
				
				GameLoading.loadGameFromName(app, loadedGamePath, gameOptions, debug);
			}

			final MatchRecord loadedRecord = MatchRecord.loadMatchRecordFromTextFile(file, app.manager().ref().context().game());
			
			app.addTextToStatusPanel("Trial Loaded.\n");

			final List<Move> trialMoves = loadedRecord.trial().generateCompleteMovesList();
			app.manager().setCurrGameStartRngState(loadedRecord.rngState());
			GameUtil.resetGame(app, true);

			app.manager().ref().makeSavedMoves(app.manager(), trialMoves);
		}
		catch (final IOException exception)
		{
			exception.printStackTrace();
		}

		AIUtil.pauseAgentsIfNeeded(app.manager());
	}

	//-------------------------------------------------------------------------

	/**
	 * Only called when the app is opened. Loads the saved trial.
	 */
	public static void loadStartTrial(final PlayerApp app)
	{
		if (SettingsExhibition.exhibitionVersion)
			return;
		
		try
		{
			final File file = new File("." + File.separator + "ludii.trl");
			if (!file.exists())
			{
				try
				{
					file.createNewFile();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
			else if (DesktopApp.shouldLoadTrial())
			{
				TrialLoading.loadTrial(app, file, false);
			}
		}
		catch (final Exception e)
		{
			// try to delete trial
			final File brokenPreferences = new File("." + File.separator + "ludii.trl");
			brokenPreferences.delete();
		}
	}
	
	//-------------------------------------------------------------------------
	
}
