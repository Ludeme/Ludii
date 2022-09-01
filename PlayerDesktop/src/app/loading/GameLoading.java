package app.loading;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;

import app.DesktopApp;
import app.PlayerApp;
import app.display.dialogs.GameLoaderDialog;
import app.utils.GameSetup;
import main.Constants;
import main.FileHandling;
import main.GameNames;
import other.GameLoader;

public class GameLoading
{

	//-------------------------------------------------------------------------

	/**
	 * Load a game from an external .lud file.
	 */
	public static void loadGameFromFile(final PlayerApp app)
	{
		final int fcReturnVal = DesktopApp.gameFileChooser().showOpenDialog(DesktopApp.frame());

		if (fcReturnVal == JFileChooser.APPROVE_OPTION)
		{
			File file = DesktopApp.gameFileChooser().getSelectedFile();
			String filePath = file.getAbsolutePath();
			if (!filePath.endsWith(".lud"))
			{
				filePath += ".lud";
				file = new File(filePath);
			}
			
			if (file.exists())
			{
				final String fileName = file.getAbsolutePath();
				
				// TODO if we want to preserve per-game last-selected-options in preferences, load them here
				app.manager().settingsManager().userSelections().setRuleset(Constants.UNDEFINED);
				app.manager().settingsManager().userSelections().setSelectOptionStrings(new ArrayList<String>());
				loadGameFromFilePath(app, fileName);
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Load a specified external .lud file.
	 */
	public static void loadGameFromFilePath(final PlayerApp app, final String filePath)
	{
		if (filePath != null)
		{
			app.manager().setSavedLudName(filePath);

			String desc = "";
			try
			{
				app.settingsPlayer().setLoadedFromMemory(false);
				desc = FileHandling.loadTextContentsFromFile(filePath);
				GameSetup.compileAndShowGame(app, desc, false);
			}
			catch (final FileNotFoundException ex)
			{
				System.out.println("Unable to open file '" + filePath + "'");
			}
			catch (final IOException ex)
			{
				System.out.println("Error reading file '" + filePath + "'");
			}
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Load an internal .lud game description from memory
	 */
	public static void loadGameFromMemory(final PlayerApp app, final boolean debug)
	{
		final String[] choices = FileHandling.listGames();

		String initialChoice = choices[0];
		for (final String choice : choices)
		{
			if (app.manager().savedLudName() != null && app.manager().savedLudName().endsWith(choice.replaceAll(Pattern.quote("\\"), "/")))
			{
				initialChoice = choice;
				break;
			}
		}
		final String choice = GameLoaderDialog.showDialog(DesktopApp.frame(), choices, initialChoice);

		if (choice != null)
		{
			// TODO if we want to preserve per-game last-selected-options in preferences, load them here
			app.manager().settingsManager().userSelections().setRuleset(Constants.UNDEFINED);
			app.manager().settingsManager().userSelections().setSelectOptionStrings(new ArrayList<String>());
			loadGameFromMemory(app, choice, debug);
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Load a selected internal .lud game description.
	 */
	public static void loadGameFromMemory(final PlayerApp app, final String gamePath, final boolean debug)
	{
		// Get game description from resource
		final StringBuilder sb = new StringBuilder();
		if (gamePath != null)
		{
			InputStream in = null;

			String path = gamePath.replaceAll(Pattern.quote("\\"), "/");
			path = path.substring(path.indexOf("/lud/"));

			app.manager().setSavedLudName(path);

			in = GameLoader.class.getResourceAsStream(path);

			try (final BufferedReader rdr = new BufferedReader(new InputStreamReader(in, "UTF-8")))
			{
				String line;
				while ((line = rdr.readLine()) != null)
					sb.append(line + "\n");
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
			
			app.settingsPlayer().setLoadedFromMemory(true);
			GameSetup.compileAndShowGame(app, sb.toString(), debug);
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Load game from name.
	 *
	 * @param name Filename + .lud extension.
	 * @param options List of options to select
	 */
	public static void loadGameFromName(final PlayerApp app, final String name, final List<String> options, final boolean debug)
	{
		try
		{
			final String gameDescriptionString = getGameDescriptionRawFromName(app, name);
			
			if (gameDescriptionString == null)
			{
				loadGameFromFilePath(app, name.substring(0, name.length()));
			}
			else
			{
				app.settingsPlayer().setLoadedFromMemory(true);
				app.manager().settingsManager().userSelections().setRuleset(Constants.UNDEFINED);
				app.manager().settingsManager().userSelections().setSelectOptionStrings(options);
				GameSetup.compileAndShowGame(app, gameDescriptionString, false);
			}
		}
		catch (final Exception e)
		{
			// used if a recent game was selected from an external file.
			
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns the raw game description for a game, based on the name.
	 */
	public static String getGameDescriptionRawFromName(final PlayerApp app, final String name)
	{
		final String filePath = GameLoader.getFilePath(name);
		
		// Probably loading from an external .lud file.
		if (filePath == null)
			return null;
		
		final StringBuilder sb = new StringBuilder();
		
		app.manager().setSavedLudName(filePath);
		
		try (InputStream in = GameLoader.class.getResourceAsStream(filePath))
		{
			try (final BufferedReader rdr = new BufferedReader(new InputStreamReader(in, "UTF-8")))
			{
				String line;
				while ((line = rdr.readLine()) != null)
					sb.append(line + "\n");
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
		}
		catch (final Exception e)
		{
			System.out.println("Did you change the name??");
		}
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Load a random game from the list of possible options in GameNames.java
	 */
	public static void loadRandomGame(final PlayerApp app)
	{
		final List<String> allGameNames = new ArrayList<>();
		EnumSet.allOf(GameNames.class).forEach(game -> allGameNames.add(game.ludName()));
		final Random random = new Random();
		final String chosenGameName = allGameNames.get(random.nextInt(allGameNames.size()));
		
		// TODO if we want to preserve per-game last-selected-options in preferences, load them here
		app.manager().settingsManager().userSelections().setRuleset(Constants.UNDEFINED);
		app.manager().settingsManager().userSelections().setSelectOptionStrings(new ArrayList<String>());
		
		loadGameFromName(app, chosenGameName + ".lud", new ArrayList<String>(), false);
	}
	
	//-------------------------------------------------------------------------

}
