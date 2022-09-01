package other.trial;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.rng.RandomProviderState;
import org.apache.commons.rng.core.RandomProviderDefaultState;

import game.Game;
import game.rules.meta.no.repeat.NoRepeat;
import game.rules.meta.no.simple.NoSuicide;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import main.Constants;
import main.Status;
import main.collections.FastArrayList;
import main.collections.FastTLongArrayList;
import other.UndoData;
import other.context.Context;
import other.context.TempContext;
import other.move.Move;
import other.move.MoveSequence;
import other.state.State;

/**
 * Instance of a played game, consisting of states and turns that led to them.
 *
 * @author cambolbro and Dennis Soemers and Eric Piette
 */
public class Trial implements Serializable
{
	private static final long serialVersionUID = 1L;

	//-----------------------------------------------------------------------------

	/** Moves made during the Trial (one less than number of states encountered). */
	private MoveSequence moves;

	/** Number of initial placement moves before player decision moves. */
	private int numInitialPlacementMoves = 0;

	/** The starting positions of each component. */
	private List<Region> startingPos;

	/** Result of game (null if game is still in progress). */
	protected Status status = null;

	/** Legal moves for next player. Typically generated the move before and cached. */
	protected Moves legalMoves;

	/** All the previous states of the game. TODO make this a TLongHashSet? */
	private FastTLongArrayList previousStates;

	/** All the previous state within a turn. TODO make this a TLongHashSet? */
	private FastTLongArrayList previousStatesWithinATurn;

	/**
	 * Number of sub-moves played in this trial. Only valid for simultaneous-move
	 * games. Only Used by the networking
	 */
	private int numSubmovesPlayed = 0;
	
	/** Ranking of the players. */
	private double[] ranking;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Auxiliary trial data
	 *
	 * NOTE: Currently we do not copy this flag when copying Trials using the copy
	 * constructor. We assume this flag is only used when we want to store logs of
	 * played games into files. If, in the future, we ever get any game rules that
	 * depend on this flag, we'll also want to make sure to copy it in the copy
	 * constructor (because it will become important for copied Trials in MCTS
	 * simulations for example)
	 */
	protected transient AuxilTrialData auxilTrialData = null;

	//------------------------------Data used to undo--------------------------------
	
	/**
	 * The list of all the end data in each previous state from the initial state.
	 */
	private List<UndoData> endData = null;
	
	/**
	 * The list of all the RNG states at each state.
	 */
	private List<RandomProviderState> RNGStates = null;

	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 *
	 * @param game The game.
	 */
	public Trial(final Game game)
	{
		if (game.hasSubgames())
		{
			// This is a Trial for a Match; we need almost no data here
			startingPos = null;
			legalMoves = null;
			previousStates = null;
			previousStatesWithinATurn = null;
		}
		else
		{
			// This is a Trial for just a single Game
			startingPos = new ArrayList<Region>();
			legalMoves = new BaseMoves(null);
			previousStates = new FastTLongArrayList();
			previousStatesWithinATurn = new FastTLongArrayList();
		}
		
		moves = new MoveSequence(null);
		ranking = new double[game.players().count() + 1];
		endData = new ArrayList<UndoData>();
		RNGStates = new ArrayList<RandomProviderState>();
	}

	/**
	 * Copy constructor.
	 *
	 * @param other The trial to copy.
	 */
	public Trial(final Trial other)
	{
		moves = copyMoveSequence(other.moves);
		numInitialPlacementMoves = other.numInitialPlacementMoves;
		startingPos = other.startingPos == null ? null : new ArrayList<Region>(other.startingPos);
		
		// NOTE: multiple Trials sharing same Status here. Usually fine
		// because null if not terminal
		status = other.status();
		
		// Just copying reference here intentional!
		legalMoves = other.legalMoves;
		
		if (other.previousStates != null)
			previousStates = new FastTLongArrayList(other.previousStates);
		
		if (other.previousStatesWithinATurn != null)
			previousStatesWithinATurn = new FastTLongArrayList(other.previousStatesWithinATurn);
		
		numSubmovesPlayed = other.numSubmovesPlayed;
		
		ranking = Arrays.copyOf(other.ranking, other.ranking.length);
		
		if (other.endData != null)
		{
			endData = new ArrayList<UndoData>(other.endData);
			RNGStates = new ArrayList<RandomProviderState>(other.RNGStates);
		}
	}
	
