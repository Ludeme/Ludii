package app.display.views.tabs.pages;

import java.awt.EventQueue;
import java.awt.Rectangle;

import app.DesktopApp;
import app.PlayerApp;
import app.display.views.tabs.TabPage;
import app.display.views.tabs.TabView;
import game.Game;
import game.equipment.component.Component;
import game.functions.booleans.is.component.IsThreatened;
import game.functions.ints.IntConstant;
import main.Constants;
import other.action.Action;
import other.context.Context;
import other.move.Move;
import other.topology.TopologyElement;
import other.trial.Trial;

/**
 * Tab for displaying all status messages (persistent between games).
 * 
 * @author Matthew.Stephenson
 */
public class StatusPage extends TabPage
{
	
	// Set of active players from the previous status update, to know if this changes.
	static boolean[] activePlayersPrevious = new boolean[Constants.MAX_PLAYERS];
	
	//-------------------------------------------------------------------------
	
	public StatusPage(final PlayerApp app, final Rectangle rect, final String title, final String text, final int pageIndex, final TabView parent)
	{
		super(app, rect, title, text, pageIndex, parent);
		
		// When tab created, assume all players are active.
		for (int i = 0; i < activePlayersPrevious.length; i++)
			activePlayersPrevious[i] = true;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public void updatePage(final Context context)
	{
		Trial longestTrial = context.trial();
		if (app.manager().savedTrial() != null)
			longestTrial = app.manager().savedTrial();
		
		final int moveNumber = context.trial().numMoves()-1;
		final Move lastMove = (moveNumber >= 0) ? longestTrial.getMove(moveNumber) : null;
		
		int nextMover = context.state().mover();
		if (longestTrial.numMoves() > moveNumber + 1)
			nextMover = longestTrial.getMove(moveNumber + 1).mover();

		String statusString = "";

		final int nbPlayers = context.game().players().count();
		final Game game = context.game();

		// The game is over
		if (longestTrial.over() && moveNumber == longestTrial.numMoves()-1)
		{
			// This move wins the game
			final int winner = longestTrial.status().winner();

			String str = "";
			if (winner == 0)// DRAW
			{
				final double[] ranks = longestTrial.ranking();
				boolean allLose = true;
				boolean allWin = true;
				for (int i = 1; i < ranks.length; i++)
				{
					if (ranks[i] != 1.0)
						allWin = false;
					else if (ranks[i] != game.players().count())
						allLose = false;
				}

				if (allWin)
					str += "Congratulations, puzzle solved!\n";
				else if (allLose)
					str += "Game Over, you lose!\n";
				else if (nbPlayers == 1)
					str += "Game Over, you lose!\n";
				else
					str += "Game won by no one" + ".\n";

				if (game.checkMaxTurns(context))
					str += "Maximum number of moves reached" + ".\n";

			}
			else if (winner == -1) // ABORT
				str += "Game aborted" + ".\n";

			else if (winner > game.players().count()) // TIE
				str += "Game won by everyone" + ".\n";

			else // WIN
			{
				if (nbPlayers == 1)
					str += "Congratulations, puzzle solved!\n";
				else
				{
					if (game.requiresTeams())
						str += "Game won by team " + context.state().getTeam(winner) + ".\n";
					else
						str += "Game won by " + context.getPlayerName(winner) + ".\n";
				}
			}

			if (game.players().count() >= 3) // Rankings
			{
				for (int i = 1; i <= game.players().count(); i++)
				{
					boolean anyPlayers = false;
					str += "Rank " + (i) + ": ";

					for (int j = 1; j <= game.players().count(); j++)
					{
						final double rank = longestTrial.ranking()[j];

						if (Math.floor(rank) == i)
						{
							if (!anyPlayers)
							{
								str += context.getPlayerName(j);
								anyPlayers = true;
							}
							else
							{
								str += ", " + context.getPlayerName(j);
							}
						}
					}

					if (!anyPlayers)
						str += "No one\n";
					else
						str += "\n";
				}
			}
			
			statusString += str;
		}
		
		// The game is not yet over.
		else if (game.players().count() > 1)
		{
			// Display check message
			final int indexMover = context.state().mover();
			for (final TopologyElement element : context.board().topology().getAllGraphElements())
			{
				final int indexPiece = context.containerState(0).what(element.index(), element.elementType());
				if (indexPiece != 0)
				{
					final Component component = context.components()[indexPiece];
					if (game.metadata().graphics().checkUsed(indexMover, component.name(), context))
					{
						boolean check = false;
						final IsThreatened threat = new IsThreatened(new IntConstant(indexPiece), element.elementType(),
								new IntConstant(element.index()), null, null);
						threat.preprocess(context.game());
						check = threat.eval(context);
						
						if (check)
						{
							EventQueue.invokeLater(() ->
							{
								DesktopApp.view().setTemporaryMessage("Check.");
							});
						}
					}
				}
			}

			// Display Note action message
			if (lastMove != null)
				for (final Action action : lastMove.actions())
					if (action.message() != null)
						if (action.who() == context.state().mover())
							statusString += "Note for Player " + action.who() + ": " + action.message() + ".\n";

			// Check if any player has just lost or won
			for (int i = 1; i <= game.players().count(); i++)
			{
				// Network
				if (!(context.active(i)) && app.manager().settingsNetwork().activePlayers()[i])
				{					
					app.manager().settingsNetwork().activePlayers()[i] = false;

					if (app.manager().settingsNetwork().getActiveGameId() != 0)
					{
						final double[] tempRanking = new double[longestTrial.ranking().length];
						for (int j = 0; j < longestTrial.ranking().length; j++)
							tempRanking[j] = longestTrial.ranking()[j];

						for (int player = 1; player < longestTrial.ranking().length; player++)
							if (longestTrial.ranking()[player] == 0.0)
								tempRanking[player] = 1000;

						app.manager().databaseFunctionsPublic().sendGameRankings(app.manager(), tempRanking);
					}
				}
				
				// Local
				if (!context.active(i) && activePlayersPrevious[i])
				{
					if (context.computeNextDrawRank() > longestTrial.ranking()[i])
						statusString += context.getPlayerName(i) + " has achieved a win.\n";
					else if (context.computeNextDrawRank() < context.trial().ranking()[i])
						statusString += context.getPlayerName(i) + " has sufferred a loss.\n";	
					else
						statusString += context.getPlayerName(i) + " has been given a draw.\n";	
				}
			}

			// Show next player to move
			if (nextMover < game.players().size())
			{
				final String str = app.manager().aiSelected()[context.state().playerToAgent(nextMover)].name() + " to move.\n";
				statusString += str;
			}
		}
		
		// Keep a constant record of the active players, each time the status tab is updated.
		for (int i = 0; i < activePlayersPrevious.length; i++)
			activePlayersPrevious[i] = context.active(i);

		addText(statusString);
		app.settingsPlayer().setSavedStatusTabString(text());
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void reset()
	{
		clear();
		addText(app.manager().aiSelected()[1].name() + " to move.\n");
	}
	
	//-------------------------------------------------------------------------

}
