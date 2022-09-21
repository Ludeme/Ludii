package core.uberposition;

import core.Chessboard;
import core.Move;

public class UPUtilities {
	
	public static double[] progressTowardsThreateningSquare(Uberposition u, Move m[], Uberposition u2[], int tx, int ty)
	{
		double out[] = new double[m.length];
		
		int power = u.attackPower(tx, ty);
		
		for (int k=0; k<m.length; k++)
		{
			if (u2[k]!=null) u2[k].scrap = true;
			Uberposition u3 = (u2[k]==null?  u.evolveWithPlayerMove(0,m[k], -1, -1, Chessboard.NO_CAPTURE, 
					Chessboard.NO_CHECK, Chessboard.NO_CHECK, 0) : u2[k]);
			if (u2[k]!=null) u2[k].scrap = false;
			
			int p2 = u3.attackPower(tx, ty);
			if (p2>power) { out[k] = 1.0; continue; }
			if (p2<power) { out[k] = -1.0; continue; }
			if (u3.piecesInBetween(m[k].toX, m[k].toY, tx, ty)<u.piecesInBetween(m[k].fromX, m[k].fromY, tx, tx))
			{
				out[k] = 0.5; continue;
			}
		}
		return out;
	}

}
