package core.eval;

import java.util.Vector;

import core.Chessboard;
import core.EvaluationFunction;
import core.Metaposition;
import core.Move;

public class KRRKEvaluationFunction extends EvaluationFunction {

	int pieces[] = new int[11];
	Metaposition s2[] = new Metaposition[5];
	public static boolean verbose = false;
	
	public KRRKEvaluationFunction()
	{
		super();
		
		this.addComponent(new core.eval.KRRKMainComponent());
	}
	
	public String getName() { return "KRRK Evaluation Function"; }
	public boolean useLoopDetector() { return true; }
	public boolean useKillerPruning() { return false; }
	public float getExplorationAlpha(Metaposition m) { return 0.05f; }
	public float getNodeMultFactor(Metaposition m) { return 1.0f; }

	public boolean canHandleSituation(Metaposition root,
			int capx, int capy, int check1, int check2, int tries)
	{
		return (root.pawnsLeft+root.piecesLeft==0 && 
				root.owner.globals.queens+
				root.owner.globals.rooks>1);
	}
	
	private static boolean moveCertainlyIllegal(Metaposition source, Move m, Metaposition dest)
	{
		int candidates = 0;
		if (m.piece!=Chessboard.KING) return false;
		if (dest.getSquareWithEnemyPiece(Chessboard.KING)>=0) return false;
		for (byte x=0; x<8; x++)
			for (byte y=0; y<0; y++)
			{
				if (source.canContainEnemyKing(x,y))
				{
					int dx = x - m.toX;
					int dy = y - m.toY;
					if (dx<0) dx = -dx; if (dy<0) dy = -dy;
					int d = (dx>dy? dx : dy);
					//if (d>1) return false;
					if (d>1) candidates++;
				}
			}
		if (candidates>0) return false;
		return true;
	}
	
	public Metaposition generateMostLikelyEvolution(Metaposition source, Move m)
	{
		
		/*if (m.piece==Chessboard.KING)
		{
			for (int k=0; k<owner.bannedMoves.size(); k++)
			{
				Move m2 = (Move)owner.bannedMoves.get(k);
				if (m2.toX==m.toX && m2.toY==m.toY) return null;
			}
		}*/
		
		Vector v = new Vector();
		Metaposition standard = super.generateMostLikelyEvolution(source,m);
		s2[0] = standard; //this one assumes no checks, then rank and file
		s2[1] = Metaposition.evolveAfterMove(source,m,Chessboard.NO_CAPTURE,-1,-1,Chessboard.CHECK_RANK,Chessboard.NO_CHECK,0);
		
		s2[2] = Metaposition.evolveAfterMove(source,m,Chessboard.NO_CAPTURE,-1,-1,Chessboard.CHECK_FILE,Chessboard.NO_CHECK,0);
		for (int k=0; k<3; k++) 
		{ 
			if (verbose) System.out.println("->"+s2[k].getSquareWithEnemyPiece(Chessboard.KING)+" "+evaluate(source,s2[k],m,v));
			if (moveCertainlyIllegal(source,m,s2[k])) s2[k]=null; //discard impossible states.
			//if (s2[k]!=null) s2[k] = Metaposition.evolveAfterOpponentMove(s2[k],-1,-1,Chessboard.NO_CHECK,Chessboard.NO_CHECK,0);
		} 
		
		Metaposition best = null;
		float bestScore = 1000000.0f; //actually the worst score
		
		for (int k=0; k<3; k++)
		{
			float sc = (s2[k]!=null? evaluate(source,s2[k],m,v) : 2000000.0f);
			if (sc<bestScore)
			{
				bestScore = sc;
				best = s2[k];
			}
		}
		if (best==null) {
			//best = standard; 
		}
		return best;
	}
	
	public float getMinValueForExpansion() { return -200.0f; }
	public float getMaxValueForExpansion() { return 5000.0f; }
	
}


