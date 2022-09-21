/*
 * Created on 17-mar-06
 *
 */
package core.eval.components;

import java.util.Vector;

import core.Chessboard;
import core.EvaluationFunction;
import core.EvaluationFunctionComponent;
import core.EvaluationGlobals;
import core.Metaposition;
import core.Move;

/**
 * @author Nikola Novarlic
 *
 */
public class DefaultMaterialComponent extends EvaluationFunctionComponent {

	public DefaultMaterialComponent()
	{
		super();
		name = "Material";
	}

	public float evaluate(Metaposition start, Metaposition dest, Move m, Vector history)
	{
		float totalvalue = 0.0f;
		float materialvalue = 0.0f;
		float result = 0.0f;
		float value = 0.0f;
		
		boolean isExtendedTree = EvaluationFunction.currentNode!=null;
		
		for (int k=0; k<64; k++)
		{
			if ((dest.squares[k] & 0x80)!=0)
			{
				switch (dest.squares[k] & 0x7F)
				{
					case Chessboard.PAWN:
					value = /*1.5f;*/ start.owner.globals.pieceValues[0]; result+=0.0f*(dest.isWhite()? k%8 : 7-(k%8))*start.owner.globals.pieceValues[Chessboard.PAWN]/5.0f; break;
					case Chessboard.KNIGHT:
					value = /*2.5f;*/ start.owner.globals.pieceValues[1]; break;
					case Chessboard.BISHOP:
					value = /*3.0f;*/ start.owner.globals.pieceValues[2]; break;
					case Chessboard.ROOK:
					value = /*5.0f;*/ start.owner.globals.pieceValues[3]; break;
					case Chessboard.QUEEN:
					value = /*9.0f;*/ start.owner.globals.pieceValues[4]; result+=10.0f; break;
					case Chessboard.KING:
					value = 5.0f * (dest.piecesLeft+dest.pawnsLeft)/15.0f; //the king's need for protection decreases with fewer enemy pieces
					break;
				}
				boolean moved = (EvaluationFunction.index == k);
				totalvalue += value;
				materialvalue += value*dest.pieceSafety(k/8,k%8,moved,moved && m!=null && m.capture);
				
			} else
			{
				result += start.owner.globals.currentEvaluator.opponentSquareValues[dest.squares[k]][k] * EvaluationGlobals.dangerFunction[dest.ageMatrix[k]];
			}
		}
		
		//result += materialvalue * (40.0f-35.0f*(15.0-dest.piecesLeft-dest.pawnsLeft)/15.0f) / totalvalue; //result is always scaled to a fixed number...
		result += materialvalue;
		
		result -= (dest.pawnsLeft*2.0);
		result -= (dest.piecesLeft*5.0);
		result += 1.0f * start.owner.globals.materialDelta;
		
		return result;
	}



}
