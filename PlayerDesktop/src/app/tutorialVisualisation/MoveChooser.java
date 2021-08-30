package app.tutorialVisualisation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import manager.Manager;
import manager.Referee;
import other.AI;
import other.GameLoader;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;
import utils.RandomAI;

/**
 * -
 * 
 * @author matthew.stephenson
 */
public class MoveChooser
{
	// Variables that should be shared across methods
	private String gameName = null;
	private GameHistory gh = null;
	private final ArrayList<int[]> c_p1 = new ArrayList<int[]>();
	private final ArrayList<ArrayList<Move>[]> m_p1 = new ArrayList<ArrayList<Move>[]>();
	private final ArrayList<int[]> c_p2 = new ArrayList<int[]>();
	private final ArrayList<ArrayList<Move>[]> m_p2 = new ArrayList<ArrayList<Move>[]>();
	private int foundMove = 0;

	public MoveChooser(final String gameName) 
	{
		this.gameName = gameName;
	}

	/** Trial PLayout **/
	// Generate a full game for a number of games
	public boolean getMoves(final int trialAmount) 
	{
		return this.getMoves(trialAmount, -1);
	}
	
	// Generate a number of moves for a number of games
	public boolean getMoves(final int trialAmount, final int moveAmount) 
	{
		// Load the necessary classes
		final Game game = GameLoader.loadGameFromName(gameName);

		Trial trial = new Trial(game);
		Context context = new Context(game, trial);

		// Save all games in a list
		final List<List<Move>> gameList = new ArrayList<List<Move>>();

		// Do playouts for the given amount
		for(int i = 0; i < trialAmount; i++)
		{
			trial = new Trial(game);
			context = new Context(game, trial);

			game.start(context);

			final List<AI> ai_players = new ArrayList<AI>();

			final AI ai1 = new RandomAI();
			final AI ai2 = new RandomAI();

			ai1.initAI(game, 0);
			ai2.initAI(game, 1);

			ai_players.add(null);
			ai_players.add(ai1);
			ai_players.add(ai2);

			final Trial t = game.playout(context, ai_players, 1.0, null, 0, moveAmount, ThreadLocalRandom.current());
			
			final List<Move> move_list = t.generateCompleteMovesList();
			gameList.add(move_list);
		}

		// Now that we have the opening moves for all the games, we're going to generalise them
		final List<List<String>> gameGML_P1 = new ArrayList<List<String>>();
		final List<List<Move>> moves_P1 = new ArrayList<List<Move>>();
		final List<List<String>> gameGML_P2 = new ArrayList<List<String>>();
		final List<List<Move>> moves_P2 = new ArrayList<List<Move>>();

		for (final List<Move> g : gameList) 
		{
			String gameString1 = "";
			final List<Move> p1m = new ArrayList<Move>();
			String gameString2 = "";
			final List<Move> p2m = new ArrayList<Move>();

			for (final Move move : g) 
			{
				if (move.mover() == 1) 
				{
					if (move.actions().size() > 1) 
					{
						gameString1 += "Extra " + move.toTurnFormat(context, true) + ": " + move.what() + "\n";
						p1m.add(move);
					} 
					else 
					{
						gameString1 += "Move " + move.toTurnFormat(context, true) + ": " + move.what() + "\n";
						p1m.add(move);
					}
				}

				if (move.mover() == 2) 
				{
					if (move.actions().size() > 1) 
					{
						gameString2 += "Extra " + move.toTurnFormat(context, true) + ": " + move.what() + "\n";
						p2m.add(move);
					} 
					else 
					{
						gameString2 += "Move " + move.toTurnFormat(context, true) + ": " + move.what() + "\n";
						p2m.add(move);
					}
				}
			}
			
			final List<String> gml1 = MoveListParser.toGeneralizedMoveList(gameString1);
			final List<String> gml2 = MoveListParser.toGeneralizedMoveList(gameString2);

			gameGML_P1.add(gml1);
			moves_P1.add(p1m);
			gameGML_P2.add(gml2);
			moves_P2.add(p2m);
		}

		// Save it as a GameHistory object
		gh = new GameHistory(gameGML_P1, gameGML_P2, moves_P1, moves_P2);
		return true;
	}

