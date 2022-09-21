/*
 * Created on 3-apr-05
 *
 */
package core;

import java.text.DecimalFormat;
import java.util.Vector;

//temp imports
/**
 * @author Nikola Novarlic
 * This class encloses all the data for maintaining the chessboard,
 * including the probability information of enemy pieces and the
 * umpire messages.
 */
public class Chessboard implements Cloneable {
	
	/* Constants. */
	public static final int PAWN = 0;
	public static final int KNIGHT = 1;
	public static final int BISHOP = 2;
	public static final int ROOK = 3;
	public static final int QUEEN = 4;
	public static final int KING = 5;
	public static final int EMPTY = 6;
	
	public static final int NO_CHECK = 0;
	public static final int CHECK_FILE = 1;
	public static final int CHECK_RANK = 2;
	public static final int CHECK_LONG_DIAGONAL = 3;
	public static final int CHECK_SHORT_DIAGONAL = 4;
	public static final int CHECK_KNIGHT = 5;
	
	public static final int NO_CAPTURE = 0;
	public static final int CAPTURE_PAWN = 1;
	public static final int CAPTURE_PIECE = 2;
	
	public static final int rookOffsetX[] = { 0,1,0,-1};
	public static final int rookOffsetY[] = { 1,0,-1,0};
	public static final int bishopOffsetX[] = { 1,1,-1,-1};
	public static final int bishopOffsetY[] = { 1,-1,-1,1};
	public static final int queenOffsetX[] = { 0,1,0,-1,1,1,-1,-1};
	public static final int queenOffsetY[] = { 1,0,-1,0,1,-1,-1,1};
	public static final int knightOffsetX[] = { 1,1,2,2,-1,-1,-2,-2};
	public static final int knightOffsetY[] = { 2,-2,1,-1,2,-2,1,-1};
	public static final int pawnOffsetX[] = { -1,1};
	public static final int pawnOffsetWhiteY[] = { 1,1};
	public static final int pawnOffsetBlackY[] = { -1,-1};
	public static final int pawnMoveOffsetX[] = { 0 };
	public static final int pawnMoveOffsetYWhite[] = { 1 };
	public static final int pawnMoveOffsetYBlack[] = { -1 };
	
	public static int savedComputations = 0;
		
	public boolean searchMatrix[][] = new boolean[8][8]; //temp matrix.
	public double controlMatrix[][] = new double[8][8];
	
	
	/* For each tile, the probabilities of each piece being there. Total is 1.0. */
	public double probabilityInfo[][][] = new double[8][8][7];
	
	/* the total probability for each piece on the chessboard. */
	public double globalPieceProbability[] = new double[6];
	
	/* Where friendly pieces are. */
	public int friendlyPieces[][] = new int[8][8];
	public int kingX, kingY; //MUSTMUSTMUST be kept up-to-date!!!!!!!
	
	public double opponentInterest[][] = new double[8][8];
	
	/* minimum and maximum pawns on each column. */
	int minPawns[] = new int[8];
	int maxPawns[] = new int[8];
	
	/* Boolean Flags (en passant info is unimportant in Kriegspiel) */
	boolean isWhite = true;
	boolean hasCastled = false;
	boolean kingMoved = false;
	boolean leftRookMoved = false;
	boolean rightRookMoved = false;
	
	Vector moveList = null;
	
	/* A Vector containing the moves tried and rejected. */
	Vector illegalMoves;
	Vector illegalMoveBlackListDuration;
	
	/* Umpire messages. */
	public boolean capturedPawn;
	public boolean capturedPiece;
	public boolean underCheck;
	public boolean doubleCheck;
	public int checkType;
	public int checkType2;
	public int pawnTries;
	
	/* Last ply's opponent messages. */
	public boolean opponentCapturedPawn;
	public boolean opponentCapturedPiece;
	public boolean opponentUnderCheck;
	public boolean opponentDoubleCheck;
	public int opponentCheckType;
	public int opponentCheckType2;
	public int opponentPawnTries;
	
	/* A list of squares containing the "line of fire" of our pieces in a check */
	public int checkTargetSquaresX[] = new int[32];
	public int checkTargetSquaresY[] = new int[32];
	public int checkTargets;
	public int checkCandidates; //the number of pieces that might be checking. They are
	//the first entries in the checkTargetSquares arrays.


	/**
	 * Creates a chessboard with the default piece disposition.
	 * @param white The player's side. Unlike chess, there is no major advantage
	 * from playing white in Kriegspiel.
	 */
	public Chessboard(boolean white)
	{
		int k,j,i;
		isWhite = white;
		for (k=0; k<8; k++) for (j=0; j<8; j++) for (i=0; i<7; i++)
			probabilityInfo[k][j][i] = 0.0;
		for (k=0; k<8; k++) for (j=0; j<8; j++) friendlyPieces[k][j] = EMPTY;
		if (white)
		{
			for (k=0; k<8; k++) friendlyPieces[k][1] = PAWN;
			friendlyPieces[0][0] = friendlyPieces[7][0] = ROOK;
			friendlyPieces[1][0] = friendlyPieces[6][0] = KNIGHT;
			friendlyPieces[2][0] = friendlyPieces[5][0] = BISHOP;
			friendlyPieces[3][0] = QUEEN; friendlyPieces[4][0] = KING;
			kingX = 4; kingY = 0;
			for (k=0; k<8; k++) probabilityInfo[k][6][PAWN] = 1.0;
			//for (k=0; k<8; k++) for (j=2; j<6; j++) probabilityInfo[k][j][PAWN] = 0.2;
			probabilityInfo[0][7][ROOK] = probabilityInfo[7][7][ROOK] = 1.0;
			probabilityInfo[1][7][KNIGHT] = probabilityInfo[6][7][KNIGHT] = 1.0;
			probabilityInfo[2][7][BISHOP] = probabilityInfo[5][7][BISHOP] = 1.0;
			probabilityInfo[3][7][QUEEN] = 1.0; probabilityInfo[4][7][KING] = 1.0;
		} else
		{
			for (k=0; k<8; k++) friendlyPieces[k][6] = PAWN;
			friendlyPieces[0][7] = friendlyPieces[7][7] = ROOK;
			friendlyPieces[1][7] = friendlyPieces[6][7] = KNIGHT;
			friendlyPieces[2][7] = friendlyPieces[5][7] = BISHOP;
			friendlyPieces[3][7] = QUEEN; friendlyPieces[4][7] = KING;
			kingX = 4; kingY = 7;
			for (k=0; k<8; k++) probabilityInfo[k][1][PAWN] = 1.0;
			probabilityInfo[0][0][ROOK] = probabilityInfo[7][0][ROOK] = 1.0;
			probabilityInfo[1][0][KNIGHT] = probabilityInfo[6][0][KNIGHT] = 1.0;
			probabilityInfo[2][0][BISHOP] = probabilityInfo[5][0][BISHOP] = 1.0;
			probabilityInfo[3][0][QUEEN] = 1.0; probabilityInfo[4][0][KING] = 1.0;
		}
		
		for (k=0; k<8; k++) for (j=0; j<8; j++) updateEmptyProbability(j,k);
		for (k=0; k<6; k++) this.globalPieceProbability[k] = totalProbability(k);
		
		for (k=0; k<8; k++) minPawns[k] = maxPawns[k] = 1;
		//illegalMoves = new Vector();
		//only init illegalMoves if necessary...
		
		for (k=0; k<8; k++) for (j=0; j<8; j++) opponentInterest[k][j] = 1.0;
		
		computeControlMatrix();
		
	}
	
	/**
	 * Defines its own cloning procedure.
	 */
	public Object clone()
	{
		try {
			Object o = super.clone();
			Chessboard c = ((Chessboard)o);
			if (illegalMoves!=null) c.illegalMoves = (Vector)illegalMoves.clone();
			if (illegalMoveBlackListDuration!=null) c.illegalMoveBlackListDuration = (Vector)illegalMoveBlackListDuration.clone();
			c.moveList = null;
			//clone the arrays.
			c.probabilityInfo = new double[8][8][7];
			c.globalPieceProbability = new double[6];
			c.friendlyPieces = new int[8][8];
			c.opponentInterest = new double[8][8];
			c.controlMatrix = new double[8][8];
			c.minPawns = new int[8];
			c.maxPawns = new int[8];
			c.checkTargetSquaresX = new int[32];
			c.checkTargetSquaresY = new int[32];
			for (int k=0; k<32; k++) 
			{
				c.checkTargetSquaresX[k] = this.checkTargetSquaresX[k];
				c.checkTargetSquaresY[k] = this.checkTargetSquaresY[k];
			}
			for (int k=0; k<8; k++) for (int j=0; j<8; j++) for (int i=0; i<7; i++)
				c.probabilityInfo[k][j][i] = this.probabilityInfo[k][j][i];
			for (int k=0; k<8; k++) for (int j=0; j<8; j++) 
				c.friendlyPieces[k][j] = this.friendlyPieces[k][j];
			for (int k=0; k<8; k++) for (int j=0; j<8; j++) 
			{
				c.opponentInterest[k][j] = this.opponentInterest[k][j];
				c.controlMatrix[k][j] = this.controlMatrix[k][j];
			}
			for (int k=0; k<6; k++) c.globalPieceProbability[k] = this.globalPieceProbability[k];
			for (int k=0; k<8; k++) c.minPawns[k] = this.minPawns[k];
			for (int k=0; k<8; k++) c.maxPawns[k] = this.maxPawns[k];
			return o;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns a copy of the current chessboard, stripped of enemy probability data. Useful
	 * for evolving the probabilities on a new copy of this chessboard.
	 * @return
	 */
	public Chessboard duplicateWithoutProbabilityData()
	{
		Chessboard c2 = (Chessboard)clone();
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				for (int i=0; i<6; i++)
					c2.probabilityInfo[k][j][i] = 0.0;
					
				c2.probabilityInfo[k][j][EMPTY] = 1.0;
			}
		
		return c2;
	}

	/**
	 * Calculates the entropy (lack of information) of the piece distribution on a given
	 * square.
	 * @param x
	 * @param y
	 * @return the entropy value
	 */
	public double entropy(int x, int y)
	{
		double result = 0.0;
		for (int i=0; i<7; i++)
		{
			if (probabilityInfo[x][y][i]>0.0)
				result -= probabilityInfo[x][y][i] * Math.log(probabilityInfo[x][y][i]);
		}
		return result; //technically, we should multiply by a constant; not here.
	}
	
	/**
	 * Calculates the total entropy on the whole chessboard.
	 * @return the total entropy.
	 */
	public double entropy()
	{
		double result = 0.0;
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				if (isUncertain(k,j)) result += entropy(k,j);
			}
		return result;
	}
	
	/**
	 * Returns true if there is absolute certainty a square is free.
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isFree(int x, int y)
	{
		return (probabilityInfo[x][y][EMPTY]>=1.0 && isUncertain(x,y));
	}
	
	/**
	 * Probability of a piece with straight movement being in the square.
	 * @param x
	 * @param y
	 * @return
	 */
	public double straightPieceProbability(int x, int y)
	{
		return (probabilityInfo[x][y][ROOK]+probabilityInfo[x][y][QUEEN]);
	}
	
	/**
	 * Probability of a piece with diagonal movement being in the square.
	 * @param x
	 * @param y
	 * @return
	 */
	public double obliquePieceProbability(int x, int y)
	{
		return (probabilityInfo[x][y][BISHOP]+probabilityInfo[x][y][QUEEN]);
	}
	
