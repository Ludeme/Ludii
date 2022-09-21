/*
 * Created on 15-apr-05
 *
 */
package umpire.local;

import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import ai.player.Player;
import core.Chessboard;
import core.Metaposition;
import core.Move;
import pgn.ExtendedPGNGame;
import umpire.Umpire;

/**
 * This class implements the umpire for a local game. The umpire has complete information.
 * @author Nikola Novarlic
 *
 */
public class LocalUmpire extends Umpire implements Cloneable {
	
	public static final int EMPTY = 0;
	public static final int WP = 1;
	public static final int WN = 2;
	public static final int WB = 3;
	public static final int WR = 4;
	public static final int WQ = 5;
	public static final int WK = 6;
	
	public static final int BP = 7;
	public static final int BN = 8;
	public static final int BB = 9;
	public static final int BR = 10;
	public static final int BQ = 11;
	public static final int BK = 12;
	
	public static final int ILLEGAL_NO_SUCH_PIECE = 1;
	public static final int ILLEGAL_INVALID_DESTINATION = 2;
	public static final int ILLEGAL_PIECE_NOT_YOURS = 3;
	public static final int ILLEGAL_MOVEMENT_RULES = 4;
	public static final int ILLEGAL_KING_VS_KING = 5;
	public static final int ILLEGAL_BLOCKED = 10;
	public static final int ILLEGAL_KING_THREATENED = 11;
	
	public boolean wmat = false; 
	public boolean bmat = false;
	
	public static final int pieceCodeBias[] = {1,7}; //constants to convert Chessboard piece codes.
	
	boolean kingMoved[] = new boolean[2];
	boolean kingCastled[] = new boolean[2];
	boolean leftRookMoved[] = new boolean[2];
	boolean rightRookMoved[] = new boolean[2];
	int kingX[] = new int[2];
	int kingY[] = new int[2];
	
	public int board[][] = new int[8][8];
	int turn;
	int passant[] = new int[2];
	int moveUndoData[] = new int[10];
	
	public int check[] = new int[6];
	public int capture;
	public int capX, capY;
	public int tries;
	int fiftyMoves;
	public int moveCount = 0;
	public int promotions[] = new int[2];
	Vector promotionVector = new Vector();
	Vector illegalMoveVector = new Vector();
	
	//This parameter can make the game end after a set amount of moves. Of course
	//this does not exist in regular Kriegspiel, but we are using it in our
	//AI genetic algorithm to only test our AI's for a set number of moves.
	int gameDuration = -1;
	
	int illegalMoveReason;
	int gameOutcome;
	Player winner;
	
	public boolean verbose = true;
	public boolean autoFinish = true; //if true, stops the game due to lack of material
	public boolean adjudication = true;
	
	public static final int WHITE = 0;
	public static final int BLACK = 1;
	
	public static final int NO_OUTCOME = 0;
	public static final int OUTCOME_CHECKMATE = 1;
	public static final int OUTCOME_STALEMATE = 2;
	public static final int OUTCOME_FIFTY_MOVES = 3;
	public static final int OUTCOME_NO_MATERIAL = 4;
	public static final int OUTCOME_AGREED_DRAW = 5;
	public static final int OUTCOME_RESIGN = 6;
	public static final int OUTCOME_ADJUDICATION = 7;
	
	public static final int defaultBoard[][] =
	{{WR,WP,EMPTY,EMPTY,EMPTY,EMPTY,BP,BR},
	{WN,WP,EMPTY,EMPTY,EMPTY,EMPTY,BP,BN},
	{WB,WP,EMPTY,EMPTY,EMPTY,EMPTY,BP,BB},
	{WQ,WP,EMPTY,EMPTY,EMPTY,EMPTY,BP,BQ},
	{WK,WP,EMPTY,EMPTY,EMPTY,EMPTY,BP,BK},
	{WB,WP,EMPTY,EMPTY,EMPTY,EMPTY,BP,BB},
	{WN,WP,EMPTY,EMPTY,EMPTY,EMPTY,BP,BN},
	{WR,WP,EMPTY,EMPTY,EMPTY,EMPTY,BP,BR}};
	
	//private LocalUmpire testbed = null; //here we make legality tests.
	//private boolean testbedinit = false;
	
	Vector listeners = new Vector();
	
	/*{{WR,WN,WB,WQ,WK,WB,WN,WR},
	{WP,WP,WP,WP,WP,WP,WP,WP},
	{EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY},
	{EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY},
	{EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY},
	{EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY},
	{BP,BP,BP,BP,BP,BP,BP,BP},
	{BR,BN,BB,BQ,BK,BB,BN,BR}};*/
	
	
	
	
	/**
	 * Creates a new umpire given two players. The umpire will start the game immediately.
	 * @param pl1
	 * @param pl2
	 */
	public LocalUmpire(Player pl1, Player pl2)
	{
		this.setP1(pl1==null? new Player() : pl1);
		this.setP2(pl2==null? new Player() : pl2);
		
		if (getP1()!=null) getP1().setCurrentUmpire(this);
		if (getP2()!=null) getP2().setCurrentUmpire(this);
		
		resetBoard();
	}
	