	/** Move counting **/
	@SuppressWarnings("unchecked")
	public ArrayList<int[]> countMoves(final int player, final int numStartOriginal) 
	{
		final int[] orient = new int[3];
		final int[] type = new int[3];
		final int[] directions = new int[8];
		final int[] captures = new int[3];

		final ArrayList<Move>[] orient_moves = new ArrayList[3];
		final ArrayList<Move>[] type_moves = new ArrayList[3];
		final ArrayList<Move>[] direction_moves = new ArrayList[8];
		final ArrayList<Move>[] captures_moves = new ArrayList[3];

		for (int n = 0; n < 8; n++) 
		{
			if (n < 3) 
			{
				orient_moves[n] = new ArrayList<Move>();
				type_moves[n] = new ArrayList<Move>();
				captures_moves[n] = new ArrayList<Move>();
			}
			direction_moves[n] = new ArrayList<Move>();
		}

		// If the getMoves has not yet been run
		if (gh == null) 
		{
			System.out.println("No moves generated");
			return null;
		}

		// Some setup for counting forwards, backwards, or everything
		int numStart = numStartOriginal;
		boolean reverse = false;
		if (numStart == 0) 
		{
			numStart = Integer.MAX_VALUE;
		} 
		else if (numStart < 0) 
		{
			reverse = true;
			numStart *= -1;
		}

		final List<List<String>> playerGames = gh.get_games(player);
		final List<List<Move>> playerMoves = gh.get_moves(player);

		// Iterate over the amount of games
		for (int j = 0; j < playerGames.size(); j++)
		{
			final List<String> game = playerGames.get(j);
			final List<Move> m = playerMoves.get(j);

			if (reverse) 
			{ 
				Collections.reverse(game); 
				Collections.reverse(m);
			}

			// Iterate over the moves
			for (int k = 0; k < game.size(); k++) 
			{
				final String move = game.get(k);
				final Move original = m.get(k);

				// If the amount of moves to be counted is reached, we break
				if (k == numStart)
					break;

				// Orientation
				if (move.contains("vertical")) 
				{
					orient[0]++;
					orient_moves[0].add(original);
				} 
				else if (move.contains("horizontal")) 
				{
					orient[1]++;
					orient_moves[1].add(original);
				}
				else if (move.contains("diagonal")) 
				{
					orient[2]++;
					orient_moves[2].add(original);
				}

				// Move type
				if (move.contains("step")) 
				{
					type[0]++;
					type_moves[0].add(original);
				} 
				else if (move.contains("leap")) 
				{
					type[1]++;
					type_moves[1].add(original);
				}
				else if (move.contains("knight move")) 
				{
					type[2]++;
					type_moves[2].add(original);
				}

				// Move type
				if (move.contains("stomp")) 
				{
					captures[1]++;
					captures_moves[1].add(original);
				} 
				else if (move.contains("jumpover")) 
				{
					captures[2]++;
					captures_moves[2].add(original);
				}
				else 
				{
					captures[0]++;
					captures_moves[0].add(original);
				}

				// Direction
				String d = move.split(" ")[2];
				// Check if the final character is a comma, if so, remove it
				if (d != null && d.length() > 0 && d.charAt(d.length() - 1) == ',')
			        d = d.substring(0, d.length() - 1);
				
				switch (d) 
				{
				case "up": 
					directions[0]++;
					direction_moves[0].add(original);
					break;
				case "right-up": 
					directions[1]++;
					direction_moves[1].add(original);
					break;
				case "right": 
					directions[2]++;
					direction_moves[2].add(original);
					break;
				case "right-down": 
					directions[3]++;
					direction_moves[3].add(original);
					break;
				case "down": 
					directions[4]++;
					direction_moves[4].add(original);
					break;
				case "left-down": 
					directions[5]++;
					direction_moves[5].add(original);
					break;
				case "left": 
					directions[6]++;
					direction_moves[6].add(original);
					break;
				case "left-up": 
					directions[7]++;
					direction_moves[7].add(original);
					break;
				}
			}
		}

		if (player == 1) 
		{
			c_p1.add(orient);
			c_p1.add(type);
			c_p1.add(directions);
			c_p1.add(captures);

			m_p1.add(orient_moves);
			m_p1.add(type_moves);
			m_p1.add(direction_moves);
			m_p1.add(captures_moves);

			return c_p1;
		} 
		else if (player == 2) 
		{
			c_p2.add(orient);
			c_p2.add(type);
			c_p2.add(directions);
			c_p2.add(captures);

			m_p2.add(orient_moves);
			m_p2.add(type_moves);
			m_p2.add(direction_moves);
			m_p2.add(captures_moves);

			return c_p2;
		}

		return null;
	}

	/** Counting the amount of times a move occurs **/
	public Move getMostMoved(final int player) 
	{
		// This list will be filled with the moves that occurred most per 'category'
		final ArrayList<Move> mostMoves = new ArrayList<Move>();

		// Take the count and move list of the specified player
		ArrayList<int[]> counters = null;
		ArrayList<ArrayList<Move>[]> movers = null;
		if (player == 1) 
		{
			counters = c_p1;
			movers = m_p1;
		} 
		else if (player == 2) 
		{
			counters = c_p2;
			movers = m_p2;
		} 
		else 
		{
			return null;
		}

		// Iterate over the four categories
		for (int i = 0; i < 4; i++) 
		{
			final int[] c = counters.get(i);
			int max = 0;
			int index = 0;

			for (int id = 0; id < c.length; id++) 
			{
				final int count = c[id];
				if (count > max) 
				{
					max = count;
					index = id;
				}
			}
			mostMoves.addAll(movers.get(i)[index]);
		} // Now we have a list of the moves that occurred most

		// We will count how often a specific move occurred and map that
		final Map<Move, Integer> countMap = new HashMap<Move, Integer>();
		for(final Move m : mostMoves) 
		{
			if (countMap.containsKey(m)) 
			{
				int count = countMap.get(m).intValue();
				count++;
				countMap.put(m, Integer.valueOf(count));
			} 
			else 
			{
				countMap.put(m, Integer.valueOf(1));
			}
		}

		Map.Entry<Move, Integer> maxEntry = null;

		for (final Map.Entry<Move, Integer> entry : countMap.entrySet()) 
			if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) 
				maxEntry = entry;

