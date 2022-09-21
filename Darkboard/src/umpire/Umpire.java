/*
 * Created on 15-apr-05
 *
 */
package umpire;

import ai.player.Player;
import pgn.ExtendedPGNGame;

/**
 * @author Nikola Novarlic
 *
 */
public class Umpire {
	
	private Player p2;
	private Player p1;
	protected int fiftyMoves = 0;
	
	
	public boolean timedGame = false;
	protected int startTime = 0;
	protected int timeIncrement = 0;
	
	protected int timeLeft = 0;
	protected int opponentTimeLeft = 0;
	protected java.util.Timer		timer;
	
	public ExtendedPGNGame transcript;

	/**
	 * @return
	 */
	public int getFiftyMoves() {
		return fiftyMoves;
	}

	/**
	 * @param i
	 */
	public void setFiftyMoves(int i) {
		fiftyMoves = i;
	}
	
	public synchronized void decreaseTime()
	{
		timeLeft--;
	}
	
	public synchronized void setTime(int sec)
	{
		timeLeft = sec;
	}
	
	public synchronized int getTime()
	{
		return timeLeft;
	}
	
	public void setOpponentTime(int t) { opponentTimeLeft = t; }
	public int getOpponentTime() { return opponentTimeLeft; }

	/**
	 * @return
	 */
	public int getStartTime() {
		return startTime;
	}

	/**
	 * @return
	 */
	public int getTimeIncrement() {
		return timeIncrement;
	}

	/**
	 * @param i
	 */
	public void setStartTime(int i) {
		startTime = i;
	}

	/**
	 * @param i
	 */
	public void setTimeIncrement(int i) {
		timeIncrement = i;
	}
	
	
	public String offerDraw(Player offerer)
	{
		return "";
	}
	
	public void resign(Player offerer)
	{
	}

	public void setP1(Player p1) {
		this.p1 = p1;
	}

	public Player getP1() {
		return p1;
	}

	public void setP2(Player p2) {
		this.p2 = p2;
	}

	public Player getP2() {
		return p2;
	}

}
