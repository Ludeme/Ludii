/*
 * Created on 3-mar-06
 *
 */
package core.eval;

import java.util.Vector;

import ai.chunk.regboard.RegBoardArray;
import ai.player.Player;
import core.Chessboard;
import core.EvaluationFunction;
import core.EvaluationTree;
import core.Metaposition;
import core.Move;
import main.Darkboard;

/**
 * @author Nikola Novarlic
 * This function is evoked when:
 * 1) Only the enemy king is left
 * 2) The player has 1 Rook
 *
 */
public class KRKEvaluationFunction extends EvaluationFunction {
	
	int pieces[] = new int[11];
	Metaposition s2[] = new Metaposition[5];
	public static boolean verbose = false;
	public static int quadrant = -1;
	public static int deltaX[] = {-1,1,-1,1};
	public static int deltaY[] = {-1,-1,1,1};
	Player owner;
	
	//RegBoardArray implementing Boyce's KRK algorithm;
	static RegBoardArray boyce;
	
	//This tree is algorithmic!
	EvaluationTree algorithmicTree;
	
	public KRKEvaluationFunction(Player p)
	{
		super();
		
		if (boyce==null)
		{
			boyce = new RegBoardArray(Darkboard.class.getResource("darkboard_data/boyce.txt").getPath());
		}
		
		owner = p;
		this.addComponent(new core.eval.KRKMainComponent());
	}
	
	public String getName() { return "KRK Evaluation Function"; }
	public boolean useLoopDetector() { return true; }
	public boolean useKillerPruning() { return false; }
	public float getExplorationAlpha(Metaposition m) { return 0.02f; }
	public float getNodeMultFactor(Metaposition m) { return 1.0f; }

	public boolean canHandleSituation(Metaposition root,
			int capx, int capy, int check1, int check2, int tries)
	{
		return (root.pawnsLeft+root.piecesLeft==0 && 
				root.owner.globals.queens==0 &&
				root.owner.globals.rooks==1);
	}
	
	/**
	 * We generate two chessboards, one assumes no checks and the other does, then we return
	 * the one with the lower value.
	 */
	/*public Metaposition generateMostLikelyEvolution(Metaposition source, Move m)
	{
		Metaposition s1 = super.generateMostLikelyEvolution(source,m);
		
		//evolve in 5 ways, one for each check type...
		for (int k=0; k<5; k++)
			s2[k] = Metaposition.evolveAfterMove(source,m,Chessboard.NO_CAPTURE,m.toX,m.toY,k+1,Chessboard.NO_CHECK,0);
		
		Metaposition other = Metaposition.getChessboard(s2[0]);
		
		//we OR the king's locations on each chessboard to find out each possible location
		for (byte k=0; k<8; k++)
			for (byte j=0; j<8; j++)
			{
				for (int z=0; z<5; z++)
					if (s2[z].canContainEnemyKing(k,j))
					{
						other.setPiecePossible(k,j,Chessboard.KING);
						break;
					}
			}
			
		//now make sure a check was *actually* possible, i.e. if 'other' has no enemy kings,
		//it doesn't make any sense and should be discarded.
		if (other.getSquareWithPiece(Chessboard.KING)<0) return s1;
		
		//return the worse case...
		if (evaluate(source,s1,m)<evaluate(source,other,m)) return s1; else return other;
	}*/
	
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
	
	
	/*public Move getAlgorithmicMove(Metaposition m)
	{
		if (algorithmicTree!=null && algorithmicTree.getChildNumber()>0)
		{
			return algorithmicTree.getChild(0).m;
		}
		
		//create algorithmic directives here...
		if (!meetAlgorithmicConditions(m)) return null;
		
		System.out.println("MEET KRK ALGORITHMIC CONDITIONS!");
		
		return null;
	}*/
	
	/**
	 * Use Boyce whenever possible.
	 */
	public Move getAutomaticMove(Metaposition m)
	{
		return boyce.getMoveSuggestion(m, owner.bannedMoves);
	}
	
	public void algorithmicMoveAccepted(Metaposition m1, Move move, Metaposition m2)
	{
		if (algorithmicTree!=null)
		{
			if (algorithmicTree!=null && algorithmicTree.getChildNumber()>0)
			{
				algorithmicTree = algorithmicTree.getChild(0);
			} else algorithmicTree=null;
		}
	}
	
	public void algorithmicMoveRejected(Metaposition m1, Move move, Metaposition m2)
	{
		if (algorithmicTree!=null)
		{
			if (algorithmicTree!=null && algorithmicTree.getChildNumber()>0)
			{
				algorithmicTree.removeChild(0);
				if (algorithmicTree.getChildNumber()<1) algorithmicTree = null;
			} else algorithmicTree=null;
		}		
	}
	
	public boolean meetAlgorithmicConditions(Metaposition m)
	{
		quadrant = -1;
		
		if (m.getSquareWithPiece(Chessboard.BISHOP)!=-1 || m.getSquareWithPiece(Chessboard.KNIGHT)!=-1
		|| m.getSquareWithPiece(Chessboard.PAWN)!=-1) return false;
			
		
		int k = m.getSquareWithPiece(Chessboard.ROOK);
		int rx = k/8; int ry = k%8;
		
		int j = m.getSquareWithPiece(Chessboard.KING);
		int kx = j/8; int ky = j%8;
		
		int quad[] = new int[4];
		
		for (int x=0; x<8; x++)
			for (int y=0; y<8; y++)
			{
				if (m.canContainEnemyKing((byte)x,(byte)y))
				{
					if (x<=rx && y<=ry) quad[0]++;
					if (x>=rx && y<=ry) quad[1]++;
					if (x<=rx && y>=ry) quad[2]++;
					if (x>=rx && y>=ry) quad[3]++;
				}
			}
		
		int quads = 0;
		for (int x=0; x<4; x++) if (quad[x]>0) quads++;
		if (quads>1) return false;
		for (int x=0; x<4; x++) if (quad[x]>0) quadrant = x;
		
		int dx = deltaX[quadrant];
		int dy = deltaY[quadrant];
		
		return (kx==rx+dx && ky==ry+dy);
		
	}
	
	private EvaluationTree makeTree(Move m)
	{
		EvaluationTree et = new EvaluationTree();
		et.m = m;
		return et;
	}
	
	protected void makeAlgorithmicTree(Metaposition m)
	{
		int k = m.getSquareWithPiece(Chessboard.ROOK);
		int rx = k/8; int ry = k%8;
		
		int j = m.getSquareWithPiece(Chessboard.KING);
		int kx = j/8; int ky = j%8;
		
		int dx = deltaX[quadrant];
		int dy = deltaY[quadrant];
		
		int distanceX = (dx<0? rx : 7-rx);
		int distanceY = (dy<0? ry : 7-ry); //rows and cols where the opponent is...
		
		boolean goWithX = (distanceX<=distanceY); //shorter way...
		
		algorithmicTree = new EvaluationTree();
		
		//TOP LEVEL: 
	}
	
	/*private Move makeMove(int p, int rx, int ry, int kx, int ky, int dx, int dy, boolean x)
	{
		Move m = new Move();
		
		m.piece = p;
		m.to
		
		return m;
	}*/
}