	public Object clone()
	{
		LocalUmpire lu;
		try {
			lu = (LocalUmpire) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
		
		lu.board = new int[8][8];
		for (int k=0; k<8; k++) for (int j=0; j<8; j++) lu.board[k][j] = this.board[k][j];
		
		lu.kingMoved = new boolean[2];
		lu.kingCastled = new boolean[2];
		lu.leftRookMoved = new boolean[2];
		lu.rightRookMoved = new boolean[2];
		lu.kingX = new int[2];
		lu.kingY = new int[2];
		lu.passant = new int[2];
		lu.check = new int[2];
		
		for (int k=0; k<2; k++)
		{
			lu.kingMoved[k] = this.kingMoved[k];
			lu.kingCastled[k] = this.kingCastled[k];
			lu.leftRookMoved[k] = this.leftRookMoved[k];
			lu.rightRookMoved[k] = this.rightRookMoved[k]; 
			lu.kingX[k] = this.kingX[k];
			lu.kingY[k] = this.kingY[k];
			lu.passant[k] = this.passant[k];
			lu.check[k] = this.check[k];
		}
		
		return lu;
	}
	
	/*private void testbedClone()
	{
		for (int k=0; k<8; k++) for (int j=0; j<8; j++) testbed.board[k][j] = this.board[k][j];
		for (int k=0; k<2; k++)
		{
			testbed.kingMoved[k] = this.kingMoved[k];
			testbed.kingCastled[k] = this.kingCastled[k];
			testbed.leftRookMoved[k] = this.leftRookMoved[k];
			testbed.rightRookMoved[k] = this.rightRookMoved[k]; 
			testbed.kingX[k] = this.kingX[k];
			testbed.kingY[k] = this.kingY[k];
			testbed.passant[k] = this.passant[k];
			testbed.check[k] = this.check[k];
		}
	}*/
	
	public ExtendedPGNGame arbitrate(int initialPosition[][])
	{
		Hashtable d1, d2; //parameters to send both players
		ExtendedPGNGame transcript = new ExtendedPGNGame();
		int illegal = 0;
		d1 = new Hashtable(); d2 = new Hashtable();
		
		d1.put("isWhite",new Boolean(true));
		d2.put("isWhite",new Boolean(false));
		
		if (getP1()!=null && getP1().playerName!=null) d1.put("white", getP1().playerName);
		if (getP2()!=null && getP2().playerName!=null) d1.put("black", getP2().playerName);
		
		d1.put("where", "local");
		d2.put("where", "local");
		
		if (initialPosition==null) resetBoard();
		getP1().startMatch(d1);
		getP2().startMatch(d2);
		
		if (initialPosition!=null)
		{
			//game is not starting from default position
			for (int k=0; k<8; k++) for (int j=0; j<8; j++)
			{
				board[k][j] = initialPosition[k][j];
				if (board[k][j]==LocalUmpire.WK)
				{
					kingX[WHITE] = k; kingY[WHITE] = j;
				}
				if (board[k][j]==LocalUmpire.BK)
				{
					kingX[BLACK] = k; kingY[BLACK] = j;
				}
			}
			//now create the appropriate Metaposition...
			getP1().simplifiedBoard = this.exportChessboard(WHITE);
			getP2().simplifiedBoard = this.exportChessboard(BLACK);
			fireChessboardChangeNotification();
		}

		transcript.setWhite("P1");
		transcript.setBlack("P2");
		
		while (true)
		{
			if (turn==WHITE) moveCount++;
			
			int playerNumber = turn;
			Player whoseTurn = (turn==WHITE? getP1() : getP2());
			Player opponent = (turn==WHITE? getP2() : getP1());
			
			Move m = whoseTurn.getNextMove();
			
			if (m==null)
			{
				if (verbose) System.out.println(boardState());
			}
			
			
			
			if (isLegalMove(m,playerNumber))
			{
				//legal move.
				illegal = 0;
				
				if (verbose) System.out.println("Move "+m+" by Player "+playerNumber+" is legal.");
				doMove(m,playerNumber);
				if (verbose) System.out.println(boardState());
				
				
				//game not over.
				int cap = this.capture;
				int cx = this.capX;
				int cy = this.capY;
				int chk = Chessboard.NO_CHECK;
				int chk2 = Chessboard.NO_CHECK;
				if (isKingThreatened(null,1-playerNumber))
				{
					chk = this.check[0]; chk2 = this.check[1];
				}
				int pawnTries = legalMoveNumber(1-playerNumber,true);
				int captureType = Chessboard.NO_CAPTURE;
				if (cap!=EMPTY)
				{
					if (this.umpire2ChessboardPieceCode(cap)==Chessboard.PAWN)
						captureType = Chessboard.CAPTURE_PAWN;
					else captureType = Chessboard.CAPTURE_PIECE;
				} else { cx = cy = -1; }
				
				//updates the fifty move check.
				if (captureType!=Chessboard.NO_CAPTURE || 
					umpire2ChessboardPieceCode(board[m.toX][m.toY])==Chessboard.PAWN)
					fiftyMoves = 0;
					else fiftyMoves++;
					
				transcript.addMove(playerNumber==WHITE,m,cx,cy,captureType,chk,chk2,pawnTries);
				//add previously recorded illegal moves and clear them
				for (int k=0; k<illegalMoveVector.size(); k++)
					transcript.getLatestMove(playerNumber==WHITE).addFailedMove((Move)illegalMoveVector.get(k));
				illegalMoveVector.clear();
				
				
				int outcome = this.gameOutcome(1-playerNumber);
				gameOutcome = outcome; //copy it for later access
				if (outcome==OUTCOME_CHECKMATE)
				{
					winner = whoseTurn;
					whoseTurn.communicateOutcome(Player.PARAM_CHECKMATE_VICTORY);
					opponent.communicateOutcome(Player.PARAM_CHECKMATE_DEFEAT);
					transcript.setResult(playerNumber==WHITE? "1-0" : "0-1");
					if (verbose) System.out.println("Player "+playerNumber+" wins!");
					getP1().receiveAftermath(transcript);
					getP2().receiveAftermath(transcript);
					return transcript;
				}
				if (outcome==OUTCOME_ADJUDICATION)
				{
					winner.communicateOutcome(Player.PARAM_CHECKMATE_VICTORY);
					(winner==getP1()? getP2() : getP1()).communicateOutcome(Player.PARAM_CHECKMATE_DEFEAT);
					transcript.setResult(winner==getP1()? "1-0" : "0-1");
					if (verbose) System.out.println("Player "+(winner==getP1()?"0":"1")+" wins!");
					gameOutcome = OUTCOME_CHECKMATE;
					getP1().receiveAftermath(transcript);
					getP2().receiveAftermath(transcript);
					return transcript;
				}
				if (outcome==OUTCOME_STALEMATE)
				{
					winner = null;
					whoseTurn.communicateOutcome(Player.PARAM_STALEMATE_DRAW);
					opponent.communicateOutcome(Player.PARAM_STALEMATE_DRAW);
					transcript.setResult("1/2-1/2");
					if (verbose) System.out.println("Stalemate!");
					getP1().receiveAftermath(transcript);
					getP2().receiveAftermath(transcript);
					return transcript;					
				}
				if (outcome==OUTCOME_FIFTY_MOVES)
				{
					winner = null;
					whoseTurn.communicateOutcome(Player.PARAM_50_DRAW);
					opponent.communicateOutcome(Player.PARAM_50_DRAW);
					transcript.setResult("1/2-1/2");
					if (verbose) System.out.println("Fifty Moves Draw!");
					getP1().receiveAftermath(transcript);
					getP2().receiveAftermath(transcript);
					return transcript;					
				}
				
				if (outcome==OUTCOME_NO_MATERIAL)
				{
					winner = null;
					whoseTurn.communicateOutcome(Player.PARAM_NO_MATERIAL);
					opponent.communicateOutcome(Player.PARAM_NO_MATERIAL);
					transcript.setResult("1/2-1/2");
					if (verbose) System.out.println("No Material Draw!");
					getP1().receiveAftermath(transcript);
					getP2().receiveAftermath(transcript);
					return transcript;					
				}


				whoseTurn.communicateLegalMove(captureType,pawnTries,chk,chk2);
				opponent.communicateUmpireMessage(cx,cy,pawnTries,chk,chk2, captureType);
				
				
				
			} else
			{
				//illegal move.
				if (verbose) System.out.println("Move "+m+" by Player "+playerNumber+" is NOT legal (reason: "+
					illegalMoveReason+").");
				
				illegal++;
				if (illegal>100)
				{
					winner = null;
					whoseTurn.communicateOutcome(Player.PARAM_50_DRAW);
					opponent.communicateOutcome(Player.PARAM_50_DRAW);
					transcript.setResult("1/2-1/2");
					if (verbose) System.out.println("Fifty Moves Draw!");
					getP1().receiveAftermath(transcript);
					getP2().receiveAftermath(transcript);
					return transcript;					
				}
				
				illegalMoveVector.add(m);
				whoseTurn.communicateIllegalMove(m);
			}
			
		}
	}
	
	/**
	 * Performs the necessary arbitration to execute a full game of Kriegspiel.
	 *
	 */
	public ExtendedPGNGame arbitrate()
	{
		return (arbitrate(null));
	}
	
	/**
	 * Brings the board back to its initial configuration.
	 *
	 */
	public void resetBoard()
	{
		for (int k=0; k<8; k++) for (int j=0; j<8; j++) board[k][j] = defaultBoard[k][j];
		turn = WHITE;
		fiftyMoves = 0;
		passant[0] = passant[1] = -1;
		kingMoved[0] = kingMoved[1] = false;
		kingCastled[0] = kingCastled[1] = false;
		leftRookMoved[0] = leftRookMoved[1] = false;
		rightRookMoved[0] = rightRookMoved[1] = false;
		promotions[0] = promotions[1] = 0;
		promotionVector.clear();
		
		kingX[WHITE] = 4; kingY[WHITE] = 0;
		kingX[BLACK] = 4; kingY[BLACK] = 7;
	}
	
	/**
	 * Constant conversion.
	 * @param umpirePieceCode
	 * @return
	 */
	public int umpire2ChessboardPieceCode(int umpirePieceCode)
	{
		if (umpirePieceCode==0) return Chessboard.EMPTY;
		return (umpirePieceCode>=7? umpirePieceCode-7 : umpirePieceCode-1);
	}
	
	/**
	 * Returns whether a given piece belongs to a player.
	 * @param code
	 * @param player
	 * @return
	 */
	public boolean pieceBelongsToPlayer(int code, int player)
	{
		if (code>=7) return (player==BLACK); else return (player==WHITE);
	}
	
	/**
	 * Returns whether doing a certain move would cause the king to be threatened.
	 * @param m
	 * @param player
	 * @return
	 */
	public boolean isKingThreatened(Move m, int player)
	{
		//simulate the effects of the move on the board, then undo them before returning.
		
		//LocalUmpire ump;
		//try {
			//ump = (LocalUmpire) this.clone();
		//} catch (CloneNotSupportedException e) {
			//e.printStackTrace();
			//return false;
		//}
		
		if (m!=null) miniDoMove(m,player);
		
		int nx, ny;
		int checkNumber=0;
		check[0] = check[1] = Chessboard.NO_CHECK;
		
		for (int k=0; k<8; k++)
		{
			nx = kingX[player] + Chessboard.knightOffsetX[k];
			ny = kingY[player] + Chessboard.knightOffsetY[k];
			
			if (nx>=0 && ny>=0 && nx<8 && ny<8)
			{
				int piece = board[nx][ny];
				if (umpire2ChessboardPieceCode(piece)==Chessboard.KNIGHT
					&& !pieceBelongsToPlayer(piece,player))
				{
					check[checkNumber++] = Chessboard.CHECK_KNIGHT;
				}
			}
		}
		
		for (int k=0; k<8; k++)
		{
			int offsetX = Chessboard.queenOffsetX[k];
			int offsetY = Chessboard.queenOffsetY[k];
			
			for (nx = kingX[player] + offsetX, ny = kingY[player] + offsetY;
				nx>=0 && ny>=0 && nx<8 && ny<8; nx+=offsetX, ny+=offsetY)
			{
				if (board[nx][ny]!=EMPTY && !pieceBelongsToPlayer(board[nx][ny],player))
				{
					int type = this.umpire2ChessboardPieceCode(board[nx][ny]);
					if ((type==Chessboard.ROOK || type==Chessboard.QUEEN) &&
						(offsetX==0 || offsetY==0))
					{
						if (offsetX==0) check[checkNumber++] = Chessboard.CHECK_FILE;
						if (offsetY==0) check[checkNumber++] = Chessboard.CHECK_RANK;
					}
					boolean isPawnCheck = (type==Chessboard.PAWN && (nx==kingX[player]+1 ||
						nx==kingX[player]-1) && ny==kingY[player]+(player==WHITE? 1 : -1));
					if ((type==Chessboard.BISHOP || type==Chessboard.QUEEN || isPawnCheck) &&
						(offsetX!=0 && offsetY!=0))
					{
						boolean longdiag = ((kingX[player]<4 && kingY[player]<4) || 
							(kingX[player]>=4 && kingY[player]>=4));
						if (offsetX*offsetY==-1) longdiag=!longdiag;
						if (longdiag) check[checkNumber++] = Chessboard.CHECK_LONG_DIAGONAL;
						else check[checkNumber++] = Chessboard.CHECK_SHORT_DIAGONAL;
					}
					
				}
				if (board[nx][ny]!=EMPTY) nx = 10000; //exit loop
			}
			
		}
		if (m!=null) miniUndoMove(m,player);
		return (checkNumber!=0);
		
	}
	
	/**
	 * Phisically performs a move, supposing it is legal.
	 * @param m
	 * @param player
	 */
	public void doMove(Move m, int player)
	{
		turn = 1 - turn;
		if (m.fromX<0 || m.fromY<0) return;
		capture = board[m.toX][m.toY];
		capX = m.toX; capY = m.toY;
		
		//en passant
		if (m.piece==Chessboard.PAWN && m.fromX!=m.toX && board[m.toX][m.toY]==EMPTY)
		{
			int realY = m.toY + (player==WHITE? -1 : 1);
			capture = board[m.toX][realY];
			board[m.toX][realY]=EMPTY;
			capY = realY;
		}
			
		
		board[m.fromX][m.fromY]=EMPTY;
		board[m.toX][m.toY]=m.piece+pieceCodeBias[player];
		
		if (m.piece==Chessboard.KING)
		{
			kingX[player] = m.toX; kingY[player] = m.toY;
			kingMoved[player] = true;
			if (m.toX==m.fromX-2)
			{
				board[0][m.toY] = EMPTY;
				board[3][m.toY] = Chessboard.ROOK+pieceCodeBias[player];
				kingCastled[player] = true;
			}
			if (m.toX==m.fromX+2)
			{
				board[7][m.toY] = EMPTY;
				board[5][m.toY] = Chessboard.ROOK+pieceCodeBias[player];
				kingCastled[player] = true;
			}
		}
		
		if (m.piece==Chessboard.PAWN && m.toY==m.fromY+(player==WHITE? 2 : -2))
		{
			passant[player]=m.toX;
		} else passant[player]=-1;
		
		if (m.piece==Chessboard.PAWN && m.toY==(player==WHITE? 7 : 0))
		{
			promotions[player]++;
			promotionVector.add(new Integer(moveCount));
			board[m.toX][m.toY] = m.promotionPiece+pieceCodeBias[player];
		}
		
		if (m.fromX==0 && m.fromY==0) leftRookMoved[0] = true; 
		if (m.fromX==7 && m.fromY==0) rightRookMoved[0] = true;
		if (m.fromX==0 && m.fromY==7) leftRookMoved[1] = true;
		if (m.fromX==7 && m.fromY==7) rightRookMoved[1] = true;
		
		if (m.toX==0 && m.toY==0) leftRookMoved[0] = true; 
		if (m.toX==7 && m.toY==0) rightRookMoved[0] = true;
		if (m.toX==0 && m.toY==7) leftRookMoved[1] = true;
		if (m.toX==7 && m.toY==7) rightRookMoved[1] = true;
		
		fireChessboardChangeNotification();
		
	}
	
	public void miniDoMove(Move m, int player)
	{
		
		
		moveUndoData[0] = board[m.fromX][m.fromY];
		moveUndoData[1] = board[m.toX][m.toY];
		moveUndoData[2] = kingX[player];
		moveUndoData[3] = kingY[player];
		moveUndoData[4] = -1; //en passant content
		moveUndoData[5] = board[0][m.toY]; //castling data
		moveUndoData[6] = board[3][m.toY]; //castling data
		moveUndoData[7] = board[7][m.toY]; //castling data
		moveUndoData[8] = board[5][m.toY]; //castling data
		
		//capture = board[m.toX][m.toY];
		
		//en passant
		if (m.piece==Chessboard.PAWN && m.fromX!=m.toX && board[m.toX][m.toY]==EMPTY)
		{
			int realY = m.toY + (player==WHITE? -1 : 1);
			moveUndoData[4] = board[m.toX][realY];
			//capture = board[m.toX][realY];
			board[m.toX][realY]=EMPTY;
		}
			
		
		board[m.fromX][m.fromY]=EMPTY;
		board[m.toX][m.toY]=m.piece+pieceCodeBias[player];
		
		if (m.piece==Chessboard.KING)
		{
			kingX[player] = m.toX; kingY[player] = m.toY;
			if (m.toX==m.fromX-2)
			{
				board[0][m.toY] = EMPTY;
				board[3][m.toY] = Chessboard.ROOK+pieceCodeBias[player];
			}
			if (m.toX==m.fromX+2)
			{
				board[7][m.toY] = EMPTY;
				board[5][m.toY] = Chessboard.ROOK+pieceCodeBias[player];
			}
		}
		
		if (m.piece==Chessboard.PAWN && m.toY==(player==WHITE? 7 : 0))
		{
			board[m.toX][m.toY] = m.promotionPiece+pieceCodeBias[player];
		}
	}
	
	public void miniUndoMove(Move m, int player)
	{
		board[m.fromX][m.fromY] = moveUndoData[0];
		board[m.toX][m.toY] = moveUndoData[1];
		kingX[player] = moveUndoData[2];
		kingY[player] = moveUndoData[3];
		if (moveUndoData[4]!=-1) //en passant content
		{
			int realY = m.toY + (player==WHITE? -1 : 1);
			board[m.toX][realY] = moveUndoData[4];
		}
		board[0][m.toY] = moveUndoData[5]; //castling data
		board[3][m.toY] = moveUndoData[6]; //castling data
		board[7][m.toY] = moveUndoData[7]; //castling data
		board[5][m.toY] = moveUndoData[8]; //castling data
	}
	
	public boolean canCastleLeft(int player)
	{
		int ky = (player==WHITE? 0 : 7);
		if (board[1][ky]!=EMPTY || board[2][ky]!=EMPTY || board[3][ky]!=EMPTY) return false;
		return (!kingMoved[player] && !kingCastled[player] && !leftRookMoved[player]);
	}
	
	public boolean canCastleRight(int player)
	{
		int ky = (player==WHITE? 0 : 7);
		if (board[5][ky]!=EMPTY || board[6][ky]!=EMPTY) return false;
		return (!kingMoved[player] && !kingCastled[player] && !rightRookMoved[player]);
	}
	
	/**
	 * Returns the number of legal moves for a given player.
	 * @param player
	 * @return
	 */
	public int legalMoveNumber(int player, boolean pawnTriesOnly)
	{
		int number=0;
		
		for (int k=0; k<8; k++) for (int j=0; j<8; j++)
		{
			if (board[k][j]!=EMPTY && pieceBelongsToPlayer(board[k][j],player))
			{
				int piece = this.umpire2ChessboardPieceCode(board[k][j]);
				int offX[] = {}; int offY[] = {};
				switch (piece)
				{
					case Chessboard.PAWN:
					int offset = (player==WHITE? 1 : -1);
					if (!pawnTriesOnly) if (isLegalMove(player,Chessboard.PAWN,k,j,k,j+offset,Chessboard.QUEEN)) number++;
					if (!pawnTriesOnly) if (isLegalMove(player,Chessboard.PAWN,k,j,k,j+offset*2,Chessboard.QUEEN)) number++;
					if (isLegalMove(player,Chessboard.PAWN,k,j,k-1,j+offset,Chessboard.QUEEN)) number++;
					if (isLegalMove(player,Chessboard.PAWN,k,j,k+1,j+offset,Chessboard.QUEEN)) number++;
					break;
					
					case Chessboard.KING:
					if (!pawnTriesOnly) for (int i=0; i<8; i++)
						if (isLegalMove(player,Chessboard.KING,k,j,k+Chessboard.queenOffsetX[i],
							j+Chessboard.queenOffsetY[i],Chessboard.EMPTY)) number++;
					break;
					
					case Chessboard.KNIGHT:
					if (!pawnTriesOnly) for (int i=0; i<8; i++)
						if (isLegalMove(player,Chessboard.KNIGHT,k,j,k+Chessboard.knightOffsetX[i],
							j+Chessboard.knightOffsetY[i],Chessboard.EMPTY)) number++;
					break;
					
					case Chessboard.BISHOP:
					offX = Chessboard.bishopOffsetX;
					offY = Chessboard.bishopOffsetY;
					
					case Chessboard.ROOK:
					offX = Chessboard.rookOffsetX;
					offY = Chessboard.rookOffsetY;
					
					case Chessboard.QUEEN:
					offX = Chessboard.queenOffsetX;
					offY = Chessboard.queenOffsetY;
					
					if (!pawnTriesOnly) for (int ite=0; ite<offX.length; ite++)
					{
						int nx, ny;
						for (nx = k+offX[ite], ny = j+offY[ite]; nx>=0 && ny>=0 && nx<8 && ny<8;
							nx+=offX[ite], ny+=offY[ite])
						{
							if (isLegalMove(player,piece,k,j,nx,ny,Chessboard.EMPTY)) number++;
						}
					}
					break;
										
				}
			}
		}
		
		return number;
	}
	
	public Vector<Move> legalMoves(int player, boolean pawnTriesOnly)
	{
		Vector<Move> result = new Vector<Move>();
		
		for (int k=0; k<8; k++) for (int j=0; j<8; j++)
		{
			if (board[k][j]!=EMPTY && pieceBelongsToPlayer(board[k][j],player))
			{
				int piece = this.umpire2ChessboardPieceCode(board[k][j]);
				int offX[] = {}; int offY[] = {};
				switch (piece)
				{
					case Chessboard.PAWN:
					int offset = (player==WHITE? 1 : -1);
					if (!pawnTriesOnly) if (isLegalMove(player,Chessboard.PAWN,k,j,k,j+offset,Chessboard.QUEEN))
					{
						Move m = new Move();
						m.fromX = (byte)k;
						m.fromY = (byte)j;
						m.toX = (byte)k;
						m.toY = (byte)(j+offset);
						m.piece = Chessboard.PAWN;
						m.promotionPiece = Chessboard.QUEEN;
						result.add(m);
					}
					if (!pawnTriesOnly) if (isLegalMove(player,Chessboard.PAWN,k,j,k,j+offset*2,Chessboard.QUEEN))
					{
						Move m = new Move();
						m.fromX = (byte)k;
						m.fromY = (byte)j;
						m.toX = (byte)k;
						m.toY = (byte)(j+offset*2);
						m.piece = Chessboard.PAWN;
						m.promotionPiece = Chessboard.QUEEN;
						result.add(m);
					}
					if (isLegalMove(player,Chessboard.PAWN,k,j,k-1,j+offset,Chessboard.QUEEN))
					{
						Move m = new Move();
						m.fromX = (byte)k;
						m.fromY = (byte)j;
						m.toX = (byte)(k-1);
						m.toY = (byte)(j+offset);
						m.piece = Chessboard.PAWN;
						m.promotionPiece = Chessboard.QUEEN;
						result.add(m);
					}
					if (isLegalMove(player,Chessboard.PAWN,k,j,k+1,j+offset,Chessboard.QUEEN))
					{
						Move m = new Move();
						m.fromX = (byte)k;
						m.fromY = (byte)j;
						m.toX = (byte)(k+1);
						m.toY = (byte)(j+offset);
						m.piece = Chessboard.PAWN;
						m.promotionPiece = Chessboard.QUEEN;
						result.add(m);
					}
					break;
					
					case Chessboard.KING:
					if (!pawnTriesOnly) for (int i=0; i<8; i++)
						if (isLegalMove(player,Chessboard.KING,k,j,k+Chessboard.queenOffsetX[i],
							j+Chessboard.queenOffsetY[i],Chessboard.EMPTY))
						{
							Move m = new Move();
							m.fromX = (byte)k;
							m.fromY = (byte)j;
							m.toX = (byte)(k+Chessboard.queenOffsetX[i]);
							m.toY = (byte)(j+Chessboard.queenOffsetY[i]);
							m.piece = Chessboard.KING;
							result.add(m);
						}
					break;
					
					case Chessboard.KNIGHT:
					if (!pawnTriesOnly) for (int i=0; i<8; i++)
						if (isLegalMove(player,Chessboard.KNIGHT,k,j,k+Chessboard.knightOffsetX[i],
							j+Chessboard.knightOffsetY[i],Chessboard.EMPTY))
						{
							Move m = new Move();
							m.fromX = (byte)k;
							m.fromY = (byte)j;
							m.toX = (byte)(k+Chessboard.knightOffsetX[i]);
							m.toY = (byte)(j+Chessboard.knightOffsetY[i]);
							m.piece = Chessboard.KNIGHT;
							result.add(m);
						}
					break;
					
					case Chessboard.BISHOP:
					offX = Chessboard.bishopOffsetX;
					offY = Chessboard.bishopOffsetY;
					
					case Chessboard.ROOK:
					offX = Chessboard.rookOffsetX;
					offY = Chessboard.rookOffsetY;
					
					case Chessboard.QUEEN:
					offX = Chessboard.queenOffsetX;
					offY = Chessboard.queenOffsetY;
					
					if (!pawnTriesOnly) for (int ite=0; ite<offX.length; ite++)
					{
						int nx, ny;
						for (nx = k+offX[ite], ny = j+offY[ite]; nx>=0 && ny>=0 && nx<8 && ny<8;
							nx+=offX[ite], ny+=offY[ite])
						{
							if (isLegalMove(player,piece,k,j,nx,ny,Chessboard.EMPTY))
							{
								Move m = new Move();
								m.fromX = (byte)k;
								m.fromY = (byte)j;
								m.toX = (byte)(nx);
								m.toY = (byte)(ny);
								m.piece = (byte)piece;
								result.add(m);
							}
						}
					}
					break;
										
				}
			}
		}
		
		return result;
	}
	
	public Vector<Move> pseudolegalMoves(int player, boolean pawnTriesOnly)
	{
		Vector<Move> result = new Vector<Move>();
		
		for (int k=0; k<8; k++) for (int j=0; j<8; j++)
		{
			if (board[k][j]!=EMPTY && pieceBelongsToPlayer(board[k][j],player))
			{
				int piece = this.umpire2ChessboardPieceCode(board[k][j]);
				int offX[] = {}; int offY[] = {};
				switch (piece)
				{
					case Chessboard.PAWN:
					int offset = (player==WHITE? 1 : -1);
					if (!pawnTriesOnly) if (isPseudolegalMove(player,Chessboard.PAWN,k,j,k,j+offset,Chessboard.QUEEN))
					{
						Move m = new Move();
						m.fromX = (byte)k;
						m.fromY = (byte)j;
						m.toX = (byte)k;
						m.toY = (byte)(j+offset);
						m.piece = Chessboard.PAWN;
						m.promotionPiece = Chessboard.QUEEN;
						result.add(m);
					}
					if (!pawnTriesOnly) if (isPseudolegalMove(player,Chessboard.PAWN,k,j,k,j+offset*2,Chessboard.QUEEN))
					{
						Move m = new Move();
						m.fromX = (byte)k;
						m.fromY = (byte)j;
						m.toX = (byte)k;
						m.toY = (byte)(j+offset*2);
						m.piece = Chessboard.PAWN;
						m.promotionPiece = Chessboard.QUEEN;
						result.add(m);
					}
					if (isPseudolegalMove(player,Chessboard.PAWN,k,j,k-1,j+offset,Chessboard.QUEEN))
					{
						Move m = new Move();
						m.fromX = (byte)k;
						m.fromY = (byte)j;
						m.toX = (byte)(k-1);
						m.toY = (byte)(j+offset);
						m.piece = Chessboard.PAWN;
						m.promotionPiece = Chessboard.QUEEN;
						result.add(m);
					}
					if (isPseudolegalMove(player,Chessboard.PAWN,k,j,k+1,j+offset,Chessboard.QUEEN))
					{
						Move m = new Move();
						m.fromX = (byte)k;
						m.fromY = (byte)j;
						m.toX = (byte)(k+1);
						m.toY = (byte)(j+offset);
						m.piece = Chessboard.PAWN;
						m.promotionPiece = Chessboard.QUEEN;
						result.add(m);
					}
					break;
					
					case Chessboard.KING:
					if (!pawnTriesOnly) for (int i=0; i<8; i++)
						if (isPseudolegalMove(player,Chessboard.KING,k,j,k+Chessboard.queenOffsetX[i],
							j+Chessboard.queenOffsetY[i],Chessboard.EMPTY))
						{
							Move m = new Move();
							m.fromX = (byte)k;
							m.fromY = (byte)j;
							m.toX = (byte)(k+Chessboard.queenOffsetX[i]);
							m.toY = (byte)(j+Chessboard.queenOffsetY[i]);
							m.piece = Chessboard.KING;
							result.add(m);
						}
					break;
					
					case Chessboard.KNIGHT:
					if (!pawnTriesOnly) for (int i=0; i<8; i++)
						if (isPseudolegalMove(player,Chessboard.KNIGHT,k,j,k+Chessboard.knightOffsetX[i],
							j+Chessboard.knightOffsetY[i],Chessboard.EMPTY))
						{
							Move m = new Move();
							m.fromX = (byte)k;
							m.fromY = (byte)j;
							m.toX = (byte)(k+Chessboard.knightOffsetX[i]);
							m.toY = (byte)(j+Chessboard.knightOffsetY[i]);
							m.piece = Chessboard.KNIGHT;
							result.add(m);
						}
					break;
					
					case Chessboard.BISHOP:
					offX = Chessboard.bishopOffsetX;
					offY = Chessboard.bishopOffsetY;
					
					case Chessboard.ROOK:
					offX = Chessboard.rookOffsetX;
					offY = Chessboard.rookOffsetY;
					
					case Chessboard.QUEEN:
					offX = Chessboard.queenOffsetX;
					offY = Chessboard.queenOffsetY;
					
					if (!pawnTriesOnly) for (int ite=0; ite<offX.length; ite++)
					{
						int nx, ny;
						for (nx = k+offX[ite], ny = j+offY[ite]; nx>=0 && ny>=0 && nx<8 && ny<8;
							nx+=offX[ite], ny+=offY[ite])
						{
							if (isPseudolegalMove(player,piece,k,j,nx,ny,Chessboard.EMPTY))
							{
								Move m = new Move();
								m.fromX = (byte)k;
								m.fromY = (byte)j;
								m.toX = (byte)(nx);
								m.toY = (byte)(ny);
								m.piece = (byte)piece;
								result.add(m);
							}
						}
					}
					break;
										
				}
			}
		}
		
		return result;
	}
	
	public Vector<Move> legalMoves(boolean white)
	{
		return legalMoves((white?0:1),false);
	}
	
	public boolean isLegalMove(int player, int piece, int x1, int y1, int x2, int y2, int promPiece)
	{
		Move m = new Move();
		m.fromX = (byte)x1; m.fromY = (byte)y1; m.toX = (byte)x2; m.toY = (byte)y2; m.piece = (byte)piece;
		m.promotionPiece = (byte)promPiece;
		
		return isLegalMove(m,player);
	}
	
	public boolean isPseudolegalMove(int player, int piece, int x1, int y1, int x2, int y2, int promPiece)
	{
		Move m = new Move();
		m.fromX = (byte)x1; m.fromY = (byte)y1; m.toX = (byte)x2; m.toY = (byte)y2; m.piece = (byte)piece;
		m.promotionPiece = (byte)promPiece;
		
		return isPseudolegalMove(m,player);
	}
	
	/**
	 * Checks for the end of a game at the beginning of a player's turn.
	 * @param player
	 * @return
	 */
	public int gameOutcome(int player)
	{
		
		
		

		int wc, bc;
		wc = bc = 0;
		
		wmat = bmat = false;
		
		//check if at least one player has enough mating material
		for (int x=0; x<8; x++)
			for (int y=0; y<8; y++)
				switch (board[x][y])
				{
				case WP:
				case WR:
				case WQ:
					wmat = true;
					break;
				case BP:
				case BR:
				case BQ:
					bmat = true;
					break;
				case BN:
				case BB:
					bc++; if (bc>=2) bmat = true;
					break;
				case WN:
				case WB:
					wc++; if (wc>=2) wmat = true;
					break;
				}
		
		if (gameDuration>0 && moveCount>=gameDuration) return OUTCOME_FIFTY_MOVES; //custom
				
		if (autoFinish && !wmat && !bmat) return OUTCOME_NO_MATERIAL;
		
		if (adjudication && fiftyMoves>1 && (wmat^bmat)) { winner = (wmat? getP1() : getP2()); return OUTCOME_ADJUDICATION; } 
		
		if (legalMoveNumber(player,false)>0 && fiftyMoves<100) return NO_OUTCOME; //game not over yet.
		
		if (isKingThreatened(null,player) && fiftyMoves<100) return OUTCOME_CHECKMATE;
		
		if (fiftyMoves==100) return OUTCOME_FIFTY_MOVES;
		
		return OUTCOME_STALEMATE;
	}
	
	/**
	 * Returns whether a certain move is legal for a given player.
	 * @param m
	 * @param player
	 * @return
	 */
	public boolean isLegalMove(Move m, int player)
	{
		if (m.fromX<0 || m.fromY<0 || m.fromX>7 || m.fromY>7 || board[m.fromX][m.fromY]!=m.piece+pieceCodeBias[player])
			{ illegalMoveReason = 1; return false; } 

		if (m.toX<0 || m.toY<0 || m.toX>7 || m.toY>7)
			{ illegalMoveReason = 2; return false; }

		if (board[m.toX][m.toY]!=EMPTY && pieceBelongsToPlayer(board[m.toX][m.toY],player))
			{ illegalMoveReason = 3; return false; }

		int pieceType = m.piece;
		
		
		if (pieceType==Chessboard.KNIGHT)
		{
			boolean legal=false;
			for (int k=0; k<8; k++) if (m.toX==m.fromX+Chessboard.knightOffsetX[k] &&
				m.toY==m.fromY+Chessboard.knightOffsetY[k]) legal = true;
				
			if (!legal) { illegalMoveReason = 4; return false; }
		}
		
		if (pieceType==Chessboard.ROOK)
		{
			if (m.toX!=m.fromX && m.toY!=m.fromY) { illegalMoveReason = 4; return false; }
		}
		
		if (pieceType==Chessboard.QUEEN)
		{
			if (m.toX!=m.fromX && m.toY!=m.fromY)
			{
				int dx = m.toX - m.fromX;
				int dy = m.toY - m.fromY;
				if (dx!=dy && dx!=-dy) { illegalMoveReason = 4; return false; }
			}
		}
		
		if (pieceType==Chessboard.BISHOP)
		{
			int dx = m.toX - m.fromX;
			int dy = m.toY - m.fromY;
			if (dx!=dy && dx!=-dy) { illegalMoveReason = 4; return false; }
		}
		
		if (pieceType==Chessboard.PAWN)
		{
			int dx = m.toX - m.fromX;
			int dy = m.toY - m.fromY;
			int offset = (player==WHITE? 1 : -1);
			if (dx!=-1 && dx!=0 && dx!=1) { illegalMoveReason = 4; return false; }
			if (dy!=offset && dy!=(2*offset)) { illegalMoveReason = 4; return false; }
			if (dy==(2*offset) && dx!=0) { illegalMoveReason = 4; return false; }
		}
		
		if (pieceType==Chessboard.KING)
		{
			boolean legal=false;
			for (int k=0; k<8; k++) if (m.toX==m.fromX+Chessboard.queenOffsetX[k] &&
				m.toY==m.fromY+Chessboard.queenOffsetY[k]) legal = true;
				
			if (!legal && canCastleLeft(player) && m.toX==m.fromX-2) legal=true;
			if (!legal && canCastleRight(player) && m.toX==m.fromX+2) legal=true;
	
			for (int k=0; k<8; k++)
			{
				//check for the presence of the opponent's king.
				int a = m.toX+Chessboard.queenOffsetX[k];
				int b = m.toY+Chessboard.queenOffsetY[k];
				
				if (a>=0 && b>=0 && a<8 && b<8 && !pieceBelongsToPlayer(board[a][b],player) &&
					umpire2ChessboardPieceCode(board[a][b])==Chessboard.KING) legal = false;
			}	
			
			if (!legal) { illegalMoveReason = 5; return false; }
		}
		
		if (pieceType==Chessboard.PAWN)
		{
			int offset = (player==WHITE? 1 : -1);
			int firstRank = (player==WHITE? 1 : 6);
			if (m.toX==m.fromX)
			{
				if (m.fromY+offset==m.toY || (m.fromY==firstRank && m.fromY+2*offset==m.toY))
				{
					if (board[m.toX][m.toY]!=EMPTY) { illegalMoveReason = 10; return false; }
				} else { illegalMoveReason = 4; return false; }
			} else
			{
				//en passant capture
				if ((m.toX==m.fromX-1 || m.toX==m.fromX+1) && m.fromY+offset==m.toY && board[m.toX][m.toY]==EMPTY)
				{
					if (m.toY!=(player==WHITE? 5 : 2)) { illegalMoveReason = 10; return false; }
					if (m.toX!=passant[1-player]) { illegalMoveReason = 10; return false; }
				}
			}
		}
		
		if (pieceType!=Chessboard.KNIGHT && pieceType!=Chessboard.KING)
		{
			int offX, offY;
			if (m.toX>m.fromX) offX=1; else if (m.toX==m.fromX) offX=0; else offX=-1;
			if (m.toY>m.fromY) offY=1; else if (m.toY==m.fromY) offY=0; else offY=-1;
			int x,y;
			for (x=m.fromX+offX, y=m.fromY+offY; x!=m.toX || y!=m.toY; x+=offX, y+=offY)
			{
				//System.out.println("Checking "+x+" "+y+" found "+board[x][y]);
				if (x<0 || y<0 || x>7 || y>7) { illegalMoveReason = 10; return false; }
				if (board[x][y]!=EMPTY) 
				{ 
					if (pieceBelongsToPlayer(board[x][y],player)) illegalMoveReason = 4;
					else illegalMoveReason = 10; return false; 
				}
			}
		}
		
		
		if (isKingThreatened(m,player)) { illegalMoveReason = 11; return false; };
		
		
		return true;
	}
	
	public boolean isPseudolegalMove(Move m, int player)
	{
		if (m.fromX<0 || m.fromY<0 || m.fromX>7 || m.fromY>7 || board[m.fromX][m.fromY]!=m.piece+pieceCodeBias[player])
			{ illegalMoveReason = 1; return false; } 

		if (m.toX<0 || m.toY<0 || m.toX>7 || m.toY>7)
			{ illegalMoveReason = 2; return false; }

		if (board[m.toX][m.toY]!=EMPTY && pieceBelongsToPlayer(board[m.toX][m.toY],player))
			{ illegalMoveReason = 3; return false; }

		int pieceType = m.piece;
		
		
		if (pieceType==Chessboard.KNIGHT)
		{
			boolean legal=false;
			for (int k=0; k<8; k++) if (m.toX==m.fromX+Chessboard.knightOffsetX[k] &&
				m.toY==m.fromY+Chessboard.knightOffsetY[k]) legal = true;
				
			if (!legal) { illegalMoveReason = 4; return false; }
		}
		
		if (pieceType==Chessboard.ROOK)
		{
			if (m.toX!=m.fromX && m.toY!=m.fromY) { illegalMoveReason = 4; return false; }
		}
		
		if (pieceType==Chessboard.QUEEN)
		{
			if (m.toX!=m.fromX && m.toY!=m.fromY)
			{
				int dx = m.toX - m.fromX;
				int dy = m.toY - m.fromY;
				if (dx!=dy && dx!=-dy) { illegalMoveReason = 4; return false; }
			}
		}
		
		if (pieceType==Chessboard.BISHOP)
		{
			int dx = m.toX - m.fromX;
			int dy = m.toY - m.fromY;
			if (dx!=dy && dx!=-dy) { illegalMoveReason = 4; return false; }
		}
		
		if (pieceType==Chessboard.PAWN)
		{
			int dx = m.toX - m.fromX;
			int dy = m.toY - m.fromY;
			int offset = (player==WHITE? 1 : -1);
			if (dx!=-1 && dx!=0 && dx!=1) { illegalMoveReason = 4; return false; }
			if (dy!=offset && dy!=(2*offset)) { illegalMoveReason = 4; return false; }
			if (dy==(2*offset) && dx!=0) { illegalMoveReason = 4; return false; }
		}
		
		if (pieceType==Chessboard.KING)
		{
			boolean legal=false;
			for (int k=0; k<8; k++) if (m.toX==m.fromX+Chessboard.queenOffsetX[k] &&
				m.toY==m.fromY+Chessboard.queenOffsetY[k]) legal = true;
				
			if (!legal && canCastleLeft(player) && m.toX==m.fromX-2) legal=true;
			if (!legal && canCastleRight(player) && m.toX==m.fromX+2) legal=true;
			
			if (!legal) { illegalMoveReason = 5; return false; }
		}
		
		if (pieceType==Chessboard.PAWN)
		{
			int offset = (player==WHITE? 1 : -1);
			int firstRank = (player==WHITE? 1 : 6);
			if (m.toX==m.fromX)
			{
				if (m.fromY+offset==m.toY || (m.fromY==firstRank && m.fromY+2*offset==m.toY))
				{
					if (board[m.toX][m.toY]!=EMPTY) { illegalMoveReason = 10; return false; }
				} else { illegalMoveReason = 4; return false; }
			} else
			{
				//en passant capture
				if ((m.toX==m.fromX-1 || m.toX==m.fromX+1) && m.fromY+offset==m.toY && board[m.toX][m.toY]==EMPTY)
				{
					if (m.toY!=(player==WHITE? 5 : 2)) { illegalMoveReason = 10; return false; }
					if (m.toX!=passant[1-player]) { illegalMoveReason = 10; return false; }
				}
			}
		}
		
		if (pieceType!=Chessboard.KNIGHT && pieceType!=Chessboard.KING)
		{
			int offX, offY;
			if (m.toX>m.fromX) offX=1; else if (m.toX==m.fromX) offX=0; else offX=-1;
			if (m.toY>m.fromY) offY=1; else if (m.toY==m.fromY) offY=0; else offY=-1;
			int x,y;
			for (x=m.fromX+offX, y=m.fromY+offY; x!=m.toX || y!=m.toY; x+=offX, y+=offY)
			{
				//System.out.println("Checking "+x+" "+y+" found "+board[x][y]);
				if (x<0 || y<0 || x>7 || y>7) { illegalMoveReason = 10; return false; }
				if (board[x][y]!=EMPTY) 
				{ 
					if (pieceBelongsToPlayer(board[x][y],player)) illegalMoveReason = 4;
					//else illegalMoveReason = 10; return false; 
				}
			}
		}
		
		
		return true;
	}
	
	
	public String boardState()
	{ 
		String s = "";
		
		for (int j=7; j>=0; j--)
		{
			for (int k=0; k<8; k++)
			{
				switch (board[k][j])
				{
					case EMPTY: if ((k+j)%2==0) s+="*"; else s+=" "; break;
					case WP: s+="P"; break;
					case WN: s+="N"; break;
					case WB: s+="B"; break;
					case WR: s+="R"; break;
					case WQ: s+="Q"; break;
					case WK: s+="K"; break;
					case BP: s+="p"; break;
					case BN: s+="n"; break;
					case BB: s+="b"; break;
					case BR: s+="r"; break;
					case BQ: s+="q"; break;
					case BK: s+="k"; break;
				}
			}
			s+="\n";
		}
		s+="\n";
		return s;
	}
	
	
	public static void main(String args[])
	{
		/*Darkboard ai = new Darkboard(true);
		//Darkboard ai2 = new Darkboard(o2,ws2);
		RandomMovingPlayer ai2 = new RandomMovingPlayer(false);
		
		LocalUmpire lu = new LocalUmpire(ai,ai2);
		
		lu.arbitrate();*/
	}
	
	/**
	 * Returns the material balance with the usual piece values, positive meaning White
	 * has the advantage.
	 * @return
	 */
	public int getMaterialDelta()
	{
		int result = 0;
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				switch (board[k][j])
				{
					case WP: result += 1; break;
					case WN: result += 3; break;
					case WB: result += 3; break;
					case WR: result += 5; break;
					case WQ: result += 9; break;
					case BP: result -= 1; break;
					case BN: result -= 3; break;
					case BB: result -= 3; break;
					case BR: result -= 5; break;
					case BQ: result -= 9; break;
				}
			}
			
		return result;
	}
	