	/**
	 * The probability of a given square being occupied.
	 * @param x
	 * @param y
	 * @return
	 */
	public double occupiedProbability(int x, int y)
	{
		try {
			if (friendlyPieces[x][y]!=EMPTY) return 1.0; 
			else return (1.0 - probabilityInfo[x][y][EMPTY]);
		} catch (RuntimeException e) {
			// System.out.println(x+" "+y);
			e.printStackTrace();
			return 0.0;
		}
	}
	
	/**
	 * Checks whether a given move has been declared illegal (because the umpire said so),
	 * and must therefore be discarded for the next attempt.
	 * @param m
	 * @return
	 */
	public boolean moveBanned(core.Move m)
	{
		for (int k=0; k<illegalMoves.size(); k++)
		{
			core.Move m2 = ((core.Move)illegalMoves.get(k));
			if (m.fromX==m2.fromX && m.fromY==m2.fromY && m.toX==m2.toX && m.toY==m2.toY)
				return true;
		}
		return false;
	}
	
	/**
	 * A variant where coordinates are provided instead of a Move object for convenience.
	 * @param fx
	 * @param fy
	 * @param tx
	 * @param ty
	 * @return
	 */
	public boolean moveBanned(int fx, int fy, int tx, int ty)
	{
		if (illegalMoves==null) return false;
		for (int k=0; k<illegalMoves.size(); k++)
		{
			core.Move m2 = ((core.Move)illegalMoves.get(k));
			if (fx==m2.fromX && fy==m2.fromY && tx==m2.toX && ty==m2.toY)
				return true;
		}
		return false;
	}
	
	/**
	 * Adds a move to the list of banned moves.
	 * @param m
	 */
	public void banMove(core.Move m)
	{
		if (illegalMoves==null) illegalMoves = new Vector();
		if (illegalMoveBlackListDuration==null) illegalMoveBlackListDuration = new Vector();
		illegalMoves.add(m);
		illegalMoveBlackListDuration.add(new Integer(5)); //better algorithm coming later...
		
		if (moveList!=null)
		{
			for (int k=0; k<moveList.size(); k++)
			{
				core.Move m2 = ((core.Move)moveList.get(k));
				if (m.fromX==m2.fromX && m.fromY==m2.fromY && m.toX==m2.toX && m.toY==m2.toY)
				{
					moveList.remove(k);
					return;
				}
							
			}
		}
	}
	
	public void unbanAllMoves()
	{
		illegalMoves.clear();
		illegalMoveBlackListDuration.clear();
		moveList = null;
	}
	
	/**
	 * Lists all the pseudolegal (not trivially illegal) moves for a king in the given position.
	 * @param x
	 * @param y
	 * @param outVector
	 * @param createVector
	 * @return the number of pseudolegal moves for the King.
	 */
	public int pseudolegalKingMoves(int x, int y, Vector outVector, boolean createVector)
	{
		int moveNumber = 0;
		int offX[] = {-1,-1,-1,0,0,1,1,1};
		int offY[] = {-1,0,1,-1,1,-1,0,1};
		
		for (int k=0; k<8; k++)
		{
			int x2 = x+offX[k];
			int y2 = y+offY[k];
			if (x2>=0 && y2>=0 && x2<8 && y2<8 && isUncertain(x2,y2) && !moveBanned(x,y,x2,y2))
			{
				//this is all we can conclude. Even if we are under check, the King
				//might still be able to move in all directions if the attacking piece
				//is adjacent to it.
				moveNumber++;
				if (createVector)
				{
					core.Move m = new core.Move();
					m.piece = KING;
					m.fromX = (byte)x; m.fromY = (byte)y; m.toX = (byte)x2; m.toY = (byte)y2;
					outVector.add(m);
				}
			}
			
		}
		
		//castling checks.
		if (!hasCastled && !kingMoved && !leftRookMoved && checkType==NO_CHECK
		&& !moveBanned(x,y,x-2,y))
		{
			if (isUncertain(x-1,y) && isUncertain(x-2,y) && isUncertain(x-3,y))
			{
				//long side castle 
				moveNumber++;
				if (createVector)
				{
					core.Move m = new core.Move();
					m.piece = KING;
					m.fromX = (byte)x; m.fromY = (byte)y; m.toX = (byte)(x-2); m.toY = (byte)y;
					outVector.add(m);
				}				
			}
		}
		
		if (!hasCastled && !kingMoved && !rightRookMoved && checkType==NO_CHECK)
		{
			if (isUncertain(x+1,y) && isUncertain(x+2,y) && !moveBanned(x,y,x+2,y))
			{
				//short side castle 
				moveNumber++;
				if (createVector)
				{
					core.Move m = new core.Move();
					m.piece = KING;
					m.fromX = (byte)x; m.fromY = (byte)y; m.toX = (byte)(x+2); m.toY = (byte)y;
					outVector.add(m);
				}				
			}
		}
		
		return moveNumber;
	}
	
	/**
	 * Computes the possible pseudolegal moves for a pawn.
	 * @param x
	 * @param y
	 * @param outVector
	 * @param createVector
	 * @return
	 */
	public int pseudolegalPawnMoves(int x, int y, Vector outVector, boolean createVector)
	{
		int moveNumber = 0;
		int possibleMovesX[] = new int[4];
		int possibleMovesY[] = new int[4];
		int offset = (isWhite? 1 : -1); //pawns move in different directions!
		int startingRank = (isWhite? 1 : 6); //starting rank for different pawns...
		int finalRank = (isWhite? 7 : 0); //promotion rank
		
		//move forward by 1
		if (isUncertain(x,y+offset))
		{
			possibleMovesX[moveNumber]=x;
			possibleMovesY[moveNumber]=y+offset;
			moveNumber++;
		}
		//move forward by 2 if first move
		if (y==startingRank && isUncertain(x,y+offset) && isUncertain(x,y+offset+offset))
		{
			possibleMovesX[moveNumber]=x;
			possibleMovesY[moveNumber]=y+offset+offset;
			moveNumber++;
		}
		//capture if pawn tries...
		if (pawnTries>0 && x>0 && isUncertain(x-1,y+offset) && probabilityInfo[x-1][y+offset][EMPTY]<1.0)
		{
			possibleMovesX[moveNumber]=x-1;
			possibleMovesY[moveNumber]=y+offset;
			moveNumber++;
		}
		if (pawnTries>0 && x<7 && isUncertain(x+1,y+offset) && probabilityInfo[x+1][y+offset][EMPTY]<1.0)
		{
			possibleMovesX[moveNumber]=x+1;
			possibleMovesY[moveNumber]=y+offset;
			moveNumber++;
		}
		
		int realMoves = 0;
		for (int k=0; k<moveNumber; k++)
		{
			//if we are in check, only accept compatible moves...
			if (!moveBanned(x,y,possibleMovesX[k],possibleMovesY[k]) &&
			(checkType==NO_CHECK || canBreakCheck(possibleMovesX[k],possibleMovesY[k])))
			{
				realMoves++;
				if (createVector)
				{
					core.Move m = new core.Move();
					m.piece = PAWN;
					m.fromX = (byte)x; m.fromY = (byte)y;
					m.toX = (byte)possibleMovesX[k]; m.toY = (byte)possibleMovesY[k];
					if (m.toY == finalRank) m.promotionPiece = QUEEN; //any doubts?
					outVector.add(m);
				}

			}
		}
		
		return realMoves;
	}
	
	/** Computes the pseudolegal moves for a rook in a given square.
	 * @param x
	 * @param y
	 * @param outVector
	 * @param createVector
	 * @return
	 */
	public int pseudolegalRookMoves(int x, int y, Vector outVector, boolean createVector)
	{
		int moveNumber = 0;
		int nx,ny;
		int offsetX[] = {1,0,-1,0};
		int offsetY[] = {0,-1,0,1};

		
		for (int k=0; k<4; k++) //each direction
		{
			double legalProb = 1.0;
			for (nx=x+offsetX[k], ny=y+offsetY[k]; nx<8 && ny<8 && nx>=0 && ny>=0
				&& isUncertain(nx,ny); nx+=offsetX[k], ny+=offsetY[k])
			{
				if (!moveBanned(x,y,nx,ny) && (checkType==NO_CHECK || canBreakCheck(nx,ny)))
				{
					if (legalProb>0.0 && (offsetY[k]==0 || !isEnemyPawnBlocking(x,y,nx,ny))) 
					//vertical, check for certain enemy pawns blocking us.
					{
						moveNumber++;
						if (createVector)
						{
							core.Move m = new core.Move();
							m.piece = (byte)friendlyPieces[x][y]; //we check for the actual piece,
								//as the Queen borrows movement rules from Rook and Bishop.
							m.fromX = (byte)x; m.fromY = (byte)y;
							m.toX = (byte)nx; m.toY = (byte)ny;
							outVector.add(m);
						}
					}
				}
				legalProb *= (1-occupiedProbability(nx,ny));
			}
		}
		
		return moveNumber;
	}
	
	/**
	 * Computes the pseudolegal moves for a Bishop.
	 * @param x
	 * @param y
	 * @param outVector
	 * @param createVector
	 * @return
	 */
	public int pseudolegalBishopMoves(int x, int y, Vector outVector, boolean createVector)
	{
		int moveNumber = 0;
		int nx,ny;
		int offsetX[] = {1,1,-1,-1}; //the offsets are the only difference from the Rook
		int offsetY[] = {1,-1,1,-1};
		
		for (int k=0; k<4; k++) //each direction
		{
			for (nx=x+offsetX[k], ny=y+offsetY[k]; nx<8 && ny<8 && nx>=0 && ny>=0
				&& isUncertain(nx,ny); nx+=offsetX[k], ny+=offsetY[k])
			{
				if (!moveBanned(x,y,nx,ny) && (checkType==NO_CHECK || canBreakCheck(nx,ny)))
				{
					moveNumber++;
					if (createVector)
					{
						core.Move m = new core.Move();
						m.piece = (byte)friendlyPieces[x][y]; //we check for the actual piece,
							//as the Queen borrows movement rules from Rook and Bishop.
						m.fromX = (byte)x; m.fromY = (byte)y;
						m.toX = (byte)nx; m.toY = (byte)ny;
						outVector.add(m);
					}
				}
			}
		}
		
		return moveNumber;
	}
	
	/**
	 * Computes all the pseudolegal moves for a Queen. It could be made more efficient
	 * instead of calling Rook and Bishop...
	 * @param x
	 * @param y
	 * @param outVector
	 * @param createVector
	 * @return
	 */
	public int pseudolegalQueenMoves(int x, int y, Vector outVector, boolean createVector)
	{
		return (pseudolegalRookMoves(x,y,outVector,createVector)+
			pseudolegalBishopMoves(x,y,outVector,createVector));
	}
	
	/**
	 * Computes all the pseudolegal moves for a Knight.
	 * @param x
	 * @param y
	 * @param outVector
	 * @param createVector
	 * @return
	 */
	public int pseudolegalKnightMoves(int x, int y, Vector outVector, boolean createVector)
	{
		int moveNumber = 0;
		int offsetX[] = {-2,-2,-1,-1,1,1,2,2};
		int offsetY[] = {-1,1,-2,2,-2,2,-1,1};
		int nx,ny;
		
		for (int k=0; k<8; k++)
		{
			nx = x+offsetX[k]; ny = y+offsetY[k];
			if (nx>=0 && ny>=0 && nx<8 && ny<8 && isUncertain(nx,ny))
				if (!moveBanned(x,y,nx,ny) && (checkType==NO_CHECK || canBreakCheck(nx,ny)))
				{
					moveNumber++;
					if (createVector)
					{
						core.Move m = new core.Move();
						m.piece = KNIGHT;
						m.fromX = (byte)x; m.fromY = (byte)y;
						m.toX = (byte)nx; m.toY = (byte)ny;
						outVector.add(m);						
					}
				}
		}
		
		return moveNumber;
	}
	
