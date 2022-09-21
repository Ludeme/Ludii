
/*
 * Created on 13-apr-05
 *
 */
package ai.player;

import java.util.Dictionary;
import java.util.Vector;

import ai.mc.MCSTSNode;
import ai.opponent.MetapositionPool;
import core.EvaluationGlobals;
import core.Globals;
import core.Metaposition;
import core.Move;
import core.uberposition.Uberposition;
import database.PlayerModel;
import debug.DebugObject;
import pgn.ExtendedPGNGame;
import umpire.Umpire;

/**
 * This class provides the highest-level view of a Kriegspiel player, encapsulating both humans and AI's.
 * @author Nikola Novarlic
 *
 */
public class Player extends DebugObject {
	
	public static final String PARAM_OPPONENT_NAME = 		"opponentName";
	public static final String PARAM_IS_WHITE = 			"isWhite";
	public static final String PARAM_TIME =					"time";
	public static final String PARAM_TIME_INCREMENT =		"timeIncrement";
	
	public static final int PARAM_CHECKMATE_VICTORY = 1;
	public static final int PARAM_STALEMATE_DRAW = 2;
	public static final int PARAM_CHECKMATE_DEFEAT = 3;
	public static final int PARAM_50_DRAW = 4;
	public static final int PARAM_NO_MATERIAL = 5;
	public static final int PARAM_RESIGN_VICTORY = 6;
	public static final int PARAM_RESIGN_DEFEAT = 7;
	public static final int PARAM_AGREED_DRAW = 8;
	
	public static final int INFO_DRAW_OFFER_REJECTED = 1;
	
	//this is a hack for experimenting stuff. Code will check whether this
	//is set before deciding whether to use experimental features. Useful
	//when doing normal vs. experimental tests.
	public boolean experimental = false;
	
	public String playerName;
	public String currentOpponentName;
	public boolean isWhite;
	
	public int genericOppModel = 0; //test
	
	public Metaposition simplifiedBoard;
	public Uberposition complexBoard;
	
	public EvaluationGlobals globals;
	public Vector bannedMoves = new Vector();
	public short shortBannedMoves[] = new short[0];
	protected Vector playerListeners = new Vector(); //called when situation is updated
	public Move lastMove;
	public int moveNumber = 1;
	
	public Umpire currentUmpire = null;
	public MetapositionPool pool;
	
	public Player()
	{
		if (usesGlobals()) globals = new EvaluationGlobals(this);
	}
	
	public boolean usesGlobals()
	{
		return false;
	}
	
	/**
	 * Begins a match with the parameters set in the dictionary. Parameters include:
	 * @param d
	 */
	public void startMatch(Dictionary d)
	{
		boolean isWhite = ((Boolean)d.get(PARAM_IS_WHITE)).booleanValue();
	}
	
	/**
	 * Asks the player for their next move.
	 * @return
	 */
	public Move getNextMove(){ return lastMove; }
	
	
	/**
	 * A placeholder for human players.
	 *
	 */
	public void setHasTurn(boolean b) {}
	
	/**
	 * Asks the player to play this move as its next, overriding
	 * its usual move generation routines. Often used to resume
	 * adjourned games or start from alternate positions.
	 * @param m
	 */
	public void emulateNextMove(Move m) { lastMove=m; }
	
	/**
	 * Forces the player to play a move. This way the player's knowledge
	 * and data are still updated even if it did not decide its move.
	 * @param m
	 */
	public void playMove(Move m)
	{
		emulateNextMove(m);
		getNextMove();
	}
	
	public void startFromAlternatePosition(Metaposition m)
	{
		simplifiedBoard = m;
	}
	
	/**
	 * Tells the player that their latest move was illegal.
	 * @param m
	 */
	public void communicateIllegalMove(Move m)
	{
		for (int k=0; k<getPlayerListenerNumber(); k++)
			getPlayerListener(k).communicateIllegalMove(this,m);
	}
	
	/**
	 * Tells the player that their latest move was legal, the game is still running and the
	 * opponent has received the following information.
	 * @param capture
	 * @param oppTries
	 * @param oppCheck
	 * @param oppCheck2
	 */
	public String communicateLegalMove(int capture, int oppTries, int oppCheck, int oppCheck2)
	{
		if (!isWhite) moveNumber++;
		for (int k=0; k<getPlayerListenerNumber(); k++)
			return getPlayerListener(k).communicateLegalMove(this,capture, oppTries, oppCheck, oppCheck2);
		return "";
	}

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
		if (isWhite) moveNumber++;
		
