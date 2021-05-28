package features.spatial.instances;

import game.Game;
import game.types.board.SiteType;
import main.collections.ChunkSet;
import other.state.State;

/**
 * A test that check for a single specific cell that must be empty
 *
 * @author Dennis Soemers
 */
public class SingleMustEmptyCell extends AtomicProposition
{
	
	//-------------------------------------------------------------------------
	
	/** The site that must be empty */
	protected final int mustEmptySite;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param mustEmptySite
	 */
	public SingleMustEmptyCell(final int mustEmptySite)
	{
		this.mustEmptySite = mustEmptySite;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean matches(final State state)
	{
		return state.containerStates()[0].emptyChunkSetCell().get(mustEmptySite);
	}

	@Override
	public boolean onlyRequiresSingleMustEmpty()
	{
		return true;
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
		return SiteType.Cell;
	}
	
	@Override
	public void addMaskTo(final ChunkSet chunkSet)
	{
		chunkSet.set(mustEmptySite);
	}
	
	@Override
	public StateVectorTypes stateVectorType()
	{
		return StateVectorTypes.Empty;
	}
	
	@Override
	public int testedSite()
	{
		return mustEmptySite;
	}
	
	@Override
	public int value()
	{
		return 1;
	}
	
	@Override
	public boolean negated()
	{
		return false;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean provesIfTrue(final AtomicProposition other, final Game game)
	{
		if (graphElementType() != other.graphElementType())
			return false;
		
		if (testedSite() != other.testedSite())
			return false;
		
		// If empty, we prove the same (equal prop, probably shouldn't happen)
		if (other.stateVectorType() == StateVectorTypes.Empty)
			return !other.negated();
		
		// If empty, we prove that it's not friend, not enemy, not piece 1, not piece 2, etc.
		return (other.stateVectorType() != StateVectorTypes.Empty && other.value() > 0 && other.negated());
	}

	@Override
	public boolean disprovesIfTrue(final AtomicProposition other, final Game game)
	{
		if (graphElementType() != other.graphElementType())
			return false;
		
		if (testedSite() != other.testedSite())
			return false;
		
		// If empty, we disprove not empty
		if (other.stateVectorType() == StateVectorTypes.Empty)
			return other.negated();
		
		// If empty, we disprove friend, enemy, piece 1, piece 2, etc.
		return (other.stateVectorType() != StateVectorTypes.Empty && other.value() > 0 && !other.negated());
	}

	@Override
	public boolean provesIfFalse(final AtomicProposition other, final Game game)
	{
		if (graphElementType() != other.graphElementType())
			return false;
		
		if (testedSite() != other.testedSite())
			return false;
		
		// If not empty, we prove not empty
		return (other.stateVectorType() == StateVectorTypes.Empty && other.negated());
	}

	@Override
	public boolean disprovesIfFalse(final AtomicProposition other, final Game game)
	{
		if (graphElementType() != other.graphElementType())
			return false;
		
		if (testedSite() != other.testedSite())
			return false;
		
		return false;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + mustEmptySite;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof SingleMustEmptyCell))
			return false;

		final SingleMustEmptyCell other = (SingleMustEmptyCell) obj;
		return (mustEmptySite == other.mustEmptySite);
	}
	
	@Override
	public String toString()
	{
		return "[Cell " + mustEmptySite + " must be empty]";
	}
	
	//-------------------------------------------------------------------------

}
