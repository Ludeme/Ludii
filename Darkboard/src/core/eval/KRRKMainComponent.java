package core.eval;

import java.util.Vector;

import core.Chessboard;
import core.EvaluationFunctionComponent;
import core.Metaposition;
import core.Move;

public class KRRKMainComponent extends EvaluationFunctionComponent {
	
	boolean rows[] = new boolean[8];
	boolean columns[] = new boolean[8];
	
	boolean attackerrows[] = new boolean[8];
	boolean attackercolumns[] = new boolean[8];
	
	public float evaluate(Metaposition start, Metaposition dest, Move m, Vector history)
	{
		float result = 0.0f;
		
		for (int k=0; k<8; k++) rows[k] = columns[k] = attackerrows[k] = attackercolumns[k] = false;
		
		int mateDetector = KRKMainComponent.stalemateAlert(start,m,dest);
		if (mateDetector<0) result -= 10000;
		
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				int p = dest.getFriendlyPiece(k,j);
				if (p == Chessboard.ROOK || p == Chessboard.QUEEN)
				{
					if (rookUnderAttack(dest,k,j)==0) result-=10000.0f;
					attackerrows[k]=true;
					attackercolumns[j]=true;
				}
			}
		if (result==0 && mateDetector>0) result+=10000;
		
		result+=columnBonus(dest);
		
		
		
		return result;
		
	}
	
	private int columnBonus(Metaposition m)
	{
		int result = 0;
		
		
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				if (m.canContainEnemyKing((byte)k,(byte)j))
				{
					rows[k] = true;
					columns[j] = true;
					result--;
					if (attackerrows[k] && k!=0 && k!=7 && !attackerrows[k-1] && !attackerrows[k+1]) result-=5;
					if (attackercolumns[k] && k!=0 && k!=7 && !attackercolumns[k-1] && !attackercolumns[k+1]) result-=5;
				}
			}
		
		int bestStreak = 0;
		boolean sequence[];
		for (int k=0; k<4; k++)
		{
			int streak = 0;
			if (k<2) sequence = rows; else sequence = columns;
			if ((k%2)==0)
			{
				for (int f=0; f<8; f++) if (!sequence[f]) streak++; else break;
			} else
			{
				for (int f=7; f>=0; f--) if (!sequence[f]) streak++; else break;
			}
			if (streak>bestStreak) bestStreak=streak;
		}
		result+=(bestStreak)*12;
		return result;
	}
	
	private int rookUnderAttack(Metaposition m, int rookx, int rooky)
	{
		if (m.owner.globals.protectionMatrix[rookx][rooky]>0) return 1;
		
		for (int k=rookx-1; k<=rookx+1; k++)
			for(int j=rooky-1; j<=rooky+1; j++)
			{
				if (k<0 || k>7 || j<0 || j>7) continue;
				if (k==rookx && j==rooky) continue;
				if (m.canContain((byte)k,(byte)j,(byte)Chessboard.KING)) return 0;
			}
			
		return 1;
	}
	

}
