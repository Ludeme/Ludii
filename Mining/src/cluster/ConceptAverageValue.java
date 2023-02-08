package cluster;

import other.concept.Concept;

/**
 * A concept associated with a value. 
 * Used to sort a list.
 * @author Eric.Piette
 *
 */
public class ConceptAverageValue
{
	/**
	 * The concept.
	 */
	final Concept concept; 
	
	/**
	 * The value associated with the concept.
	 */
	final double value;
	
	public ConceptAverageValue(
		final Concept concept,
		final double value
	)
	{
		this.concept = concept;
		this.value = value;
	}
	
}
