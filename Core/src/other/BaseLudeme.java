package other;

import java.util.BitSet;

import game.Game;

/**
 * BaseLudeme abstract class for all ludemes.
 * 
 * @author cambolbro and Eric.Piette
 */
public abstract class BaseLudeme implements Ludeme
{
	/**
	 * Default behaviour: English description not known for this ludeme.
	 * General toEnglish principles:
	 * - Every Ludeme should convert to an English description irrespective of its parent.
	 * - Parents are responsible for adding spaces either side of their children.
	 */
	@Override
	public String toEnglish(final Game game)
	{
		return "<" + this.getClass().getName() + ">";
	}

	@Override
	public BitSet concepts(final Game game)
	{
		return new BitSet();
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		return new BitSet();
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		return new BitSet();
	}
	
	@Override
	public BitSet readsEvalContextFlat()
	{
		return new BitSet();
	}

	@Override
	public BitSet writesEvalContextFlat()
	{
		return new BitSet();
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		return false;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		return false;
	}
}
