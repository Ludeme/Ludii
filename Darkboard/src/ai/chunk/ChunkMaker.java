package ai.chunk;

import java.io.File;
import java.util.Vector;

import ai.player.Darkboard;
import ai.player.MimicDarkboard;
import ai.player.Player;
import core.Chessboard;
import core.Globals;
import core.Move;
import database.GameDatabase;
import pgn.ExtendedPGNGame;
import tester.Tester;
import umpire.local.LocalUmpire;
import umpire.local.StepwiseLocalUmpire;

public class ChunkMaker {
	
	private static final int kqbTemplate[][] = {{1,2,4},{128,0,8},{64,32,16}};
	private static final int wpTemplate[][] = {{0,2,4},{128,0,8},{0,32,16}};
	private static final int bpTemplate[][] = {{1,2,0},{128,0,8},{64,32,0}};
	private static final int rnTemplate[][] = {{0,2,0},{128,0,8},{0,32,0}};
	
	public static final Chunk oldBoard[][] = new Chunk[8][8];
	
	private static final int templateChoice[][][] = {kqbTemplate,kqbTemplate,rnTemplate,kqbTemplate,rnTemplate,bpTemplate,null,
			wpTemplate,rnTemplate,kqbTemplate,rnTemplate,kqbTemplate,kqbTemplate};
	
	public static void clearOldBoard()
	{
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
				oldBoard[k][j] = null;
	}
	
	public static Vector<Chunk> makeChunks(String player, GameDatabase db)
	{
		int games = 0;
		
		Vector<Chunk> result = new Vector();
		for (int k=0; k<db.gameNumber(); k++)
		{
			ExtendedPGNGame pgn = db.getGame(k);
			if (!pgn.getWhite().toUpperCase().equals(player.toUpperCase()) && !pgn.getBlack().toUpperCase().equals(player.toUpperCase())) continue;
			games++;
			Vector<Chunk> chunks = chunkifyGame(pgn,pgn.getWhite().toUpperCase().equals(player.toUpperCase()));
			for (int k1=0; k1<chunks.size(); k1++)
			{
				Chunk c = chunks.get(k1);
				Chunk c1 = chunkExists(c,result);
				if (c1!=null) c1.integrateChunk(c);
				else result.add(c);
			}
		}
		// System.out.println("GAMES -> "+games);
		return result;
	}
	
	public static Chunk chunkExists(Chunk c, Vector<Chunk> v)
	{
		for (int k=0; k<v.size(); k++)
			if (c.equals(v.get(k))) return v.get(k);
		return null;
	}
	