	/**
	 * @return
	 */
	public int getGameDuration() {
		return gameDuration;
	}

	/**
	 * @param i
	 */
	public void setGameDuration(int i) {
		gameDuration = i;
	}

	/**
	 * @return
	 */
	public int getGameOutcome() {
		return gameOutcome;
	}

	/**
	 * @param i
	 */
	public void setGameOutcome(int i) {
		gameOutcome = i;
	}

	/**
	 * @return
	 */
	public Player getWinner() {
		return winner;
	}
	
	public String getPGNMoveString(Move m, boolean white, boolean omniscient)
	{		
		if (m==null) return "??"; //unknown move
		//System.out.println("Full move string is "+m.toString());
		if (m.toString().equals("O-O") || m.toString().equals("O-O-O")) return m.toString();
		
		int movx = m.fromX; int movy = m.fromY;
		boolean needsX = false;
		boolean needsY = false;
		//try to remove as much information as possible without making the notation ambiguous...
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				if (k!=movx || j!=movy && umpire2ChessboardPieceCode(getBoard(k,j))==m.piece)
				{
					m.fromX = (byte)k; m.fromY = (byte)j;
					//depending on whether we are omniscient (i.e. know where the opponent's pieces are)
					//the same move may or may not be ambiguous.
					boolean legal = isLegalMove(m,(white?WHITE:BLACK));
					if (legal || (!legal && !omniscient && (illegalMoveReason>=10)))
					{
						//ambiguity, need to disambiguate
						//if (verbose) System.out.println("Move "+movx+","+movy+" ambiguity with "+k+","+j);
						//if (verbose) System.out.println(this.boardState());
						if (movx!=k && movy==j) { needsX = true; needsY = false; } 
						if (movx==k && movy!=j) { needsY = true;  needsX = false; }
						if (movx!=k && movy!=j) { needsX = true; needsY = false; } 
					}
				}
			}
		m.showFromX = needsX;
		m.showFromY = needsY;
		m.showSeparator = false;
		m.fromX = (byte)movx; m.fromY = (byte)movy;
		if (isLegalMove(m,(white?WHITE:BLACK)))
		{
			doMove(m,(white?WHITE:BLACK));
			
			if (this.capture!=EMPTY) m.capture = true;
			 
			if (isKingThreatened(null,1-(white?WHITE:BLACK)))
			{
				if (this.check[0]!=Chessboard.NO_CHECK) m.check = true;
				//if (this.check[1]!=Chessboard.NO_CHECK) m.doublecheck = true;
				if (this.gameOutcome(1-(white?WHITE:BLACK))==OUTCOME_CHECKMATE) m.checkmate = true;
			}
		} else
		{
			//System.out.println
			if (m.piece==Chessboard.PAWN && m.fromX!=m.toX) m.capture = true;
		}
		if (m.piece==Chessboard.PAWN && m.capture) m.showFromX = true; //always show file when capturing.
		
