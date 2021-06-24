package app.utils;

import app.PlayerApp;
import app.move.MoveHandler;
import app.move.animation.MoveAnimation;
import compiler.Compiler;
import game.Game;
import main.Constants;
import manager.Referee;
import manager.ai.AIUtil;
import other.context.Context;
import other.location.FullLocation;
import other.trial.Trial;
import tournament.TournamentUtil;

/**
 * Utility functions for games.
 * 
 * @author Matthew.Stephenson
 */
public class GameUtil
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * All function calls needed to restart the game.
	 */
	public static void resetGame(final PlayerApp app, final boolean keepSameTrial)
	{
		final Referee ref = app.manager().ref();
		final Context context = ref.context();
		Game game = context.game();
		
		// If game has stochastic equipment, need to recompile the whole game from scratch.
		if (game.equipmentWithStochastic())
		{
			game = (Game)Compiler.compile(game.description(), app.manager().settingsManager().userSelections(), null, false);		
			app.manager().ref().setGame(app.manager(), game);
		}
		
		if (keepSameTrial)
		{
			// Reset all necessary information about the context.
			context.rng().restoreState(app.manager().currGameStartRngState());
			context.reset();
			context.state().initialise(context.currentInstanceContext().game());
			context.trial().setStatus(null);
		}
		else
		{
			app.manager().setSavedTrial(null);
			ref.setContext(new Context(game, new Trial(game)));
			app.manager().ref().setGame(app.manager(), game);
			UpdateTabMessages.postMoveUpdateStatusTab(app);
		}
		
		// Start the game
		GameUtil.startGame(app);

		app.loadGameSpecificPreferences();
		
		updateRecentGames(app, app.manager().ref().context().game().name());
		resetUIVariables(app);
	}

	public static void resetUIVariables(final PlayerApp app)
	{
		app.contextSnapshot().setContext(app);
		MVCSetup.setMVC(app);
		
		app.bridge().setGraphicsRenderer(app);

		app.manager().ref().interruptAI(app.manager());
		
		//app.view().createPanels();
		
		app.bridge().settingsVC().setSelectedFromLocation(new FullLocation(Constants.UNDEFINED));
		app.bridge().settingsVC().setSelectingConsequenceMove(false);
		app.settingsPlayer().setCurrentWalkExtra(0);
		MoveAnimation.resetAnimationValues(app);
		
		app.manager().settingsManager().movesAllowedWithRepetition().clear();
		app.manager().settingsManager().storedGameStatesForVisuals().clear();
		app.manager().settingsManager().storedGameStatesForVisuals().add(Long.valueOf(app.manager().ref().context().state().stateHash()));
		
		app.setTemporaryMessage("");
		
		app.manager().settingsNetwork().resetNetworkPlayers();
		
		app.updateFrameTitle();
		
		AIUtil.pauseAgentsIfNeeded(app.manager());

		MoveHandler.checkMoveWarnings(app);
		app.repaint();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * various tasks that are performed when a normal game ends.
	 */
	public static void gameOverTasks(final PlayerApp app)
	{
		final Context context = app.manager().ref().context();
		final int moveNumber = context.currentInstanceContext().trial().numMoves() - 1;
		
		if (context.trial().over())
		{
			UpdateTabMessages.updateStatusTabGameOver(app);
			app.manager().databaseFunctionsPublic().sendResultToDatabase(app.manager(), context);
			TournamentUtil.saveTournamentResults(app.manager(), app.manager().ref().context());
			app.setTemporaryMessage("Choose Game > Restart to play again.");
		}
		else if (context.isAMatch() && moveNumber < context.currentInstanceContext().trial().numInitialPlacementMoves())
		{
			resetUIVariables(app);		
		}
	}
	
	//-------------------------------------------------------------------------
	
	public static void startGame(final PlayerApp app)
	{
		final Context context = app.manager().ref().context();
		context.game().start(context);
		
		final int numPlayers = context.game().players().count();
		for (int p = 1; p < app.manager().aiSelected().length; ++p)
		{
			// Close AI players that may have had data from previous game
			if (app.manager().aiSelected()[p].ai() != null)
				app.manager().aiSelected()[p].ai().closeAI();
			
			// Initialise AI players (only if player ID relevant)
			if (p <= numPlayers && app.manager().aiSelected()[p].ai() != null)
				app.manager().aiSelected()[p].ai().initIfNeeded(context.game(), p);
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Add gameName to the list of recent games, or update its position in this list.
	 * These games can then be selected from the menu bar
	 * @param gameName
	 */
	private static void updateRecentGames(final PlayerApp app, final String gameName)
	{
		String GameMenuName = gameName;
		final String[] recentGames = app.settingsPlayer().recentGames();
		
		if (!app.settingsPlayer().loadedFromMemory())
			GameMenuName = app.manager().savedLudName();
		
		int gameAlreadyIncluded = -1;
		
		// Check if the game is already included in our recent games list, and record its position.
		for (int i = 0; i < recentGames.length; i++)
			if (recentGames[i] != null && recentGames[i].equals(GameMenuName))
				gameAlreadyIncluded = i;

		// If game was not already in recent games list, record the last position on the list
		if (gameAlreadyIncluded == -1)
			gameAlreadyIncluded = recentGames.length-1;

		// Shift all games ahead of the recored position down a spot.
		for (int i = gameAlreadyIncluded; i > 0; i--)
			recentGames[i] = recentGames[i-1];
		
		// Add game at front of recent games list.
		recentGames[0] = GameMenuName;
	}
	
	//-------------------------------------------------------------------------
	
}
