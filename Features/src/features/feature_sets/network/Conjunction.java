package features.feature_sets.network;

import java.util.BitSet;

/**
 * Represents a conjunction of atomic propositions. Is mutable: we can modify
 * it by giving it propositions that we "assume to have been proven", which will
 * then be removed from the requirements
 *
 * @author Dennis Soemers
 */
public class Conjunction
{
	
	//-------------------------------------------------------------------------
	
	/** IDs of atomic propositions that must be true */
	private final BitSet mustTrue;
	
	/** Number of propositions that are to be proven */
	private int length;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param mustTrue
	 */
	public Conjunction(final BitSet mustTrue)
	{
		this.mustTrue = mustTrue;
		length = mustTrue.cardinality();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Tells this conjunction to assume that proposition of given ID is true
	 * (safe to also call on conjunctions that do not require this proposition at all)
	 * @param id
	 * @return True if and only if the given proposition was a requirement of this conjunction
	 */
	public boolean assumeTrue(final int id)
	{
		if (mustTrue.get(id))
		{
			mustTrue.clear(id);
			--length;
			return true;
		}
		
		return false;
	}
	
	/**
	 * @param other
	 * @return True if, ignoring propositions that are already assumed to have been proven,
	 * this conjunction generalises the given other conjunction.
	 */
	public boolean generalises(final Conjunction other)
	{
		if (length > other.length)
			return false;
		
		final BitSet otherToProve = other.toProve();
		final BitSet toProve = (BitSet) mustTrue.clone();
		toProve.andNot(otherToProve);
		return toProve.isEmpty();
	}
	
	/**
	 * @return Length of this conjunction: number of propositions remaining to be proven
	 */
	public int length()
	{
		return length;
	}
	
	/**
	 * @return BitSet of IDs that must still be proven
	 */
	public BitSet toProve()
	{
		return mustTrue;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mustTrue == null) ? 0 : mustTrue.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof Conjunction))
			return false;
		
		final Conjunction other = (Conjunction) obj;
		return mustTrue.equals(other.mustTrue);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return "[Conjunction: " + mustTrue + "]";
	}
	
	//-------------------------------------------------------------------------

}
