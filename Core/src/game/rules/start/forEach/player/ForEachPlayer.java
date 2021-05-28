package game.rules.start.forEach.player;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.intArray.IntArrayFunction;
import game.rules.start.StartRule;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;

/**
 * Applies a move for each value from a value to another (included).
 * 
 * @author Eric.Piette
 */
@Hide
public final class ForEachPlayer extends StartRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The starting rule to apply. */
	private final StartRule startRule;
	
	/** The list of players. */
	private final IntArrayFunction playersFn;

	/**
	 * @param startRule The starting rule to apply.
	 */
	public ForEachPlayer
	(
       final StartRule startRule
	)
	{
		this.startRule = startRule;
		this.playersFn = null;
	}
	
	/**
	 * @param players   The list of players.
	 * @param startRule The starting rule to apply.
	 */
	public ForEachPlayer
	(
	     final IntArrayFunction players,
	     final StartRule        startRule
	)
	{
		this.playersFn = players;
		this.startRule = startRule;
	}

	@Override
	public void eval(final Context context)
	{
		final int savedPlayer = context.player();

		if (playersFn == null)
		{
			for (int pid = 1; pid < context.game().players().size(); pid++)
			{
				context.setPlayer(pid);
				startRule.eval(context);
			}
		}
		else
		{
			final int[] players = playersFn.eval(context);
			for (int i = 0 ; i < players.length ;i++)
			{
				final int pid = players[i];

				if (pid < 0 || pid > context.game().players().size())
					continue;

				context.setPlayer(pid);
				startRule.eval(context);
			}
		}

		context.setPlayer(savedPlayer);
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = startRule.gameFlags(game);

		if (playersFn != null)
			gameFlags |= playersFn.gameFlags(game);

			return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));

		if (playersFn != null)
			concepts.or(playersFn.concepts(game));

		concepts.or(startRule.concepts(game));
		concepts.set(Concept.ControlFlowStatement.id(), true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();

		if (playersFn != null)
			writeEvalContext.or(playersFn.writesEvalContextRecursive());

		writeEvalContext.or(startRule.writesEvalContextRecursive());
		return writeEvalContext;
	}
	
	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.set(EvalContextData.Player.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());

		if (playersFn != null)
			readEvalContext.or(playersFn.readsEvalContextRecursive());

		readEvalContext.or(startRule.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);

		if (playersFn != null)
			missingRequirement |= (playersFn.missingRequirement(game));

		missingRequirement |= startRule.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);

		if (playersFn != null)
			willCrash |= (playersFn.willCrash(game));

		willCrash |= startRule.willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public void preprocess(final Game game)
	{
		startRule.preprocess(game);

		if (playersFn != null)
			playersFn.preprocess(game);
	}
}
