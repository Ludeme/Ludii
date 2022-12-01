package game.functions.booleans.math;

import java.util.BitSet;

import annotations.Alias;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.count.component.CountPieces;
import game.types.board.SiteType;
import game.types.play.RoleType;
import other.concept.Concept;
import other.context.Context;
import other.state.puzzle.ContainerDeductionPuzzleState;

/**
 * Tests if valueA $\\leq$ valueB.
 * 
 * @author Eric.Piette
 * 
  */
@Alias(alias = "<=")
public final class Le extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which value A. */
	private final IntFunction valueA;

	/** Which value B. */
	private final IntFunction valueB;

	/** Precomputed boolean. */
	private Boolean precomputedBoolean;

	//-------------------------------------------------------------------------

	/**
	 * @param valueA The left value.
	 * @param valueB The right value.
	 * @example (<= (mover) (next))
	 */
	public Le
	(
		final IntFunction valueA,
		final IntFunction valueB
	)
	{
		this.valueA = valueA;
		this.valueB = valueB;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		if (precomputedBoolean != null)
			return precomputedBoolean.booleanValue();
		
		if (!context.game().isDeductionPuzzle())
		{
			return valueA.eval(context) <= valueB.eval(context);
		}
		else
		{
			final ContainerDeductionPuzzleState ps = ((ContainerDeductionPuzzleState) context.state().containerStates()[0]);
			final SiteType type = context.board().defaultSite();
			final int indexA = valueA.eval(context);
			final int indexB = valueB.eval(context);

			if (!ps.isResolved(indexA, type) || !ps.isResolved(indexB, type))
				return true;

			final int vA = ps.what(indexA, type);
			final int vB = ps.what(indexB, type);
			return vA <= vB;
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
		str += "LesserThanOrEqual(" + valueA + ", " + valueB + ")";
		return str;
	}

	@Override
	public boolean isStatic()
	{
		return valueA.isStatic() && valueB.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		return valueA.gameFlags(game) | valueB.gameFlags(game);
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(valueA.concepts(game));
		concepts.or(valueB.concepts(game));
		concepts.set(Concept.LesserThanOrEqual.id(), true);
		
		if (valueA instanceof CountPieces)
		{
			concepts.set(Concept.CountPiecesComparison.id(), true);
			final CountPieces countPieces = (CountPieces) valueA;
			if(countPieces.roleType() != null)
			{
				if(countPieces.roleType().equals(RoleType.Mover))
					concepts.set(Concept.CountPiecesMoverComparison.id(), true);
				else if(countPieces.roleType().equals(RoleType.Next) || countPieces.roleType().equals(RoleType.Player))
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
				else if(countPieces.roleType().equals(RoleType.Next) || countPieces.roleType().equals(RoleType.Player))
					concepts.set(Concept.CountPiecesNextComparison.id(), true);
			}
		}
		
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(valueA.writesEvalContextRecursive());
		writeEvalContext.or(valueB.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(valueA.readsEvalContextRecursive());
		readEvalContext.or(valueB.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		valueA.preprocess(game);
		valueB.preprocess(game);

		if (isStatic())
			precomputedBoolean = Boolean.valueOf(eval(new Context(game, null)));
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= valueA.missingRequirement(game);
		missingRequirement |= valueB.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= valueA.willCrash(game);
		willCrash |= valueB.willCrash(game);
		return willCrash;
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
		
		return valueAEnglish + " is less than or equal to " + valueBEnglish;
	}
}
