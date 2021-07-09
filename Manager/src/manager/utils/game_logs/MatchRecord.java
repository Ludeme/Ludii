package manager.utils.game_logs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.rng.core.RandomProviderDefaultState;

import game.Game;
import gnu.trove.list.array.TIntArrayList;
import main.Status;
import main.Status.EndType;
import other.context.Context;
import other.move.Move;
import other.state.State;
import other.trial.Trial;

/**
 * A record of a single played Match (to be serialised/deserialised, typically as a collection
 * in GameLogs)
 * 
 * @author Dennis Soemers
 */
public class MatchRecord implements Serializable 
{
	/** */
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------
	
	/** */
	private final Trial trial;
	
	/** */
	private final RandomProviderDefaultState rngState;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param trial
	 * @param rngState
	 * @param loadedGameName
	 */
	public MatchRecord
	(
		final Trial trial, 
		final RandomProviderDefaultState rngState, 
		final String loadedGameName
	)
	{
		this.trial = trial;
		this.rngState = rngState;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Reference to the current trial.
	 */
	public Trial trial()
	{
		return trial;
	}
	
	/**
	 * @return RNG state.
	 */
	public RandomProviderDefaultState rngState()
	{
		return rngState;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Loads a MatchRecord from a text file
	 * @param file
	 * @param game
	 * @return The match record.
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static MatchRecord loadMatchRecordFromTextFile
	(
		final File file,
		final Game game
	) throws FileNotFoundException, IOException
	{
		try (final InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "UTF-8"))
		{
			return loadMatchRecordFromInputStream(reader, game);
		}
	}
	
	/**
	 * Loads a MatchRecord from a text file
	 * @param inputStreamReader
	 * @param game
	 * @return The match record.
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static MatchRecord loadMatchRecordFromInputStream
	(
		final InputStreamReader inputStreamReader,
		final Game game
	) throws FileNotFoundException, IOException
	{
		try (final BufferedReader reader = new BufferedReader(inputStreamReader))
		{
			final String gameNameLine = reader.readLine();
			final String loadedGameName = gameNameLine.substring("game=".length());
			
			String nextLine = reader.readLine();
			
			while (true)
			{
				if (nextLine == null)
					break;
				
				if (nextLine.startsWith("RNG internal state="))
					break;
				
				if (nextLine.startsWith("NEW LEGAL MOVES LIST"))
					break;
				
				if (nextLine.startsWith("winner="))
					break;
				
				if (nextLine.startsWith("rankings="))
					break;
				
				//if (!nextLine.startsWith("START GAME OPTIONS") && !nextLine.startsWith("END GAME OPTIONS"))
					// Do nothing
				
				nextLine = reader.readLine();
			}
			
			if (!nextLine.startsWith("RNG internal state="))
			{
				System.err.println("ERROR: MatchRecord::loadMatchRecordFromTextFile expected to read RNG internal state!");
				return null;
			}
			
			final String rngInternalStateLine = nextLine;
			final String[] byteStrings = rngInternalStateLine.substring("RNG internal state=".length()).split(Pattern.quote(","));
			final byte[] bytes = new byte[byteStrings.length];
			
			for (int i = 0; i < byteStrings.length; ++i)
				bytes[i] = Byte.parseByte(byteStrings[i]);
			
			final RandomProviderDefaultState rngState = new RandomProviderDefaultState(bytes);
			
			final Trial trial = new Trial(game);
			
			// now we expect to be reading played Moves
			nextLine = reader.readLine();
			while (true)
			{
				if (nextLine == null)
					break;
				
				if (!nextLine.startsWith("Move="))
					break;
				
				final Move move = new Move(nextLine.substring("Move=".length()));
				trial.addMove(move);
				
				nextLine = reader.readLine();
			}
			
			// now we expect to be reading sizes of histories of legal moves lists (if they were stored)
			final TIntArrayList legalMovesHistorySizes = new TIntArrayList();
			
			while (true)
			{
				if (nextLine == null)
					break;
				
				if (nextLine.startsWith("NEW LEGAL MOVES LIST"))
					break;
				
				if (nextLine.startsWith("winner="))
					break;
				
				if (nextLine.startsWith("rankings="))
					break;
				
				if (nextLine.startsWith("numInitialPlacementMoves="))
					break;
				
				if (nextLine.startsWith("LEGAL MOVES LIST SIZE = "))
					legalMovesHistorySizes.add(Integer.parseInt(nextLine.substring("LEGAL MOVES LIST SIZE = ".length())));
				
				nextLine = reader.readLine();
			}
			
			// now we expect to be reading sequences of legal moves (if they were stored)
			final List<List<Move>> legalMovesHistory = new ArrayList<List<Move>>();
			
			while (true)
			{
				if (nextLine == null)
					break;
				
				if (nextLine.startsWith("winner="))
					break;
				
				if (nextLine.startsWith("rankings="))
					break;
				
				if (nextLine.startsWith("numInitialPlacementMoves="))
					break;
				
				if (nextLine.equals("NEW LEGAL MOVES LIST"))
					legalMovesHistory.add(new ArrayList<Move>());
				
				else if (!nextLine.equals("END LEGAL MOVES LIST"))
					legalMovesHistory.get(legalMovesHistory.size() - 1).add(new Move(nextLine));
				
				nextLine = reader.readLine();	
			}
			
			if (!legalMovesHistory.isEmpty())
			{
				trial.storeLegalMovesHistory();
				trial.setLegalMovesHistory(legalMovesHistory);
			}
			
			if (!legalMovesHistorySizes.isEmpty())
			{
				trial.storeLegalMovesHistorySizes();
				trial.setLegalMovesHistorySizes(legalMovesHistorySizes);
			}
			
			int winner = -99;	// Not 100% sure that any of the nice constants are good enough, maybe used for losers in single-player games or something?
			EndType endType = EndType.Unknown;
			int numInitialPlacementMoves = 0;
			
			if (nextLine != null && nextLine.startsWith("numInitialPlacementMoves="))
			{
				numInitialPlacementMoves = Integer.parseInt(nextLine.substring("numInitialPlacementMoves=".length()));
				nextLine = reader.readLine();
			}
			
			if (nextLine != null && nextLine.startsWith("winner="))
			{
				winner = Integer.parseInt(nextLine.substring("winner=".length()));
				nextLine = reader.readLine();
			}
			
			if (nextLine != null && nextLine.startsWith("endtype="))
			{
				endType = EndType.valueOf(nextLine.substring("endtype=".length()));
				nextLine = reader.readLine();
			}
			
			trial.setNumInitialPlacementMoves(numInitialPlacementMoves);
			
			if (winner > -99)
				trial.setStatus(new Status(winner, endType));
			
			if (nextLine != null && nextLine.startsWith("rankings="))
			{
				final String[] rankingStrings = nextLine.substring("rankings=".length()).split(Pattern.quote(","));
				for (int i = 0; i < rankingStrings.length; ++i)
				{
					trial.ranking()[i] = Double.parseDouble(rankingStrings[i]);
				}
			}
			
			return new MatchRecord(trial, rngState, loadedGameName);
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Tests integrity of this match record
	 * @param game
	 */
	public void testIntegrity(final Game game)
	{
		final Context context = new Context(game, new Trial(game));
		context.rng().restoreState(rngState);
		
		final List<State> stateHistory = trial.auxilTrialData().stateHistory();
		final List<Move> actionHistory = trial.generateCompleteMovesList();
		
		// let the game re-playing start in new context
		game.start(context);
		
		// re-play all actions, make sure all states are equal (if stored)
		int currActionIndex = 0;
		while (currActionIndex < actionHistory.size())
		{
			final State currentState = context.state();
			
			if (stateHistory != null)
			{
				final State historicState = stateHistory.get(currActionIndex);
				
				if (!historicState.equals(currentState))
				{
					System.err.println("State " + currActionIndex + 
							" in history not equal to state in re-played game!");
					return;
				}
			}
			
			if (context.trial().over())
			{
				System.err.println("Re-played game ended faster than game in history did!");
				return;
			}
			
			final Move actionToPlay = actionHistory.get(currActionIndex);
			
			if (!(game.moves(context).moves().contains(actionToPlay)))
			{
				System.err.println("Action to play according to history is not legal!");
				return;
			}
			
			game.apply(context, actionToPlay);
			++currActionIndex;
		}
		
		// now compare final states if states are saved...
		if (stateHistory != null)
		{
			if (!(stateHistory.get(stateHistory.size() - 1).equals(context.state())))
			{
				System.err.println("Last state of history not equal to state in re-play!");
				return;
			}
		}
		
		// and status at end of game
		if (!(trial.status().equals(context.trial().status())))
		{
			System.err.println("Final Status in history does not equal final Status in re-play!");
			return;
		}
		
		return;
	}
	
	//-------------------------------------------------------------------------

}
