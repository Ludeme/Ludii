package game.functions.booleans.is.simple;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.types.state.GameType;
import gnu.trove.list.array.TLongArrayList;
import other.context.Context;
import other.trial.Trial;

/**
 * Returns true if the game is repeating the same set of states three times with
 * exactly the same moves during these states.
 * 
 * @author Eric.Piette
 */
@Hide
public final class IsCycle extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * 
	 */
	public IsCycle()
	{
		// Nothing to do.
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		final Trial trial = context.trial();
		final TLongArrayList previousStates = trial.previousState();
		final int sizeCycle = context.game().players().count() * context.game().players().count();
		
		if(previousStates.size() < 3 * sizeCycle)
			return false;
		
		final TLongArrayList cycleToCheck = new TLongArrayList();
		int index = previousStates.size() - 1;
		for(; index > (previousStates.size() - 1) - sizeCycle; index--)
			cycleToCheck.add(previousStates.get(index));
		
		// Check one loop
		int cycleIndex = 0;
		for(; index > (previousStates.size() - 1) - (sizeCycle * 2); index--)
		{
			if(previousStates.get(index) != cycleToCheck.get(cycleIndex))
				return false;
			cycleIndex++;
		}
		
		// Check second loop
		cycleIndex = 0;
		for(; index > (previousStates.size() - 1) - (sizeCycle * 3); index--)
		{
			if(previousStates.get(index) != cycleToCheck.get(cycleIndex))
				return false;
			cycleIndex++;
		}
		
		return true;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "AllPass()";
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = 0l;
		gameFlags |= GameType.CycleDetection;
		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		// nothing to do
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "we have repeated the same state three times";
	}
	
	//-------------------------------------------------------------------------
	
}
