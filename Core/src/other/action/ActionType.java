package other.action;

/**
 * The different type of actions.
 * 
 * @author Eric.Piette
 */
public enum ActionType
{
	/** Action to add a piece. */
	Add,

	/** To Pass the move. */
	Pass,

	/** To go to the next instance of a match. */
	NextInstance,

	/** Noop, a fake action. */
	Noop,

	/** Forfeit action. */
	Forfeit,

	/** To send a message to a player. */
	Note,

	/** To propose something to vote. */
	Propose,

	/** To vote on a proposition done previously. */
	Vote,

	/** To set the value of a player. */
	SetValueOfPlayer,

	/** To set the trump suit in a card game. */
	SetTrumpSuit,

	/** To use a specific die to make another move. */
	UseDie,

	/** To use a specific die to make another move. */
	SetDiceAllEqual,

	/** To set the state site and update the die value. */
	SetStateAndUpdateDice,

	/** To set the cost of a graph element. */
	SetCost,

	/** To set the phase of a graph element. */
	SetPhase,

	/** To set visible a site to a player. */
	SetVisible,

	/** To set masked a site to a player. */
	SetMasked,

	/** To set invisible a site to a player. */
	SetInvisible,

	/** To remove component(s) from a site. */
	Remove,

	/** To select the from/to sites. */
	Select,

	/** To promote a piece. */
	Promote,

	/** Add a player to a team. */
	AddPlayerToTeam,

	/** To bet a value. */
	Bet,

	/** To set the pot. */
	SetPot,

	/** To set the amount of a player. */
	SetAmount,
}
