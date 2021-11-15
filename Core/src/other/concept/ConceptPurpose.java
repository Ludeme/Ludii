package other.concept;

/**
 * The different possible uses of a game concept.
 * 
 * @author Eric.Piette
 */
public enum ConceptPurpose
{
	/** Can be used for AI. */
	AI(1),

	/** Can be used for reconstruction. */
	Reconstruction(2),
	;
	//-------------------------------------------------------------------------

	/** The id of the concept purpose. */
	final int id;

	//-------------------------------------------------------------------------

	/**
	 * To create a new concept purpose.
	 * 
	 * @param id The id of the concept purpose.
	 */
	private ConceptPurpose
	(
		final int id 
	)
	{
		this.id = id;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The id of the concept purpose.
	 */
	public int id()
	{
		return this.id;
	}
}
