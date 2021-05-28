package game.rules.play.moves.nonDecision.operators.foreach.player;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.intArray.IntArrayFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Then;
import game.rules.play.moves.nonDecision.operator.Operator;
import main.collections.FastArrayList;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.move.Move;

/**
 * Iterates through the players, generating moves based on the indices of the
 * players.
 * 
 * @author Eric.Piette
 */
@Hide
public final class ForEachPlayer extends Operator
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The moves to generate according to the players. */
	private final Moves moves;
	
	/** The list of players. */
	private final IntArrayFunction playersFn;
	
	//-------------------------------------------------------------------------

	/**
	 * @param moves The moves.
	 * @param then  The moves applied after that move is applied.
	 */
	public ForEachPlayer
	(
			  final Moves moves, 
		@Opt  final Then  then
	)
	{
		super(then);

		this.moves = moves;
		this.playersFn = null;
	}
	
	/**
	 * @param players The list of players.
	 * @param moves   The moves.
	 * @param then    The moves applied after that move is applied.
	 */
	public ForEachPlayer
	(
			  final IntArrayFunction players,
			  final Moves moves, 
		@Opt  final Then then
	)
	{
		super(then);
		this.playersFn = players;
		this.moves = moves;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves movesToReturn = new BaseMoves(super.then());
		final int savedPlayer = context.player();

		if (playersFn == null)
		{
			for (int pid = 1; pid < context.game().players().size(); pid++)
			{
				context.setPlayer(pid);
				final FastArrayList<Move> generatedMoves = moves.eval(context).moves();
				movesToReturn.moves().addAll(generatedMoves);
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
				final FastArrayList<Move> generatedMoves = moves.eval(context).moves();
				movesToReturn.moves().addAll(generatedMoves);
			}
		}

		if (then() != null)
			for (int j = 0; j < moves.moves().size(); j++)
				movesToReturn.moves().get(j).then().add(then().moves());

		context.setPlayer(savedPlayer);

		return movesToReturn;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = moves.gameFlags(game) | super.gameFlags(game);

		if (playersFn != null)
			gameFlags |= playersFn.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

			return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		if (playersFn != null)
			concepts.or(playersFn.concepts(game));

		concepts.or(moves.concepts(game));
		concepts.set(Concept.ControlFlowStatement.id(), true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());

		if (playersFn != null)
			writeEvalContext.or(playersFn.writesEvalContextRecursive());

		writeEvalContext.or(moves.writesEvalContextRecursive());
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

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());

		if (playersFn != null)
			readEvalContext.or(playersFn.readsEvalContextRecursive());

		readEvalContext.or(moves.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);

		if (playersFn != null)
			missingRequirement |= (playersFn.missingRequirement(game));

		missingRequirement |= moves.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);

		if (playersFn != null)
			willCrash |= (playersFn.willCrash(game));

		willCrash |= moves.willCrash(game);
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
		super.preprocess(game);

		moves.preprocess(game);

		if (playersFn != null)
			playersFn.preprocess(game);
	}
}