	public static Vector<Chunk> chunkifyGame(ExtendedPGNGame pgn, boolean w)
	{
		Vector<Chunk> v = new Vector();
		Darkboard p1= new Darkboard(true);
		Darkboard p2 = new Darkboard(false);
		Chunk myboard[][] = new Chunk[8][8];
		Chunk temp;
		
		StepwiseLocalUmpire slu = new StepwiseLocalUmpire(p1,p2);
		slu.verbose = false;
		try
		{
			slu.stepwiseInit(null,null);
		} catch(Exception e) {
			e.printStackTrace();
			return v;
		}
		
		boolean victory = ((w && pgn.getResult().equals("1-0")) || (!w && pgn.getResult().equals("0-1")));
		int pl = (w? 0 : 1);
		
		clearOldBoard();
		
		int board[][] = new int[8][8];
		for (int x=0; x<8; x++)
			for (int y=0; y<8; y++)
				board[x][y] = slu.board[x][y];
		
		Vector<Chunk> initial = chunkify(board,w);
		v.add(initial.get(0));
		
		for (int k=0; k<pgn.getMoveNumber(); k++)
		{
			for (int turn=0; turn<2; turn++)
			{
				Darkboard p = (turn==0? p1:p2);
				ExtendedPGNGame.PGNMoveData move = pgn.getMove(turn==0,k);
				if (move==null) break;
				Move fin = move.finalMove;
				if (fin==null) break;
				p.lastMove = fin;
				boolean consider = (slu.capture==Chessboard.NO_CAPTURE && 
						(slu.tries==0 || fin.piece!=Chessboard.PAWN || fin.toX==fin.toY) && slu.check[0]==Chessboard.NO_CHECK);
				if (turn==pl && consider && oldBoard[fin.fromX][fin.fromY]!=null)
				{
					temp = chunkExists(oldBoard[fin.fromX][fin.fromY],v);
					if (temp!=null)
					{
						//find lowest left corner for the chunk...
						int x, y;
						x = y = 8;
						for (int t=0; t<8; t++)
							for (int o=0; o<8; o++)
							{
								if (oldBoard[t][o]==oldBoard[fin.fromX][fin.fromY])
								{
									if (t<x) x=t; if (o<y) y=o;
								}
							}
						temp.addExitOccurrence(fin.fromX-x, fin.fromY-y, fin.toX-fin.fromX, fin.toY-fin.fromY, w);
					}
				} else temp = null;
				
				slu.stepwiseArbitrate(fin);
				
				
				for (int x=0; x<8; x++)
					for (int y=0; y<8; y++)
						board[x][y] = slu.board[x][y];
				
				Vector<Chunk> c = chunkify(board,w);
				
				if (turn!=pl) continue;

				for (int j=0; j<c.size(); j++)
				{
					Chunk ck = c.get(j);
					ck.setFirstMove(k); ck.setLastMove(k);
					ck.setOccurrences(ck.getOccurrences()+1);
					int material = p.simplifiedBoard.pawnsLeft + p.simplifiedBoard.piecesLeft;
					ck.setOccurrenceForMaterial(material, ck.getOccurrenceForMaterial(material)+1);
					
					Chunk ck2 = chunkExists(ck,v);
					if (ck2!=null)
						ck2.integrateChunk(ck);
					else v.add(ck);
				}
				
			}
		}
		
		for (int k=0; k<v.size(); k++)
		{
			Chunk c = v.get(k);
			c.setTotalGames(c.getTotalGames()+1);
			if (victory) c.setVictories(c.getVictories()+1);
		}
		
		return v;
	}
	
	/**
	 * Subdivides a board (using LocalUmpire piece codes) into chunks.
	 * @param board Board data, lost in the operation
	 * @param white Which player is being studied
	 * @return
	 */
	public static Vector<Chunk> chunkify(int board[][], boolean white)
	{
		Vector<Chunk> v = new Vector();
		clearOldBoard();
		if (board==null || board.length!=8 || board[0].length!=8) return v;
		
		int canvas[][] = new int[8][8];
		
		for (int k=0; k<8; k++)
			for (int h=0; h<8; h++)
			{
				switch (board[h][k])
				{
				case LocalUmpire.EMPTY: board[h][k]=0; break;
				case LocalUmpire.WP: board[h][k] = (white? 1 : 0); break;
				case LocalUmpire.WN: board[h][k] = (white? 2 : 0); break;
				case LocalUmpire.WB: board[h][k] = (white? 3 : 0); break;
				case LocalUmpire.WR: board[h][k] = (white? 4 : 0); break;
				case LocalUmpire.WQ: board[h][k] = (white? 5 : 0); break;
				case LocalUmpire.WK: board[h][k] = (white? 6 : 0); break;
				case LocalUmpire.BP: board[h][k] = (!white? -1 : 0); break;
				case LocalUmpire.BN: board[h][k] = (!white? -2 : 0); break;
				case LocalUmpire.BB: board[h][k] = (!white? -3 : 0); break;
				case LocalUmpire.BR: board[h][k] = (!white? -4 : 0); break;
				case LocalUmpire.BQ: board[h][k] = (!white? -5 : 0); break;
				case LocalUmpire.BK: board[h][k] = (!white? -6 : 0); break;
				}
			}
		
		//Perform convolution using the template for the given piece...
		for (int k=0; k<8; k++)
			for (int h=0; h<8; h++)
			{
				if (board[k][h]!=0)
				{
					int template[][] = templateChoice[board[k][h]+6];
					
					for (int x=-1; x<=1; x++)
						for (int y=-1; y<=1; y++)
						{
							int dx = k+x; int dy = h+y;
							if (dx>=0 && dy>=0 && dx<8 && dy<8 && board[dx][dy]!=0)
							{
								if (template[x+1][y+1]!=0)
								{
									canvas[k][h] |= template[x+1][y+1];
									canvas[dx][dy] |= kqbTemplate[-x+1][-y+1];
								}
							}
						}
					
				}
			}
		
		Vector<int[]> chunk = new Vector();
		Vector<int[]> queue = new Vector();
		
		for (int k=0; k<8; k++)
			for (int h=0; h<8; h++)
			{
				if (canvas[k][h]!=0)
				{
					//found new chunk
					int c[] = {k,h,board[k][h]};
					queue.add(c);
					while (queue.size()!=0)
					{
						int square[] = queue.remove(0);
						if (canvas[square[0]][square[1]]!=0)
						{
							chunk.add(square);
							//System.out.println("EXT FROM "+square[0]+" "+square[1]+" "+square[2]);
							for (int dir=1; dir<=128; dir*=2)
							{
								if ((canvas[square[0]][square[1]]&dir)!=0)
								{
									//keep searching in that direction
									int dx = square[0] + (dir<=4? -1 : dir==8 || dir==128? 0 : 1);
									int dy = square[1] + (dir==1 || dir>=64? -1 : dir==2 || dir==32? 0 : 1);
									//System.out.println("EXT TO "+dx+" "+dy);
									if (dx>=0 && dy>=0 && dx<8 && dy<8 && canvas[dx][dy]!=0)
									{
										int add[] = {dx,dy,board[dx][dy]}; queue.add(add);
									}
								}
							}
						}
						canvas[square[0]][square[1]]=0;
					}
					//chunk fully isolated, its squares are in 'chunk'. Now make the object and put it in the vector
					Chunk chn = new Chunk(chunk,white);
					
					for (int i=0; i<chunk.size(); i++)
						oldBoard[chunk.get(i)[0]][chunk.get(i)[1]] = chn; //remember which chunk was where, will come in handy
					
					v.add(chn);
					chunk.clear();
				}
			}
		
		/*for (int h=7; h>=0; h--)
		{
			for (int k=0; k<8; k++) System.out.print(canvas[k][h]+" ");
			System.out.println();
		}*/
		
		return v;
	}
	