		return maxEntry.getKey();
	}


	public Map<String, Move> getMoveType(final int player, final String type, final int piece) 
	{
		// Take the move list of the specified player
		ArrayList<ArrayList<Move>[]> movers = null;
		if (player == 1)
			movers = m_p1;
		else if (player == 2)
			movers = m_p2;
		else
			return null;

		// Get the move list according to the move type
		ArrayList<Move> moveList = null;
		if (type.compareTo("Move") == 0)
			moveList = movers.get(3)[0];
		else if (type.compareTo("Stomp") == 0)
			moveList = movers.get(3)[1];
		else if (type.compareTo("Jumpover") == 0)
			moveList = movers.get(3)[2];

		if (moveList == null || moveList.size() == 0)
			System.out.println("No moves for this type");

		final String[] dirs = {"U", "RU", "R", "RD", "D", "LD", "L", "LU"};
		final Map<String, Move> dirMap = new HashMap<String, Move>();
		for (int i = 0; i < movers.get(2).length; i++) 
		{
			final ArrayList<Move> direction = movers.get(2)[i];
			if (direction.size() != 0) 
			{
				boolean match = false;
				for(final Move dirmove : direction) 
				{
					if (match) 
						break;
					
					for(final Move typeMove : moveList)
						if (dirmove.from() == typeMove.from() && dirmove.to() == typeMove.to())
							if (dirmove.what() == piece || piece == -1) 
							{
								dirMap.put(dirs[i], typeMove);
								match = true;
								break;
							}
				}
			}
		}
		return dirMap;
	}

	/** Do playouts until the given move is played **/
	public File findTrial(final Manager manager, final Move move, final boolean begin) 
	{
		// Load the necessary classes
		final Game game = GameLoader.loadGameFromName(gameName);

		Trial trial = new Trial(game);
		Context context = new Context(game, trial);

		boolean trialFound = false;

		Trial t = null;

		int foundMoveNum = 0;
		while(!trialFound) 
		{
			trial = new Trial(game);
			context = new Context(game, trial);

			game.start(context);

			final List<AI> ai_players = new ArrayList<AI>();

			final AI ai1 = new RandomAI();
			final AI ai2 = new RandomAI();

			ai1.initAI(game, 0);
			ai2.initAI(game, 1);

			ai_players.add(null);
			ai_players.add(ai1);
			ai_players.add(ai2);

			int moveAmount = -1;
			if (begin) 
				moveAmount = 1;

			t = game.playout(context, ai_players, 1.0, null, 0, moveAmount, ThreadLocalRandom.current());

			int moveCount = 0;
			for (final Move tmp : t.generateCompleteMovesList()) 
			{
				if (tmp.mover() != 0) 
				{
					if (tmp.from() == move.from() && tmp.to() == move.to()) 
					{
						trialFound = true;
						foundMoveNum = moveCount;
						break;
					}
				}
				moveCount++;
			}

		}
		System.out.println("Found move at " + foundMoveNum);
		foundMove = foundMoveNum;
		return makeTrialFile(manager, t);
	}


	private File makeTrialFile(final Manager manager, final Trial trial) 
	{
		if (trial == null)
			System.out.println("No trial found...");

		// Save the trial to a file for later use
		final int trialNum = (int) Math.floor(Math.random()*100);
		final String trialPath = "tutorialVisualisation/trials/test-" + trialNum + ".trl";
		final File trialFile = new File(trialPath);
		try 
		{
			final Referee ref = manager.ref();
			List<String> gameOptionStrings = new ArrayList<>();
			if (ref != null) 
			{		
				if (ref.context().game().description().gameOptions() != null) 
				{
					gameOptionStrings = ref.context().game().description().gameOptions().allOptionStrings
										(
												manager.settingsManager().userSelections().selectedOptionStrings()
										);
				}
				trial.saveTrialToTextFile(trialFile, gameName, gameOptionStrings, manager.currGameStartRngState());
			} 
		} 
		catch(final IOException e) 
		{
			e.printStackTrace();
		}
		return trialFile;
	}

	public int getFoundMoveNum() 
	{
		return foundMove;
	}

	/** Console printing of moves from getMoves **/
	public void printMoves() 
	{
		if (gh != null) 
		{
			System.out.println("-- Player 1");
			int c1 = 0;
			for (final List<String> game: gh.get_games(1)) 
			{
				System.out.println("- Game " + c1);
				for (final String move : game) 
					System.out.println(move);
				
				c1++;
			}
			System.out.println("-- Player 2");
			int c2 = 0;
			for (final List<String> game: gh.get_games(2)) 
			{
				System.out.println("- Game " + c2);
				for (final String move : game) 
					System.out.println(move);
				
				c2++;
			}
		}
	}
}