		for (int k=0; k<getPlayerListenerNumber(); k++)
			return getPlayerListener(k).communicateUmpireMessage(this,capX,capY,tries,check,check2, captureType);
		return "";
	}
	
	/**
	 * Tells the player generic information from the umpire, for example that their request for a draw
	 * was rejected.
	 * @param code
	 * @param parameter
	 */
	public void communicateUmpireInfo(int code, int parameter)
	{
		for (int k=0; k<getPlayerListenerNumber(); k++)
			getPlayerListener(k).communicateInfo(this, code, parameter);
	}

	/**
	 * Informs about the available time.
	 * @param newQty
	 */
	public void updateTime(long newQty)
	{
		for (int k=0; k<getPlayerListenerNumber(); k++)
			getPlayerListener(k).updateTime(this,newQty);
	}
	
	/**
	 * Tells the player that the game is over.
	 * @param outcome
	 */
	public void communicateOutcome(int outcome)
	{
		for (int k=0; k<getPlayerListenerNumber(); k++)
			getPlayerListener(k).communicateOutcome(this,outcome);
	}
	/**
	 * @return
	 */
	public String getCurrentOpponentName() {
		return currentOpponentName;
	}

	/**
	 * @return
	 */
	public String getPlayerName() {
		return playerName;
	}

	/**
	 * @param string
	 */
	public void setCurrentOpponentName(String string) {
		currentOpponentName = string;
	}

	/**
	 * @param string
	 */
	public void setPlayerName(String string) {
		playerName = string;
	}

	/**
	 * @return
	 */
	public Umpire getCurrentUmpire() {
		return currentUmpire;
	}

	/**
	 * @param umpire
	 */
	public void setCurrentUmpire(Umpire umpire) {
		currentUmpire = umpire;
	}
	
	public boolean isHuman()
	{
		return false;
	}

	public boolean shouldAskDraw()
	{
		return false;
	}
	
	public boolean shouldAcceptDraw()
	{
		return false;
	}
	
	public boolean isMoveBanned(Move m)
	{
		for (int k=0; k<bannedMoves.size(); k++)
		{
			Move m2 = (Move)bannedMoves.get(k);
			if (m2.equals(m)) return true;
		}
		return false;
	}
	
	public boolean isShortMoveBanned(short m)
	{
		for (int k=0; k<shortBannedMoves.length; k++)
			if (m==shortBannedMoves[k]) return true;
		
		return false;
	}
	
	public void banMove(Move m)
	{
		//System.out.println("Banning "+m);
		if (!isMoveBanned(m)) 
		{
			bannedMoves.add(m);
			banShortMove(MCSTSNode.move2Short(m));
		}
	}
	
	public void banShortMove(short m)
	{
		if (!isShortMoveBanned(m))
		{
			short a[] = new short[shortBannedMoves.length+1];
			for (int k=0; k<shortBannedMoves.length; k++) a[k] = shortBannedMoves[k];
			a[shortBannedMoves.length] = m;
			shortBannedMoves = a;
			banMove(MCSTSNode.short2Move(m));
		}
	}
	
	public void unbanMoves()
	{
		bannedMoves.clear();
		shortBannedMoves = new short[0];
	}

	public void addPlayerListener(PlayerListener pl)
	{
		playerListeners.add(pl);
	}
	
	public void removePlayerListener(PlayerListener pl)
	{
		playerListeners.remove(pl);
	}
	
	public int getPlayerListenerNumber()
	{
		return playerListeners.size();
	}
	
	public PlayerListener getPlayerListener(int k)
	{
		return (PlayerListener)playerListeners.get(k);
	}
	
	/**
	 * If true, at least one listener is interested in an information object from the player.
	 * This is useful as it allows the player to not generate information if no-one is
	 * interested in it.
	 * @param tag
	 * @return
	 */
	public boolean IsAnyoneInterestedIn(Object tag, int messageType)
	{
		for (int k=0; k<getPlayerListenerNumber(); k++)
			if (getPlayerListener(k).isInterestedInObject(this, tag, messageType)) return true;
		
		return false;
	}
	
	/**
	 * Communicates a piece of information to interested parties. Examples include
	 * computation data, main variants, expectations and internal variables.
	 * @param tag
	 * @param value
	 */
	public void communicateObject(Object tag,Object value, int messageType)
	{
		for (int k=0; k<getPlayerListenerNumber(); k++)
			if (getPlayerListener(k).isInterestedInObject(this, tag, messageType))
				getPlayerListener(k).communicateObject(this, tag, value, messageType);
	}
	
	public PlayerModel getOpponentModel()
	{
		if (isWhite) return Globals.blackModel[genericOppModel]; else return Globals.whiteModel[genericOppModel];
	}
	
	public void receiveAftermath(ExtendedPGNGame game)
	{
		
	}
	
}
