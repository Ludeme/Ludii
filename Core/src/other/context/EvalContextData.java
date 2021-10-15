package other.context;

/**
 * Defines each eval context data used in the eval method of the ludemes.
 * 
 * @author Eric.Piette
 */
public enum EvalContextData
{
	/** Variable used to iterate the 'from' locations. */
	From,

	/** Variable used to iterate the levels. */
	Level,

	/** Variable used to iterate the 'to' locations. */
	To,

	/** Variable used to iterate the 'between' locations. */
	Between,

	/** Variable used to iterate the number of pips of each die. */
	PipCount,

	/** Variable used to iterate the players. */
	Player,

	/** Variable used to iterate the tracks. */
	Track,

	/** Variable used to iterate some sites. */
	Site,

	/** Variable used to iterate values. */
	Value,

	/** Variable used to iterate regions. */
	Region,

	/** Variable used to iterate hint regions. */
	HintRegion,

	/** Variable used to iterate the hints. */
	Hint,

	/** Variable used to iterate edges. */
	Edge,

	/** Variable used to iterate teams. */
	Team,
	;

	//-------------------------------------------------------------------------

	/**
	 * @return The id of the data.
	 */
	public int id()
	{
		return this.ordinal();
	}
}