	/**
	 * Computes all the pseudolegal moves for the current chessboard, and can either
	 * make a list or just return the number of moves (which may be included in the
	 * evaluation function for a given position).
	 * @param outVector
	 * @param createVector
	 * @return
	 */
	public int pseudolegalMoves(Vector outVector, boolean createVector)
	{
		int moveNumber = 0;
		
		if (moveList!=null)
		{
			//savedComputations++;
			//if (savedComputations%1000==0) System.out.println("Yahoo!");
			if (createVector) for (int k=0; k<moveList.size(); k++) outVector.add(moveList.get(k)); 
			return moveList.size();
		} 
		
		moveList = new Vector();
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
				switch (friendlyPieces[k][j])
				{
					case KING:
					moveNumber+=this.pseudolegalKingMoves(k,j,moveList,createVector);
					break;
					case PAWN:
					moveNumber+=this.pseudolegalPawnMoves(k,j,moveList,createVector);
					break;
					case ROOK:
					moveNumber+=this.pseudolegalRookMoves(k,j,moveList,createVector);
					break;
					case BISHOP:
					moveNumber+=this.pseudolegalBishopMoves(k,j,moveList,createVector);
					break;
					case QUEEN:
					moveNumber+=this.pseudolegalQueenMoves(k,j,moveList,createVector);
					break;
					case KNIGHT:
					moveNumber+=this.pseudolegalKnightMoves(k,j,moveList,createVector);
					break;
				}

		if (createVector) for (int k=0; k<moveList.size(); k++) outVector.add(moveList.get(k));
		return moveNumber;
	}
	
	/**
	 * Computes various things about the probability of a piece defending the King.
	 * @param x
	 * @param y
	 * @param squareVector if useVector is true, the possible locations of an attacking piece
	 * will be put in the Vector as Move objects (locations in the "from" fields)
	 * @param useVector
	 * @return the probability of that piece defending the King.
	 */
	public double isProtectingKing(int x, int y, Vector squareVector, boolean useVector)
	{
		double prob = 0.0;
		int offsetX=-1000; int  offsetY=-1000;
		
		if (friendlyPieces[x][y]==KING) return 0.0;
		
		//if not on the same rank/file/diagonal as the king, this is obviously 0.
		if (x==kingX)
		{
			offsetX = 0; offsetY = 1;
		}
		if (y==kingY)
		{
			offsetX = 1; offsetY = 0;
		}
		if ((kingX-kingY)==(x-y))
		{
			offsetX = 1; offsetY = 1;
		}
		if ((kingY+kingX)==x+y)
		{
			offsetX = 1; offsetY = -1;
		}
		if (offsetX==-1000) return 0.0; //out of here!
		
		if (kingX>x)
		{
			offsetX*=-1; offsetY*=-1;
		}
		if (kingX==x && kingY>y) offsetY*=-1;
		
		//now we are ready to travel between the king and the piece to see if any square is
		//occupied...
		int nx, ny;
		for (nx = kingX+offsetX, ny = kingY+offsetY; nx!=x || ny!=y; nx+=offsetX, ny+=offsetY)
		{
			if (occupiedProbability(nx,ny)>=1.0) return 0.0;
		}
		
		double legalMoveProb = 1.0;
		//now we see which squares could host the attacker, continuing the for cycle.
		//We'll have to refine this to take into account files with existing pawns and the
		//fact that straight movement to the last ranks is impossible while they exist.
		for (nx = x+offsetX, ny = y+offsetY; nx>=0 && ny>=0 && nx<8 && ny<8 &&
			legalMoveProb>0.001; nx+=offsetX, ny+=offsetY)
		{
			double baseProb = ((offsetX==0 || offsetY==0)? straightPieceProbability(nx,ny):
				obliquePieceProbability(nx,ny));
				
			//vertical protection and there's a pawn in between!
			if (offsetX==0 && isFriendlyPawnBlocking(nx,ny,x,y)) legalMoveProb = 0.0;
			
			if (prob==0.0) prob = baseProb;
			else
			{
				baseProb*=legalMoveProb; //let's make changes more unlikely as we get farther.
				prob = 1-(1-prob)*(1-baseProb); //and then compound probabilities as usual.
			}
			legalMoveProb*=occupiedProbability(nx,ny);
			if (useVector)
			{
				core.Move m = new core.Move();
				m.fromX = (byte)nx; m.fromY = (byte)ny;
				squareVector.add(m);
			}
		}
		return prob;
	}
	
	/**
	 * Returns true if the piece is protecting the king and will keep protecting it along
	 * the same direction after the move.
	 * @param x
	 * @param y
	 * @param nx
	 * @param y
	 * @return
	 */
	public boolean willStillBeProtectingKing(int x, int y, int nx, int ny)
	{
		if (isProtectingKing(x,y,null,false)==0.0) return false;
		
		//we use offsets to perform the checks...
		int xdiff = kingX - x;
		int ydiff = kingY - y;
		int xdiff2 = kingX - nx;
		int ydiff2 = kingY - ny;
		
		if (xdiff==0) return (xdiff2==0);
		if (ydiff==0) return (ydiff2==0);
		if ((xdiff+ydiff)==0) return (xdiff2+xdiff2==0);
		if ((xdiff-ydiff)==0) return (xdiff2-xdiff2==0);
		
		return false;
	}
	
	/**
	 * The probability system is good enough, but it is an approximation. While
	 * reasonable most of the time, there is one special important case where we want to
	 * use logical deduction in addition to probability. The final ranks are blocked by
	 * an enemy pawn, and if we just use compound probabilities we find out that there is
	 * a small chance of a move being possible when it is, in fact, impossible. Since this
	 * affects the initial moves when the enemy pieces are behind their pawns, we must
	 * get it right.
	 * @param x
	 * @param y
	 * @param destX
	 * @param destY
	 * @return
	 */
	public boolean isEnemyPawnBlocking(int x, int y, int destX, int destY)
	{
		//we cannot say anything, the pawn may or may not be there!
		if (minPawns[x]==0) return false; 
		if (x!=destX) return false; //what were you thinking? This only applies to vertical moves.
		
		//if there is a chance (even small) of the pawn being behind the piece,
		//then we cannot be sure it's blocking it.
		int offset = (isWhite? -1 : 1); //look behind!
		for (int k=y+offset; k<8 && k>=0; k+=offset)
			if (isUncertain(x,k) && probabilityInfo[x][k][PAWN]>0.0) return false;
			
		//same reasoning, if the pawn might be beyond the square we are trying to go to,
		//then we can't rule out the possibility.
		
		//for (int k=destY-offset; k<8 && k>=0; k-=offset)
		for (int k=destY; k<8 && k>=0; k-=offset)
		{
			if (isUncertain(x,k) && probabilityInfo[x][k][PAWN]>0.0) return false;
		}
			
			
		return true;
		
	}
	
	/**
	 * In order to evaluate the possibilities of enemy movement, we have to consider
	 * whether the opponent's pieces are blocked by their own pawns. Since freedom of
	 * movement is simmetrical in chess, this is a very simple shortcut.
	 * @param x
	 * @param y
	 * @param destX
	 * @param destY
	 * @return
	 */
	public boolean isFriendlyPawnBlocking(int x, int y, int destX, int destY)
	{
		return isEnemyPawnBlocking(destX,destY,x,y);
	}



	
	/**
	 * Returns true if moving to this square can help deal with a check.
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean canBreakCheck(int x, int y)
	{
		//no matter how good it is, no move can block 2 checks at the same time.
		int offset,offset2,k,j;
		
		if (checkType2!=NO_CHECK) return false; 
		switch (checkType)
		{
			
			case CHECK_FILE:
				if (kingX!=x) return false;
				offset = (kingY>y? -1 : 1);
				for (k = kingY+offset; k!=y; k+=offset)
					if (!isUncertain(x,k)) return false;
				break;
				
			case CHECK_RANK:
				if (kingY!=y) return false;
				offset = (kingX>x? -1 : 1);
				for (k = kingX+offset; k!=x; k+=offset)
					if (!isUncertain(k,y)) return false;
				break;		
				
			case CHECK_LONG_DIAGONAL:
				//find which diagonal is the long one... the algorithm is
				//actually very simple, since we can divide the chessboard into
				//four 4x4 squares. In the lower left and upper right quadrants,
				//the top right diagonal is always longer, and the opposite holds
				//in the other two.
				offset = (kingX>x? -1 : 1);
				if ((kingX<4 && kingY<4) || (kingX>=4 && kingY>=4))
				{
					//top right diagonal
					offset2 = offset;
					if ((kingX-x)!=(kingY-y)) return false; //not on diagonal!
				} else
				{
					//top left diagonal
					offset2 = -offset;
					if ((kingX+kingY)!=(x+y)) return false; //not on diagonal!
				}
				for (k = kingX+offset, j = kingY+offset2; k!=x; k+=offset, j+=offset2)
					if (!isUncertain(k,j)) return false;
				break;
				
			case CHECK_SHORT_DIAGONAL:
				//same story, inverted if cycles...
				offset = (kingX>x? -1 : 1);
				if ((kingX<4 && kingY<4) || (kingX>=4 && kingY>=4))
				{
					//top left diagonal
					offset2 = -offset;
					if ((kingX+kingY)!=(x+y)) return false; //not on diagonal!					
				} else
				{
					//top right diagonal
					offset2 = offset;
					if ((kingX-x)!=(kingY-y)) return false; //not on diagonal!					
				}
				for (k = kingX+offset, j = kingY+offset2; k!=x; k+=offset, j+=offset2)
					if (!isUncertain(k,j)) return false;
				break;	
				
			case CHECK_KNIGHT:
				//just test each square...
				int testX[]	= {-2,-2,-1,-1,1,1,2,2};
				int testY[]	= {-1,1,-2,2,-2,2,-1,1};
				
				for (k=0; k<8; k++)
				{
					offset = kingX+testX[k]; offset2 = kingY+testY[k];
					if (x==offset && y==offset2) return true;
				}
				return false; //any square other than those 8 will not help!
		}
		
		return true;
	}
	
	/**
	 * @param x
	 * @param y
	 * @return Whether the square is occupied by a friendly piece.
	 */
	public boolean isUncertain(int x, int y)
	{
		return friendlyPieces[x][y]==EMPTY;
	}
	
