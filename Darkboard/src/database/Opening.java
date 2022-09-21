/*
 * Created on 3-ott-05
 *
 */
package database;

import java.io.Serializable;

import core.Chessboard;
import core.Move;
import pgn.ExtendedPGNGame;

/**
 * @author Nikola Novarlic
 *
 */
public class Opening extends MoveDatabase /*implements Serializable*/ {
	
	boolean white;
	
	public Opening(boolean w)
	{
		super();
		white = w;
	}
	
	public void extractFromGame(ExtendedPGNGame game)
	{
		boolean finished = false;
		int k=0;
		ExtendedPGNGame.PGNMoveData data,data1,data2;
		while(!finished)
		{
			data1 = game.getMove(true,k);
			data2 = game.getMove(false,k);
			k++;
			data = (white? data1 : data2);
			if (data==null) break;
			if (data.capturewhat!=Chessboard.NO_CAPTURE) break;
			if (data1!=null && !white) //we consider the opening void when something breaks it.
			{
				if (data1.capturewhat!=Chessboard.NO_CAPTURE) break;
				if (data1.check1!=Chessboard.NO_CHECK) break;
			}
			if (data!=null)
			{
				this.addMove(data.finalMove);
				
			} else finished = true;
			if (data1!=null) //we consider the opening void when something breaks it.
			{
				if (data1.capturewhat!=Chessboard.NO_CAPTURE) break;
				if (data1.check1!=Chessboard.NO_CHECK) break;
			}
			if (data2!=null) //we consider the opening void when something breaks it.
			{
				if (data2.capturewhat!=Chessboard.NO_CAPTURE) break;
				if (data2.check1!=Chessboard.NO_CHECK) break;
			}
		}
	}
	
	/**
	 * 
	 * @param o
	 * @param subset - if true, we check that o is a subset of this, if false, the other way around.
	 * @return
	 */
	boolean isOpeningSubset(Opening o, boolean subset)
	{
		if (isWhite()!=o.isWhite()) return false;
		for (int k=0; k<this.getMoveNumber(); k++)
		{
			Move m1 = this.getMove(k);
			Move m2 = o.getMove(k);
			if (m1==null || m2==null || !m1.equals(m2)) return false;
		}
		if (subset)
		{
			return (o.getMoveNumber()<this.getMoveNumber());
		} else
		{
			return (o.getMoveNumber()>=this.getMoveNumber());
		}
	}

	/**
	 * @return
	 */
	public boolean isWhite() {
		return white;
	}

	/**
	 * @param b
	 */
	public void setWhite(boolean b) {
		white = b;
	}
	


}
