package game.functions.booleans.math;

import java.util.BitSet;

import annotations.Alias;
import annotations.Or;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.types.play.RoleType;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import other.concept.Concept;
import other.context.Context;

/**
 * Tests if valueA $\\neq$ valueB.
 * 
 * @author cambolbro and Eric.Piette
 */
@Alias(alias = "!=")
public final class NotEqual extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which value A. */
	private final IntFunction valueA;

	/** Which value B. */
	private final IntFunction valueB;

	/** Which region A. */
	private final RegionFunction regionA;

	/** Which region B. */
	private final RegionFunction regionB;

	/** Precomputed boolean. */
	private Boolean precomputedBoolean;

	//-------------------------------------------------------------------------

	/**
	 * For testing if two int functions are not equals. Also use to test if the
	 * index of a roletype is not equal to an int function.
	 * 
	 * @param valueA The first value.
	 * @param valueB The second value.
	 * @param roleB  The second owner value of this role.
	 * 
	 * @example (!= (mover) (next))
	 */
	public NotEqual
	(
			final IntFunction valueA,
		@Or final IntFunction valueB,
		@Or final RoleType roleB
	)
	{
		int numNonNull2 = 0;
		if (valueB != null)
			numNonNull2++;
		if (roleB != null)
			numNonNull2++;

		if (numNonNull2 != 1)
			throw new IllegalArgumentException("Only one Or2 should be non-null.");

		this.valueA = valueA;
		this.valueB = (valueB != null) ? valueB : RoleType.toIntFunction(roleB);
		regionA = null;
		regionB = null;
	}

	/**
	 * For test if two regions are not equals.
	 * 
	 * @param regionA The left value.
	 * @param regionB The right value.
	 * 
	 * @example (!= (sites Occupied by:Mover) (sites Mover))
	 */
	public NotEqual
	(
		final RegionFunction regionA,
		final RegionFunction regionB
	)
	{
		valueA = null;
		valueB = null;
		this.regionA = regionA;
		this.regionB = regionB;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		if (precomputedBoolean != null)
			return precomputedBoolean.booleanValue();
		
		if (regionA == null)
		{
			return valueA.eval(context) != valueB.eval(context);
		}
		else
		{
			final Region rA = regionA.eval(context);
			final Region rB = regionB.eval(context);
			final TIntArrayList listA = new TIntArrayList(rA.sites());
			final TIntArrayList listB = new TIntArrayList(rB.sites());
			
			if (listA.size() != listB.size())
				return true;
			
			for (int i = 0; i < listA.size(); i++)
			{
				final int siteA = listA.getQuick(i);
				if (!listB.contains(siteA))
					return true;
			}
			
			return false;
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The first value.
	 */
	public IntFunction valueA()
	{
		return valueA;
	}

	/**
	 * @return The second value.
	 */
	public IntFunction valueB()
	{
		return valueB;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		String str = "";
		if (regionA == null)
			str += "NotEqual(" + valueA + ", " + valueB + ")";
		else
			str += "NotEqual(" + regionA + ", " + regionB + ")";
		return str;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		if (regionA == null)
			return valueA.isStatic() && valueB.isStatic();
		else
			return regionA.isStatic() && regionB.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		if (regionA == null)
			return valueA.gameFlags(game) | valueB.gameFlags(game);
		else
			return regionA.gameFlags(game) | regionB.gameFlags(game);
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		if (regionA == null)
		{
			concepts.or(valueA.concepts(game));
			concepts.or(valueB.concepts(game));
		}
		else
		{
			concepts.or(regionA.concepts(game));
			concepts.or(regionB.concepts(game));
		}

		concepts.set(Concept.NotEqual.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (regionA == null)
		{
			writeEvalContext.or(valueA.writesEvalContextRecursive());
			writeEvalContext.or(valueB.writesEvalContextRecursive());
		}
		else
		{
			writeEvalContext.or(regionA.writesEvalContextRecursive());
			writeEvalContext.or(regionB.writesEvalContextRecursive());
		}
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (regionA == null)
		{
			readEvalContext.or(valueA.readsEvalContextRecursive());
			readEvalContext.or(valueB.readsEvalContextRecursive());
		}
		else
		{
			readEvalContext.or(regionA.readsEvalContextRecursive());
			readEvalContext.or(regionB.readsEvalContextRecursive());
		}
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (regionA == null)
		{
			missingRequirement |= valueA.missingRequirement(game);
			missingRequirement |= valueB.missingRequirement(game);
		}
		else
		{
			missingRequirement |= regionA.missingRequirement(game);
			missingRequirement |= regionB.missingRequirement(game);
		}
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (regionA == null)
		{
			willCrash |= valueA.willCrash(game);
			willCrash |= valueB.willCrash(game);
		}
		else
		{
			willCrash |= regionA.willCrash(game);
			willCrash |= regionB.willCrash(game);
		}
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (regionA == null)
		{
			valueA.preprocess(game);
			valueB.preprocess(game);
		}
		else
		{
			regionA.preprocess(game);
			regionB.preprocess(game);
		}
		
		if (isStatic())
			precomputedBoolean = Boolean.valueOf(eval(new Context(game, null)));
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		String valueAEnglish = "null";
		String valueBEnglish = "null";
		
		if (valueA != null)
			valueAEnglish = valueA.toEnglish(game);
		if (valueB != null)
			valueBEnglish = valueB.toEnglish(game);
		
		return valueAEnglish + " is not equal to " + valueBEnglish;
	}
}
