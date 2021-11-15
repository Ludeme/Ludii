package other.move;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents, and provides views of, a sequence of moves. Every Move object
 * is expected to be immutable, and MoveSequences are expected to never shrink
 * (but they can grow). A MoveSequence does not necessarily store all of the moves 
 * in a list itself, but may also point to a parent MoveSequence which stores the 
 * initial part of a sequence.
 * 
 * By allowing links to parent MoveSequences, we can avoid time-consuming copying
 * and excessive memory consumption when dealing with very long MoveSequences.
 *
 * @author Dennis Soemers
 */
public class MoveSequence implements Serializable
{
	
	//-----------------------------------------------------------------------------
	
	private static final long serialVersionUID = 1L;
	
	//-----------------------------------------------------------------------------
	
	/** Our parent MoveSequence */
	protected final MoveSequence parent;
	
	/** List of move objects */
	private final List<Move> moves;
	
	/** 
	 * Are we a parent of a different Move Sequence?
	 * (if we are, we'll no longer be allowed to append moves to our list)
	 */
	private boolean isParent = false;
	
	/** Number of moves in the complete chain of parent sequences, or 0 if no parent */
	private final int cumulativeParentSize;
	
	//-----------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param parent
	 */
	public MoveSequence(final MoveSequence parent)
	{
		this.parent = parent;
		this.moves = new ArrayList<Move>();
		
		if (parent != null)
		{
			cumulativeParentSize = parent.movesList().size() + parent.cumulativeParentSize;
			parent.isParent = true;
		}
		else
		{
			cumulativeParentSize = 0;
		}
	}
	
	/**
	 * Constructor which allows for invalidation
	 * @param parent
	 * @param allowInvalidation
	 */
	public MoveSequence(final MoveSequence parent, final boolean allowInvalidation)
	{
		this.parent = parent;
		this.moves = new ArrayList<Move>(1);	// Very often only need to add 1 move after (temp) copy
		
		if (parent != null)
		{
			cumulativeParentSize = parent.movesList().size() + parent.cumulativeParentSize;
			
			if (!allowInvalidation)
				parent.isParent = true;
		}
		else
		{
			cumulativeParentSize = 0;
		}
	}
	
	//-----------------------------------------------------------------------------
	
	/**
	 * Adds the given move to end of sequence, and returns updated move sequence.
	 * @param move
	 * @return Updated MoveSequence. NOTE: this may be a different object altogether,
	 * 	this object may remain unchanged if it already is a parent of other sequences!
	 */
	public MoveSequence add(final Move move)
	{
		if (isParent)
		{
			final MoveSequence newSeq = new MoveSequence(this);
			newSeq.add(move);
			return newSeq;
		}
		
		moves.add(move);
		return this;
	}
	
	/**
	 * @param idx
	 * @return Move at given index in sequence
	 */
	public Move getMove(final int idx)
	{
		final List<MoveSequence> parents = new ArrayList<MoveSequence>();
		MoveSequence nextParent = parent;
		
		while (nextParent != null)
		{
			parents.add(nextParent);
			nextParent = nextParent.parent;
		}
		
		int sublistIdx = idx;
		for (int i = parents.size() - 1; i >= 0; --i)
		{
			final List<Move> sublist = parents.get(i).movesList();
			if (sublistIdx < sublist.size())
				return sublist.get(sublistIdx);
			else
				sublistIdx -= sublist.size();
		}
		
		return moves.get(sublistIdx);
	}
	
	/**
	 * Replaces the last move in this sequence with the given new move
	 * @param move
	 */
	public void replaceLastMove(final Move move)
	{
		moves.set(moves.size() - 1, move);
	}
	
	//-----------------------------------------------------------------------------
	
	/**
	 * @return The last move in this sequence, or null if it's empty
	 */
	public Move removeLastMove()
	{
		final int size = moves.size();
		if (size != 0)
		{
			final Move move = moves.get(size -1);
			moves.remove(moves.size()-1);
			return move;
		}
		
		return null;
	}
	
	/**
	 * @return The last move in this sequence, or null if it's empty
	 */
	public Move lastMove()
	{
		final int size = moves.size();
		if (size != 0)
			return moves.get(size - 1);
		
		if (parent != null)
			return parent.lastMove();
		
		return null;
	}
	
	/**
	 * @param pid The index of the player.
	 * @return Last move of a specific player.
	 */
	public Move lastMove(final int pid)
	{
		for (int i = moves.size() - 1; i >= 0; i--)
		{
			final Move m = moves.get(i);
			if (m.mover() == pid)
				return m;
		}
		
		if (parent != null)
			return parent.lastMove(pid);
		
		return null;
	}
	
	/**
	 * @return Number of moves in complete sequence (including chain of parents)
	 */
	public int size()
	{
		return moves.size() + cumulativeParentSize;
	}
	
	//-----------------------------------------------------------------------------
	
	/**
	 * Generates a complete list of all the moves
	 * @return Complete list of moves
	 */
	public List<Move> generateCompleteMovesList()
	{
		final List<MoveSequence> parents = new ArrayList<MoveSequence>();
		MoveSequence nextParent = parent;
		
		while (nextParent != null)
		{
			parents.add(nextParent);
			nextParent = nextParent.parent;
		}
		
		final List<Move> completeMovesList = new ArrayList<Move>();
		
		for (int i = parents.size() - 1; i >= 0; --i)
		{
			completeMovesList.addAll(parents.get(i).movesList());
		}
		
		completeMovesList.addAll(moves);
		return completeMovesList;
	}
	
	//-----------------------------------------------------------------------------
	
	/**
	 * @return An iterator that iterates through all the moves in reverse order
	 */
	public Iterator<Move> reverseMoveIterator()
	{
		return new Iterator<Move>()
				{
					private MoveSequence currSeq = MoveSequence.this;
					private int currIdx = currSeq.movesList().size() - 1;

					@Override
					public boolean hasNext()
					{
						return currIdx >= 0;
					}

					@Override
					public Move next()
					{
						final Move move = currSeq.movesList().get(currIdx--);
						updateCurrSeq();
						return move;
					}
					
					protected Iterator<Move> updateCurrSeq()
					{
						while (currIdx < 0)
						{
							currSeq = currSeq.parent;
							
							if (currSeq == null)
								break;
							
							currIdx = currSeq.movesList().size() - 1;
						}
						return this;
					}
			
				}.updateCurrSeq();
	}
	
	//-----------------------------------------------------------------------------
	
	/**
	 * @return List of moves stored in just this part of the sequence
	 */
	protected List<Move> movesList()
	{
		return moves;
	}
	
	//-----------------------------------------------------------------------------

}
