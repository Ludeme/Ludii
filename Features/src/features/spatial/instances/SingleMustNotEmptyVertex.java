package features.spatial.instances;

import game.Game;
import game.types.board.SiteType;
import main.collections.ChunkSet;
import other.state.State;

/**
 * A test that check for a single specific vertex that must NOT be empty
 *
 * @author Dennis Soemers
 */
public class SingleMustNotEmptyVertex extends AtomicProposition
{
	
	//-------------------------------------------------------------------------
	
	/** The site that must be empty */
	protected final int mustNotEmptySite;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param mustNotEmptySite
	 */
	public SingleMustNotEmptyVertex(final int mustNotEmptySite)
	{
		this.mustNotEmptySite = mustNotEmptySite;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean matches(final State state)
	{
		return !state.containerStates()[0].emptyChunkSetVertex().get(mustNotEmptySite);
	}

	@Override
	public boolean onlyRequiresSingleMustEmpty()
	{
		return false;
	}

	@Override
	public boolean onlyRequiresSingleMustWho()
	{
		return false;
	}

	@Override
	public boolean onlyRequiresSingleMustWhat()
	{
		return false;
	}

	@Override
	public SiteType graphElementType()
	{
		return SiteType.Vertex;
	}
	
	@Override
	public void addMaskTo(final ChunkSet chunkSet)
	{
		chunkSet.set(mustNotEmptySite);
	}
	
	@Override
	public StateVectorTypes stateVectorType()
	{
		return StateVectorTypes.Empty;
	}
	
	@Override
	public int testedSite()
	{
		return mustNotEmptySite;
	}
	
	@Override
	public int value()
	{
		return 1;
	}
	
	@Override
	public boolean negated()
	{
		return true;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean provesIfTrue(final AtomicProposition other, final Game game)
	{
		if (graphElementType() != other.graphElementType())
			return false;
		
		if (testedSite() != other.testedSite())
			return false;
		
		// If not empty, we prove not empty
		return (other.stateVectorType() == StateVectorTypes.Empty && other.negated());
	}

	@Override
	public boolean disprovesIfTrue(final AtomicProposition other, final Game game)
	{
		if (graphElementType() != other.graphElementType())
			return false;
		
		if (testedSite() != other.testedSite())
			return false;
		
		// If not empty, we disprove empty
		return (other.stateVectorType() == StateVectorTypes.Empty && !other.negated());
	}

	@Override
	public boolean provesIfFalse(final AtomicProposition other, final Game game)
	{
		if (graphElementType() != other.graphElementType())
			return false;
		
		if (testedSite() != other.testedSite())
			return false;
		
		// If not not empty, we prove empty
		if (other.stateVectorType() == StateVectorTypes.Empty)
			return !other.negated();
		
		// If not not empty, we prove that it's not friend, not enemy, not piece 1, not piece 2, etc.
		return (other.stateVectorType() != StateVectorTypes.Empty && other.value() > 0 && other.negated());
	}

	@Override
	public boolean disprovesIfFalse(final AtomicProposition other, final Game game)
	{
		if (graphElementType() != other.graphElementType())
			return false;
		
		if (testedSite() != other.testedSite())
			return false;
		
		// If not not empty, we disprove not empty
		if (other.stateVectorType() == StateVectorTypes.Empty)
			return other.negated();
		
		// If not not empty, we disprove friend, enemy, piece 1, piece 2, etc.
		return (other.stateVectorType() != StateVectorTypes.Empty && other.value() > 0 && !other.negated());
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + mustNotEmptySite;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof SingleMustNotEmptyVertex))
			return false;

		final SingleMustNotEmptyVertex other = (SingleMustNotEmptyVertex) obj;
		return (mustNotEmptySite == other.mustNotEmptySite);
	}
	
	@Override
	public String toString()
	{
		return "[Vertex " + mustNotEmptySite + " must NOT be empty]";
	}
	
	//-------------------------------------------------------------------------

}
