package features.feature_sets.network;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * A disjunctive clause: a disjunction of one or more conjunctions. Is mutable:
 * we can modify it by giving it propositions that are assumed to have already
 * been proven, which will then be removed from conjunctions they appear in,
 * and fully-proven conjunctions will also be removed entirely from the
 * disjunctive clause!
 *
 * @author Dennis Soemers
 */
public class DisjunctiveClause
{
	
	//-------------------------------------------------------------------------
	
	/** List of conjunctions, one of which must be true for this disjunction to be true */
	private final List<Conjunction> conjunctions;
	
	/** Number of conjunctions that we assume to have already been proven */
	private int numAssumedTrue = 0;		// NOTE: field ignored in hashCode() and equals()
	
	/** BitSet of all propositions that show up anywhere in any of the conjunctions of this disjunction */
	private final BitSet usedPropositions = new BitSet();
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public DisjunctiveClause()
	{
		this.conjunctions = new ArrayList<Conjunction>();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Adds the given conjunction to list of conjunctions that can prove this disjunction
	 * @param conjunction
	 */
	public void addConjunction(final Conjunction conjunction)
	{
		conjunctions.add(conjunction);
		usedPropositions.or(conjunction.toProve());
	}
	
	/**
	 * Tells this disjunction to assume that proposition of given ID is true. Will propagate
	 * to all conjunctions, and remove fully-proven conjunctions.
	 * 
	 * @param id
	 */
	public void assumeTrue(final int id)
	{
		if (usedPropositions.get(id))
		{
			for (final Conjunction conjunction : conjunctions)
			{
				if (conjunction.assumeTrue(id))
				{
					if (conjunction.length() == 0)
					{
						conjunctions.remove(conjunction);
						++numAssumedTrue;
					}
				}
			}
			
			usedPropositions.clear(id);
		}
	}
	
	/**
	 * Tells this disjunction to assume that all propositions in given 
	 * BitSet are true. Will propagate to all conjunctions, and remove 
	 * fully-proven conjunctions.
	 * 
	 * @param propositions
	 */
	public void assumeTrue(final BitSet propositions)
	{
		final BitSet intersection = (BitSet) propositions.clone();
		intersection.and(usedPropositions);
		
		for (int id = intersection.nextSetBit(0); id >= 0; id = intersection.nextSetBit(id + 1))
		{
			for (int i = conjunctions.size() - 1; i >= 0; --i)
			{
				final Conjunction conjunction = conjunctions.get(i);
				if (conjunction.assumeTrue(id))
				{
					if (conjunction.length() == 0)
					{
						conjunctions.remove(i);
						++numAssumedTrue;
					}
				}
			}
		}
		
		usedPropositions.andNot(intersection);
	}
	
	/**
	 * @return Our list of conjunctions
	 */
	public List<Conjunction> conjunctions()
	{
		return conjunctions;
	}
	
	/**
	 * Removes any conjunctions that are generalised by any other conjunctions
	 * that are also in this disjunction.
	 */
	public void eliminateGeneralisedConjunctions()
	{
		final int oldSize = conjunctions.size();
		
		for (int i = 0; i < conjunctions.size(); ++i)
		{
			final Conjunction iConj = conjunctions.get(i);
			
			for (int j = conjunctions.size() - 1; j > i; --j)
			{
				final Conjunction jConj = conjunctions.get(j);
				if (iConj.generalises(jConj))
					conjunctions.remove(j);
			}
		}
		
		if (conjunctions.size() != oldSize)
		{
			// We've removed some conjunctions, should recompute usedPropositions to be safe
			usedPropositions.clear();
			for (final Conjunction conj : conjunctions)
			{
				usedPropositions.or(conj.toProve());
			}
		}
	}
	
	/**
	 * @param other
	 * @return True if and only if this disjunctive clause generalises the given
	 * other disjunctive clause.
	 */
	public boolean generalises(final DisjunctiveClause other)
	{
		outer:
		for (final Conjunction otherConj : other.conjunctions)
		{
			for (final Conjunction myConj : conjunctions)
			{
				if (myConj.generalises(otherConj))
					continue outer;
			}
			
			return false;
		}
		
		return !other.conjunctions.isEmpty();
	}
	
	/**
	 * @return Number of conjunctions (excluding ones already assumed to be true)
	 */
	public int length()
	{
		return conjunctions.size();
	}
	
	/**
	 * @return Number of conjunctions that we assume to have been fully proven
	 */
	public int numAssumedTrue()
	{
		return numAssumedTrue;
	}
	
	/**
	 * Sets the number of conjunctions that are assumed to have already been fully proven.
	 * Can be used when "merging" disjunctions that have become "equal" after making some
	 * assumptions of proven conjunctions.
	 * @param num
	 */
	public void setNumAssumedTrue(final int num)
	{
		this.numAssumedTrue = num;
	}
	
	/**
	 * @return Bitset of all propositions that show up in any conjunctions in this disjunction
	 */
	public BitSet usedPropositions()
	{
		return usedPropositions;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return "[Disjunction: " + conjunctions + "]";
	}
	
	//-------------------------------------------------------------------------

}
