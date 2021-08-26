package app.tutorialVisualisation;

import java.util.List;

import other.move.Move;

public class GameHistory
{
	private List<List<String>> player1 = null;
	private List<List<Move>> player1_moves = null;
	private List<List<String>> player2 = null;
	private List<List<Move>> player2_moves = null;

	public GameHistory(final List<List<String>> p1, 
			final List<List<String>> p2,
			final List<List<Move>> p1m,
			final List<List<Move>> p2m) {
		player1 = p1;
		player1_moves = p1m;
		player2 = p2;
		player2_moves = p2m;
	}

	// Get all games of specified player
	public List<List<String>> get_games(final int player) {
		if (player == 1) {
			return player1;
		} else if (player == 2) {
			return player2;
		} else {
			return null;
		}
	}

	// Get a specific game of a specific player
	public List<String> getGame(final int player, final int game) {
		if (player == 1) {
			return player1.get(game);
		} else if (player == 2) {
			return player2.get(game);
		} else {
			return null;
		}
	}

	// Get all moves of specified player
	public List<List<Move>> get_moves(final int player) {
		if (player == 1) {
			return player1_moves;
		} else if (player == 2) {
			return player2_moves;
		} else {
			return null;
		}
	}

	// Get specific list of moves for specific player
	public List<Move> getGameMove(final int player, final int game) {
		if (player == 1) {
			return player1_moves.get(game);
		} else if (player == 2) {
			return player2_moves.get(game);
		} else {
			return null;
		}
	}

}
