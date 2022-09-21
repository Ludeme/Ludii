/*
 * Created on 6-dic-05
 *
 */
package database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import pgn.ExtendedPGNGame;
import umpire.local.LocalUmpire;

/**
 * @author Nikola Novarlic
 * This class contains piece density data on each square. There are
 * 256 arrays comprising 64 floats each (squares). The 256 arrays are
 * a 16x16 combination where the first number represents how many pieces
 * the player has left, and the second is for his opponent.
 */
public class PieceDensityInfo /*implements Serializable*/ {
	
	public float data[][][] = new float[16][16][64];
	
	private static int support[][][] = new int[16][16][64];
	private static int supporttot[][][] = new int[16][16][64];
	
	
	public void getFromDatabase(GameDatabase gd)
	{
		if (gd==null) return;
		
		LocalUmpire lu = new LocalUmpire(null,null);
		
		for (int k=0; k<16; k++) for (int j=0; j<16; j++) for (int z=0; z<64; z++)
			support[k][j][z] = supporttot[k][j][z] = 0;
		
		for (int k=gd.gameNumber()-1; k>=0; k--)
		{
			ExtendedPGNGame g = gd.getGame(k);
			lu.resetBoard();
			for (int c=0; c<g.getMoveNumber(); c++)
			{
				ExtendedPGNGame.PGNMoveData data = g.getMove(true,c);
				if (data!=null)
				{
					lu.doMove(data.finalMove,0); 
					examinePosition(lu,true);
				} else break;
				data = g.getMove(false,c);
				if (data!=null) 
				{
					lu.doMove(data.finalMove,1);
					examinePosition(lu,false);
				} else break;
			}
			if (k%100 == 99) System.out.print("*");
			
		}
		
		//now compute results through simple division...
		for (int my=0; my<16; my++)
			for (int opp=0; opp<16; opp++)
			{
				if (supporttot[my][opp][0]<1)
				{
					//this combination of friendly and enemy pieces never occurred, make it uniform
					for (int sq=0; sq<64; sq++) data[my][opp][sq] = 1.0f * (opp+1) / 64.0f;
					// System.out.println("Situation never occurred, "+my+"-"+opp);
				} else
				{
					for (int sq=0; sq<64; sq++) data[my][opp][sq] = 1.0f * support[my][opp][sq] / supporttot[my][opp][sq];
					
				}
			}
	}
	
	private void examinePosition(LocalUmpire lu, boolean white)
	{
		//count pieces left to either player
		int wp, bp;
		int who = (white? LocalUmpire.WHITE : LocalUmpire.BLACK);
		
		wp = lu.getPieceNumber(LocalUmpire.WHITE)-1;
		bp = lu.getPieceNumber(LocalUmpire.BLACK)-1;
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				int index;
				if (white)
				{
					index = k*8 + (7-j); //reverse y coord so we view it as Black pieces
					//we are modelling the OPPONENT, so if we are modelling White,
					//we do it from Black's point of view.
					supporttot[bp][wp][index]++; //our (black) pieces, opponent pieces, square
					if (lu.occupied(k,j,who))
						support[bp][wp][index]++;
				} else
				{
					index = k*8 + j;
					supporttot[wp][bp][index]++;
					if (lu.occupied(k,j,who))
						support[wp][bp][index]++;
				}
			}
	}
	
	public static PieceDensityInfo load(File f)
	{
		try
		{
			FileInputStream fis = new FileInputStream(f);
			ObjectInputStream ois = new ObjectInputStream(fis);
			PieceDensityInfo ob = (PieceDensityInfo)ois.readObject();
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

}
