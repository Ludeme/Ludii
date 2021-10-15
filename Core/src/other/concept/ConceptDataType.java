package other.concept;

/**
 * The different concept data types.
 * 
 * @author Eric.Piette
 */
public enum ConceptDataType
{
	/** Boolean Data. */
	BooleanData(1),

	/** Integer Data. */
	IntegerData(2),

	/** String Data. */
	StringData(3),

	/** Double Data. */
	DoubleData(4),
	;
	
	//-------------------------------------------------------------------------

	/** The id of the concept data type. */
	final int id;

	//-------------------------------------------------------------------------

	/**
	 * To create a new concept data type.
	 * 
	 * @param id The id of the concept data type.
	 */
	private ConceptDataType
	(
		final int id 
	)
	{
		this.id = id;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The id of the concept data type.
	 */
	public int id()
	{
		return this.id;
	}
}
