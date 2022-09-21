package ai.player;

import core.Move;

public interface PlayerListener {
	
	public void communicateIllegalMove(ai.player.Player p, Move m);
	
	/**
	 * Tells the player that their latest move was legal, the game is still running and the
	 * opponent has received the following information.
	 * @param capture
	 * @param oppTries
	 * @param oppCheck
	 * @param oppCheck2
	 * @return
	 */
	public String communicateLegalMove(Player p, int capture, int oppTries, int oppCheck, int oppCheck2);

	/**
	 * Tells the player that the opponent has moved and the following messages have been sent
	 * by the umpire.
     * @param capX
     * @param capY
     * @param tries
     * @param check
     * @param check2
     * @return
     */
	public String communicateUmpireMessage(Player p, int capX, int capY, int tries, int check, int check2, int capture);

	/**
	 * Informs about the available time.
	 * @param newQty
	 */
	public void updateTime(ai.player.Player p, long newQty);
	
	/**
	 * Tells the player that the game is over.
	 * @param outcome
	 */
	public void communicateOutcome(ai.player.Player p, int outcome);
	
	public void communicateInfo(ai.player.Player p, int code, int parameter);
	
	//Additional information about computer AI internals, which a listener may or may be interested about.
	public boolean isInterestedInObject(ai.player.Player p, Object tag, int messageType);
	public void communicateObject(ai.player.Player p, Object tag, Object value, int messageType);

}
