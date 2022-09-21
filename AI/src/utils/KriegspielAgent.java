package utils;

/**
 * Interface that each Kriegspiel agent has to implement.
 *
 * @author Nikola Novarlic
 */

import java.util.List;

import other.move.Move;

public interface KriegspielAgent {
	
	/**
	 * Using this method Umpire requests the next move from the player.
	 *  
	 * @return move to be played
	**/
	public Move sendNextMove();
	
	
	/**
	 * Using this method Umpire notifies the player about an illegal move he made.
	**/
	public void illegalMove();
	
	
	/**
	 * Using this method Umpire notifies the player that the opponent has made a move.
	 * 
	 * @param messages List of Umpire messages after the move is played. 
	**/
	public void umpireMessage(List <String> messages);
	
	
	/**
	 * Using this method Umpire notifies the player about the legal move he made.
	 * 
	 * @param messages List of Umpire messages after the move is played. 
	**/
	public void legalMove(List <String> messages);
	
	
	/**
	 * @return Identifier of the desired promotion piece.
	**/
	public PromotionPiece communicatePromotionPiece();
	
	//-------------------------------------------------------------------------
	
	/**
	 * Convert a board square to a standard Ludii integer identifier.
	 * 
	 * @param first Char 'A' - 'H'
	 * @param second Char '1' - '8'
	 * @return Integer 
	**/
	public static int squareToInt(char first, char second) {
		return (second - '1') * 8 + (first - 'A') ; 
	}
	
	
	/**
	 * @param from Integer
	 * @param to Integer
	 * @return Ludii Move Class object
	**/
	public static Move initiateMove(final int from, final int to) {
		String detailedString = String.format("Move:from=%d, to=%d, mover=-1, actions=", from, to);
		return new Move(detailedString);
	}
	
}
