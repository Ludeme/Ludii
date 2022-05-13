package metadata.recon.concept;

import annotations.Or;
import metadata.info.InfoItem;

//-----------------------------------------------------------------------------

/**
 * Specifies the what concept values are required
 * 
 * @author Matthew.Stephenson
 */
public class Concept implements InfoItem
{
	
	/** Concept name. */
	private final String conceptName;
	
	/** Concept value. */
	private final double value;

	//-------------------------------------------------------------------------

	/**
	 * @param source The source of the game's rules.
	 * 
	 * @example (concept "Num Players" 6)
	 */
	public Concept
	(
		final String conceptName, 
		@Or final Double valueDouble,
		@Or final Boolean valueBoolean
	)
	{
		this.conceptName = conceptName;
		value = ((valueDouble != null) ? valueDouble : ((valueBoolean != null) ? (valueBoolean ? 1d : 0d) : 1));
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("    (concept \"" + conceptName + " value " + "\")\n");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The name of the concept
	 */
	public String conceptName()
	{
		return conceptName;
	}
	
	/**
	 * @return The value of the concept
	 */
	public double value()
	{
		return value;
	}
	
	//-------------------------------------------------------------------------

}
