/*
 * Created on 2-mar-06
 *
 */
package core.eval;

import core.Chessboard;
import core.Metaposition;
import umpire.local.LocalUmpire;

/**
 * @author Nikola Novarlic
 * This evaluation function is used when:
 * 1) The enemy only has the king left
 * 2) The player has no major pieces left, but has at least one pawn.
 *
 */
public class KPKEvaluationFunction extends core.EvaluationFunction {
	
	static int pawnLocs[] = new int[8];
	
	public KPKEvaluationFunction()
	{
		super();
		
		this.addComponent(new core.eval.KPKMainComponent());
	}
	
	public String getName() { return "KPK Evaluation Function"; }
	public boolean useKillerPruning() { return false; }
	
	/*public boolean canHandleSituation(Metaposition root,
		int capx, int capy, int check1, int check2, int tries)
	{
		return (root.pawnsLeft+root.piecesLeft==0) && EvaluationGlobals.queens==0 &&
		EvaluationGlobals.rooks==0 && EvaluationGlobals.pawns>0;
	}*/
	public boolean canHandleSituation(Metaposition root,
			int capx, int capy, int check1, int check2, int tries)
	{
		return (root.pawnsLeft+root.piecesLeft<5) && root.owner.globals.queens==0 &&
		root.owner.globals.rooks==0 && root.owner.globals.pawns>0;
	}
	
	private static void prepareBoard(LocalUmpire u)
	{
		u.emptyBoard();
		u.insertPiece(Chessboard.KING,0,false);
		u.insertPiece(Chessboard.ROOK,0,true);
		u.insertPiece(Chessboard.KING,1,false);
	}
	

}
