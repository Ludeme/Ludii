/*
 * Created on 18-mar-06
 *
 */
package umpire.local;

import java.util.Hashtable;

import ai.player.Player;
import core.Chessboard;
import core.Move;
import pgn.ExtendedPGNGame;
import umpire.local.FENData.MalformedFENException;

/**
 * @author Nikola Novarlic
 * This umpire is designed to referee a game one move at a time;
 * it is a passive entity rather than an active arbiter like its parent class.
 */
public class StepwiseLocalUmpire extends LocalUmpire {
	
	//memorize last umpire messages...
	public int c1, c2, pt, cx, cy, turn;
	
	public StepwiseLocalUmpire(Player p1, Player p2)
	{
		super(p1,p2);
		transcript = new ExtendedPGNGame();
	}

	public void resetBoard()
	{
		super.resetBoard();
		c1 = c2 = Chessboard.NO_CHECK;
		cx = cy = -1;
		pt = 0;
		turn = LocalUmpire.WHITE;
	}
	

	public void stepwiseInit(FENData fen, ExtendedPGNGame partial)
	{
		Hashtable d1, d2; //parameters to send both players
		d1 = new Hashtable(); d2 = new Hashtable();
		
		d1.put("isWhite",new Boolean(true));
		d2.put("isWhite",new Boolean(false));
		
		if (getP1()!=null && getP1().playerName!=null) d1.put("white", getP1().playerName);
		if (getP2()!=null && getP2().playerName!=null) d1.put("black", getP2().playerName);
		
		d1.put("where", "local");
		d2.put("where", "local");
		
		if (fen==null) resetBoard();
		if (getP1()!=null) getP1().startMatch(d1);
		if (getP2()!=null) getP2().startMatch(d2);
		//fireChessboardChangeNotification();
		
		if (fen!=null)
		{
			//game is not starting from default position
			for (int k=0; k<8; k++) for (int j=0; j<8; j++)
			{
				board[k][j] = fen.getBoard()[k][j];
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
			//getP1().simplifiedBoard = this.exportChessboard(WHITE);
			//getP2().simplifiedBoard = this.exportChessboard(BLACK);
			
			this.fiftyMoves = fen.getHalfMove();
			this.moveCount = fen.getFullMove();
			this.turn = (fen.isWhiteTurn()? WHITE : BLACK);
			this.tries = 0;
			transcript.addTag("FEN", fen.getFEN());
			if (this.turn==BLACK) 
			{
				transcript.addMove(true, null, -1, -1, Chessboard.NO_CAPTURE, Chessboard.NO_CHECK, Chessboard.NO_CHECK, 0);
				if (getP1()!=null) getP1().setHasTurn(false);
				if (getP2()!=null) getP2().setHasTurn(true);
			}
			
			if (getP1()!=null) getP1().startFromAlternatePosition(this.exportPerfectInformationChessboard(WHITE));
			if (getP2()!=null) getP2().startFromAlternatePosition(this.exportPerfectInformationChessboard(BLACK));
			
			fireChessboardChangeNotification();
			
			
		}
		
		
		if (partial!=null)
		{
			if (verbose) System.out.println("::: "+partial.getMoveNumber());
			for (int k=0; k<partial.getMoveNumber(); k++)
			{
				for (int t=0; t<2; t++)
				{
					boolean w = (t==0);
					ExtendedPGNGame.PGNMoveData data = partial.getMove(w, k);
					if (data==null) break;
					
					Move m = data.finalMove;
					if (m==null) break;
					if (verbose) System.out.println("UMPIRING "+m);
					if (!this.isLegalMove(m, t)) break; //stop if this move is not legal
					Player p = (w? getP1() : getP2());
					if (p!=null) 
					{
						p.emulateNextMove(m);
						stepwiseArbitrate(m);
					}
					
				}
				
			}
		}
		
		if (partial!=null) transcript=partial;
		if (getP1()!=null) { transcript.setWhite(getP1().getPlayerName()); if (getP1().simplifiedBoard!=null) getP1().simplifiedBoard.setOwner(getP1()); }
		if (getP2()!=null) { transcript.setBlack(getP2().getPlayerName()); if (getP2().simplifiedBoard!=null) getP2().simplifiedBoard.setOwner(getP2()); }
		//transcript.firePGNChange();
	}
	
	/**
	 * Shortcut for initializing a game from a PGN with or without a FEN
	 * @param g
	 */
	public void stepwiseInit(ExtendedPGNGame g) throws MalformedFENException
	{
		if (g==null)
		{
			stepwiseInit(null,null);
			return;
		}
		
		String fen = g.getTag("FEN");
		if (fen==null)
		{
			stepwiseInit(null,g);
			return;
		}
		
		stepwiseInit(new FENData(fen),g);
		
	}
	
	/**
	 * Init from a FEN and nothing else.
	 * @param fen
	 */
	public void stepwiseInit(String fen) throws MalformedFENException
	{
		if (fen==null)
		{
			stepwiseInit(null,null);
			return;
		}
		stepwiseInit(new FENData(fen),null);
	}
	
	public Player turn()
	{
		return (turn==WHITE? getP1() : getP2());
	}
	
	public synchronized String offerDraw(Player p)
	{
		Player opponent;
		if (gameOutcome!=NO_OUTCOME) return "";
		
		if (p==getP1()) opponent = getP2();
		else if (p==getP2()) opponent = getP1();
		else return "";
		
		if (opponent.shouldAcceptDraw())
		{
			gameOutcome = OUTCOME_AGREED_DRAW;
			winner = null;
			p.communicateOutcome(Player.PARAM_AGREED_DRAW);
			opponent.communicateOutcome(Player.PARAM_AGREED_DRAW);
			transcript.setResult("1/2-1/2");
			getP1().receiveAftermath(transcript);
			getP2().receiveAftermath(transcript);
			return "Mutual agreement draw.";
		} else p.communicateUmpireInfo(Player.INFO_DRAW_OFFER_REJECTED, 0);
		return "Draw offer was rejected.";
	}
	
	public synchronized void resign(Player p)
	{
		Player opponent;
		if (gameOutcome!=NO_OUTCOME) return;
		
		if (p==getP1()) opponent = getP2();
		else if (p==getP2()) opponent = getP1();
		else return;
		gameOutcome = OUTCOME_RESIGN;
		winner = null;
		p.communicateOutcome(Player.PARAM_RESIGN_DEFEAT);
		opponent.communicateOutcome(Player.PARAM_RESIGN_VICTORY);
		transcript.setResult(p.isWhite? "0-1" : "1-0");
		
		getP1().receiveAftermath(transcript);
		getP2().receiveAftermath(transcript);
			
	}
	
	public synchronized String stepwiseArbitrate(Move m)
	{
		if (gameOutcome!=NO_OUTCOME) return "";
			
		int playerNumber = turn;
		Player whoseTurn = (turn==WHITE? getP1() : getP2());
		Player opponent = (turn==WHITE? getP2() : getP1());
			
		//Move m = whoseTurn.getNextMove();
			
		if (m==null) return "";
		String umpireMsg = "";

		if (isLegalMove(m,playerNumber))
		{
			//legal move.
			if (turn==WHITE) moveCount++;
				
			//if (verbose) System.out.println("Move "+m+" by Player "+playerNumber+" is legal.");
			doMove(m,playerNumber);
			//if (verbose) System.out.println(boardState());


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
			tries = pawnTries;
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
			transcript.firePGNChange();
				
			int outcome = this.gameOutcome(1-playerNumber);
			gameOutcome = outcome; //copy it for later access
			
			if (outcome==OUTCOME_CHECKMATE)
			{
				winner = whoseTurn;
				whoseTurn.communicateOutcome(Player.PARAM_CHECKMATE_VICTORY);
				opponent.communicateOutcome(Player.PARAM_CHECKMATE_DEFEAT);
				transcript.setResult(playerNumber==WHITE? "1-0" : "0-1");
				if (verbose) System.out.println("Player "+playerNumber+" wins!");
				//return;
				String opponentColor = "White";
				if(playerNumber == WHITE) opponentColor = "Black";
				return (opponentColor+" checkmated");
			}
			if (outcome==OUTCOME_STALEMATE)
			{
				winner = null;
				whoseTurn.communicateOutcome(Player.PARAM_STALEMATE_DRAW);
				opponent.communicateOutcome(Player.PARAM_STALEMATE_DRAW);
				transcript.setResult("1/2-1/2");
				if (verbose) System.out.println("Stalemate!");
				//return;
				return ("Stalemate!");
			}
			if (outcome==OUTCOME_FIFTY_MOVES)
			{
				winner = null;
				whoseTurn.communicateOutcome(Player.PARAM_50_DRAW);
				opponent.communicateOutcome(Player.PARAM_50_DRAW);
				transcript.setResult("1/2-1/2");
				if (verbose) System.out.println("Fifty Moves Draw!");
				//return;
				return ("Fifty Moves Draw!");
			}
			if (outcome==OUTCOME_NO_MATERIAL)
			{
				winner = null;
				whoseTurn.communicateOutcome(Player.PARAM_NO_MATERIAL);
				opponent.communicateOutcome(Player.PARAM_NO_MATERIAL);
				transcript.setResult("1/2-1/2");
				if (verbose) System.out.println("Insufficient Material Draw!");
				return ("Insufficient Material Draw!");
			}

			umpireMsg += whoseTurn.communicateLegalMove(captureType,pawnTries,chk,chk2);
			umpireMsg += opponent.communicateUmpireMessage(cx,cy,pawnTries,chk,chk2, captureType);
			// umpireMsg += opponent.communicateUmpireMessage(captureType,cy,pawnTries,chk,chk2);
			this.fireChessboardChangeNotification();
			if (outcome!=NO_OUTCOME)
			{
				getP1().receiveAftermath(transcript);
				getP2().receiveAftermath(transcript);
				return "";
			}
			turn = 1-turn;
			
			/*if (opponent.simplifiedBoard!=null)
			{
				Vector<Metaposition> mv = MetapositionGenerator.generateMetaposition(whoseTurn, opponent.simplifiedBoard, 1, transcript);
				if (mv.size()>0)
				{
					Metaposition mp = mv.get(0);
					System.out.println(mp);
				}
			}*/
		} else
		{
			//illegal move.
			if (verbose) System.out.println("Move "+m+" by Player "+playerNumber+" is NOT legal (reason: "+
				illegalMoveReason+").");

			/*
			No need to show this as Umpire message
			umpireMsg = ("Move "+m+" by Player "+playerNumber+" is NOT legal (reason: "+
					illegalMoveReason+").");
			*/
			if (illegalMoveReason==ILLEGAL_BLOCKED || illegalMoveReason==ILLEGAL_KING_THREATENED ||
					illegalMoveReason==ILLEGAL_KING_VS_KING) 
			{
				boolean add = true;
				for (int ill=0; ill<illegalMoveVector.size(); ill++)
					if (illegalMoveVector.get(ill).equals(m)) add = false;
				if (add) illegalMoveVector.add(m);
			}
			whoseTurn.communicateIllegalMove(m);
			this.fireChessboardChangeNotification();
		}
		return umpireMsg;
	}

	public String getPGNMoveString(Move m, boolean white, boolean omniscient)
	{		
		boolean trivial = false;
		String result = "";
		
		if (m==null) return "??"; //unknown move
		//System.out.println("Full move string is "+m.toString());
		if (m.toString().equals("O-O") || m.toString().equals("O-O-O")) 
		{
			trivial = true;
			result = m.toString();
		}
		
		int movx = m.fromX; int movy = m.fromY;
		boolean needsX = false;
		boolean needsY = false;
		//try to remove as much information as possible without making the notation ambiguous...
		if (!trivial)
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
		if (/*omniscient && */isLegalMove(m,(white?WHITE:BLACK)))
		{
			stepwiseArbitrate(m);
			
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
		
		if (trivial) return result; else return m.toString();
	}
	
	
}