	/**
	 * @param pieceType
	 * @return The sum of probabilities for a given type on the whole chessboard.
	 */
	public double totalProbability(int pieceType)
	{
		double result = 0.0;
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				if (isUncertain(k,j)) result += probabilityInfo[k][j][pieceType];
			}
		return result;
	}
	
	public void computeTotalProbabilities()
	{
		for (int k=0; k<6; k++) 
		{
			globalPieceProbability[k] = totalProbability(k);
			if (globalPieceProbability[k]<0.0) 
				System.out.println("@@@");
		} 
	}
	
	/**
	 * After playing with the probabilities, updates the prob. of a square being empty.
	 * @param x
	 * @param y
	 */
	public void updateEmptyProbability(int x, int y)
	{
		double sum = 0.0;
		for (int k=0; k<6; k++) sum += probabilityInfo[x][y][k];
		probabilityInfo[x][y][EMPTY] = 1.0 - sum;
		if (probabilityInfo[x][y][EMPTY]<0.0)
		{ 
			/*if (probabilityInfo[x][y][EMPTY]<-0.01)
			{
				try
				{throw (new Exception("Probability < 0!!!!!!!"));
				} catch (Exception e) { e.printStackTrace(); }
			}*/
			for (int k=0; k<6; k++) probabilityInfo[x][y][k]+= (probabilityInfo[x][y][k]*probabilityInfo[x][y][EMPTY]);
			probabilityInfo[x][y][EMPTY] = 0.0;

		}
		if (probabilityInfo[x][y][EMPTY]==probabilityInfo[x][y][EMPTY]+1.0)
		{
			System.out.println("Oops... infinity?");
		}
	}
	
	public void updateAllEmptyProbabilities()
	{
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++) updateEmptyProbability(k,j);
	}
	
	/**
	 * Adjust all probabilities, knowing that a square is empty.
	 * @param x
	 * @param y
	 */
	public void squareIsEmpty(int x, int y)
	{
		double diff[] = new double[6];
		double surplus[] = new double[6];
		
		for (int k=0; k<6; k++)
		{
			surplus[k] = probabilityInfo[x][y][k];
			probabilityInfo[x][y][k] = 0.0;
		}
		probabilityInfo[x][y][EMPTY] = 1.0;
		
		this.distributePositiveProbability(surplus,x,y,true);
	}
	
	/**
	 * Updates probabilities knowing that a square contains a pawn.
	 * @param x
	 * @param y
	 */
	public void squareContainsPawn(int x, int y)
	{
		//here, we first distribute the other probabilities between the other squares, then
		//we raise the pawn brobability to 1.0, subtracting it from the other squares.
		double diff[] = new double[6];
		double surplus[] = new double[6];
		
		for (int k=0; k<6; k++)
		{ 
			diff[k] = this.globalPieceProbability[k] - this.probabilityInfo[x][y][k];
			if (diff[k]==0.0) diff[k] = 0.000001;
			surplus[k] = this.probabilityInfo[x][y][k];
		}
		
		//surplus for pawns is actually a negative surplus, in that we draw from other squares
		//instead of donating probability to them. Intuitively, if we know that a pawn is here,
		//it is less likely to encounter them elsewhere.
		surplus[PAWN] = -1.0 + this.probabilityInfo[x][y][PAWN];
				
		probabilityInfo[x][y][PAWN] = 1.0;
		probabilityInfo[x][y][KNIGHT] = 0.0;
		probabilityInfo[x][y][BISHOP] = 0.0;
		probabilityInfo[x][y][ROOK] = 0.0;
		probabilityInfo[x][y][QUEEN] = 0.0;
		probabilityInfo[x][y][KING] = 0.0;
		probabilityInfo[x][y][EMPTY] = 0.0;
		
		distributePositiveProbability(surplus,x,y,true);
		distributeNegativeProbability(surplus,x,y);
		
		
	}
	
	/**
	 * Updates all probabilities knowing there is a piece on a given square.
	 * @param x
	 * @param y
	 */
	public void squareContainsPiece(int x, int y)
	{
		//This is a little more complex. We zero the probability of there being pawns, kings
		//or emptiness, and then we update the rest of the chessboard accordingly.
		double diff[] = new double[6];
		double surplus[] = new double[6];
		double currentPieceProb = 1.0 - this.probabilityInfo[x][y][EMPTY] -
		this.probabilityInfo[x][y][PAWN] - this.probabilityInfo[x][y][KING]; 
		double pieceProbIncrease = 1.0 - currentPieceProb;
		
		for (int k=0; k<5; k++)
		{ 
			diff[k] = this.globalPieceProbability[k] - this.probabilityInfo[x][y][k];
			if (diff[k]==0.0) diff[k] = 0.000001;
			//negative surplus...
			surplus[k] = -pieceProbIncrease*this.probabilityInfo[x][y][k]/currentPieceProb;
		}
		surplus[PAWN] = this.probabilityInfo[x][y][PAWN];
		surplus[KING] = this.probabilityInfo[x][y][KING];
	
	/*for (int k=0; k<8; k++)
		for (int j=0; j<8; j++)
			if (isUncertain(k,j) && (x!=k || y!=j))
			{
				 for (int i=0; i<6; i++)
				{
					//increases probability proportionally to presence.
					probabilityInfo[k][j][i] += (surplus[i]*probabilityInfo[k][j][i]/diff[i]);
				}
				this.updateEmptyProbability(k,j);
			}*/
				
	probabilityInfo[x][y][PAWN] = 0.0;
	probabilityInfo[x][y][EMPTY] = 0.0;
	probabilityInfo[x][y][KING] = 0.0;
	for (int k=1; k<5; k++) probabilityInfo[x][y][k]-=surplus[k];
	
	distributePositiveProbability(surplus,x,y,true);
	distributeNegativeProbability(surplus,x,y);
	
	}
	
	public void squareWasCaptured(int x, int y)
	{
		//like above, but no special pawn case.
		double diff[] = new double[6];
		double surplus[] = new double[6];
		double currentPieceProb = 1.0 - this.probabilityInfo[x][y][EMPTY]; 
		double pieceProbIncrease = 1.0 - currentPieceProb;

		
		squareContainsSomething(x,y);
		
		//the surplus is first filled with probability from the squares from which the attack
		//is supposed to have been originated.
		double matrix[][] = new double[8][8];
		int squareX[] = new int[40];
		int squareY[] = new int[40];
		double squareValue[] = new double[40];
		double total = 0.0;
		double totalProb = 0.0;
		int moveNumber=0;
		int nx, ny;
		
		for (int piece=0; piece<6; piece++)
		{
			//moveNumber=0;
			//fill KNIGHT surplus
			if (piece==KNIGHT) for (int k=0; k<8; k++)
			{
				nx = x + knightOffsetX[k]; ny = y + knightOffsetY[k];
				if (nx>=0 && ny>=0 && nx<8 && ny<8 && probabilityInfo[nx][ny][KNIGHT]>0)
				{
					double add = probabilityInfo[nx][ny][KNIGHT]*probabilityInfo[x][y][KNIGHT];
					matrix[nx][ny] += add;
					total+=add;
				}
			}
			
			if (piece==PAWN)
			{
				//Add EN PASSANT later on...
				int rank = (isWhite? y+1 : y-1);
				if (x>0 && rank>=0 && rank<8 && probabilityInfo[x-1][rank][PAWN]>0)
				{
					double add = probabilityInfo[x-1][rank][PAWN]*probabilityInfo[x][y][PAWN];
					matrix[x-1][rank] += add;
					total+=add;				
				}
				if (x<7 && rank>=0 && rank<8 && probabilityInfo[x+1][rank][PAWN]>0)
				{
					double add = probabilityInfo[x+1][rank][PAWN]*probabilityInfo[x][y][PAWN];
					matrix[x+1][rank] += add;
					total+=add;						
				}				
			}
			
			if (piece==KING) for (int k=0; k<8; k++)
			{
				nx = x + queenOffsetX[k]; ny = y + queenOffsetY[k];
				if (nx>=0 && ny>=0 && nx<8 && ny<8 && probabilityInfo[nx][ny][KING]>0)
				{
					double add = probabilityInfo[nx][ny][KING]*probabilityInfo[x][y][KING];
					matrix[nx][ny] += add;
					total+=add;	
				}
			}
			
			if (piece==ROOK || piece==BISHOP || piece==QUEEN)
			{
				int offX[]={}; int offY[]={};
				if (piece==ROOK)
				{
					offX = rookOffsetX; offY = rookOffsetY;
				}
				if (piece==BISHOP)
				{
					offX = bishopOffsetX; offY = bishopOffsetY;
				}
				if (piece==QUEEN)
				{
					offX = queenOffsetX; offY = queenOffsetY;
				}
				for (int k=0; k<offX.length; k++)
				{
					double legalProb = probabilityInfo[x][y][piece];
					for (nx = x+offX[k], ny = y+offY[k];
						 nx>=0 && ny>=0 && nx<8 && ny<8
						&& isUncertain(nx,ny) && legalProb>0.0; nx+=offX[k], ny+=offY[k])
					{
						legalProb*=probabilityInfo[nx][ny][piece];
						if (probabilityInfo[nx][ny][piece]>0.0)
						{
							matrix[nx][ny] += legalProb;
							total+=legalProb;						
						}
					}
				}
			}
		}
		//now we kick the probabilities on the whole chessboard.
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				double ratio = matrix[k][j]/total;
				for (int var=0; var<6; var++)
				{
					double loss = ratio*probabilityInfo[k][j][var];
					surplus[var]+=loss;
					probabilityInfo[k][j][var]-=loss;
				}
			}
		
		updateAllEmptyProbabilities();
			
		/*for (int var=0; var<6; var++)
		{
			//this.addProbabilityAllOverChessboard(surplus[var],var,-1,-1);
			//if (surplus[var]>0.0) this.addProbabilityAllOverChessboard(surplus[var],var,x,y);
			
		}	*/
		distributePositiveProbability(surplus,x,y,true);
	}
	
	/**
	 * Adjusts probabilities knowing that there is something on a certain square, but not
	 * because of a capture (that is, because of an illegal move).
	 * @param x
	 * @param y
	 */
	public void squareContainsSomething(int x, int y)
	{
		//like above, but no special pawn case.
		double surplus[] = new double[6];
		
		updateEmptyProbability(x,y);
		double currentPieceProb = 1.0 - this.probabilityInfo[x][y][EMPTY]; 
		double pieceProbIncrease = this.probabilityInfo[x][y][EMPTY];
		
		for (int k=0; k<6; k++)
		{ 
			//negative surplus...
			surplus[k] = -pieceProbIncrease/currentPieceProb*this.probabilityInfo[x][y][k];
		}
		
		probabilityInfo[x][y][EMPTY] = 0.0;
		for (int k=0; k<6; k++) probabilityInfo[x][y][k]-=surplus[k];
		
		updateEmptyProbability(x,y);
		
		distributeNegativeProbability(surplus,x,y);
	}
	
	
	/**
	 * Called when we are certain that a certain piece is NOT on a certain square.
	 * @param x
	 * @param y
	 * @param type
	 */
	public void excludePieceFromSquare(int x, int y, int type)
	{
		//redistributes all probabilities, including EMPTY.
		double surplus[] = new double[6];
		
		for (int k=0; k<6; k++) surplus[k] = 0;
		surplus[type] = probabilityInfo[x][y][type];
		probabilityInfo[x][y][type] = 0.0;
		updateEmptyProbability(x,y);
		distributePositiveProbability(surplus,x,y,true);
	}
	
	/**
	 * Distributes a probability among a certain set of squares, decreasing the probabilities
	 * for the other pieces, which gets transferred to the squares outside the set.
	 * @param x
	 * @param y
	 * @param qty
	 * @param piece
	 */
	public void addProbabilityToSquareSet(int x[], int y[], int size, double intensity[], double total, double qty, int piece, int exceptX, int exceptY)
	{
		double surplus[] = new double[6];
		int matrix[][] = new int[8][8];
		
		for (int k=0; k<size; k++)
		{
			matrix[x[k]][y[k]]=1;
			double expectation = qty*intensity[k]/total;
			double newSquareTotalForPiece = probabilityInfo[x[k]][y[k]][piece] + expectation;
			if (newSquareTotalForPiece>1.0) newSquareTotalForPiece=1.0;
			double otherPieceRatio = (1.0 - newSquareTotalForPiece)/(1.0 - probabilityInfo[x[k]][y[k]][piece])
				;
			if (qty<0.0) otherPieceRatio = 1.0; //don't replace with other pieces.
			for (int otherPiece=0; otherPiece<6; otherPiece++)
				if (otherPiece!=piece)
				{
					double newVal = probabilityInfo[x[k]][y[k]][otherPiece]*otherPieceRatio;
					surplus[otherPiece]+= (probabilityInfo[x[k]][y[k]][otherPiece]-newVal);
					probabilityInfo[x[k]][y[k]][otherPiece]=newVal;
				}
			probabilityInfo[x[k]][y[k]][piece]=newSquareTotalForPiece;
		}
		surplus[piece]=-qty;
		
		//if (qty<0.0) return;
		
		//now we have to redistribute the surplus onto the squares outside the set.
		double totals[] = new double[6];
		/*for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
				if (matrix[k][j]==0) for (int ite=0; ite<6; ite++) 
					totals[ite]+=(probabilityInfo[k][j][ite]*(1-occupiedProbability(k,j))); //VOODOO!
					
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
				if (matrix[k][j]==0) for (int ite=0; ite<6; ite++)	
					probabilityInfo[k][j][ite]+=(surplus[ite]*(1-occupiedProbability(k,j))*probabilityInfo[k][j][ite]/totals[ite]);
		*/
		for (int k=0; k<6; k++) addProbabilityAllOverChessboard(surplus[k],k, exceptX,exceptY);
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++) updateEmptyProbability(k,j);
	}
	
	/**
	 * Distributes a probability absolutely, that is, forcefully evicting any existing
	 * probability if necessary. This method becomes necessary when important changes
	 * occur, for example when we are notified about a check. The evicted probability
	 * is distributed normally over the remaining squares. The method assumes the search matrix
	 * has been previously initialized by the caller.
	 * @param qty
	 * @param piece
	 */
	public void absoluteDistributePositiveProbability(double qty, int piece)
	{
		double surplus[] = new double[6];
		double total=0.0;
		double recursion=0.0;
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				if (searchMatrix[k][j] && probabilityInfo[k][j][EMPTY]>0.0)
					total+=probabilityInfo[k][j][piece];
			}
			
		if (total==0.0)
		{
			// System.out.println("Bleh, bailing out!");
			return;
		}
			
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				double add = qty * probabilityInfo[k][j][piece] / total;
				if (probabilityInfo[k][j][EMPTY]>=add)
				{
					probabilityInfo[k][j][piece]+=add;
					updateEmptyProbability(k,j);
				} else
				{
					double allowed = 1.0 - probabilityInfo[k][j][piece] - add;
					if (allowed>0.0)
					{
						double ratio = allowed / (1.0 - probabilityInfo[k][j][EMPTY] - probabilityInfo[k][j][piece]);
						for (int i=0; i<6; i++)
						{
							if (i!=piece)
							{
								double newVal = probabilityInfo[k][j][i]*ratio;
								surplus[i]+=(probabilityInfo[k][j][i]-newVal);
								probabilityInfo[k][j][i] = newVal;
							}
						}
					}
					probabilityInfo[k][j][piece]+=add;
					if (probabilityInfo[k][j][piece]>1.0)
					{
						//add that recursively!
						recursion+=(probabilityInfo[k][j][piece]-1.0);
						probabilityInfo[k][j][piece] = 1.0;
					}
					updateEmptyProbability(k,j);
				}
			}
			
		if (recursion>0.0)
		{
			absoluteDistributePositiveProbability(recursion,piece);
		}
		
		distributePositiveProbability(surplus,-1,-1,false);
		
	}
	
	/**
	 * Distributes a certain quantity of probability all over the chessboard.
	 * @param qty
	 * @param exceptX
	 * @param exceptY
	 */
	public void distributePositiveProbability(double qty[], int exceptX, int exceptY, boolean init)
	{
		//the search matrix indicates which squares we can still pour probability on.
		if (init)
		{
			for (int k=0; k<8; k++) for (int j=0; j<8; j++) searchMatrix[k][j]=true;
			if (exceptX>=0) searchMatrix[exceptX][exceptY] = false;
		}
		double surplus[] = new double[6];
		double nextSurplus[] = new double[6];
		double totals[] = new double[6];
		boolean types[] = new boolean[6];
		double freeSpace = 0.0;
		int allowedSquares = 0;
		
		for (int k=0; k<6; k++) { surplus[k]=(qty[k]>=0.0? qty[k]: 0.0); types[k]=(qty[k]!=0.0); }
		
		boolean cont = true;
		int piecesToGo = 0;
		
		while (cont)
		{
			//at each iteration cycle, we compute the totals for each piece on the allowed squares.
			allowedSquares = 0;
			piecesToGo = 0;
			for (int k=0; k<6; k++) { types[k]=(surplus[k]!=0.0); nextSurplus[k]=0.0;}
			for (int k=0; k<6; k++) if (types[k]) piecesToGo++;
			
			if (piecesToGo==0) return; //finished!
			
			for (int k=0; k<8; k++)
				for (int j=0; j<8; j++)
				{
					if (searchMatrix[k][j])
					{
						/*boolean canItHelpUs = false;
						for (int i=0; i<6; i++) if (types[i] && probabilityInfo[k][j][i]>0.0) canItHelpUs = true;
						if (!canItHelpUs) searchMatrix[k][j] = false;
						else*/
						//{
						if (probabilityInfo[k][j][EMPTY]>0.0)
						{
							allowedSquares++;
							freeSpace+=probabilityInfo[k][j][EMPTY];
							for (int i=0; i<6; i++) totals[i]+=probabilityInfo[k][j][i];
						} else searchMatrix[k][j]=false;
					}
				}
				
			//now we perform a step of dumping on the squares that can bear it.
			if (allowedSquares==0)
			{
				this.computeTotalProbabilities();
				System.out.println("Distribution failed: "+surplus);
				return; //gasp!
			}
			
			for (int k=0; k<8; k++)
			{
				for (int j=0; j<8; j++)
				{
					if (searchMatrix[k][j])
					{
						/*double rations[] = new double[6]; double thisSquareTotal=0.0;
						for (int i=0; i<6; i++)
						{
							rations[i] = surplus[i]*probabilityInfo[k][j][i]/totals[i];
							thisSquareTotal+=rations[i];
						}*/
						double empty = probabilityInfo[k][j][EMPTY];
						/*if (thisSquareTotal<=empty)
						{
							//the square can bear the previous prob. info as well as the new load
							for (int i=0; i<6; i++)
							{
								probabilityInfo[k][j][i]+=rations[i];
							}							
							updateEmptyProbability(k,j);
						} else
						{
							//eviction for the old data we can't find room for.
							double evictedQty = (thisSquareTotal-empty);
							double evictedRatio = evictedQty / (1.0-empty);
							for
							
						}*/
						//dump!
						
						for (int i=0; i<6; i++)
						{
							if (types[i])
							{
								//don't care if you get over 100%, we'll deal with that later!
								probabilityInfo[k][j][i]+=(surplus[i]*probabilityInfo[k][j][i]/totals[i]);
							}
						}
						//if you overloaded it, just add it to the surplus for the next step.
						probabilityInfo[k][j][EMPTY] = 1.0;
						for (int flag=0; flag<6; flag++) probabilityInfo[k][j][EMPTY]-=probabilityInfo[k][j][flag];
						//this.updateEmptyProbability(k,j);
						if (probabilityInfo[k][j][EMPTY]<=0.0 && probabilityInfo[k][j][EMPTY]<empty)
						{
							double newEmpty = (empty<0.0001? empty : 0.0001);
							double fullProb = (1.0 - probabilityInfo[k][j][EMPTY]);
							searchMatrix[k][j] = false; //don't dump on this square again.
							for (int i=0; i<6; i++)
							{
								double over = probabilityInfo[k][j][i] - probabilityInfo[k][j][i]*(1.0-newEmpty)/fullProb;
								probabilityInfo[k][j][i] -= over;
								nextSurplus[i] += over;
							}
							this.updateEmptyProbability(k,j);
						}
					}
				}
			}
			for (int k=0; k<6; k++) surplus[k] = nextSurplus[k];
		}
	}
	
	/**
	 * Reduces the probability of a certain piece on the chessboard.
	 * @param qty
	 * @param exceptX
	 * @param exceptY
	 */
	public void distributeNegativeProbability(double qty[],int exceptX, int exceptY)
	{
		for (int i=0; i<6; i++)
		{
			if (qty[i]<0.0)
			{
				double demand = qty[i];
				double total = 0.0;
				for (int k=0; k<8; k++)
					for (int j=0; j<8; j++)
					{
						if ((k!=exceptX || j!=exceptY) && probabilityInfo[k][j][EMPTY]>0.0) total += probabilityInfo[k][j][i];
					}
					
				if (total+demand<0.0)
				{
					//not enough probability on the rest of the board. Reduce your demands.
					demand = (-total)*0.99; //leave a small probability of a mistake.
				}
				for (int k=0; k<8; k++)
					for (int j=0; j<8; j++)
					{
						if ((k!=exceptX || j!=exceptY) && probabilityInfo[k][j][EMPTY]>0.0) probabilityInfo[k][j][i]+=(demand*probabilityInfo[k][j][i]/total);
					}
				
			}
		}
		updateAllEmptyProbabilities();
		this.computeTotalProbabilities();
	}
	
	
	
	public void addProbabilityAllOverChessboard(double qty, int piece, int exceptX, int exceptY)
	{
		int cont = 10;
		double surplus = qty;
		double initialSurplus;
		
		double globProb = globalPieceProbability[piece];
		if (exceptX>=0) globProb-=probabilityInfo[exceptX][exceptY][piece];
		
		if (globProb+qty<=0.0) qty=globProb - 0.0000001;
		
		while (cont-->=0)
		{
			initialSurplus = surplus;
			for (int k=0; k<8; k++)
				for (int j=0; j<8; j++)
				{
					if (isUncertain(k,j) && (k!=exceptX || j!=exceptY))
					{
						//double idealQty = (initialSurplus*probabilityInfo[k][j][piece]/globalPieceProbability[piece]);
						double idealQty = (initialSurplus*probabilityInfo[k][j][piece]/globProb);
						if (idealQty==idealQty+1)
						{
							System.out.println("!!!");
						}
						if (qty>0 && probabilityInfo[k][j][EMPTY]>=idealQty)
						{
							probabilityInfo[k][j][piece]+=idealQty;
							surplus-=idealQty;
							updateEmptyProbability(k,j);
						}
						if (qty<0 && probabilityInfo[k][j][piece]>=-idealQty)
						{
							probabilityInfo[k][j][piece]+=idealQty;
							surplus-=idealQty;
						}
					}
				}
				
			updateAllEmptyProbabilities();
			
			if (surplus<0.000001 && surplus>-0.000001) return;
			if (surplus==initialSurplus)
			{
				//AAAAAAAAHHHHHHHHH!
				this.computeTotalProbabilities();
				System.out.println("ERROR, COULD NOT DISTRIBUTE "+surplus);
				return;
			}
			
		}
	}
	
	/**
	 * Changes the probabilities to reflect a capture;
	 * @param x
	 * @param y
	 */
	public void executeFriendlyCapture(int x, int y)
	{
		for (int k=0; k<6; k++) probabilityInfo[x][y][k]=0.0;
		probabilityInfo[x][y][EMPTY]=1.0;
		
		computeTotalProbabilities();
	}
	
	/**
	 * Computes very useful information from an illegal move.
	 * @param m
	 */
	public void updateAfterIllegalMove(core.Move m)
	{
		if (m.piece==PAWN)
		{
			//straight one-square movement, when impossible, means the square is blocked
			//or the pawn is protecting the King.
			int offset = (isWhite? 1 : -1);
			if (m.toY==m.fromY+offset)
			{
				//forward movement
				if (m.toX==m.fromX)
				{
					double protProb = this.isProtectingKing(m.fromX,m.fromY,null,false);
					if (protProb==0.0 || willStillBeProtectingKing(m.fromX,m.fromY,m.toX,m.toY))
					{
						squareContainsSomething(m.toX,m.toY);
					}
				}
			}
			if (m.toX!=m.fromX)
			{
				//failed pawn try
				double protProb = this.isProtectingKing(m.fromX,m.fromY,null,false);
				if (protProb==0.0 || willStillBeProtectingKing(m.fromX,m.fromY,m.toX,m.toY))
				{
					squareIsEmpty(m.toX,m.toY);
				}
			}
			
		}
	}
	
	/**
	 * This function updates the state of the chessboard following a successful player's move.
	 * @param m
	 * @param capture
	 * @param pawnTries the opponent's pawn tries
	 * @param check opponent checked
	 * @param check2
	 */
	public void evolveAfterLegalMove(core.Move m, int capture, int pawnTries, int check, int check2)
	{
		if (capture==NO_CAPTURE)
		{
			squareIsEmpty(m.toX,m.toY);
			capturedPawn = false;
			capturedPiece = false;
		}
		if (capture==CAPTURE_PAWN)
		{
			squareContainsPawn(m.toX,m.toY);
			executeFriendlyCapture(m.toX,m.toY);
			capturedPawn = true;
			capturedPiece = false;
			minPawns[m.toX]--;
			maxPawns[m.toX]--;
		}
		if (capture==CAPTURE_PIECE)
		{
			//we can infer something stronger if the capture happened after a check!
			if (checkType!=NO_CHECK)
			{
				//it was a single check, or a capture wouldn't have saved us from it...
				if (checkType==CHECK_RANK || checkType==CHECK_FILE)
				{
					excludePieceFromSquare(m.toX,m.toY,BISHOP);
					excludePieceFromSquare(m.toX,m.toY,KNIGHT);
				}
				if (checkType==CHECK_LONG_DIAGONAL || checkType==CHECK_SHORT_DIAGONAL)
				{
					excludePieceFromSquare(m.toX,m.toY,ROOK);
					excludePieceFromSquare(m.toX,m.toY,KNIGHT);
				}
				if (checkType==CHECK_KNIGHT)
				{
					excludePieceFromSquare(m.toX,m.toY,BISHOP);
					excludePieceFromSquare(m.toX,m.toY,ROOK);
					excludePieceFromSquare(m.toX,m.toY,QUEEN);
				}
			}
			squareContainsPiece(m.toX,m.toY);
			executeFriendlyCapture(m.toX,m.toY);
			capturedPawn = false;
			capturedPiece = true;
		}
		
		//delete banned move data.
		//NOT ANYMORE, moves can be blacklisted for more than one move...
		//Instead, we decrement the blacklist counter...
		//illegalMoves = null;
		if (illegalMoveBlackListDuration!=null)
		{
			for (int mv=0; mv<illegalMoveBlackListDuration.size(); mv++)
			{
				illegalMoveBlackListDuration.set
					(mv,new Integer(((Integer)illegalMoveBlackListDuration.get(mv)).intValue()-1));
			}
			for (int mv=0; mv<illegalMoveBlackListDuration.size(); mv++)
			{
				int val = ((Integer)illegalMoveBlackListDuration.get(mv)).intValue();
				if (val<=0)
				{
					//remove the move from the ban list...
					illegalMoves.remove(mv);
					illegalMoveBlackListDuration.remove(mv);
					mv--;
				}
			}
		}
		
		friendlyPieces[m.fromX][m.fromY] = EMPTY;
		friendlyPieces[m.toX][m.toY] = m.piece;
		if (m.piece==PAWN && m.promotionPiece!=EMPTY) friendlyPieces[m.toX][m.toY] = m.promotionPiece;
		int firstRank = (isWhite? 0 : 7);
		if (m.piece==ROOK && m.fromX==0 && m.fromY==firstRank) leftRookMoved=true;
		if (m.piece==ROOK && m.fromX==7 && m.fromY==firstRank) rightRookMoved=true;
		
		if (m.piece==KING)
		{
			//update king position
			kingX = m.toX; kingY = m.toY;
			kingMoved = true;
			if (m.toX==m.fromX+2)
			{
				//short castling!
				hasCastled = true;
				friendlyPieces[7][m.toY] = EMPTY;
				squareIsEmpty(5,m.toY);
				friendlyPieces[5][m.toY] = ROOK;
			}
			if (m.toX==m.fromX-2)
			{
				//long castling!
				hasCastled = true;
				friendlyPieces[0][m.toY] = EMPTY;
				squareIsEmpty(3,m.toY);
				friendlyPieces[3][m.toY] = ROOK;
				squareIsEmpty(1,m.toY);
			}
		}
		
		//for sliding pieces, clear all the probabilities on your path.
		if (m.piece!=KNIGHT && m.piece!=KING)
		{
			int offsetX, offsetY;
			if (m.toX>m.fromX) offsetX = 1;
				else { if (m.toX==m.fromX) offsetX = 0; else offsetX = -1; } 
			if (m.toY>m.fromY) offsetY = 1;
				else {if (m.toY==m.fromY) offsetY = 0; else offsetY = -1;} 
				
			int nx, ny;
			double surplus[] = new double[6];
			for (nx = m.fromX+offsetX, ny = m.fromY+offsetY; nx!=m.toX || ny!=m.toY;
				nx+=offsetX, ny+=offsetY)
			{
				for (int k=0; k<6; k++)
				{
					surplus[k]+=probabilityInfo[nx][ny][k];
					probabilityInfo[nx][ny][k] = 0.0;
				}
				probabilityInfo[nx][ny][EMPTY] = 1.0;
			}
			/*for (int k=0; k<6; k++)
			{ 
				this.addProbabilityAllOverChessboard(surplus[k],k,-1,-1);
			}*/
			distributePositiveProbability(surplus,-1,-1,true);
			
		}
		
		
		
		opponentCheckType=check;
		opponentCheckType2=check2;
		this.opponentPawnTries=pawnTries;
		
		//since we cannot be in check after our own move, we can draw our own conclusions.
		updateKingSafety();
		
		
		//ADD CHECK CONTROLS!
		computeControlMatrix();
		if (check==NO_CHECK) adjustEnemyKingProbabilityNoCheck();
		else adjustEnemyKingProbabilityCheck(m,capture,check,check2);
	}
	
	/**
	 * Calculates information from the absence of checks on the king. Even when under check,
	 * it still removes check threats of types that do not apply.
	 *
	 */
	public void updateKingSafety()
	{
		double surplus[] = new double[6];
		for (int k=0; k<6; k++) surplus[k] = 0.0;
		
		for (int k=0; k<8; k++)
		{
			int offX = queenOffsetX[k];
			int offY = queenOffsetY[k];
			int nx, ny, ite;
			double prob = 1.0;
			boolean subtract[] = new boolean[6];
			subtract[PAWN] = subtract[KING] = true;
			subtract[KNIGHT] = false;
			if (offX==0 || offY==0)
			{
				subtract[ROOK] = subtract[QUEEN] = true; subtract[BISHOP] = false;
			} else
			{
				subtract[QUEEN] = true; subtract[BISHOP] = true; subtract[ROOK] = false;
			}
			
			if (checkType==CHECK_RANK || checkType2==CHECK_RANK)
			{
				if (offX!=0 && offY==0) for (int index=0; index<6; index++) subtract[index]=false;
			}
			
			if (checkType==CHECK_FILE || checkType2==CHECK_FILE)
			{
				if (offX==0 && offY!=0) for (int index=0; index<6; index++) subtract[index]=false;
			}
			
			if (checkType==CHECK_LONG_DIAGONAL || checkType2==CHECK_LONG_DIAGONAL)
			{
				boolean invert = (kingX>3 && kingY<4) || (kingX<4 && kingY>3);
				if (offX*offY==(invert? -1 : 1))
					for (int index=0; index<6; index++) subtract[index]=false;
			}
			if (checkType==CHECK_SHORT_DIAGONAL || checkType2==CHECK_SHORT_DIAGONAL)
			{
				boolean invert = (kingX>3 && kingY<4) || (kingX<4 && kingY>3);
				if (offX*offY==(invert? 1 : -1))
					for (int index=0; index<6; index++) subtract[index]=false;
			}
			
			for (nx=kingX+offX, ny=kingY+offY, ite=0; nx>=0 && ny>=0 && nx<8 && ny<8 &&
				occupiedProbability(nx,ny)<1.0; nx+=offX, ny+=offY, ite++)
			{
				//remove the applicable piece, based on the probability of the path being free.
				if (ite>0) subtract[KING] = false; //king is only ruled out in the neigh. squares
				if (ite>0 || offX==0 || offY!=(isWhite? 1 : -1)) subtract[PAWN] = false;
				for (int piece=0; piece<6; piece++)
				{
					if (subtract[piece])
					{
						double loss = probabilityInfo[nx][ny][piece] * prob;
						probabilityInfo[nx][ny][piece] -= loss;
						surplus[piece] += loss;
					}
				}
				updateEmptyProbability(nx,ny);
				prob *= (1.0 - occupiedProbability(nx,ny));
				if (offX==0 && isEnemyPawnBlocking(kingX,kingY,nx,ny)) prob = 0.0;
			}
		}
		
		//deal with knight separately!
		if (checkType!=CHECK_KNIGHT && checkType2!=CHECK_KNIGHT)
			for (int k=0; k<8; k++)
			{
				int nx = kingX + knightOffsetX[k];
				int ny = kingY + knightOffsetY[k];
				if (nx>=0 && ny>=0 && nx<8 && ny<8 && friendlyPieces[nx][ny]==EMPTY)
				{
					surplus[KNIGHT] += probabilityInfo[nx][ny][KNIGHT];
					probabilityInfo[nx][ny][KNIGHT] = 0.0;
					updateEmptyProbability(nx,ny);
				}
			}
		
		distributePositiveProbability(surplus,-1,-1,true);
	}
	
	
	
	
	/**
	 * Prints a String representation of this chessboard.
	 */
	public String toString()
	{
		String result = "";
		DecimalFormat myFormatter = new DecimalFormat("###.###%");
		
		for (int k=7; k>=0; k--)
		{
			for (int j=0; j<8; j++)
			{
				switch (friendlyPieces[j][k])
				{
					case PAWN: result += "P"; break;
					case KNIGHT: result += "N"; break;
					case BISHOP: result += "B"; break;
					case ROOK: result += "R"; break;
					case QUEEN: result += "Q"; break;
					case KING: result += "K"; break;
					case EMPTY: if (occupiedProbability(j,k)>=0.5) result+="@";
						else if ((j+k) % 2 == 0) result+="*"; else result+=" "; break;
				}
			}
			result += "\n";
		}
		
		result += "\n\n";
		result += "Entropy: " + entropy() + "\n";
		result += "Total Probabilities: ";
		for (int k=0; k<6; k++) result=result+globalPieceProbability[k]+" ";
		result+="\n";
		Vector v = new Vector();
		int n = this.pseudolegalMoves(v,true);
		result += "Pseudolegal moves ("+n+"):\n";
		for (int k=0; k<n; k++)
		{
			core.Move m = ((core.Move)v.get(k));
			double d = moveProbability(m);
			result+= m.toString() + " ("+ myFormatter.format(d) + ")\n";
		}	
		return result;
	}
	
	public void displayAllProbabilities()
	{
		DecimalFormat myFormatter = new DecimalFormat("000.000%");
		
		for (int k=7; k>=0; k--)
		{
			for (int piece=0; piece<7; piece++)
			{
				for (int j=0; j<8; j++)
				{
					System.out.print(myFormatter.format(this.probabilityInfo[j][k][piece]) + "  ");
				}
				System.out.println("");
			}
			System.out.println("");
		}
	}
	
	//The following methods estimate the probability of a move being successful.
	
	/**
	 * Provides an estimate of the probability of a move being accepted by the umpire.
	 */
	public double moveProbability(core.Move m)
	{
		double result = 1.0;
		
		switch (m.piece)
		{
			case PAWN: result = pawnMoveProbability(m); break;
			case KNIGHT: result = knightMoveProbability(m); break;
			case ROOK: result = rookMoveProbability(m); break;
			case BISHOP: result = bishopMoveProbability(m); break;
			case QUEEN: result = queenMoveProbability(m); break;
		}
		
		if (m.piece!=KING && m.piece!=KNIGHT)
		{
			//unfortunately, it is not over. We must check if the piece might be protecting the King,
			//and in that case, if it is abandoning that protection with the current move.
			double protProb = isProtectingKing(m.fromX,m.fromY,null,false);
			if (protProb==0.0) return result;
			if (!willStillBeProtectingKing(m.fromX,m.fromY,m.toX,m.toY))
				result*=(1.0-protProb);
		}
		return result;
	}
	
	/**
	 * Same as above, but does not consider pieces protecting the king. Useful when trying
	 * to figure out where the opponent's king might be.
	 * @param m
	 * @return
	 */
	public double moveProbabilityWithoutProtection(core.Move m)
	{
		double result = 1.0;
		
		switch (m.piece)
		{
			case PAWN: result = pawnMoveProbability(m); break;
			case KNIGHT: result = knightMoveProbability(m); break;
			case ROOK: result = rookMoveProbability(m); break;
			case BISHOP: result = bishopMoveProbability(m); break;
			case QUEEN: result = queenMoveProbability(m); break;
		}
		
		return result;
	}
	
	
	/**
	 * Returns the probability of a knight move being successful. The simplest of
	 * this kind of functions, because of the knight's unique movement type
	 * (it can always move except when it's protecting the King.
	 */
	public double knightMoveProbability(core.Move m)
	{	
		double pr = this.isProtectingKing(m.fromX,m.fromY,null,false);
		return (1.0-pr);
	}
	
	public double pawnMoveProbability(core.Move m)
	{
		double result = 0.0;
		
		if (m.fromX==m.toX)
		{
			int offset = (isWhite? 1 : -1);
			//forward movement, check for enemy pawns on the same file...
			//we add 1 (or -1) because, unlike the other pieces, a pawn cannot
			//capture with a forward movement, so we have to test one more square.
			if (isEnemyPawnBlocking(m.fromX,m.fromY,m.toX,m.toY+offset)) return 0.0;
			result = probabilityInfo[m.fromX][m.fromY+offset][EMPTY];
			if (m.toY==m.fromY+2*offset) result*=probabilityInfo[m.fromX][m.fromY+offset*2][EMPTY];
		} else
		{
			//it's a capture. Probability is merely the probability of the square not being empty.
			result = occupiedProbability(m.toX,m.toY);
		}
		

		
		return result;
	}
	
	/**
	 * Estimates the probability of a rook move being successful.
	 * @param m
	 * @return
	 */
	public double rookMoveProbability(core.Move m)
	{
		double result = 1.0;
		int offsetX, offsetY, ite;
		
		if (m.fromX==m.toX)
		{
			offsetX = 0; offsetY = (m.toY>m.fromY? 1 : -1);
			ite = m.toY - m.fromY; if (ite<0) ite*=-1; ite--;
		} else 
		{
			offsetY = 0; offsetX = (m.toX>m.fromX? 1 : -1);
			ite = m.toX - m.fromX; if (ite<0) ite*=-1; ite--;
		}
		
		int nx, ny, k;
		for (nx = m.fromX+offsetX, ny = m.fromY+offsetY, k = 0; k<ite; k++, nx+= offsetX, ny+= offsetY)
		{
			result*=(1.0-occupiedProbability(nx,ny));
		}
		
		if (offsetX==0 && isEnemyPawnBlocking(m.fromX,m.fromY,m.toX,m.toY)) return 0.0;
		
		return result;
	}
	
	/**
	 * Computes the probability of a bishop move being successful.
	 * @param m
	 * @return
	 */
	public double bishopMoveProbability(core.Move m)
	{
		double result = 1.0;
		int offsetX, offsetY, ite;
		
		if (m.fromX+m.fromY==m.toX+m.toY)
		{
			offsetX = 1; offsetY = -1;
			if (m.fromX>m.toX) { offsetX*=-1; offsetY*=-1; }
			ite = m.toY - m.fromY; if (ite<0) ite*=-1; ite--;
		} else 
		{
			offsetX = 1; offsetY = 1;
			if (m.fromX>m.toX) { offsetX*=-1; offsetY*=-1; }
			ite = m.toX - m.fromX; if (ite<0) ite*=-1; ite--;
		}
		
		int nx, ny, k;
		for (nx = m.fromX+offsetX, ny = m.fromY+offsetY, k = 0; k<ite; k++, nx+= offsetX, ny+= offsetY)
		{
			result*=(1.0-occupiedProbability(nx,ny));
		}
		
		return result;
	}
	
	
	/**
	 * Computes the probability of a Queen move being successful.
	 * @param m
	 * @return
	 */
	public double queenMoveProbability(core.Move m)
	{
		if (m.fromX==m.toX || m.fromY==m.toY) return rookMoveProbability(m);
			else return bishopMoveProbability(m);
	}
	
	/**
	 * Returns the control matrix, that is, the probability that a square be under our control.
	 * @return
	 */
	public void computeControlMatrix()
	{
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++) controlMatrix[k][j] = 0.0;
		
		Vector v = new Vector();
		pseudolegalMoves(v,true);
		
		for (int k=0; k<v.size(); k++)
		{
			core.Move m = (core.Move)v.get(k);
			double prob = moveProbabilityWithoutProtection(m);
			if (m.piece!=PAWN) 
				//result[m.toX][m.toY] = 1.0 - (1.0 - result[m.toX][m.toY])*(1.0-prob);	
				controlMatrix[m.toX][m.toY] = Math.max(controlMatrix[m.toX][m.toY],prob); //faster!
		}
		
		//add pawn control
		int offset = (isWhite? 1 : -1);
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				if (friendlyPieces[k][j]==PAWN)
				{
					if (k>0) controlMatrix[k-1][j+offset] = 1.0;
					if (k<7) controlMatrix[k+1][j+offset] = 1.0;
				}
			}

	}
	
	/**
	 * When we control a square with high probability, the probability to find the enemy king there
	 * decreases accordingly.
	 *
	 */
	public void adjustEnemyKingProbabilityNoCheck()
	{
		double surplus[] = new double[6];
		
		checkTargets = 0;
		checkCandidates = 0;
		
		//we subtract King probability according to our square control.
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				double prob = controlMatrix[k][j];
				//double loss = probabilityInfo[k][j][KING]*prob;
				if (prob>0.8)
				{
					double loss = probabilityInfo[k][j][KING]*prob;
					probabilityInfo[k][j][KING]-=loss;
					surplus[KING]+=loss;
				}
				
			}
			
		distributePositiveProbability(surplus,-1,-1,true);
	}
	
	/**
	 * Infers the position of the enemy King based on a check. Much less simple than it could sound!
	 * @param lastMove
	 * @param capture
	 * @param check1
	 * @param check2
	 */
	public void adjustEnemyKingProbabilityCheck(core.Move lastMove, int capture, int check1, int check2)
	{
		int x[] = new int[15];
		int y[] = new int[15];
		int x2[] = new int[15];
		int y2[] = new int[15];
		int squareNumber[] = new int[2];
		
		checkTargets = 0;
		checkCandidates = 0;
		
		for (int k=0; k<2; k++)
		{
			int squareX[] = (k==0? x : x2);
			int squareY[] = (k==0? y : y2);
			int checkType = (k==0? check1 : check2);
			switch (checkType)
			{
				case CHECK_KNIGHT:
					for (int j=0; j<8; j++)
					{
						int nx = lastMove.toX + knightOffsetX[j];
						int ny = lastMove.toY + knightOffsetY[j];
						if (nx>=0 && ny>=0 && nx<8 && ny<8 && friendlyPieces[nx][ny]==EMPTY &&
						(nx!=lastMove.fromX || ny!=lastMove.fromY))
						{
							squareX[squareNumber[k]] = nx; squareY[squareNumber[k]] = ny;
							squareNumber[k]++;
						}
					}
					checkTargetSquaresX[checkTargets] = lastMove.toX;
					checkTargetSquaresY[checkTargets] = lastMove.toY;
					checkTargets++;
					checkCandidates++;
					break;
					
				case CHECK_RANK:
					//if the piece is uncapable of this type of check, it must have cleared the
					//way for that kind of piece.
					int piece = friendlyPieces[lastMove.toX][lastMove.toY];
					if (piece==ROOK || piece==QUEEN)
					{
						if (capture==NO_CAPTURE || piece==QUEEN || (lastMove.fromY==lastMove.toY && lastMove.toX>lastMove.fromX))
							for (int i=lastMove.toX+1; i<8 && friendlyPieces[i][lastMove.toY]==EMPTY; i++)
							{
								squareX[squareNumber[k]] = i; squareY[squareNumber[k]] = lastMove.toY;
								squareNumber[k]++;
							}
						if (capture==NO_CAPTURE || piece==QUEEN || (lastMove.fromY==lastMove.toY && lastMove.toX<lastMove.fromX))
							for (int i=lastMove.toX-1; i>=0 && friendlyPieces[i][lastMove.toY]==EMPTY; i--)
							{
								squareX[squareNumber[k]] = i; squareY[squareNumber[k]] = lastMove.toY;
								squareNumber[k]++;
							}
						checkTargetSquaresX[checkTargets] = lastMove.toX;
						checkTargetSquaresY[checkTargets] = lastMove.toY;
						checkTargets++;
						checkCandidates++;
					} else
					{
						int direction=0;
						int direction2=0;
						for (int i=lastMove.fromX+1; i<8; i++)
						{
							if (friendlyPieces[i][lastMove.fromY]==ROOK || friendlyPieces[i][lastMove.fromY]==QUEEN)
							{
								direction = -1; //it's on the right, so the king must be on the left of the moved piece.
								checkTargetSquaresX[checkTargets] = i;
								checkTargetSquaresY[checkTargets] = lastMove.toY;
								checkTargets++;	
								checkCandidates++;						
							} else if (friendlyPieces[i][lastMove.fromY]!=EMPTY) i=10000;
						}
						for (int i=lastMove.fromX-1; i>=0; i--)
						{
							if (friendlyPieces[i][lastMove.fromY]==ROOK || friendlyPieces[i][lastMove.fromY]==QUEEN)
							{
								direction2 = 1;
								checkTargetSquaresX[checkTargets] = i;
								checkTargetSquaresY[checkTargets] = lastMove.toY;
								checkTargets++;
								checkCandidates++;
							} else if (friendlyPieces[i][lastMove.fromY]!=EMPTY) i=10000;
						}
						if (direction!=0) for (int i=lastMove.fromX+direction; i>=0 && i<8 && friendlyPieces[i][lastMove.fromY]==EMPTY; i+=direction)
						{
							squareX[squareNumber[k]] = i; squareY[squareNumber[k]] = lastMove.fromY;
							squareNumber[k]++;							
						}
						if (direction2!=0) for (int i=lastMove.fromX+direction2; i>=0 && i<8 && friendlyPieces[i][lastMove.fromY]==EMPTY; i+=direction2)
						{
							squareX[squareNumber[k]] = i; squareY[squareNumber[k]] = lastMove.fromY;
							squareNumber[k]++;							
						}
					}
					break;
				
				case CHECK_FILE:
					//same thing as rank check!
					//if the piece is uncapable of this type of check, it must have cleared the
					//way for that kind of piece.
					piece = friendlyPieces[lastMove.toX][lastMove.toY];
					if (piece==ROOK || piece==QUEEN)
					{
						//if there was a capture, only the squares from the capture point on are eligible...
						if (capture==NO_CAPTURE || piece==QUEEN || (lastMove.fromX==lastMove.toX && lastMove.toY>lastMove.fromY))
							for (int i=lastMove.toY+1; i<8 && friendlyPieces[lastMove.toX][i]==EMPTY; i++)
							{
								squareX[squareNumber[k]] = lastMove.toX; squareY[squareNumber[k]] = i;
								squareNumber[k]++;
							}
						if (capture==NO_CAPTURE || piece==QUEEN || (lastMove.fromX==lastMove.toX && lastMove.toY<lastMove.fromY))
							for (int i=lastMove.toY-1; i>=0 && friendlyPieces[lastMove.toX][i]==EMPTY; i--)
							{
								squareX[squareNumber[k]] = lastMove.toX; squareY[squareNumber[k]] = i;
								squareNumber[k]++;
							}
						checkTargetSquaresX[checkTargets] = lastMove.toX;
						checkTargetSquaresY[checkTargets] = lastMove.toY;
						checkTargets++;
						checkCandidates++;
					} else
					{
						int direction=0;
						int direction2=0;
						for (int i=lastMove.fromY+1; i<8; i++)
						{
							if (friendlyPieces[lastMove.fromX][i]==ROOK || friendlyPieces[lastMove.fromX][i]==QUEEN)
							{
								direction = -1; //it's on the right, so the king must be on the left of the moved piece.
								checkTargetSquaresX[checkTargets] = lastMove.toX;
								checkTargetSquaresY[checkTargets] = i;
								checkTargets++;		
								checkCandidates++;					
							} else if (friendlyPieces[lastMove.fromX][i]!=EMPTY) i = 10000;
						}
						for (int i=lastMove.fromY-1; i>=0; i--)
						{
							if (friendlyPieces[lastMove.fromX][i]==ROOK || friendlyPieces[lastMove.fromX][i]==QUEEN)
							{
								direction2 = 1;
								checkTargetSquaresX[checkTargets] = lastMove.toX;
								checkTargetSquaresY[checkTargets] = i;
								checkTargets++;
								checkCandidates++;
							} else if (friendlyPieces[lastMove.fromX][i]!=EMPTY) i = 10000;
						}
						if (direction!=0) for (int i=lastMove.fromY+direction; i>=0 && i<8 && friendlyPieces[lastMove.fromX][i]==EMPTY; i+=direction)
						{
							squareX[squareNumber[k]] = lastMove.fromX; squareY[squareNumber[k]] = i;
							squareNumber[k]++;							
						}
						if (direction2!=0) for (int i=lastMove.fromY+direction2; i>=0 && i<8 && friendlyPieces[lastMove.fromX][i]==EMPTY; i+=direction2)
						{
							squareX[squareNumber[k]] = lastMove.fromX; squareY[squareNumber[k]] = i;
							squareNumber[k]++;							
						}
					}
					break;
					
				case CHECK_LONG_DIAGONAL:
				case CHECK_SHORT_DIAGONAL:
					//this case is more tricky, mostly because the pawn can move forward
					//and check diagonally, meaning that it can cover, say, a bishop, and both
					//might be responsible for a diagonal check.
					piece = friendlyPieces[lastMove.toX][lastMove.toY];
					if (piece==PAWN || piece==BISHOP || piece==QUEEN)
					{
						int dirX, dirY;
						checkTargetSquaresX[checkTargets] = lastMove.toX;
						checkTargetSquaresY[checkTargets] = lastMove.toY;
						checkTargets++;
						checkCandidates++;
						//on which direction did we move? It's important since the king cannot be everywhere...
						dirX = (lastMove.toX>lastMove.fromX? 1 : (lastMove.toX==lastMove.fromX? 0 : -1));
						dirY = (lastMove.toY>lastMove.fromY? 1 : (lastMove.toY==lastMove.fromY? 0 : -1));
						//the piece itself might be the one giving check.
						for (int ite=0; ite<4; ite++)
						{
							int offX = bishopOffsetX[ite]; int offY = bishopOffsetY[ite];
							int nx, ny, count;
							//the meaning of this is: the king is never on the direction you came from,
							//and can only be on the direction you were moving to IF there was a capture (otherwise,
							//it would have been in check even before moving).
							if ((offX!=-dirX || offY!=-dirY) && (offX!=dirX || offY!=dirY || capture!=NO_CAPTURE))
								for (count=0,nx=lastMove.toX+offX,ny=lastMove.toY+offY; nx>=0 && ny>=0 && nx<8 && ny<8 &&
									friendlyPieces[nx][ny]==EMPTY; nx+=offX, ny+=offY, count++)
								{
									if (piece!=PAWN || (count==0 && offY==(isWhite? 1 : -1)))
									{
										//now, it's not over yet! For each square, is it long or short diagonal from
										//the king's point of view?
										boolean longDiag = (nx>3 && ny>3) || (nx<4 && ny<4);
										if (checkType==CHECK_SHORT_DIAGONAL) longDiag=!longDiag;
										if ((offX*offY==1)==longDiag)
										{
											squareX[squareNumber[k]] = nx; squareY[squareNumber[k]] = ny;
											squareNumber[k]++;										
										}
									}
								}
						}
					}
					if (piece!=BISHOP && piece!=QUEEN)
					{
						//well, the piece might have uncovered the piece threatening the king!
						boolean checks[] = new boolean[4];
						for (int i=0; i<4; i++) checks[i]=false;
						
						for (int i=0; i<4; i++)
						{
							int offX = bishopOffsetX[i]; int offY = bishopOffsetY[i];
							int nx, ny;
							for (nx=lastMove.fromX+offX,ny=lastMove.fromY+offY; nx>=0 && ny>=0 && nx<8 && ny<8; nx+=offX, ny+=offY)
							{
								if (friendlyPieces[nx][ny]==QUEEN || friendlyPieces[nx][ny]==BISHOP)
								{
									checks[(i+2)%4] = true; //put king markers in the opposite direction.
									checkTargetSquaresX[checkTargets] = nx;
									checkTargetSquaresY[checkTargets] = ny;
									checkTargets++;
									checkCandidates++;
								}
									
								else if (friendlyPieces[nx][ny]!=EMPTY) nx=10000; //bail out.
							}
						}
						//now that you know the directions, mark them.
						for (int i=0; i<4; i++)
						{
							if (checks[i])
							{
								int offX = bishopOffsetX[i]; int offY = bishopOffsetY[i];
								int nx, ny;
								for (nx=lastMove.fromX+offX,ny=lastMove.fromY+offY; nx>=0 && ny>=0 && nx<8 && ny<8
								&& friendlyPieces[nx][ny]==EMPTY; nx+=offX, ny+=offY)
								{
									//now, it's not over yet! For each square, is it long or short diagonal from
									//the king's point of view?
									boolean longDiag = (nx>3 && ny>3) || (nx<4 && ny<4);
									if (checkType==CHECK_SHORT_DIAGONAL) longDiag=!longDiag;
									if ((offX*offY==1)==longDiag)
									{
										squareX[squareNumber[k]] = nx; squareY[squareNumber[k]] = ny;
										squareNumber[k]++;										
									}
								}								
							}
						}
					}
					break;

			}
		}
		
		int checkTarget[][] = new int[8][8];
		
		if (squareNumber[0]==0) System.out.println("VERY VERY WRONG!");
		

		for (int k=0; k<squareNumber[0]; k++)
		{
			checkTarget[x[k]][y[k]]++;
		}
		if (check2!=NO_CHECK) //if double check, perform an intersection (that is, only 1 square remains!)
		{ 
			for (int k=0; k<squareNumber[1]; k++)
			{
				checkTarget[x2[k]][y2[k]]++;
			}
			for (int k=0; k<8; k++)
				for (int j=0; j<8; j++) if (checkTarget[k][j]>0) checkTarget[k][j]--;
		}
		
		//record the information for later use... include the suspect pieces that might
		//be responsible for the check, so we know what the opponent might capture with
		//higher probability.
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				if (checkTarget[k][j]>0)
				{
					checkTargetSquaresX[checkTargets] = k;
					checkTargetSquaresY[checkTargets] = j;
					checkTargets++;
				}
			}
		
		
		//now, remove king probabilities from each and every square not marked.
		double surplus = 0.0;
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				if (checkTarget[k][j]==0)
				{
					surplus+= probabilityInfo[k][j][KING];
					probabilityInfo[k][j][KING] = 0.0;
					updateEmptyProbability(k,j);
				}
			}
			
		//now update the probability absolutely...
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++) searchMatrix[k][j]=true;
		
		absoluteDistributePositiveProbability(surplus,KING); //*breathes* aww, it's done!
	}
	
	public static void main(String args[])
	{
		//test...

	}
	

	/**
	 * @return
	 */
	public boolean isWhite() {
		return isWhite;
	}

	/**
	 * @param b
	 */
	public void setWhite(boolean b) {
		isWhite = b;
	}
	
	public static int[] getPieceCaptureVectorX(int piece, boolean white)
	{
		int movementX[] = null;
		switch(piece)
		{
			case Chessboard.QUEEN:
			case Chessboard.KING:
				movementX = Chessboard.queenOffsetX;
				break;
			case Chessboard.ROOK:
				movementX = Chessboard.rookOffsetX;	
				break;	
			case Chessboard.BISHOP:
				movementX = Chessboard.bishopOffsetX;	
				break;	
			case Chessboard.KNIGHT:
				movementX = Chessboard.knightOffsetX;	
				break;	
			case Chessboard.PAWN:
				movementX = Chessboard.pawnOffsetX;
		}
		return movementX;
	}
	
	public static int[] getPieceMovementVectorX(int piece, boolean white)
	{
		if (piece==Chessboard.PAWN) return pawnMoveOffsetX;
		else return getPieceCaptureVectorX(piece,white);
	}
	
	public static int[] getPieceCaptureVectorY(int piece, boolean white)
	{
		int movementX[] = null;
		switch(piece)
		{
			case Chessboard.QUEEN:
			case Chessboard.KING:
				movementX = Chessboard.queenOffsetY;
				break;
			case Chessboard.ROOK:
				movementX = Chessboard.rookOffsetY;	
				break;	
			case Chessboard.BISHOP:
				movementX = Chessboard.bishopOffsetY;	
				break;	
			case Chessboard.KNIGHT:
				movementX = Chessboard.knightOffsetY;	
				break;	
			case Chessboard.PAWN:
				movementX = (white? Chessboard.pawnOffsetWhiteY : Chessboard.pawnOffsetBlackY);
		}
		return movementX;
	}
	
	public static int[] getPieceMovementVectorY(int piece, boolean white)
	{
		if (piece==Chessboard.PAWN) return (white? Chessboard.pawnMoveOffsetYWhite : Chessboard.pawnMoveOffsetYBlack);
		else return getPieceCaptureVectorY(piece,white);
	}
	
	public static boolean movesOnlyOnce(int piece)
	{
		return (piece==PAWN || piece==KNIGHT || piece==KING);
	}
	
	public static boolean isSquareCompatibleWithDiagonalCheck(int x, int y, int check, int offx, int offy)
	{
		boolean longDiag = (x>3 && y>3) || (x<4 && y<4);
		if (check==Chessboard.CHECK_SHORT_DIAGONAL) longDiag=!longDiag;
		if ((offx*offy==1)==longDiag)
		{
			return true;
		} else return false;
	}

}
