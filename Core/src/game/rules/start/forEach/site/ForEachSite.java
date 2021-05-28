package game.rules.start.forEach.site;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.region.RegionFunction;
import game.rules.start.StartRule;
import gnu.trove.list.array.TIntArrayList;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;

/**
 * Applies a move for each value from a value to another (included).
 * 
 * @author Eric.Piette
 */
@Hide
public final class ForEachSite extends StartRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Region to filter. */
	private final RegionFunction region;

	/** Condition to check. */
	private final BooleanFunction condition;

	/** The starting rule to apply. */
	private final StartRule startRule;

	/**
	 * @param regionFn The original region.
	 * @param If     The condition to satisfy.
	 * @param startingRule The starting rule to apply.
	 */
	public ForEachSite
	(
		           final RegionFunction  regionFn,
		@Opt @Name final BooleanFunction If,
			       final StartRule       startingRule
	)
	{
		this.region = regionFn;
		this.condition = If == null ? new BooleanConstant(true) : If;
		this.startRule = startingRule;
	}

	@Override
	public void eval(final Context context)
	{
		
		final TIntArrayList sites = new TIntArrayList(region.eval(context).sites());
		final int originSiteValue = context.site();
		
		for (int i = 0; i < sites.size(); i++)
		{
			final int site = sites.getQuick(i);
			context.setSite(site);
			if (condition.eval(context))
				startRule.eval(context);
		}

		context.setSite(originSiteValue);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return condition.isStatic() && region.isStatic() && startRule.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		return condition.gameFlags(game) | region.gameFlags(game) | startRule.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(condition.concepts(game));
		concepts.or(startRule.concepts(game));
		concepts.or(region.concepts(game));

		concepts.set(Concept.ControlFlowStatement.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(condition.writesEvalContextRecursive());
		writeEvalContext.or(region.writesEvalContextRecursive());
		writeEvalContext.or(startRule.writesEvalContextRecursive());
		return writeEvalContext;
	}
	
	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.set(EvalContextData.Site.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(condition.readsEvalContextRecursive());
		readEvalContext.or(region.readsEvalContextRecursive());
		readEvalContext.or(startRule.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		condition.preprocess(game);
		region.preprocess(game);
		startRule.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= condition.missingRequirement(game);
		missingRequirement |= region.missingRequirement(game);
		missingRequirement |= startRule.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= condition.willCrash(game);
		willCrash |= region.willCrash(game);
		willCrash |= startRule.willCrash(game);
		return willCrash;
	}
}
