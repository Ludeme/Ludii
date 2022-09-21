/*
 * Created on 17-mar-06
 *
 */
package core.eval.components;

import java.util.Vector;

import ai.evolve.SimpleWeightSet;
import core.Chessboard;
import core.EvaluationFunction;
import core.EvaluationFunctionComponent;
import core.EvaluationGlobals;
import core.Globals;
import core.Metaposition;
import core.Move;

/**
 * @author Nikola Novarlic
 *
 */
public class DefaultPositionComponent extends EvaluationFunctionComponent {
	
	public DefaultPositionComponent()
	{
		super();
		name = "Position";
	}
	
	public float evaluate(Metaposition start, Metaposition dest, Move m, Vector history)
	{
		float totalvalue = 0.0f;
		float materialvalue = 0.0f;
		float result = 0.0f;
		float value = 0.0f;
		
		boolean isExtendedTree = EvaluationFunction.currentNode!=null;
		
		//compute mobility bonus...
		int mob = 0;
		for (int k=7; k>=0; k--)
		for (int j=7; j>=0; j--) if (start.owner.globals.protectionMatrix[k][j]>0) mob++;
			
		result += (EvaluationGlobals.weights.weights[SimpleWeightSet.W_MOBILITY_MODIFIER] * mob);


		//add king proximity...
		if (start.owner.globals.totalMaterial<4) result+=dest.kingProximityBonus(
				start.owner.globals.kingLocationX,start.owner.globals.kingLocationY)*0.1;
		

		//calculate pawn number
		for (int k=0; k<8; k++)
		{
			if (start.owner.globals.pawnSquaresInFiles[k]==0) result += 1.0f;
		}
		
		//if (start.owner.experimental && dest.canKeep(m)==Discardable.NO) result -= 10.0f;
		if (start.owner.experimental && Globals.usePools)
		{
			if (m.piece!=Chessboard.PAWN)
			{
				double protDelta = start.owner.globals.protectionMatrix[m.toX][m.toY] -
					2.0 * /*start.owner.pool.protection(m.toX, m.toY)*/
					start.owner.globals.opponentProtectionData[m.toX][m.toY];
				
				result += (1.0-/*start.owner.pool.probability(m.toX, m.toY, Chessboard.EMPTY))*/
						start.owner.globals.opponentDensityData[m.toX][m.toY][Chessboard.EMPTY])
						*4.0*protDelta;
			}
			
			int y = (start.isWhite()? m.toY+1 : m.toY-1);
			if (y>=0 && y<=7)
			{
				for (int k=0; k<2; k++)
				{
					int x = (k==0? m.toX-1 : m.toX+1);
					if (x<0 || x>7) continue;
					result -= /*start.owner.pool.probability(x, y, Chessboard.PAWN)*/
						start.owner.globals.opponentDensityData[x][y][Chessboard.PAWN]*10.0;
				}
			}
		}
		
		return result;
	}

}
