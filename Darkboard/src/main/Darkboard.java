package main;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import ai.opening.OpeningTreeNode;
import database.OpeningBook;
import ai.opponent.OpponentProfile;
import ai.player.DeepDarkboard101;
import core.Chessboard;
import core.Move;
import umpire.Umpire;
import utils.KriegspielAgent;
import utils.PromotionPiece;

public class Darkboard extends Umpire implements KriegspielAgent{
	
	boolean localPlayerWhite;
	boolean currentTurnWhite;
	Move lastAttemptedMove;
	Vector failedMoves = new Vector();
	int promotionPiece;
	
	private static int NO_CAPTURE = 0;
	private static int CAPTURE_PAWN = 1;
	private static int CAPTURE_PIECE = 2;
	
	private static int NO_CHECK = 0;
	private static int CHECK_FILE = 1;
	private static int CHECK_RANK = 2;
	private static int CHECK_LONG_DIAGONAL = 3;
	private static int CHECK_SHORT_DIAGONAL = 4;
	private static int CHECK_KNIGHT = 5;
	
	 public OpponentProfile load() {
		 try {
				ClassLoader classloader = this.getClass().getClassLoader(); //Thread.currentThread().getContextClassLoader();
				ObjectInputStream ois = new ObjectInputStream (classloader.getResourceAsStream("rjay"));
				OpponentProfile ob = new OpponentProfile();
				try {
					ob.name = (String) ois.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				try {
					ob.where = (String) ois.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				try {
					ob.whitesize = ((Integer)ois.readObject()).intValue();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				try {
					ob.blacksize = ((Integer)ois.readObject()).intValue();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				try {
					ob.whiteBook = (OpeningBook) ois.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				try {
					ob.blackBook = (OpeningBook) ois.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				try {
					ob.piecePresenceWhite = (float[][][][]) ois.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				try {
					ob.piecePresenceBlack = (float[][][][]) ois.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				try {
					ob.openingBookWhite = (OpeningTreeNode) ois.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				try {
					ob.openingBookBlack = (OpeningTreeNode) ois.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				try {
					ob.customStrategyWhite = (OpeningTreeNode) ois.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				try {
					ob.customStrategyBlack = (OpeningTreeNode) ois.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				ois.close();
				return ob;
				
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
	 }
	 
	 public OpeningBook loadOpeningBook(InputStream is) {
		try {
				ObjectInputStream ois = new ObjectInputStream (is);
				OpeningBook ob = new OpeningBook();
				try {
					ob = (OpeningBook) ois.readObject();
					ois.close();
					return ob;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					return null;
				}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Darkboard(boolean isWhite) {
		localPlayerWhite = isWhite;
		failedMoves.clear();
		OpponentProfile op = load();
		setP1(new DeepDarkboard101(localPlayerWhite, op.openingBookWhite, op.openingBookBlack));
		getP1().setCurrentUmpire(this);
		currentTurnWhite = true;
		
		Hashtable<String,Object> d1; //parameters to send the local player
		d1 = new Hashtable<String,Object>();
		
		d1.put("isWhite", isWhite);
		
		d1.put("white", 2);
		d1.put("black", 1);
		d1.put("where", "ludii");
		
		getP1().startMatch(d1);
	}
	
	public void illegalMove() {
		getP1().communicateIllegalMove(lastAttemptedMove);
		failedMoves.add(lastAttemptedMove.clone());
	}
	
	private int getTries(String message) {
		int i = 0, tries = 0;
		while(message.charAt(i) >= '0' && message.charAt(i) <= '9') {
			tries *= 10;
			tries += message.charAt(i) - '0';
			i++;
		}
		return tries;
	}
	
	private String getCapturePosition(String message) {
		String square = ""; 
		
		for(int i = 1; i < message.length(); i++) {
			if(Character.isUpperCase(message.charAt(i)) || Character.isDigit(message.charAt(i))) 
				square += message.charAt(i);

		}
		return square;
	}
	
	public void umpireMessage(List <String> messages) {
		int captureX = -1;
		int captureY = -1;
		int check1 = NO_CHECK;
		int check2 = NO_CHECK;
		int pawnTries = 0;
		int captureType = NO_CAPTURE;
		
		for(int i = 0; i < messages.size(); i++) {
			
			String message = messages.get(i);
			
			if(message.matches("(.*) captured")) {
				String square = getCapturePosition(message);
				captureX = square.charAt(0) - 'A';
				captureY = square.charAt(1) - '1';
				if(message.matches("Pawn at (.*) captured")) captureType = CAPTURE_PAWN;
				else if(message.matches("Piece at (.*) captured")) captureType = CAPTURE_PIECE;
			}
			
			else if(message.matches("Short diagonal check")) {
				if(check1 > 0) check2 = CHECK_SHORT_DIAGONAL;
				else check1 = CHECK_SHORT_DIAGONAL;
			}
			
			else if(message.matches("Long diagonal check")) {
				if(check1 > 0) check2 = CHECK_LONG_DIAGONAL;
				else check1 = CHECK_LONG_DIAGONAL;
			}
			
			else if(message.matches("File check")) {
				if(check1 > 0) check2 = CHECK_FILE;
				else check1 = CHECK_FILE;
			}
			
			else if(message.matches("Rank check")) {
				if(check1 > 0) check2 = CHECK_RANK;
				else check1 = CHECK_RANK;
			}
			
			else if(message.matches("Knight check")) {
				if(check1 > 0) check2 = CHECK_KNIGHT;
				else check1 = CHECK_KNIGHT;
			}
			else if(message.matches("(.*) try") || message.matches("(.*) tries")) {
				pawnTries = getTries(message);
			}
		}
		if (captureX!=-1 || (lastAttemptedMove!=null && lastAttemptedMove.piece==Chessboard.PAWN))
			fiftyMoves = 0;
		else if (currentTurnWhite==localPlayerWhite) fiftyMoves++;
		currentTurnWhite = !currentTurnWhite;
		getP1().communicateUmpireMessage(captureX,captureY,pawnTries,check1, check2, captureType);
	}
	
	public void legalMove(List <String> messages) {
		int capture = NO_CAPTURE;
		int oppTries = 0;
		int oppCheck = NO_CHECK;
		int oppCheck2 = NO_CHECK;
		
		for(int i = 0; i < messages.size(); i++) {
			
			String message = messages.get(i);
			// System.out.println(message);
			
			if(message.matches("Pawn at (.*) captured")) capture = CAPTURE_PAWN;
			else if(message.matches("Piece at (.*) captured")) capture = CAPTURE_PIECE;
			
			else if(message.matches("Short diagonal check")) {
				if(oppCheck > 0) oppCheck2 = CHECK_SHORT_DIAGONAL;
				else oppCheck = CHECK_SHORT_DIAGONAL;
			}
			
			else if(message.matches("Long diagonal check")) {
				if(oppCheck > 0) oppCheck2 = CHECK_LONG_DIAGONAL;
				else oppCheck = CHECK_LONG_DIAGONAL;
			}
			
			else if(message.matches("File check")) {
				if(oppCheck > 0) oppCheck2 = CHECK_FILE;
				else oppCheck = CHECK_FILE;
			}
			
			else if(message.matches("Rank check")) {
				if(oppCheck > 0) oppCheck2 = CHECK_RANK;
				else oppCheck = CHECK_RANK;
			}
			
			else if(message.matches("Knight check")) {
				if(oppCheck > 0) oppCheck2 = CHECK_KNIGHT;
				else oppCheck = CHECK_KNIGHT;
			}
			else if(message.matches("(.*) try") || message.matches("(.*) tries")) {
				oppTries = getTries(message);
			}
			
		}
		if (capture!=Chessboard.NO_CAPTURE || (lastAttemptedMove!=null && lastAttemptedMove.piece==Chessboard.PAWN))
			fiftyMoves = 0;
		else if (currentTurnWhite==localPlayerWhite) fiftyMoves++;
		failedMoves.clear();
		currentTurnWhite = !currentTurnWhite;
		getP1().communicateLegalMove(capture,oppTries,oppCheck,oppCheck2);
	}
	
	
	public other.move.Move sendNextMove() {
		try
		{
			if (getP1().shouldAskDraw()) offerDraw(getP1());
			else
			{
				lastAttemptedMove = getP1().getNextMove();
				if (lastAttemptedMove==null)
				{
					//not a move, eh? Maybe you want to offer a draw, resign, anything?
				 	resign(getP1());
				 	return null;
				}
			}
		} catch (Exception e)
		{
			//if an exception happens, just resign...
			lastAttemptedMove = null;
			e.printStackTrace();
			resign(getP1());
			return null;
		}
		
		promotionPiece = lastAttemptedMove.piece;
		return KriegspielAgent.initiateMove(lastAttemptedMove.fromY * 8 + lastAttemptedMove.fromX, lastAttemptedMove.toY * 8 + lastAttemptedMove.toX);
	}
	
	public PromotionPiece communicatePromotionPiece() {
		switch (promotionPiece)
		{
			case Chessboard.BISHOP: return PromotionPiece.B;
			case Chessboard.ROOK: return PromotionPiece.R;
			case Chessboard.KNIGHT: return PromotionPiece.N;
			case Chessboard.QUEEN: return PromotionPiece.Q;
		}
		return PromotionPiece.Q;
	}
	
}