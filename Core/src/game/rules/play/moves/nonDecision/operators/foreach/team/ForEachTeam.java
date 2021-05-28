package game.rules.play.moves.nonDecision.operators.foreach.team;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import gnu.trove.list.array.TIntArrayList;
import main.collections.FastArrayList;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.move.Move;

/**
 * Applies a move for each value from a value to another (included).
 * 
 * @author Eric.Piette
 */
@Hide
public final class ForEachTeam extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The moves to apply. */
	private final Moves generator;

	/**
	 * @param generator The move to apply.
	 * @param then      The moves applied after that move is applied.
	 */
	public ForEachTeam
	(
			 final Moves generator, 
		@Opt final Then  then
	)
	{
		super(then);
		this.generator = generator;
	}

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());
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
				final FastArrayList<Move> generatedMoves = generator.eval(context).moves();
				moves.moves().addAll(generatedMoves);
			}
		}
		if (then() != null)
			for (int j = 0; j < moves.moves().size(); j++)
				moves.moves().get(j).then().add(then().moves());

		context.setTeam(savedTeam);

		return moves;
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
		return generator.gameFlags(game) | super.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(generator.concepts(game));
		concepts.or(super.concepts(game));
		concepts.set(Concept.ControlFlowStatement.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(generator.writesEvalContextRecursive());
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
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(generator.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		super.preprocess(game);
		generator.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= generator.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= generator.willCrash(game);
		return willCrash;
	}
}
