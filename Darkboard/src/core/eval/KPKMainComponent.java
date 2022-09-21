/*
 * Created on 17-mar-06
 *
 */
package core.eval;

import java.util.Vector;

import core.Chessboard;
import core.EvaluationFunctionComponent;
import core.Metaposition;
import core.Move;

/**
 * @author Nikola Novarlic
 *
 */
public class KPKMainComponent extends EvaluationFunctionComponent {
	
	static int pawnLocs[] = new int[8];
	
	public float evaluate(Metaposition start, Metaposition dest, Move m, Vector history)
	{
		dest.computeProtectionMatrix(true);
		dest.getSquaresWithPiece(Chessboard.PAWN,pawnLocs);
		
		float result = 0.0f;
		
		if (pawnLocs[0]==-1)
		{
			//pawn promoted to queen, if queen is protected then ok!
			int q = dest.getSquareWithPiece(Chessboard.QUEEN);
			if (q==-1) return 0.0f;
			if (/*dest.dangerRating(q/8,q%8)>0.0f &&*/
					start.owner.globals.protectionMatrix[q/8][q%8]<1) return -120.0f;
			else return 120.0f;
		}
		 
		
		int kingLoc = dest.getSquareWithPiece(Chessboard.KING);
		int kx = kingLoc/8; int ky = kingLoc%8;
		int minDist = 8;
		
		
		
		for (int k=0; k<8; k++)
		{
			if (pawnLocs[k]==-1) break;
			int px = pawnLocs[k]/8; int py = pawnLocs[k]%8;
			int dx = (px>kx? px-kx : kx-px);
			int dy = (py>ky? py-ky : ky-py);
			int d = (dx>dy? dx : dy);
			if (d<minDist) minDist=d;
			
			if (/*dest.dangerRating(px,py)==0.0f ||*/
					start.owner.globals.protectionMatrix[px][py]<1) result+=(dest.isWhite()? py : 7-py);
		}
		
		result += 5.0f * (8.0f-minDist);
		result += (dest.isWhite()? ky : 7-ky); //make the king push forward.
		
		return result;
	}

}
