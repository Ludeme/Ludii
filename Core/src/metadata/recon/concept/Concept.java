package metadata.recon.concept;

import annotations.Name;
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
	
	/** Concept name. */
	private other.concept.Concept concept;

	/** Concept minimal value. */
	private double minValue;
	
	/** Concept maximal value. */
	private double maxValue;

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
		try{this.concept = other.concept.Concept.valueOf(conceptName);}
		catch(final Exception e)
		{
			this.concept = null;
		}
		minValue = ((valueDouble != null) ? valueDouble.doubleValue() : (valueBoolean.booleanValue() ? 1d : 0d) );
		maxValue = minValue;
	}
	
	/**
	 * @param conceptName The name of the concept.
	 * @param minValue The minimum value.
	 * @param maxValue The maximum value.
	 *
	 * @example (concept "Num Players" 2 4)
	 */
	public Concept
	(
			 final String conceptName,
	   @Name final Float minValue,
	   @Name final Float maxValue
	)
	{
		this.conceptName = conceptName;
		try{this.concept = other.concept.Concept.valueOf(conceptName);}
		catch(final Exception e)
		{
			this.concept = null;
		}
		this.minValue = minValue.doubleValue();
		this.maxValue = maxValue.doubleValue();
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();

		if(minValue == maxValue)
			sb.append(conceptName + " value = " + minValue + "\n");
		else
			sb.append(conceptName + " min value = " + minValue + " " + " max value = " + maxValue +"\n");

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

	//-------------------------------------------------------------------------
	/**
	 * @return The name of the concept
	 */
	public other.concept.Concept concept()
	{
		return concept;
	}

	/**
	 * @return The minimum value of the concept
	 */
	public double minValue()
	{
		return minValue;
	}

	/**
	 * @return The maximum value of the concept
	 */
	public double maxValue()
	{
		return maxValue;
	}

	//-------------------------------------------------------------------------

}
