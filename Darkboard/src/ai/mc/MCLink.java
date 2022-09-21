package ai.mc;

import java.util.Random;

import core.Chessboard;
import core.Move;
import core.uberposition.Uberposition;

public class MCLink {
	
	public static final int ALT_ILLEGAL = 0;
	public static final int ALT_CAPTURE = 1;
	public static final int ALT_PAWN = 2;
	public static final int ALT_CHECK = 3;
	
	protected static Random r = new Random();
	
	public ai.mc.MCNode child;
	public Move move;
	ai.mc.MCNode parent;
	double heuristicBias = 0.0;
	double silentChance = 1.0;
	//MCNode[] alternatives = null; //nodes with non-silent messages
	double[] altChances = null;
	double[] altValues = null;
	public double theRestValue = 0.0;
	
	public MCLink (ai.mc.MCNode p)
	{
		parent = p;
	}
	
	public void setChild(ai.mc.MCNode ch)
	{
		child = ch;
		child.parent = parent;
		child.parentLink = this;
	}
	
	public void setMove(Move m)
	{
		move = m;
	}
	
	public boolean refined()
	{
		return (altChances!=null);
	}
	
	public void refine()
	{
		Uberposition s = (Uberposition)((ai.mc.MCStateNode)parent).state;
		double legal = s.chanceOfLegality(move);
		double pawnTries = s.chanceOfPawnTries(move);
		double cap = s.chanceOfCapture(move);
		double check = s.chanceOfCheck(move);
		
		
		silentChance = legal*(1.0-cap)*(1.0-pawnTries);
		heuristicBias = 0.0;
		
		//alternatives = new MCNode[3];
		altChances = new double[4];
		altValues = new double[4];
		
		if (silentChance==1.0)
		{
			theRestValue = 0.0;
			return;
		}
		
		int attackPower = s.attackPower(move.toX,move.toY);
		
		altChances[ALT_ILLEGAL] = 1.0-legal;
		altChances[ALT_CAPTURE] = legal*cap;
		altChances[ALT_PAWN] = legal*pawnTries;
		altChances[ALT_CHECK] = check;
		//silentChance = 1.0 - altChances[0] - altChances[1] - altChances[2];
		
		altValues[ALT_ILLEGAL] = parent.value-0.01;
		altValues[ALT_PAWN] = (move.piece!=Chessboard.PAWN || attackPower<1? -1.0 : parent.value-0.01);
			//-1.0;
		altValues[ALT_CAPTURE] = parent.value+/*(1.0-parent.value)*/ 50.0 *cap*cap/(s.pawnsLeft+s.piecesLeft+1.0)*(s.evaluateWarSaga(move)-0.1);
		if (altValues[ALT_CAPTURE]>1.0) altValues[ALT_CAPTURE] = 1.0;
		altValues[ALT_CHECK] = (attackPower<1? -1.0 : parent.value+0.01);
		theRestValue = 0.0;
		for (int k=0; k<4; k++) theRestValue += altValues[k]*altChances[k];
		theRestValue /= (altChances[0] + altChances[1] + altChances[2] + altChances[3]);
		if (theRestValue>1.0 || theRestValue<-1.0)
		{
			return;
		}
	}
	
	public boolean somethingElseHappens()
	{
		if (altChances==null) return false;
		else return (r.nextDouble()>silentChance);
	}
	
	

}
