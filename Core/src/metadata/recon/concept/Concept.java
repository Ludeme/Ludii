package metadata.recon.concept;

import annotations.Or;
import metadata.recon.ReconItem;

//-----------------------------------------------------------------------------

/**
 * Specifies the what concept values are required.
 *
 * @author Matthew.Stephenson and Eric.Piette
 */
public class Concept implements ReconItem
{

	/** Concept name. */
	private final String conceptName;

	/** Concept value. */
	private final double value;

	//-------------------------------------------------------------------------

	/**
	 * @param conceptName The name of the concept.
	 * @param valueDouble The double value.
	 * @param valueBoolean The boolean value.
	 *
	 * @example (concept "Num Players" 6)
	 */
	public Concept
	(
			final String conceptName,
		@Or final Float valueDouble,
		@Or final Boolean valueBoolean
	)
	{
		this.conceptName = conceptName;
		value = ((valueDouble != null) ? valueDouble.doubleValue() : (valueBoolean.booleanValue() ? 1d : 0d) );
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