		return m.toString();
	}
	
	public Move getMoveFromPGN(String s, boolean white)
	{
		//System.out.println("Move String is " + s);
		Move m = new Move();
		
		m.fromX = m.fromY = m.toX = m.toY = (byte)-1;
		
		boolean parsingStartingSquare = true;
		boolean parsingPromotionPiece = false;
		
		m.piece = Chessboard.PAWN;
		
		if (s.startsWith("?")) return m;
		
		//count the number of 'a'..'h' chars in the string to disambiguate...
		//EDIT: some implementations will use only the rank number instead of full square info in some cases!
		int counter = 0;
		int numberCounter = 0;
		for (int k=0; k<s.length(); k++)
		{
			char c = s.charAt(k);
			if (c>='a' && c<='h') counter++;
			if (c>='1' && c<='8') numberCounter++;
		}
		if (counter<2 && numberCounter<2) parsingStartingSquare = false; //only 1 file char in the string, can't be 2 squares!
		
		if (s.startsWith("O-O") && !s.startsWith("O-O-O"))
		{
			m.piece = Chessboard.KING;
			m.fromX = 4; m.toX = 6;
			m.fromY = m.toY = (byte)(white? 0 : 7);
		} else
		if (s.startsWith("O-O-O"))
		{
			m.piece = Chessboard.KING;
			m.fromX = 4; m.toX = 2;
			m.fromY = m.toY = (byte)(white? 0 : 7);		
		}
		else
		for (int k=0; k<s.length(); k++)
		{
			char c = s.charAt(k);
			switch (c)
			{
				case 'N':
					if (!parsingPromotionPiece) m.piece = Chessboard.KNIGHT;
					else m.promotionPiece = Chessboard.KNIGHT;
					break;
				case 'B':
					if (!parsingPromotionPiece) m.piece = Chessboard.BISHOP;
					else m.promotionPiece = Chessboard.BISHOP;
					break;
				case 'R':
					if (!parsingPromotionPiece) m.piece = Chessboard.ROOK;
					else m.promotionPiece = Chessboard.ROOK;
					break;
				case 'Q':
					if (!parsingPromotionPiece) m.piece = Chessboard.QUEEN;
					else m.promotionPiece = Chessboard.QUEEN;
					break;
				case 'K':
					if (!parsingPromotionPiece) m.piece = Chessboard.KING;
					else m.promotionPiece = Chessboard.KING;
					break;
				case '=':
					parsingPromotionPiece = true;
					break;
				case 'x':
					parsingStartingSquare = false;
					m.capture = true;
					break;
				case '+':
					if (!m.check) m.check = true; else m.doublecheck = true;
					break;
				case '1': case '2': case '3': case '4':
				case '5': case '6': case '7': case '8':
					if (parsingStartingSquare) 
					{
						m.fromY = (byte)(c - '1');
						parsingStartingSquare = false;
					} else m.toY = (byte)(c - '1');
					break;
					
				case 'a': case 'b': case 'c': case 'd':
				case 'e': case 'f': case 'g': case 'h':				
					if (parsingStartingSquare)
					{
						m.fromX = (byte)(c - 'a');
						char next = (s.length()>k+1? s.charAt(k+1) : ' ');
						if (next<'1' || next>'8') parsingStartingSquare = false;
					} else m.toX = (byte)(c - 'a');
					break;
				
			}
		}
		
		if (m.fromX==-1 || m.fromY==-1) fillStartingSquare(m,white);
		
		//now execute the move to update the board status...
		doMove(m,(white?WHITE:BLACK));
		tries = legalMoveNumber(1-(white?WHITE:BLACK),true);
		//System.out.println(this.boardState());
		
		return m;
	}
	
	private void fillStartingSquare(Move m,boolean white)
	{
		//in case the NAT did not specify the starting square (or only specified the file letter)
		//we use the board's current status to disambiguate...
		int knownFile = m.fromX;
		int knownRank = m.fromY;
		
		if (m.toX < 0 || m.toY < 0)
		{
			// System.out.println("MOVE FORMAT ERROR");
			return;
		}
		
		//now we run through all the squares looking for The One (TM).
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				int piece = board[k][j];
				boolean canUse = true;
				if (knownFile!=-1 && knownFile!=k) canUse = false;
				if (knownRank!=-1 && knownRank!=j) canUse = false;
				//if we know the right file, we can discard everything else.
				//depending on which piece was used, we can or cannot use this square...
				if (canUse && !pieceBelongsToPlayer(piece,(white?WHITE:BLACK))) canUse = false; //wrong owner
				if (canUse && umpire2ChessboardPieceCode(piece)!=m.piece) canUse = false; //wrong piece
				if (canUse) //The piece is compatible with the move type. Check whether the move is legal!
				{
					m.fromX = (byte)k; m.fromY = (byte)j;
					//System.out.println("Checking validity of " + m);
					canUse = isLegalMove(m,(white?WHITE:BLACK));
					if (canUse) return; //only one move can fit all criteria.
				}
			}
	}


	public int squaresOfType(int code)
	{
		int result=0;
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++) if (board[k][j]==code) result++;
			
		return result;
	}
	
	public int[] getPieceLocation(int code)
	{
		int[] r = new int[2];
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
				if (board[k][j]==code)
				{
					r[0] = k;
					r[1] = j;
					return r;
				}
				
		return null;
	}
	
	public int getPieceNumber(int white)
	{
		int n = 0;
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				int c = board[k][j];
				if (c==EMPTY) continue;
				if (pieceBelongsToPlayer(c,white)) n++;
			}
			
		return n;
	}
	
	public boolean occupied(int x, int y)
	{
		return (board[x][y]!=EMPTY);
	}
	
	public boolean occupied(int x, int y, int who)
	{
		int c = board[x][y];
		return (c!=EMPTY && pieceBelongsToPlayer(c,who));
	}
	
	public Metaposition exportChessboard(int player)
	{
		Metaposition sc = new Metaposition(player==WHITE? this.getP1(): this.getP2());
		if (sc.getOwner()==null) sc.setOwner(new Player());
		sc.pawnsLeft = sc.piecesLeft = 0;
		for (byte k=0; k<8; k++) 
		{
			sc.setMinPawns(k,(byte)0); sc.setMaxPawns(k,(byte)0);
		}
		
		sc.setWhite(player==WHITE);
		
		if (canCastleLeft(player) && canCastleRight(player)) sc.setCastleStatus((byte)0);
		if (canCastleLeft(player) && !canCastleRight(player)) sc.setCastleStatus((byte)1);
		if (!canCastleLeft(player) && canCastleRight(player)) sc.setCastleStatus((byte)2);
		if (!canCastleLeft(player) && !canCastleRight(player)) sc.setCastleStatus((byte)3);
		
		for (byte k=0; k<8; k++)
			for (byte j=0; j<8; j++) sc.setUnknown(k,j);
			
		for (byte k=0; k<8; k++)
			for (byte j=0; j<8; j++)
			{
				int b = board[k][j];
				if (b!=EMPTY && pieceBelongsToPlayer(b,player))
				{
					sc.setFriendlyPiece(k,j,umpire2ChessboardPieceCode(b));
				}
				if (b!=EMPTY && !pieceBelongsToPlayer(b,player))
				{
					if (umpire2ChessboardPieceCode(b)==Chessboard.PAWN)
					{
						sc.pawnsLeft++;
						sc.setMinPawns(k,(byte)(sc.getMinPawns(k)+1));
						sc.setMaxPawns(k,(byte)(sc.getMaxPawns(k)+1));
					} else if (umpire2ChessboardPieceCode(b)!=Chessboard.KING) sc.piecesLeft++;
				}
			}
			
		sc.computeProtectionMatrix(true);
		
		for (byte k=0; k<8; k++)
			for (byte j=0; j<8; j++)
			{
				if (sc.getFriendlyPiece(k,j)==Chessboard.EMPTY)
				{
					if (sc.pawnsLeft==0)
					{
						sc.setPieceImpossible(k,j,Chessboard.PAWN);
					}
					if (sc.piecesLeft==0)
					{
						sc.setPieceImpossible(k,j,Chessboard.KNIGHT);
						sc.setPieceImpossible(k,j,Chessboard.BISHOP);
						sc.setPieceImpossible(k,j,Chessboard.ROOK);
						sc.setPieceImpossible(k,j,Chessboard.QUEEN);
					}
					if (sc.owner.globals.protectionMatrix[k][j]>0)
						sc.setPieceImpossible(k,j,Chessboard.KING);
				}
			}
		
		return sc;
	}
	
	public void insertPiece(int p, int player, boolean protect)
	{
		Random r = new Random();
		for (int k=0; k<100000; k++)
		{
			int x = r.nextInt(8); int y = r.nextInt(8);
			if (board[x][y]!=EMPTY) continue;
			
			Metaposition m = this.exportChessboard(player);
			m.computeProtectionMatrix(true);
			if (protect && m.owner.globals.protectionMatrix[x][y]<1) continue;
			if (!protect && m.owner.globals.protectionMatrix[x][y]>0) continue;
			
			if (p==Chessboard.KING)
			{
				Metaposition m2 = this.exportChessboard(1-player);
				m2.computeProtectionMatrix(true);
				if (m.owner.globals.protectionMatrix[x][y]>0) continue;
			}
			
			board[x][y] = p + pieceCodeBias[player];
			return;
		}
	}
	
	public void emptyBoard()
	{
		for (int x=0; x<8; x++) for (int y=0; y<8; y++)
			board[x][y] = EMPTY;
	}
	
	public Metaposition exportPerfectInformationChessboard(int player)
	{
		Metaposition sc = new Metaposition(player==WHITE? this.getP1(): this.getP2());
		if (sc.getOwner()==null) sc.setOwner(new Player());
		sc.setWhite(player==WHITE);
		sc.pawnsLeft = 0;
		sc.piecesLeft = 0;
		
		if (canCastleLeft(player) && canCastleRight(player)) sc.setCastleStatus((byte)0);
		if (canCastleLeft(player) && !canCastleRight(player)) sc.setCastleStatus((byte)1);
		if (!canCastleLeft(player) && canCastleRight(player)) sc.setCastleStatus((byte)2);
		if (!canCastleLeft(player) && !canCastleRight(player)) sc.setCastleStatus((byte)3);

		for (byte k=0; k<8; k++)
			for (byte j=0; j<8; j++) sc.setUnknown(k,j);
			
		for (byte k=0; k<8; k++)
			for (byte j=0; j<8; j++)
			{
				int b = board[k][j];
				if (b!=EMPTY)
				{
				
					if (pieceBelongsToPlayer(b,player))
					{
						sc.setFriendlyPiece(k,j,umpire2ChessboardPieceCode(b));
					}
					else
					{
						sc.setEmpty(k,j);
						int code = umpire2ChessboardPieceCode(b);
						sc.setPiecePossible(k,j,umpire2ChessboardPieceCode(b));
						sc.setPieceImpossible(k,j,Chessboard.EMPTY);
						if (code==Chessboard.PAWN) { sc.pawnsLeft++; 
							sc.setMaxPawns((byte)k, (byte)(sc.getMaxPawns((byte)k)+1));
							sc.setMinPawns((byte)k, (byte)(sc.getMinPawns((byte)k)+1));
							}
						if (code==Chessboard.KNIGHT || code==Chessboard.BISHOP || code==Chessboard.ROOK ||
							code==Chessboard.QUEEN) sc.piecesLeft++;
					}
				} else sc.setEmpty(k,j);
			}
		return sc;
	}
	
	/**
	 * 
	 * @param player
	 * @return a string like "KQRRPPP" representing the player's material.
	 */
	public String exportPlayerMaterial(int player)
	{
		int king = 0;
		int pawn = 0;
		int knight = 0;
		int bishop = 0;
		int rook = 0;
		int queen = 0;
		
		for (byte k=0; k<8; k++)
			for (byte j=0; j<8; j++)
			{
				int b = board[k][j];
				if (b!=EMPTY && pieceBelongsToPlayer(b,player))
				{
					switch (umpire2ChessboardPieceCode(b))
					{
						case Chessboard.KING: king++; break;
						case Chessboard.PAWN: pawn++; break;
						case Chessboard.KNIGHT: knight++; break;
						case Chessboard.BISHOP: bishop++; break;
						case Chessboard.ROOK: rook++; break;
						case Chessboard.QUEEN: queen++; break;
					}
				}
			}
			
		String s = "";
		
		for (int k=0; k<king; k++) s+="K";
		for (int k=0; k<queen; k++) s+="Q";
		for (int k=0; k<rook; k++) s+="R";
		for (int k=0; k<bishop; k++) s+="B";
		for (int k=0; k<knight; k++) s+="N";
		for (int k=0; k<pawn; k++) s+="P";
		return s;
		
	}
	
	public String exportPlayerMaterials(int whoFirst)
	{
		String s1 = exportPlayerMaterial(whoFirst);
		String s2 = exportPlayerMaterial(1-whoFirst);
		return s1+" vs. "+s2;
	}
	
	public int getMaterial(int player)
	{
		int result=0;
		
		for (byte k=0; k<8; k++)
			for (byte j=0; j<8; j++)
			{
				int b = board[k][j];
				if (b!=EMPTY && pieceBelongsToPlayer(b,player))
				{
					switch (umpire2ChessboardPieceCode(b))
					{
						case Chessboard.KING: break;
						case Chessboard.PAWN: result++; break;
						case Chessboard.KNIGHT: result+=3; break;
						case Chessboard.BISHOP: result+=3; break;
						case Chessboard.ROOK: result+=5; break;
						case Chessboard.QUEEN: result+=9; break;
					}
				}
			}
			
		return result;
	}
	
	public int getBoard(int x, int y) { return board[x][y]; }
	public void setBoard(int x, int y, int what) { board[x][y]=what; fireChessboardChangeNotification(); }
	
	/**
	 * Creates a board layout from a FEN notation.
	 * @param fen
	 * @return
	 */
	public static int[][] boardLayoutFromFEN(String fen)
	{
		int result[][] = new int[8][8];
		for (int k=0; k<8; k++) for (int j=0; j<8; j++) result[k][j] = LocalUmpire.EMPTY;
		
		byte file,rank;
		file = 0;
		rank = 7;

		for (int k=0; k<fen.length(); k++)
		{
			char c = fen.charAt(k);
			char piece=0;
			switch (c)
			{
				case 'K': piece = LocalUmpire.WK; break;
				case 'P': piece = LocalUmpire.WP; break;
				case 'Q': piece = LocalUmpire.WQ; break;
				case 'R': piece = LocalUmpire.WR; break;
				case 'B': piece = LocalUmpire.WB; break;
				case 'N': piece = LocalUmpire.WN; break;				
				case 'k': piece = LocalUmpire.BK; break;
				case 'p': piece = LocalUmpire.BP; break;
				case 'q': piece = LocalUmpire.BQ; break;
				case 'r': piece = LocalUmpire.BR; break;
				case 'b': piece = LocalUmpire.BB; break;
				case 'n': piece = LocalUmpire.BN; break;
				case '1': file+=1; break;
				case '2': file+=2; break;
				case '3': file+=3; break;
				case '4': file+=4; break;
				case '5': file+=5; break;
				case '6': file+=6; break;
				case '7': file+=7; break;
				case '8': file+=8; break;
				
				case '/':
				if (rank==0)
				{
					// System.out.println("FEN error, too many ranks.");
					return null;
				} else
				if (file!=8)
				{
					// System.out.println("FEN error, incorrect rank width.");
					return null;
				}
				else
				{
					file=0; rank--;
				}
				break;
			}
			
			if (piece!=0)
			{
				result[file++][rank] = piece;
				piece = 0;
			}
			
		}
		
		if (rank!=0 || file!=8)
		{
			// System.out.println("FEN error, unexpected end of string.");
			return null;
		}

		return result;
	}
	
	public void addListener(ChessboardStateListener l) { listeners.add(l);}
	public void removeListener(ChessboardStateListener l) { listeners.remove(l); }
	public void fireChessboardChangeNotification()
	{
		for (int k=0; k<listeners.size(); k++)
			((ChessboardStateListener)listeners.get(k)).chessboardStateChanged();
	}
	
	public String toString()
	{
		return toFen();
	}
	
	public String toFen()
	{
		String fen = "";
		
		for (int rank=7; rank>=0; rank--)
		{
			int empty = 0;
			for (int file=0; file<8; file++)
			{
				if (board[file][rank]!=LocalUmpire.EMPTY && empty>0)
				{
					fen+=empty;
					empty = 0;
				}
				switch (board[file][rank])
				{
				case LocalUmpire.WK: fen+="K"; break;
				case LocalUmpire.WQ: fen+="Q"; break;
				case LocalUmpire.WR: fen+="R"; break;
				case LocalUmpire.WB: fen+="B"; break;
				case LocalUmpire.WN: fen+="N"; break;
				case LocalUmpire.WP: fen+="P"; break;
				case LocalUmpire.BK: fen+="k"; break;
				case LocalUmpire.BQ: fen+="q"; break;
				case LocalUmpire.BR: fen+="r"; break;
				case LocalUmpire.BB: fen+="b"; break;
				case LocalUmpire.BN: fen+="n"; break;
				case LocalUmpire.BP: fen+="p"; break;
				case LocalUmpire.EMPTY: empty++; break;
				}
			}
			if (empty>0) fen+=empty;
			if (rank!=0) fen+="/";
		}
		
		fen+=" ";
		if (turn==WHITE) fen+="w "; else fen+="b ";
		
		String castle = "";
		if ((!kingMoved[WHITE] && !kingCastled[WHITE] && !rightRookMoved[WHITE])) castle+="K";
		if ((!kingMoved[WHITE] && !kingCastled[WHITE] && !leftRookMoved[WHITE])) castle+="Q";
		if ((!kingMoved[BLACK] && !kingCastled[BLACK] && !rightRookMoved[BLACK])) castle+="k";
		if ((!kingMoved[BLACK] && !kingCastled[BLACK] && !leftRookMoved[BLACK])) castle+="q";
		
		if (castle.equals("")) castle = "-";
		fen+=castle;
		fen+=" ";
		
		int pa = passant[1-turn];
		if (pa==-1) fen+="-";
		else fen+= Move.squareString(pa, (1-turn==WHITE? 2 : 5));
		fen+=" ";
		
		fen+=fiftyMoves;
		fen+=" ";
		
		fen+=moveCount;
		
		return fen;
		
	}
	
	public boolean areMetapositionsCompatibleWithUmpireMessage(Metaposition w, Metaposition b, boolean white, int cx, int cy, /*int ctype,*/ int c1, int c2, int pawnTries) throws umpire.local.IncompatibleMetapositionException
	{
		if (!w.isWhite())
		{
			Metaposition mp = w; w = b; b = mp;
		}
		
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				board[k][j] = EMPTY;
				int pc = w.getFriendlyPiece(k, j);
				switch (pc)
				{
				case Chessboard.PAWN: board[k][j] = WP; break;
				case Chessboard.KNIGHT: board[k][j] = WN; break;
				case Chessboard.BISHOP: board[k][j] = WB; break;
				case Chessboard.ROOK: board[k][j] = WR; break;
				case Chessboard.QUEEN: board[k][j] = WQ; break;
				case Chessboard.KING: board[k][j] = WK; kingX[0] = k; kingY[0] = j; break;
				}
				int pc2 = pc;
				pc = b.getFriendlyPiece(k, j);
				boolean blackOnSquare = pc!=Chessboard.EMPTY;
				boolean whiteOnSquare = board[k][j]!=LocalUmpire.EMPTY;
				if (blackOnSquare && whiteOnSquare && (k!=cx || j!=cy)) throw new umpire.local.IncompatibleMetapositionException(w, b);
				if (cx!=-1 && k==cx && j==cy)
				{
					if (!blackOnSquare || !whiteOnSquare) throw new umpire.local.IncompatibleMetapositionException(w, b);
					if (!white) continue;
					
				}
				
				switch (pc)
				{
				case Chessboard.PAWN: board[k][j] = BP; break;
				case Chessboard.KNIGHT: board[k][j] = BN; break;
				case Chessboard.BISHOP: board[k][j] = BB; break;
				case Chessboard.ROOK: board[k][j] = BR; break;
				case Chessboard.QUEEN: board[k][j] = BQ; break;
				case Chessboard.KING: board[k][j] = BK; kingX[1] = k; kingY[1] = j; break;
				}
				
				
				
			}
		
		turn = (white? 0 : 1);
		int chk = Chessboard.NO_CHECK; int chk2 = Chessboard.NO_CHECK;
		
		if (isKingThreatened(null,1-turn)) return false; //opponent moved into check, impossible
		
		if (isKingThreatened(null,turn)) //is check compatible?
		{
			chk = this.check[0]; chk2 = this.check[1];
		}
		
		if (c1!=chk && c1!=chk2) return false;
		if (c2!=chk && c2!=chk2) return false;
		
		int tri = legalMoveNumber(1-turn,true);
		if (tri!=pawnTries) return false;
		
		return true;
	}
	
	public boolean areMetapositionsCompatibleWithIllegalMove(Metaposition w, Metaposition b, boolean white, Move m) throws umpire.local.IncompatibleMetapositionException
	{
		if (!w.isWhite())
		{
			Metaposition mp = w; w = b; b = mp;
		}
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				board[k][j] = EMPTY;
				int pc = w.getFriendlyPiece(k, j);
				switch (pc)
				{
				case Chessboard.PAWN: board[k][j] = WP; break;
				case Chessboard.KNIGHT: board[k][j] = WN; break;
				case Chessboard.BISHOP: board[k][j] = WB; break;
				case Chessboard.ROOK: board[k][j] = WR; break;
				case Chessboard.QUEEN: board[k][j] = WQ; break;
				case Chessboard.KING: board[k][j] = WK; kingX[0] = k; kingY[0] = j; break;
				}
				pc = b.getFriendlyPiece(k, j);
				if (pc!=Chessboard.EMPTY && board[k][j]!=LocalUmpire.EMPTY) throw new umpire.local.IncompatibleMetapositionException(w,b);
				switch (pc)
				{
				case Chessboard.PAWN: board[k][j] = BP; break;
				case Chessboard.KNIGHT: board[k][j] = BN; break;
				case Chessboard.BISHOP: board[k][j] = BB; break;
				case Chessboard.ROOK: board[k][j] = BR; break;
				case Chessboard.QUEEN: board[k][j] = BQ; break;
				case Chessboard.KING: board[k][j] = BK; kingX[1] = k; kingY[1] = j; break;
				}
				
				
				
			}
		
		turn = (white? 0 : 1);
		
		return (!isLegalMove(m, turn));
		
	}
	
	public void merge(Metaposition w, Metaposition b)
	{
		
		turn = (w.isWhite()? 0 : 1);
		
		if (!w.isWhite())
		{
			Metaposition mp = w; w = b; b = mp;
		}
			
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				board[k][j] = EMPTY;
				int pc = w.getFriendlyPiece(k, j);
				switch (pc)
				{
				case Chessboard.PAWN: board[k][j] = WP; break;
				case Chessboard.KNIGHT: board[k][j] = WN; break;
				case Chessboard.BISHOP: board[k][j] = WB; break;
				case Chessboard.ROOK: board[k][j] = WR; break;
				case Chessboard.QUEEN: board[k][j] = WQ; break;
				case Chessboard.KING: board[k][j] = WK; kingX[0] = k; kingY[0] = j; break;
				}
				pc = b.getFriendlyPiece(k, j);
				
				switch (pc)
				{
				case Chessboard.PAWN: board[k][j] = BP; break;
				case Chessboard.KNIGHT: board[k][j] = BN; break;
				case Chessboard.BISHOP: board[k][j] = BB; break;
				case Chessboard.ROOK: board[k][j] = BR; break;
				case Chessboard.QUEEN: board[k][j] = BQ; break;
				case Chessboard.KING: board[k][j] = BK; kingX[1] = k; kingY[1] = j; break;
				}	
			}
		
		
	}
	
	public int[][] getProtection(Metaposition w, Metaposition b, boolean white)
	{
		int out[][] = new int[8][8];
		if (!w.isWhite())
		{
			Metaposition mp = w; w = b; b = mp;
		}
		
		Metaposition one = (white? w: b);
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				board[k][j] = EMPTY;
				int pc = w.getFriendlyPiece(k, j);
				switch (pc)
				{
				case Chessboard.PAWN: board[k][j] = WP; break;
				case Chessboard.KNIGHT: board[k][j] = WN; break;
				case Chessboard.BISHOP: board[k][j] = WB; break;
				case Chessboard.ROOK: board[k][j] = WR; break;
				case Chessboard.QUEEN: board[k][j] = WQ; break;
				case Chessboard.KING: board[k][j] = WK; kingX[0] = k; kingY[0] = j; break;
				}
				pc = b.getFriendlyPiece(k, j);
				switch (pc)
				{
				case Chessboard.PAWN: board[k][j] = BP; break;
				case Chessboard.KNIGHT: board[k][j] = BN; break;
				case Chessboard.BISHOP: board[k][j] = BB; break;
				case Chessboard.ROOK: board[k][j] = BR; break;
				case Chessboard.QUEEN: board[k][j] = BQ; break;
				case Chessboard.KING: board[k][j] = BK; kingX[1] = k; kingY[1] = j; break;
				}
			}
		Metaposition m = exportPerfectInformationChessboard(white? 0: 1);
		m.computeProtectionMatrix(true);
		return m.owner.globals.protectionMatrix;
	}
}
