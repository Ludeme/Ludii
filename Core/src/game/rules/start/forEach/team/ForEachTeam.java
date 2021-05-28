package game.rules.start.forEach.team;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
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
public final class ForEachTeam extends StartRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The starting rule to apply. */
	private final StartRule startRule;

	/**
	 * @param startingRule The starting rule to apply.
	 */
	public ForEachTeam
	(
		final StartRule startingRule
	)
	{
		this.startRule = startingRule;
	}

	@Override
	public void eval(final Context context)
	{
		final int[] savedTeam = context.team();

		for (int tid = 1; tid < context.game().players().size(); tid++)
		{
			final TIntArrayList team = new TIntArrayList();
			for (int pid = 1; pid < context.game().players().size(); pid++)
			{
				if (context.state().playerInTeam(pid, tid))
					team.add(pid);
			}
			if (!team.isEmpty())
			{
				context.setTeam(team.toArray());
				startRule.eval(context);
			}
		}
		context.setTeam(savedTeam);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return startRule.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		return startRule.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(startRule.concepts(game));
		concepts.set(Concept.ControlFlowStatement.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(startRule.writesEvalContextRecursive());
		return writeEvalContext;
	}
	
	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.set(EvalContextData.Team.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(startRule.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		startRule.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= startRule.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= startRule.willCrash(game);
		return willCrash;
	}
}
