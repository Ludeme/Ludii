package ai.player;

import core.Chessboard;
import core.Metaposition;
import core.Move;

/**
 * HumanPlayer is a player that depends on external (usually human) input for generating a move.
 * When asked for a move, it waits until notified that a move has been provided by a synchronized
 * method, usually called by a piece of the GUI.
 * @author Nikola Novarlic
 *
 */
public class HumanPlayer extends Player {
	
	Move nextMove = null;
	int setPromotionPiece = Chessboard.QUEEN;
	
	boolean hasTurn;
	
	public HumanPlayer(boolean white)
	{
		isWhite = white;
		hasTurn = white;
		
		playerName = "Human";
		
		simplifiedBoard = Metaposition.getChessboard(this);
		simplifiedBoard.setup(isWhite);
	}
	
	public void setHasTurn(boolean b) 
	{
		hasTurn = b;
	}
	
	public Move getNextMove()
	{
		//System.out.println("!MOVE!! ");
		while (nextMove==null)
		{
			try { wait(); } catch (Exception e) {}
			//try { Thread.sleep(100); } catch (Exception e) {}
		}
		//System.out.println("!KSKSN2!");
		Move m = nextMove;
		lastMove = m;
		nextMove = null;
		hasTurn = false;
		return m;
	}
	
	public synchronized void provideMove(Move m)
	{
		//System.out.println("!KSKSN! "+m.toString());
		if (!hasTurn) return;
		nextMove = m;
		if (m.piece==Chessboard.PAWN && (m.toY==0 || m.toY==7)) m.promotionPiece = (byte)setPromotionPiece;
		notifyAll();
	}

	public void emulateNextMove(Move m) {
		lastMove=m;
	} //we need this to bypass human selection...
	
	/**
	 * Tells the human player what it should promote pawns to. Generally set from a menu.
	 * @param what
	 */
	public void setPromotionPiece(int what)
	{
		setPromotionPiece = what;
	}
	
	public boolean isHuman()
	{
		return true;
	}
	
	/**
	 * Tells the player that their latest move was illegal.
	 * @param m
	 */
	public void communicateIllegalMove(Move m) { super.communicateIllegalMove(m); hasTurn = true;  }

	/**
	 * Tells the player that the opponent has moved and the following messages have been sent
	 * by the umpire.
	 * @param capX
	 * @param capY
	 * @param tries
	 * @param check
	 * @param check2
	 */
	public String communicateUmpireMessage(int capX, int capY, int tries, int check, int check2, int captureType)
	{
		hasTurn = true;
		return super.communicateUmpireMessage(capX, capY, tries, check, check2, captureType);
		//simplifiedBoard = Metaposition.evolveAfterOpponentMove(simplifiedBoard,capX,capY,check,check2,tries);
	}
	
	public String communicateLegalMove(int capture, int oppTries, int oppCheck, int oppCheck2)
	{
		return super.communicateLegalMove(capture, oppTries, oppCheck, oppCheck2);
		//simplifiedBoard = Metaposition.evolveAfterMove(simplifiedBoard,lastMove,
			//	capture,lastMove.toX,lastMove.toY,oppCheck,oppCheck2,oppTries);

	}
	
}