	/**
	 * Method used to set the trial to another trial.
	 * @param trial The trial to reset to.
	 */
	public void resetToTrial(final Trial trial)
	{
		moves = copyMoveSequence(trial.moves);
		numInitialPlacementMoves = trial.numInitialPlacementMoves;
		startingPos = trial.startingPos == null ? null : new ArrayList<Region>(trial.startingPos);
		
		// NOTE: multiple Trials sharing same Status here. Usually fine
		// because null if not terminal
		status = trial.status();
		
		// Just copying reference here intentional!
		legalMoves = trial.legalMoves;
		
		if (trial.previousStates != null)
			previousStates = new FastTLongArrayList(trial.previousStates);
		
		if (trial.previousStatesWithinATurn != null)
			previousStatesWithinATurn = new FastTLongArrayList(trial.previousStatesWithinATurn);
		
		numSubmovesPlayed = trial.numSubmovesPlayed;
		
		ranking = Arrays.copyOf(trial.ranking, trial.ranking.length);
		endData = new ArrayList<UndoData>(trial.endData);
		RNGStates = new ArrayList<RandomProviderState>(trial.RNGStates);
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Method for copying MoveSequence. NOTE: we override this in TempTrial
	 * for copying MoveSequences that are allowed to be invalidated.
	 * 
	 * @param otherState
	 * @return Copy of given game state.
	 */
	@SuppressWarnings("static-method")
	protected MoveSequence copyMoveSequence(final MoveSequence otherSequence)
	{
		return new MoveSequence(otherSequence);
	}

	/**
	 * @return Auxiliary trial data. Will often be null (only not null if we
	 *         explicitly require Trial to keep track of some auxiliary data).
	 */
	public AuxilTrialData auxilTrialData()
	{
		return auxilTrialData;
	}

	/**
	 * @param move Action to be added to the history of played actions.
	 */
	public void addMove(final Move move)
	{
		moves = moves.add(move);
	}
	
	/**
	 * To remove the last action from the history of played actions.
	 * @return The action removed.
	 */
	public Move removeLastMove()
	{
		return moves.removeLastMove();
	}
	
	/**
	 * @param idx
	 * @return Move at given index
	 */
	public Move getMove(final int idx)
	{
		return moves.getMove(idx);
	}
	
	/**
	 * Replaces the last move in the sequence with the given new move
	 * @param move
	 */
	public void replaceLastMove(final Move move)
	{
		moves.replaceLastMove(move);
	}

	/**
	 * @return Result of game (null if game is still in progress).
	 */
	public Status status()
	{
		return status;
	}

	/**
	 * @param res
	 */
	public void setStatus(final Status res)
	{
		status = res;
	}

	/**
	 * @return Cached legal moves
	 */
	public Moves cachedLegalMoves()
	{
		if (over())
			return new BaseMoves(null);
		
		return legalMoves;
	}

	/**
	 * Sets the cached legal moves
	 * @param legalMoves
	 * @param context
	 */
	public void setLegalMoves(final Moves legalMoves, final Context context)
	{		
		// Remove moves that don't satisfy meta rules
		for (int i = 0; i < legalMoves.moves().size(); i++)
		{
			final Move m = legalMoves.moves().get(i);
			
			if (!NoRepeat.apply(context, m) || !NoSuicide.apply(context, m))
				legalMoves.moves().removeSwap(i--);
		}
		
		if (!over())
		{
			// Ensure that we have at least one legal move
			if (context.game().isAlternatingMoveGame())
			{
				if (legalMoves.moves().isEmpty())
					legalMoves.moves().add(Game.createPassMove(context,true));
			}
			
			// NOTE: case for simultaneous-move games not handled here, but already
			// prior to state repetition checks in Game.moves(). State repetitions
			// in simultaneous-move games are a mess...
		}

		this.legalMoves = legalMoves;

		if (auxilTrialData != null)
			auxilTrialData.updateNewLegalMoves(legalMoves, context);
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Whether game is over.
	 */
	public boolean over()
	{
		return status != null;
	}

	/**
	 * @return Number of setup moves
	 */
	public int numInitialPlacementMoves()
	{
		return numInitialPlacementMoves;
	}

	/**
	 * To set the number of start place rules.
	 *
	 * @param numInitialPlacementMoves
	 */
	public void setNumInitialPlacementMoves(final int numInitialPlacementMoves)
	{
		this.numInitialPlacementMoves = numInitialPlacementMoves;
	}

	//-------------------------------------------------------------------------

	/**
	 * Clears cached list of legal moves (NOTE: not really clearing, actually
	 * it instantiates a new empty list)
	 */
	public void clearLegalMoves()
	{
		legalMoves = new BaseMoves(null);
	}
	
	/**
	 * Generates a complete list of all the moves
	 * @return Complete list of moves
	 */
	public List<Move> generateCompleteMovesList()
	{
		return moves.generateCompleteMovesList();
	}
	
	/**
	 * Generates a complete list of all the real moves that were made
	 * @return Complete list of real moves
	 */
	public List<Move> generateRealMovesList()
	{
		final List<Move> realMoves = new ArrayList<>();
		for (int i = numInitialPlacementMoves(); i < numMoves(); i++)
			realMoves.add(moves.getMove(i));
		return realMoves;
	}
	
	/**
	 * @return An iterator that iterates through all the moves in reverse order
	 */
	public Iterator<Move> reverseMoveIterator()
	{
		return moves.reverseMoveIterator();
	}

	/**
	 * Reset this trial ready to play again.
	 *
	 * @param game
	 */
	public void reset(final Game game)
	{
		moves = new MoveSequence(null);
		numInitialPlacementMoves = 0;
		
		if (startingPos != null)
			startingPos.clear();
		
		status = null;
		
		if (legalMoves != null)
			clearLegalMoves();
		
		if (previousStates != null)
			previousStates.clear();
		if (previousStatesWithinATurn != null)
			previousStatesWithinATurn.clear();
		
		numSubmovesPlayed = 0;
		
		if (auxilTrialData != null)
			auxilTrialData.clear();
		
		Arrays.fill(ranking, 0.0);
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Last move. Null if there is no last move.
	 */
	public Move lastMove()
	{
		return moves.lastMove();
	}
	
	/**
	 * @param pid The index of the player.
	 * @return Last move of a specific player.
	 */
	public Move lastMove(final int pid)
	{
		return moves.lastMove(pid);
	}

	/**
	 * @param moverId the id of the mover.
	 * @return The index of the mover in the last turn.
	 */
	public int lastTurnMover(final int moverId)
	{
		final Iterator<Move> movesIterated = moves.reverseMoveIterator();
		while (movesIterated.hasNext())
		{
			final int idPlayer = movesIterated.next().mover();
			if (idPlayer != moverId)
				return idPlayer;
		}
		
		return Constants.UNDEFINED;
	}

	/**
	 * @return Number of move played so far.
	 */
	public int numMoves()
	{
		return moves.size();
	}
	
	/**
	 * @return Number of forced passes played so far.
	 */
	public int numForcedPasses()
	{
		int count = 0;
		for(int index = numInitPlacement(); index < moves.size(); index++)
		{
			final Move m = moves.getMove(index);
			if(m.isForced())
				count++;
		}
		return count;
	}
	
	/**
	 * @param game The game to replay.
	 * @return Number of times throughout a trial that the mover must choose from two or more legal moves.
	 */
	public int numLogicalDecisions(final Game game)
	{
		final Context context = new Context(game,new Trial(game));
		context.game().start(context);
		int count = 0;
		for(int index = context.trial().numInitialPlacementMoves(); index < moves.size(); index++)
		{
			final Move m = moves.getMove(index);
			if(context.game().moves(context).moves().size() >= 2)
				count++;
			context.game().apply(context, m);
		}

		return count;
	}
	
	/**
	 * @param game The game to replay. 
	 * @return Number of times throughout a trial that the mover must choose from two or more plausible moves (i.e. legal moves that do not immediately lose the game or give the next player a winning reply -- need two ply search to detect this).
	 */
	public int numPlausibleDecisions(final Game game)
	{
		final Context context = new Context(game,new Trial(game));
		context.game().start(context);
		int count = 0;
		for(int index = context.trial().numInitialPlacementMoves(); index < moves.size(); index++)
		{
			final int mover = context.state().mover();
			final int next = context.state().next();
			final Move m = moves.getMove(index);
			final FastArrayList<Move> newLegalMoves = context.game().moves(context).moves();
			int counterPlausibleMove = 0;
			for(final Move legalMove : newLegalMoves)
			{
				final Context newContext = new TempContext(context);
				newContext.game().apply(newContext, legalMove);
				final boolean moverLoss = !newContext.active(mover) && !newContext.winners().contains(mover);
				final boolean nextPlayerWin = next != mover && !newContext.active(next) && newContext.winners().contains(next);

				if(!moverLoss && !nextPlayerWin)
					counterPlausibleMove++;
				
				if(counterPlausibleMove >= 2)
				{
					count++;
					break;
				}
			}
			context.game().apply(context, m);
		}

		return count;
	}

	/**
	 * @return The number of the init placement.
	 */
	public int numInitPlacement()
	{
		return numInitialPlacementMoves;
	}

	/**
	 * To add another init Placement.
	 */
	public void addInitPlacement()
	{
		numInitialPlacementMoves++;
	}
	
	/**
	 * @return Array of ranks (one rank per player, indexing starts at 1). Best rank is 1.0,
	 * higher values are worse.
	 */
	public double[] ranking()
	{
		return ranking;
	}

	//-------------------------------------------------------------------------

	/**
	 * Tells this Trial that it can save the current state (if it wants to).
	 * 
	 * @param state
	 */
	public void saveState(final State state)
	{
		if (auxilTrialData != null)
			auxilTrialData.saveState(state);
	}

	/**
	 * Tells this trial that it should store a history of all states.
	 */
	public void storeStates()
	{
		if (auxilTrialData == null)
			auxilTrialData = new AuxilTrialData();
		
		auxilTrialData.storeStates();
	}

	/**
	 * Tells this trial that it should store the list of legal moves
	 * for every game state encountered.
	 */
	public void storeLegalMovesHistory()
	{
		if (auxilTrialData == null)
			auxilTrialData = new AuxilTrialData();
		
		auxilTrialData.storeLegalMovesHistory();
	}
	
	/**
	 * Tells this trial that it should store the sizes of lists 
	 * of legal moves for every game state encountered.
	 */
	public void storeLegalMovesHistorySizes()
	{
		if (auxilTrialData == null)
			auxilTrialData = new AuxilTrialData();
		
		auxilTrialData.storeLegalMovesHistorySizes();
	}

	/**
	 * Set the legal moves history (used when deserializing a trial)
	 * @param legalMovesHistory
	 */
	public void setLegalMovesHistory(final List<List<Move>> legalMovesHistory)
	{
		if (auxilTrialData == null)
			auxilTrialData = new AuxilTrialData();
			
		auxilTrialData.setLegalMovesHistory(legalMovesHistory);
	}
	
	/**
	 * Sets the history of legal moves list sizes (used when deserializing a trial)
	 * @param legalMovesHistorySizes
	 */
	public void setLegalMovesHistorySizes(final TIntArrayList legalMovesHistorySizes)
	{
		if (auxilTrialData == null)
			auxilTrialData = new AuxilTrialData();
			
		auxilTrialData.setLegalMovesHistorySizes(legalMovesHistorySizes);
	}
	
	/**
	 * @return Number of sub-moves played in this trial. Only valid for simultaneous-move games
	 */
	public int numSubmovesPlayed()
	{
		return numSubmovesPlayed;
	}
	
	/**
	 * Sets the number of sub-moves played in this trial (for simultaneous-move games)
	 * @param numSubmovesPlayed
	 */
	public void setNumSubmovesPlayed(final int numSubmovesPlayed)
	{
		this.numSubmovesPlayed = numSubmovesPlayed;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return The number of the move.
	 */
	public int moveNumber()
	{
		return numMoves() - numInitPlacement();
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Saves this trial to the given file (in binary format)
	 * @param file
	 * @param gameName
	 * @param gameStartRngState Internal state of Context's RNG before start of game
	 * @throws IOException
	 */
	public void saveTrialToFile
	(
		final File file,
		final String gameName,
		final RandomProviderDefaultState gameStartRngState
	) throws IOException
	{
		if (file != null)
		{
			file.getParentFile().mkdirs();

			if (!file.exists())
				file.createNewFile();

			try 
			(
				final ObjectOutputStream out = 
					new ObjectOutputStream
					(
						new BufferedOutputStream
						(
							new FileOutputStream(file.getAbsoluteFile())
						)
					)
			)
			{
				out.writeUTF(gameName);
				out.writeInt(gameStartRngState.getState().length);
				out.write(gameStartRngState.getState());
				//System.out.println("wrote RNG state: " + Arrays.toString(currGameStartRngState.getState()));
				out.writeObject(this);
				out.reset();
				out.flush();
			}
			catch (final IOException e1)
			{
				e1.printStackTrace();
			}
		}
	}
	
	/**
	 * Just calls the method below
	 * 
	 * @param file
	 * @param gameName
	 * @param gameOptions
	 * @param gameStartRngState
	 * @throws IOException
	 */
	public void saveTrialToTextFile
	(
		final File file,
		final String gameName,
		final List<String> gameOptions,
		final RandomProviderDefaultState gameStartRngState
	) throws IOException
	{
		saveTrialToTextFile(file, gameName, gameOptions, gameStartRngState, false);
	}

	/**
	 * Saves this trial to the given file (in text format)
	 * @param file
	 * @param gameName
	 * @param gameStartRngState
	 * @param gameOptions
	 * @param trialContainsSandbox
	 * @throws IOException
	 */
	public void saveTrialToTextFile
	(
		final File file,
		final String gameName,
		final List<String> gameOptions,
		final RandomProviderDefaultState gameStartRngState,
		final boolean trialContainsSandbox
	) throws IOException
	{
		if (file != null)
		{
			file.getParentFile().mkdirs();

			if (!file.exists())
				file.createNewFile();

			try (final PrintWriter writer = new PrintWriter(file))
			{
				writer.print(convertTrialToString(gameName, gameOptions, gameStartRngState));
				writer.println("SANDBOX=" + trialContainsSandbox);
				writer.println("LUDII_VERSION=" + Constants.LUDEME_VERSION);
			}
			catch (final IOException e1)
			{
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Saves this trial to the given file (in text format)
	 * @param gameName
	 * @param gameOptions
	 * @param gameStartRngState
	 * @throws IOException
	 * @return String representation of Trial
	 */
	public String convertTrialToString
	(
		final String gameName,
		final List<String> gameOptions,
		final RandomProviderDefaultState gameStartRngState
	) throws IOException
	{
		final StringBuilder sb = new StringBuilder();
		
//		if (getTrials().size() > 0)
//		{
			// TODO saving all sub-trials of a match in a single file
			
			// A match trial with multiple instance trials
//			for (int i = 0; i < getTrials().size(); i++)
//			{
//				final String trialString = getTrial(i).convertTrialToString(gameName, gameOptions, gameStartRngState);
//				final String[] splitTrialString = trialString.split("\n");
//
//				if (i == 0)
//				{
//					for (final String s : splitTrialString)
//					{
//						if 
//						(
//							s.substring(0, 4).equals("Move") || 
//							s.substring(0, 4).equals("game") || 
//							s.substring(0, 3).equals("RNG")
//						)
//						{
//							sb.append(s + "\n");
//						}
//					}
//				}
//				else
//				{
//					for (final String s : splitTrialString)
//					{
//						if 
//						(
//							s.substring(0, 4).equals("Move") ||
//							(i == getTrials().size() - 1 && s.substring(0, 8).equals("rankings"))
//						)
//						{
//							sb.append(s + "\n");
//						}
//					}
//				}
//			}
//		}
//		else
//		{
		
		// Just a normal trial
		if (gameName != null)
			sb.append("game=" + gameName + "\n");

		if (gameOptions != null)
		{
			sb.append("START GAME OPTIONS\n");
			for (final String option : gameOptions)
			{
				sb.append(option + "\n");
			}
			sb.append("END GAME OPTIONS\n");
		}
		
		if (gameStartRngState != null)
		{
			sb.append("RNG internal state=");
			final byte[] bytes = gameStartRngState.getState();
			for (int i = 0; i < bytes.length; ++i)
			{
				sb.append(bytes[i]);
				if (i < bytes.length - 1)
					sb.append(",");
			}
			sb.append("\n");
		}
		
		final List<Move> movesList = moves.generateCompleteMovesList();
		for (int i = 0; i < movesList.size(); ++i)
		{
			sb.append("Move=" + movesList.get(i).toTrialFormat(null) + "\n");
		}

		if (auxilTrialData != null)
		{
			if (auxilTrialData.storeLegalMovesHistorySizes)
			{
				for (int i = 0; i < auxilTrialData.legalMovesHistorySizes.size(); ++i)
				{
					sb.append("LEGAL MOVES LIST SIZE = " + auxilTrialData.legalMovesHistorySizes.getQuick(i) + "\n");
				}
			}
	
			if (auxilTrialData.storeLegalMovesHistory)
			{
				for (final List<Move> legal : auxilTrialData.legalMovesHistory)
				{
					sb.append("NEW LEGAL MOVES LIST" + "\n");
	
					for (int i = 0; i < legal.size(); ++i)
						sb.append(legal.get(i).toTrialFormat(null) + "\n");
	
					sb.append("END LEGAL MOVES LIST" + "\n");
				}
			}
		}
		
		sb.append("numInitialPlacementMoves=" + numInitialPlacementMoves + "\n");

		if (status != null)
		{
			sb.append("winner=" + status.winner() + "\n");
			sb.append("endtype=" + status.endType() + "\n");
		}

		if (ranking != null)
		{
			sb.append("rankings=");
			for (int i = 0; i < ranking.length; ++i)
			{
				if (i > 0)
					sb.append(",");

				sb.append(ranking[i]);
			}
			sb.append("\n");
		}

		return sb.toString();
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		String str = "";
		try
		{
			str = convertTrialToString(null, null, null);
		} 
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		return str;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Number of turns, i.e. mover changes, in this trial.
	 */
	public int numTurns()
	{
		int currentPlayerNumber = 0;
		int numTurns = 0;
		for (final Move m : moves.generateCompleteMovesList())
		{
			if (m.mover() != currentPlayerNumber)
			{
				currentPlayerNumber = m.mover();
				numTurns++;
			}
		}
		return numTurns;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The previous state in case of no repetition rule.
	 */
	public TLongArrayList previousState()
	{
		return previousStates;
	}
	
	/**
	 * @return The previous state in the same turn.
	 */
	public TLongArrayList previousStateWithinATurn()
	{
		return previousStatesWithinATurn;
	}

	/**
	 * @param idComponent
	 * @return The starting positions of a component.
	 */
	public Region startingPos(final int idComponent)
	{
		return startingPos.get(idComponent);
	}

	/**
	 * @return The starting positions of all the components.
	 */
	public List<Region> startingPos()
	{
		return startingPos;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The number of non-initial placement moves made in this trial.
	 */
	public int numberRealMoves()
	{
		return numMoves() - numInitialPlacementMoves();
	}
	
	//-----------------------Undo Data------------------------------------
	
	/**
	 * @return The list of End Data.
	 */
	public List<UndoData> endData()
	{
		return endData;
	}
	
	/**
	 * To add an endData to the list.
	 * @param endDatum The end Data to add.
	 */
	public void addUndoData(final UndoData endDatum)
	{
		endData.add(endDatum);
	}
	
	/**
	 * To remove the last end data from the list.
	 */
	public void removeLastEndData()
	{
		endData.remove(endData.size()-1);
	}
	
	/**
	 * @return The list of RNG States.
	 */
	public List<RandomProviderState> RNGStates()
	{
		return RNGStates;
	}
	
	/**
	 * To add an RNGState to the list.
	 * @param RNGState The RNG state to add.
	 */
	public void addRNGState(final RandomProviderState RNGState)
	{
		RNGStates.add(RNGState);
	}
	
	/**
	 * To remove the last RNG state from the list.
	 */
	public void removeLastRNGStates()
	{
		RNGStates.remove(RNGStates.size()-1);
	}
	
	/**
	 * Sets any data only required for undo() operations to null
	 */
	public void nullUndoData()
	{
		endData = null;
		RNGStates = null;
	}
}