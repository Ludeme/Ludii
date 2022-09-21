package core.uberposition;

import java.util.Enumeration;
import java.util.Properties;

import ai.mc.MCState;
import ai.player.Darkboard;
import ai.player.Player;
import core.Chessboard;
import core.EvaluationGlobals;
import core.Move;

public class Uberposition implements MCState {
	
	//make copies of these for each thread!
	private static int kingMatrix[][][] = new int[1][8][8];
	private static float protectionMatrix[][][] = new float[1][8][8];
	private static Move moveArray[][] = new Move[1][300];
	public static short shortMoveArray[][] = new short[1][300];
	private static float movementBias[][][] = new float[1][8][8];
	
	public static int MAIN_THREAD_SET = 0;
	public static float DANGER_COEFF = 0.1f;
	
	//Handlers
	public static OpponentUpdater oppUpdater = null;
	
	public static void setupForProcessors(int howMany)
	{
		kingMatrix = new int[howMany+1][8][8];
		protectionMatrix = new float[howMany+1][8][8];
		moveArray = new Move[howMany+1][300];
		shortMoveArray = new short[howMany+1][300];
		movementBias = new float[howMany+1][8][8];
	}
	
	public static int currentProcessorSetup()
	{
		return kingMatrix.length-1;
	}
	
	private static final float knightProximity[][] =
	{{0.0f,1.0f/3.0f,0.5f,1.0f/3.0f,0.5f,1.0f/3.0f,0.25f,0.2f},
		{1.0f/3.0f,0.5f,1.0f,0.5f,1.0f/3.0f,0.25f,1.0f/3.0f,0.25f},
		{0.5f,1.0f,0.25f,1.0f/3.0f,0.5f,1.0f/3.0f,0.25f,0.2f},
		{1.0f/3.0f,0.5f,1.0f/3.0f,0.5f,1.0f/3.0f,0.25f,1.0f/3.0f,0.25f},
		{0.5f,1.0f/3.0f,0.5f,1.0f/3.0f,0.25f,1.0f/3.0f,0.25f,0.2f},
		{1.0f/3.0f,0.25f,1.0f/3.0f,0.25f,1.0f/3.0f,0.25f,0.2f,0.25f},
		{0.25f,1.0f/3.0f,0.25f,1.0f/3.0f,0.25f,0.2f,0.25f,0.2f},
		{0.2f,0.25f,0.2f,0.25f,0.2f,0.25f,0.2f,1.0f/6.0f}};
	
	public Player owner;
	public boolean broken;
	public boolean scrap; //when true, skip anything remotely time-consuming
	
	public float piece[][];
	public float pawn[][];
	public float king[][];
	public float empty[][];
	
	public byte allied[][];
	public byte danger[][];
	
	int minpawns[] = new int[8];
	int maxpawns[] = new int[8];
	int minpawny[] = new int[8];
	int maxpawny[] = new int[8];
	
	public float piecesLeft, pawnsLeft;
	public float pieceTotal, pawnTotal;
	
	public boolean white;
	boolean whiteTurn;
	int moves;
	int fiftyMoves;
	int kingX, kingY;
	int pawnTry, lastPawnTry, CX, CY, CWhat, lastCX, lastCY, lastCWhat;
	int check1, check2, lastCheck1, lastCheck2;
	boolean castleLeft, castleRight;
	
	public Uberposition(boolean w, Player own)
	{
		owner = own;
		broken = false;
		scrap = false;
		
		piece = new float[8][8];
		pawn = new float[8][8];
		king = new float[8][8];
		empty = new float[8][8];
		
		allied = new byte[8][8];
		danger = new byte[8][8];
		
		
		int y = (w? 0 : 7);
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				allied[k][j] = Chessboard.EMPTY;
				danger[k][j] = 5;
			}
		
		allied[0][y] = Chessboard.ROOK;
		allied[1][y] = Chessboard.KNIGHT;
		allied[2][y] = Chessboard.BISHOP;
		allied[3][y] = Chessboard.QUEEN;
		allied[4][y] = Chessboard.KING;
		allied[5][y] = Chessboard.BISHOP;
		allied[6][y] = Chessboard.KNIGHT;
		allied[7][y] = Chessboard.ROOK;
		for (int k=0; k<8; k++) allied[k][(w?1:6)] = Chessboard.PAWN;
		
		for (int k=0; k<8; k++)
		{
			minpawns[k] = 1; maxpawns[k] = 1;
			minpawny[k] = (w? 6:1);
			maxpawny[k] = (w? 6:1);
		}
		
		for (int k=0; k<8; k++) 
			for (int j=0; j<8; j++)
				if (j==(w? 6:1)) pawn[k][j] = 1.0f; else pawn[k][j] = 0.0f;
		
		for (int k=0; k<8; k++) 
			for (int j=0; j<8; j++)
				if (j==(w? 7:0) && k!=4) piece[k][j] = 1.0f; else piece[k][j] = 0.0f;
		
		for (int k=0; k<8; k++) 
			for (int j=0; j<8; j++)
				king[k][j] = 0.0f;
		king[4][(w? 7:0)] = 1.0f;
		
		for (int k=0; k<8; k++) 
			for (int j=0; j<8; j++)
			{
				empty[k][j] = 1.0f-pawn[k][j]-piece[k][j]-king[k][j];
			}
		
		piecesLeft = 7; pawnsLeft = 8;
		pieceTotal = 7.0f; pawnTotal = 8.0f;
		
		white = w;
		whiteTurn = true;
		
