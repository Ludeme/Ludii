package app.utils;

import app.PlayerApp;
import compiler.Compiler;
import game.Game;
import manager.Referee;
import other.context.Context;
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
		
		app.settingsPlayer().updateRecentGames(app, app.manager().ref().context().game().name());
		app.resetUIVariables();
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
			app.resetUIVariables();		
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
	
}
