package other.trial;

import java.util.ArrayList;
import java.util.List;

import game.rules.play.moves.Moves;
import gnu.trove.list.array.TIntArrayList;
import other.context.Context;
import other.move.Move;
import other.state.State;

/**
 * Wrapper class for auxiliary data for Trials, which we usually do not want to keep track of
 * but sometimes do (usually for unit testing purposes).
 *
 * @author Dennis Soemers
 */
public class AuxilTrialData
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * If true, we'll also store a history of states (in addition to actions).
	 *
	 * NOTE: Currently we do not copy this flag when copying Trials using the copy
	 * constructor. We assume this flag is only used when we want to store logs of
	 * played games into files. If, in the future, we ever get any game rules that
	 * depend on this flag, we'll also want to make sure to copy it in the copy
	 * constructor (because it will become important for copied Trials in MCTS
	 * simulations for example)
	 */
	protected transient boolean storeStates = false;

	/**
	 * If true, we'll also store a history of the legal moves in every encountered
	 * game state.
	 *
	 * NOTE: same note as above.
	 */
	protected transient boolean storeLegalMovesHistory = false;
	
	/**
	 * If true, we'll also store a history of sizes of legal moves lists in every
	 * encountered game state.
	 * 
	 * NOTE: same note as above.
	 */
	protected transient boolean storeLegalMovesHistorySizes = false;

	/** History of states */
	protected List<State> states = null;

	/** History of legal moves per game state */
	protected List<List<Move>> legalMovesHistory = null;
	
	/** History of sizes of legal moves lists per game state */
	protected TIntArrayList legalMovesHistorySizes = null;

	//-------------------------------------------------------------------------
	
	/**
	 * @return History of all states. Will often be null (only not null if we
	 *         explicitly require Trial to store states).
	 */
	public List<State> stateHistory()
	{
		return states;
	}
	
	/**
	 * Tells this Trial that it can save the current state (if it wants to).
	 * 
	 * @param state
	 */
	public void saveState(final State state)
	{
		if (storeStates)
		{
			states.add(new State(state));
		}
	}

	/**
	 * Tells this trial that it should store a history of all states.
	 */
	public void storeStates()
	{
		if (!storeStates)
		{
			storeStates = true;
			states = new ArrayList<>();
		}
	}

	/**
	 * Tells this trial that it should store the list of legal moves
	 * for every game state encountered.
	 */
	public void storeLegalMovesHistory()
	{
		if (!storeLegalMovesHistory)
		{
			storeLegalMovesHistory = true;
			legalMovesHistory = new ArrayList<>();
		}
	}
	
	/**
	 * Tells this trial that it should store the sizes of lists 
	 * of legal moves for every game state encountered.
	 */
	public void storeLegalMovesHistorySizes()
	{
		if (!storeLegalMovesHistorySizes)
		{
			storeLegalMovesHistorySizes = true;
			legalMovesHistorySizes = new TIntArrayList();
		}
	}

	/**
	 * Set the legal moves history (used when deserializing a trial)
	 * @param legalMovesHistory
	 */
	public void setLegalMovesHistory(final List<List<Move>> legalMovesHistory)
	{
		this.legalMovesHistory = legalMovesHistory;
	}
	
	/**
	 * Sets the history of legal moves list sizes (used when deserializing a trial)
	 * @param legalMovesHistorySizes
	 */
	public void setLegalMovesHistorySizes(final TIntArrayList legalMovesHistorySizes)
	{
		this.legalMovesHistorySizes = legalMovesHistorySizes;
	}
	
	/**
	 * @return History of legal moves in all traversed states
	 */
	public List<List<Move>> legalMovesHistory()
	{
		return legalMovesHistory;
	}
	
	/**
	 * @return History of sizes of legal moves lists in all traversed states
	 */
	public TIntArrayList legalMovesHistorySizes()
	{
		return legalMovesHistorySizes;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Clears all stored data
	 */
	public void clear()
	{
		if (states != null)
			states.clear();

		if (legalMovesHistory != null)
			legalMovesHistory.clear();
		
		if (legalMovesHistorySizes != null)
			legalMovesHistorySizes.clear();
	}
	
	/**
	 * Update this auxiliary data based on a newly-computed list of legal moves
	 * @param legalMoves New list of legal moves
	 * @param context 
	 */
	public void updateNewLegalMoves(final Moves legalMoves, final Context context)
	{
		final Trial trial = context.trial();
		
		if (storeLegalMovesHistory)
		{
			if (legalMovesHistory.size() == (trial.numMoves() - trial.numInitialPlacementMoves()) + 1)
			{
				// We probably previous called this from a (stalemated Next) End rule,
				// need to correct for that
				legalMovesHistory.remove(legalMovesHistory.size() - 1);
			}
			
			if (legalMovesHistory.size() == trial.numMoves() - trial.numInitialPlacementMoves())
			{
				final List<Move> historyList = new ArrayList<>();
				for (final Move move : legalMoves.moves())
				{
					final Move moveToAdd = new Move(move.getActionsWithConsequences(context));
					moveToAdd.setFromNonDecision(move.fromNonDecision());
					moveToAdd.setToNonDecision(move.toNonDecision());
					moveToAdd.setMover(move.mover());
					historyList.add(moveToAdd);
				}
				legalMovesHistory.add(historyList);
			}
		}
		
		if (storeLegalMovesHistorySizes)
		{
			if (legalMovesHistorySizes.size() == (trial.numMoves() - trial.numInitialPlacementMoves()) + 1)
			{
				// We probably previous called this from a (stalemated Next) End rule,
				// need to correct for that
				legalMovesHistorySizes.removeAt(legalMovesHistorySizes.size() - 1);
			}
			
			if (legalMovesHistorySizes.size() == trial.numMoves() - trial.numInitialPlacementMoves())
			{
				legalMovesHistorySizes.add(legalMoves.moves().size());
			}
		}
	}
	
	/**
	 * Update auxiliary data based on a given subtrial
	 * @param subtrial
	 */
	public void updateFromSubtrial(final Trial subtrial)
	{
		if (storeLegalMovesHistory)
		{
			for (final List<Move> movesList : subtrial.auxilTrialData().legalMovesHistory())
			{
				legalMovesHistory.add(new ArrayList<Move>(movesList));
			}
		}
		
		if (storeLegalMovesHistorySizes)
			legalMovesHistorySizes.addAll(subtrial.auxilTrialData().legalMovesHistorySizes());
	}
	
	//-------------------------------------------------------------------------

}
