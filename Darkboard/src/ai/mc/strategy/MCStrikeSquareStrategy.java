package ai.mc.strategy;

import core.Chessboard;
import core.Move;
import ai.mc.MCLink;
import ai.mc.MCLowLevelStrategy;
import ai.mc.MCState;

public class MCStrikeSquareStrategy extends MCLowLevelStrategy {
	
	public MCStrikeSquareStrategy(int x, int y)
	{
		parameters[0] = x; parameters[1] = y;
		name = "X";
	}
	
	public double[] progressInformation(MCState m, MCLink[] moves) 
	{
		double out[] = new double[moves.length];
		
		for (int k=0; k<out.length; k++)
		{
			Move mv = moves[k].move;
			out[k] = (mv.toX==parameters[0] && mv.toY==parameters[1] && (mv.piece!=Chessboard.PAWN || mv.toX!=mv.fromX)? 1.0 : -100.0);
		}
		return out;
	}

}
