package app.utils;

import java.awt.EventQueue;

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
	public static void restartGame(final PlayerApp app)
	{
		final Referee ref = app.manager().ref();
		Game game = ref.context().game();
		
		// If game has stochastic equipment, need to recompile the whole game from scratch.
		if (game.equipmentWithStochastic())
		{
			game = (Game)Compiler.compile(game.description(), app.manager().settingsManager().userSelections(), null, false);		
			app.manager().ref().setGame(app.manager(), game);
		}
		
		// Reset game variables specific to the Player instance.
		app.resetGameVariables();
		
		// Setup the context
		final Context context = new Context(game, new Trial(game));
		ref.setContext(context);
		
		// Start the game
		GameUtil.startGame(app);
		
		app.settingsPlayer().updateRecentGames(app, app.manager().ref().context().game().name());
		MVCSetup.setMVC(app);

		EventQueue.invokeLater(() -> 
		{
			app.addTextToStatusPanel("-------------------------------------------------\n");
			app.addTextToStatusPanel("Game Restarted.\n");
			app.updateTabs(context);
		});
	}

	//-------------------------------------------------------------------------

	/**
	 * Called when we need to perform a trial on the game from the starting state.
	 */
	public static void resetContext(final PlayerApp app)
	{
		final Context context = app.manager().ref().context();
		
		// Reset all necessary information about the context.
		context.rng().restoreState(app.manager().currGameStartRngState());
		context.reset();
		context.state().initialise(context.currentInstanceContext().game());
		startGame(app);
		context.trial().setStatus(null);

		EventQueue.invokeLater(() -> 
		{
			app.resetUIVariables();
		});
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
