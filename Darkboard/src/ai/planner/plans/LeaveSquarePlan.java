package ai.planner.plans;

import java.util.Vector;

import ai.planner.Plan;
import ai.player.Darkboard;
import core.Metaposition;
import core.Move;

/**
 * This simple plan encourages the AI to stay clear of a given square.
 * This is useful, for example, when the player suspects that retaliation
 * is coming.
 * @author Nikola Novarlic
 *
 */
public class LeaveSquarePlan extends Plan {
	
	int sx,sy; //square to be left
	
	public LeaveSquarePlan(Darkboard d, int x, int y)
	{
		super(d);
		sx = x; sy = y;
	}
	
	protected float fitness(Metaposition start, Metaposition end, Move m, Vector history)
	{
		float result = 0.01f;
		
		description = "LS -> ";
		
		if (history.size()==0) result = -0.05f;
		
		for (int k=0; k<history.size(); k++)
			description+=history.get(k)+" ";
		
		for (int k=0; k<history.size(); k++)
		{
			Move move = (Move)history.get(k);
			if (move.fromX!=sx || move.fromY!=sy) result-=3.0f;
			else break;
		}
		description+=result;
		return result;
		
		/*int piece = end.getFriendlyPiece(sx,sy);
		if (piece==Chessboard.EMPTY) return 0.0f;
		if (piece==Chessboard.PAWN && EvaluationGlobals.protectionMatrix[sx][sy]>0)
			return 0.0f; //we give no negative bonus to protected pawns.
		
		return -1.0f;*/
	}
	
	public String toString()
	{
		return "Leave square "+Move.squareString(sx,sy);
	}

	public int getSx() {
		return sx;
	}

	public void setSx(int sx) {
		this.sx = sx;
	}

	public int getSy() {
		return sy;
	}

	public void setSy(int sy) {
		this.sy = sy;
	}

}
