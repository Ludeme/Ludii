package database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Vector;

import core.Chessboard;
import pgn.ExtendedPGNGame;
import umpire.local.LocalUmpire;

public class PlayerModel /*implements Serializable */ {
	
	public class PieceDistribution implements Serializable
	{
		public double quantities[][][] = new double[8][8][7];
		public int samples = 0;
		
		public String toString()
		{
			String s = "Samples "+samples+"\n";
			for (int k=0; k<8; k++)
				for (int j=0; j<8; j++)
				{
					for (int l=0; l<7; l++) s+= quantities[k][j][l]+" ";
					s+="\n";
				}
			return s;
		}
	}
	
	Vector<PieceDistribution> distributions = new Vector<PieceDistribution>();
	
	public void getFromDatabase(GameDatabase gd, boolean white, String player, int limit)
	{
		if (gd==null) return;
		
		LocalUmpire lu = new LocalUmpire(null,null);
		
		for (int k=gd.gameNumber()-1; k>=0; k--)
		{
			ExtendedPGNGame g = gd.getGame(k);
			lu.resetBoard();
			String res = g.getResult();
			if (res.equals("1-0") && !white) continue;
			if (res.equals("0-1") && white) continue;
			for (int c=0; c<g.getMoveNumber(); c++)
			{
				if (c>=limit) break;
				ExtendedPGNGame.PGNMoveData data = g.getMove(true,c);
				if (data!=null)
				{
					if (c>=distributions.size())
					{
						distributions.add(new PieceDistribution());
					}
					lu.doMove(data.finalMove,0); 
					if (white && (player==null || player.equals(g.getWhite()))) examinePosition(lu,true,distributions.get(c));
				} else break;
				data = g.getMove(false,c);
				if (data!=null) 
				{
					lu.doMove(data.finalMove,1);
					if (!white && (player==null || player.equals(g.getBlack()))) examinePosition(lu,false,distributions.get(c));
				} else break;
			}
			if (k%100 == 99) System.out.print("*");
			
		}
		
		//now compute results through simple division...
		for (int k=0; k<distributions.size(); k++)
		{
			PieceDistribution pd = distributions.get(k);
			if (pd.samples<1) continue;
			for (int j=0; j<8; j++) for (int s=0; s<8; s++)
				for (int l=0; l<7; l++) pd.quantities[j][s][l] /= pd.samples;
		}
		
	}
	
	private void examinePosition(LocalUmpire lu, boolean white, PieceDistribution pd)
	{
		//count pieces left to either player
		int wp, bp;
		int who = (white? LocalUmpire.WHITE : LocalUmpire.BLACK);
		
		wp = lu.getPieceNumber(LocalUmpire.WHITE)-1;
		bp = lu.getPieceNumber(LocalUmpire.BLACK)-1;
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				if (lu.occupied(k,j,who))
				{
					int pc = lu.umpire2ChessboardPieceCode(lu.board[k][j]);
					pd.quantities[k][j][pc] += 1.0;
				} else pd.quantities[k][j][Chessboard.EMPTY] += 1.0;
			}
		
		pd.samples++;
	}
	
	public static PlayerModel load(File f)
	{
		try
		{
			FileInputStream fis = new FileInputStream(f);
			ObjectInputStream ois = new ObjectInputStream(fis);
			PlayerModel ob = (PlayerModel)ois.readObject();
			ois.close();
			fis.close();
			return ob;
		} catch (Exception e) {e.printStackTrace(); return null; }
	}
	
	public void save(File f)
	{
		try
		{
			FileOutputStream fos = new FileOutputStream(f);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(this);
			oos.close();
			fos.close(); 
		} catch (Exception e) {e.printStackTrace(); }
	}
	
	public String toString()
	{
		if (distributions.size()<1) return "";
		return distributions.get(0).toString();
	}
	
	public PieceDistribution get(int k)
	{
		if (k<0) k = 0;
		if (k>=distributions.size()) k = distributions.size() - 1;
		if (distributions.size()==0) return null; else return distributions.get(k);
	}

}
