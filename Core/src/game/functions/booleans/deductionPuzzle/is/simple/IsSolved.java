package game.functions.booleans.deductionPuzzle.is.simple;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanFunction;
import game.rules.play.moves.nonDecision.effect.Satisfy;
import game.types.board.SiteType;
import game.types.state.GameType;
import gnu.trove.list.array.TIntArrayList;
import other.concept.Concept;
import other.context.Context;
import other.context.TempContext;
import other.state.container.ContainerState;
import other.state.puzzle.ContainerDeductionPuzzleState;

/**
 * Returns true if all the variables of a deduction puzzle are set to value
 * satisfying all the constraints.
 * 
 * @author Eric.Piette
 * 
 * @remarks Works only for the ending condition of a deduction puzzle.
 */
@Hide
public final class IsSolved extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * 
	 */
	public IsSolved()
	{
		// Nothing to do.
	}

	@Override
	public boolean eval(final Context context)
	{
		final ContainerState ps = context.state().containerStates()[0];
		final TIntArrayList varsConstraints = context.game().constraintVariables();
		final BooleanFunction[] constraints = ((Satisfy) context.game().rules().phases()[0].play().moves()).constraints();
		final TIntArrayList notAssignedVars = new TIntArrayList();
		final SiteType type = context.board().defaultSite();
		
		for (int i = 0; i < varsConstraints.size(); i++)
		{
			final int var = varsConstraints.getQuick(i);
			if (!ps.isResolved(var, type))
				notAssignedVars.add(var);
		}
		
		// Check constraint
		final Context newContext = new TempContext(context);
		for (int i = 0; i < notAssignedVars.size(); i++)
			((ContainerDeductionPuzzleState) newContext.state().containerStates()[0]).set(notAssignedVars.getQuick(i), 0, type);
		
		boolean constraintOK = true;
		if (constraints != null)
			for (final BooleanFunction constraint : constraints)
				if (!constraint.eval(newContext))
				{
					constraintOK = false;
					break;
				}

		if (constraintOK)
			return true;
		else
			return false;
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
		return GameType.DeductionPuzzle;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (game.players().count() != 1)
		{
			game.addCrashToReport("The ludeme (is Solved) is used but the number of players is not 1.");
			willCrash = true;
		}
		return willCrash;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.DeductionPuzzle.id(), true);
		concepts.set(Concept.CopyContext.id(), true);
		concepts.set(Concept.SolvedEnd.id(), true);
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
		// Do nothing
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "the puzzle is solved";
	}
}
