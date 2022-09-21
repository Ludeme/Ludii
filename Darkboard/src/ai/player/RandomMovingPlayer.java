/*
 * Created on 20-apr-05
 *
 */
package ai.player;


import java.util.Random;
import java.util.Vector;

import core.Chessboard;
import core.Move;
import umpire.local.LocalUmpire;

/**
 * This is a dummy AI player that moves randomly. Used for benchmarking purposes.
 * @author Nikola Novarlic
 *
 */
public class RandomMovingPlayer extends Player {
	
	Random r;
	boolean w;
	int capx,capy,check1,check2,tries;
	
	public RandomMovingPlayer(boolean isWhite)
	{
		w = isWhite;
		
		r = new Random();
	}
	
	
	public Move getNextMove()
	{
		//Vector v = simplifiedBoard.generateMoves(true,this);
		//board.pseudolegalMoves(v,true);
		Vector<Move> v = ((LocalUmpire)currentUmpire).legalMoves(w);
		
		//pseudo-random player will be smarter now...
		Vector<Move> v2 = new Vector<Move>();
		for (int k=0; k<v.size(); k++)
		{
			Move m = (Move)v.get(k);
			if ((m.piece==Chessboard.PAWN && m.toX!=m.fromX) || (m.toX==capx && m.toY==capy))
				v2.add(m);
		}
		
		if (v2.size()==0)
			lastMove = (Move)v.get(r.nextInt(v.size()));
		else lastMove = (Move)v2.get(r.nextInt(v2.size()));
		
		return (lastMove);
	}
	
	public String communicateUmpireMessage(int capX, int capY, int tries, int check, int check2, int captureType)
	{	
		super.communicateUmpireMessage(capX, capY, tries, check, check2, captureType);
		capx=capX; capy=capY; this.tries=tries; this.check1=check; this.check2=check2;
		return "";
	}

}
