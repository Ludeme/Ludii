package ai.player;

import java.util.Random;
import java.util.Vector;

import ai.chunk.Chunk;
import ai.chunk.ChunkMaker;
import ai.chunk.ChunkTable.ChunkTableEntry;
import core.Chessboard;
import core.Globals;
import core.Metaposition;
import core.Move;
import umpire.local.LocalUmpire;

public class MimicDarkboard extends Darkboard {
	
	private Random rand = new Random();
	private String mimicry = "";
	
	public class ChunkOccurrence
	{
		public Metaposition start;
		public Chunk what;
		int x = -1;
		int y = -1;
		
		public Move getMoveForExit(int k, boolean w)
		{
			return what.getMoveForExit(k, x, y, w);
		}
	}
	
	Vector<Chunk> availableChunks = new Vector();
	
	public MimicDarkboard(boolean white, Vector<Chunk> mimic, String pl)
	{
		super(white);
		for (int k=0; k<mimic.size(); k++) availableChunks.add(mimic.get(k)); //use your own copy
		mimicry = pl;
		System.out.println("MimicDarkboard loaded with "+availableChunks.size()+" chunks.");
		this.playerName = "MimicDarkboard ("+mimicry+")";
	}
	
	public MimicDarkboard(boolean white, String pl)
	{
		super(white);
		
		ChunkTableEntry cte = Globals.chunks.lookup(pl);
		
		if (cte!=null)
		{
			mimicry = pl;
			Vector<Chunk> mimic = cte.chunks;
			for (int k=0; k<mimic.size(); k++) availableChunks.add(mimic.get(k)); //use your own copy
			System.out.println("MimicDarkboard loaded with "+availableChunks.size()+" chunks.");
			this.playerName = "MimicDarkboard ("+mimicry+")";
		}
	}
	
	public Vector<ChunkOccurrence> findChunkOccurrences(Metaposition m)
	{
		Vector<ChunkOccurrence> v = new Vector();
		
		Chunk chunks[][] = chunkifyMetaposition(m);
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				Chunk c = chunks[k][j];
				if (c!=null)
				{
					//find the boundaries for the chunk
					int minx = k;
					int miny = j;
					for (int x=0; x<8; x++)
						for (int y=0; y<8; y++)
						{
							if (chunks[x][y]==c)
							{
								chunks[x][y] = null;
								if (x<minx) minx=x;
								if (y<miny) miny=y;
							}
				
						}
					Chunk databaseChunk = ChunkMaker.chunkExists(c, availableChunks);
					if (databaseChunk==null) break; //too bad, no chunk
					
					ChunkOccurrence co = new ChunkOccurrence();
					co.start = m;
					//co.what = c;
					co.what = databaseChunk;
					co.x = minx; co.y = miny;
					// System.out.println(co.what);
					v.add(co);
				}
			}
		return v;
	}
	
	public Chunk[][] chunkifyMetaposition(Metaposition m)
	{
		int board[][] = new int[8][8];
		Chunk out[][] = new Chunk[8][8];
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				int piece = m.getFriendlyPiece(k, j);
				int piece2 = LocalUmpire.EMPTY;
				switch (piece)
				{
				case Chessboard.PAWN: piece2 = (m.isWhite()? LocalUmpire.WP : LocalUmpire.BP); break;
				case Chessboard.KNIGHT: piece2 = (m.isWhite()? LocalUmpire.WN : LocalUmpire.BN); break;
				case Chessboard.BISHOP: piece2 = (m.isWhite()? LocalUmpire.WB : LocalUmpire.BB); break;
				case Chessboard.ROOK: piece2 = (m.isWhite()? LocalUmpire.WR : LocalUmpire.BR); break;
				case Chessboard.QUEEN: piece2 = (m.isWhite()? LocalUmpire.WQ : LocalUmpire.BQ); break;
				case Chessboard.KING: piece2 = (m.isWhite()? LocalUmpire.WK : LocalUmpire.BK); break;
				}
				board[k][j] = piece2;
			}
		
		ChunkMaker.clearOldBoard();
		/*Vector<Chunk> v = */ ChunkMaker.chunkify(board, m.isWhite());
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
				out[k][j] = ChunkMaker.oldBoard[k][j];
		
		return out;
	}
	
	public Vector<ChunkOccurrence> findBiggestChunks(Vector<ChunkOccurrence> in)
	{
		int biggest = 0;
		
		for (int k=0; k<in.size(); k++)
		{
			int s = in.get(k).what.pieceSize();
			if (s>biggest) biggest = s;
		}
		
		Vector<ChunkOccurrence> out = new Vector();
		
		for (int k=0; k<in.size(); k++)
		{
			if (in.get(k).what.pieceSize()==biggest) out.add(in.get(k));
		}
		
		return out;
	}
	
	public Vector<ChunkOccurrence> findChunksWithPseudolegalMove(Vector<ChunkOccurrence> in)
	{
		Vector<ChunkOccurrence> out = new Vector();
		
		Vector<Move> pseudo = this.simplifiedBoard.generateMoves(true, this);
		
		for (int k=0; k<in.size(); k++)
		{
			ChunkOccurrence co = in.get(k);
			int total = 0;
			for (int j=0; j<co.what.getExitNumber(); j++)
			{
				Move m = co.getMoveForExit(j, this.isWhite);
				for (int t=0; t<pseudo.size(); t++)
					if (m.equals(pseudo.get(t)))
					{
						total++;
						break;
					}
			}
			if (total>0) out.add(co);
		}
		
		return out;
	}
	
	public Move getBestChunkExit(Vector<ChunkOccurrence> in)
	{
		Vector<ChunkOccurrence> step1 = findChunksWithPseudolegalMove(in);
		Vector<ChunkOccurrence> step2 = findBiggestChunks(step1);
		
		if (step2.size()<1) return null;
		
		//find a random chunk...
		int index = rand.nextInt(step2.size());
		ChunkOccurrence co = step2.get(index);
		
		//now find its best exit
		Vector<Integer> v = new Vector();
		Vector<Move> pseudo = this.simplifiedBoard.generateMoves(true, this);
		
		for (int j=0; j<co.what.getExitNumber(); j++)
		{
			Move m = co.getMoveForExit(j, this.isWhite);
			for (int t=0; t<pseudo.size(); t++)
				if (m.equals(pseudo.get(t)))
				{
					v.add(new Integer(j));
					break;
				}
		}
		
		//now we have all pseudolegal exits... for now just pick the first (most common)
		return co.getMoveForExit(v.get(0).intValue(),isWhite);
	}
	
	public Move getNextMove()
	{
		if ((simplifiedBoard.pawnsLeft+simplifiedBoard.piecesLeft<=5) || this.tries>0 || this.check1!=Chessboard.NO_CHECK
				|| this.capx>=0)
		{
			// System.out.println("Mimic surrendering control to Darkboard.");
			return super.getNextMove();
		}
		Move mv = getBestChunkExit(findChunkOccurrences(simplifiedBoard));
		if (mv!=null)
		{
			// System.out.println("Mimic found move: "+mv);
			lastMove = mv;
			return mv;
		} else 
		{
			// System.out.println("Mimic found no suitable move.");
			return super.getNextMove();
		}
		
		
	}

}
