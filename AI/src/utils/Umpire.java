package utils;

import java.util.ArrayList;
import java.util.List;

import game.Game;
import main.collections.FastArrayList;
import other.AI;
import other.action.Action;
import other.context.Context;
import other.move.Move;
import main.Darkboard;

/**
 * Umpire that controls the AI players in the game of Kriegspiel.
 * 
 * @author Nikola Novarlic
 */

public class Umpire extends AI
{

	//-------------------------------------------------------------------------
	
	/** AI player index **/
	protected int player = -1;
	
	/** Opponent player index **/
	protected int opponent = -1;
	
	/** Indicator of the first move **/
	protected boolean firstMove;
	
	/** Number of players **/
	protected int players = 2;
	
	/**  The agent we use for the current game **/
	KriegspielAgent agent;
	
	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public Umpire(String friendlyName)
	{
		this.friendlyName = friendlyName;
	}

	//-------------------------------------------------------------------------

	/** 
	 * Method for retrieving Umpire messages after the specific move.
	 * 
	 * @param context Copy of the context containing the current state of the game.
	 * @param player Player that provides a last move.
	 * @return List containing Umpire messages.
	**/
	private List <String> getNotes(Context context, int player) {
		
		List <String> notes = new ArrayList <String>(); 
		
		if(context.trial().lastMove(player) == null) 
			return notes;
		
		List <Action> actions = context.trial().lastMove(player).actions();
		
		for (Action action: actions) {
			if(action.getDescription().contentEquals("Note"))
				notes.add(action.message());
		}
		
		// Notes are for some reason duplicated in actions
		// We need to remove all even indexes
		for(int i = notes.size() - 1;  i >= 0; i -= 2) {
			notes.remove(i);
		}
		return notes;
	}
	
	/**
	 *  Method to communicate the opponent's legal move to the player. 
	 * 
	 * @param context Copy of the context containing the current state of the game.
	**/
	private void communicateUmpireMessage(Context context) {
		List <String> umpireMessages = getNotes(context, opponent);
		// Generate umpire message communicating pawn tries 
		umpireMessages.add(String.valueOf(context.score(player)).concat((context.score(player) == 1)? " try" : " tries"));
		agent.umpireMessage(umpireMessages);
	}
	
	/** 
	 * Method to communicate legal move to the player. 
	 * 
	 * @param context Copy of the context containing the current state of the game.
	**/
	private void communicateLegalMove(Context context) {
		List <String> legalMove = getNotes(context, player);		
		legalMove.add(String.valueOf(context.score(opponent)).concat((context.score(opponent) == 1)? " try" : " tries"));
		agent.legalMove(legalMove);
	}
	
	/** 
	 * Method to check if the provided move is legal
	 *  
	 * @param nextMove Move provided by the player 
	 * @param legalMoves List of legal moves obtained from the game
	 * @return A move, if exists in the list of legal moves, null otherwise   
	**/
	private Move examineLegalMove(Move nextMove, FastArrayList<Move> legalMoves) {
		for (Move legalMove : legalMoves) {
			if(legalMove.from() == nextMove.fromNonDecision() && legalMove.to() == nextMove.toNonDecision())
				return legalMove;
		}
		return null;
	} 
	
	/** 
	 * @return True in case the agent plays with white pieces, false otherwise 
	**/
	public boolean aiPlayerWhite() {
		return (player == 1);
	}
	
	/** 
	 * @param legalMoves A list of available promotion moves
	 * @param promotionPiece An identifier of the desired piece to replace a pawn
	 * @return A promotion move to the desired piece 
	**/
	private Move promotePawn(FastArrayList<Move> legalMoves, PromotionPiece promotionPiece) {
		int pieceLetter = 0;
		if(aiPlayerWhite()) {
			switch (promotionPiece) {
			case B: 
				pieceLetter = 7;
				break;
			case R: 
				pieceLetter = 3;
				break;
			case N: 
				pieceLetter = 9;
				break;
			case Q: 
				pieceLetter = 11;
				break;
			}
		}
		else {
			switch (promotionPiece) {
			case B: 
				pieceLetter = 8;
				break;
			case R: 
				pieceLetter = 4;
				break;
			case N: 
				pieceLetter = 10;
				break;
			case Q: 
				pieceLetter = 12;
				break;
			}
		}
		for(Move move:legalMoves) {
			if(move.what() == pieceLetter) return move;
		}
		return null;
	}
	
	/** 
	 * Method to keep requesting a legal move from the player by notifying 
	 * him about illegal moves he made.
	 * 
	 * @param game Reference to the game we're playing.
	 * @param context Copy of the context containing the current state of the game 
	 * @return nextMove A legal move to be played
	**/
	public Move seekLegalMove(final Game game, final Context context) {
		
		FastArrayList<Move> legalMoves = game.moves(context).moves();
		
		// Check if there is a promotion opportunity
		if(legalMoves.get(0).actionDescriptionStringShort().contentEquals("Promote")) {
			return promotePawn(legalMoves, agent.communicatePromotionPiece());
		}
		
		Move nextMove = examineLegalMove(agent.sendNextMove(), legalMoves);
		
		while(nextMove == null) {
			agent.illegalMove();
			nextMove = examineLegalMove(agent.sendNextMove(), legalMoves);
		}
			
		return nextMove;
	}
	
	@Override
	public Move selectAction
	(
		final Game game, 
		final Context context, 
		final double maxSeconds,
		final int maxIterations,
		final int maxDepth
	)
	{
		if(firstMove && aiPlayerWhite()) {
			// There are no previous moves to be communicated
			firstMove = false;
			return seekLegalMove(game, context);
		}
		else if(firstMove) {
			// For the black player. The first move of an opponent should be communicated
			communicateUmpireMessage(context);
			firstMove = false;
			return seekLegalMove(game, context);
		}
		communicateLegalMove(context);
		communicateUmpireMessage(context);
		return seekLegalMove(game, context);	
	}
	
	@Override
	public boolean supportsGame(final Game game)
	{
		return game.name().equals("Kriegspiel");
	}
	
	@Override
	public void initAI(final Game game, final int playerID)
	{
		this.player = playerID;
		this.opponent = this.players - playerID + 1;
		this.firstMove = true;
		// Assigning constructed AI which implements functions defined in KriegspielAgent
		if(friendlyName.contentEquals("Darkboard")) agent = new Darkboard(aiPlayerWhite());
	}
	
	//-------------------------------------------------------------------------

}