	public class MimicTester extends Tester
	{
		public Player getPlayer(int index, boolean white)
		{
			if (index==0)
				return new MimicDarkboard(white,"paoloc");
			else return new Darkboard(white);
		}
	}
	
	public static void main(String args[])
	{
		//if (args.length<=0) return; 
		
		//Darkboard.initialize(args[0]);

		String path = System.getProperty("user.home") + "/"+args[0]+"/";
		// System.out.println(path);
		Darkboard.initialize(path);
		
		String s = Globals.PGNPath+"databasecream.pgn";
		// System.out.println(s);
		GameDatabase gd = new GameDatabase();
		gd.addAllGames(new File(s));
		Vector<Chunk> v = makeChunks("paoloc",gd);
		Globals.chunks.add("paoloc", v);
		gd = null;
		/*int withExits = 0;
		for (int k=0; k<v.size(); k++) 
		{
			//if (v.get(k).getTotalGames()>1)
			//{
				if (v.get(k).getExitNumber()>0) withExits++; else continue;
				System.out.println(v.get(k).toString());
				System.out.println("OCC: "+v.get(k).getOccurrences());
				System.out.print("-> ");
				for (int k2=0; k2<16; k2++) System.out.print(v.get(k).getOccurrenceForMaterial(k2)+" ");
				System.out.println();
				System.out.println("GAM: "+v.get(k).getTotalGames());
				System.out.println("VIC: "+v.get(k).getVictories());
				System.out.println("MV: "+v.get(k).getFirstMove()+"-"+v.get(k).getLastMove());
				System.out.println("EXITS:");
				for (int o=0; o<v.get(k).getExitNumber(); o++)
				{
					int pr[] = v.get(k).getExit(o);
					System.out.println("-> "+pr[0]+" "+pr[1]+" "+pr[2]+" "+pr[3]+" "+pr[4]);
				}
				System.out.println();
			//}
		}
		System.out.println("CHUNKS: "+v.size());
		System.out.println("WITH EXITS: "+withExits);
		
		OnePlayerFrame f = new OnePlayerFrame(new HumanPlayer(true),new MimicDarkboard(false,"paoloc"),null,null);
		
		while (f.umpire.getGameOutcome()==LocalUmpire.NO_OUTCOME)
		{
			Player p = f.umpire.turn();
			//f.interrogatePlayer(t==0);
			Move m = p.getNextMove();
			f.umpire.stepwiseArbitrate(m);
		}*/
		
		new ChunkMaker().new MimicTester().test(-1, 1);
		
		
	}

}
