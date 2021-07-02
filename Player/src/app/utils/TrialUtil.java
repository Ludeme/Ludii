package app.utils;

import java.util.List;

import manager.Manager;
import other.context.Context;
import other.move.Move;

/**
 * Functions to help with Trials. 
 * 
 * @author Matthew.Stephenson
 */
public class TrialUtil 
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns the index of the start of the current trial, within the complete game trial (used for matches).
	 */
	public static int getInstanceStartIndex(final Context context)
	{
		final int numInitialPlacementMoves = context.currentInstanceContext().trial().numInitialPlacementMoves();
		final int startIndex = context.trial().numMoves() - context.currentInstanceContext().trial().numMoves() + numInitialPlacementMoves;
		return startIndex;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns the index of the end of the current trial, within the complete game trial (used for matches).
	 */
	public static int getInstanceEndIndex(final Manager manager, final Context context)
	{
		final List<Move> allMoves = manager.ref().context().trial().generateCompleteMovesList();
		allMoves.addAll(manager.undoneMoves());
		
		if (context.isAMatch())
		{
			int endOfInstance = context.trial().numMoves();
			while (endOfInstance < allMoves.size())
			{
				if (allMoves.get(endOfInstance).containsNextInstance())
					break;
				
				endOfInstance++;
			}
			return endOfInstance;
		}
		
		return allMoves.size();
	}
	
	//-------------------------------------------------------------------------
	
}
