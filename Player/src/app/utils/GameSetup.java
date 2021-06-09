package app.utils;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.rng.core.RandomProviderDefaultState;
import org.json.JSONObject;

import app.PlayerApp;
import compiler.Compiler;
import game.Game;
import main.Constants;
import main.grammar.Description;
import main.grammar.Report;
import manager.ai.AIDetails;
import manager.ai.AIMenuName;
import manager.ai.AIUtil;
import other.AI;
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
		app.clearGraphicsCache();
		app.settingsPlayer().setLoadSuccessful(false);
		app.settingsPlayer().setMatchDescriptionFull("");
		app.settingsPlayer().setMatchDescriptionShort("");
		final Description gameDescription = new Description(desc);
		final Report report = new Report();
		report.setReportMessageFunctions(new ReportMessengerGUI(app));
		
		try
		{
			final Game game =	(Game)Compiler.compile
						(
							gameDescription, 
							app.manager().settingsManager().userSelections(), 
							report, 
							debug
						);				
			if (menuOption || app.settingsPlayer().preferencesLoaded())
			{
				// Use the existing options selected by the user
				app.settingsPlayer().setPreferencesLoaded(false);
			}
			
			if (game.hasSubgames())
			{
				app.settingsPlayer().setMatchDescriptionFull(gameDescription.raw());
				app.settingsPlayer().setMatchDescriptionShort(gameDescription.expanded());
			}
			
			if (app.manager().ref().context() != null)
				app.manager().ref().interruptAI(app.manager());
	
			app.manager().ref().setGame(app.manager(), game);
			app.contextSnapshot().setContext(app);
			MVCSetup.setMVC(app);
			
			app.settingsPlayer().updateRecentGames(app, app.manager().ref().context().game().name());
			
			app.updateFrameTitle();
			
			GameSetup.cleanUpAfterLoading(app, game, true);
			
			app.settingsPlayer().setLoadSuccessful(true);
			app.loadGameSpecificPreferences();
			app.manager().ref().context().game().description().setFilePath(filePath);
			
			System.out.println("\nCompiled " + game.name() + " successfully.");
			
			if (!app.settingsPlayer().savedStatusTabString().equals(""))
				app.addTextToStatusPanel("-------------------------------------------------\n");
	
			if (app.manager().settingsNetwork().getActiveGameId() != 0)
				for (int i = 0; i < app.manager().aiSelected().length; i++)
					app.manager().aiSelected()[i] = new AIDetails(app.manager(), null, i, AIMenuName.Human);
			
			if (report.isWarning())
				for (final String warning : report.warnings())
					app.reportError("Warning: " + warning);
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
			
			if (debug)
				app.writeTextToFile("debug_log.txt", report.log());
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			app.reportError(e.getMessage());
		}
				
		// Free up resources AIs may have held from previous game
		for (int i = 0; i < app.manager().aiSelected().length; i++)
		{
			final AI ai = app.manager().aiSelected()[i].ai();
			if (ai != null)
				ai.closeAI();
		}
	
		AIUtil.checkAISupported(app.manager(), app.manager().ref().context());
	
		try
		{
			EventQueue.invokeLater(() -> 
			{
				app.clearGraphicsCache();
				app.updateTabs(app.manager().ref().context());
				app.repaint();
			});
	
		}
		catch (final Exception e)
		{
			// do nothing
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
	    		
	    		app.updateFrameTitle();
	    		
	    		app.manager().ref().context().game().setMaxTurns(turnLimit);
	    		final String gameRNG = app.manager().databaseFunctionsPublic().getRNG(app.manager());
	
				final String[] byteStrings = gameRNG.split(Pattern.quote(","));
				final byte[] bytes = new byte[byteStrings.length];
	
				for (int i = 0; i < byteStrings.length; ++i)
				{
					bytes[i] = Byte.parseByte(byteStrings[i]);
				}
				final RandomProviderDefaultState rngState = new RandomProviderDefaultState(bytes);
				app.manager().ref().context().rng().restoreState(rngState);
				app.manager().ref().context().game().start(app.manager().ref().context());
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
		catch (final Exception E)
		{
			E.printStackTrace();
			// carry on.
		}
		app.manager().settingsNetwork().setLoadingNetworkGame(false);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Various function calls needed after loading a game.
	 * @param game
	 * @param startGame Do we want to start the game that was passed in? 
	 * 	(false if its a new instance of a Match, because that will have already started)
	 */
	public static void cleanUpAfterLoading(final PlayerApp app, final Game game, final boolean startGame)
	{
		// not called if we have just started the App and the tabs have not been created yet.
		try
		{
			app.resetUIVariables();
	
			if (startGame)
			{
				app.manager().updateCurrentGameRngInternalState();
				game.start(app.manager().ref().context());
			}
			
			if (startGame)	// This is false if we're switching games in middle of Matches, and then we don't want this to reset
				app.manager().setSavedTrial(null);
			
			app.resetPanels();
		}
		catch (final Exception e)
		{
			// do nothing
		}
	
		// If the game is an adversarial puzzle, then set AI to AlphaBeta
		if (game.metadata().graphics().adversarialPuzzle())
		{
			final JSONObject json = new JSONObject().put
									(
										"AI",
										new JSONObject().put("algorithm", "Alpha-Beta")
									);
			AIUtil.updateSelectedAI(app.manager(), json, 2, AIMenuName.AlphaBeta);
		}
		
		EventQueue.invokeLater(() -> 
		{
			if (game.equipmentWithStochastic())
				app.addTextToStatusPanel("Warning: This game uses stochastic equipment, automatic trial saving is disabled.\n");
			
			app.repaint();
		});
		
		app.resetGameVariables(startGame);
	}
	
	//-------------------------------------------------------------------------

}
