package ai.opponent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;

import ai.opening.OpeningTreeNode;
import ai.player.Darkboard;
import ai.player.Player;
import core.Chessboard;
import core.Globals;
import core.Move;
import database.OpeningBook;
import pgn.ExtendedPGNGame;
import pgn.ExtendedPGNGame.PGNMoveData;
import umpire.local.StepwiseLocalUmpire;


public class OpponentProfile /*implements Serializable*/ {
	public static Hashtable<String, OpponentProfile> profiles = new Hashtable<String, OpponentProfile>();
	
	public String name;
	public String where; //es. "icc", "local"
	
	public static int MAX_OPENING_MOVES = 12;
	
	public OpeningBook whiteBook, blackBook;
	
	public float piecePresenceWhite[][][][];
	public float piecePresenceBlack[][][][];
	
	public int whitesize, blacksize = 0;
	
	public OpeningTreeNode openingBookWhite = null;
	public OpeningTreeNode openingBookBlack = null;
	public OpeningTreeNode customStrategyWhite = null; //play these openings against this player
	public OpeningTreeNode customStrategyBlack = null;
	
	public OpponentProfile()
	{
		
	}
	
	public OpponentProfile(String n, String w)
	{
		name = n;
		where = w;
		whiteBook = new OpeningBook();
		blackBook = new OpeningBook();
		
		piecePresenceWhite = new float[8][8][7][MAX_OPENING_MOVES];
		piecePresenceBlack = new float[8][8][7][MAX_OPENING_MOVES];

		openingBookWhite = new OpeningTreeNode();
		openingBookBlack = new OpeningTreeNode();
		customStrategyWhite = new OpeningTreeNode();
		customStrategyBlack = new OpeningTreeNode();
	}
	
	public static OpponentProfile getProfile(String n)
	{
		n = n.toLowerCase();
		OpponentProfile op = profiles.get(n);		
		return op;
	}
	
	public static OpponentProfile load(File f)
	{
		try
		{
			// fis.close();
			return new OpponentProfile();
		} catch (Exception e) {e.printStackTrace(); return null; }
	}
	
	public void save(File f)
	{
		try
		{
			FileOutputStream fos = new FileOutputStream(f);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(name);
			oos.writeObject(where);
			oos.writeObject(new Integer(whitesize));
			oos.writeObject(new Integer(blacksize));
			oos.writeObject(whiteBook);
			oos.writeObject(blackBook);
			oos.writeObject(piecePresenceWhite);
			oos.writeObject(piecePresenceBlack);
			oos.writeObject(openingBookWhite);
			oos.writeObject(openingBookBlack);
			oos.writeObject(customStrategyWhite);
			oos.writeObject(customStrategyBlack);
			oos.close();
			fos.close(); 
		} catch (Exception e) {e.printStackTrace(); }
	}
	
	public void save()
	{
		save(new File(Globals.PGNPath+"/profiles/"+name));
	}
	
	public void updateWithNewGame(ExtendedPGNGame game, float weight)
	{
		int white = -1;
		if (game.getWhite().toLowerCase().equals(name.toLowerCase())) white = 0;
		else if (game.getBlack().toLowerCase().equals(name.toLowerCase())) white = 1;
		else return;
		
		if (white==0) openingBookWhite.addGame(game, true, 12, true);
		else openingBookBlack.addGame(game, false, 12, true);
		
		int size = (white==0? whitesize : blacksize);
		
		float updateArray[][][][] = (white==0? piecePresenceWhite : piecePresenceBlack);
		
		if (weight==0.0f) weight = 1.0f / (1.0f+size);
		
		StepwiseLocalUmpire slu = new StepwiseLocalUmpire(new Player(), new Player());
		slu.stepwiseInit(null,null);
		
		for (int k=0; k<MAX_OPENING_MOVES; k++)
		{
			if (k>=game.getMoveNumber()) break;
			for (int m=0; m<2; m++)
			{
				PGNMoveData data = game.getMove(m==0, k);
				if (data==null) break;
				Move fin = data.finalMove;
				if (fin==null) break;
				slu.stepwiseArbitrate(fin);
				if (m==white)
				{
					//record data...
					for (int x = 0; x<8; x++)
						for (int y=0; y<8; y++)
						{
							if (!slu.pieceBelongsToPlayer(slu.board[x][y],white)) continue;
							int content = slu.umpire2ChessboardPieceCode(slu.board[x][y]);
							for (int aaa=0; aaa<7; aaa++) 
								updateArray[x][y][aaa][k] = weight*(aaa==content? 1.0f : 0.0f) + (1.0f-weight)*updateArray[x][y][aaa][k];
						}
					
					
				}
			}
			
		}
		
		if (white==0) whitesize++; else blacksize++;
	}
	
	public void printValue(int piece, int move)
	{
		for (int k=0; k<8; k++)
		{
			for (int j=0; j<8; j++)
			{
				float value = piecePresenceWhite[j][7-k][piece][move];
				System.out.print(value +" ");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public void printGradient(int piece, int move, int delta)
	{
		for (int k=0; k<8; k++)
		{
			for (int j=0; j<8; j++)
			{
				float value = piecePresenceWhite[j][7-k][piece][move] - piecePresenceWhite[j][7-k][piece][move-delta];
				System.out.print(value +" ");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public static void main(String args[])
	{
		String path = System.getProperty("user.home") + "/darkboard_data/";
		// System.out.println(path);
		Darkboard.initialize(path);
		/*
		OpponentProfile p = getProfile("paoloc", null);
		p.printValue(Chessboard.KNIGHT,10);
		p.printGradient(Chessboard.PAWN,10,1);
		p.printGradient(Chessboard.BISHOP,10,1);
		p.printGradient(Chessboard.KNIGHT,10,1);
		p.printGradient(Chessboard.ROOK,10,1);
		p.printGradient(Chessboard.QUEEN,10,1);
		p.printGradient(Chessboard.KING,10,1);
		*/
	}

}
