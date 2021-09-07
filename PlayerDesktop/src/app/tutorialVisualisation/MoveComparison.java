package app.tutorialVisualisation;

import java.util.ArrayList;
import java.util.List;

import other.context.Context;
import other.move.Move;
import other.topology.Topology;

public class MoveComparison
{

	//-------------------------------------------------------------------------
	
	/**
	 * Determines if two moves can be merged due to them containing the same key information.
	 */
	public final static boolean movesCanBeMerged(final Topology topo, final MoveCompleteInformation m1, final MoveCompleteInformation m2)
	{
		if (m1.what() != m2.what())
			return false;
		
		if (m1.move().mover() != m2.move().mover())
			return false;
		
		if (!m1.move().getDescription().equals(m2.move().getDescription()))
			return false;
		
		if (m1.move().actions().size() != m2.move().actions().size())
			return false;
		
		if (!m1.englishDescription().equals(m2.englishDescription()))
			return false;
		
		if (!m1.move().direction(topo).equals(m2.move().direction(topo)))
			return false;
		
		for (int i = 0; i < m1.move().actions().size(); i++)
		{
			final String m1ActionDescription = m1.move().actions().get(i).getDescription();
			final String m2ActionDescription = m2.move().actions().get(i).getDescription();
			if (!m1ActionDescription.equals(m2ActionDescription))
				return false;
		}
		
		// m.direction(ref.context()
		
		return true;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns a list off all similar legal moves at the point in the context where the trueMove was applied.
	 * Similar moves are those that can be merged, and are from the same location.
	 */
	public final static List<Move> similarMoves(final Context context, final Move trueMove)
	{
		final int trueMoveWhat = ValueUtils.getWhatOfMove(context, trueMove);
		final MoveCompleteInformation trueMoveCompleteInfo = new MoveCompleteInformation(context.game(), null, null, trueMove, -1, trueMoveWhat, null);
		
		final List<Move> similarMoves = new ArrayList<>();
		for (final Move move : context.moves(context).moves())
		{
			final Move moveWithConsequences = new Move(move.getMoveWithConsequences(context));
			moveWithConsequences.setMovesLudeme(move.movesLudeme());
			
			final int moveWhat = ValueUtils.getWhatOfMove(context, moveWithConsequences);
			final MoveCompleteInformation moveCompleteInfo = new MoveCompleteInformation(context.game(), null, null, moveWithConsequences, -1, moveWhat, null);
			
			if (movesCanBeMerged(context.topology(), trueMoveCompleteInfo, moveCompleteInfo) && moveWithConsequences.getFromLocation().equals(trueMove.getFromLocation()))
				similarMoves.add(new Move(moveWithConsequences));
		}
		
		if (similarMoves.isEmpty())
			System.out.println("ERROR! similarMoves was empty");
		
		return similarMoves;
	}
	
	//-------------------------------------------------------------------------
	
}
