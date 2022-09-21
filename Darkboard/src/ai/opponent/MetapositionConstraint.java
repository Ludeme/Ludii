package ai.opponent;

import java.util.Random;
import java.util.Vector;

import core.Metaposition;

public class MetapositionConstraint {
	
	public Vector<int[]> squares = new Vector<int[]>(); //X, Y, Probability
	public boolean pieces[] = new boolean[7];
	public int movesAgo = 0;
	public double fadeoffRate = 1.0;
	public boolean unsatisfiable = false;
	
	public void addSquare(int x, int y, int prob)
	{
		int abc[] = {x,y,prob};
		squares.add(abc);
	}

	public double metapositionSatisfiesConstraint(Metaposition m)
	{
		if (unsatisfiable) return 0.0;
		
		for (int k=0; k<squares.size(); k++)
		{
			int a[] = squares.get(k);
			int x = a[0]; int y = a[1];
			int piece = m.getFriendlyPiece(x, y);
			if (pieces[piece]) return 1.0;
		}
		if (movesAgo==0) return 0.0; else return (0.1*16.0/(m.pawnsLeft+m.piecesLeft+1)*fadeoffRate*movesAgo);
	}
	
	public double pieceRatio(int x, int y)
	{
		int total = 0;
		int mine = 0;
		
		for (int k=0; k<squares.size(); k++) 
		{
			total += squares.get(k)[2];
			if (squares.get(k)[0]==x && squares.get(k)[1]==y) mine = squares.get(k)[2];
		}
		return (1.0*mine/total);
	}
	
	public double pieceBonusWithConstraintVector(int x, int y, Vector<MetapositionConstraint> v)
	{
		double bonus = 1.0;
		
		for (int k=0; k<v.size(); k++)
		{
			MetapositionConstraint mc = v.get(k);
			if (mc==this) continue;
			
			bonus *= (1.0+mc.pieceRatio(x,y));
		}
		
		return bonus;
	}
	
	public int pieceType()
	{
		Random r = new Random();
		int k = 0;
		
		do
		{
			k = r.nextInt(7);
		} while (!pieces[k]);
		
		return k;
	}
	
}
