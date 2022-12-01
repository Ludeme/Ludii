package game.functions.booleans.math;

import java.util.BitSet;

import annotations.Alias;
import annotations.Or;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.ints.count.component.CountPieces;
import game.functions.ints.state.Counter;
import game.functions.region.RegionFunction;
import game.types.play.RoleType;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Tests if valueA = valueB, if all the integers in the list are equals, or if
 * the result of the two regions functions are equals.
 * 
 * @author Eric.Piette and cambolbro
 */
@Alias(alias = "=")
public final class Equals extends BaseBooleanFunction
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
	 * For testing if two int functions are equals. Also use to test if the index of
	 * a roletype is equal to an int function.
	 * 
	 * @param valueA The first value.
	 * @param valueB The second value.
	 * @param roleB  The second owner value of this role.
	 * 
	 * @example (= (mover) 1)
	 */
	public Equals
	(
		    final IntFunction valueA,
		@Or final IntFunction valueB,
		@Or final RoleType    roleB
	)
	{
		int numNonNull2 = 0;
		if (valueB != null)
			numNonNull2++;
		if (roleB != null)
			numNonNull2++;

		if (numNonNull2 != 1)
			throw new IllegalArgumentException("Only one Or should be non-null.");

		this.valueA = valueA;
		this.valueB = (valueB != null) ? valueB : RoleType.toIntFunction(roleB);
		regionA = null;
		regionB = null;
	}

	/**
	 * For test if two regions are equals.
	 * 
	 * @param regionA The first region function.
	 * @param regionB The second region function.
	 * 
	 * @example (= (sites Occupied by:Mover) (sites Next))
	 */
	public Equals
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
			return valueA.eval(context) == valueB.eval(context);
		}
		else
		{
			final Region rA = regionA.eval(context);
			final Region rB = regionB.eval(context);
			final TIntArrayList listA = new TIntArrayList(rA.sites());
			final TIntArrayList listB = new TIntArrayList(rB.sites());
			
			if (listA.size() != listB.size())
				return false;
			
			for (int i = 0; i < listA.size(); i++)
			{
				final int siteA = listA.getQuick(i);
				if (!listB.contains(siteA))
					return false;
			}
			
			return true;
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
			str += "Equal(" + valueA + ", " + valueB + ")";
		else
			str += "Equal(" + regionA + ", " + regionB + ")";
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

		final Trial trial = new Trial(game);
		if (valueA instanceof CountPieces && valueB instanceof IntConstant
				&& 0 == valueB.eval(new Context(game, trial)))
		{
			concepts.set(Concept.NoPiece.id(), true);
			final CountPieces countPieces = (CountPieces) valueA;
			if(countPieces.roleType() != null)
			{
				if(countPieces.roleType().equals(RoleType.Mover))
					concepts.set(Concept.NoPieceMover.id(), true);
				else if(countPieces.roleType().equals(RoleType.Next))
					concepts.set(Concept.NoPieceNext.id(), true);
			}
		}
		else if (valueB instanceof CountPieces && valueA instanceof IntConstant
				&& 0 == valueA.eval(new Context(game, trial)))
		{
			concepts.set(Concept.NoPiece.id(), true);
			final CountPieces countPieces = (CountPieces) valueB;
			if(countPieces.roleType() != null)
			{
				if(countPieces.roleType().equals(RoleType.Mover))
					concepts.set(Concept.NoPieceMover.id(), true);
				else if(countPieces.roleType().equals(RoleType.Next))
					concepts.set(Concept.NoPieceNext.id(), true);
			}
		}
		
		if (valueA instanceof CountPieces)
		{
			concepts.set(Concept.CountPiecesComparison.id(), true);
			final CountPieces countPieces = (CountPieces) valueA;
			if(countPieces.roleType() != null)
			{
				if(countPieces.roleType().equals(RoleType.Mover))
					concepts.set(Concept.CountPiecesMoverComparison.id(), true);
				else if(countPieces.roleType().equals(RoleType.Next))
					concepts.set(Concept.CountPiecesNextComparison.id(), true);
			}
		}
		else if (valueB instanceof CountPieces)
		{
			concepts.set(Concept.CountPiecesComparison.id(), true);
			final CountPieces countPieces = (CountPieces) valueB;
			if(countPieces.roleType() != null)
			{
				if(countPieces.roleType().equals(RoleType.Mover))
					concepts.set(Concept.CountPiecesMoverComparison.id(), true);
				else if(countPieces.roleType().equals(RoleType.Next))
				concepts.set(Concept.CountPiecesNextComparison.id(), true);
			}
		}

		if (valueA instanceof Counter && valueB instanceof IntConstant)
			concepts.set(Concept.ProgressCheck.id(), true);
		else if (valueB instanceof Counter && valueA instanceof IntConstant)
			concepts.set(Concept.ProgressCheck.id(), true);

		if (valueA != null && valueB != null)
		{
			if (valueA.getClass().toString().contains("Where") && valueB instanceof IntConstant
					&& Constants.OFF == valueB.eval(new Context(game, trial)))
				concepts.set(Concept.NoTargetPiece.id(), true);
			else if (valueB.getClass().toString().contains("Where") && valueA instanceof IntConstant
					&& Constants.OFF == valueA.eval(new Context(game, trial)))
				concepts.set(Concept.NoTargetPiece.id(), true);
			else if (valueA.getClass().toString().contains("Where") && valueB.getClass().toString().contains("MapEntry"))
				concepts.set(Concept.Contains.id(), true);
			else if (valueB.getClass().toString().contains("Where") && valueA.getClass().toString().contains("MapEntry"))
				concepts.set(Concept.Contains.id(), true);
			else if (valueB.getClass().toString().contains("What") && valueA.getClass().toString().contains("Id"))
					concepts.set(Concept.IsPieceAt.id(), true);
			else if (valueA.getClass().toString().contains("What") && valueB.getClass().toString().contains("Id"))
				concepts.set(Concept.IsPieceAt.id(), true);
		}

		if (regionA != null && regionB != null)
			concepts.set(Concept.Fill.id(), true);

		concepts.set(Concept.Equal.id(), true);

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
		
		return valueAEnglish + " is equal to " + valueBEnglish;
	}
}
