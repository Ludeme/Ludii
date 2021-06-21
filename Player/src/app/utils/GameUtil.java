package app.utils;

import java.awt.EventQueue;

import app.PlayerApp;
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
	 * @param fromServer If this call to restart the game came from the Ludii server.
	 */
	public static void restartGame(final PlayerApp app, final boolean fromServer)
	{
		final Referee ref = app.manager().ref();
		final Game game = ref.context().game();
		
		// If game has stochastic equipment, need to recompile the whole game from scratch.
		if (game.equipmentWithStochastic())
		{
			GameSetup.compileAndShowGame(app, game.description().raw(), true, game.description().filePath(), false);
			return;
		}
		
		// Reset game variables specific to the Player instance.
		app.resetGameVariables(true);
		
		// Setup the context
		final Context context = new Context(game, new Trial(game));
		ref.setContext(context);

		// If receiving this from a host, then use the provided RNG value.
		if (!fromServer)
			app.manager().updateCurrentGameRngInternalState();
		else
			context.rng().restoreState(app.manager().currGameStartRngState());

		// Start the game
		game.start(context);
		
		final int numPlayers = game.players().count();
		for (int p = 1; p < app.manager().aiSelected().length; ++p)
		{
			// Close AI players that may have had data from previous game
			if (app.manager().aiSelected()[p].ai() != null)
				app.manager().aiSelected()[p].ai().closeAI();
			
			// Initialise AI players (only if player ID relevant)
			if (p <= numPlayers && app.manager().aiSelected()[p].ai() != null)
				app.manager().aiSelected()[p].ai().initIfNeeded(game, p);
		}
	
		// Reset UI variables.
		app.resetUIVariables();
		
		app.addTextToStatusPanel("-------------------------------------------------\n");
		app.addTextToStatusPanel("Game Restarted.\n");
		
		EventQueue.invokeLater(() -> 
		{
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
		context.game().start(context);
		context.trial().setStatus(null);

		EventQueue.invokeLater(() -> 
		{
			app.resetUIVariables();
		});
	}

	//-------------------------------------------------------------------------
	
	/**
	 * various tasks that are performed when a normal game ends (not due to
	 * random playout or saved moves)
	 */
	public static void gameOverTasks(final PlayerApp app)
	{
		// check if match
		if (app.manager().ref().context().isAMatch())
		{
			if (!app.manager().ref().context().trial().over())
			{
				EventQueue.invokeLater(() -> 
				{
					MVCSetup.setMVC(app);
					GameSetup.cleanUpAfterLoading(app, app.manager().ref().context().currentInstanceContext().game(), false);
					app.updateFrameTitle();
				});
				
				return;
			}
		}
		
		if (app.manager().ref().context().trial().over())
		{
			app.manager().databaseFunctionsPublic().sendResultToDatabase(app.manager(), app.manager().ref().context());
			TournamentUtil.saveTournamentResults(app.manager(), app.manager().ref().context());
			app.setTemporaryMessage("Choose Game > Restart to play again.");
		}
	}
	
	//-------------------------------------------------------------------------
	
}