		moves = 0;
		fiftyMoves = 0;
		kingX = 4; kingY = (w?0:7);
		pawnTry = 0; lastPawnTry = 0; lastCX = -1; lastCY = -1;
		castleLeft = true; castleRight = true;
	}
	
	/*public Uberposition(Uberposition copy, boolean scrap)
	{
		allied = new byte[8][8];
		for (int k=0; k<8; k++)
		{
			for (int j=0; j<8; j++) allied[k][j] = copy.allied[k][j];
			minpawns[k] = copy.minpawns[k];
			maxpawns[k] = copy.maxpawns[k];
			minpawny[k] = copy.minpawny[k];
			maxpawny[k] = copy.maxpawny[k];
		}
		owner = copy.owner;
		broken = copy.broken;
		scrap = true;
		
		white=copy.white;
		whiteTurn=copy.whiteTurn;
		moves=copy.moves;
		fiftyMoves=copy.fiftyMoves;
		kingX=copy.kingX;
		kingY=copy.kingY;
		pawnTry=copy.pawnTry;
		lastPawnTry=copy.lastPawnTry;
		CX=copy.CX;
		CY=copy.CY;
		CWhat=copy.CWhat;
		lastCX=copy.lastCX;
		lastCY=copy.lastCY;
		lastCWhat=copy.lastCWhat;
		check1=copy.check1;
		check2=copy.check2;
		lastCheck1=copy.lastCheck1;
		lastCheck2=copy.lastCheck2;
		castleLeft=copy.castleLeft;
		castleRight=copy.castleRight;
	}*/
	
	public Uberposition(Uberposition copy)
	{
		piece = (copy.piece!=null && !copy.scrap? new float[8][8] : null);
		pawn = (copy.pawn!=null && !copy.scrap? new float[8][8] : null);
		king = (copy.king!=null? new float[8][8] : null);
		empty = (copy.empty!=null? new float[8][8] : null);
		allied = new byte[8][8];
		danger = new byte[8][8];
		
		for (int k=0; k<8; k++) 
			for (int j=0; j<8; j++)
			{
				if (piece!=null) piece[k][j] = copy.piece[k][j];
				if (pawn!=null) pawn[k][j] = copy.pawn[k][j];
				king[k][j] = copy.king[k][j];
				empty[k][j] = copy.empty[k][j];
				allied[k][j] = copy.allied[k][j];
				danger[k][j] = copy.danger[k][j];
			}

		if (!copy.scrap)
		{
			for (int k=0; k<8; k++) 
			{
				minpawns[k] = copy.minpawns[k];
				maxpawns[k] = copy.maxpawns[k];
				minpawny[k] = copy.minpawny[k];
				maxpawny[k] = copy.maxpawny[k];
			}
		}
		owner = copy.owner;
		broken = copy.broken;
		scrap = copy.scrap;
		
		piecesLeft=copy.piecesLeft;
		pawnsLeft=copy.pawnsLeft;
		pieceTotal=copy.pieceTotal;
		pawnTotal=copy.pawnTotal;
		
		white=copy.white;
		whiteTurn=copy.whiteTurn;
		moves=copy.moves;
		fiftyMoves=copy.fiftyMoves;
		kingX=copy.kingX;
		kingY=copy.kingY;
		pawnTry=copy.pawnTry;
		lastPawnTry=copy.lastPawnTry;
		CX=copy.CX;
		CY=copy.CY;
		CWhat=copy.CWhat;
		lastCX=copy.lastCX;
		lastCY=copy.lastCY;
		lastCWhat=copy.lastCWhat;
		check1=copy.check1;
		check2=copy.check2;
		lastCheck1=copy.lastCheck1;
		lastCheck2=copy.lastCheck2;
		castleLeft=copy.castleLeft;
		castleRight=copy.castleRight;
	}
	
	public float chanceOfLegality(Move m)
	{
		float chance = 1.0f-chanceOfCoveringKing(m);
		if (m.piece==Chessboard.KNIGHT) return chance;
		if (m.piece==Chessboard.KING)
		{
			return chance/(1.0f+protectionLevel(m.toX,m.toY));
		}
		
		int dx = (m.toX>m.fromX? 1 : (m.toX==m.fromX? 0 : -1)); int dy = (m.toY>m.fromY? 1 : (m.toY==m.fromY? 0 : -1)); 
		
		int x = m.fromX+dx; int y = m.fromY+dy;
		while (x!=m.toX || y!=m.toY)
		{
			chance*=empty[x][y];
			x+=dx; y+=dy;
		}
		if (m.piece==Chessboard.PAWN && m.fromX==m.toX) chance*=empty[m.toX][m.toY];
		
		return chance;
	}
	
	public float chanceOfCoveringKing(Move m)
	{
		if (piecesLeft+pawnsLeft==0) return 0.0f;
		
		int x = m.fromX; int y = m.fromY;
		boolean possible = (x==kingX || y==kingY || (x-kingX==y-kingY) || (x+y==kingX+kingY));
		if (!possible) return 0.0f;
		if (x==kingX && m.toX==x) return 0.0f;
		if (y==kingY && m.toY==y) return 0.0f;
		if (x-kingX==y-kingY && m.toX-kingX==m.toY-kingY) return 0.0f;
		if (x+y==kingX+kingY && m.toX+m.toY==kingX+kingY) return 0.0f;
		
		int dx = (kingX>x? 1 : (kingX==x? 0 : -1)); int dy = (kingY>y? 1 : (kingY==y? 0 : -1)); 
		int tx = x + dx; int ty = y + dy;
		for (int k=0; k<8; k++)
		{
			if (tx==kingX && ty==kingY) break;
			if (allied[tx][ty]!=Chessboard.EMPTY) return 0.0f;
			tx+=dx; ty+=dy;
		}
		
		//okay, there IS some chance
		tx = x - dx; ty = y - dy;
		float chance = 0.0f;
		while (tx>=0 && ty>=0 && tx<=7 && ty<=7)
		{
			if (allied[tx][ty]!=Chessboard.EMPTY) return chance;
			chance += (1.0-chance)*piece[tx][ty]*0.45; //0.45 is temporary
			tx-=dx; ty-=dy;
		}
		return chance;
	}
	
	public float protectionLevel(int x, int y)
	{
		float result = 0.0f;
		if (piece==null) return 0.0f;
		for (int xx=-1; xx<=1; xx++)
		{
			for (int yy=-1; yy<=1; yy++)
			{
				if (xx==0 && yy==0) continue;
				
				int tx = x+xx; int ty = y+yy;
				while (tx>=0 && ty>=0 && tx<=7 && ty<=7)
				{
					result+=piece[tx][ty]*0.6;
					if (xx==0 && ty==(white? y+1 : y-1)) result+=pawn[tx][ty]*2.0;
					tx+=xx; ty+=yy;
				}
			}
		}
		int kni[] = EvaluationGlobals.capVectors[Chessboard.KNIGHT];
		int kniy[] = EvaluationGlobals.capVectorsY[Chessboard.KNIGHT];
		for (int k=0; k<8; k++)
		{
			int x2 = x+kni[k]; int y2 = y+kniy[k];
			if (x2>=0 && y2>=0 && x2<8 && y2<8) result+=piece[x2][y2]*0.2;
			
		}
		
		if (x>0)
		{
			int yy = (white? y+2 : y-2);
			if (yy>=0 && yy<8) result+=pawn[x-1][yy]*2.0;
		}
		
		if (x<7)
		{
			int yy = (white? y+2 : y-2);
			if (yy>=0 && yy<8) result+=pawn[x+1][yy]*2.0;
		}
		
		return result;
	}
	
	public float chanceOfPawnTries(Move m)
	{
		if (pawn==null) return 0.0f;
		int y = (white? m.toY+1 : m.toY-1);
		if (y<1 || y>6) return 0.0f;
		float chance = 1.0f;
		if (m.toX>0) chance *= (1.0-pawn[m.toX-1][y]);
		if (m.toX<7) chance *= (1.0-pawn[m.toX+1][y]);
		return 1.0f-chance;
	}
	
	public float chanceOfYourPawnTries()
	{
		float f = 0.0f;
		int b = (white? 1 : -1);
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
				if (allied[k][j]==Chessboard.PAWN)
				{
					int y = j+b;
					if (y>=0 && y<8)
					{
						if (k>0) f+=(1.0f-empty[k-1][y]);
						if (k<7) f+=(1.0f-empty[k+1][y]);
					}
				}
		return f;
	}
	
	public float chanceOfCapture(Move m)
	{
		if (m.piece==Chessboard.PAWN) { if (m.toX==m.fromX) return 0.0f; else return 1.0f; }
		return (1.0f-empty[m.toX][m.toY]);
	}
	
	public float chanceOfCheck(Move m)
	{
		float out = 0.0f;
		int x[] = Chessboard.getPieceCaptureVectorX(m.piece, white);
		int y[] = Chessboard.getPieceCaptureVectorY(m.piece, white);
		
		if (m.piece==Chessboard.KING) return 0.0f;
		
		for (int k=0; k<x.length; k++)
		{
			float partial = 0.0f;
			boolean add = true;
			int xx = m.toX + x[k];
			int yy = m.toY + y[k];
			while (xx>=0 && yy>=0 && xx<8 && yy<8 && allied[xx][yy]==Chessboard.EMPTY)
			{
				partial += king[xx][yy];
				if (xx==m.fromX && yy==m.fromY) 
				{
					add = false; //don't add in the direction you came from
					break;
				}
				xx += x[k]; yy += y[k];
			}
			if (add) out += partial;
		}
		return out; //for now
	}
	
	public void chanceOfMessages(Move m, float out[]) 
	{
		out[0] = chanceOfLegality(m);
		out[1] = chanceOfCapture(m);
		out[2] = chanceOfPawnTries(m);
		out[3] = chanceOfCheck(m);
		
	}
	
	public Uberposition evolveWithPlayerMove(int set, Move m, int cx, int cy, int cwhat, int ck1, int ck2, int pt)
	{
		Uberposition up = new Uberposition(this);
		up.allied[m.fromX][m.fromY] = Chessboard.EMPTY;
		up.allied[m.toX][m.toY] = m.piece;
		
		if (m.piece==Chessboard.KING)
		{
			up.kingX = m.toX; up.kingY = m.toY;
		}
		if (m.piece==Chessboard.KING && m.toX==m.fromX+2)
		{
			int yy = white? 0 : 7;
			up.allied[7][yy] = Chessboard.EMPTY;
			if (up.pawn!=null) up.pawn[7][yy] = 0.0f;
			if (up.piece!=null) up.piece[7][yy] = 0.0f;
			up.king[7][yy] = 0.0f;
			up.empty[7][yy] = 1.0f;
			up.allied[5][yy] = Chessboard.ROOK;
		}
		if (m.piece==Chessboard.KING && m.toX==m.fromX-2)
		{
			int yy = white? 0 : 7;
			up.allied[0][yy] = Chessboard.EMPTY;
			if (up.pawn!=null) up.pawn[7][yy] = 0.0f;
			if (up.piece!=null) up.piece[7][yy] = 0.0f;
			up.king[7][yy] = 0.0f;
			up.empty[7][yy] = 1.0f;
			up.allied[3][yy] = Chessboard.ROOK;
		}
		if (m.piece==Chessboard.KING) { up.castleLeft = false; up.castleRight = false; }
		if (m.fromX==0 && m.fromY==(white? 0 : 7)) up.castleLeft = false;
		if (m.fromX==7 && m.fromY==(white? 0 : 7)) up.castleRight = false;
		
		if (cwhat!=Chessboard.NO_CAPTURE) up.danger[cx][cy] = 120;
		
		if (cwhat==Chessboard.CAPTURE_PAWN) 
		{
			up.minpawns[cx]--;
			up.maxpawns[cx]--;
			if (up.minpawns[cx]<0) up.minpawns[cx]=0;
			up.pawnsLeft-=1.0; 
			up.pawnTotal-=1.0; 
			if (up.pawnTotal<0.0)
			{
				up.pawnTotal = (up.pawnTotal+1.0f)*up.pawnsLeft/(up.pawnsLeft+1.0f);
				up.pieceTotal = up.pawnsLeft+up.piecesLeft-up.pawnTotal;
			}
		}
		if (cwhat==Chessboard.CAPTURE_PIECE)
		{
			up.piecesLeft--; up.pieceTotal-=1.0; 
			if (up.piecesLeft<0) 
			{ 
				up.piecesLeft = 0; up.pawnsLeft--; 
				//MAGIC!
				up.pawnTotal*=(up.pawnsLeft/(up.pawnsLeft+1.0)); 
				up.pieceTotal = up.pawnsLeft+up.piecesLeft-up.pawnTotal;
			}
		}
		if (up.piecesLeft+up.pawnsLeft<0.5 && up.pawn!=null) { up.pawn = null; up.piece = null; }
		if (cwhat==Chessboard.NO_CAPTURE) up.fiftyMoves++; else up.fiftyMoves=0;
		
		if (m.piece==Chessboard.PAWN)
		{
			up.fiftyMoves=0;
			if (m.toY==(white?7:0)) up.allied[m.toX][m.toY] = m.promotionPiece;
		}
		
		if (m.piece!=Chessboard.KNIGHT)
		{
			int dx = (m.toX>m.fromX? 1 : (m.toX==m.fromX? 0 : -1)); int dy = (m.toY>m.fromY? 1 : (m.toY==m.fromY? 0 : -1)); 
			int tx = (m.fromX+dx); int ty = (m.fromY+dy);
			while (tx>=0 && ty>=0 && tx<=7 && ty<=7)
			{
				if (tx==m.toX && ty==m.toY) break;
				up.king[tx][ty] = 0.0f; if (up.pawn!=null) up.pawn[tx][ty] = 0.0f;
				if (up.piece!=null) up.piece[tx][ty] = 0.0f;
				tx+=dx; ty+=dy;
			}
		}
		
		if (up.pawn!=null) up.pawn[m.toX][m.toY] = 0.0f;
		if (up.piece!=null) up.piece[m.toX][m.toY] = 0.0f;
		up.king[m.toX][m.toY] = 0.0f;
		//empty[m.toX][m.toY] = 1.0;
		
		up.handleCheck(set, m, ck1, ck2, cwhat);
		
		up.normalize();
		
		up.recordUmpireMessage(pt, cx, cy, cwhat, ck1, ck2);
		
		up.whiteTurn = !up.whiteTurn;
		up.moves++;
		
		return up;
		
	}
	
	/*public Uberposition evolveWithPlayerMoveScrap(Move m)
	{
		Uberposition up = new Uberposition(this,true);
		up.allied[m.fromX][m.fromY] = Chessboard.EMPTY;
		up.allied[m.toX][m.toY] = m.piece;
		
		if (m.piece==Chessboard.KING)
		{
			up.kingX = m.toX; up.kingY = m.toY;
		}
		if (m.piece==Chessboard.KING && m.toX==m.fromX+2)
		{
			int yy = white? 0 : 7;
			up.allied[7][yy] = Chessboard.EMPTY;
			up.allied[5][yy] = Chessboard.ROOK;
		}
		if (m.piece==Chessboard.KING && m.toX==m.fromX-2)
		{
			int yy = white? 0 : 7;
			up.allied[0][yy] = Chessboard.EMPTY;
			up.allied[3][yy] = Chessboard.ROOK;
		}
		if (m.piece==Chessboard.KING) { up.castleLeft = false; up.castleRight = false; }
		if (m.fromX==0 && m.fromY==(white? 0 : 7)) up.castleLeft = false;
		if (m.fromX==7 && m.fromY==(white? 0 : 7)) up.castleRight = false;
		
		up.fiftyMoves++;
		
		if (m.piece==Chessboard.PAWN)
		{
			up.fiftyMoves=0;
			if (m.toY==(white?7:0)) up.allied[m.toX][m.toY] = m.promotionPiece;
		}
		
		up.whiteTurn = !up.whiteTurn;
		up.moves++;
		
		return up;
		
	}*/
	
	public Uberposition evolveWithIllegalMove(int set, Move m)
	{
		Uberposition up = new Uberposition(this);
		
		if (m.piece==Chessboard.KNIGHT && check1==Chessboard.NO_CHECK)
		{
			//an illegal move with a knight and no check means that there is at least one enemy piece
			if (up.piecesLeft==0)
			{
				up.pawnsLeft--;
				up.piecesLeft++;
				up.pawnTotal*=(up.pawnsLeft/(up.pawnsLeft+1.0)); 
				up.pieceTotal = up.pawnsLeft+up.piecesLeft-up.pawnTotal;
			}
			
			int dx = (m.fromX>up.kingX? 1 : (m.fromX==up.kingX? 0 : -1));
			int dy = (m.fromY>up.kingY? 1 : (m.fromY==up.kingY? 0 : -1));
			up.shootPieceAugmentingBeam(m.fromX, m.fromY, dx, dy);
		}
		else if (m.piece==Chessboard.KING && check1==Chessboard.NO_CHECK)
		{
			if ((up.pawnsLeft+up.piecesLeft<0.5) || up.protectionLevel(m.toX, m.toY)==0.0)
			{
				//restrict king
				for (int k=0; k<8; k++)
					for (int j=0; j<8; j++)
					{
						int dx = (k-m.toX); int dy = (j-m.toY);
						if (dx<-1 || dx>1) up.king[k][j] = 0.0f;
						else if (dy<-1 || dy>1) up.king[k][j] = 0.0f;
					}
			}
		}
		else if (up.check1==Chessboard.NO_CHECK)
		{
			int dx = (m.fromX<m.toX? 1 : (m.fromX==m.toX? 0 : -1));
			int dy = (m.fromY<m.toY? 1 : (m.fromY==m.toY? 0 : -1));
			int x = m.fromX+dx; int y = m.fromY+dy;
			float total=0.0f;
			while (true)
			{
				if (x<0 || y<0 || x>7 || y>7) break;
				if (x==m.toX && y==m.toY)
				{
					if (m.piece==Chessboard.PAWN && m.toX==m.fromX) total+=(1.0-up.empty[x][y]);
					break;
				}
				total+=(1.0-up.empty[x][y]);
				x+=dx; y+=dy;
			}
			
			if (total<1.0 && total>0.0)
			{
				float ratio = (1.0f/total);
				x = m.fromX+dx; y = m.fromY+dy;
				while (true)
				{
					if (x<0 || y<0 || x>7 || y>7) break;
					if (x==m.toX && y==m.toY)
					{
						if (m.piece==Chessboard.PAWN && m.toX==m.fromX)
						{
							up.king[x][y]*=ratio;
							if (up.pawn!=null) up.pawn[x][y]*=ratio;
							if (up.piece!=null) up.piece[x][y]*=ratio;
						}
						break;
					}
					up.king[x][y]*=ratio;
					if (up.pawn!=null) up.pawn[x][y]*=ratio;
					if (up.piece!=null) up.piece[x][y]*=ratio;
					x+=dx; y+=dy;
				}
			}
			
		}
		
		
		
		return up;
	}
	
	public Uberposition evolveWithOpponentMove(int set, int pt, int cx, int cy, int ck1, int ck2)
	{
		int cw = (cx<0? Chessboard.NO_CAPTURE : allied[cx][cy]==Chessboard.PAWN? Chessboard.CAPTURE_PAWN : Chessboard.CAPTURE_PIECE);
	
		Uberposition up = new Uberposition(this);
		
		if (cx==0 && cy==(white?0:7)) up.castleLeft=false;
		if (cx==7 && cy==(white?0:7)) up.castleRight=false;
		
		up.updateOpponent(set,cx,cy,ck1,ck2,pt);
		
		up.normalize();
		
		if (cw!=Chessboard.NO_CAPTURE)
		{
			up.empty[cx][cy] = 0.0f;
			up.danger[cx][cy] = 5;
		}
		
		up.recordUmpireMessage(pt, cx, cy, cw, ck1, ck2);
		
		up.whiteTurn = !up.whiteTurn;
		up.moves++;
		
		return up;
	}
	
	private void updateOpponent(int set, int cx, int cy, int ck1, int ck2, int pt)
	{
		if (cx>=0)
		{
			allied[cx][cy] = Chessboard.EMPTY;
			
			//we suffered a capture
			boolean hadTries = pawnTry>0;
			float pawnchance = 0.9f;
			float piecechance = 0.09f;
			float kingchance = 0.01f;
			
			if (pawnsLeft<0.5f || !hadTries) pawnchance = 0.0f;
			else 
			{
				boolean c = (cx>0 && cy!=(white? 7:0) && pawn[cx-1][(white?cy+1:cy-1)]>0.0);
				boolean c2 = (cx<7 && cy!=(white? 7:0) && pawn[cx+1][(white?cy+1:cy-1)]>0.0);
				if (!c && !c2) pawnchance = 0.0f; else
				{
					if (c) { minpawns[cx-1]--; if (minpawns[cx-1]<0) minpawns[cx-1] = 0; }
					if (c2) { minpawns[cx+1]--; if (minpawns[cx+1]<0) minpawns[cx+1] = 0; }
					maxpawns[cx]++;
				}
			} 
			if (pieceTotal<=0.0) piecechance = 0.0f;
			boolean kingPossible = false;
			for (int k=cx-1; k<=cx+1; k++)
				for (int j=cy-1; j<=cy+1; j++)
				{
					if (k>=0 && j>=0 && k<8 && j<8 && king[k][j]>0.0) { kingPossible=true; break;}
				}
			if (!kingPossible) kingchance = 0.0f;
			float total = pawnchance + piecechance + kingchance;
			if (total>0.0)
			{
				king[cx][cy]=kingchance/total;
				if (pawn!=null) pawn[cx][cy]=pawnchance/total;
				if (piece!=null) piece[cx][cy]=piecechance/total;
				if (pawnchance==0.0 && piecechance==0.0)
				{
					for (int k=0; k<8; k++)
						for (int j=0; j<8; j++)
							king[k][j]=0.0f;
					
					king[cx][cy] = 1.0f;
				}
			}
		}
		
		if (/*ck1==Chessboard.NO_CHECK &&*/ cx<0)
		{
			//no check, no capture
			if (oppUpdater!=null) oppUpdater.updateWithOpponentMove(this, set);
			else spreadProbabilities(set);
		}
		
		//pawn tries
		if (pt==0 && ck1==Chessboard.NO_CHECK)
		{
			//lack of pawn tries...
			for (int k=0; k<8; k++)
				for (int j=0; j<8; j++)
				{
					if (allied[k][j]==Chessboard.PAWN)
					{
						for (int a=0; a<2; a++)
						{
							int x = (a==0? k-1 : k+1);
							int y = (white? j+1 : j-1);
							if (x<0 || y<0 || x>7 || y>7) continue;
							Move m = new Move();
							m.piece = Chessboard.PAWN; m.fromX = (byte)k; m.fromY = (byte)j;
							m.toX = (byte)x; m.toY = (byte)y;
							float c = chanceOfCoveringKing(m);
							if (piece!=null && c==0.0) piece[x][y]=0.0f;
							if (pawn!=null && c==0.0) pawn[x][y]=0.0f;
							//king[x][y]=0.0;
							
						}
					}
				}
		}
		
		//reduce king
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				int pc = allied[k][j];
				if (pc!=Chessboard.EMPTY)
				{
					int xx[] = (white? EvaluationGlobals.capVectors[pc] : EvaluationGlobals.capVectorsBlack[pc]);
					int yy[] = (white? EvaluationGlobals.capVectorsY[pc] : EvaluationGlobals.capVectorsBlackY[pc]);
					for (int a=0; a<xx.length; a++)
					{
						int dx = xx[a]; int dy = yy[a];
						int x2 = k+dx; int y2 = j+dx;
						while (true)
						{
							if (x2<0 || y2<0 || x2>7 || y2>7) break;
							king[x2][y2] = 0.0f;
							if (pc==Chessboard.PAWN || pc==Chessboard.KING || pc==Chessboard.KNIGHT) break;
							if (allied[x2][y2]!=Chessboard.EMPTY) break;
							if (empty[x2][y2]<1.0 && ((pawn!=null && pawn[x2][y2]>0.0) || (piece!=null && piece[x2][y2]>0.0))) break;
							x2+=dx; y2+=dy;
						}
					}

				}
			}
	}
	
	void spreadProbabilities(int set)
	{
		float matrix[][] = new float[8][8];
		
		float pieceIntensity = 2*pieceTotal/(pieceTotal*2+pawnTotal+1.0f);
		float pawnIntensity = pawnTotal/(pieceTotal*2+pawnTotal+1.0f);
		float kingIntensity = 1.0f/(pieceTotal*2+pawnTotal+1.0f);
		
		if (piece!=null)
		{
			//incredibly simple and incorrect model - but then again, it's the fastest
			float add = pieceIntensity/50.0f;
			
			for (int k=0; k<8; k++)
				for (int j=0; j<8; j++)
				{
					movementBias[set][k][j] = 0.0f;
				}
			
			int streak;
			float total, oldtotal;
			//horizontal and vertical
			for (int k=0; k<8; k++)
			{
				streak = 0;
				total = 0.0f;
				oldtotal = 0.0f;
				for (int j=0; j<=8; j++)
				{
					if (j<8 && allied[k][j]==Chessboard.EMPTY)
					{
						streak++;
						total += piece[k][j];
						continue;
					}
					if (streak>1)
					{
						float avg = total/streak;
						for (int i=j-streak; i<j; i++)
							movementBias[set][k][i] += (avg-piece[k][i])*add;
					}
					if (streak>0 && oldtotal>0.0 && j<8)
					{
						float dng = (oldtotal*(7-j+1)+total*(j-streak-1))*DANGER_COEFF/piecesLeft;
						danger[k][j] += dng;
					}
					
					streak = 0;
					oldtotal = total;
					total = 0.0f;
				}
			}
			for (int k=0; k<8; k++)
			{
				streak = 0;
				total = 0.0f;
				oldtotal = 0.0f;
				for (int j=0; j<=8; j++)
				{
					if (j<8 && allied[j][k]==Chessboard.EMPTY)
					{
						streak++;
						total += piece[j][k];
						continue;
					}
					if (streak>1)
					{
						float avg = total/streak;
						for (int i=j-streak; i<j; i++)
							movementBias[set][i][k] += (avg-piece[i][k])*add;
					}
					if (streak>0 && oldtotal>0.0 && j<8)
					{
						float dng = (oldtotal*(7-k+1)+total*(k-streak-1))*DANGER_COEFF/piecesLeft;
						danger[j][k] += dng;
					}
					streak = 0;
					oldtotal = total;
					total = 0.0f;
				}
			}
			//diagonal
			for (int t=1; t<=13; t++)
			{
				int x = (t<8? 0 : t-7);
				int y = (t<8? 7-t: 0);
				int len = (t<8? t+1 : 15-t);
				streak = 0;
				total = 0.0f;
				oldtotal = 0.0f;
				for (int u=0; u<=len; u++)
				{
					if (u<len && allied[x][y]==Chessboard.EMPTY)
					{
						streak++;
						total += piece[x][y];
						x++; y++;
						continue;
					}
					if (streak>1)
					{
						float avg = total/streak;
						for (int i=1; i<=streak; i++)
							movementBias[set][x-i][y-i] += (avg-piece[x-i][y-i])*add;
					}
					if (streak>0 && oldtotal>0.0 && x<8 && y<8)
					{
						float dng = (oldtotal*(len-u)+total*(u-streak))*DANGER_COEFF/piecesLeft;
						danger[x][y] += dng;
					}
					streak = 0;
					oldtotal = total;
					total = 0.0f;
				}
			}
			for (int t=1; t<=13; t++)
			{
				int x = (t<8? 0 : t-7);
				int y = (t<8? t: 7);
				int len = (t<8? t+1 : 15-t);
				streak = 0;
				total = 0.0f;
				oldtotal = 0.0f;
				for (int u=0; u<=len; u++)
				{
					if (u<len && allied[x][y]==Chessboard.EMPTY)
					{
						streak++;
						total += piece[x][y];
						x++; y--;
						continue;
					}
					if (streak>1)
					{
						float avg = total/streak;
						for (int i=1; i<=streak; i++)
							movementBias[set][x-i][y+i] += (avg-piece[x-i][y+i])*add;
					}
					if (streak>0 && oldtotal>0.0 && x<8 && y>=0)
					{
						float dng = (oldtotal*(len-u)+total*(u-streak))*DANGER_COEFF/piecesLeft;
						danger[x][y] += dng;
					}
					streak = 0;
					oldtotal = total;
					total = 0.0f;
				}
			}
			
			for (int k=0; k<8; k++)
				for (int j=0; j<8; j++)
				{
					if (allied[k][j]==Chessboard.EMPTY && empty[k][j]>0.0) 
					{
						movementBias[set][k][j]+=add*0.02;
						danger[k][j] *= 0.75;
						if (danger[k][j]<5) danger[k][j] = 5;
					}
					piece[k][j] += movementBias[set][k][j];
				}
			
		}
		
		if (pawn!=null)
		{
			for (int k=0; k<8; k++)
			{
				int start = (!white? 7 : 0);
				int increment = (!white? -1 : 1);
				int y = start;
				while ((y+increment)>=0 && (y+increment)<8)
				{
					if (allied[k][y]!=Chessboard.EMPTY || empty[k][y]==0.0) { y += increment; continue; }
					if (pawn[k][y+increment]>0.0) pawn[k][y]+=pawn[k][y+increment]*pawnIntensity/pawnTotal;
					if (y==(!white?3:4) && pawn[k][y+2*increment]>0.0) pawn[k][y]+=pawn[k][y+2*increment]*pawnIntensity/pawnTotal;
					y += increment;
				}
			}
		}
		
		int kx[] = EvaluationGlobals.capVectors[Chessboard.KING];
		int ky[] = EvaluationGlobals.capVectorsY[Chessboard.KING];
		
		float kingAdd = kingIntensity/15.0f;
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				if (allied[k][j]!=Chessboard.EMPTY || empty[k][j]==0.0) continue;
				for (int a=0; a<8; a++)
				{
					int x1 = kx[a]; int y1 = ky[a];
					int x2 = k+x1; int y2 = j+y1;
					if (x2<0 || y2<0 || x2>7 || y2>7) continue;
					if (king[x2][y2]>0.0) { matrix[k][j]=1.0f; break; }
				}
			}
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				if (matrix[k][j]>0.0) king[k][j]+=kingAdd;
			}
		
		
	}
	
	private void recordUmpireMessage(int pt, int cx, int cy, int cwhat, int ck1, int ck2)
	{
		lastPawnTry = pawnTry;
		lastCWhat = CWhat;
		lastCX = CX;
		lastCY = CY;
		lastCheck1 = check1;
		lastCheck2 = check2;
		
		pawnTry = pt;
		CWhat = cwhat;
		CX = cx;
		CY = cy;
		check1 = ck1;
		check2 = ck2;
	}
	
	private void normalize()
	{
		if (scrap) return;
		
		if (piece!=null) normalize(piece,pieceTotal);
		if (pawn!=null) normalize(pawn,pawnTotal);
		if (king!=null) normalize(king,1.0f);
		if (pawn!=null) normalizePawns();
		updateEmpty();
		
		if (pawn!=null) updatePawnPositions();
	}
	
	private void normalize(float mat[][], float val)
	{
		float current = 0.0f;
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++) current+=mat[k][j];
				
		
		if (current==0.0) 
		{ 
			broken = true; //this means the position cannot occur with the specified umpire messages
			/*System.out.println("AAAAHHHH!");*/ return; 
		}
		float ratio = val/current;
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
				mat[k][j]*=ratio;
	}
	
	private void normalizePawns()
	{
		if (pawn==null) return;
		float amt[] = new float[8];
		float avail = pawnTotal;
		float surplus = 0.0f;
		for (int k=0; k<8; k++)
		{
			amt[k]+=minpawns[k];
			avail -= minpawns[k];
			surplus += (maxpawns[k]-minpawns[k]);
		}
		if (avail>0.0 && surplus>0.0)
		{
			for (int k=0; k<8; k++)
			{
				amt[k]+=avail*(maxpawns[k]-minpawns[k])/surplus;
			}
		}
		
		for (int k=0; k<8; k++)
		{
			float total = 0.0f;
			//if (amt[k]<=0.0) continue;
			for (int j=0; j<8; j++)
			{
				total+=pawn[k][j];
			}
			if (total<=0.0) continue;
			float ratio = amt[k]/total;
			for (int j=0; j<8; j++)
			{
				pawn[k][j]*=ratio;
			}
		}
		
		int y = (white? 0 : 7);
		for (int k=0; k<8; k++)
		{
			if (pawn[k][y]>0.0) minpawns[k] = 0;
			pawnTotal -= pawn[k][y];
			pieceTotal += pawn[k][y];
			pawn[k][y] = 0.0f;
		}
	}
	
	private void updateEmpty()
	{
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				if (allied[k][j]!=Chessboard.EMPTY) empty[k][j] = 0.0f; else
				{
					empty[k][j]=1.0f-(piece!=null? piece[k][j]:0.0f)-
					(pawn!=null? pawn[k][j]:0.0f)-king[k][j];
					if (empty[k][j]<0.0) empty[k][j] = 0.0f;
				}
			}
	}
	
	private void handleCheck(int set, Move m, int ck1, int ck2, int capturewhat)
	{
		if (scrap) return;
		
		if (ck1!=Chessboard.NO_CHECK)
		{
			for (int k=0; k<8; k++)
				for (int j=0; j<8; j++) kingMatrix[set][k][j] = 0;
			boolean floatCheck = (ck1!=Chessboard.NO_CHECK && ck2!=Chessboard.NO_CHECK);
			
			for (int a=0; a<2; a++)
			{
				int ch = (a==0? ck1 : ck2);
				if (ch == Chessboard.NO_CHECK) continue;
				
				boolean compatible = Move.pieceCompatible(m.piece,ch);
				
				if (compatible)
				{
					addTargetSquaresToKingMatrix(set,m,ch,capturewhat);
				}
				if (!compatible || (m.piece==Chessboard.PAWN && (ch==Chessboard.CHECK_LONG_DIAGONAL
					|| ch==Chessboard.CHECK_SHORT_DIAGONAL)))
				{
					processDiscoveryCheck(set,m,ch,capturewhat);
				}
				//now update the metaposition with the content of the king matrix...
				int minimum = (floatCheck? 2 : 1); //a float check means the candidates must
					//satisfy both check sets (only 1 square will).
				for (int k=0; k<8; k++)
					for (int j=0; j<8; j++)
					{
						if (kingMatrix[set][k][j]<minimum) king[k][j]=0.0f;
					}
			}
		} else
		{
			/*int xd[] = (white? EvaluationGlobals.capVectors[m.piece]: EvaluationGlobals.capVectorsBlack[m.piece]);
			int yd[] = (white? EvaluationGlobals.capVectorsY[m.piece]: EvaluationGlobals.capVectorsBlackY[m.piece]);
			int nox = (m.toX>m.fromX? -1 : (m.toX==m.fromX? 0 : 1));
			int noy = (m.toY>m.fromY? -1 : (m.toY==m.fromY? 0 : 1));
			for (int k=0; k<xd.length; k++)
			{
				int x = xd[k]; int y = yd[k];
				if (nox!=x || noy!=y) shootAntikingBeam(m.toX, m.toY, x, y, -1, -1, true);
			}*/
			restrictEnemyKingNoCheck(set);
		}
	}
	
	protected void processDiscoveryCheck(int set, Move m, int check, int capture)
	{
		//a discovery check proceeds as follows: we examine each one of the 8 directions
		//except for the move's direction and its opposite. If we find a compatible friendly piece,
		//we mark the opposite direction as possible.
		int dx = (m.toX > m.fromX? -1 : (m.toX==m.fromX? 0 : 1)); //reversed deltas.
		int dy = (m.toY > m.fromY? -1 : (m.toY==m.fromY? 0 : 1));
		int x,y;
		int movementX[] = Chessboard.queenOffsetX;
		int movementY[] = Chessboard.queenOffsetY;
		
		for (int k=0; k<8; k++) EvaluationGlobals.directions[k] = true;
		
		for (int k=0; k<8; k++)
		{
			int incrementX = movementX[k];
			int incrementY = movementY[k];
			if (incrementX==dx && incrementY==dy) EvaluationGlobals.directions[k] = false;
			if (incrementX==-dx && incrementY==-dy) EvaluationGlobals.directions[k] = false;
			if (check==Chessboard.CHECK_RANK && incrementY!=0) EvaluationGlobals.directions[k] = false;
			if (check==Chessboard.CHECK_FILE && incrementX!=0) EvaluationGlobals.directions[k] = false;
			if ((incrementX==0 || incrementY==0) && (check==Chessboard.CHECK_LONG_DIAGONAL ||
				check==Chessboard.CHECK_SHORT_DIAGONAL)) EvaluationGlobals.directions[k] = false;
				
			if (!EvaluationGlobals.directions[k]) continue;
			
			EvaluationGlobals.directions[k] = false; //now false by default...
			
			//compatible direction, but you need a compatible piece in order to call it a candidate.
			x = m.fromX; y = m.fromY;
			boolean cont = true;
			while (cont)
			{
				x+=incrementX; y+=incrementY;
				if (x<0 || y<0 || x>=8 || y>=8) break;
				if (allied[x][y]!=Chessboard.EMPTY)
				{
					byte piece = allied[x][y];
					if (Move.pieceCompatible(piece,check)) 
					{
						EvaluationGlobals.directions[k] = true; break;
					} else break;
				} else if (empty[x][y]<=0.0) break;
			}
			
		}
		
		//now, go through the compatible directions and add kings in the opposite directions!
		for (int k=0; k<8; k++)
		{
			if (!EvaluationGlobals.directions[k]) continue;
			int incrementX = -movementX[k];
			int incrementY = -movementY[k];
			x = m.fromX; y = m.fromY;
			boolean cont = true;
			while (cont)
			{
				x+=incrementX; y+=incrementY;
				if (x<0 || y<0 || x>=8 || y>=8) break;
				if (king[x][y]>0.0)
				{
					//if a diagonal check, make sure it's the right diagonal...
					if (check==Chessboard.CHECK_LONG_DIAGONAL || check==Chessboard.CHECK_SHORT_DIAGONAL)
					{
						if (Chessboard.isSquareCompatibleWithDiagonalCheck(x,y,check,incrementX,
							incrementY)) kingMatrix[set][x][y]++;
					} else kingMatrix[set][x][y]++; 
				}  
				if (empty[x][y]<=0.0) break;
			}
						
			
		}
		
	}
	
	private void restrictEnemyKingNoCheck(int set)
	{
		computeProtectionMatrix(set,false);
		//no check...
		for (byte k=0; k<8; k++)
		{
			for (byte j=0; j<8; j++)
			{
				if (king[k][j]>0.0f && protectionMatrix[set][k][j]>0)
					king[k][j]=0.0f;
			}
		}
		
	}
	
	private float shootAntikingBeam(int x, int y, int dx, int dy, int endx, int endy, boolean reduceKing)
	{
		x+=dx; y+=dy;
		float intensity = 1.0f;
		while (x>=0 && y>=0 && x<=7 && y<=7)
		{
			if (reduceKing) king[x][y]*=(1.0-intensity);
			intensity*=empty[x][y];
			if (x==endx && y==endy) break;
			if (allied[x][y]!=Chessboard.EMPTY) break;
			x+=dx; y+=dy;
		}
		return intensity;
	}
	
	private void shootPieceAugmentingBeam(int x2, int y2, int dx, int dy)
	{
		float total = 0.0f;
		int x = x2; int y = y2;
		x+=dx; y+=dy;
		while (x>=0 && y>=0 && x<=7 && y<=7)
		{
			total+=piece[x][y];
			if (allied[x][y]!=Chessboard.EMPTY) break;
			x+=dx; y+=dy;
		}
		if (total==0.0) return;
		float ratio = 1.0f/total;
		x = x2+dx; y = y2+dy;
		while (x>=0 && y>=0 && x<=7 && y<=7)
		{
			piece[x][y]*=ratio;
			if (allied[x][y]!=Chessboard.EMPTY) break;
			x+=dx; y+=dy;
		}
	}
	
	private int shootPieceFindingBeam(int x, int y, int dx, int dy, int out[])
	{
		x+=dx; y+=dy;
		while (x>=0 && y>=0 && x<=7 && y<=7)
		{
			if (allied[x][y]!=Chessboard.EMPTY)
			{
				out[0] = x; out[1] = y; return allied[x][y];
			}
			if (allied[x][y]!=Chessboard.EMPTY) break;
			x+=dx; y+=dy;
		}
		return Chessboard.EMPTY;
	}
	
	protected void addTargetSquaresToKingMatrix(int set, Move m,int check, int capture)
	{
		//we add the squares controlled by the moving piece
		int dx = (m.toX > m.fromX? -1 : (m.toX==m.fromX? 0 : 1)); //reversed deltas.
		int dy = (m.toY > m.fromY? -1 : (m.toY==m.fromY? 0 : 1));
		int x,y;
		int piece = m.piece;
		int movementX[] = Chessboard.getPieceCaptureVectorX(piece,white);
		int movementY[] = Chessboard.getPieceCaptureVectorY(piece,white);
		boolean movesOnce = Chessboard.movesOnlyOnce(piece); //Knight and Pawn
		
		for (int k=0; k<movementX.length; k++)
		{
			//when pursuing a direction, we NEVER go back from where the piece moved
			//(the king can't be there), and we ONLY go forward if there was a capture.
			int incrementX = movementX[k];
			int incrementY = movementY[k];
			//System.out.println("Testing "+incrementX+" "+incrementY);
			boolean cont = true;
			x = m.toX; y = m.toY;
			if (incrementX==dx && incrementY==dy && (m.piece!=Chessboard.PAWN || m.toY!=(white?7:0))) cont = false; //don't go back unless you were a pawn and have just promoted!
			if (capture==Chessboard.NO_CAPTURE && !movesOnce && 
				incrementX==-dx && incrementY==-dy) cont = false; //don't go forward unless you captured!
			if (incrementY==0 && check!=Chessboard.CHECK_RANK) cont = false;
			if (incrementX==0 && check!=Chessboard.CHECK_FILE) cont = false;
			if (incrementX!=0 && incrementY!=0 && (check==Chessboard.CHECK_FILE ||
				check==Chessboard.CHECK_RANK)) cont = false; //only appropriate check directions.
			//System.out.println("Still testing "+incrementX+" "+incrementY);
			while (cont)
			{
				x+=incrementX; y+=incrementY;
				if (x<0 || y<0 || x>=8 || y>=8) break;
				//System.out.println("Testing "+x+" "+y);
				if (king[x][y]>0.0)
				{
					//a candidate square. If diagonal, check whether it is the right diagonal.
					if (check==Chessboard.CHECK_LONG_DIAGONAL || check==Chessboard.CHECK_SHORT_DIAGONAL)
					{
						if (Chessboard.isSquareCompatibleWithDiagonalCheck(x,y,check,incrementX,
							incrementY)) kingMatrix[set][x][y]++;
					} else kingMatrix[set][x][y]++;
				}
				if (empty[x][y]<=0.0) cont = false; //stop if you encounter an obstacle.
				if (movesOnce) cont = false; //...or if the piece is a knight or pawn.
			}
			
		}
		
		
	}
	
	private void updatePawnPositions()
	{
		for (byte k=0; k<8; k++)
		{
			minpawny[k] = 10;
			maxpawny[k] = -1;
			if (minpawns[k]>0)
			{
				for (byte p=1; p<7; p++)
				{
					if (pawn[k][p]>0.0)
					{
						if (p<minpawny[k]) minpawny[k] = p;
						if (p>maxpawny[k]) maxpawny[k] = p;
					}
				}
			}
			/*if (maxpawns[k]>0 && maxpawny[k]<0)
			{
				//there is no pawn here
				minpawns[k] = 0;
				maxpawns[k] = 0;
			}*/
		}
		
	}
	
	public float evaluateWarSaga(Move m)
	{
		float pawns = chanceOfPawnTries(m);
		//float pieces = protectionLevel(m.toX, m.toY);
		
		int attackPower = attackPower(m.toX,m.toY);
		/*if (attackPower==1) return 0.01f;
		float plevel = protectionLevel(m.toX, m.toY);
		return (1.0f-plevel*0.35f/attackPower);*/
		float retalChance = 1.0f;
		float victory = 0.0f;
		for (int k=0; k<attackPower; k++)
		{
			victory += (1.0-victory)*(1.0-retalChance);
			retalChance *= 0.7;
			if (k==0) retalChance += (1.0 - retalChance)*pawns;
		}
		return victory;
	}
	
	public Move[] generateMoves(int set, boolean topLevel, Player pl)
	{
		int generatedMoves = 0;
		byte piece;
		boolean once,pawnControl,rookControl,capture;
		Move m;
		
		int vecs[][];
		int vecsY[][];
		int movx[];
		int movy[];
		int dx,dy,x,y;
		int limitY = 0;
		
		vecs = (white? EvaluationGlobals.vectors : EvaluationGlobals.vectorsBlack);
		vecsY = (white? EvaluationGlobals.vectorsY : EvaluationGlobals.vectorsBlackY);
		
		//if (topLevel && check1!=Chessboard.NO_CHECK) computeKingLocation();

		/*for (byte k=0; k<8; k++)
		{
			minpawny[k] = 10;
			maxpawny[k] = -1;
			if (minpawns[k]>0)
			{
				for (byte p=1; p<7; p++)
				{
					if (pawn[k][p]>0.0)
					{
						if (p<minpawny[k]) minpawny[k] = p;
						if (p>maxpawny[k]) maxpawny[k] = p;
					}
				}
			}
		}*/
		
		for (byte k=0; k<8; k++)
		{
			for (byte j=0; j<8; j++)
			{
				piece = allied[k][j];
				if (piece==Chessboard.EMPTY) continue;
				movx = vecs[piece];
				movy = vecsY[piece];
				once = Chessboard.movesOnlyOnce(piece);
				pawnControl = (piece==Chessboard.PAWN && j==(white?1:6));
				for (int dir=0; dir<movx.length; dir++)
				{
					dx = movx[dir]; dy = movy[dir];
					x = k+dx; y = j+dy;
					rookControl = (dx==0 && /*!once &&*/ maxpawny[x]!=-1); //vertical movement on files with pawns, (rook & queen)
					if (rookControl)
					{
						//Rook control happens when our piece starts its move from
						//outside of the pawn envelope and tries to move all the way
						//through it.
						if (j>=maxpawny[x] && dy<0) limitY = minpawny[x]-1;
						else if (j<=minpawny[x] && dy>0) limitY = maxpawny[x]+1;
						else rookControl = false; //if the rook is INSIDE the pawn envelope, we cannot
						//rule out movement either way (the pawn might be ahead or behind the rook).
					} 
					while (true)
					{
						//System.out.println("Trying "+x+" "+y+" "+piece);
						if (x<0 || y<0 || x>=8 || y>=8) break;
						if (rookControl && y==limitY) break; //prevent rooks and queens from moving through their own pawns.
						if (allied[x][y]!=Chessboard.EMPTY) break;
						//System.out.println("Adding "+x+" "+y+" "+piece);

						m = new Move();
						m.piece = piece; m.fromX = k; m.fromY = j; m.toX = (byte)x; m.toY = (byte)y;
						if (piece==Chessboard.PAWN && m.toY == (white?7:0)) m.promotionPiece = Chessboard.QUEEN;
						if (piece==Chessboard.PAWN && empty[x][y]<=0.0) break; //pawns do not go to occupied squares with their normal movement.
						if (!topLevel || ((pl==null || !pl.isMoveBanned(m)) && moveCompatibleWithChecks(m))) moveArray[set][generatedMoves++] = m;
						if (empty[x][y]<=0.0) break;
						if (pawnControl)
						{
							pawnControl=false;
							x+=dx; y+=dy;
							continue; //pawn gets a "second chance"
						}
						if (once) break;
						x+=dx; y+=dy;

					}
				}		
				//pawn tries...
				if (topLevel && pawnTry>0 && piece==Chessboard.PAWN)
				{		
					for (int tr=0; tr<2; tr++)
					{
						byte px = (byte) (tr==0? k-1 : k+1);
						byte py = (byte) (white? j+1 : j-1);
						if (px>=0 && px<8 && py>=0 && py<8 && allied[px][py]==Chessboard.EMPTY /*&& !isEmpty(px,py)*/)
						{
							Move pt = new Move();
							pt.piece = Chessboard.PAWN; pt.promotionPiece = Chessboard.QUEEN;
							pt.fromX = k; pt.fromY = j; pt.toX = px; pt.toY = py;
							if (pl==null || !pl.isMoveBanned(pt)) moveArray[set][generatedMoves++] = pt;	
						}
					}
				}
			}
		}
		
		int rank = (white? 0 : 7);
		if (castleLeft)
		{
			if (allied[0][rank]==Chessboard.ROOK && allied[1][rank]==Chessboard.EMPTY && allied[2][rank]==Chessboard.EMPTY && allied[3][rank]==Chessboard.EMPTY && allied[4][rank]==Chessboard.KING)
			{
				Move c = new Move();
				c.piece = Chessboard.KING; c.fromY = (byte)rank; c.toY = (byte)rank;
				c.fromX = 4; c.toX = 2; 
				if (pl==null || !pl.isMoveBanned(c)) moveArray[set][generatedMoves++] = c;
			}
		}
		if (castleRight)
		{
			if (allied[7][rank]==Chessboard.ROOK && allied[5][rank]==Chessboard.EMPTY && allied[6][rank]==Chessboard.EMPTY && allied[4][rank]==Chessboard.KING)
			{
				Move c = new Move();
				c.piece = Chessboard.KING; c.fromY = (byte)rank; c.toY = (byte)rank;
				c.fromX = 4; c.toX = 6; 
				if (pl==null || !pl.isMoveBanned(c)) moveArray[set][generatedMoves++] = c;
			}
		}
		
		Move out[] = new Move[generatedMoves];
		if (generatedMoves>0)
		{
			System.arraycopy(moveArray[set], 0, out, 0, generatedMoves);
		}
		
		for (int k=0; k<generatedMoves; k++)
			if (out[k]==null)
			{
				out[k] = null;
				// System.out.println("NULL!");
			}
		
		return out;
	}
	
	public short[] generateShortMoves(int set, boolean topLevel, Player pl)
	{
		int generatedMoves = 0;
		byte piece;
		boolean once,pawnControl,rookControl,capture;
		short m;
		
		int vecs[][];
		int vecsY[][];
		int movx[];
		int movy[];
		int dx,dy,x,y;
		int limitY = 0;
		
		vecs = (white? EvaluationGlobals.vectors : EvaluationGlobals.vectorsBlack);
		vecsY = (white? EvaluationGlobals.vectorsY : EvaluationGlobals.vectorsBlackY);
		
		for (byte k=0; k<8; k++)
		{
			for (byte j=0; j<8; j++)
			{
				piece = allied[k][j];
				if (piece==Chessboard.EMPTY) continue;
				movx = vecs[piece];
				movy = vecsY[piece];
				once = Chessboard.movesOnlyOnce(piece);
				pawnControl = (piece==Chessboard.PAWN && j==(white?1:6));
				for (int dir=0; dir<movx.length; dir++)
				{
					dx = movx[dir]; dy = movy[dir];
					x = k+dx; y = j+dy;
					rookControl = (dx==0 && /*!once &&*/ maxpawny[x]!=-1); //vertical movement on files with pawns, (rook & queen)
					if (rookControl)
					{
						//Rook control happens when our piece starts its move from
						//outside of the pawn envelope and tries to move all the way
						//through it.
						if (j>=maxpawny[x] && dy<0) limitY = minpawny[x]-1;
						else if (j<=minpawny[x] && dy>0) limitY = maxpawny[x]+1;
						else rookControl = false; //if the rook is INSIDE the pawn envelope, we cannot
						//rule out movement either way (the pawn might be ahead or behind the rook).
					} 
					while (true)
					{
						//System.out.println("Trying "+x+" "+y+" "+piece);
						if (x<0 || y<0 || x>=8 || y>=8) break;
						if (rookControl && y==limitY) break; //prevent rooks and queens from moving through their own pawns.
						if (allied[x][y]!=Chessboard.EMPTY) break;
						//System.out.println("Adding "+x+" "+y+" "+piece);
						if (piece==Chessboard.PAWN && empty[x][y]<=0.0) break; //pawns do not go to occupied squares with their normal movement.
						m = (short)((piece<<12)|(k<<9)|(j<<6)|(x<<3)|y);
						if (!topLevel || ((pl==null || !pl.isShortMoveBanned(m)) && shortMoveCompatibleWithChecks(m))) shortMoveArray[set][generatedMoves++] = m;
						if (empty[x][y]<=0.0) break;
						if (pawnControl)
						{
							pawnControl=false;
							x+=dx; y+=dy;
							continue; //pawn gets a "second chance"
						}
						if (once) break;
						x+=dx; y+=dy;

					}
				}		
				//pawn tries...
				if (topLevel && pawnTry>0 && piece==Chessboard.PAWN)
				{		
					for (int tr=0; tr<2; tr++)
					{
						byte px = (byte) (tr==0? k-1 : k+1);
						byte py = (byte) (white? j+1 : j-1);
						if (px>=0 && px<8 && py>=0 && py<8 && allied[px][py]==Chessboard.EMPTY /*&& !isEmpty(px,py)*/)
						{
							short c = (short)((Chessboard.PAWN<<12)|(k<<9)|(j<<6)|(px<<3)|py);
							if (pl==null || !pl.isShortMoveBanned(c)) shortMoveArray[set][generatedMoves++] = c;	
						}
					}
				}
			}
		}
		
		int rank = (white? 0 : 7);
		if (castleLeft)
		{
			if (allied[0][rank]==Chessboard.ROOK && allied[1][rank]==Chessboard.EMPTY && allied[2][rank]==Chessboard.EMPTY && allied[3][rank]==Chessboard.EMPTY && allied[4][rank]==Chessboard.KING)
			{
				short c = (short)((Chessboard.KING<<12)|(4<<9)|(rank<<6)|(2<<3)|rank); 
				if (pl==null || !pl.isShortMoveBanned(c)) shortMoveArray[set][generatedMoves++] = c;
			}
		}
		if (castleRight)
		{
			if (allied[7][rank]==Chessboard.ROOK && allied[5][rank]==Chessboard.EMPTY && allied[6][rank]==Chessboard.EMPTY && allied[4][rank]==Chessboard.KING)
			{
				short c = (short)((Chessboard.KING<<12)|(4<<9)|(rank<<6)|(6<<3)|rank);
				if (pl==null || !pl.isShortMoveBanned(c)) shortMoveArray[set][generatedMoves++] = c;
			}
		}
		
		short out[] = new short[generatedMoves];
		if (generatedMoves>0)
		{
			System.arraycopy(shortMoveArray[set], 0, out, 0, generatedMoves);
		}
		
		return out;
	}
	

	public boolean moveCompatibleWithChecks(Move m)
	{
		return (m.piece == Chessboard.KING || moveOnCheckTrajectory(m));
	}
	
	public boolean moveOnCheckTrajectory(Move m)
	{
		if (check1 == Chessboard.NO_CHECK) return true;
		
		for (int k=0; k<2; k++)
		{
			int type = (k==0? check1 : check2);
			
			if (type == Chessboard.CHECK_FILE && m.toX!=kingX) return false;
			if (type == Chessboard.CHECK_RANK && m.toY!=kingY) return false;
			if ((type==Chessboard.CHECK_LONG_DIAGONAL || type==Chessboard.CHECK_SHORT_DIAGONAL)
				&& (m.toX+m.toY != kingX+kingY) && (m.toX-m.toY != kingX-kingY)) return false;
			 
			if ((type==Chessboard.CHECK_LONG_DIAGONAL || type==Chessboard.CHECK_SHORT_DIAGONAL))
			{
				//quick dirty hack to determine if a check is compatible with short/long diagonal
				int diagCheckCounter = 0;
				if (type==Chessboard.CHECK_LONG_DIAGONAL) diagCheckCounter++;
				if ((m.toX+m.toY == kingX+kingY)) diagCheckCounter++;
				if ((kingX<4 && kingY<4) || (kingX>=4 && kingY>=4)) diagCheckCounter++;
				if (diagCheckCounter%2 == 1) return false;
			}
			
			if (type==Chessboard.CHECK_KNIGHT)
			{
				int dx = m.toX - kingX;
				int dy = m.toY - kingY;
				int p = dx*dy; 
				if (p!=2 && p!=-2) return false;
			}
		}
		
		return true;
	}
	
	public boolean shortMoveCompatibleWithChecks(short m)
	{
		return (((m>>12)&7) == Chessboard.KING || shortMoveOnCheckTrajectory(m));
	}
	
	public boolean shortMoveOnCheckTrajectory(short m)
	{
		if (check1 == Chessboard.NO_CHECK) return true;
		
		int x = ((m>>3)&7); 
		int y = (m&7);
		
		for (int k=0; k<2; k++)
		{
			int type = (k==0? check1 : check2);
			
			if (type == Chessboard.CHECK_FILE && x!=kingX) return false;
			if (type == Chessboard.CHECK_RANK && y!=kingY) return false;
			if ((type==Chessboard.CHECK_LONG_DIAGONAL || type==Chessboard.CHECK_SHORT_DIAGONAL)
				&& (x+y != kingX+kingY) && (x-y != kingX-kingY)) return false;
			 
			if ((type==Chessboard.CHECK_LONG_DIAGONAL || type==Chessboard.CHECK_SHORT_DIAGONAL))
			{
				//quick dirty hack to determine if a check is compatible with short/long diagonal
				int diagCheckCounter = 0;
				if (type==Chessboard.CHECK_LONG_DIAGONAL) diagCheckCounter++;
				if ((x+y == kingX+kingY)) diagCheckCounter++;
				if ((kingX<4 && kingY<4) || (kingX>=4 && kingY>=4)) diagCheckCounter++;
				if (diagCheckCounter%2 == 1) return false;
			}
			
			if (type==Chessboard.CHECK_KNIGHT)
			{
				int dx = x - kingX;
				int dy = y - kingY;
				int p = dx*dy; 
				if (p!=2 && p!=-2) return false;
			}
		}
		
		return true;
	}
	
	public void computeProtectionMatrix(int set, boolean soft)
	{
		int vecs[][], vecsY[][];
		int movx[], movy[];
		int x,y,dx,dy;
		int limitY = 0;
		boolean once;
		boolean rookControl;
		float chance;
		
		if (white)
		{
			vecs = EvaluationGlobals.capVectors; vecsY = EvaluationGlobals.capVectorsY;
		} else
		{
			vecs = EvaluationGlobals.capVectorsBlack; vecsY = EvaluationGlobals.capVectorsBlackY;
		}
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++) 
			{
				protectionMatrix[set][k][j] = 0;
			}  
			
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				int piec = allied[k][j];	
				if (piec!=Chessboard.EMPTY)
				{
					chance = 1.0f;
					once = Chessboard.movesOnlyOnce(piec);
					/*if (piec>6)
					{
						piec = 6;
					}*/
					movx = vecs[piec];
					movy = vecsY[piec];
					for (int dir=0; dir<movx.length; dir++)
					{
						dx = movx[dir]; dy = movy[dir];
						x = k+dx; y = j+dy;
						rookControl = (dx==0 && !once && maxpawny[x]!=-1); //vertical movement on files with pawns, (rook & queen)
						if (rookControl)
						{
							//Rook control happens when our piece starts its move from
							//outside of the pawn envelope and tries to move all the way
							//through it.
							if (j>=maxpawny[x] && dy<0) limitY = minpawny[x]-1;
							else if (j<=minpawny[x] && dy>0) limitY = maxpawny[x]+1;
							else rookControl = false; //if the rook is INSIDE the pawn envelope, we cannot
							//rule out movement either way (the pawn might be ahead or behind the rook).
						}
						while (true)
						{
							//System.out.println("Trying "+x+" "+y+" "+piece);
							if (x<0 || y<0 || x>=8 || y>=8) break;
							if (rookControl && y==limitY) break; //prevent rooks and queens from moving through their own pawns.
							
							protectionMatrix[set][x][y]+=chance;
							//protectionMatrix[x][y]+=1.0;
							
							if (soft || pawn==null)
							{
								if (allied[x][y]!=Chessboard.EMPTY)
								{
									//protection 'passes through' pieces with the same attack capabilities
									boolean passthrough = false;
									if ((dx==0 || dy==0) && (allied[x][y]==Chessboard.ROOK || allied[x][y]==Chessboard.QUEEN)) passthrough=true;
									if ((dx*dy==1 || dx*dy==-1) && (allied[x][y]==Chessboard.BISHOP || allied[x][y]==Chessboard.QUEEN)) passthrough=true;	
									if (passthrough)
									{
										if (once) break;
										x+=dx; y+=dy;
										continue;
									}
								}
								if (empty[x][y]<=0.0) break;
							}
							else
							{ 
								if (empty[x][y]<1.0 && (pawn[x][y]>0.0 || piece[x][y]>0.0)) break; 
							} 
							if (once) break;
							chance *= (empty[x][y]);
							x+=dx; y+=dy;
						}
					}	
				} /*else
				{
					if (!isEmpty(k,j))
					{
						owner.globals.opponentControlledSquares++;
					}
				}*/
			}
	}
	
	public int attackPower(int x, int y)
	{
		int out = 0;
		int vecs[] = EvaluationGlobals.capVectors[Chessboard.QUEEN]; 
		int vecsY[] = EvaluationGlobals.capVectorsY[Chessboard.QUEEN];
		
		for (int k=0; k<vecs.length; k++)
		{
			float obstacle = 0.0f;
			int dx = vecs[k]; int dy = vecsY[k];
			int x2 = x+dx; int y2 = y+dy;
			boolean first = true;
			while (x2>=0 && y2>=0 && x2<8 && y2<8)
			{
				int pc = allied[x2][y2];
				if (pc==Chessboard.EMPTY)
				{
					first = false; x2+=dx; y2+=dy; continue;
				}
				if (dx==0 || dy==0)
				{
					if (pc==Chessboard.ROOK || pc==Chessboard.QUEEN || (pc==Chessboard.KING && first))
						/*if (obstacle<0.5)*/ out++;
					else break;
				} else
				{
					if (pc==Chessboard.BISHOP || pc==Chessboard.QUEEN || (pc==Chessboard.KING && first)
						|| (pc==Chessboard.PAWN && first && dy==(white?-1:1)))
						/*if (obstacle<0.5)*/ out++;
					else break;
				}
				obstacle += (1.0-empty[x2][y2]);
				x2+=dx; y2+=dy;
				first = false;
			}
		}
		vecs = EvaluationGlobals.capVectors[Chessboard.KNIGHT]; 
		vecsY = EvaluationGlobals.capVectorsY[Chessboard.KNIGHT];
		for (int k=0; k<vecs.length; k++)
		{
			int dx = vecs[k]; int dy = vecsY[k];
			int x2 = x+dx; int y2 = y+dy;
			if (x2>=0 && y2>=0 && x2<8 && y2<8 && allied[x2][y2]==Chessboard.KNIGHT) out++;
		}
		
		return out;
		
	}
	
	public float eval3(int set) 
	{
		float result = 0.0f;

		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				int pc = allied[k][j]; 
				switch (pc)
				{
				case Chessboard.PAWN: result += 1.0; break;
				case Chessboard.KNIGHT: result += 1.5; break;
				case Chessboard.BISHOP: result += 2.0; break;
				case Chessboard.ROOK: result += 2.5; break;
				case Chessboard.QUEEN: result += 3.0; break;
				//case Chessboard.KING: result += 3.0; break;
				}	
			}
		
		result -= (pieceTotal*15.0f/7.0f+pawnTotal)*0.8;
		return result;
	}
	
	public float eval(int set) {
		
		float result = 0.0f;
		
		computeProtectionMatrix(set,true);
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				int pc = allied[k][j]; 
				float val = 0.0f;
				if (pc!=Chessboard.EMPTY)
				{
					switch (pc)
					{
					case Chessboard.PAWN: val = 0.6f*(white? j : 7-j); break;
					case Chessboard.KNIGHT: val = 1.5f; break;
					case Chessboard.BISHOP: val = 2.0f; break;
					case Chessboard.ROOK: val = 3.0f; break;
					case Chessboard.QUEEN: val = 5.0f; break;
					}
					
					float danger = protectionLevel(k, j)*2.0f;
					float prot = protectionMatrix[set][k][j];
					if (danger>0.0)
					{
						//float delta = danger-prot;
						float delta = danger-prot;
						//if (prot==0.0) val=0.0; else
						if (delta>0.0) val*=Math.pow(0.75, delta);
					}
					result += val;
				} else
				{
					if (piece!=null) result+=piece[k][j]*piece[k][j]*protectionMatrix[set][k][j]*protectionMatrix[set][k][j]*2.0;
				}
			}
		
		//result -= protectionLevel(kingX,kingY)*0.04;
		
		float enemyResult = (pawnTotal*2.4f+pieceTotal*18.0f/7.0f)*0.9f;
		
		return 2.0f*((result/(result+enemyResult))-0.5f);
	}
	
	public float evalAlt(int set) {
		
		float result = 0.0f;
		
		computeProtectionMatrix(set,true);
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				int pc = allied[k][j]; 
				float val = 0.0f;
				if (pc!=Chessboard.EMPTY)
				{
					switch (pc)
					{
					case Chessboard.PAWN: val = 0.6f*(white? j : 7-j); break;
					case Chessboard.KNIGHT: val = 1.5f; break;
					case Chessboard.BISHOP: val = 2.0f; break;
					case Chessboard.ROOK: val = 3.0f; break;
					case Chessboard.QUEEN: val = 5.0f; break;
					}
					
					float danger = protectionLevel(k, j)*2.0f;
					float prot = protectionMatrix[set][k][j];
					if (danger<0.1) result += val;
					else if (danger<1.0)
					{
						if (prot<1.0) result += val/2.0f; else result += val;
					}
					else if (danger<2.0)
					{
						if (prot>=2.0) result += val;
						else if (prot>=1.0) result += val/2.0f;
						else result += val/4.0f;
					}
					else
					{
						if (prot>=3.0) result += val;
						else if (prot>=2.0) result += val/2.0f;
						else if (prot>=1.0) result += val/4.0f;
					}
				}/* else
				{
					if (piece!=null) result+=piece[k][j]*piece[k][j]*protectionMatrix[set][k][j]*protectionMatrix[set][k][j]*2.0;
				}*/
			}
		
		//result -= protectionLevel(kingX,kingY)*0.04;
		
		float enemyResult = (pawnTotal*2.4f+pieceTotal*18.0f/7.0f)*0.9f;
		
		return 2.0f*((result/(result+enemyResult))-0.5f);
	}
	
	
	public float eval(int set, Move m, MCState after) {
		// TODO Auto-generated method stub
		return after.eval(set);
	}
	
	public float eval2(int set)
	{
		float out = 0.0f;
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				int pc = allied[k][j]; 
				if (pc!=Chessboard.EMPTY)
				{
					switch (pc)
					{
					case Chessboard.PAWN: out += 0.6*(white? j : 7-j); break;
					case Chessboard.KNIGHT: out += 1.5; break;
					case Chessboard.BISHOP: out += 2.0; break;
					case Chessboard.ROOK: out += 3.0; break;
					case Chessboard.QUEEN: out += 5.0; break;
					}
				}
			}
		out -= (pawnTotal*2.4+pieceTotal*18.0/7.0);
		return out;
	}
	
	public float pieceValue(int x, int y)
	{
		switch (allied[x][y])
		{
		case Chessboard.PAWN: return 0.6f*(white? y : 7-y);
		case Chessboard.KNIGHT: return 1.5f; 
		case Chessboard.BISHOP: return 2.0f;
		case Chessboard.ROOK: return 3.0f;
		case Chessboard.QUEEN: return 5.0f;
		}
		return 0.0f;
	}
	
	public float risk(int set)
	{
		float out = 0.0f;
		computeProtectionMatrix(set,true);
			
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				int pc = allied[k][j]; 
				float val = 0.0f;
				if (pc!=Chessboard.EMPTY)
				{
					switch (pc)
					{
					case Chessboard.PAWN: val = 0.6f*(white? j : 7-j); break;
					case Chessboard.KNIGHT: val = 1.5f; break;
					case Chessboard.BISHOP: val = 2.0f; break;
					case Chessboard.ROOK: val = 3.0f; break;
					case Chessboard.QUEEN: val = 5.0f; break;
					}
					float plevel = protectionLevel(k, j)/(pieceTotal+1.0f);
					float dng = 1.0f*danger[k][j]/100.0f;
					dng /= Math.pow(2.0, protectionMatrix[set][k][j]);
					float loss = val*plevel*dng;
					if (loss>val) loss = val;
					out+=loss;
				}
			}
		
		return out;
	}
	
	public float getHeuristicBias(int set, Move m)
	{
		float result = 0.0f;
		int pc = m.piece;
		float val = 0.0f;
		//return 0.0;
		switch (pc)
		{
		case Chessboard.PAWN: val = 1.0f; break;
		case Chessboard.KNIGHT: val = 1.0f; break;
		case Chessboard.BISHOP: val = 1.5f; break;
		case Chessboard.ROOK: val = 2.0f; break;
		case Chessboard.QUEEN: val = 3.0f; break;
		case Chessboard.KING: val = 1.0f; break;
		}
		if (m.piece!=Chessboard.PAWN) result -= chanceOfPawnTries(m)*val;
		float risk = protectionLevel(m.toX, m.toY);
		float reward = pawn[m.toX][m.toY]+piece[m.toX][m.toY]*12.0f/7.0f;
		//if (risk<1.0) risk=1.0;
		result += reward;
		if (risk>0.0)
		{
			int power = attackPower(m.toX,m.toY);
			if (power<2) result-=val; else result-=(1.0*val/power);
		}
		float legal = chanceOfLegality(m);
		/*if (legal<0.01 && pc==Chessboard.PAWN)
		{
			float rep = chanceOfLegality(m);
			rep = rep+0.01;
		}*/
		if (legal<0.25) result -= (1.0-legal)*1.25;
		if (Chessboard.KING==m.piece) result-=1.0;
		
		return result*0.025f;
	}
	
	public float piecesInBetween(int x1, int y1, int x2, int y2)
	{
		int sw;
		float count = 0;
		float chance = 1.0f;
		
		if (x1>x2) { sw = x1; x1 = x2; x2 = sw; }
		if (y1>y2) { sw = y1; y1 = y2; y2 = sw; }
		
		boolean xx = (x2-x1)>0;
		boolean yy = (y2-y1)>0;
		
		for (int x=x1; x<=x2; x++)
			for (int y=y1; y<=y2; y++)
			{
				boolean inside = (xx && x!=x1 && x!=x2) || (yy && y!=y1 && y!=y2);
				if (allied[x][y]!=Chessboard.EMPTY)
				{
					if (inside && (allied[x][y]==Chessboard.ROOK || allied[x][y]==Chessboard.QUEEN)) continue;
					count++;
					if (inside && allied[x][y]==Chessboard.PAWN) count+=Float.POSITIVE_INFINITY; //a pawn should not be relied on to move
				}
				else if (inside) chance/=empty[x][y];
			}
		if (count<2.0f) count*=chance;
		return count;
	}
	
	public float piecesInBetweenNoRooks(int x1, int y1, int x2, int y2)
	{
		int sw;
		float count = 0;
		float chance = 1.0f;
		
		if (x1>x2) { sw = x1; x1 = x2; x2 = sw; }
		if (y1>y2) { sw = y1; y1 = y2; y2 = sw; }
		
		boolean xx = (x2-x1)>0;
		boolean yy = (y2-y1)>0;
		
		for (int x=x1; x<=x2; x++)
			for (int y=y1; y<=y2; y++)
			{
				boolean inside = (xx && x!=x1 && x!=x2) || (yy && y!=y1 && y!=y2);
				if (allied[x][y]!=Chessboard.EMPTY)
				{
					count++;
					if (inside && allied[x][y]==Chessboard.PAWN) count+=Float.POSITIVE_INFINITY; //a pawn should not be relied on to move
				}
				else if (inside) chance/=empty[x][y];
			}
		if (count<2.0f) count*=chance;
		return count;
	}
	
	public float piecesInBetweenDiagonal(int x1, int y1, int x2, int y2)
	{
		int sw;
		float count = 0;
		float chance = 1.0f;
		
		if (x1>x2) { sw = x1; x1 = x2; x2 = sw; sw = y1; y1 = y2; y2 = sw; }
		//if (y1>y2) { sw = y1; y1 = y2; y2 = sw; }
		
		if (x1<0)
		{
			if (y2>y1) y1-=x1; else y1+=x1;
			x1 = 0;
		}
		if (x2>7)
		{
			if (y2>y1) y2-=(x2-7); else y2+=(x2-7);
			x2 = 7;
		}
		
		int y=y1;
		for (int x=x1; x<=x2; x++)
		{
			boolean inside = y>=0 && y<8 && x!=x1 && x!=x2;
			if (y>=0 && y<8 && allied[x][y]!=Chessboard.EMPTY) 
			{
				if (!inside || (allied[x][y]!=Chessboard.QUEEN && allied[x][y]!=Chessboard.BISHOP)) count++;
				if (inside && allied[x][y]==Chessboard.PAWN) count+=Float.POSITIVE_INFINITY; //a pawn should not be relied on to move
			}
			if (y==y2) break;
			if (inside && allied[x][y]==Chessboard.EMPTY) chance /= empty[x][y];
			if (y2>y1) y++; else y--;
		}
		
		if (count<2) count*=chance;
		return count;
	}
	
	public long getZobrist()
	{
		long out = 0;
		int k,j;

		for (k=0; k<8; k++)
			for (j=0; j<8; j++)
				if (allied[k][j]!=Chessboard.EMPTY)
				{
					out ^= Zobrist.pieces[allied[k][j]][k][j];
				}
		
		if (castleLeft) out ^= Zobrist.castling[0];
		if (castleRight) out ^= Zobrist.castling[1];
		
		return out;
	}
	
	public static long getZobrist(long start, short move)
	{
		int piece = (int)((move>>12)&7);
		int fromx = (int)((move>>9)&7);
		int fromy = (int)((move>>6)&7);
		int tox = (int)((move>>3)&7);
		int toy = (int)(move&7);
		start ^= Zobrist.pieces[piece][fromx][fromy];
		start ^= Zobrist.pieces[piece][tox][toy];
		if (piece==Chessboard.KING)
		{
			if (tox-fromx==-2) 
			{
				start ^= Zobrist.castling[0];
				start ^= Zobrist.pieces[Chessboard.ROOK][0][fromy];
				start ^= Zobrist.pieces[Chessboard.ROOK][3][fromy];
			}
			if (tox-fromx==-2) 
			{
				start ^= Zobrist.castling[1];
				start ^= Zobrist.pieces[Chessboard.ROOK][7][fromy];
				start ^= Zobrist.pieces[Chessboard.ROOK][5][fromy];
			}
		}
		return start;
	}
	
	public Move[] getMoves(int set) 
	{
		return generateMoves(set,true,owner);
	}
	public MCState getState(int set,Move move) 
	{
		return evolveWithPlayerMove(set,move, -1, -1, Chessboard.NO_CAPTURE, Chessboard.NO_CHECK, Chessboard.NO_CHECK, 0);
	}
	public MCState[] getStates(int set,Move[] moves) 
	{
		Uberposition[] u = new Uberposition[moves.length];
		for (int k=0; k<moves.length; k++) 
			u[k] = evolveWithPlayerMove(set,moves[k], -1, -1, Chessboard.NO_CAPTURE, Chessboard.NO_CHECK, Chessboard.NO_CHECK, 0);
		return null;
	}

	public float chanceOfMessage(int set, Move m, int msg, int submsg) {
		// TODO Auto-generated method stub
		if (msg==MCState.MSG_SILENT) return chanceOfLegality(m);
		else return 1.0f-chanceOfLegality(m);
	}
	
	public float attackProgress(int x, int y, Move m)
	{
		float out = 0.0f;
		float temp;
		int dx,dy;
		
		if (m!=null)
		{
			//do move
			allied[m.toX][m.toY] = allied[m.fromX][m.fromY];
			allied[m.fromX][m.fromY] = Chessboard.EMPTY;
		}
		
		/*if (allied[x][y]!=Chessboard.EMPTY) out = Float.POSITIVE_INFINITY;
		else
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				float partial = 0.0f;
				int p = allied[k][j];
				if (p==Chessboard.EMPTY) continue;
				
				switch (p)
				{
				case Chessboard.PAWN:
					//partial = 0.0f; break;
					if (x<=k+1 && x>=k-1)
					{
						if (y==j-1 && !white) partial = 1.0f;
						if (y==j+1 && white) partial = 1.0f;
					}
					break;
					/*if (x<k-1 || x>k+1) break;
					if (y==j) break;
					if (j>y && white) break;
					if (j<y && !white) break;
					partial = 1.0f/(y-j);
					if (partial<0.0) partial = -partial;
					if (partial<1.0)
					{
						temp = piecesInBetween(k, y, k, j);
						if (temp==0) break; //weird, shouldn't happen
						partial*=1.0/temp;
					} //else { directAttacks++; actualDirectAttacks += 1.0f; }
					break;
					
				case Chessboard.KING:
					dx = k-x; if (dx<0) dx = -dx;
					dy = j-y; if (dy<0) dy = -dy;
					if (dy>dx) dx = dy;
					partial = 1.0f/dx;
					//if (partial==1.0f) { directAttacks++; actualDirectAttacks += 1.0f; }
					break;
					
				case Chessboard.KNIGHT:
					dx = k-x; if (dx<0) dx = -dx;
					dy = j-y; if (dy<0) dy = -dy;
					partial = knightProximity[dx][dy];
					//if (partial==1.0f) { directAttacks++; actualDirectAttacks += 1.0f; }
					break;
					
				case Chessboard.BISHOP:
					if (((x+y)%2)!=((k+j)%2)) break; //square of a different color, don't bother
					dx = (x+y-k-j)/2; if (dx<0) dx = -dx;
					dy = (x-y-k+j)/2; if (dy<0) dy = -dy;
					int di = dx;
					if (di>dy) di = dy;
					if (di==0)
					{
						partial = 1.0f/piecesInBetweenDiagonal(x, y, k, j);
						break;
					}
					int a = (x+y-k-j)/2;
					int a2 = (x-y-k+j)/2;
					float obstacles = piecesInBetweenDiagonal(k, j, k+a, j+a) +
					piecesInBetweenDiagonal(k, j, k-a2, j+a2);
					partial = 1.0f/(obstacles+1.0f);
					break;
					
				case Chessboard.ROOK:
					if (x==k || y==j)
					{
						partial = 1.0f/piecesInBetween(k,j,x,y);
						break;
					}
					obstacles = piecesInBetween(k,j,k,y) + piecesInBetween(k,j,x,j);
					partial = 1.0f/(obstacles+1.0f);
					break;
					
				case Chessboard.QUEEN:
					//it's everything a bishop and a rook are
					if (((x+y)%2)==((k+j)%2))
					{
						dx = (x+y-k-j)/2; if (dx<0) dx = -dx;
						dy = (x-y-k+j)/2; if (dy<0) dy = -dy;
						di = dx;
						if (di>dy) di = dy;
						if (di==0)
						{
							partial = 1.0f/piecesInBetweenDiagonal(x, y, k, j);
							break;
						}
					}
					if (x==k || y==j)
					{
						partial = 1.0f/piecesInBetween(k,j,x,y);
						break;
					}
					//the queen is quite maneuverable... let's try something simple
					dx = x - k; dy = y - j;
					di = k + dy;
					obstacles = 0;
					if (di>=0 && di<8) obstacles += (piecesInBetweenDiagonal(k, j, di, y)-1); else obstacles += 2;
					di = k - dy;
					if (di>=0 && di<8) obstacles += (piecesInBetweenDiagonal(k, j, di, y)-1); else obstacles += 2;
					di = j + dx;
					if (di>=0 && di<8) obstacles += (piecesInBetweenDiagonal(k, j, x, di)-1); else obstacles += 2;
					di = j - dx;
					if (di>=0 && di<8) obstacles += (piecesInBetweenDiagonal(k, j, x, di)-1); else obstacles += 2;
					obstacles += piecesInBetween(k,j,k,y) + piecesInBetween(k,j,x,j) - 2;
					partial = 1.0f/(1.0f+obstacles);
					break;
					
				}
				
				out += partial;
			}*/
		
		if (allied[x][y]!=Chessboard.EMPTY) 
		{
			out = Float.POSITIVE_INFINITY;
		}
		else
		{
			out = attackPower(x, y);
			if (m!=null && m.piece!=Chessboard.PAWN) out++;
		}
		
		if (m!=null)
		{
			//undo move
			allied[m.fromX][m.fromY] = allied[m.toX][m.toY];
			allied[m.toX][m.toY] = Chessboard.EMPTY;
		}
		
		return out;
		
	}
	
	
	public String toString()
	{
		String o = "";
		
		for (int j=7; j>=0; j--)
		{
			for (int k=0; k<8; k++)
			{
				if (allied[k][j]!=Chessboard.EMPTY)
				{
					switch (allied[k][j])
					{
						case Chessboard.PAWN: o+="P"; break;
						case Chessboard.KNIGHT: o+="N"; break;
						case Chessboard.BISHOP: o+="B"; break;
						case Chessboard.ROOK: o+="R"; break;
						case Chessboard.QUEEN: o+="Q"; break;
						case Chessboard.KING: o+="K"; break;
					}
				} else
				{
					if (empty[k][j]>=1.0) o+=" ";
					else if (empty[k][j]>0.9) o+="0";
					else if (empty[k][j]>0.8) o+="1";
					else if (empty[k][j]>0.7) o+="2";
					else if (empty[k][j]>0.6) o+="3";
					else if (empty[k][j]>0.5) o+="4";
					else if (empty[k][j]>0.4) o+="5";
					else if (empty[k][j]>0.3) o+="6";
					else if (empty[k][j]>0.2) o+="7";
					else if (empty[k][j]>0.1) o+="8";
					else if (empty[k][j]>0.0) o+="9";
					else o+="*";
				}
			}
			o+="\n";
		}
		return o;
	}
	
	public String toString2()
	{
		String o = "";
		
		for (int j=7; j>=0; j--)
		{
			for (int k=0; k<8; k++)
			{
				if (allied[k][j]!=Chessboard.EMPTY)
				{
					switch (allied[k][j])
					{
						case Chessboard.PAWN: o+="P"; break;
						case Chessboard.KNIGHT: o+="N"; break;
						case Chessboard.BISHOP: o+="B"; break;
						case Chessboard.ROOK: o+="R"; break;
						case Chessboard.QUEEN: o+="Q"; break;
						case Chessboard.KING: o+="K"; break;
					}
				} else
				{
					if (king[k][j]>=1.0) o+="*";
					else if (king[k][j]>0.9) o+="9";
					else if (king[k][j]>0.8) o+="8";
					else if (king[k][j]>0.7) o+="7";
					else if (king[k][j]>0.6) o+="6";
					else if (king[k][j]>0.5) o+="5";
					else if (king[k][j]>0.4) o+="4";
					else if (king[k][j]>0.3) o+="3";
					else if (king[k][j]>0.2) o+="2";
					else if (king[k][j]>0.1) o+="1";
					else if (king[k][j]>0.0) o+="0";
					else o+=" ";
				}
			}
			o+="\n";
		}
		return o;
	}
	
	public static void main(String args[])
	{

		String path = System.getProperty("user.home") + "/darkboard_data/";
		// System.out.println(path);
		Darkboard.initialize(path);

		
		Uberposition up = new Uberposition(true,null);
		// System.out.println(up);
		
		up = up.evolveWithOpponentMove(0,0, -1, -1, Chessboard.NO_CHECK, Chessboard.NO_CHECK);
		// System.out.println(up);
		Move m = new Move();
		m.fromX = 4; m.fromY = 1; m.toX = 4; m.toY = 2; m.piece = Chessboard.PAWN;
		up = up.evolveWithPlayerMove(0,m, -1, -1, Chessboard.NO_CAPTURE, Chessboard.NO_CHECK, Chessboard.NO_CHECK, 0);
		// System.out.println(up);
		up = up.evolveWithOpponentMove(0,0, -1, -1, Chessboard.NO_CHECK, Chessboard.NO_CHECK);
		// System.out.println(up);
		m.fromX = 3; m.fromY = 0; m.toX = 5; m.toY = 2; m.piece = Chessboard.QUEEN;
		up = up.evolveWithPlayerMove(0,m, -1, -1, Chessboard.NO_CAPTURE, Chessboard.NO_CHECK, Chessboard.NO_CHECK, 0);
		// System.out.println(up);
		up = up.evolveWithOpponentMove(0,0, -1, -1, Chessboard.NO_CHECK, Chessboard.NO_CHECK);
		// System.out.println(up);
		m.fromX = 5; m.fromY = 2; m.toX = 0; m.toY = 7;
		up = up.evolveWithPlayerMove(0,m, 0, 7, Chessboard.CAPTURE_PIECE, Chessboard.NO_CHECK, Chessboard.NO_CHECK, 0);
		// System.out.println(up);
		up = up.evolveWithOpponentMove(0,0, -1, -1, Chessboard.NO_CHECK, Chessboard.NO_CHECK);
		// System.out.println(up);
		m.fromX = 0; m.fromY = 7; m.toX = 5; m.toY = 2;
		up = up.evolveWithPlayerMove(0,m, -1, -1, Chessboard.NO_CAPTURE, Chessboard.NO_CHECK, Chessboard.NO_CHECK, 0);
		// System.out.println(up);
		up = up.evolveWithOpponentMove(0,0, -1, -1, Chessboard.NO_CHECK, Chessboard.NO_CHECK);
		// System.out.println(up);
		
		
		Properties p = System.getProperties();
		Enumeration<Object> e = p.keys();
		while (e.hasMoreElements())
		{
			Object key = e.nextElement();
			Object val = p.get(key);
			// System.out.println(key+" -> "+val);
		}
		
		/*Move m[] = up.generateMoves(false, null);
		for (int k=0; k<m.length; k++) System.out.println(m[k]);
		
		for (int k=0; k<m.length; k++) System.out.println(up.getState(m[k]));*/
	}

	public boolean isBroken() 
	{
		return broken;
	}

}
