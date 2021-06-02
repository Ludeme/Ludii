package other.concept;

/**
 * The different concept computation types.
 * 
 * @author Eric.Piette
 */
public enum ConceptComputationType
{
	/** Compute during compilation. */
	Compilation(1),

	/** Compute thanks to playouts. */
	Playout(2),
	;
	
	// -------------------------------------------------------------------------

	/** The id of the concept computation type. */
	final int id;

	// -------------------------------------------------------------------------

	/**
	 * To create a new concept computation type.
	 * 
	 * @param id The id of the concept data type.
	 */
	private ConceptComputationType
	(
		final int id 
	)
	{
		this.id = id;
	}

	// -------------------------------------------------------------------------

	/**
	 * @return The id of the concept computation type.
	 */
	public int id()
	{
		return this.id;
	}
}
