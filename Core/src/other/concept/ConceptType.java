package other.concept;

/**
 * The different types of the concepts.
 * 
 * @author Eric.Piette
 */
public enum ConceptType
{
	/** The properties of the game. */
	Properties(1),

	/** The concepts related to the equipment. */
	Equipment(2),

	/** The concepts related to the meta rules. */
	Meta(3),

	/** The concepts related to the starting rules. */
	Start(4),

	/** The concepts related to the play rules. */
	Play(5),

	/** The concepts related to the ending rules. */
	End(6),
	
	/** The concepts related to the metrics (behaviour). */
	Behaviour(7),

	/** The concepts related to the implementation. */
	Implementation(8),

	/** The concepts related to the visuals. */
	Visual(9),

	/** The concepts related to the Math. */
	Math(10),
	
	/** The concepts related to the containers. */
	Container(11),
	
	/** The concepts related to the components. */
	Component(12)
	;
	
	//-------------------------------------------------------------------------

	/** The id of the concept type. */
	final int id;

	//-------------------------------------------------------------------------

	/**
	 * To create a new concept type.
	 * 
	 * @param id The id of the concept type.
	 */
	private ConceptType
	(
		final int id 
	)
	{
		this.id = id;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The id of the concept type.
	 */
	public int id()
	{
		return this.id;
	}
}
