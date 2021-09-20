package game.functions.ints.iterator;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.types.board.SiteType;
import main.Constants;
import other.context.Context;
import other.context.EvalContextData;

/**
 * Returns the ``hint'' value of the context.
 * 
 * @author Eric.Piette and cambolbro
 * 
 * @remarks This ludeme identifies the hint position of a deduction puzzle
 *          stored in the context.
 */
public final class Hint extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * The type of the site.
	 */
	final SiteType type;

	/**
	 * The site to look the hint.
	 */
	final IntFunction siteFn;

	/**
	 * @param type The type of the site to look.
	 * @param at   The index of the site.
	 * @example (hint)
	 */
	public Hint
	(
		      @Opt final SiteType type, 
		@Name @Opt final IntFunction at
	)
	{	
		this.type = type;
		siteFn = at;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		if (siteFn == null)
			return context.hint();
		else
		{
			final int site = siteFn.eval(context);
			Integer[][] regions = null;
			Integer[] hints = null;
			switch(type)
			{
			case Edge:
				regions = context.game().equipment().edgesWithHints();
				hints = context.game().equipment().edgeHints();
				break;
			case Vertex:
				regions = context.game().equipment().verticesWithHints();
				hints = context.game().equipment().vertexHints();
				break;
			case Cell:
				regions = context.game().equipment().cellsWithHints();
				hints = context.game().equipment().cellHints();
				break;
			}
			
			if (regions == null || hints == null)
				return Constants.UNDEFINED;

			for (int i = 0; i < Math.min(hints.length, regions.length); i++)
			{
				if (regions[i][0].intValue() == site)
					return hints[i].intValue();
			}
			return Constants.UNDEFINED;
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		return readsEvalContextFlat();
	}
	
	@Override
	public BitSet readsEvalContextFlat()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.set(EvalContextData.Hint.id(), true);
		return readEvalContext;
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flags = 0l;

		if (siteFn != null)
			flags |= siteFn.gameFlags(game);

		flags |= SiteType.gameFlags(type);

		return flags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		if (siteFn != null)
			concepts.or(siteFn.concepts(game));

		return concepts;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (siteFn != null)
			siteFn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (siteFn != null)
			missingRequirement |= siteFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (game.players().count() != 1)
		{
			game.addCrashToReport("The ludeme (hint ...) is used but the number of players is not 1.");
			willCrash = true;
		}
		if (siteFn != null)
			willCrash |= siteFn.willCrash(game);
		return willCrash;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "Hint()";
	}

	@Override
	public boolean isHint()
	{
		return true;
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "current hint";
	}
}
