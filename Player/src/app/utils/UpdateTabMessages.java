package app.utils;

import app.PlayerApp;
import game.Game;
import game.equipment.component.Component;
import game.functions.booleans.is.component.IsThreatened;
import game.functions.ints.IntConstant;
import other.action.Action;
import other.context.Context;
import other.move.Move;
import other.topology.TopologyElement;
import other.trial.Trial;

public class UpdateTabMessages 
{

	public static void postMoveUpdateStatusTab(final PlayerApp app)
	{
		final Context context = app.manager().ref().context();
		final Trial trial = context.trial();
		final Game game = context.game();
		
		final int moveNumber = context.trial().numMoves()-1;
		final Move lastMove = (moveNumber >= 0) ? trial.getMove(moveNumber) : null;
		
		int nextMover = context.state().mover();
		if (trial.numMoves() > moveNumber + 1)
			nextMover = trial.getMove(moveNumber + 1).mover();
		
		String statusString = "";
		
		// Display check message
		if (!game.isDeductionPuzzle())
		{
			final int indexMover = context.state().mover();
			for (final TopologyElement element : context.board().topology().getAllGraphElements())
			{
				final int indexPiece = context.containerState(0).what(element.index(), element.elementType());
				if (indexPiece != 0)
				{
					final Component component = context.components()[indexPiece];
					if (game.metadata().graphics().checkUsed(context, indexMover, component.name()))
					{
						boolean check = false;
						final IsThreatened threat = new IsThreatened(new IntConstant(indexPiece), element.elementType(),
								new IntConstant(element.index()), null, null);
						threat.preprocess(context.game());
						check = threat.eval(context);
						
						if (check)
							app.setTemporaryMessage("Check.");
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
		
		if (lastMove != null && lastMove.isSwap())
			app.setTemporaryMessage("Player " + lastMove.mover() + " made a swap move.");

		// Check if any player has just lost or won
		for (int i = 1; i <= game.players().count(); i++)
		{
			// Network
			if (!(context.active(i)) && app.manager().settingsNetwork().activePlayers()[i])
			{					
				app.manager().settingsNetwork().activePlayers()[i] = false;

				if (app.manager().settingsNetwork().getActiveGameId() != 0)
				{
					final double[] tempRanking = new double[trial.ranking().length];
					for (int j = 0; j < trial.ranking().length; j++)
						tempRanking[j] = trial.ranking()[j];

					for (int player = 1; player < trial.ranking().length; player++)
						if (trial.ranking()[player] == 0.0)
							tempRanking[player] = 1000;

					app.manager().databaseFunctionsPublic().sendGameRankings(app.manager(), tempRanking);
				}
			}
			
			// Local
			if (!context.trial().over() && !context.active(i) && app.contextSnapshot().getContext(app).active(i))
			{
				if (context.computeNextDrawRank() > trial.ranking()[i])
					statusString += context.getPlayerName(i) + " has achieved a win.\n";
				else if (context.computeNextDrawRank() < context.trial().ranking()[i])
					statusString += context.getPlayerName(i) + " has suffered a loss.\n";	
				else
					statusString += context.getPlayerName(i) + " has been given a draw.\n";	
			}
		}

		// Show next player to move
		if (!context.trial().over() && nextMover < game.players().size())
			statusString += app.manager().aiSelected()[app.manager().playerToAgent(nextMover)].name() + " to move.\n";
		
		app.addTextToStatusPanel(statusString);
	}
	
	//-----------------------------------------------------------------------------
	
	public static String gameOverMessage(final Context context, final Trial trial)
	{
		final Game game = context.game();
		
		// This move wins the game
		final int winner = trial.status().winner();
		
		final int nbPlayers = context.game().players().count();

		String str = "";
		if (winner == 0)// DRAW
		{
			final double[] ranks = trial.ranking();
			boolean allWin = true;
			for (int i = 1; i < ranks.length; i++)
				if (ranks[i] != 1.0)
					allWin = false;

			if (nbPlayers == 1 && allWin)
				str += "Congratulations, puzzle solved!\n";
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
					final double rank = trial.ranking()[j];

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
		
		return str;
	}
	
	//-----------------------------------------------------------------------------
	
}
