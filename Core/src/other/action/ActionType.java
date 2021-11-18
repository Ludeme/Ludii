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
	
	/** To select the from/to sites. */
	Insert,

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
	
	/** To set the count of a site. */
	SetCount,
	
	/** To move a piece from a site to another. */
	Move,
	
	/** To move n piece(s) from a site to another. */
	MoveN,
	
	/** To move a stack of piece(s) from a site to another. */
	StackMove,
	
	/** To copy a piece from a site to another. */
	Copy,
	
	/** To set hidden a site to a player. */
	SetHidden,
	
	/** To set hidden count a site to a player. */
	SetHiddenCount,
	
	/** To set hidden rotation a site to a player. */
	SetHiddenRotation,
	
	/** To set hidden state a site to a player. */
	SetHiddenState,
	
	/** To set hidden value a site to a player. */
	SetHiddenValue,
	
	/** To set hidden What a site to a player. */
	SetHiddenWhat,
	
	/** To set hidden Who a site to a player. */
	SetHiddenWho,
	
	/** To set the counter. */
	SetCounter,
	
	/** To set the next player. */
	SetNextPlayer,
	
	/** To set the pending value. */
	SetPending,
	
	/** To swap two players. */
	Swap,
	
	/** To reset the possible values in a deduction puzzle. */
	Reset,
	
	/** To set a value to a variable in a deduction puzzle. */
	SetValuePuzzle,
	
	/** To toggle a value to a variable in a deduction puzzle. */
	Toggle,
	
	/** To forget a value. */
	Forget,
	
	/** To remember a value. */
	Remember,
	
	/** To set the rotation. */
	SetRotation,
	
	/** To set the value. */
	SetValue,
	
	/** To set the local state. */
	SetState,
	
	/** To set the score. */
	SetScore,
	
	/** To set the temp value. */
	SetTemp,
	
	/** To set a var. */
	SetVar,
	
	/** To store the state. */
	StoreState,
	
	/** To trigger an event. */
	Trigger,
}
