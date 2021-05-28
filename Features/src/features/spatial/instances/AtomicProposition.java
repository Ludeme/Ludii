package features.spatial.instances;

import game.Game;
import game.equipment.component.Component;
import gnu.trove.list.array.TIntArrayList;
import main.collections.ChunkSet;

/**
 * An atomic proposition is a test that checks for only a single specific
 * value (either absent or present) in a single specific chunk of a single
 * data vector.
 *
 * @author Dennis Soemers
 */
public abstract class AtomicProposition implements BitwiseTest
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Types of state vectors that atomic propositions can apply to
	 *
	 * @author Dennis Soemers
	 */
	public enum StateVectorTypes
	{
		/** For propositions that check the Empty chunkset */
		Empty,
		/** For propositions that check the Who chunkset */
		Who,
		/** For propositions that check the What chunkset */
		What
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public boolean hasNoTests()
	{
		return false;
	}
	
	/**
	 * Add mask for bits checked by this proposition to given chunkset
	 * @param chunkSet
	 */
	public abstract void addMaskTo(final ChunkSet chunkSet);
	
	/**
	 * @return State vector type this atomic proposition applies to.
	 */
	public abstract StateVectorTypes stateVectorType();
	
	/**
	 * @return Which site does this proposition look at?
	 */
	public abstract int testedSite();
	
	/**
	 * @return What value do we expect to (not) see?
	 */
	public abstract int value();
	
	/**
	 * @return Do we expect to NOT see the value returned by value()?
	 */
	public abstract boolean negated();
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param other
	 * @param game
	 * @return Does this proposition being true also prove the given other prop?
	 */
	public abstract boolean provesIfTrue(final AtomicProposition other, final Game game);
	
	/**
	 * @param other
	 * @param game
	 * @return Does this proposition being true disprove the given other prop?
	 */
	public abstract boolean disprovesIfTrue(final AtomicProposition other, final Game game);
	
	/**
	 * @param other
	 * @param game
	 * @return Does this proposition being false prove the given other prop?
	 */
	public abstract boolean provesIfFalse(final AtomicProposition other, final Game game);
	
	/**
	 * @param other
	 * @param game
	 * @return Does this proposition being false disprove the given other prop?
	 */
	public abstract boolean disprovesIfFalse(final AtomicProposition other, final Game game);
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param game
	 * @param player
	 * @return List of component IDs owned by given player
	 */
	public static TIntArrayList ownedComponentIDs(final Game game, final int player)
	{
		final TIntArrayList owned = new TIntArrayList();
		final Component[] components = game.equipment().components();
		
		for (int i = 0; i < components.length; ++i)
		{
			final Component comp = components[i];
			if (comp == null)
				continue;
			
			if (comp.owner() == player)
				owned.add(i);
		}
		
		return owned;
	}
	
	/**
	 * @param game
	 * @param compID
	 * @return True if and only if, in the given game, the owner of given comp ID doesn't own any other components
	 */
	public static boolean ownerOnlyOwns(final Game game, final int compID)
	{
		final Component[] components = game.equipment().components();
		final int owner = components[compID].owner();
		
		for (int i = 0; i < components.length; ++i)
		{
			if (i == compID)
				continue;
			
			final Component comp = components[i];
			if (comp == null)
				continue;
			
			if (comp.owner() == owner)
				return false;
		}
		
		return true;
	}
	
	/**
	 * @param game
	 * @param player
	 * @param compID
	 * @return True if and only if, in the given game, given player only owns the given component ID and no other components
	 */
	public static boolean playerOnlyOwns(final Game game, final int player, final int compID)
	{
		final Component[] components = game.equipment().components();
		if (components[compID].owner() != player)
			return false;
		
		for (int i = 0; i < components.length; ++i)
		{
			if (i == compID)
				continue;
			
			final Component comp = components[i];
			if (comp == null)
				continue;
			
			if (comp.owner() == player)
				return false;
		}
		
		return true;
	}
	
	//-------------------------------------------------------------------------

}
