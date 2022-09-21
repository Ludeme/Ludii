/*
 * Created on 30-set-05
 *
 */
package database;

import java.util.Vector;

import pgn.ExtendedPGNGame;

/**
 * @author Nikola Novarlic
 *
 */
public class DatabasePlayer {
	
	public static final int PLAYER_WIN = 0;
	public static final int PLAYER_DRAW = 1;
	public static final int PLAYER_LOSS = 2;
	
	
	String name="";
	int gamesPlayed=0;
	int wins=0;
	int draws=0;
	int losses=0;
	int white=0;
	int black=0;
	
	Vector games;
	
	public DatabasePlayer(String s) { name = s; games = new Vector(); }
	
	public double internalRating()
	{
		if (gamesPlayed>0) return (1.0*wins/gamesPlayed)*wins;
		else return 0.0;
	}
	
	public void addGame(ExtendedPGNGame pgn)
	{
		games.add(pgn);
		gamesPlayed++;
		if (pgn.getWhite().equals(name))
		{
			white++;
			if (pgn.getResult().equals("1-0")) wins++;
			if (pgn.getResult().equals("0-1")) losses++;
		} else
		{
			black++;
			if (pgn.getResult().equals("1-0")) losses++;
			if (pgn.getResult().equals("0-1")) wins++;
		}
		if (pgn.getResult().equals("1/2-1/2")) draws++;
	}
	
	public String toString()
	{
		return (name+"\t\t\t"+gamesPlayed+" games, "+white+"W "+black+"B, "+wins+"/"+draws+"/"+losses
		+" [Rat "+(int)internalRating()+"]");
	}
	
	
	//info extracting methods
	public database.MoveDatabase generateFirstMoveMap(int moves)
	{
		database.MoveDatabase m = new database.MoveDatabase();
		
		for (int k=0; k<games.size(); k++)
		{
			ExtendedPGNGame game = (ExtendedPGNGame)games.get(k);
			if (game.getBlack().equals(name)) continue; //only White, for now...
			m.gameSize++;
			for (int i=0; i<moves; i++)
			{
				ExtendedPGNGame.PGNMoveData mov = game.getMove(true,i);
				if (mov!=null)
				{
					m.addMove(mov.finalMove);
					m.getData(mov.finalMove).addCount(i+1);
				}
				
			}
		}
		m.sortDatabase();
		return m;
	}
	
	public database.OpeningBook generateOpeningBook(boolean white, int minLength, database.OpeningBook start)
	{
		database.OpeningBook ob = (start!=null? start : new database.OpeningBook());
		
		for (int k=0; k<this.games.size(); k++)
		{
			ExtendedPGNGame game = (ExtendedPGNGame)games.get(k);
			if ((game.isPlayerWhite(name)&&white) || (!game.isPlayerWhite(name) && !white))
			{
				database.Opening o = new database.Opening(white);
				o.extractFromGame(game);
				if (o.getMoveNumber()>=minLength)
					ob.addOpening(o);
			}
			
		}
		
		return ob;
	}

}
