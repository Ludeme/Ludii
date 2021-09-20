package game.rules.start.deductionPuzzle;

import java.util.Arrays;
import java.util.BitSet;

import annotations.Opt;
import game.Game;
import game.rules.start.StartRule;
import game.types.board.SiteType;
import game.types.state.GameType;
import other.action.BaseAction;
import other.action.puzzle.ActionSet;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

/**
 * Sets a variable to a specified value in a deduction puzzle.
 * 
 * @author Eric.Piette and cambolbro
 * 
 * @remarks Applies to deduction puzzles.
 */
public final class Set extends StartRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	protected final Integer[] vars;
	protected final Integer[] values;
	protected final SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param type  The graph element type [Cell].
	 * @param pairs The first element of the pair is the index of the variable, the
	 *              second one the value of the variable.
	 * @example (set { {1 9} {6 4} {11 8} {12 5} {16 1} {20 1} {25 6} {26 8} {30 1}
	 *          {34 3} {40 4} {41 5} {42 7} {46 5} {50 7} {55 7} {58 9} {60 2} {65
	 *          3} {66 6} {72 8} })*/
	public Set
	(
		@Opt final SiteType     type,
			 final Integer[]... pairs
	)
	{
		if (pairs == null)
		{
			values = null;
			vars 	= null;
		}
		else
		{
			values = new Integer[pairs.length];
			vars   = new Integer[pairs.length];
			
			for (int n = 0; n < pairs.length; n++)
			{
				vars[n]   = pairs[n][0];
				values[n] = pairs[n][1];
			}
		}
		this.type = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public void eval(final Context context)
	{
		final SiteType realType = (type == null) ? context.board().defaultSite() : type;

		final int minSize = Math.min(vars.length, values.length);
		
		for (int i = 0 ; i < minSize ; i++) 
		{
			final BaseAction actionAtomic = new ActionSet(realType, vars[i].intValue(), values[i].intValue());
			actionAtomic.apply(context, true);
			context.trial().addMove(new Move(actionAtomic));
			context.trial().addInitPlacement();
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * @return vars
	 */

	public Integer[] vars()
	{
		return vars;
	}
	
	/**
	 * @return values
	 */

	public Integer[] values()
	{
		return values;
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
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.DeductionPuzzle.id(), true);
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
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (game.players().count() != 1)
		{
			game.addCrashToReport(
					"The ludeme (set ...) in the starting rules is used but the number of players is not 1.");
			willCrash = true;
		}
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		// Do nothing.
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString() 
	{
		String str = "(set ";
	
		final int minSize = Math.min(vars.length, values.length);
		for (int i = 0 ; i < minSize ; i++) 
				str += values[i] + " on " + vars[i] + " ";

		str+=")";
		return str;
	}

	@Override
	public boolean isSet()
	{
		return true;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "set the variables " + Arrays.toString(vars) + " to values " + Arrays.toString(values);
	}
	
	//-------------------------------------------------------------------------
		
}
