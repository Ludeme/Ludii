package app.utils;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.rng.core.RandomProviderDefaultState;

import app.PlayerApp;
import compiler.Compiler;
import game.Game;
import main.Constants;
import main.grammar.Description;
import main.grammar.Report;
import manager.ai.AIDetails;
import manager.ai.AIMenuName;
import util.StringUtil;

/**
 * Functions to assist with setting up games.
 * 
 * @author Matthew.Stephenson
 */
public class GameSetup 
{

	//-------------------------------------------------------------------------
	
	/**
	 * Compile and display the specified description with the corresponding menu options.
	 * @param desc
	 * @param menuOption
	 * @param debug
	 */
	public static void compileAndShowGame(final PlayerApp app, final String desc, final boolean menuOption, final String filePath, final boolean debug)
	{
		final Description gameDescription = new Description(desc);
		final Report report = new Report();
		report.setReportMessageFunctions(new ReportMessengerGUI(app));
		
		try
		{
			final Game game = (Game)Compiler.compile(gameDescription, app.manager().settingsManager().userSelections(), report, debug);
			app.manager().ref().setGame(app.manager(), game);			
			GameUtil.resetGame(app, false);
	
//			if (app.manager().ref().context() != null)
//				app.manager().ref().interruptAI(app.manager());
//	
//			app.manager().ref().setGame(app.manager(), game);
//			app.manager().updateCurrentGameRngInternalState();
//			GameUtil.startGame(app);
//			app.manager().setSavedTrial(null);
//			app.resetGameVariables();
//			
//			MVCSetup.setMVC(app);
//			app.settingsPlayer().updateRecentGames(app, app.manager().ref().context().game().name());
//			app.updateFrameTitle();
//			app.resetPanels();
//
//			app.loadGameSpecificPreferences();
//			app.manager().ref().context().game().description().setFilePath(filePath);
//			app.contextSnapshot().setContext(app);
			
//			view.createPanels();
//			Arrays.fill(view.playerSwatchList, null);
//			Arrays.fill(view.playerNameList, null);
//			Arrays.fill(view.playerSwatchHover, false);
//			Arrays.fill(view.playerNameHover, false);	
//			//MainMenu.updateOptionsMenu(this, manager().ref().context(), MainMenu.mainOptionsMenu);
//			resetMenuGUI();
			
			System.out.println("\nCompiled " + game.name() + " successfully.");
			
			if (!app.settingsPlayer().savedStatusTabString().equals(""))
			{
				app.addTextToStatusPanel("-------------------------------------------------\n");
			}
			if (report.isWarning())
			{
				for (final String warning : report.warnings())
					app.reportError("Warning: " + warning);
			}
			if (game.hasMissingRequirement())
			{
				app.reportError("");
				app.reportError("Requirement Warning: ");
				final List<String> missingRequirements = game.requirementReport();
				for (final String missingRequirement : missingRequirements)
					app.reportError("--> " + missingRequirement);
				app.reportError("");
			}
			if (game.willCrash())
			{
				app.reportError("");
				app.reportError("Crash Warning: ");
				final List<String> crashes = game.crashReport();
				for (final String crash : crashes)
					app.reportError("--> " + crash);
				app.reportError("");
			}
			if (game.equipmentWithStochastic())
			{
				app.addTextToStatusPanel("Warning: This game uses stochastic equipment, automatic trial saving is disabled.\n");
			}
			if (debug)
			{
				app.writeTextToFile("debug_log.txt", report.log());
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			app.reportError(e.getMessage());
		}
		
		// Try to make Java run GC in case previous game occupied a lot of memory
		System.gc();
	}
	
	//-------------------------------------------------------------------------

	public static void setupNetworkGame(final PlayerApp app, final String gameName, final List<String> gameOptions, final String inputLinePlayerNumber, final boolean aiAllowed, final int selectedGameID, final int turnLimit)
	{
		app.manager().settingsNetwork().setLoadingNetworkGame(true);
		
		try
		{
	    	if (!inputLinePlayerNumber.equals("") && StringUtil.isInteger(inputLinePlayerNumber))
	    	{
	    		// Disable features which are not allowed in network games.
	    		EventQueue.invokeLater(() -> 
				{
					app.settingsPlayer().setCursorTooltipDev(false);
					app.settingsPlayer().setSwapRule(false);
					app.settingsPlayer().setNoRepetition(false);
					app.settingsPlayer().setNoRepetitionWithinTurn(false);
					app.settingsPlayer().setSandboxMode(false);
				});
	    		
	    		final int playerNumber = Integer.parseInt(inputLinePlayerNumber);
	    		
	    		// Format the stored string into the syntax needed for loading the game.
	    		final List<String> formattedGameOptions = new ArrayList<>();
	    		for (int i = 0; i < gameOptions.size(); i++)
	    		{
	    			String formattedString = gameOptions.get(i);
	    			formattedString = formattedString.replaceAll("_", " ");
	    			formattedString = formattedString.replaceAll("\\|", "/");
	    			formattedGameOptions.add(formattedString);
	    		}
	    		
	    		if (!formattedGameOptions.get(0).equals("-") && !formattedGameOptions.get(0).equals(""))
	    			app.loadGameFromName(gameName, formattedGameOptions, false);
	    		else
	    			app.loadGameFromName(gameName, new ArrayList<String>(), false);
	    		
	    		if (playerNumber > Constants.MAX_PLAYERS)
	    			app.addTextToStatusPanel("Joined game as a spectator\n");
	    		else
	    			app.addTextToStatusPanel("Joined game as player number " + playerNumber + "\n");
	    		
	    		app.manager().settingsNetwork().setActiveGameId(selectedGameID); 		
	    		app.manager().settingsNetwork().setNetworkPlayerNumber(playerNumber);
	    		
	    		app.manager().ref().context().game().setMaxTurns(turnLimit);
	    		final String gameRNG = app.manager().databaseFunctionsPublic().getRNG(app.manager());
	
				final String[] byteStrings = gameRNG.split(Pattern.quote(","));
				final byte[] bytes = new byte[byteStrings.length];
				for (int i = 0; i < byteStrings.length; ++i)
					bytes[i] = Byte.parseByte(byteStrings[i]);

				final RandomProviderDefaultState rngState = new RandomProviderDefaultState(bytes);
				app.manager().ref().context().rng().restoreState(rngState);
				GameUtil.startGame(app);
				app.manager().setCurrGameStartRngState(rngState);
				
				for (int i = 0; i < app.manager().aiSelected().length; i++)
				{
					if (app.manager().aiSelected()[i].ai() != null)
						app.manager().aiSelected()[i].ai().closeAI();
					
					app.manager().aiSelected()[i] = new AIDetails(app.manager(), null, i, AIMenuName.Human);
				}
				
				app.manager().settingsNetwork().setOnlineAIAllowed(aiAllowed);
	    	}
	    	else
	    	{
	    		app.addTextToStatusPanel(inputLinePlayerNumber);
	    	}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			// carry on.
		}
		
		app.manager().settingsNetwork().setLoadingNetworkGame(false);
	}
	
	//-------------------------------------------------------------------------

}
